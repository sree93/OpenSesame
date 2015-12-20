package com.sreemenon.opensesame;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.sreemenon.crypt.Lamp;
import com.sreemenon.crypt.Crypt;
import com.sreemenon.sqlite.DBTransactions;

import net.sqlcipher.Cursor;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class HomeFragment extends Fragment {
    private RecyclerView rvList;
    private FloatingActionButton fab;

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        rvList = (RecyclerView)view.findViewById(R.id.rvList);

        rvList.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        rvList.setLayoutManager(mLayoutManager);

        rvList.setAdapter(new RvListAdapter(((MainActivity)getActivity()).getDataItemList()));

        fab = (FloatingActionButton)view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setEditPosition(-1);
                ((MainActivity)getActivity()).switchFragment(R.layout.fragment_add);
            }
        });


        return view;
    }

    public void refreshAdapter(){
        rvList.setAdapter(new RvListAdapter(((MainActivity)getActivity()).getDataItemList()));
    }

    private class RvListAdapter extends RecyclerView.Adapter<RvListAdapter.RvListViewHolder>{

        private List<DataItem> dataItemList;

        public RvListAdapter(List<DataItem> dataItemList) {
            this.dataItemList = dataItemList;
        }

        @Override
        public RvListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.home_list_row,parent, false);
            RvListViewHolder viewHolder = new RvListViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RvListViewHolder holder, int position) {
            holder.tvRowUname.setText(dataItemList.get(position).getUname());
            holder.tvRowWebsite.setText(dataItemList.get(position).getWebsite());
        }

        @Override
        public int getItemCount() {
            return dataItemList.size();
        }

        public class RvListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

            TextView tvRowWebsite;
            TextView tvRowUname;
            ImageButton imgbtnMore;
            public RvListViewHolder(View itemView) {
                super(itemView);

                itemView.setOnClickListener(this);

                tvRowWebsite = (TextView)itemView.findViewById(R.id.tvRowWebsite);
                tvRowUname = (TextView)itemView.findViewById(R.id.tvRowUname);
                imgbtnMore = (ImageButton)itemView.findViewById(R.id.imgbtnMore);

                imgbtnMore.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
               if(v == itemView){
                   int position = getAdapterPosition();
                   DecryptPassword decryptPassword = new DecryptPassword();
                   decryptPassword.execute(dataItemList.get(position).getWebsite(), dataItemList.get(position).getUname(), dataItemList.get(position).getSalt());
               }else if(v == imgbtnMore){
                   PopupMenu popup = new PopupMenu(v.getContext(), v);
                   popup.inflate(R.menu.menu_rv_list_row);
                   popup.setOnMenuItemClickListener(this);
                   popup.show();
               }
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int menuId = item.getItemId();

                DialogInterface.OnClickListener deleteConfirmListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if  (which == DialogInterface.BUTTON_POSITIVE){
                            DeleteEntry deleteEntry = new DeleteEntry();
                            deleteEntry.execute(getAdapterPosition());
                        }
                    }
                };

                switch (menuId){
                    case R.id.action_edit:
                        ((MainActivity)getActivity()).setEditPosition(getAdapterPosition());
                        ((MainActivity)getActivity()).switchFragment(R.layout.fragment_add);
                        return true;
                    case R.id.action_delete:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setPositiveButton("Confirm", deleteConfirmListener);
                        builder.setNegativeButton("Cancel", deleteConfirmListener);
                        builder.setTitle("Confirm Delete!");
                        builder.setMessage("Are you sure you want to delete this Entry?");
                        builder.show();
                        return true;
                }

                return false;
            }
        }
    }

    private class DeleteEntry extends AsyncTask<Integer,Void,Void>{
        DialogFragment dialogFragment = new CustomProgressDialog();

        DataItem item;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Bundle args = new Bundle();
            args.putString("message","Opening Vault!!!");
            dialogFragment.setArguments(args);
            dialogFragment.show(getFragmentManager(), "Progress Dialog");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((MainActivity)getActivity()).deleteFromDataItemList(item.getId());
            dialogFragment.dismiss();
        }

        @Override
        protected Void doInBackground(Integer... params) {
            item = ((MainActivity)getActivity()).getDataItemList().get(params[0]);
            DBTransactions transactions = new DBTransactions(getContext());
            transactions.deleteId(String.valueOf(item.getId()));
            transactions.closeDB();
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null, null);

                if(keyStore.containsAlias(item.getWebsite() + item.getUname())){
                    keyStore.deleteEntry(item.getWebsite() + item.getUname());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    private class DecryptPassword extends AsyncTask<String, Void, String>{
        DialogFragment dialogFragment = new CustomProgressDialog();

        @Override
        protected String doInBackground(String... params) {
            String ticket = "";
            String genie = "";
            String salt = params[2];
            String website = params[0];
            String uname = params[1];

            DBTransactions transactions = new DBTransactions(getContext());
            Cursor cursor = transactions.getCursor(false, "gems", new String[]{"ticket", "genie"}, "website=? AND uname=?", new String[]{website, uname}, null, null, null, null);
            if(cursor.moveToFirst()){
                ticket = cursor.getString(cursor.getColumnIndex("ticket"));
                genie = cursor.getString(cursor.getColumnIndex("genie"));
            }
            cursor.close();
            transactions.closeDB();
            String result = null;
            try {
                String key = String.valueOf(Lamp.decryptGenie(website+uname, genie));
                Crypt sreeCrypt = Crypt.getDefault(key, salt, new byte[16]);

                result = sreeCrypt.decrypt(ticket);
            }catch(KeyStoreException | NoSuchProviderException | IllegalBlockSizeException | NoSuchAlgorithmException | UnrecoverableEntryException | IOException | CertificateException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException e) {
                e.printStackTrace();
                this.cancel(false);
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Bundle args = new Bundle();
            args.putString("message","Opening Vault!!!");
            dialogFragment.setArguments(args);
            dialogFragment.show(getFragmentManager(),"Progress Dialog");
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialogFragment.dismiss();
            Bundle args = new Bundle();
            args.putString("pass", s);

            DialogFragment passDialog = new PasswordDialogFragment();
            passDialog.setArguments(args);
            passDialog.show(getFragmentManager(),"DialogFragment");
        }
    }
}

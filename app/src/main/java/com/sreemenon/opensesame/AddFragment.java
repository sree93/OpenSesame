package com.sreemenon.opensesame;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.sreemenon.crypt.Crypt;
import com.sreemenon.sqlite.DBTransactions;

import net.sqlcipher.Cursor;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddFragment extends Fragment {

    private AutoCompleteTextView actvWebsite;
    private EditText etUname;
    private Switch hasNum;
    private  Switch hasSpl;
    private Switch hasUpper;
    private NumberPicker npCharCount;

    private boolean isEdit;
    private DataItem item;

    private EditText etPass;
    private ImageButton btnGenPas;

    public static AddFragment newInstance() {
        AddFragment fragment = new AddFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isEdit = false;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        actvWebsite = (AutoCompleteTextView)view.findViewById(R.id.actvWebsite);
        etUname = (EditText)view.findViewById(R.id.etUname);
        hasNum = (Switch)view.findViewById(R.id.switchNumeric);
        hasSpl = (Switch)view.findViewById(R.id.switchSpl);
        hasUpper = (Switch)view.findViewById(R.id.switchUpper);
        npCharCount = (NumberPicker)view.findViewById(R.id.npCharCount);


        npCharCount.setMaxValue(15);
        npCharCount.setMinValue(7);
        npCharCount.setValue(15);

        etPass = (EditText)view.findViewById(R.id.etPass);
        etPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() != 0) {
                    Pattern numPattern = Pattern.compile("(.*)(\\d+)(.*)");
                    Matcher numMatcher = numPattern.matcher(s);

                    hasNum.setChecked(numMatcher.matches());

                    Pattern upperPattern = Pattern.compile("(.*)([A-Z])(.*)");
                    Matcher upperMatcher = upperPattern.matcher(s);

                    hasUpper.setChecked(upperMatcher.matches());

                    Pattern splPattern = Pattern.compile("(.*)([^A-Za-z0-9])(.*)");
                    Matcher splMathcer = splPattern.matcher(s);

                    hasSpl.setChecked(splMathcer.matches());
                }else {
                    hasNum.setChecked(true);
                    hasSpl.setChecked(true);
                    hasUpper.setChecked(true);
                }
            }
        });

        btnGenPas = (ImageButton)view.findViewById(R.id.btnGenPass);
        btnGenPas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean boolSpl = hasSpl.isChecked();
                boolean boolUpper = hasUpper.isChecked();
                boolean boolNum = hasNum.isChecked();

                String lower = "abcdefghijklmnopqrstuvwxyz";
                String upper = lower.toUpperCase();
                String numbers = "1234567890";
                String splChars = "@%+\\/'!#$^?:,(){}[]~-_";

                StringBuilder baseChars = new StringBuilder(lower);

                if (boolNum)
                    baseChars.append(numbers);
                if (boolSpl)
                    baseChars.append(splChars);
                if (boolUpper)
                    baseChars.append(upper);
                if (boolNum)
                    baseChars.append(numbers);

                StringBuilder preTicket;
                Random random = new Random();
                do {
                    preTicket = new StringBuilder();
                    for (int i = 0; i < npCharCount.getValue(); i++) {
                        preTicket.append(baseChars.charAt(random.nextInt(baseChars.length())));
                    }
                    etPass.setText(preTicket.toString());
                }
                while (!(boolSpl == hasSpl.isChecked() && boolUpper == hasUpper.isChecked() && boolNum == hasNum.isChecked()));
            }
        });

        List<DataItem> dataItemList = ((MainActivity)getActivity()).getDataItemList();

        List<String> websiteList = new ArrayList<>();
        for (DataItem dataItem: dataItemList) {
            String website = dataItem.getWebsite();
            if(!websiteList.contains(website)){
                websiteList.add(website);
            }
        }
        ArrayAdapter<String> websiteAdapter = new ArrayAdapter<String>(AddFragment.this.getActivity(), R.layout.spinner_layout, R.id.tvSpinnerItem, websiteList);
        actvWebsite.setAdapter(websiteAdapter);

        int editPosition = ((MainActivity)getActivity()).getEditPosition();
        if(editPosition != -1){
            isEdit = true;
            item = dataItemList.get(editPosition);
            actvWebsite.setText(item.getWebsite());
            etUname.setText(item.getUname());
            DecryptPassword decryptPassword = new DecryptPassword();
            decryptPassword.execute(item.getWebsite(), item.getUname(), item.getSalt());
        }

        return view;
    }

    public void saveNewPassword() {
        if(isEdit){
            DialogInterface.OnClickListener deleteConfirmListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch(which){
                        case DialogInterface.BUTTON_POSITIVE:
                            DataItem currRow = new DataItem(actvWebsite.getText().toString(), etUname.getText().toString(), hasSpl.isChecked(), hasNum.isChecked(), hasUpper.isChecked(), AddFragment.this);
                            currRow.setId(item.getId());
                            currRow.updateRow(etPass.getText().toString());
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            ((MainActivity)getActivity()).switchFragment(R.layout.fragment_main);
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setPositiveButton("Confirm", deleteConfirmListener);
            builder.setNegativeButton("Cancel", deleteConfirmListener);
            builder.setTitle("Confirm!");
            builder.setMessage("Are you sure you want to edit this Entry?");
            builder.show();

        }else {
            DataItem currRow = new DataItem(actvWebsite.getText().toString(), etUname.getText().toString(), hasSpl.isChecked(), hasNum.isChecked(), hasUpper.isChecked(), AddFragment.this);
            currRow.insertRow(etPass.getText().toString());
        }
    }

    private class DecryptPassword extends AsyncTask<String, Void, String>{
        DialogFragment dialogFragment = new CustomProgressDialog();

        @Override
        protected String doInBackground(String... params) {
            String salt, website, uname;
            String ticket = "";
            salt = params[2];
            website = params[0];
            uname = params[1];

            DBTransactions transactions = new DBTransactions(getContext());
            Cursor cursor = transactions.getCursor(false, "gems", new String[]{"ticket", "salt"}, "website=? AND uname=?", new String[]{website, uname}, null, null, null, null);
            if(cursor.moveToFirst()){
                ticket = cursor.getString(cursor.getColumnIndex("ticket"));
            }
            cursor.close();
            transactions.closeDB();

            KeyStore keyStore;
            String result = null;
            try {
                keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null, null);
                KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(website+uname, null);
                RSAPrivateKey privKey = (RSAPrivateKey) keyEntry.getPrivateKey();

                String key = String.valueOf(privKey.getModulus());

                Crypt sreeCrypt = Crypt.getDefault(key, salt, new byte[16]);

                result = sreeCrypt.decryptOrNull(ticket);
            }catch(KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | IOException e) {
                e.printStackTrace();
                this.cancel(false);
            } catch (CertificateException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Bundle args = new Bundle();
            args.putString("message","Opening Vault!!!");
            dialogFragment.setArguments(args);
            dialogFragment.show(getFragmentManager(), "Progress Dialog");

        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialogFragment.dismiss();
            etPass.setText(s);
        }
    }
}

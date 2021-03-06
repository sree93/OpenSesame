package com.sreemenon.opensesame;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.sreemenon.crypt.Lamp;
import com.sreemenon.crypt.Crypt;
import com.sreemenon.sqlite.DBTransactions;

import net.sqlcipher.Cursor;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Sree on 12/12/2015.
 *
 * A container class for database data and actions
 */
public class DataItem implements Serializable{
    private int id;
    private String website;
    private String uname;
    private String salt;
    private String genie;
    private boolean spl;
    private boolean num;
    private boolean ucase;
    private boolean isRead;

    private AddFragment parent;

    /**
     * Constructor to create a new DataItem
     *
     * @param website website name
     * @param uname username
     * @param spl can contain spl chars in password
     * @param num can contain numeric chars in password
     * @param ucase can contain uppercase chars in password
     * @param parent AddFragment instance to create progress dialogs and handle context calls
     */
    public DataItem(String website, String uname, boolean spl, boolean num, boolean ucase, AddFragment parent) {
        this.website = website;
        this.uname = uname;

        String baseChars="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890@%+/'!#$^?:,(){}[]~-_";
        StringBuilder saltBuilder = new StringBuilder();
        Random random = new Random();

        for(int i = 0 ; i < 100; i++){
            saltBuilder.append(baseChars.charAt(random.nextInt(baseChars.length())));
        }
        salt = website + saltBuilder.toString() + uname;

        this.spl = spl;
        this.num = num;
        this.ucase = ucase;
        this.parent = parent;
        isRead = false;
    }

    /**
     * Constructor to initialize a new DataItem object from database cursor
     *
     * @param cursor Cursor object to load dataitem
     */
    public DataItem(Cursor cursor){
        id = cursor.getInt(cursor.getColumnIndex("_id"));
        website = cursor.getString(cursor.getColumnIndex("website"));
        uname = cursor.getString(cursor.getColumnIndex("uname"));
        salt = cursor.getString(cursor.getColumnIndex("salt"));
        genie = cursor.getString(cursor.getColumnIndex("genie"));

        int temp = cursor.getInt(cursor.getColumnIndex("spl"));
        spl = temp != 0;

        temp = cursor.getInt(cursor.getColumnIndex("num"));
        num = temp != 0;

        temp = cursor.getInt(cursor.getColumnIndex("ucase"));
        ucase = temp != 0;
        isRead = true;
    }

    /**
     * Insert a new Entry in DB
     * @param pass the password in string
     */
    public void insertRow(String pass){
        EncryptPassword encryptPassword = new EncryptPassword();
        encryptPassword.execute(pass);
    }

    /**
     * Update an Entry in DB
     * @param pass the password in string
     */
    public void updateRow(String pass){
        UpdateEntry updateEntry = new UpdateEntry();
        updateEntry.execute(pass);
    }

    /**
     * AsyncTask to update an entry in DB
     */
    private class UpdateEntry extends AsyncTask<String, Void, Void> {
        DialogFragment dialogFragment = new CustomProgressDialog();
        ContentValues contentValues;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Bundle args = new Bundle();
            args.putString("message", "Locking in Vault!!!");
            dialogFragment.setArguments(args);
            dialogFragment.show(parent.getFragmentManager(), "Progress Dialog");

            contentValues = new ContentValues();
            contentValues.put("website", website);
            contentValues.put("uname", uname);
            contentValues.put("salt", salt);
            contentValues.put("spl", (spl)? 1 : 0);
            contentValues.put("num", (num)? 1 : 0);
            contentValues.put("ucase", (ucase) ? 1 : 0);
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            MainActivity mainActivity = (MainActivity)parent.getActivity();
            parent = null;
            mainActivity.editDataItemList(id, DataItem.this);
            dialogFragment.dismiss();
            mainActivity.switchFragment(R.layout.fragment_home);
        }

        @Override
        protected Void doInBackground(String... params) {
            String ticket = "";
            try{
                String key = String.valueOf(Lamp.decryptGenie(website + uname, genie));
                Crypt sreeCrypt = Crypt.getDefault(key, salt, new byte[16]);
                String pass = params[0];
                ticket = sreeCrypt.encrypt(pass);
            }catch(KeyStoreException | NoSuchProviderException | IllegalBlockSizeException | NoSuchAlgorithmException | UnrecoverableEntryException | IOException | CertificateException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException e) {
                e.printStackTrace();
            }

            contentValues.put("ticket", ticket);

            DBTransactions transactions = new DBTransactions(parent.getContext());
            transactions.update("gems", contentValues, "_id = ?", new String[]{String.valueOf(id)});
            transactions.closeDB();
            return null;
        }
    }

    /**
     * AsyncTask to Insert a new entry in DB
     */
    private class EncryptPassword extends AsyncTask<String, Void, Void> {
        DialogFragment dialogFragment = new CustomProgressDialog();
        ContentValues contentValues;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Bundle args = new Bundle();
            args.putString("message", "Locking in Vault!!!");
            dialogFragment.setArguments(args);
            dialogFragment.show(parent.getFragmentManager(),"Progress Dialog");

            contentValues = new ContentValues();
            contentValues.put("website", website);
            contentValues.put("uname", uname);
            contentValues.put("salt", salt);
            contentValues.put("spl", (spl)? 1 : 0);
            contentValues.put("num", (num)? 1 : 0);
            contentValues.put("ucase", (ucase) ? 1 : 0);
        }

        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);
            MainActivity mainActivity = (MainActivity)parent.getActivity();
            parent = null;
            mainActivity.addToDataItemList(DataItem.this);
            dialogFragment.dismiss();
            mainActivity.switchFragment(R.layout.fragment_home);
        }

        @Override
        protected Void doInBackground(String... params) {
            String pass = params[0];
            String ticket = "";
            try {
                String[] genies = Lamp.encryptGenie(website + uname, parent.getContext());
                contentValues.put("genie", genies[0]);

                Crypt sreeCrypt = Crypt.getDefault(String.valueOf(genies[1]), salt, new byte[16]);
                ticket = sreeCrypt.encrypt(pass);
            }catch(InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException | KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableEntryException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException e){
                e.printStackTrace();
            }

            contentValues.put("ticket", ticket);

            DBTransactions transactions = new DBTransactions(parent.getContext());
            transactions.insertData("gems", contentValues);
            transactions.closeDB();
            return null;
        }
    }

    /**
     * Getter
     *
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * Setter
     *
     * @param id id
     */
    public void setId(int id){
        this.id = id;
    }

    /**
     * Getter
     *
     * @return website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * Getter
     *
     * @return uname
     */
    public String getUname() {
        return uname;
    }

    /**
     * Getter
     *
     * @return salt
     */
    public String getSalt() {
        return salt;
    }
}

package com.sreemenon.sqlite;


import android.content.Context;
import android.security.KeyPairGeneratorSpec;

import com.sreemenon.crypt.KeyStoreHelper;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

/**
 * SQLiteOpen Helper implementation
 */
public class SreeSqliteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "open_sesame";

    private static final int DATABASE_VERSION = 4;

    SQLiteDatabase database;

    /**
     * Constructor
     *
     * @param context context for key pair generation and database initialization
     */
    public SreeSqliteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        SQLiteDatabase.loadLibs(context);

        try {
            KeyStoreHelper keyStoreHelper = new KeyStoreHelper("AndroidKeyStore");
            if(!keyStoreHelper.getKeystore().containsAlias("diamond")) {
                keyStoreHelper.createNewKeyPair("diamond", context);
            }

            String dbPass = String.valueOf(keyStoreHelper.getPrivateKey("diamond").getModulus());
            database = getWritableDatabase(dbPass);

        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | IOException | CertificateException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter for Database variable
     * @return
     */
    protected SQLiteDatabase getDb() {
        return database;
    }

    /**
     * Close Database variable
     */
    protected void closeSqlDB(){
        database.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE `gems` (" +
                "`_id` integer PRIMARY KEY," +
                "`website` varchar(400)," +
                "`uname` varchar(400)," +
                "`salt` varchar(400) UNIQUE," +
                "`spl` integer," +
                "`num` integer," +
                "`ucase` integer," +
                "`ticket` varchar(400)," +
                "`genie` varchar(500)" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("Drop table if exists `gems`");

        onCreate(db);
    }
}

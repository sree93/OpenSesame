package com.sreemenon.crypt;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;

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
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

/**
 * Created by Admin on 18/12/2015.
 */
public class KeyStoreHelper {
    private KeyStore keyStore;
    private KeyStore.PrivateKeyEntry privateKeyEntry;

    public KeyStoreHelper(String keyStoreType)throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException{
        keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null);
    }

    public RSAPrivateKey getPrivateKey(String alias) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException{
        privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
        return (RSAPrivateKey)privateKeyEntry.getPrivateKey();
    }

    public RSAPublicKey getPublicKey(String alias)throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException{
        privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, null);
        return (RSAPublicKey)privateKeyEntry.getCertificate().getPublicKey();
    }

    public RSAPublicKey createNewKeyPair(String alias, Context context)throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException{
        Calendar notBefore = Calendar.getInstance();
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.YEAR, 25);
        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(alias)
                .setSubject(
                        new X500Principal(String.format("CN=%s, OU=%s", "sreemenon",
                                context.getPackageName())))
                .setSerialNumber(BigInteger.ONE).setStartDate(notBefore.getTime())
                .setEndDate(notAfter.getTime()).build();

        KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        kpGenerator.initialize(spec);
        KeyPair kp = kpGenerator.genKeyPair();
        return (RSAPublicKey)kp.getPublic();
    }

    public KeyStore getKeystore(){
        return keyStore;
    }
}

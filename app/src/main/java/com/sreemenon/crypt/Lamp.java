package com.sreemenon.crypt;

import android.content.Context;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Admin on 18/12/2015.
 */
public class Lamp {

    public static String decryptGenie(String alias, String genie)throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException {

        KeyStoreHelper keyStoreHelper = new KeyStoreHelper("AndroidKeyStore");
        RSAPrivateKey privateKey = keyStoreHelper.getPrivateKey(alias);

        Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
        output.init(Cipher.DECRYPT_MODE, privateKey);

        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(Base64.decode(genie, Base64.DEFAULT)), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte)nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for(int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i).byteValue();
        }

        return new String(bytes, 0, bytes.length, "UTF-8");
    }

    public static String[] encryptGenie(String alias, Context context)throws InvalidAlgorithmParameterException, KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException {

        KeyStoreHelper keyStoreHelper = new KeyStoreHelper("AndroidKeyStore");
        RSAPublicKey publicKey = keyStoreHelper.createNewKeyPair(alias, context);

        String baseChars="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890@%+/'!#$^?:,(){}[]~-_";
        StringBuilder randomBuilder = new StringBuilder();
        Random random = new Random();

        for(int i = 0 ; i < 100; i++){
            randomBuilder.append(baseChars.charAt(random.nextInt(baseChars.length())));
        }

        // Encrypt the text
        String initialText = randomBuilder.toString();

        Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
        input.init(Cipher.ENCRYPT_MODE, publicKey);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(
                outputStream, input);
        cipherOutputStream.write(initialText.getBytes("UTF-8"));
        cipherOutputStream.close();

        byte [] vals = outputStream.toByteArray();
        String result[] = new String[]{Base64.encodeToString(vals, Base64.DEFAULT), initialText};
        return result;
    }
}

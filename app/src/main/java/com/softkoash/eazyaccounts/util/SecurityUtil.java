package com.softkoash.eazyaccounts.util;

import android.os.Build;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Created by Deepak on 4/22/2017.
 */
public class SecurityUtil {

    //TODO read it from Build config...
    private static final String salt = "12324343434343";

    public static final byte[] generateHashPassword(String password) {
        try {
            SecretKeyFactory factory = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // Use compatibility key factory -- only uses lower 8-bits of passphrase chars
                factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1And8bit");
            } else {
                // Traditional key factory. Will use lower 8-bits of passphrase chars on
                // older Android versions (API level 18 and lower) and all available bits
                // on KitKat and newer (API level 19 and higher).
                factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            }
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 100, 512);
            SecretKey key = factory.generateSecret( spec );
            return key.getEncoded();
        } catch( NoSuchAlgorithmException | InvalidKeySpecException e ) {
            throw new RuntimeException( e );
        }
    }
}

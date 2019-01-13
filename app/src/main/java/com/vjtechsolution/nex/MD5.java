package com.vjtechsolution.nex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Aryo on 3/23/2018.
 */

public class MD5 {
    private String word;

    public MD5(String word) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(word.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }

            this.word = hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String getWord() {
        return word;
    }
}

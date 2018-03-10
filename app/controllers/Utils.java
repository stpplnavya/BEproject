package controllers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Random;

public class Utils {

    static public String generateSalt() {

       /* String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();

        while (salt.length() < 9) { // length of the random string.

            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }

        String saltStr = salt.toString();
        return saltStr;*/
       play.Logger.ALogger LOGGER = play.Logger.of(SurveyController.class);
       String salt = generateToken();
       String saltvalue = salt.substring(0,9);
       LOGGER.debug("Saltvalue : "+saltvalue);
        return saltvalue;
    }

    public static String generateHashedPassword(String password, String salt, int iteration) throws NoSuchAlgorithmException {

        String saltPwd = salt.concat(password);
        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
        final byte[] hash = mDigest.digest(saltPwd.getBytes(StandardCharsets.UTF_8));
        StringBuffer hexString = new StringBuffer();

        for(int i=0; i< iteration; i++){

            String hex = Integer.toHexString(0xff & hash[i] );
            hexString.append(hex);
        }
        return hexString.toString();
    }

    static public String generateToken() {

        String saltchars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();

        while (salt.length() < 18) { // length of the random string.

            int index = (int) (rnd.nextFloat() * saltchars.length());
            salt.append(saltchars.charAt(index));
        }

        String saltStr = salt.toString();
        return saltStr;
    }

    static public Long generateThreshold() {

        long generatedTime = Calendar.getInstance().getTimeInMillis();
        long threshold=generatedTime+600000;

        return threshold;
    }

}

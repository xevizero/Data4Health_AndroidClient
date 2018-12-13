package frassonlancellottilodi.data4health.utils;

import android.util.Base64;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

    /**
     * Checks whether the argument email could be a real email.
     * @param email the argument email to be checked
     * @return true if the email is valid, false otherwise
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String BASE64Encode(String input) {
        // This is base64 encoding, which is not an encryption
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    public static String BASE64Decode(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
    }
}

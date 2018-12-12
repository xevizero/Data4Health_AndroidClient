package frassonlancellottilodi.data4health.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import frassonlancellottilodi.data4health.HomeActivity;
import frassonlancellottilodi.data4health.LoginActivity;

public class SessionUtils {

    public static void checkLogin(Context applicationContext, Activity packageContext, Boolean loginPage){

        Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        String loggedIn  = encryption.decryptOrNull(preferences.getString("LoggedIn",""));

        if("true".equals(loggedIn) || loginPage){

        }else{
            revokeAuthToken(applicationContext, packageContext);
        }

    }

    public static void revokeAuthToken(Context applicationContext, Activity packageContext){
        Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove("authToken");
        editor.remove("userEmail");
        editor.putString("LoggedIn", encryption.encryptOrNull("false"));
        editor.apply();

        Toast.makeText(applicationContext, "Login denied", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(packageContext, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        packageContext.startActivity(intent);
        packageContext.finish();
    }

    public static String getLoggedUserEmail(Context applicationContext){
        Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return encryption.decryptOrNull(preferences.getString("userEmail",""));
    }

    public static String getAuthToken(Context applicationContext){
        Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return encryption.decryptOrNull(preferences.getString("authToken",""));
    }

    public static String getAutomatedSOSStatus(Context applicationContext){
        Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return encryption.decryptOrNull(preferences.getString("AutomatedSOSOn","false"));
    }

    public static void setAutomatedSOSStatus(Context applicationContext, boolean status){
        Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("AutomatedSOSOn", encryption.encryptOrNull(String.valueOf(status)));
        editor.apply();
    }
}

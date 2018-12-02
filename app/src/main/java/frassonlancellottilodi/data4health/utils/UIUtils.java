package frassonlancellottilodi.data4health.utils;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

public class UIUtils {

    public static void displayErrorAlert(String title, String message, Activity packageContext){
        AlertDialog alertDialog = new AlertDialog.Builder(packageContext).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
}

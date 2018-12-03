package frassonlancellottilodi.data4health.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
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

    public static int pxFromDp(final Context context, final float dp) {
        return (int)(dp * context.getResources().getDisplayMetrics().density);
    }

    public static Typeface getTitleFont(Activity packageContext){
        return Typeface.createFromAsset(packageContext.getAssets(), "fonts/Montserrat-Light.ttf");
    }

    public static Typeface getMainTitleFont(Activity packageContext){
        return Typeface.createFromAsset(packageContext.getAssets(), "fonts/Montserrat-Medium.ttf");
    }
}

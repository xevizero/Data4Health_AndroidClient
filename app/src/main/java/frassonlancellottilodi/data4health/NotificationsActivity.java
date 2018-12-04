package frassonlancellottilodi.data4health;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_NOTIFICATIONS;
import static frassonlancellottilodi.data4health.utils.SessionUtils.checkLogin;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getAuthToken;
import static frassonlancellottilodi.data4health.utils.SessionUtils.revokeAuthToken;
import static frassonlancellottilodi.data4health.utils.UIUtils.displayErrorAlert;
import static frassonlancellottilodi.data4health.utils.UIUtils.getTitleFont;
import static frassonlancellottilodi.data4health.utils.UIUtils.pxFromDp;


public class NotificationsActivity extends AppCompatActivity {

    private final String TAG = "NotificationsActivity";
    private LinearLayout notificationsScrollViewContainer;
    private TextView titleNotifications;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin(getApplicationContext(), this, false);
        setContentView(R.layout.activity_notifications);


    }

    private void downloadNotifications(){

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_NOTIFICATIONS, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if("Success".equals(response.getString("Response"))){
                                    JSONObject responseData = response.getJSONObject("Data");

                                    initializeUI();
                                }else if("Error".equals(response.getString("Response"))){
                                    int errorCode = Integer.valueOf(response.getString("Code"));
                                    switch (errorCode){
                                        case 104:
                                            revokeAuthToken(getApplicationContext(), this);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error ->
                                displayErrorAlert("There was a problem with your request!", error.getLocalizedMessage(), this));
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void initializeUI(){
        notificationsScrollViewContainer = findViewById(R.id.notificationsscrollviewContainer);
        titleNotifications = findViewById(R.id.titlenotifications);

        titleNotifications.setTypeface(getTitleFont(this));


        notificationsScrollViewContainer.addView(generateNotificationCard("You have a new access request!", "From Jack Smith"));
        notificationsScrollViewContainer.addView(generateNotificationSeparator());
        notificationsScrollViewContainer.addView(generateNotificationCard("You have a new access request!", "From Jack Smith"));
        notificationsScrollViewContainer.addView(generateNotificationSeparator());
        notificationsScrollViewContainer.addView(generateNotificationCard("You have a new access request!", "From Jack Smith"));

    }

    private RelativeLayout generateNotificationSeparator(){
        RelativeLayout dotsContainer = new RelativeLayout(this);
        dotsContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, pxFromDp(this, 16f)));
        dotsContainer.setGravity(Gravity.CENTER);

        ImageView dotsImageView = new ImageView(this);
        RelativeLayout.LayoutParams paramsImage = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        paramsImage.setMargins(0,0,0,0);
        dotsImageView.setLayoutParams(paramsImage);
        dotsImageView.setImageResource(R.drawable.threedots);

        dotsContainer.addView(dotsImageView);
        return dotsContainer;
    }

    private LinearLayout generateNotificationCard(String notificationTitle, String notificationMessage){

        LinearLayout verticalColumn = new LinearLayout(this);
        LinearLayout.LayoutParams paramsColumn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, pxFromDp(this, 120));
        paramsColumn.setMargins(100,0,100,0);
        verticalColumn.setLayoutParams(paramsColumn);
        verticalColumn.setOrientation(LinearLayout.VERTICAL);
        verticalColumn.setWeightSum(1);
        verticalColumn.setGravity(Gravity.CENTER);

            LinearLayout horizontalRow1 = new LinearLayout(this);
            LinearLayout.LayoutParams paramsRow = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.49f);
            paramsRow.setMargins(100,0,100,0);
            horizontalRow1.setLayoutParams(paramsRow);
            horizontalRow1.setOrientation(LinearLayout.HORIZONTAL);
            horizontalRow1.setWeightSum(1);
            horizontalRow1.setGravity(Gravity.CENTER);

                RelativeLayout titleContainer = new RelativeLayout(this);
                titleContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f));
                titleContainer.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

                    TextView titleView = new TextView(this);
                    titleView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                    titleView.setTextColor(getResources().getColor(R.color.darkGrey));
                    titleView.setText(notificationTitle);
                    titleView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    titleView.setGravity(Gravity.CENTER);

                RelativeLayout arrowContainer = new RelativeLayout(this);
                arrowContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.2f));
                arrowContainer.setGravity(Gravity.CENTER);

                    ImageView arrowImageView = new ImageView(this);
                    RelativeLayout.LayoutParams paramsImage = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    paramsImage.setMargins(30,30,30,30);
                    arrowImageView.setLayoutParams(paramsImage);
                    arrowImageView.setImageResource(R.drawable.arrow_right);

            LinearLayout horizontalSeparator = new LinearLayout(this);
            LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 0.02f);
            separatorParams.setMargins(0,0,0,0);
            horizontalSeparator.setBackgroundColor(getResources().getColor(R.color.lightGrey));
            horizontalSeparator.setLayoutParams(separatorParams);

            LinearLayout horizontalRow2 = new LinearLayout(this);
            horizontalRow2.setLayoutParams(paramsRow);
            horizontalRow2.setOrientation(LinearLayout.HORIZONTAL);
            horizontalRow2.setWeightSum(1);
            horizontalRow2.setGravity(Gravity.CENTER);

                RelativeLayout subtitleContainer = new RelativeLayout(this);
                subtitleContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f));
                subtitleContainer.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

                    TextView subtitleView = new TextView(this);
                    subtitleView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                    subtitleView.setTextColor(getResources().getColor(R.color.darkGrey));
                    subtitleView.setText(notificationMessage);
                    titleView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    subtitleView.setGravity(Gravity.CENTER);

                RelativeLayout imageContainer = new RelativeLayout(this);
                imageContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.2f));
                imageContainer.setGravity(Gravity.CENTER);

                    ImageView detailImageView = new ImageView(this);
                    detailImageView.setLayoutParams(paramsImage);
                    detailImageView.setImageResource(R.drawable.addphoto);

        titleContainer.addView(titleView);
        arrowContainer.addView(arrowImageView);
        horizontalRow1.addView(titleContainer);
        horizontalRow1.addView(arrowContainer);
        verticalColumn.addView(horizontalRow1);
        subtitleContainer.addView(subtitleView);
        imageContainer.addView(detailImageView);
        horizontalRow2.addView(subtitleContainer);
        horizontalRow2.addView(imageContainer);
        verticalColumn.addView(horizontalSeparator);
        verticalColumn.addView(horizontalRow2);

        return verticalColumn;
    }




}

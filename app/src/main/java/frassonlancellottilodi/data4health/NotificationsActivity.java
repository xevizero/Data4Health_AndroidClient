package frassonlancellottilodi.data4health;

import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import frassonlancellottilodi.data4health.utils.APIUtils;

import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_IMAGES;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_NOTIFICATIONS;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_NOTIFICATIONS_CLEAR_ALL;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_NOTIFICATIONS_REQUEST_ANSWER;
import static frassonlancellottilodi.data4health.utils.SessionUtils.checkLogin;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getAuthToken;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getLoggedUserEmail;
import static frassonlancellottilodi.data4health.utils.SessionUtils.revokeAuthToken;
import static frassonlancellottilodi.data4health.utils.UIUtils.displayErrorAlert;
import static frassonlancellottilodi.data4health.utils.UIUtils.getTitleFont;
import static frassonlancellottilodi.data4health.utils.UIUtils.pxFromDp;


public class NotificationsActivity extends AppCompatActivity {

    private final String TAG = "NotificationsActivity";
    private LinearLayout notificationsScrollViewContainer;
    private RelativeLayout paddingScrollView;
    private TextView titleNotifications;
    private Button clearAllButton;
    private List<String> emailsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin(getApplicationContext(), this, false);
        setContentView(R.layout.activity_notifications);

        downloadNotifications();
    }

    private void initializeUI(JSONArray requests) throws JSONException {
        notificationsScrollViewContainer = findViewById(R.id.notificationsscrollviewContainer);
        titleNotifications = findViewById(R.id.titlenotifications);
        clearAllButton = findViewById(R.id.notificationsClearAllButton);
        paddingScrollView = findViewById(R.id.paddingnotificationsscrollview);

        emailsList = new ArrayList<>();
        titleNotifications.setTypeface(getTitleFont(this));


        for(int i = 0; i < requests.length(); i++){
            JSONObject request = requests.getJSONObject(i);
            final String email = request.getString("Email");
            final String name = request.getString("Name");
            final String surname = request.getString("Surname");
            emailsList.add(email);

            notificationsScrollViewContainer.addView(generateNotificationCard("You have a new friend request!", "From " + name + " " + surname, email));

            int ii = i+1;
            if(!(ii == requests.length()))
                notificationsScrollViewContainer.addView(generateNotificationSeparator(email));

        }
        clearAllButton.setOnClickListener(v -> displayClearAllChoiceDialog());


    }

    private void clearAllUI(){
        emailsList.clear();
        notificationsScrollViewContainer.removeAllViews();
        notificationsScrollViewContainer.addView(paddingScrollView);
    }

    private void clearAllRequest(){

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
            JSONArray emailArray = new JSONArray();
            for(String entry: emailsList){
                Log.d(TAG, "clearAllRequest: " + entry);
                emailArray.put(entry);
            }
            POSTParams.put("Email", emailArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_NOTIFICATIONS_CLEAR_ALL, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if("Success".equals(response.getString("Response"))){
                                    clearAllUI();
                                }else if("Error".equals(response.getString("Response"))){
                                    int errorCode = Integer.valueOf(response.getString("Code"));
                                    switch (errorCode){
                                        case 104:
                                            revokeAuthToken(getApplicationContext(), this);
                                        default:
                                            displayErrorAlert("Error", response.getString("Message"), this);
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
                                    JSONArray requests = responseData.getJSONArray("Requests");
                                    initializeUI(requests);
                                }else if("Error".equals(response.getString("Response"))){
                                    int errorCode = Integer.valueOf(response.getString("Code"));
                                    switch (errorCode){
                                        case 104:
                                            revokeAuthToken(getApplicationContext(), this);
                                        default:
                                            displayErrorAlert("Error", response.getString("Message"), this);
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

    private void submitAnswerFriendRequest(String email, Boolean answer, View v){

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
            POSTParams.put("Email", email);
            POSTParams.put("Answer", answer);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_NOTIFICATIONS_REQUEST_ANSWER, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if("Success".equals(response.getString("Response"))){
                                    Toast.makeText(NotificationsActivity.this, (answer)?"Request accepted":"Request rejected", Toast.LENGTH_LONG);
                                    notificationsScrollViewContainer.removeView(v);
                                    emailsList.remove(email);
                                    for(int i = 0; i < notificationsScrollViewContainer.getChildCount(); i++){
                                        View child = notificationsScrollViewContainer.getChildAt(i);
                                        if(child.getTag() != null && child.getTag().equals(email)){
                                            notificationsScrollViewContainer.removeView(child);
                                            break;
                                        }
                                    }
                                }else if("Error".equals(response.getString("Response"))){
                                    int errorCode = Integer.valueOf(response.getString("Code"));
                                    switch (errorCode){
                                        case 104:
                                            revokeAuthToken(getApplicationContext(), this);
                                        default:
                                            displayErrorAlert("Error", response.getString("Message"), this);
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



    private RelativeLayout generateNotificationSeparator(String tag){
        RelativeLayout dotsContainer = new RelativeLayout(this);
        dotsContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, pxFromDp(this, 16f)));
        dotsContainer.setGravity(Gravity.CENTER);
        dotsContainer.setTag(tag);

        ImageView dotsImageView = new ImageView(this);
        RelativeLayout.LayoutParams paramsImage = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        paramsImage.setMargins(0,0,0,0);
        dotsImageView.setLayoutParams(paramsImage);
        dotsImageView.setImageResource(R.drawable.threedots);

        dotsContainer.addView(dotsImageView);
        return dotsContainer;
    }

    private LinearLayout generateNotificationCard(String notificationTitle, String notificationMessage, String email){

        LinearLayout verticalColumn = new LinearLayout(this);
        LinearLayout.LayoutParams paramsColumn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, pxFromDp(this, 120));
        paramsColumn.setMargins(100,0,100,0);
        verticalColumn.setLayoutParams(paramsColumn);
        verticalColumn.setOrientation(LinearLayout.VERTICAL);
        verticalColumn.setWeightSum(1);
        verticalColumn.setBackgroundResource(R.drawable.ripplewhite);
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
                    downloadProfilePicture(response -> detailImageView.setImageBitmap(response), email);
                    detailImageView.setTransitionName("ProfilePictureTransitionHomePage");

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

        verticalColumn.setOnClickListener(v -> displayRequestChoiceDialog(email, detailImageView, v));

        return verticalColumn;
    }


    private void downloadProfilePicture(final APIUtils.imageRequestCallback callback, String userEmail){

        String requestURL = WEBSERVICE_URL_IMAGES + "?Token=" + getAuthToken(NotificationsActivity.this) + "&Filename=" + userEmail + ".png";
        ImageRequest imageRequest = new ImageRequest(requestURL,
                response -> callback.onSuccess(response), 100, 100, ImageView.ScaleType.CENTER_CROP,null,
                error -> imageDownloadErrorHandler());


        Volley.newRequestQueue(this).add(imageRequest);


    }

    private void imageDownloadErrorHandler(){
        //Nothing for now
    }


    private void displayRequestChoiceDialog(String email, ImageView picture, View v){
        AlertDialog alertDialog = new AlertDialog.Builder(NotificationsActivity.this).create();
        alertDialog.setTitle("What do you want to do?");
        alertDialog.setMessage("If you accept this request, your new friend will be able to see your data.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Accept request",
                (dialog, which) -> {
                    dialog.dismiss();
                    submitAnswerFriendRequest(email, true, v);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Reject request",
                (dialog, which) -> {
                    submitAnswerFriendRequest(email, false, v);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Open profile",
                (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.setAction(email);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            NotificationsActivity.this, picture, "ProfilePictureTransitionHomePage");
                    startActivity(intent, options.toBundle());
                });
        alertDialog.show();
    }

    private void displayClearAllChoiceDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(NotificationsActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Clearing all notifications will automatically reject all pending friend requests.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Clear all",
                (dialog, which) -> {
                    dialog.dismiss();
                    clearAllRequest();
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                (dialog, which) -> {
                    dialog.dismiss();
                });
        alertDialog.show();
    }
}

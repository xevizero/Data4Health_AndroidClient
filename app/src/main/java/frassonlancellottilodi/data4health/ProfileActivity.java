package frassonlancellottilodi.data4health;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_EXTERNAL_PROFILE;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_FRIEND_REQUEST;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_IMAGES;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_PROFILE;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_SUBSCRIPTION_REQUEST;
import static frassonlancellottilodi.data4health.utils.SessionUtils.checkLogin;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getAuthToken;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getLoggedUserEmail;
import static frassonlancellottilodi.data4health.utils.SessionUtils.revokeAuthToken;
import static frassonlancellottilodi.data4health.utils.UIUtils.displayErrorAlert;
import static frassonlancellottilodi.data4health.utils.UIUtils.getTitleFont;


public class ProfileActivity extends AppCompatActivity {

    private final String TAG = "ProfileActivity";
    private Button title, sendRequestButton;
    private Boolean personalProfile;
    private ImageView profilePicture;
    private String userEmail;
    private TextView text1, text2, nameText, stepsText, heartrateText, sendRequestText;
    private LinearLayout buttonSteps, buttonHeart, buttonSOS, separator1, separator2, requestButtonContainer;
    private CheckBox subscriptionCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin(getApplicationContext(), this, false);
        setContentView(R.layout.activity_profile);

        userEmail = getIntent().getAction();
        personalProfile = getLoggedUserEmail(getApplicationContext()).equals(userEmail);

        downloadProfileData();
    }

    private void initializeUI(String name, String surname, String sex, String birthday, String steps, String heartrate, Boolean subscription, int statusCode){
        title = findViewById(R.id.titleprofile);
        nameText  = findViewById(R.id.profilePageProfileName);
        text1 = findViewById(R.id.profilePageProfileText1);
        text2 = findViewById(R.id.profilePageProfileText2);
        stepsText = findViewById(R.id.profilePageStepsSubText);
        heartrateText = findViewById(R.id.profilePageHeartbeatSubText);
        profilePicture = findViewById(R.id.profilePageProfilePicture);
        buttonHeart = findViewById(R.id.profilepageHeartButton);
        buttonSOS = findViewById(R.id.profilepageSOSButton);
        buttonSteps = findViewById(R.id.profilepageStepsButton);
        separator1 = findViewById(R.id.profilePageSeparator1);
        separator2 = findViewById(R.id.profilePageSeparator2);
        requestButtonContainer = findViewById(R.id.profileSendRequestButtonContainer);
        sendRequestText = findViewById(R.id.profileSendRequestText);
        sendRequestButton = findViewById(R.id.profileSendRequestButton);
        subscriptionCheckbox = findViewById(R.id.profileCheckSubscription);

        title.setTypeface(getTitleFont(this));
        title.setText((personalProfile)?"Your profile":"Profile");
        text2.setVisibility((personalProfile)?View.GONE:View.VISIBLE);
        subscriptionCheckbox.setVisibility((personalProfile)?View.GONE:View.VISIBLE);
        nameText.setText(name + " " + surname);
        String age = getAge(birthday);
        text1.setText(sex + ", " + age);
        if(personalProfile || statusCode == 1){
            stepsText.setText(steps);
            heartrateText.setText(heartrate);
        }else{
            buttonHeart.setVisibility(View.GONE);
            buttonSOS.setVisibility(View.GONE);
            buttonSteps.setVisibility(View.GONE);
            separator1.setVisibility(View.GONE);
            separator2.setVisibility(View.GONE);
        }
        if(!personalProfile){
            switch (statusCode){
                case 0:
                    text2.setText("Not connected");
                    subscriptionCheckbox.setVisibility(View.GONE);
                    requestButtonContainer.setVisibility(View.VISIBLE);
                    sendRequestText.setText("You are not connected to " + name);
                    sendRequestButton.setOnClickListener(v -> sendFriendRequest());
                    break;
                case 1:
                    text2.setText((subscription)?"Subscribed":"Not subscribed");
                    subscriptionCheckbox.setChecked(subscription);
                    subscriptionCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        sendSubscriptionRequest(isChecked);
                    });
                    break;
                case 2:
                    text2.setText("Not connected");
                    subscriptionCheckbox.setVisibility(View.GONE);
                    requestButtonContainer.setVisibility(View.VISIBLE);
                    sendRequestText.setText("Friend request sent.");
                    sendRequestButton.setOnClickListener(v -> displayErrorAlert("Request already pending!", "Your friend will no have to manually review the request.", this));
                    break;
            }
        }
        downloadProfilePicture(userEmail);
    }

    private void sendSubscriptionRequest(Boolean subRequest){

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
            POSTParams.put("Email", userEmail);
            POSTParams.put("Query", subRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_SUBSCRIPTION_REQUEST, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if("Success".equals(response.getString("Response"))){
                                    Boolean subscriptionResult = response.getBoolean("Data");
                                    text2.setText((subscriptionResult)?"Subscribed":"Not subscribed");
                                    subscriptionCheckbox.setChecked(subscriptionResult);
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

    private void sendFriendRequest(){

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
            POSTParams.put("Email", userEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_FRIEND_REQUEST, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if("Success".equals(response.getString("Response"))){
                                    sendRequestText.setText("Friend request sent.");
                                    sendRequestButton.setOnClickListener(v -> displayErrorAlert("Request already pending!", "Your friend will no have to manually review the request.", this));
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



    private void downloadProfileData(){

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
            if (!personalProfile)
                POSTParams.put("Email", userEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                ((personalProfile)?WEBSERVICE_URL_PROFILE:WEBSERVICE_URL_EXTERNAL_PROFILE, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if("Success".equals(response.getString("Response"))){
                                    JSONObject responseData = response.getJSONObject("Data");
                                    final String name = responseData.getString("Name");
                                    final String surname = responseData.getString("Surname");
                                    final String sex = responseData.getString("Sex");
                                    final String birthday = responseData.getString("Birthday");
                                    final String steps = responseData.getString("Steps");
                                    final String heartrate = responseData.getString("Heartbeat");
                                    Boolean subscription = false;
                                    int statusCode = 0;
                                    if(!personalProfile) {
                                            subscription = responseData.getBoolean("Subscription");
                                            statusCode = responseData.getInt("StatusCode");
                                    }
                                    initializeUI(name, surname, sex, birthday, steps, heartrate, subscription, statusCode);
                                }else if("Error".equals(response.getString("Response"))){
                                    int errorCode = Integer.valueOf(response.getString("Code"));
                                    switch (errorCode){
                                        case 104:
                                            revokeAuthToken(getApplicationContext(), this);
                                        case 105:
                                            displayErrorAlert("Error", "This user does not exist", this);
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

    private void downloadProfilePicture(String email){

        String requestURL = WEBSERVICE_URL_IMAGES + "?Token=" + getAuthToken(ProfileActivity.this) + "&Filename=" + email + ".png";
        ImageRequest imageRequest = new ImageRequest(requestURL,
                response -> profilePicture.setImageBitmap(response), 100, 100, ImageView.ScaleType.CENTER_CROP,null,
                error -> imageDownloadErrorHandler());


        Volley.newRequestQueue(this).add(imageRequest);


    }

    private String getAge(String birthday){
        int year = Integer.parseInt(birthday.split("-")[0]);
        int month = Integer.parseInt(birthday.split("-")[1]);
        int day = Integer.parseInt(birthday.split("-")[2]);
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }


    private void imageDownloadErrorHandler(){
        //Nothing for now
    }
}

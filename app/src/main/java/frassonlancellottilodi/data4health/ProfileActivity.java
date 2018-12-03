package frassonlancellottilodi.data4health;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_IMAGES;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_PROFILE;
import static frassonlancellottilodi.data4health.utils.SessionUtils.checkLogin;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getAuthToken;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getLoggedUserEmail;
import static frassonlancellottilodi.data4health.utils.SessionUtils.revokeAuthToken;
import static frassonlancellottilodi.data4health.utils.UIUtils.displayErrorAlert;
import static frassonlancellottilodi.data4health.utils.UIUtils.getTitleFont;


public class ProfileActivity extends AppCompatActivity {

    private final String TAG = "ProfileActivity";
    private Button title;
    private Boolean personalProfile;
    private ImageView profilePicture;
    private String userEmail;
    private TextView text1, text2, nameText, stepsText, heartrateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin(getApplicationContext(), this, false);
        setContentView(R.layout.activity_profile);

        userEmail = getIntent().getAction();
        personalProfile = getLoggedUserEmail(getApplicationContext()).equals(userEmail);


        downloadProfileData();
    }

    private void initializeUI(String name, String surname, String sex, String birthday, String steps, String heartrate){
        title = findViewById(R.id.titleprofile);
        nameText  = findViewById(R.id.profilePageProfileName);
        text1 = findViewById(R.id.profilePageProfileText1);
        text2 = findViewById(R.id.profilePageProfileText2);
        stepsText = findViewById(R.id.profilePageStepsSubText);
        heartrateText = findViewById(R.id.profilePageHeartbeatSubText);
        profilePicture = findViewById(R.id.profilePageProfilePicture);

        title.setTypeface(getTitleFont(this));
        title.setText((personalProfile)?"Your profile":"Profile");
        text2.setVisibility((personalProfile)?View.GONE:View.VISIBLE);
        nameText.setText(name + " " + surname);
        String age = getAge(birthday);
        text1.setText(sex + ", " + age);
        stepsText.setText(steps);
        heartrateText.setText(heartrate);

        downloadProfilePicture();
    }

    private void downloadProfileData(){

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_PROFILE, POSTParams,
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
                                    initializeUI(name, surname, sex, birthday, steps, heartrate);
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

    private void downloadProfilePicture(){

        String requestURL = WEBSERVICE_URL_IMAGES + "?Token=" + getAuthToken(ProfileActivity.this) + "&Filename=" + getLoggedUserEmail(ProfileActivity.this) + ".png";
        ImageRequest imageRequest = new ImageRequest(requestURL,
                response -> {
                    profilePicture.setImageBitmap(response);
                }, 100, 100, ImageView.ScaleType.CENTER_CROP,null, error -> {

        });


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
}

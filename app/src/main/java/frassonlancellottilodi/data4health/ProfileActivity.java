package frassonlancellottilodi.data4health;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_PROFILE;
import static frassonlancellottilodi.data4health.utils.SessionUtils.checkLogin;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getAuthToken;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getLoggedUserEmail;
import static frassonlancellottilodi.data4health.utils.UIUtils.displayErrorAlert;


public class ProfileActivity extends AppCompatActivity {

    private final String TAG = "ProfileActivity";
    private Button title;
    private Boolean personalProfile;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin(getApplicationContext(), this, false);
        setContentView(R.layout.activity_profile);

        userEmail = getIntent().getAction();
        personalProfile = getLoggedUserEmail(getApplicationContext()).equals(userEmail);


        downloadProfileData();
    }

    private void initializeUI(){
        title = findViewById(R.id.titleprofile);


        title.setText((personalProfile)?"Your profile":"Profile");

    }

//name surname birthday

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
                                if("Success".equals(response.getString("Response"))){
                                    Log.d(TAG, response.toString());
                                    initializeUI();
                                }else if("Error".equals(response.getString("Response"))){
                                    Log.d(TAG, response.toString());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error ->
                                displayErrorAlert("There was a problem with your request!", error.getLocalizedMessage(), this));
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}

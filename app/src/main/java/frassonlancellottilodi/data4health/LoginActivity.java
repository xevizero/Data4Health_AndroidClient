package frassonlancellottilodi.data4health;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import frassonlancellottilodi.data4health.utils.Encryption;
import frassonlancellottilodi.data4health.utils.Endpoints;

import static frassonlancellottilodi.data4health.utils.SessionUtils.checkLogin;
import static frassonlancellottilodi.data4health.utils.TextUtils.isEmailValid;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";

    private Button registerButton, websiteButton, loginButton;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin(getApplicationContext(), this, true);
        setContentView(R.layout.activity_login);
        initializeUI();
    }


    private void initializeUI(){
        registerButton = findViewById(R.id.loginRegisterButton);
        loginButton = findViewById(R.id.buttonlogin);
        websiteButton = findViewById(R.id.loginWebsiteLinkButton);
        emailEditText = findViewById(R.id.emailLoginEditText);
        passwordEditText = findViewById(R.id.passwordLoginEditText);


        registerButton.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
        });
        websiteButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Endpoints.WEBSITE_URL));
            startActivity(browserIntent);
        });
        loginButton.setOnClickListener(validateLoginRequest());

    }

    private View.OnClickListener validateLoginRequest(){
        return v -> {
            final String email = String.valueOf(emailEditText.getText());
            final String password = String.valueOf(passwordEditText.getText());
            Boolean stop = false;

            if (!stop && email.length() == 0){
                displayErrorAlert("Fulfill al fields!", "Insert your email.");
                stop = true;
            }
            if (!stop && !isEmailValid(email)){
                displayErrorAlert("Email not valid!", "Insert a valid email.");
                stop = true;
            }
            if (!stop && password.length() == 0){
                displayErrorAlert("Fulfill al fields!", "Insert your password.");
                stop = true;
            }
            if(!stop)
                startLoginRequest();
        };
    }

    private void displayErrorAlert(String title, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    private void startLoginRequest(){

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("email", String.valueOf(emailEditText.getText()));
            POSTParams.put("password", String.valueOf(passwordEditText.getText()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Endpoints.WEBSERVICE_URL_LOGIN, POSTParams,
                        response -> {
                            try {
                                if("Success".equals(response.getString("Response"))){
                                    saveLoginSession(response.getString("Token"));
                                    Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(this, NotificationsActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }else if("Error".equals(response.getString("Response"))){
                                    displayErrorAlert("There was a problem with your request!", response.getString("Message"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error ->
                        {
                            displayErrorAlert("There was a problem with your request!", error.getLocalizedMessage());
                        });
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void saveLoginSession(String authToken){
        Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("LoggedIn", encryption.encryptOrNull("true"));
        editor.putString("authToken", encryption.encryptOrNull(authToken));
        editor.apply();
    }
}

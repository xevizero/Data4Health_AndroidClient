package frassonlancellottilodi.data4health;

import com.android.volley.AuthFailureError;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import frassonlancellottilodi.data4health.utils.DataPart;
import frassonlancellottilodi.data4health.utils.ImageUtils;
import frassonlancellottilodi.data4health.utils.VolleyMultipartRequest;

public class RegisterActivity extends AppCompatActivity{

    private String TAG = "RegisterActivity";
    private final String WEBSERVICEURL = "http://192.168.1.129:5000/android/register";
    //--host=192.168.43.60

    private Button buttonRegister;
    private ImageView cameraButton;
    private Bitmap profileImage;
    private EditText nameEditText, surnameEditText, emailEditText, phoneEditText, passwordEditText, passwordConfirmEditText;
    private DatePicker birthdayDatePicker;
    private CheckBox automatedSOSCheckBox, developerCheckBox, datasharingCheckBox, termsCheckBox;
    private Spinner sexSpinner;
    static final int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeUI();


    }

    private void initializeUI(){

        buttonRegister = findViewById(R.id.buttonregister);
        cameraButton = findViewById(R.id.registerCameraButton);
        nameEditText = findViewById(R.id.nameRegisterEditText);
        surnameEditText = findViewById(R.id.surnameRegisterEditText);
        emailEditText = findViewById(R.id.emailRegisterEditText);
        phoneEditText = findViewById(R.id.phoneRegisterEditText);
        passwordEditText = findViewById(R.id.passwordRegisterEditText);
        passwordConfirmEditText = findViewById(R.id.repeatPasswordRegisterEditText);
        birthdayDatePicker = findViewById(R.id.birthdayRegisterDatePicker);
        automatedSOSCheckBox = findViewById(R.id.registerCheckAutomatedSOS);
        developerCheckBox = findViewById(R.id.registerCheckDeveloper);
        datasharingCheckBox = findViewById(R.id.registerCheckDataSharing);
        termsCheckBox = findViewById(R.id.registerCheckTerms);
        sexSpinner = findViewById(R.id.spinnersexregister);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sex_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexSpinner.setAdapter(adapter);



        buttonRegister.setOnClickListener(signUpClickListener());
        cameraButton.setOnClickListener(takeProfilePhotoListener());

    }

    private View.OnClickListener takeProfilePhotoListener(){
        return v -> {
            dispatchTakePictureIntent();
        };
    }

    private View.OnClickListener signUpClickListener(){

        return v -> {
            uploadBitmap();
        };
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = ImageUtils.getCroppedBitmap((Bitmap) extras.get("data"));
            cameraButton.setImageBitmap(imageBitmap);
            profileImage = imageBitmap;
        }
    }

    private void uploadBitmap() {

        //getting the tag from the edittext
        final String name = String.valueOf(nameEditText.getText());
        final String surname = String.valueOf(surnameEditText.getText());
        final String email = String.valueOf(emailEditText.getText());
        final String userPhoneNumber = String.valueOf(phoneEditText.getText());
        final String birthday_str = "2018-11-11";
        final String password = String.valueOf(passwordEditText.getText());
        final String sex = "ciao";
        final String automatedSOSOn = "True";
        final String developerAccount = "True";
        final String anonymousDataSharingON = "True";

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, WEBSERVICEURL,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(new String(response.data));
                        Toast.makeText(getApplicationContext(), obj.getString("Response") + " - " + obj.getString("Message"), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, obj.getString("Response") + " - " + obj.getString("Message"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show()) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("surname", surname);
                params.put("email", email);
                params.put("userPhoneNumber", userPhoneNumber);
                params.put("birthday", birthday_str);
                params.put("password", password);
                params.put("sex", sex);
                params.put("automatedSOSOn", automatedSOSOn);
                params.put("developerAccount", developerAccount);
                params.put("anonymousDataSharingON", anonymousDataSharingON);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("file", new DataPart(imagename + ".png", ImageUtils.getFileDataFromDrawable(profileImage)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }


}

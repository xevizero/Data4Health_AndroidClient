package frassonlancellottilodi.data4health;

import com.android.volley.AuthFailureError;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import frassonlancellottilodi.data4health.utils.TextUtils;
import frassonlancellottilodi.data4health.utils.VolleyMultipartRequest;

public class RegisterActivity extends AppCompatActivity{

    private String TAG = "RegisterActivity";
    private final String WEBSERVICEURL = "http://192.168.1.129:5000/android/register";
    private String selectedSex;
    //--host=192.168.43.60

    private Button buttonRegister;
    private ImageView cameraButton;
    private Bitmap profileImage;
    private EditText nameEditText, surnameEditText, emailEditText, phoneEditText, passwordEditText, passwordConfirmEditText;
    private DatePicker birthdayDatePicker;
    private CheckBox automatedSOSCheckBox, developerCheckBox, datasharingCheckBox, termsCheckBox;
    private Spinner sexSpinner;
    static final int REQUEST_IMAGE_CAPTURE = 1, REQUEST_IMAGE_SELECTION = 2, REQUEST_STORAGE_PERMISSION = 3;


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



        buttonRegister.setOnClickListener(signUpClickListener());
        cameraButton.setOnClickListener(takeProfilePhotoListener());
        sexSpinner.setOnItemSelectedListener(sexSpinnerItemClickListener());
        sexSpinner.setAdapter(adapter);
        sexSpinner.setSelection(0);

    }



    private boolean checkStoragePermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            }
        }
        return false;
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    private AdapterView.OnItemSelectedListener sexSpinnerItemClickListener(){
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSex = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                CharSequence[] sexes = getResources().getTextArray(R.array.sex_array);
                selectedSex = sexes[2].toString();
            }
        };
    }

    private View.OnClickListener takeProfilePhotoListener(){
        return v -> {
            displayImageChoiceDialog();
        };
    }

    private View.OnClickListener signUpClickListener(){

        return v -> {
            final String name = String.valueOf(nameEditText.getText());
            final String surname = String.valueOf(surnameEditText.getText());
            final String email = String.valueOf(emailEditText.getText());
            final String password = String.valueOf(passwordEditText.getText());
            final String passwordRepeat = String.valueOf(passwordConfirmEditText.getText());
            Boolean stop = false;
            Boolean check = false;

            if (!stop && name.length() == 0){
                displayErrorAlert("Fulfill al fields!", "Insert your name.");
                stop = true;
            }
            if (!stop && surname.length() == 0){
                displayErrorAlert("Fulfill al fields!", "Insert your surname.");
                stop = true;
            }
            if (!stop && email.length() == 0){
                displayErrorAlert("Fulfill al fields!", "Insert your email.");
                stop = true;
            }
            if (!stop && !TextUtils.isEmailValid(email)){
                displayErrorAlert("Email not valid!", "Insert a valid email.");
                stop = true;
            }
            if (!stop && !isStoragePermissionGranted()){
                checkStoragePermissions();
                stop = true;
            }
            if (!stop && profileImage == null){
                displayErrorAlert("Please, add a photo", "You have to add a profile photo to sign up to Data4Health.");
                stop = true;
            }
            if (!stop && (password.length() == 0 || passwordRepeat.length() == 0)){
                displayErrorAlert("Fulfill al fields!", "Insert your password.");
                stop = true;
            }
            if (!stop && !password.equals(passwordRepeat)){
                displayErrorAlert("Repeat your password!", "You have to confirm your password.");
                stop = true;
            }
            if (!stop && !termsCheckBox.isChecked()){
                displayErrorAlert("Please, accept Terms and Conditions", "Accept Terms and Conditions to sign up to Data4Health.");
                stop = true;
            }
            if(!stop)
                uploadBitmap();
        };
    }

    private void displayErrorAlert(String title, String message){
        AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    private void displayImageChoiceDialog(){
        AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
        alertDialog.setTitle("Choose a profile picture");
        alertDialog.setMessage("Do you want to take a photo or to select an existing image?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Take a photo",
                (dialog, which) -> {
                    dialog.dismiss();
                    dispatchTakePictureIntent();
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Choose from gallery",
                (dialog, which) -> {
                    dialog.dismiss();
                    if(isStoragePermissionGranted()) {
                        dispatchSelectPictureIntent();
                    }else{
                        checkStoragePermissions();
                    }
                });
        alertDialog.show();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void dispatchSelectPictureIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_SELECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = ImageUtils.getCroppedBitmap((Bitmap) extras.get("data"));
            cameraButton.setImageBitmap(imageBitmap);
            profileImage = imageBitmap;
        }else if (requestCode == REQUEST_IMAGE_SELECTION && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap imageBitmap = ImageUtils.getCroppedBitmap(BitmapFactory.decodeFile(picturePath));
            cameraButton.setImageBitmap(imageBitmap);
            profileImage = imageBitmap;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
        }
    }

    private void uploadBitmap() {

        final String name = String.valueOf(nameEditText.getText());
        final String surname = String.valueOf(surnameEditText.getText());
        final String email = String.valueOf(emailEditText.getText());
        final String userPhoneNumber = String.valueOf(phoneEditText.getText());
        final String birthday_str = String.valueOf(birthdayDatePicker.getYear()) + "-" + String.valueOf(birthdayDatePicker.getMonth()+1) + "-" + String.valueOf(birthdayDatePicker.getDayOfMonth());
        final String password = String.valueOf(passwordEditText.getText());
        final String sex = selectedSex;
        final String automatedSOSOn = String.valueOf(automatedSOSCheckBox.isChecked());
        final String developerAccount = String.valueOf(developerCheckBox.isChecked());
        final String anonymousDataSharingON = String.valueOf(datasharingCheckBox.isChecked());

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, WEBSERVICEURL,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(new String(response.data));
                        Log.d(TAG, obj.getString("Response") + " - " + obj.getString("Message"));
                        if("Success".equals(obj.getString("Response"))){
                            Toast.makeText(getApplicationContext(), "You have successfully been registered to Data4Health!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(this, LoginActivity.class);// New activity
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }else if("Error".equals(obj.getString("Response"))){
                            displayErrorAlert("There was a problem with your request!", obj.getString("Message"));
                        }
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

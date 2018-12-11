package frassonlancellottilodi.data4health;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import frassonlancellottilodi.data4health.utils.APIUtils;
import frassonlancellottilodi.data4health.utils.Encryption;

import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_HOMEPAGE;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_IMAGES;
import static frassonlancellottilodi.data4health.utils.SessionUtils.checkLogin;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getAuthToken;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getAutomatedSOSStatus;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getLoggedUserEmail;
import static frassonlancellottilodi.data4health.utils.SessionUtils.revokeAuthToken;
import static frassonlancellottilodi.data4health.utils.UIUtils.displayErrorAlert;
import static frassonlancellottilodi.data4health.utils.UIUtils.getTitleFont;
import static frassonlancellottilodi.data4health.utils.UIUtils.pxFromDp;


//adb -d forward tcp:5601 tcp:5601

public class HomeActivity extends android.support.v4.app.FragmentActivity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private LinearLayout profileButton, data4helpButton, peopleBar, addFriendButtonContainer, automatedSOSButton;
    private ImageView notificationsButton;
    private Button titleView;
    private FloatingActionButton addFriendButton;
    private TextView profileName;
    private ImageView profilePicture, automatedSOSIcon;

    private static final String TAG = "HomeActivity";
    private static final String AUTH_PENDING = "isAuthPending";
    private GoogleApiClient googleApiClient;
    private boolean authInProgress = false;
    private final static int SERVICE_REQUEST_CODE = 8;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin(getApplicationContext(), this, false);
        setContentView(R.layout.activity_home);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        requestBackgroundServiceAuthorization();

        downloadHomeData();
    }

    private void requestBackgroundServiceAuthorization(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        Intent intent=new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            }
            else {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }




    private void initializeUI(String name, String surname, JSONArray emails) throws JSONException {

        titleView = findViewById(R.id.titlehome);
        titleView.setTypeface(getTitleFont(this));
        profileButton = findViewById(R.id.homepageProfileButton);
        data4helpButton = findViewById(R.id.homepageData4HealthButton);
        notificationsButton = findViewById(R.id.homepageNotificationsButton);
        profileName = findViewById(R.id.homePageProfileName);
        profilePicture = findViewById(R.id.homepageProfilePicture);
        peopleBar = findViewById(R.id.homepagePeopleBarContainer);
        addFriendButtonContainer = findViewById(R.id.homePageAddFriendButtonContainer);
        addFriendButton = findViewById(R.id.homePageAddFriendButton);
        automatedSOSButton = findViewById(R.id.homepageAutomatedSOSButton);
        automatedSOSIcon = findViewById(R.id.homepageAutomatedSOSIcon);

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.setAction(getLoggedUserEmail(getApplicationContext()));
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    HomeActivity.this, profilePicture, "ProfilePictureTransitionHomePage");
            startActivity(intent, options.toBundle());
        });
        data4helpButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        });
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            intent.setAction(getLoggedUserEmail(getApplicationContext()));
            startActivity(intent);
        });

        profileName.setText(name + " " + surname);
        downloadProfilePicture(response -> profilePicture.setImageBitmap(response), getLoggedUserEmail(this));

        peopleBar.removeView(addFriendButtonContainer);
        for(int i = 0; i < emails.length(); i++){
            final String ext_email = emails.getJSONObject(i).getString("Email");
            peopleBar.addView(generatePersonImageContainer(ext_email));
        }
        peopleBar.addView(addFriendButtonContainer);
        addFriendButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        automatedSOSButton.setOnClickListener(v -> displayActivateAutomatedSOSDialog());
        automatedSOSIcon.setImageResource((getAutomatedSOSStatus(this) != null && getAutomatedSOSStatus(this).equals("true"))?R.drawable.medic2:R.drawable.medic2_grey);

    }

    private RelativeLayout generatePersonImageContainer(String email){
        RelativeLayout pictureContainer = new RelativeLayout(this);
        LinearLayout.LayoutParams paramsPictureContainer = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        paramsPictureContainer.setMargins(0,0,0,0);
        pictureContainer.setLayoutParams(paramsPictureContainer);
        pictureContainer.setGravity(Gravity.CENTER_VERTICAL | RelativeLayout.CENTER_HORIZONTAL);

        ImageView photoImageView = new ImageView(this);
        RelativeLayout.LayoutParams paramsImage = new RelativeLayout.LayoutParams(pxFromDp(this, 80), pxFromDp(this, 80));
        //paramsImage.setMargins(30,30,30,30);
        photoImageView.setLayoutParams(paramsImage);
        photoImageView.setImageResource(R.drawable.bgspinner);
        photoImageView.setBackgroundResource(R.drawable.ripplecirclewhite);
        photoImageView.setTransitionName("ProfilePictureTransitionHomePage");
        downloadProfilePicture(response -> photoImageView.setImageBitmap(response), email);
        photoImageView.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            intent.setAction(email);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    HomeActivity.this, photoImageView, "ProfilePictureTransitionHomePage");
            startActivity(intent, options.toBundle());
        });

        pictureContainer.addView(photoImageView);
        return pictureContainer;
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient = googleAPIClientBuild(this, this, this);
        googleAPIConnect(this, googleApiClient);
    }

    public static GoogleApiClient googleAPIClientBuild(Activity activity, GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener failedListener){

        return new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(failedListener)
                .addApi(Wearable.API)
                .build();
    }

    public static void googleAPIConnect(final Activity activity, final GoogleApiClient mGoogleApiClient){
        Log.d(TAG, "google API connect called");
        if(!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
        Log.d(TAG, "OnStop called");
        if (null != googleApiClient && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected called");

        //WEARABLE
        String WEARABLE_DATA_PATH = "/wearable_data";
        DataMap dataMap = new DataMap();
        dataMap.putLong("time", new Date().getTime());
        dataMap.putString("hole", "1");
        dataMap.putString("front", "250");
        dataMap.putString("middle", "260");
        dataMap.putString("back", "270");
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            dataMap.putString("AGGIUNTA", "QUI");
            new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap).start();

        },5000);
        new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap).start();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended i= " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if( !authInProgress ) {
            Log.d(TAG, "!AUTHINPROG");
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(this, 1);
            } catch(IntentSender.SendIntentException e ) {
                Log.d(TAG, "SendIntentExc: " + e.toString());
            }
        } else {
            Log.d(TAG, "authInProgress" );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "OnActivityResult called");
        if( requestCode == 1) {
            authInProgress = false;
            if( resultCode == RESULT_OK ) {
                Log.d(TAG, "Result_OK");
                if( !googleApiClient.isConnecting() && !googleApiClient.isConnected() ) {
                    Log.d(TAG, "Calling googleApiClient.connect again");
                    googleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
                } else {
                    onConnected(null);
                }
            } else if( resultCode == RESULT_CANCELED ) {
                Log.d( TAG, "RESULT_CANCELED" );
            }
        } else {
            Log.d(TAG, "requestCode NOT request_oauth");
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "OnDestroy called");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Onsaveinstance called");
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    class SendToDataLayerThread extends Thread {
        String path;
        DataMap dataMap;


        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data) {
            Log.d(TAG, "sending message");

            path = p;
            dataMap = data;
        }

        public void run() {
            Log.d(TAG, "sending message - start");

            // Construct a DataRequest and send over the data layer
            PutDataMapRequest putDMR = PutDataMapRequest.create(path);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            request.setUrgent();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient, request).await();
            if (result.getStatus().isSuccess()) {
                Log.d(TAG, "DataMap: " + dataMap + " sent successfully to data layer ");
            }
            else {
                // Log an error
                Log.d(TAG, "ERROR: failed to send DataMap to data layer");
            }


            Task<DataItem> dataItemTask = Wearable.getDataClient(HomeActivity.this).putDataItem(request);

            dataItemTask.addOnSuccessListener(
                    dataItem -> Log.d(TAG, "Sending image was successful: " + dataItem));

        }
    }


    //Communication

    private void downloadHomeData(){
        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_HOMEPAGE, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if("Success".equals(response.getString("Response"))){
                                    JSONArray emails = response.getJSONArray("Data");
                                    final String name = response.getString("Name");
                                    final String surname = response.getString("Surname");
                                    initializeUI(name, surname, emails);
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

    private void downloadProfilePicture(final APIUtils.imageRequestCallback callback, String userEmail){

        String requestURL = WEBSERVICE_URL_IMAGES + "?Token=" + getAuthToken(HomeActivity.this) + "&Filename=" + userEmail + ".png";
        ImageRequest imageRequest = new ImageRequest(requestURL,
                response -> callback.onSuccess(response), 100, 100, ImageView.ScaleType.CENTER_CROP,null,
                error -> imageDownloadErrorHandler());


        Volley.newRequestQueue(this).add(imageRequest);


    }

    private void imageDownloadErrorHandler(){
        //Nothing for now
    }

    private void manageAutomatedSOS(Boolean setting){
        Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("AutomatedSOSOn", encryption.encryptOrNull(((setting)?"true":"false")));
        editor.apply();

        automatedSOSIcon.setImageResource((setting)?R.drawable.medic2:R.drawable.medic2_grey);
    }

    private void displayActivateAutomatedSOSDialog(){
        Boolean automatedSOSStatus = false;
        if(!(getAutomatedSOSStatus(this) == null || getAutomatedSOSStatus(this).equals("false"))){
            automatedSOSStatus = true;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this).create();
        alertDialog.setTitle("AutomatedSOS is " + ((automatedSOSStatus)?"ON":"OFF"));
        alertDialog.setMessage("Do you want to " + ((automatedSOSStatus)?"deactivate":"activate") + " AutomatedSOS?");
        Boolean finalAutomatedSOSStatus = automatedSOSStatus;
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                (dialog, which) -> {
                    dialog.dismiss();
                    manageAutomatedSOS(!finalAutomatedSOSStatus);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                (dialog, which) -> {
                    dialog.dismiss();
                });
        alertDialog.show();
    }
}

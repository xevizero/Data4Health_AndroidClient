package frassonlancellottilodi.data4health;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import frassonlancellottilodi.data4health.utils.APIUtils;
import frassonlancellottilodi.data4health.utils.Encryption;
import frassonlancellottilodi.data4health.utils.SquareImageView;

import static frassonlancellottilodi.data4health.utils.Constants.PHONE_DATA_PATH;
import static frassonlancellottilodi.data4health.utils.Constants.REQUEST_CURRENT_STEPS;
import static frassonlancellottilodi.data4health.utils.Constants.REQUEST_EMERGENCY_SOS;
import static frassonlancellottilodi.data4health.utils.Constants.REQUEST_SYNC_DATA_FROM_WATCH;
import static frassonlancellottilodi.data4health.utils.Constants.RESPONSE_CURRENT_STEPS;
import static frassonlancellottilodi.data4health.utils.Constants.WEARABLE_DATA_PATH;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_EMERGENCY_AUTOMATEDSOS;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_HOMEPAGE;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_IMAGES;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_MANAGE_AUTOMATEDSOS;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_PROFILE;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_SYNC_HEALTH_DATA;
import static frassonlancellottilodi.data4health.utils.SessionUtils.checkLogin;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getAuthToken;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getAutomatedSOSStatus;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getLoggedUserEmail;
import static frassonlancellottilodi.data4health.utils.SessionUtils.revokeAuthToken;
import static frassonlancellottilodi.data4health.utils.SessionUtils.setAutomatedSOSStatus;
import static frassonlancellottilodi.data4health.utils.UIUtils.displayErrorAlert;
import static frassonlancellottilodi.data4health.utils.UIUtils.getTitleFont;
import static frassonlancellottilodi.data4health.utils.UIUtils.pxFromDp;


//adb -d forward tcp:5601 tcp:5601

public class HomeActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        CapabilityClient.OnCapabilityChangedListener,
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener {

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
    private final static int SERVICE_REQUEST_CODE = 8, MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private final static long locationUpdateTime = 1000;
    private final static float  locationUpdateDistance = 25;
    private LocationManager mLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin(getApplicationContext(), this, false);
        setContentView(R.layout.activity_home);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        initializeLocationService();
        downloadHomeData();
    }

    private void initializeLocationService() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    private void initializeUI(String name, String surname, JSONArray emails, boolean automatedSOSOn) throws JSONException {

        titleView = findViewById(R.id.titlehome);

        titleView.setOnClickListener(v -> {
            final String emergencyType ="FALL";
            boolean locationAccurate = false;
            double latitude = 0f, longitude = 0f;
            Log.d(TAG, String.valueOf(checkLocationPermission()));
            if(checkLocationPermission()){
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d(TAG, "gg" + String.valueOf(location.getAccuracy()));

                if (location.getAccuracy()<50){
                    locationAccurate = true;
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
            sendEmergencyRequest(emergencyType, locationAccurate, latitude, longitude);
        });


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


        setAutomatedSOSStatus(this, automatedSOSOn);

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
        for (int i = 0; i < emails.length(); i++) {
            final String ext_email = emails.getJSONObject(i).getString("Email");
            peopleBar.addView(generatePersonImageContainer(ext_email));
        }
        peopleBar.addView(addFriendButtonContainer);
        addFriendButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });
        automatedSOSButton.setOnClickListener(v -> displayActivateAutomatedSOSDialog());
        automatedSOSIcon.setImageResource((getAutomatedSOSStatus(this) != null && getAutomatedSOSStatus(this).equals("true")) ? R.drawable.medic2 : R.drawable.medic2_grey);
        if (getAutomatedSOSStatus(this).equals("true")) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdateTime,
                        locationUpdateDistance, mLocationListener);
            }
        }else{
            mLocationManager.removeUpdates(mLocationListener);
        }

    }

    private RelativeLayout generatePersonImageContainer(String email) {
        RelativeLayout pictureContainer = new RelativeLayout(this);
        LinearLayout.LayoutParams paramsPictureContainer = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        paramsPictureContainer.setMargins(0, 0, 0, 0);
        pictureContainer.setLayoutParams(paramsPictureContainer);
        pictureContainer.setGravity(Gravity.CENTER_VERTICAL | RelativeLayout.CENTER_HORIZONTAL);

        SquareImageView photoImageView = new SquareImageView(this);
        RelativeLayout.LayoutParams paramsImage = new RelativeLayout.LayoutParams(pxFromDp(this, 56), pxFromDp(this, 56));
        //paramsImage.setMargins(30,30,30,30);
        photoImageView.setLayoutParams(paramsImage);
        photoImageView.setImageResource(R.drawable.bgspinner);
        photoImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoImageView.setElevation(pxFromDp(this, 6));
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
        googleAPIConnect(googleApiClient);
    }

    public static GoogleApiClient googleAPIClientBuild(Activity activity, GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener failedListener) {

        return new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(failedListener)
                .addApi(Wearable.API)
                .build();
    }

    public static void googleAPIConnect(final GoogleApiClient mGoogleApiClient) {
        Log.d(TAG, "google API connect called");
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
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
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended i= " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!authInProgress) {
            Log.d(TAG, "!AUTHINPROG");
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(this, 1);
            } catch (IntentSender.SendIntentException e) {
                Log.d(TAG, "SendIntentExc: " + e.toString());
            }
        } else {
            Log.d(TAG, "authInProgress");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "OnActivityResult called");
        if (requestCode == 1) {
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Result_OK");
                if (!googleApiClient.isConnecting() && !googleApiClient.isConnected()) {
                    Log.d(TAG, "Calling googleApiClient.connect again");
                    googleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
                } else {
                    onConnected(null);
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "RESULT_CANCELED");
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

    private class SendToDataLayerThread extends Thread {
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
            Wearable.DataApi.deleteDataItems(googleApiClient, putDMR.getUri());
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            request.setUrgent();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient, request).await();
            if (result.getStatus().isSuccess()) {
                Log.d(TAG, "DataMap: " + dataMap + " sent successfully to data layer ");
            } else {
                // Log an error
                Log.d(TAG, "ERROR: failed to send DataMap to data layer");
            }


            Task<DataItem> dataItemTask = Wearable.getDataClient(HomeActivity.this).putDataItem(request);

            dataItemTask.addOnSuccessListener(
                    dataItem -> Log.d(TAG, "Sending image was successful: " + dataItem));

        }
    }


    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d("data", "changedactivity");

        DataMap dataMap;
        for (DataEvent event : dataEventBuffer) {
            Log.d("data", "received");

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(PHONE_DATA_PATH)) {
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.v("HomeActivity", "DataMap received on phone: " + dataMap);
                    handleWearDataMap(dataMap);
                }
            }
        }
    }

    private void handleWearDataMap(DataMap dataMap) {
        String requestName = dataMap.getString("request");
        if (requestName.equals(REQUEST_CURRENT_STEPS)) {
            downloadStepData();
        }
        if (requestName.equals(REQUEST_SYNC_DATA_FROM_WATCH)) {
            Log.d("DATA UPDATE", "" + dataMap);
            final String heartrate = dataMap.getString("heartrate");
            final String steps = dataMap.getString("steps");
            sendHealthData(heartrate, steps);
        }
        if (requestName.equals(REQUEST_EMERGENCY_SOS)) {
            final String emergencyType = dataMap.getString("emergency");
            boolean locationAccurate = false;
            double latitude = 0f, longitude = 0f;
            Log.d(TAG, String.valueOf(checkLocationPermission()));
            if(checkLocationPermission()){
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Log.d(TAG, "gg" + String.valueOf(location.getAccuracy()));

                if (location.getAccuracy()<50){
                    locationAccurate = true;
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }
            sendEmergencyRequest(emergencyType, locationAccurate, latitude, longitude);
        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
    }


    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {

    }

    //Communication

    private void downloadHomeData() {
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
                                if ("Success".equals(response.getString("Response"))) {
                                    JSONArray emails = response.getJSONArray("Data");
                                    final String name = response.getString("Name");
                                    final String surname = response.getString("Surname");
                                    final boolean automatedSOSOn = response.getBoolean("AutomatedSOSOn");
                                    initializeUI(name, surname, emails, automatedSOSOn);
                                } else if ("Error".equals(response.getString("Response"))) {
                                    int errorCode = Integer.valueOf(response.getString("Code"));
                                    switch (errorCode) {
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

    private void downloadStepData() {

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
                                if ("Success".equals(response.getString("Response"))) {
                                    JSONObject responseData = response.getJSONObject("Data");
                                    final String steps = responseData.getString("Steps");
                                    DataMap dataMap = new DataMap();
                                    dataMap.putLong("time", new Date().getTime());
                                    dataMap.putString("request", RESPONSE_CURRENT_STEPS);
                                    dataMap.putString("steps", steps);
                                    dataMap.putString("automatedSOS", getAutomatedSOSStatus(HomeActivity.this));
                                    new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap).start();
                                } else if ("Error".equals(response.getString("Response"))) {
                                    int errorCode = Integer.valueOf(response.getString("Code"));
                                    switch (errorCode) {
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

    private void sendHealthData(String heartrate, String steps) {

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
            POSTParams.put("Steps", steps);
            POSTParams.put("Heartrate", heartrate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_SYNC_HEALTH_DATA, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if ("Success".equals(response.getString("Response"))) {

                                } else if ("Error".equals(response.getString("Response"))) {
                                    int errorCode = Integer.valueOf(response.getString("Code"));
                                    switch (errorCode) {
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

    private void manageAutomatedSOSRequest(Boolean automatedSOSStatusRequest) {

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
            POSTParams.put("AutomatedSOS", automatedSOSStatusRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_MANAGE_AUTOMATEDSOS, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if ("Success".equals(response.getString("Response"))) {
                                    Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("AutomatedSOSOn", encryption.encryptOrNull(((automatedSOSStatusRequest) ? "true" : "false")));
                                    editor.apply();

                                    automatedSOSIcon.setImageResource((automatedSOSStatusRequest) ? R.drawable.medic2 : R.drawable.medic2_grey);

                                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        checkLocationPermission();
                                    }else{
                                        if(getAutomatedSOSStatus(HomeActivity.this).equals("true")) {
                                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdateTime,
                                                    locationUpdateDistance, mLocationListener);
                                        }else{
                                            mLocationManager.removeUpdates(mLocationListener);
                                        }
                                    }
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

    private void sendEmergencyRequest(String requestType, Boolean locationAccurate, Double locationLat, Double locationLong){

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
            POSTParams.put("Type", requestType);
            POSTParams.put("Accurate", locationAccurate);
            POSTParams.put("Latitude", locationLat);
            POSTParams.put("Longitude", locationLong);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_EMERGENCY_AUTOMATEDSOS, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if("Success".equals(response.getString("Response"))){
                                    displayErrorAlert("AutomatedSOS emergency detected!", "SOS request sent!", this);
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
                    if (!finalAutomatedSOSStatus == true){
                        checkLocationPermission();
                    }
                    manageAutomatedSOSRequest(!finalAutomatedSOSStatus);
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                (dialog, which) -> {
                    dialog.dismiss();
                });
        alertDialog.show();
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            Log.d(TAG, String.valueOf(location.getAccuracy()));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("AutomatedSOS")
                        .setMessage("This functionality requires access to your location to work properly.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(HomeActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (getAutomatedSOSStatus(this).equals("true")) {
                                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationUpdateTime,
                                        locationUpdateDistance, mLocationListener);
                            }else{
                                mLocationManager.removeUpdates(mLocationListener);

                        }
                        }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

}

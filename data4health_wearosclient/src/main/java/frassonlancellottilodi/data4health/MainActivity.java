package frassonlancellottilodi.data4health;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wear.ambient.AmbientModeSupport;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

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
import java.util.Date;

import frassonlancellottilodi.data4health.utils.Encryption;

import static frassonlancellottilodi.data4health.utils.Constants.FALL;
import static frassonlancellottilodi.data4health.utils.Constants.PHONE_DATA_PATH;
import static frassonlancellottilodi.data4health.utils.Constants.REQUEST_CURRENT_STEPS;
import static frassonlancellottilodi.data4health.utils.Constants.REQUEST_EMERGENCY_SOS;
import static frassonlancellottilodi.data4health.utils.Constants.REQUEST_SYNC_DATA_FROM_WATCH;
import static frassonlancellottilodi.data4health.utils.Constants.RESPONSE_CURRENT_STEPS;
import static frassonlancellottilodi.data4health.utils.Constants.WEARABLE_DATA_PATH;

public class MainActivity extends WearableActivity implements AmbientModeSupport.AmbientCallbackProvider,
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener,
        CapabilityClient.OnCapabilityChangedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private TextView textSteps, textHeart, textAutomatedSOS;


    private static final String TAG = "MainActivityWear";

    private GoogleApiClient googleApiClient;
    private boolean authInProgress = false;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;
    private Sensor stepSensor, heartSensor, accelerometer;
    private int maxdelay = 0, mSteps, mCounterSteps, mPreviousCounterSteps, syncTimeout = 20000;
    private final int MY_PERMISSIONS_REQUEST_BODY_SENSOR = 202;
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private long timeFallStart, timeFallEnd, fallThreshold = 150, timeDownStart, timeDownEnd, downThreshold = 4000;
    private boolean isFalling = false, isDown = false, isAutomatedSOSOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();
        initializeSensors();

    }

    private void initializeUI(){
        textHeart = findViewById(R.id.textHeart);
        textSteps = findViewById(R.id.textSteps);
        textAutomatedSOS = findViewById(R.id.textAutomatedSOS);
        final String automatedSOSOn = readSharedPrefs("AutomatedSOSOn");
        isAutomatedSOSOn = Boolean.valueOf(automatedSOSOn);
        textAutomatedSOS.setText((isAutomatedSOSOn)?"AUTOMATED SOS: ON":"AUTOMATED SOS: OFF");
        Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/Montserrat-Light.ttf");
        textHeart.setTypeface(font);
        textSteps.setTypeface(font);
        textAutomatedSOS.setTypeface(font);

        // Enables Always-on
        setAmbientEnabled();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initializeSensors(){
        if (checkSelfPermission(Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "NOT ALREADY GRANTED");
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS},
                    MY_PERMISSIONS_REQUEST_BODY_SENSOR);

        } else {
            Log.d(TAG, "ALREADY GRANTED");
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        heartSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorEventListener = mSensorListener();
        mSensorManager.registerListener(
                mSensorEventListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL, maxdelay);
        mSensorManager.registerListener(
                mSensorEventListener, heartSensor, SensorManager.SENSOR_DELAY_NORMAL, maxdelay);
        if(isAutomatedSOSOn){
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(
                    mSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, maxdelay);
        }


        //writeSharedPrefs("lastSyncTime", String.valueOf(0));
    }



    private SensorEventListener mSensorListener(){

        final SensorEventListener mListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                // BEGIN_INCLUDE(sensorevent)
                // store the delay of this event
                //recordDelay(event);
                //final String delayString = getDelayString();

                if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    mSteps = Integer.valueOf(readSharedPrefs("currentSteps"));
                    mSteps += event.values.length;
                    Log.i(TAG,
                            "New step detected by STEP_DETECTOR sensor. Total step count: " + mSteps);
                    textSteps.setText(""+ mSteps);
                    writeSharedPrefs("currentSteps", String.valueOf(mSteps));


                } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

                }else if(event.sensor.getType() == Sensor.TYPE_HEART_RATE){

                    String heartRateValue = Integer.toString(Math.round(event.values.length > 0 ? event.values[0] : 0.0f));
                    textHeart.setText(heartRateValue);
                    writeSharedPrefs("currentHeartRate", heartRateValue);

                }else if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

                    if(isAutomatedSOSOn){

                        // alpha is calculated as t / (t + dT)
                        // with t, the low-pass filter's time-constant
                        // and dT, the event delivery rate

                        final float alpha = 0.8f;

                        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                        linear_acceleration[0] = event.values[0] - gravity[0];
                        linear_acceleration[1] = event.values[1] - gravity[1];
                        linear_acceleration[2] = event.values[2] - gravity[2];

                        Double linearAccelerationResultant = Math.sqrt(linear_acceleration[0] * linear_acceleration[0] + linear_acceleration[1] * linear_acceleration[1] + linear_acceleration[2] * linear_acceleration[2]);
                        //String linearAccelerationStr = String.valueOf(linearAccelerationResultant);
                        //Log.d(TAG, "acce "+linearAccelerationStr + " TEMPO " + System.currentTimeMillis());
                        if(linearAccelerationResultant > 70 && linearAccelerationResultant < 100){
                            isDown = false;
                            if(isFalling){

                            }else{
                                isFalling = true;
                                timeFallStart = System.currentTimeMillis();
                            }
                        }else if(linearAccelerationResultant < 1f){
                                if(isFalling && !isDown){
                                    isFalling = false;
                                    timeFallEnd = System.currentTimeMillis();
                                    final long fallTime = timeFallEnd - timeFallStart;
                                    if (fallTime > fallThreshold){
                                        isDown = true;
                                        timeDownStart = System.currentTimeMillis();
                                    }
                                }else if (!isFalling && isDown){
                                    timeDownEnd = System.currentTimeMillis();
                                    final long downTime = timeDownEnd - timeDownStart;
                                    if (downTime > downThreshold){
                                        isDown = false;
                                        sendEmergencyRequest();
                                    }
                                }else{
                                    isFalling = false;
                                    isDown = false;
                                }
                        }else if(linearAccelerationResultant < 100){

                        }else{
                            isDown = false;
                            isFalling = false;
                        }



                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        return mListener;
    };

    private void sendHealthUpdateToPhone(){

        DataMap dataMap = new DataMap();
        dataMap.putLong("time", new Date().getTime());
        dataMap.putString("request", REQUEST_SYNC_DATA_FROM_WATCH);
        dataMap.putString("steps", readSharedPrefs("currentSteps"));
        dataMap.putString("heartrate", readSharedPrefs("currentHeartRate"));
        new SendToDataLayerThread(PHONE_DATA_PATH, dataMap).start();
    }

    private void sendEmergencyRequest(){
        DataMap dataMap = new DataMap();
        dataMap.putLong("time", new Date().getTime());
        dataMap.putString("request", REQUEST_EMERGENCY_SOS);
        dataMap.putString("emergency", FALL);
        new SendToDataLayerThread(PHONE_DATA_PATH, dataMap).start();
    }

    private void requestCurrentStepsCount(){

        DataMap dataMap = new DataMap();
        dataMap.putLong("time", new Date().getTime());
        dataMap.putString("request", REQUEST_CURRENT_STEPS);
        new SendToDataLayerThread(PHONE_DATA_PATH, dataMap).start();
    }

    private void startDataSync(){
        Handler handler = new Handler();
        Runnable syncData = new Runnable() {
            @Override
            public void run() {
                if(googleApiClient.isConnected()) {
                    sendHealthUpdateToPhone();
                }
                handler.postDelayed(this, syncTimeout);
            }
        };
        handler.postDelayed(syncData, syncTimeout);
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
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected called");
        requestCurrentStepsCount();
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return null;
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {

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
                if (path.equals(WEARABLE_DATA_PATH)) {
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    Log.v("MainActivity", "DataMap received on watch: " + dataMap);
                    handlePhoneDataMap(dataMap);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Instantiates clients without member variables, as clients are inexpensive to create and
        // won't lose their listeners. (They are cached and shared between GoogleApi instances.)
        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
        Wearable.getCapabilityClient(this)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
        mSensorManager.registerListener(
                mSensorEventListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL, maxdelay);
        mSensorManager.registerListener(
                mSensorEventListener, heartSensor, SensorManager.SENSOR_DELAY_NORMAL, maxdelay);
        if(isAutomatedSOSOn){
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(
                    mSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, maxdelay);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
        mSensorManager.unregisterListener(mSensorEventListener);
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
            Wearable.DataApi.deleteDataItems(googleApiClient,putDMR.getUri());
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


            Task<DataItem> dataItemTask = Wearable.getDataClient(MainActivity.this).putDataItem(request);

            dataItemTask.addOnSuccessListener(
                    dataItem -> Log.d(TAG, "Sending image was successful: " + dataItem));

        }
    }

    private void handlePhoneDataMap(DataMap dataMap){
        String requestName = dataMap.getString("request");
        if (requestName.equals(RESPONSE_CURRENT_STEPS)){
            String steps = dataMap.getString("steps");
            String automatedSOSOn = dataMap.getString("automatedSOS");
            writeSharedPrefs("currentSteps", steps);
            writeSharedPrefs("AutomatedSOSOn", automatedSOSOn);
            textSteps.setText(steps);
            boolean automatedSOSStatus = Boolean.valueOf(automatedSOSOn);
            textAutomatedSOS.setText((automatedSOSStatus)?"AUTOMATED SOS: ON":"AUTOMATED SOS: OFF");
            isAutomatedSOSOn = automatedSOSStatus;
            startDataSync();
        }
    }

    private void writeSharedPrefs(String key, String value){
        Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, encryption.encryptOrNull(value));
        editor.apply();
    }

    private String readSharedPrefs(String key){
        Encryption encryption = Encryption.getDefault("Kovfefe", "Harambe", new byte[16]);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        return encryption.decryptOrNull(preferences.getString(key,""));
    }
}

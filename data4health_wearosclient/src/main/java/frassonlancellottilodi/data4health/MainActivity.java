package frassonlancellottilodi.data4health;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wear.ambient.AmbientModeSupport;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import static java.text.DateFormat.getTimeInstance;

public class MainActivity extends WearableActivity implements AmbientModeSupport.AmbientCallbackProvider,
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener,
        CapabilityClient.OnCapabilityChangedListener{

    private TextView textSteps, textHeart, textAutomatedSOS;


    private static final String TAG = "MainActivityWear";
    private static final String WEARABLE_DATA_PATH = "/wearable_data";

    GoogleApiClient googleApiClient;

    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;
    private Sensor stepSensor, heartSensor;
    private int maxdelay = 0;
    private final int MY_PERMISSIONS_REQUEST_BODY_SENSOR = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textHeart = findViewById(R.id.textHeart);
        textSteps = findViewById(R.id.textSteps);
        textAutomatedSOS = findViewById(R.id.textAutomatedSOS);
        textAutomatedSOS.setText("AUTOMATED SOS: OFF");
        Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/Montserrat-Light.ttf");
        textHeart.setTypeface(font);
        textSteps.setTypeface(font);
        textAutomatedSOS.setTypeface(font);
        // Enables Always-on
        setAmbientEnabled();


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
        //startService(new Intent(this, ListenerService.class));

    }

    private int mSteps, mCounterSteps, mPreviousCounterSteps;


    private SensorEventListener mSensorListener(){
        Log.d(TAG, "HERES");


        final SensorEventListener mListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                Log.d(TAG, "HERE");
                // BEGIN_INCLUDE(sensorevent)
                // store the delay of this event
                //recordDelay(event);
                //final String delayString = getDelayString();

                if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    // A step detector event is received for each step.
                    // This means we need to count steps ourselves

                    mSteps += event.values.length;

                    Log.i(TAG,
                            "New step detected by STEP_DETECTOR sensor. Total step count: " + mSteps);
                    textSteps.setText(""+ mSteps);

                } else if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

                    /*
                    A step counter event contains the total number of steps since the listener
                    was first registered. We need to keep track of this initial value to calculate the
                    number of steps taken, as the first value a listener receives is undefined.
                     */
                    if (mCounterSteps < 1) {
                        // initial value
                        mCounterSteps = (int) event.values[0];
                    }

                    // Calculate steps taken based on first counter value received.
                    mSteps = (int) event.values[0] - mCounterSteps;

                    // Add the number of steps previously taken, otherwise the counter would start at 0.
                    // This is needed to keep the counter consistent across rotation changes.
                    mSteps = mSteps + mPreviousCounterSteps;

                    Log.i(TAG, "New step detected by STEP_COUNTER sensor. Total step count: " + mSteps);
                    // END_INCLUDE(sensorevent)
                }else if(event.sensor.getType() == Sensor.TYPE_HEART_RATE){

                    String heartRateValue = Integer.toString(Math.round(event.values.length > 0 ? event.values[0] : 0.0f));
                    textHeart.setText(heartRateValue);

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        return mListener;
    };

    @Override
    protected void onStart() {
        super.onStart();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      /*  Log.d(TAG, "OnActivityResult called");
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
        }*/
    }

    // Disconnect from the data layer when the Activity stops
    @Override
    protected void onStop() {
      /*  Log.d(TAG, "onStop called");
/*
        if (null != googleApiClient && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }*/
        super.onStop();
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
                if (path.equals(WEARABLE_DATA_PATH)) {}
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                Log.v("MainActivity", "DataMap received on watch: " + dataMap);
            }
        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);
    }
    // [END parse_dataset]

    class SendToDataLayerThread extends Thread {
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread(String p, DataMap data) {
            path = p;
            dataMap = data;
        }

        public void run() {
            // Construct a DataRequest and send over the data layer
            PutDataMapRequest putDMR = PutDataMapRequest.create(path);
            putDMR.getDataMap().putAll(dataMap);
            PutDataRequest request = putDMR.asPutDataRequest();
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(googleApiClient, request).await();
            if (result.getStatus().isSuccess()) {
                Log.v("myTag", "DataMap: " + dataMap + " sent successfully to data layer ");
            }
            else {
                // Log an error
                Log.v("myTag", "ERROR: failed to send DataMap to data layer");
            }
        }
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
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
        mSensorManager.unregisterListener(mSensorEventListener);
    }
}

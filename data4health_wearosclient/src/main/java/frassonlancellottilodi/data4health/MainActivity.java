package frassonlancellottilodi.data4health;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.wear.ambient.AmbientModeSupport;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import frassonlancellottilodi.data4health.utils.ListenerService;

import static java.text.DateFormat.getTimeInstance;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MainActivity extends WearableActivity implements OnDataPointListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AmbientModeSupport.AmbientCallbackProvider,
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener,
        CapabilityClient.OnCapabilityChangedListener{

    private TextView mTextView;


    private static final String TAG = "CCC";
    private static final String AUTH_PENDING = "isAuthPending";
    GoogleApiClient googleApiClient;
    private boolean authInProgress = false;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int maxdelay = 0;
    private final int MY_PERMISSIONS_REQUEST_BODY_SENSOR = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);

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
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Log.d(TAG, "ARES" + String.valueOf(mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) == null));
        Log.d(TAG, "ARES" + String.valueOf(mSensorManager.getSensorList(Sensor.TYPE_STEP_DETECTOR)));

        mSensorManager.registerListener(
                getmListener(), mSensor, SensorManager.SENSOR_DELAY_NORMAL, maxdelay);
        //startService(new Intent(this, ListenerService.class));

    }

    private int mSteps, mCounterSteps, mPreviousCounterSteps;


    private SensorEventListener getmListener(){
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
        //very important that the following lines are called in onStart
        //when they are called in onCreate, when the permission fragment opens up, onStop gets called which disconnects the api client.
        //after which it needs to be reConnected which does not happen as the apiClient is built in onCreate
        //Hence these should be called in onStart or probably onResume.
        //googleApiClient = googleFitBuild(this, this, this);
        //googleFitConnect(this, googleApiClient);
    }

    /*
    public static GoogleApiClient googleFitBuild(Activity activity, GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener failedListener){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .build();

        return new GoogleApiClient.Builder(activity)
//without GOOGLE_SIGN_IN_API, RESULT_CANCELED is always the output
//The new version of google Fit requires that the user authenticates with gmail account
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Wearable.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(failedListener)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.SENSORS_API)
                .build();
    }*/
/*
    //runs an automated Google Fit connect sequence
    public static void googleFitConnect(final Activity activity, final GoogleApiClient mGoogleApiClient){
        Log.d(TAG, "google fit connect called");
        if(!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Log.d(TAG, "Google API connected");
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    activity.startActivityForResult(signInIntent, 1);
                }
                @Override
                public void onConnectionSuspended(int i) {

                }
            });
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
    }*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {
      /*  Log.d(TAG, "onConnected called");

        //WEARABLE
        String WEARABLE_DATA_PATH = "/wearable_data";

        // Create a DataMap object and send it to the data layer
        DataMap dataMap = new DataMap();
        dataMap.putLong("time", new Date().getTime());
        dataMap.putString("hole", "1");
        dataMap.putString("front", "250");
        dataMap.putString("middle", "260");
        dataMap.putString("back", "270");
        //Requires a new thread to avoid blocking the UI
        new SendToDataLayerThread(WEARABLE_DATA_PATH, dataMap).start();



        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .build();
        Log.d(TAG, "DataSourcetype: " + dataSourceRequest.getDataTypes().toString());


        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                Log.d(TAG, "onResult in Result Callback called");
                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                    if(DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataSource.getDataType())) {
                        Log.d(TAG, "type step");
                        registerStepsDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(googleApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -52);
        long startTime = cal.getTimeInMillis();

        PendingResult<DataReadResult> result = Fitness.HistoryApi.readData(googleApiClient, new DataReadRequest.Builder()
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .build());
        result.setResultCallback(new ResultCallback<DataReadResult>() {
            @Override
            public void onResult(@NonNull DataReadResult dataReadResult) {
                Log.d(TAG, "ASD");
                printData(dataReadResult);
            }
        });*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        /*Log.d(TAG, "Connection suspended i= " + i);*/
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
      /*  if( !authInProgress ) {
            Log.d(TAG, "!AUTHINPROG" +connectionResult.getErrorCode());
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(this, 1);
            } catch(IntentSender.SendIntentException e ) {
                Log.d(TAG, "SendIntentExc: " + e.toString());
            }
        } else {
            Log.d(TAG, "authInProgress" );
        }*/
    }

    private void registerStepsDataListener(DataSource dataSource, DataType dataType) {
/*
        SensorRequest request = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(dataType)
                .setSamplingRate(3, SECONDS )
                .build();

        Fitness.SensorsApi.add(googleApiClient, request, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "SensorApi successfully added" );

                        }
                    }
                });*/

    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
       /* for( final Field field : dataPoint.getDataType().getFields() ) {
            final Value value = dataPoint.getValue( field );
            Log.d(TAG, "Field Name: " + field.getName() + " Value: " + value.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Field: " + field.getName() + " Value: " + value, Toast.LENGTH_SHORT).show();
                }
            });
        }*/
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

/*
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy called");
        super.onDestroy();

        Fitness.SensorsApi.remove( googleApiClient, this )
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            googleApiClient.disconnect();
                        }
                    }
                });
    }
*//*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Onsaveinstance called");
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }*/

    /**
     * Logs a record of the query result. It's possible to get more constrained data sets by
     * specifying a data source or data type, but for demonstrative purposes here's how one would dump
     * all the data. In this sample, logging also prints to the device screen, so we can see what the
     * query returns, but your app should not log fitness information as a privacy consideration. A
     * better option would be to dump the data you receive to a local data directory to avoid exposing
     * it to other applications.
     *//*
    public static void printData(DataReadResult dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.i(
                    TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.i(TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
        // [END parse_read_data_result]
    }*/
/*
    // [START parse_dataset]
    private static void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
            }
        }
    }*/

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return null;
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {

    }

    private static final String WEARABLE_DATA_PATH = "/wearable_data";

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
                Log.v("myTag", "DataMap received on watch: " + dataMap);
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
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getDataClient(this).removeListener(this);
        Wearable.getMessageClient(this).removeListener(this);
        Wearable.getCapabilityClient(this).removeListener(this);
    }
}

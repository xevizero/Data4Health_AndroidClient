package frassonlancellottilodi.data4health_wearosclient;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.Button;
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
import com.google.android.gms.fitness.HistoryApi;
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
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.wearable.Wearable;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MainActivity extends WearableActivity   implements OnDataPointListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private TextView mTextView;


    private static final String TAG = "CCC";
    private static final String AUTH_PENDING = "isAuthPending";
    GoogleApiClient googleApiClient;
    private boolean authInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //very important that the following lines are called in onStart
        //when they are called in onCreate, when the permission fragment opens up, onStop gets called which disconnects the api client.
        //after which it needs to be reConnected which does not happen as the apiClient is built in onCreate
        //Hence these should be called in onStart or probably onResume.
        googleApiClient = googleFitBuild(this, this, this);
        googleFitConnect(this, googleApiClient);
    }

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
    }

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
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected called");
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
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended i= " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if( !authInProgress ) {
            Log.d(TAG, "!AUTHINPROG" +connectionResult.getErrorCode());
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

    private void registerStepsDataListener(DataSource dataSource, DataType dataType) {

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
                });

    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
        for( final Field field : dataPoint.getDataType().getFields() ) {
            final Value value = dataPoint.getValue( field );
            Log.d(TAG, "Field Name: " + field.getName() + " Value: " + value.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Field: " + field.getName() + " Value: " + value, Toast.LENGTH_SHORT).show();
                }
            });
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
    protected void onStop() {
        Log.d(TAG, "Onstop called");
        super.onStop();

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Onsaveinstance called");
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    /**
     * Logs a record of the query result. It's possible to get more constrained data sets by
     * specifying a data source or data type, but for demonstrative purposes here's how one would dump
     * all the data. In this sample, logging also prints to the device screen, so we can see what the
     * query returns, but your app should not log fitness information as a privacy consideration. A
     * better option would be to dump the data you receive to a local data directory to avoid exposing
     * it to other applications.
     */
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
    }

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
    }
    // [END parse_dataset]

}

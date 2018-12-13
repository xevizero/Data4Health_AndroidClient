package frassonlancellottilodi.data4health.utils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Listener service that should manage the connection between the wearable and the phone.
 * Currently totally unreliable probably due to Android battery optimizations, needs further work.
 * At the moment the connection is entirely handled by live listeners in the home activity and in the Main wear activity.
 */
public class ListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        CapabilityClient.OnCapabilityChangedListener,
        DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener{

    private final String TAG = "AppListenerService";
    private GoogleApiClient googleApiClient;
    private boolean authInProgress = false;

    @Override
    public void onCreate(){
        Log.d(TAG, "ON");
        googleApiClient = googleAPIClientBuild(this, this);
        googleAPIConnect(googleApiClient);
        Wearable.getDataClient(this).addListener(this);
        Wearable.getMessageClient(this).addListener(this);
    }


    private static final String PHONE_DATA_PATH = "/phone_data";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "changed");

        DataMap dataMap;
        for (DataEvent event : dataEvents) {
            Log.d(TAG, "received");

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(PHONE_DATA_PATH)) {}
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                Log.v(TAG, "DataMap received on phone: " + dataMap);
            }
        }
    }

    public GoogleApiClient googleAPIClientBuild(GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener failedListener){

        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(failedListener)
                .addApi(Wearable.API)
                .build();
    }

    public void googleAPIConnect(final GoogleApiClient mGoogleApiClient){
        Log.d(TAG, "google API connect called");
        if(!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if( !authInProgress ) {
            Log.d(TAG, "!AUTHINPROG");
            try {
                authInProgress = true;
                //connectionResult.startResolutionForResult(this, 1);
            } catch(Exception e ) {
                Log.d(TAG, "SendIntentExc: " + e.toString());
            }
        } else {
            Log.d(TAG, "authInProgress" );
        }
    }

}
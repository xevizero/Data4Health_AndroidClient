package frassonlancellottilodi.data4health.utils;

import android.util.Log;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Listener service that should manage the connection between the wearable and the phone.
 * Currently totally unreliable probably due to Android battery optimizations, needs further work.
 * At the moment the connection is entirely handled by live listeners in the home activity and in the Main wear activity.
 */
public class ListenerService extends WearableListenerService{

    @Override
    public void onCreate(){
        Log.d("WEARSERVICE", "ON");
    }

    private static final String WEARABLE_DATA_PATH = "/wearable_data";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("ListenerService", "onMessageReceived: " + messageEvent);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("data", "changed");

        DataMap dataMap;
        for (DataEvent event : dataEvents) {
            Log.d("data", "received");

            // Check the data type
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // Check the data path
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_DATA_PATH)) {}
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                Log.v("ListenerService", "DataMap received on watch: " + dataMap);
            }
        }
    }
}
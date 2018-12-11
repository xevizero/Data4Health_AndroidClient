package frassonlancellottilodi.data4health.utils;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService{

    @Override
    public void onCreate(){
        Log.d("APPSERVICE", "ON");
    }

    private static final String PHONE_DATA_PATH = "/phone_data";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("AppListenerService", "onMessageReceived: " + messageEvent);
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
                if (path.equals(PHONE_DATA_PATH)) {}
                dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                Log.v("ListenerService", "DataMap received on phone: " + dataMap);
            }
        }
    }
}
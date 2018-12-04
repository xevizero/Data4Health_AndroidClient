package frassonlancellottilodi.data4health.utils;

import android.graphics.Bitmap;

public class APIUtils {
    public interface imageRequestCallback{
        void onSuccess(Bitmap response);
    }
}

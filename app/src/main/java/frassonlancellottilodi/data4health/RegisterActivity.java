package frassonlancellottilodi.data4health;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RegisterActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient googleApiClient;
    private static final String AUTH_PENDING = "isAuthPending";
    private boolean authInProgress = false;
    private String TAG = "RegisterActivity";
    String downloadUrl;

    private final String WEBSERVICEURL = "http://10.0.2.2:5000/android/register?name=Matteo&surname=Lodi&email=matteo@tititi3&userPhoneNumber=333&birthday=2015-10-11&password=ciao&sex=M&automatedSOSOn=True&developerAccount=True&anonymousDataSharingON=True";
    //--host=192.168.43.60

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        RequestQueue queue = Volley.newRequestQueue(this);

        //String requestURL = WEBSERVICEURL + "register?name=Matteo&surname=Lodi&email=matteo@tititi1&userPhoneNumber=333&birthday=2015-10-11&password=ciao&sex=M&automatedSOSOn=True&developerAccount=True&anonymousDataSharingON=True";
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, WEBSERVICEURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(TAG, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Log.d(TAG, error.getLocalizedMessage());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

/*

        if (Plus.PeopleApi.getCurrentPerson(googleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi
                    .getCurrentPerson(googleApiClient);
            String go_Id = currentPerson.getUrl().split("/")[3];
            downloadUrl = "https://www.googleapis.com/plus/v1/people/" + go_Id + "?fields=image&key=AIzaSyBXlUQtVglaZvrrV_IBmbAT9pzJdUfEuXQ";
        }
*/
    }

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
        new DownloadUrl().execute(downloadUrl);

    }

    public static GoogleApiClient googleFitBuild(Activity activity, GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener failedListener){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        return new GoogleApiClient.Builder(activity)
                //without GOOGLE_SIGN_IN_API, RESULT_CANCELED is always the output
                //The new version of google Fit requires that the user authenticates with gmail account
                .addApi(Plus.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(failedListener)
                .addApi(Wearable.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();
    }

    //runs an automated Google Fit connect sequence
    public void googleFitConnect(final Activity activity, final GoogleApiClient mGoogleApiClient){
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


    class DownloadUrl extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        boolean failure = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            try {
                int success;



                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("data", ""));

                Log.d("request!", "starting");
                org.apache.http.HttpResponse response = null;
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    request.setURI(new URI(downloadUrl));
                    response = client.execute(request);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                JSONObject json = null;
                try {
                    InputStream pre_json = response.getEntity().getContent();
                    json = new JSONObject(convertStreamToString(pre_json));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //JSONParser jsonParser = new JSONParser();
                //ArrayList<String> subcats = new ArrayList<String>();
                //JSONObject json = jsonParser.makeHttpRequest(
                //       downloadUrl, "GET", params);
                JSONObject ob = json.getJSONObject(downloadUrl);
                downloadUrl = ob.getString("url");
                downloadUrl = downloadUrl + "0";


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                Log.d("NO!","" + e);
            }


            return null;

        }
        public String convertStreamToString(InputStream inputStream) throws IOException {
            if (inputStream != null) {
                Writer writer = new StringWriter();

                char[] buffer = new char[1024];
                try {
                    Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),1024);
                    int n;
                    while ((n = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, n);
                    }
                } finally {
                    inputStream.close();
                }
                return writer.toString();
            } else {
                return "";
            }
        }
        protected void onPostExecute(String file_url) {
            new ImageDownloader().execute(downloadUrl);
        }
    }

    private Bitmap downloadBitmap(String url) {
        Log.d("url", url);
        // initilize the default HTTP client object
        final DefaultHttpClient client = new DefaultHttpClient();

        //forming a HttoGet request
        final HttpGet getRequest = new HttpGet(url);
        try {

            org.apache.http.HttpResponse response = client.execute(getRequest);

            //check 200 OK for success
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + url);
                return null;

            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    // getting contents from the stream
                    inputStream = entity.getContent();

                    // decoding stream data back into image Bitmap that android understands
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // You Could provide a more explicit error message for IOException
            getRequest.abort();
            Log.e("ImageDownloader", "Something went wrong while" +
                    " retrieving bitmap from " + url + e.toString());
        }

        return null;
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        int radius = (bitmap.getWidth() > bitmap.getHeight()) ? bitmap.getHeight() / 2 : bitmap.getWidth() / 2;
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        String id_parente;

        @Override
        protected Bitmap doInBackground(String... param) {
            // TODO Auto-generated method stub
            id_parente = param[1];
            return downloadBitmap(param[0]);
        }

        @Override
        protected void onPreExecute() {
            Log.i("Async-Example", "onPreExecute Called");
            //simpleWaitDialog = ProgressDialog.show(Register.this,
            //      "Wait", "Downloading Image");

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Log.i("Async-Example", "onPostExecute Called");
            Bitmap socialphoto = getCroppedBitmap(result);
            //now store the image
            FileOutputStream out = null;
            try {
                File cacheDir = getBaseContext().getCacheDir();
                File dir = new File(cacheDir.getAbsolutePath() + "/MyHealth");
                dir.mkdirs();
                File file = new File(dir, "/SocialPhotoParente_" + id_parente);
                out = new FileOutputStream(file);
                socialphoto.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance


                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

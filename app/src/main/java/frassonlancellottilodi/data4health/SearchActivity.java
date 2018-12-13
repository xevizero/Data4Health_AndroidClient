package frassonlancellottilodi.data4health;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import frassonlancellottilodi.data4health.utils.APIUtils;
import frassonlancellottilodi.data4health.utils.SquareImageView;

import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_IMAGES;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_PROFILE;
import static frassonlancellottilodi.data4health.utils.Endpoints.WEBSERVICE_URL_SEARCH;
import static frassonlancellottilodi.data4health.utils.SessionUtils.checkLogin;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getAuthToken;
import static frassonlancellottilodi.data4health.utils.SessionUtils.getLoggedUserEmail;
import static frassonlancellottilodi.data4health.utils.SessionUtils.revokeAuthToken;
import static frassonlancellottilodi.data4health.utils.UIUtils.displayErrorAlert;
import static frassonlancellottilodi.data4health.utils.UIUtils.getTitleFont;
import static frassonlancellottilodi.data4health.utils.UIUtils.pxFromDp;

public class SearchActivity extends AppCompatActivity {
    private final String TAG = "SearchActivity";

    private Button titleSearch;
    private LinearLayout searchScrollViewContainer;
    private EditText searchField;
    private ImageView searchButton;
    private Boolean drawSeparator = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin(getApplicationContext(), this, false);
        setContentView(R.layout.activity_search);

        initializeUI();
    }

    /**
     * Initialize the UI and listeners
     */
    private void initializeUI(){
        searchScrollViewContainer = findViewById(R.id.searchScrollviewContainerSearch);
        titleSearch = findViewById(R.id.titlesearch);
        searchField = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchFieldButton);

        titleSearch.setTypeface(getTitleFont(this));

        searchField.addTextChangedListener(searchFieldWatcher());
        searchButton.setOnClickListener(v -> {
            if(searchField.getText().length()>0){
                searchScrollViewContainer.removeAllViews();
                downloadSearchResults(String.valueOf(searchField.getText()));
            }else{
                searchScrollViewContainer.removeAllViews();
            }

        });

    }

    private TextWatcher searchFieldWatcher(){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Needs a rework for slow network responses
                // downloadSearchResults(String.valueOf(searchField.getText()));
            }
        };
    }

    /**
     * Generates the search results fetched from the JSON response and displays them in the dedicated view
     * @param dataArray the JSON data from the server
     * @throws JSONException
     */
    private void populateSearchResults(JSONArray dataArray) throws JSONException {
        for(int i = 0; i < dataArray.length(); i++){
            JSONObject userData = dataArray.getJSONObject(i);
            final String name = userData.getString("Name");
            final String surname = userData.getString("Surname");
            final String email = userData.getString("Email");
            searchScrollViewContainer.addView(generateSearchResult(name + " " + surname, email));
            if(drawSeparator)
                searchScrollViewContainer.addView(generateSearchSeparator());
        }
    }

    /**
     * Request to download search results
     * @param query the user search query
     */
    private void downloadSearchResults(String query){

        JSONObject POSTParams = new JSONObject();
        try {
            POSTParams.put("Token", getAuthToken(getApplicationContext()));
            POSTParams.put("Text", query);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (WEBSERVICE_URL_SEARCH, POSTParams,
                        response -> {
                            try {
                                Log.d(TAG, response.toString());
                                if("Success".equals(response.getString("Response"))){
                                    JSONArray responseData = response.getJSONArray("Data");
                                    populateSearchResults(responseData);
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


    /**
     * Generate a view that contains the user name and image
     * @param name
     * @param email
     * @return
     */
    private LinearLayout generateSearchResult(String name, String email){

        LinearLayout horizontalRow = new LinearLayout(this);
        LinearLayout.LayoutParams paramsRow = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, pxFromDp(this, 80));
        paramsRow.setMargins(50,0,50,0);
        horizontalRow.setLayoutParams(paramsRow);
        horizontalRow.setOrientation(LinearLayout.HORIZONTAL);
        horizontalRow.setWeightSum(1);
        horizontalRow.setGravity(Gravity.CENTER);

        RelativeLayout nameContainer = new RelativeLayout(this);
        LinearLayout.LayoutParams paramsNameContainer = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f);
        paramsNameContainer.setMargins(50,0,0,0);
        nameContainer.setLayoutParams(paramsNameContainer);
        nameContainer.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

        TextView nameView = new TextView(this);
        nameView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        nameView.setTextColor(getResources().getColor(R.color.darkGrey));
        nameView.setText(name);
        nameView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        nameView.setGravity(Gravity.CENTER);

        RelativeLayout photoContainer = new RelativeLayout(this);
        photoContainer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.2f));
        photoContainer.setGravity(Gravity.CENTER);

        SquareImageView photoImageView = new SquareImageView(this);
        RelativeLayout.LayoutParams paramsImage = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        paramsImage.setMargins(30,30,30,30);
        photoImageView.setLayoutParams(paramsImage);
        photoImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoImageView.setImageResource(R.drawable.bgspinner);
        photoImageView.setTransitionName("ProfilePictureTransitionHomePage");
        downloadProfilePicture(response -> photoImageView.setImageBitmap(response), email);

        nameContainer.addView(nameView);
        photoContainer.addView(photoImageView);
        horizontalRow.addView(photoContainer);
        horizontalRow.addView(nameContainer);

        horizontalRow.setOnClickListener(v -> {
            Intent i = new Intent(SearchActivity.this, ProfileActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    SearchActivity.this, photoImageView, "ProfilePictureTransitionHomePage");
            i.setAction(email);
            startActivity(i, options.toBundle());

        });

        return horizontalRow;
    }

    private LinearLayout generateSearchSeparator(){
        LinearLayout horizontalSeparator = new LinearLayout(this);
        LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, pxFromDp(this, 1));
        separatorParams.setMargins(0,0,0,0);
        horizontalSeparator.setBackgroundColor(getResources().getColor(R.color.lightGrey));
        horizontalSeparator.setLayoutParams(separatorParams);

        return horizontalSeparator;
    }


    private void downloadProfilePicture(final APIUtils.imageRequestCallback callback, String userEmail){

        String requestURL = WEBSERVICE_URL_IMAGES + "?Token=" + getAuthToken(SearchActivity.this) + "&Filename=" + userEmail + ".png";
        ImageRequest imageRequest = new ImageRequest(requestURL,
                response -> callback.onSuccess(response), 100, 100, ImageView.ScaleType.CENTER_CROP,null,
                error -> imageDownloadErrorHandler());


        Volley.newRequestQueue(this).add(imageRequest);


    }

    private void imageDownloadErrorHandler(){
        //Nothing for now
    }

}

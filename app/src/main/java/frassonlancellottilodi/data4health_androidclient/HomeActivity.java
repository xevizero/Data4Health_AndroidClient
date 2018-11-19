package frassonlancellottilodi.data4health_androidclient;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;

import frassonlancellottilodi.data4health_androidclient.viewModel.homePageVM;

public class HomeActivity extends android.support.v4.app.FragmentActivity {

    ViewModel viewModel;
    Button titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        viewModel = ViewModelProviders.of(this).get(homePageVM.class);
        titleView = findViewById(R.id.titlehome);
        initializeUI();
    }

    private void initializeUI(){
        Typeface fontTitle = Typeface.createFromAsset(getAssets(), "fonts/Montserrat-Medium.ttf");
        titleView.setTypeface(fontTitle);
        titleView.setAllCaps(false);
    }
}

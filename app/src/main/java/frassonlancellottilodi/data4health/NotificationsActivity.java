package frassonlancellottilodi.data4health;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import static frassonlancellottilodi.data4health.utils.SessionUtils.checkLogin;


public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLogin(getApplicationContext(), this, false);
        setContentView(R.layout.activity_notifications);
    }
}

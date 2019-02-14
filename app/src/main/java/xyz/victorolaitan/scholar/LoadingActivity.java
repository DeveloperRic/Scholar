package xyz.victorolaitan.scholar;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.session.Settings;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        connect(this, false);
    }

    static boolean connect(Activity activity, boolean fromMain) {
        if (Session.getSession() == null) {
            boolean signedIn = Session.newSession(activity);
            if (Settings.STUDENT_LOGIN_KEY.get() != null) {
                if (!fromMain) {
                    activity.startActivity(
                            new Intent(activity, MainActivity.class)
                                    .putExtra("loading:connected", signedIn));
                }
            } else {
                activity.startActivity(new Intent(activity, LoginActivity.class));
            }
            return signedIn;
        }
        return true;
    }
}

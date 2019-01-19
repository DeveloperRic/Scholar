package xyz.victorolaitan.scholar.controller;

import android.app.Activity;
import android.widget.TextView;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.session.Session;

public class DrawerCtrl implements ActivityCtrl {
    private Session session;

    private TextView stuName;
    private TextView stuEmail;
    private boolean initialised;

    public DrawerCtrl(Session session) {
        this.session = session;
    }

    public void init(Activity activity) {
        stuName = activity.findViewById(R.id.drawer_txtStuName);
        stuEmail = activity.findViewById(R.id.drawer_txtStuEmail);
        initialised = true;
    }

    @Override
    public void updateInfo() {
        stuName.setText(session.getStudent().getFullName());
        stuEmail.setText(session.getStudent().getEmailAddress());
    }

    public boolean isInitialised() {
        return initialised;
    }
}

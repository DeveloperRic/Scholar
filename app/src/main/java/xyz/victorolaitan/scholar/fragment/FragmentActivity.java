package xyz.victorolaitan.scholar.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Stack;

import xyz.victorolaitan.scholar.ActivityId;
import xyz.victorolaitan.scholar.session.Session;

public abstract class FragmentActivity extends AppCompatActivity {
    private static HashMap<ActivityId, FragmentActivity> savedInstances;

    private static void checkMapNotNull() {
        if (savedInstances == null)
            savedInstances = new HashMap<>();
    }

    protected static void saveInstance(ActivityId activityId, FragmentActivity activity) {
        checkMapNotNull();
        savedInstances.put(activityId, activity);
    }

    static FragmentActivity getSavedInstance(ActivityId activityId) {
        checkMapNotNull();
        return savedInstances.get(activityId);
    }

    protected static Session getSession() {
        return Session.getSession();
    }

    protected final Stack<Fragment> fragmentStack = new Stack<>();

    protected Fragment currentFragment() {
        return fragmentStack.peek();
    }

    protected void pushFragment(FragmentId fragmentId) {
        pushFragment(fragmentId, new Object[0]);
    }

    protected abstract ActivityId getActivityId();

    public abstract void pushFragment(FragmentId fragmentId, Object... args);

    public abstract void popFragment(FragmentId fragmentId);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Session.getSession() == null) {
            if (!Session.newSession(this)) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (savedInstances != null) {
            savedInstances.clear();
            savedInstances = null;
        }
    }
}

package xyz.victorolaitan.scholar;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import xyz.victorolaitan.scholar.controller.CalendarCtrl;
import xyz.victorolaitan.scholar.controller.DrawerCtrl;
import xyz.victorolaitan.scholar.controller.HomeCtrl;
import xyz.victorolaitan.scholar.fragment.CalendarFragment;
import xyz.victorolaitan.scholar.fragment.Fragment;
import xyz.victorolaitan.scholar.fragment.FragmentActivity;
import xyz.victorolaitan.scholar.fragment.FragmentId;
import xyz.victorolaitan.scholar.fragment.HomeFragment;

public class MainActivity extends FragmentActivity {
    private final FragmentManager fragmentManager = getSupportFragmentManager();

    private DrawerCtrl drawerCtrl;
    private DrawerLayout mDrawerLayout;

    public MainActivity() {
        saveInstance(ActivityId.MAIN_ACTIVITY, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        //noinspection ConstantConditions
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        mDrawerLayout = findViewById(R.id.activity_main);
        mDrawerLayout.addDrawerListener(drawerMotionListener);
        ((NavigationView) findViewById(R.id.drawer_navView))
                .setNavigationItemSelectedListener(drawerItemListener);

        BottomNavigationView navigation = findViewById(R.id.main_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        pushFragment(FragmentId.HOME_FRAGMENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawerCtrl == null)
            drawerCtrl = new DrawerCtrl(getSession());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                drawerCtrl.init(this);
                drawerCtrl.updateInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                pushFragment(FragmentId.HOME_FRAGMENT);
                return true;
            case R.id.navigation_calendar:
                pushFragment(FragmentId.CALENDAR_FRAGMENT);
                return true;
            case R.id.navigation_evaluation:
                //mTextMessage.setText(R.string.title_evaluation);
                return true;
        }
        return false;
    };

    private final DrawerLayout.DrawerListener drawerMotionListener
            = new DrawerLayout.DrawerListener() {

        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            // Respond when the drawer's position changes
        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            if (!drawerCtrl.isInitialised()) {
                drawerCtrl.init(MainActivity.this);
            }
            drawerCtrl.updateInfo();
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            // Respond when the drawer is closed
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            // Respond when the drawer motion state changes
        }
    };

    private final NavigationView.OnNavigationItemSelectedListener drawerItemListener
            = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            // set item as selected to persist highlight
            //menuItem.setChecked(true);
            mDrawerLayout.closeDrawers();

            switch (menuItem.getItemId()) {
                case R.id.drawer_action_subjects:
                    startActivity(new Intent(MainActivity.this, SubjectsActivity.class));
                    break;
            }

            return true;
        }
    };

    @Override
    protected ActivityId getActivityId() {
        return ActivityId.MAIN_ACTIVITY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void pushFragment(FragmentId fragmentId, Object... args) {
        Fragment fragment;
        if (fragmentId == FragmentId.HOME_FRAGMENT) {
            fragment = new HomeFragment();
            fragment.setController(new HomeCtrl(getSession()));
        } else if (fragmentId == FragmentId.CALENDAR_FRAGMENT) {
            fragment = new CalendarFragment();
            fragment.setController(new CalendarCtrl(getSession()));
        } else {
            return;
        }
        fragmentStack.push(fragment);
        fragmentManager.beginTransaction()
                .replace(R.id.main_fragment, currentFragment())
                .addToBackStack(String.valueOf(fragmentId))
                .commit();
    }

    @Override
    public void popFragment(FragmentId fragmentId) {
        fragmentManager.popBackStack(
                String.valueOf(fragmentId), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentStack.pop().destroy();
    }
}

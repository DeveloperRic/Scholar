package xyz.victorolaitan.scholar.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import xyz.victorolaitan.scholar.ActivityId;
import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.TestEditCtrl;
import xyz.victorolaitan.scholar.model.subject.Test;

import static xyz.victorolaitan.scholar.fragment.FragmentId.TEST_EDIT_FRAGMENT;

public class TestEditFragment extends Fragment<TestEditCtrl> {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            controller.setTest(UUID.fromString(savedInstanceState.getString("testId")));
        } else if (controller.getTest() == null) {
            controller.setTest((Test) getSavedObject(TEST_EDIT_FRAGMENT, "test"));
        }
        return inflater.inflate(R.layout.fragment_edit_test, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);
        controller.updateInfo();
    }

    @Override
    public FragmentId getFragmentId() {
        return TEST_EDIT_FRAGMENT;
    }

    @Override
    public boolean onHomeUpPressed() {
        return onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY).popFragment(TEST_EDIT_FRAGMENT);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveObject(TEST_EDIT_FRAGMENT, "test", controller.getTest());
        allowDestruction();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("testId", controller.getTest().getTestId().toString());
    }
}

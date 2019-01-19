package xyz.victorolaitan.scholar.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import xyz.victorolaitan.scholar.ActivityId;
import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.ClassEditCtrl;

import static xyz.victorolaitan.scholar.fragment.FragmentId.CLASS_EDIT_FRAGMENT;

public class ClassEditFragment extends Fragment<ClassEditCtrl> {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            controller.setTheClass(UUID.fromString(savedInstanceState.getString("classId")));
        } else if (controller.getTheClass() == null) {
            controller.setTheClass((xyz.victorolaitan.scholar.model.subject.Class)
                    getSavedObject(CLASS_EDIT_FRAGMENT, "class"));
        }
        return inflater.inflate(R.layout.fragment_edit_class, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);
        controller.updateInfo();
    }

    @Override
    public FragmentId getFragmentId() {
        return CLASS_EDIT_FRAGMENT;
    }

    @Override
    public boolean onHomeUpPressed() {
        return onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY).popFragment(CLASS_EDIT_FRAGMENT);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveObject(CLASS_EDIT_FRAGMENT, "class", controller.getTheClass());
        allowDestruction();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("classId", controller.getTheClass().getId().toString());
    }
}

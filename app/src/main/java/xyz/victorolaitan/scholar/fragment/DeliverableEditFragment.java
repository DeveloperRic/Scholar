package xyz.victorolaitan.scholar.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import xyz.victorolaitan.scholar.ActivityId;
import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.DeliverableEditCtrl;
import xyz.victorolaitan.scholar.model.subject.Deliverable;

public class DeliverableEditFragment extends Fragment<DeliverableEditCtrl> {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            controller.setDeliverable(UUID.fromString(savedInstanceState.getString("deliverableId")));
        } else if (controller.getDeliverable() == null) {
            controller.setDeliverable(
                    (Deliverable) getSavedObject(FragmentId.DELIVERABLE_EDIT_FRAGMENT, "deliverable"));
        }
        return inflater.inflate(R.layout.fragment_edit_deliverable, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);
        controller.updateInfo();
    }

    @Override
    public FragmentId getFragmentId() {
        return FragmentId.DELIVERABLE_EDIT_FRAGMENT;
    }

    @Override
    public boolean onHomeUpPressed() {
        return onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY).popFragment(FragmentId.DELIVERABLE_EDIT_FRAGMENT);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveObject(FragmentId.DELIVERABLE_EDIT_FRAGMENT, "deliverable", controller.getDeliverable());
        allowDestruction();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("deliverableId", controller.getDeliverable().getDeliverableId().toString());
    }
}

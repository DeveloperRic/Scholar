package xyz.victorolaitan.scholar.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import xyz.victorolaitan.easyjson.JSONElement;
import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.FragmentCtrl;
import xyz.victorolaitan.scholar.controller.ModelCtrl;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.ScholarModel;

public abstract class Fragment<T extends FragmentCtrl> extends android.support.v4.app.Fragment {
    private static HashMap<String, ScholarModel> savedObjects = new HashMap<>();
    protected T controller;
    private boolean allowDestruction, pendingDestruction;

    protected static void saveObject(FragmentId fragmentId, String key, ScholarModel obj) {
        savedObjects.put(fragmentId + ":" + key, obj);
    }

    protected static void saveDummy(FragmentId fragmentId, String key, String obj) {
        savedObjects.put(fragmentId + ":" + key, new DummyModel(obj));
    }

    protected static ScholarModel getSavedObject(FragmentId fragmentId, String key) {
        return savedObjects.get(fragmentId + ":" + key);
    }

    private static class DummyModel implements ScholarModel {
        private String obj;

        private DummyModel(String obj) {
            this.obj = obj;
        }

        @Override
        public String consoleFormat(String prefix) {
            return obj;
        }

        @Override
        public JSONElement toJSON() {
            return null;
        }

        @Override
        public Object fromJSON(JSONElement json) {
            return null;
        }
    }

    /*protected static void removeSavedObject(FragmentId fragmentId, String key) {
        savedObjects.remove(fragmentId + ":" + key);
    }*/

    public T getController() {
        return controller;
    }

    public void setController(T controller) {
        if (this.controller != null)
            this.controller.onDestroy();
        this.controller = controller;
    }

    private void clearSavedObjects(FragmentId fragmentId) {
        List<String> keys = new ArrayList<>(savedObjects.keySet());
        for (int i = 0; i < savedObjects.size(); i++) {
            if (keys.get(i).startsWith(String.valueOf(fragmentId))) {
                savedObjects.remove(keys.get(i));
                keys.remove(i);
                i--;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (controller == null) {
            if (Session.getSession() == null) {
                if (!Session.newSession(getContext())) {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        clearSavedObjects(getFragmentId());
        disallowDestruction();
    }

    public boolean onOptionsItemSelected(int id) {
        switch (id) {
            case R.id.options_action_duplicate:
                if (controller instanceof ModelCtrl) {
                    ((ModelCtrl) controller).duplicateModel();
                    return true;
                }
            case R.id.options_action_delete:
                if (controller instanceof ModelCtrl) {
                    ((ModelCtrl) controller).deleteModel();
                    onBackPressed();
                    return true;
                }
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveChanges();
    }

    private void saveChanges() {
        if (controller instanceof ModelCtrl) {
            if (!((ModelCtrl) controller).postModel(Session.getSession().getDatabase())) {
                Snackbar.make(
                        Objects.requireNonNull(getActivity()).findViewById(
                                getFragmentId().getDefaultActivityId().getLayoutId()),
                        R.string.snackbar_failedSave,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_retry, v -> saveChanges())
                        .show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy();
    }

    protected void disallowDestruction() {
        allowDestruction = pendingDestruction = false;
    }

    protected void allowDestruction() {
        allowDestruction = true;
        if (pendingDestruction)
            destroy();
    }

    public void destroy() {
        if (!allowDestruction) {
            pendingDestruction = true;
        } else if (controller != null) {
            clearSavedObjects(getFragmentId());
            controller.onDestroy();
            controller = null;
        }
    }

    public abstract FragmentId getFragmentId();

    /**
     * @return true if this fragment overrides default behaviour, false otherwise
     */
    public abstract boolean onHomeUpPressed();

    /**
     * @return true if this fragment overrides default behaviour, false otherwise
     */
    public abstract boolean onBackPressed();
}

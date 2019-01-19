package xyz.victorolaitan.scholar.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import xyz.victorolaitan.scholar.ActivityId;
import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.PersonSelectCtrl;
import xyz.victorolaitan.scholar.controller.RecyclerAdapter;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.Indexable;

import static xyz.victorolaitan.scholar.fragment.FragmentId.PERSON_SELECT_FRAGMENT;

public class PersonSelectFragment extends Fragment<PersonSelectCtrl> {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (savedInstanceState != null) {
            controller.setContext(
                    PersonSelectCtrl.PersonType.valueOf(savedInstanceState.getString("personType")),
                    Session.getSession().getDatabase()
                            .getPerson(UUID.fromString(savedInstanceState.getString("personContext"))));
        } else if (controller.getContext() == null) {
            controller.setContext(
                    PersonSelectCtrl.PersonType.valueOf(
                            getSavedObject(PERSON_SELECT_FRAGMENT, "personType").consoleFormat("")),
                    (Indexable) getSavedObject(PERSON_SELECT_FRAGMENT, "personContext"));
        }
        return inflater.inflate(R.layout.fragment_select_person, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);
        RecyclerView peopleRecycler = view.findViewById(R.id.selectPerson_peopleRecycler);
        peopleRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        peopleRecycler.setItemAnimator(new DefaultItemAnimator());

        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), controller.observablePeople,
                R.layout.content_card_person, R.id.person_cardview, R.anim.trans_fade_in);
        peopleRecycler.setAdapter(adapter);
        controller.setPeopleAdapter(adapter);
        controller.updateInfo();
    }

    @Override
    public FragmentId getFragmentId() {
        return PERSON_SELECT_FRAGMENT;
    }

    @Override
    public boolean onHomeUpPressed() {
        return onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        FragmentActivity.getSavedInstance(ActivityId.SUBJECTS_ACTIVITY).popFragment(PERSON_SELECT_FRAGMENT);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        saveDummy(PERSON_SELECT_FRAGMENT, "personType", controller.getPersonType().toString());
        saveObject(PERSON_SELECT_FRAGMENT, "personContext", controller.getContext());
        allowDestruction();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("personType", controller.getPersonType().toString());
        outState.putString("personContext", controller.getContext().getId().toString());
    }
}

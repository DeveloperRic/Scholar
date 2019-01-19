package xyz.victorolaitan.scholar.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.controller.HomeCtrl;
import xyz.victorolaitan.scholar.controller.RecyclerAdapter;
import xyz.victorolaitan.scholar.session.Session;

public class HomeFragment extends Fragment<HomeCtrl> {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (controller == null) {
            controller = new HomeCtrl(Session.getSession());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        controller.init(view);

        RecyclerView homeRecycler = view.findViewById(R.id.home_recycler);
        homeRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        homeRecycler.setItemAnimator(new DefaultItemAnimator());

        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), controller.observableCards,
                R.layout.content_card_home, R.id.home_cardview, R.anim.trans_fade_in);
        homeRecycler.setAdapter(adapter);
        controller.setCardsAdapter(adapter);

        TextView mTextMessage = view.findViewById(R.id.home_message);
        controller.launch(mTextMessage);
    }

    public void setController(HomeCtrl controller) {
        this.controller = controller;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller = null;
    }

    @Override
    public FragmentId getFragmentId() {
        return FragmentId.HOME_FRAGMENT;
    }

    @Override
    public boolean onHomeUpPressed() {
        return false;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}

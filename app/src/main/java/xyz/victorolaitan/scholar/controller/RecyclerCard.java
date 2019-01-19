package xyz.victorolaitan.scholar.controller;

import android.support.v7.widget.CardView;
import android.view.View;

public interface RecyclerCard {

    void attachLayoutViews(View layout, CardView cv);

    void updateInfo();
}

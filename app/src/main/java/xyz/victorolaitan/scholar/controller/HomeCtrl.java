package xyz.victorolaitan.scholar.controller;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import xyz.victorolaitan.scholar.R;
import xyz.victorolaitan.scholar.session.Session;
import xyz.victorolaitan.scholar.util.Nameable;
import xyz.victorolaitan.scholar.util.Schedule;
import xyz.victorolaitan.scholar.util.ScheduleHolder;
import xyz.victorolaitan.scholar.util.Util;

public class HomeCtrl implements FragmentCtrl {
    private Session parent;
    private Context context;

    public List<RecyclerCard> observableCards;
    private RecyclerAdapter cardsAdapter;

    public HomeCtrl(Session parent, Context context) {
        this.parent = parent;
        this.context = context;
    }

    public void init(View view) {
        observableCards = new ArrayList<>();
    }

    public void launch(TextView txt) {
        txt.setText(parent.getCalendar().toJSON().toString());
        updateInfo();
    }

    @SuppressWarnings("ConstantConditions")
    public void updateInfo() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(new Date());

        List<ScheduleHolder> modelList = new ArrayList<>();
        for (Nameable n : parent.getCalendar().filterRecursively(calendar.getTime())) {
            if (!(n instanceof ScheduleHolder))
                continue;

            modelList.add((ScheduleHolder) n);
        }
        for (int i = 1; i <= 2; i++) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            //noinspection unchecked
            for (Nameable n : parent.getCalendar().filterRecursively(calendar.getTime())) {
                if (n instanceof ScheduleHolder) {
                    modelList.add((ScheduleHolder) n);
                }
            }
        }

        List<HomeCard> homeCardList = new ArrayList<>();
        Util.initHomeCardList(homeCardList, modelList);
        Util.sortList(homeCardList);
        observableCards.addAll(homeCardList);

        Date now = new Date();
        boolean doneToday = false, doneTomorrow = false;
        for (int i = 0; i < observableCards.size(); i++) {
            Schedule date = ((HomeCard) observableCards.get(i)).getObject();
            if (date.occursOn(now)) {
                if (!doneToday) {
                    observableCards.add(i, new HomeGroupTitleCard(
                            context.getResources().getString(R.string.home_occursToday)));
                    doneToday = true;
                }
            } else if (date.occursInXDays(1)) {
                if (!doneTomorrow) {
                    observableCards.add(i, new HomeGroupTitleCard(
                            context.getResources().getString(R.string.home_occursTomorrow)));
                    doneTomorrow = true;
                }
            } else if (date.occursInXDays(2)) {
                observableCards.add(i, new HomeGroupTitleCard(
                        context.getResources().getString(R.string.home_occursInThreeDays)));
                break;
            }
        }

        observableCards.add(0, new HomeHeaderCard());
        cardsAdapter.notifyDataSetChanged();
    }

    public void setCardsAdapter(RecyclerAdapter cardsAdapter) {
        cardsAdapter.setCardViewSelector(new RecyclerAdapter.CardViewSelector() {
            @Override
            public int getItemViewType(RecyclerCard card) {
                if (card instanceof HomeHeaderCard) return 0;
                else if (card instanceof HomeCard) return 1;
                else return 2;
            }

            @Override
            public int getViewLayoutId(int itemViewType) {
                switch (itemViewType) {
                    case 0:
                        return R.layout.content_card_home_header;
                    case 1:
                        return R.layout.content_card_home_event;
                    case 2:
                        return R.layout.content_card_home_title;
                    default:
                        return -1;
                }
            }

            @Override
            public int getCardViewId(int itemViewType) {
                return R.id.home_cardview;
            }
        });
        this.cardsAdapter = cardsAdapter;
    }

    private class HomeHeaderCard implements RecyclerCard {
        private TextView txtGreeting;

        @Override
        public void attachLayoutViews(View layout, CardView cv) {
            txtGreeting = layout.findViewById(R.id.home_txtGreeting);
        }

        @Override
        public void updateInfo() {
            txtGreeting.setText(txtGreeting.getResources()
                    .getString(R.string.home_greeting, parent.getStudent().getShortName()));
        }
    }

    private class HomeGroupTitleCard implements RecyclerCard {
        private String title;
        private TextView txtTitle;

        private HomeGroupTitleCard(String title) {
            this.title = title;
        }

        @Override
        public void attachLayoutViews(View layout, CardView cv) {
            txtTitle = layout.findViewById(R.id.home_card_txtGroup);
        }

        @Override
        public void updateInfo() {
            txtTitle.setText(title);
        }
    }

}

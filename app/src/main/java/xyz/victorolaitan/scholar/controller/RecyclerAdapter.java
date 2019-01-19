package xyz.victorolaitan.scholar.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.List;

public final class RecyclerAdapter<T extends RecyclerCard> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int layoutId;
    private int cardViewId;
    private int animationId;
    private Context context;
    private List<T> cards;
    private int lastPosition = -1;

    public RecyclerAdapter(Context context, List<T> cards, int layoutId, int cardViewId, int animationId) {
        this.context = context;
        this.cards = cards;
        this.layoutId = layoutId;
        this.cardViewId = cardViewId;
        this.animationId = animationId;
    }


    private class CardViewHolder extends RecyclerView.ViewHolder {
        RecyclerCard card;
        View itemView;
        CardView cv;

        CardViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            cv = itemView.findViewById(cardViewId);
        }

        void setCard(RecyclerCard card) {
            this.card = card;
            this.card.attachLayoutViews(itemView, cv);
        }

        void updateInfo() {
            card.updateInfo();
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new CardViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(layoutId, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        cardViewHolder.setCard(cards.get(position));
        cardViewHolder.updateInfo();
        setAnimation(viewHolder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(
                    context, animationId);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}

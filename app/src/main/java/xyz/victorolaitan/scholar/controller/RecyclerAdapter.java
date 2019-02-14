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
    private CardViewSelector cardViewSelector;
    private int animationId;
    private Context context;
    private List<T> cards;
    private int lastPosition = -1;

    public RecyclerAdapter(Context context, List<T> cards, int layoutId, int cardViewId, int animationId) {
        this.context = context;
        this.cards = cards;
        this.cardViewSelector = new CardViewSelector() {
            @Override
            public int getItemViewType(RecyclerCard card) {
                return 0;
            }

            @Override
            public int getViewLayoutId(int itemViewType) {
                return layoutId;
            }

            @Override
            public int getCardViewId(int itemViewType) {
                return cardViewId;
            }
        };
        this.animationId = animationId;
    }

    public interface CardViewSelector {
        int getItemViewType(RecyclerCard card);

        int getViewLayoutId(int itemViewType);

        int getCardViewId(int itemViewType);
    }

    private class CardViewHolder extends RecyclerView.ViewHolder {
        RecyclerCard card;
        View itemView;

        CardViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        void setCard(RecyclerCard card) {
            this.card = card;
            View cv = itemView.findViewById(
                    cardViewSelector.getCardViewId(cardViewSelector.getItemViewType(card)));
            this.card.attachLayoutViews(itemView, cv instanceof CardView ? (CardView) cv : null);
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
        return cardViewSelector.getItemViewType(cards.get(position));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new CardViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(cardViewSelector.getViewLayoutId(viewType), viewGroup, false));
    }

    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        CardViewHolder cardViewHolder = (CardViewHolder) viewHolder;
        cardViewHolder.setCard(cards.get(position));
        cardViewHolder.updateInfo();
        setAnimation(viewHolder.itemView, position);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
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

    public void setCardViewSelector(CardViewSelector cardViewSelector) {
        this.cardViewSelector = cardViewSelector;
    }
}

package com.dipitize.app.dipitize.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.model.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Made by acefalobi on 3/16/2017.
 */

public class NotificationsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Notification> notifications = new ArrayList<>();

    public NotificationsRecyclerAdapter( List<Notification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationsRecyclerAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        Notification notification = notifications.get(position);
        itemHolder.textMessage.setText(notification.message);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        TextView textMessage;

        ItemHolder(View view) {
            super(view);

            textMessage = (TextView) view.findViewById(R.id.text_notification_message);
        }
    }
}

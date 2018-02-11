package com.dipitize.app.dipitize.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.model.Message;

import java.util.Calendar;
import java.util.List;

/**
 * Made by acefalobi on 3/31/2017.
 */

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private String userId;

    public ChatRecyclerAdapter(String userId, List<Message> messages) {
        this.messages = messages;
        this.userId = userId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatRecyclerAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        Message message = messages.get(position);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(message.timeSent);
        String timeSent = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))
                + "/" + String.valueOf(calendar.get(Calendar.MONTH))
                + "/" + String.valueOf(calendar.get(Calendar.YEAR))
                + " " + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY))
                + ":" + String.valueOf(calendar.get(Calendar.MINUTE));

        if (message.userId.equals(userId)) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            params.gravity = Gravity.END;
            itemHolder.textInfo.setLayoutParams(params);
            itemHolder.textMessage.setLayoutParams(params);
            itemHolder.textInfo.setText("You • " + timeSent);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.weight = 1.0f;
            params.gravity = Gravity.START;
            itemHolder.textInfo.setLayoutParams(params);
            itemHolder.textMessage.setLayoutParams(params);
            itemHolder.textInfo.setText(message.user.username + " • " + timeSent);
        }
        itemHolder.textMessage.setText(message.message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {

        TextView textInfo, textMessage;
        CardView cardView;

        public ItemHolder(View view) {
            super(view);

            cardView = (CardView) view.findViewById(R.id.message_card_view);

            textInfo = (TextView) view.findViewById(R.id.text_message_info);
            textMessage = (TextView) view.findViewById(R.id.text_message_message);
        }
    }
}

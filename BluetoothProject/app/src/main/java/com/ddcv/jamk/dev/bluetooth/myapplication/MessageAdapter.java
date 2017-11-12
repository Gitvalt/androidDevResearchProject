package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.style.TextAppearanceSpan;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.awt.font.TextAttribute;
import java.lang.reflect.Array;
import java.time.Instant;
import java.util.Date;

/**
 * Created by Valtteri on 11.11.2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>
{

    public ArrayMap<Date, MessageItem> messageArray;

    public MessageAdapter()
    {
        messageArray = new ArrayMap<>();
    }

    @Override
    public int getItemCount()
    {
        if(messageArray == null)
        {
            return 0;
        }
        else
        {
            return messageArray.size();
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        MessageItem item = messageArray.valueAt(position);
        String message = item.getContent();
        String sender = item.getSender();

        holder.message.setText(message);

        String senderName = "";

        if(sender == null)
        {
            senderName = "Undefined name";
        }
        else
        {
            senderName = sender;
        }

        holder.sender.setText(senderName);

        if(sender == "this")
        {
            holder.message.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        else if(sender == "SYSTEM")
        {
            holder.message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        else
        {
            holder.message.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView message;
        public TextView sender;

        public ViewHolder(View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.messageBox);
            sender = (TextView) itemView.findViewById(R.id.SenderBox);
        }
    }

}

package com.ddcv.jamk.dev.bluetooth.myapplication;

import java.util.Date;

/**
 * Created by Valtteri on 11.11.2017.
 * Class to contain bluetooth chat message components
 */

public class MessageItem
{
    private final String Message_content;
    private final java.util.Date Date;
    private final String MAC;

    public MessageItem(String sender, String message, java.util.Date time)
    {
        Message_content = message;
        MAC = sender;
        Date = time;
    }

    public String getSender()
    {
        return MAC;
    }

    public String getContent(){
        return Message_content;
    }

    public Date getDate(){
        return Date;
    }
}

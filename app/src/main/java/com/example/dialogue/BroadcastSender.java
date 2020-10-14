package com.example.dialogue;

import android.content.Intent;
import static com.example.dialogue.MainActivity.BROADCAST_ACTION;

/**
 * Created by looper on 2020/9/14.
 */
public class BroadcastSender {
    Intent intent = new Intent();
    public void send(String name ,String value)
    {
        intent.setAction(BROADCAST_ACTION);
        intent.putExtra(name, value);
        MyApplication.getmContext().sendBroadcast(intent);
    }
}

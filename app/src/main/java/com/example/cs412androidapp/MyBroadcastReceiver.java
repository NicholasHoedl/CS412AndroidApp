package com.example.cs412androidapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_MY_BROADCAST = "com.example.cs412androidapp.MY_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Lifecycle", "MyBroadcastReceiver onReceive: " + intent.getAction());
        Toast.makeText(context, "Broadcast received!", Toast.LENGTH_SHORT).show();
    }
}
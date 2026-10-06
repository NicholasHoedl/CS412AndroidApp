package com.example.cs412androidapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.widget.Button;
import android.content.ActivityNotFoundException;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.TextView;
import android.content.IntentFilter;

public class MainActivity extends LifecycleLoggingActivity  {

    private static final String ACTION_SHOW_SECOND = "com.example.cs412androidapp.ACTION_SHOW_SECOND";
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notifications are off, service notification won't be shown", Toast.LENGTH_LONG).show();
                }
                startMyService();
            });

    private MyService myService;
    private boolean isBound = false;
    private final MyBroadcastReceiver myBroadcastReceiver = new MyBroadcastReceiver();

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            TextView textGrade = findViewById(R.id.textGrade);
            textGrade.setText(getString(R.string.grade_format, myService.getMyGrade()));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button buttonExplicit = findViewById(R.id.buttonExplicit);
        buttonExplicit.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            startActivity(intent);
        });

        Button buttonImplicit = findViewById(R.id.buttonImplicit);
        buttonImplicit.setOnClickListener(v -> {
            Intent intent = new Intent(ACTION_SHOW_SECOND);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(MainActivity.this, "No activity found for " + ACTION_SHOW_SECOND, Toast.LENGTH_LONG).show();
            }
        });

        Button buttonStartService = findViewById(R.id.buttonStartService);
        buttonStartService.setOnClickListener(v -> onStartServiceClicked());
        Button buttonBindService = findViewById(R.id.buttonBindService);
        buttonBindService.setOnClickListener(v -> {
            if (!isBound) {
                Intent intent = new Intent(MainActivity.this, MyService.class);
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        });
        Button buttonSendBroadcast = findViewById(R.id.buttonSendBroadcast);
        buttonSendBroadcast.setOnClickListener(v -> {
            Intent broadcastIntent = new Intent(MyBroadcastReceiver.ACTION_MY_BROADCAST);
            broadcastIntent.setPackage(getPackageName());
            sendBroadcast(broadcastIntent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(MyBroadcastReceiver.ACTION_MY_BROADCAST);
        ContextCompat.registerReceiver(this, myBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(myBroadcastReceiver);
    }
    private void startMyService() {
        Intent serviceIntent = new Intent(this, MyService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void onStartServiceClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            startMyService();
        }
    }


}
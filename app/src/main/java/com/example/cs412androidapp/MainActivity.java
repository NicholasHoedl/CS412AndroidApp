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

public class MainActivity extends LifecycleLoggingActivity  {

    private static final String ACTION_SHOW_SECOND = "com.example.cs412androidapp.ACTION_SHOW_SECOND";

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
    }
}
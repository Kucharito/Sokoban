package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity {
    private static final String PREFS_NAME = "SokoPrefs";
    private static final String KEY_USE_NEW_IMAGES = "use_new_images";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button standardButton = findViewById(R.id.button_use_standard);
        Button newButton = findViewById(R.id.button_use_new);

        standardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageChoice(false);
                startMainActivity();
            }
        });

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageChoice(true);
                startMainActivity();
            }
        });
    }

    private void saveImageChoice(boolean useNewImages) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_USE_NEW_IMAGES, useNewImages);
        editor.apply();
    }

    private void startMainActivity() {
        Intent intent = new Intent(MainActivity2.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
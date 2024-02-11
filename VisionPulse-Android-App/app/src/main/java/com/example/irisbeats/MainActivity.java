package com.example.irisbeats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import android.os.Bundle;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {
private ImageButton heartdiagnosismainscreenbutton;
private ImageButton cataractdiagnosismainscreenbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.main_screen_color));
        }
        heartdiagnosismainscreenbutton=findViewById(R.id.buttonHeart);
        cataractdiagnosismainscreenbutton=findViewById(R.id.buttonCataract);

        heartdiagnosismainscreenbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This method is called when the button is clicked
                Intent intent = new Intent(MainActivity.this, HeartScreen.class);
                startActivity(intent); // Show a dialog for image source selection
            }
        });

        cataractdiagnosismainscreenbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This method is called when the button is clicked
                Intent intent = new Intent(MainActivity.this, CataractScreen.class);
                startActivity(intent); // Show a dialog for image source selection
            }
        });

    }
}


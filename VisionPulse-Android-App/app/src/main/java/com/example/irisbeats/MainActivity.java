package com.example.irisbeats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
private Button heartdiagnosismainscreenbutton;
private Button cataractdiagnosismainscreenbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


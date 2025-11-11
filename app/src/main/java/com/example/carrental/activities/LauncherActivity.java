package com.example.carrental.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.carrental.network.TokenManager;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TokenManager tokenManager = new TokenManager(this);

        if (tokenManager.getToken() != null) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        finish();
    }
}
package com.example.carrental.activities; // Hoặc package chứa MainActivity

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.carrental.R;
import com.example.carrental.fragments.AccountFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    final Fragment accountFragment = new AccountFragment();
    final FragmentManager fm = getSupportFragmentManager();

    private Fragment active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.carrental.R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigationView);


        fm.beginTransaction().add(R.id.fragment_container, accountFragment, "1").commit();
        active = accountFragment;


        bottomNav.setSelectedItemId(R.id.nav_profile);


        bottomNav.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();


            if (itemId == R.id.nav_profile) {
                fm.beginTransaction().hide(active).show(accountFragment).commit();
                active = accountFragment;
                return true;
            }


            else if (itemId == R.id.nav_discover) {
                // Tạm thời chưa làm gì
                return true;
            } else if (itemId == R.id.nav_messages) {
                // Tạm thời chưa làm gì
                return true;
            } else if (itemId == R.id.nav_transfer) {
                // Tạm thời chưa làm gì
                return true;
            } else if (itemId == R.id.nav_help) {
                // Tạm thời chưa làm gì
                return true;
            }

            return false;
        });


    }
}
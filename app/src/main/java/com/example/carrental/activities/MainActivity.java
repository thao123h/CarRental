package com.example.carrental.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.carrental.R;
import com.example.carrental.fragments.HomeFragment;
import com.example.carrental.fragments.AccountFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        addFragment(new HomeFragment(), "HOME", true);
        addFragment(new AccountFragment(), "ACCOUNT", false);

        bottomNavigation.setOnItemSelectedListener(item -> {
            String tagToShow = null;
            if (item.getItemId() == R.id.nav_home) tagToShow = "HOME";
            else if (item.getItemId() == R.id.nav_login) tagToShow = "ACCOUNT";

            if (tagToShow != null) showFragment(tagToShow);

            return true;
        });
    }

    private void addFragment(Fragment fragment, String tag, boolean show) {
        Fragment existing = getSupportFragmentManager().findFragmentByTag(tag);
        if (existing == null) {
            var transaction = getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, tag);
            if (!show) transaction.hide(fragment);
            transaction.commit();
        }
    }

    private void showFragment(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment target = fm.findFragmentByTag(tag);
        if (target == null) return;

        var transaction = fm.beginTransaction();
        for (Fragment frag : fm.getFragments()) {
            transaction.hide(frag);
        }
        transaction.show(target);
        transaction.commit();
    }
}

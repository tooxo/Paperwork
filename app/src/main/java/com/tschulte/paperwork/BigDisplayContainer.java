package com.tschulte.paperwork;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BigDisplayContainer extends FragmentActivity {
    int selected = 0;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.big_display_container);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        final BigDisplayImageFragment fragment = new BigDisplayImageFragment();
        final BigDisplayTextFragment fragment1 = new BigDisplayTextFragment();

        fragmentTransaction.add(R.id.container, fragment);
        fragmentTransaction.commit();

        BottomNavigationView.OnNavigationItemSelectedListener listener = new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (selected == 0) {
                    selected = item.getItemId();
                } else {
                    if (item.getItemId() == selected) {
                        return false;
                    }
                }
                selected = item.getItemId();

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                switch (item.getItemId()) {
                    case R.id.navigation_item_a:
                        fragmentTransaction.hide(fragment1);
                        fragmentTransaction.show(fragment);
                        fragmentTransaction.commit();
                        break;
                    case R.id.navigation_item_b:
                        fragmentTransaction.hide(fragment);
                        if (fragmentManager.findFragmentById(fragment1.getId()) == null) {
                            fragmentTransaction.add(R.id.container, fragment1);
                        }
                        fragmentTransaction.show(fragment1);
                        fragmentTransaction.commit();
                        break;
                    default:
                        ;
                }
                return true;
            }
        };

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(listener);


        bottomNavigationView.post(new Runnable() {
            @Override
            public void run() {
                FrameLayout frame = findViewById(R.id.container);
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp.setMargins(0,0,0, bottomNavigationView.getHeight());
                frame.setLayoutParams(lp);
            }
        });


    }
}

package com.tschulte.paperwork;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    static String STORAGE_DIRECTORY = "STORAGE_DIRECTORY";
    FragmentManager fm = getSupportFragmentManager();

    MainActivityFolderFragment mainActivityFolderFragment = new MainActivityFolderFragment();
    MainActivitySetupFragment mainActivitySetupFragment = new MainActivitySetupFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_container);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!preferences.getString(STORAGE_DIRECTORY, "").equals("") &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            fm.beginTransaction().add(R.id.frameLayout, mainActivityFolderFragment).commit();
        } else {
            fm.beginTransaction().add(R.id.frameLayout, mainActivitySetupFragment).commit();
        }

    }

    void folderSelected() {
        fm.beginTransaction().remove(mainActivitySetupFragment).add(R.id.frameLayout, mainActivityFolderFragment).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivityForResult(intent, 777);
            return true;
        } else if (id == R.id.refresh) {
            fm.beginTransaction().detach(mainActivityFolderFragment).attach(mainActivityFolderFragment).commit();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 777 && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra("RELOAD", false)) {
                fm.beginTransaction().detach(mainActivityFolderFragment).attach(mainActivityFolderFragment).commit();
            }
        }

    }
}

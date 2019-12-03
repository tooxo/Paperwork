package com.tschulte.paperwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

import static com.tschulte.paperwork.FileUtils.getPath;
import static com.tschulte.paperwork.MainActivity.STORAGE_DIRECTORY;

public class Settings extends AppCompatActivity {

    static String TESSDATA_LANGUAGE = "TESS_DATA_LANG_UAGE";

    SharedPreferences sharedPreferences;
    boolean englishChecked;
    boolean germanChecked;

    MaterialButton changeDir;
    AppCompatEditText editText;

    boolean reload = false;

    Switch english;
    Switch german;

    private void updatePreferencesOcrLangugage() {
        String prefs;
        if (english.isChecked() && german.isChecked()) {
            prefs = "eng+deu";
        } else if (english.isChecked() && !german.isChecked()) {
            prefs = "eng";
        } else if (!english.isChecked() && german.isChecked()) {
            prefs = "deu";
        } else return;
        sharedPreferences.edit().putString(TESSDATA_LANGUAGE, prefs).apply();
    }

    private void setChecks() {
        String ch = sharedPreferences.getString(TESSDATA_LANGUAGE, "eng+deu");
        if (ch.contains("eng")) {
            english.setChecked(true);
        } else {
            english.setChecked(false);
        }
        if (ch.contains("deu")) {
            german.setChecked(true);
        } else {
            german.setChecked(false);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.settings);

        Toolbar toolbar = findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        editText = findViewById(R.id.directory_path);
        changeDir = findViewById(R.id.change_directory);
        english = findViewById(R.id.switchEnglish);
        german = findViewById(R.id.switchGerman);

        setChecks();

        CompoundButton.OnCheckedChangeListener l = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updatePreferencesOcrLangugage();
            }
        };
        english.setOnCheckedChangeListener(l);
        german.setOnCheckedChangeListener(l);

        editText.setText(sharedPreferences.getString(MainActivity.STORAGE_DIRECTORY, ""));
        changeDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(Intent.createChooser(intent, "Select New Directory"), 567);
                reload = true;
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 567 && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData();
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(selectedFile,
                    DocumentsContract.getTreeDocumentId(selectedFile));
            String path = getPath(this, docUri);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(STORAGE_DIRECTORY, path).apply();
            editText.setText(path);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("RELOAD", reload);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();

        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("RELOAD", reload);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();

    }
}

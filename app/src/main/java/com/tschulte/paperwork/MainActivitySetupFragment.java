package com.tschulte.paperwork;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import static android.app.Activity.RESULT_OK;
import static com.tschulte.paperwork.FileUtils.getPath;
import static com.tschulte.paperwork.MainActivity.STORAGE_DIRECTORY;

public class MainActivitySetupFragment extends Fragment {
    View view;
    MaterialButton permissionsButton;
    MaterialButton directoryButton;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedFile = data.getData();
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(selectedFile,
                    DocumentsContract.getTreeDocumentId(selectedFile));
            String path = getPath(getActivity(), docUri);
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString(STORAGE_DIRECTORY, path).apply();
            ((MainActivity) getActivity()).folderSelected();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 69) {
            refreshButtons();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_setup, container, false);
        return view;
    }

    private void refreshButtons() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

            permissionsButton.setEnabled(false);
            permissionsButton.setIcon(getActivity().getDrawable(R.drawable.ic_lock_open_black_24dp));

            directoryButton.setEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        permissionsButton = view.findViewById(R.id.permissions_button);
        directoryButton = view.findViewById(R.id.directory_button);

        refreshButtons();
        permissionsButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            String[] array = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(array, 69);
                        }
                    }
                }
        );
        directoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(Intent.createChooser(intent, "Select Directory"), 123);
            }
        });
    }
}

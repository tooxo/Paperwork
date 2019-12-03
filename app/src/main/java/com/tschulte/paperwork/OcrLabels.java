package com.tschulte.paperwork;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import top.defaults.colorpicker.ColorPickerPopup;

public class OcrLabels extends Fragment {

    private TagContainerLayout tagContainerLayout;
    private List<String> tags = new ArrayList<>();
    private List<int[]> colors = new ArrayList<int[]>();
    private List<Label> labels = new ArrayList<>();
    public static String LAST_COLOR;
    private SharedPreferences preferences;
    private int selectedColor;
    private MaterialButton colorSelect;
    private MaterialButton confirm;
    private MaterialButton masterConfirm;
    private EditText editText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ocr_labels, container, false);
        tagContainerLayout = view.findViewById(R.id.tags);
        colorSelect = view.findViewById(R.id.colorSelect);
        confirm = view.findViewById(R.id.confirm);
        masterConfirm = view.findViewById(R.id.masterConfirm);
        editText = view.findViewById(R.id.editText);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        selectedColor = preferences.getInt(LAST_COLOR, Color.RED);
        colorSelect.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
        colorSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ColorPickerPopup.Builder(getActivity())
                        .initialColor(selectedColor)
                        .enableBrightness(false)
                        .enableAlpha(false)
                        .showIndicator(true)
                        .showValue(false)
                        .cancelTitle("")
                        .build()
                        .show(new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                selectedColor = color;
                                colorSelect.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                            }
                        });
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText.getText().toString().equals("")) {
                    if (!Label.labelListToStringList(labels).contains(editText.getText().toString())) {

                        Label label = new Label(editText.getText().toString(), selectedColor);
                        labels.add(label);
                        editText.setText("");
                        recreate();
                    } else {
                        editText.setError("Tag duplicate!");
                    }
                } else {
                    editText.setError("Tag name cannot be empty");
                }
            }
        });

        tagContainerLayout.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {

            }

            @Override
            public void onTagLongClick(int position, String text) {

            }

            @Override
            public void onSelectedTagDrag(int position, String text) {
                // NOT WORKING
            }

            @Override
            public void onTagCrossClick(int position) {
                tagContainerLayout.removeTag(position);
                labels.remove(position);
            }
        });

        masterConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder labelFile = new StringBuilder();
                for (Label l : labels) {
                    labelFile.append(l.toLabelFile());
                }

                File newFolder = ((OcrContainer) getActivity()).folder;
                File labelsFile = new File(newFolder + "/labels");


                try {
                    if (labelsFile.createNewFile()) {
                        BufferedWriter w = new BufferedWriter(new FileWriter(labelsFile));
                        w.write(labelFile.toString());
                        w.flush();
                        w.close();
                    }
                } catch (IOException io) {
                    io.printStackTrace();
                }
                ((OcrContainer) getActivity()).stepFour();
            }
        });

        return view;
    }

    private void recreate() {
        tagContainerLayout.removeAllTags();
        tagContainerLayout.setTags(Label.labelListToStringList(labels), Label.labelListToColorList(labels));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

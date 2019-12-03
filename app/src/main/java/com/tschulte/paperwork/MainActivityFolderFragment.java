package com.tschulte.paperwork;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.defaults.drawabletoolbox.DrawableBuilder;

import static com.tschulte.paperwork.MainActivity.STORAGE_DIRECTORY;

public class MainActivityFolderFragment extends Fragment {
    private View view;
    private SharedPreferences preferences;
    private List<FolderObject> folders = new ArrayList<>();

    private LinearLayout createTile(Context context, final FolderObject folder) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        final ImageView im = new ImageView(context);
        final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
        final double heightd = width * 1.4;
        final int height = (int) heightd;
        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                width,
                height);
        im.setLayoutParams(params);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bitmap = folder.getThumbnail();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            im.setImageBitmap(bitmap);
                        }
                    });
                } catch (FileNotFoundException fnf) {
                    fnf.printStackTrace();
                }
            }
        }).start();

        linearLayout.addView(im);
        FontFitTextView textView = new FontFitTextView(context);

        textView.setText(folder.fileName);
        try {

            Date date = new SimpleDateFormat("yyyyMMdd_HHmm_ss", Locale.getDefault()).parse(folder.fileName);
            textView.setText(date.toLocaleString());
        } catch (
                ParseException ignored) {

        }
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(20, 0, 20, 0);

        FlexboxLayout labelLayout = new FlexboxLayout(context);
        for (
                String[] label : folder.labels) {
            TextView l = new TextView(context);
            String labelText = label[0];
            l.setText(labelText);

            Pattern regex = Pattern.compile("rgb\\((\\d{1,3}),(\\d{1,3}),(\\d{1,3})\\)");
            Matcher m = regex.matcher(label[1]);

            try {
                if (m.find()) {
                    int r = Integer.valueOf(m.group(1));
                    int g = Integer.valueOf(m.group(2));
                    int b = Integer.valueOf(m.group(3));

                    Drawable drawable = new DrawableBuilder()
                            .rectangle()
                            .rounded()
                            .solidColor(Color.rgb(r, g, b))
                            .strokeColor(Color.BLACK)
                            .build();

                    l.setBackground(drawable);
                    l.setTextColor(FolderObject.determineTextColor(r, g, b));
                }
            } catch (Exception ignored) {
            }

            l.setPadding(35, 0, 35, 0);
            labelLayout.addView(l);

        }
        labelLayout.setJustifyContent(JustifyContent.CENTER);
        labelLayout.setAlignItems(AlignItems.CENTER);
        labelLayout.setFlexWrap(FlexWrap.WRAP);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        textLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        textLayout.addView(textView);

        textLayout.addView(labelLayout);
        linearLayout.addView(textLayout);

        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Details.class);
                Bundle b = new Bundle();
                b.putSerializable(BigDisplayImageFragment.FOLDER_PARCEL, folder);

                EditText ed = view.findViewById(R.id.search_bar);
                String s = ed.getText().toString();

                if (s.length() > 0) {
                    List<Integer> ints = folder.search(s);
                    StringBuilder sb = new StringBuilder();
                    for (Integer i : ints) {
                        sb.append(i.toString()).append(",");
                    }
                    b.putString(Details.TO_HIGHLIGHT, sb.toString());
                }

                intent.putExtras(b);
                startActivity(intent);
            }
        });

        return linearLayout;
    }

    private void createLayout(String toLeaveOut) {
        LinearLayout layout = view.findViewById(R.id.linear_stub);
        layout.removeAllViews();
        int i = 0;
        List<String> leaveOut = Arrays.asList(toLeaveOut.split(","));
        for (FolderObject o : folders) {
            if (leaveOut.contains(String.valueOf(i))) {
                i++;
                continue;
            }
            LinearLayout l = createTile(getActivity(), o);
            layout.addView(l);
            i++;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_folder, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        LinearLayout layout = view.findViewById(R.id.linear_stub);

        if (layout.getChildCount() > 0) {
            layout.removeAllViews();
        }

        String path = preferences.getString(STORAGE_DIRECTORY, "");
        final File f = new File(path);
        File[] files = f.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    FolderObject folder = new FolderObject(file);
                    if (folder.valid) {
                        LinearLayout l = createTile(getActivity(), folder);
                        layout.addView(l);
                        folders.add(folder);
                    }
                }
            }
        }
        final EditText search = view.findViewById(R.id.search_bar);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                StringBuilder leaveOut = new StringBuilder();
                int i = 0;
                for (FolderObject o : folders) {
                    if (!o.getText().contains(text.toString().toLowerCase())) {
                        leaveOut.append(i).append(",");
                    }
                    i++;
                }
                createLayout(leaveOut.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Button advancedSearch = view.findViewById(R.id.help_button);
        advancedSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvancedSearch asearch = new AdvancedSearch();
                asearch.showDialog(getActivity(), search.getText().toString());
            }
        });

        MaterialButton con = view.findViewById(R.id.speed_dial_view_container);
        con.setClickable(true);
        con.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OcrContainer.class);
                startActivity(intent);
            }
        });

        ProgressBar pb = getActivity().findViewById(R.id.main_progress_bar);
        pb.setVisibility(View.GONE);


        return view;
    }
}

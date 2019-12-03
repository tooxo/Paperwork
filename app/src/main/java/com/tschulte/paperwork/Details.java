package com.tschulte.paperwork;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.tschulte.paperwork.BigDisplayImageFragment.FOLDER_PARCEL;

public class Details extends AppCompatActivity {

    static String TO_HIGHLIGHT = "TO_HIGHLIGHT";
    static String DIRECTORY_BUNDLE = "DIRECTORY_BUNDLE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.side_selection);

        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final FolderObject folder = (FolderObject) getIntent().getExtras().getSerializable(FOLDER_PARCEL);

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Bitmap> thumbnails = new ArrayList<>();
                if (folder.type.equals(folder.TYPE_IMAGE)) {
                    thumbnails = folder.getImageThumbnails();
                } else if (folder.type.equals(folder.TYPE_PDF)) {
                    thumbnails = folder.getPDFThumbnails(Details.this);
                    // TODO Instantly go to pdf viewer
                }

                final List<Bitmap> thumbnails_ = thumbnails;

                final FlexboxLayout flexboxLayout = findViewById(R.id.thumbnail_flex);

                int width = getResources().getDisplayMetrics().widthPixels;
                final int fourty = (int) (width * 0.4);

                int i = 1;
                for (Bitmap bm : thumbnails_) {
                    final ImageView imageView = new ImageView(Details.this);
                    imageView.setImageBitmap(bm);
                    imageView.setMinimumWidth(fourty);

                    float multiplier = Float.valueOf(fourty) / bm.getWidth();
                    int height = (int) (bm.getHeight() * multiplier);
                    imageView.setMinimumHeight(height);

                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    final int i1 = i;
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Details.this, BigDisplayContainer.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(FOLDER_PARCEL, folder);
                            intent.putExtras(bundle);
                            Log.v(folder.fileName, String.valueOf(i1));
                            intent.putExtra(BigDisplayImageFragment.PAGE_INTENT, i1);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            flexboxLayout.addView(imageView);
                        }
                    });
                    i++;
                }

                String toHighlight = getIntent().getExtras().getString(TO_HIGHLIGHT, "");
                for (String highlight : toHighlight.split(",")) {
                    if (!highlight.equals("")) {
                        try {
                            ImageView im = (ImageView) flexboxLayout.getChildAt(Integer.valueOf(highlight));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                im.setForeground(getDrawable(R.drawable.selected_border));
                            } else {
                                im.setColorFilter(Color.argb(125, 0, 255, 0));
                            }
                        } catch (NullPointerException ne) {
                            ne.printStackTrace();
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressBar progressBar = findViewById(R.id.progress_circular);
                        ScrollView linearLayout = findViewById(R.id.scrollable);
                        progressBar.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}



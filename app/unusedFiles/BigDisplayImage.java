package com.tschulte.paperwork;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;

public class BigDisplayImage extends AppCompatActivity {

    public static String FOLDER_PARCEL = "FOLDER_PARCEL";
    public static String PAGE_INTENT = "PAGE_INTENT";
    SubsamplingScaleImageView siv;
    FolderObject folder;
    int page;

    public void loadImage() {
        if (folder.type.equals(folder.TYPE_IMAGE)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final File imageFile = folder.requestFullImage(page);

                    if (imageFile != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                siv.setImage(ImageSource.uri(Uri.fromFile(imageFile)));
                                ProgressBar p = findViewById(R.id.progress_circular);
                                p.setVisibility(View.GONE);
                                siv.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

                    OCR ocr = new OCR();
                    // ocr.runOcr(bitmap, BigDisplayImage.this);
                }
            }).start();
        } else if (folder.type.equals(folder.TYPE_PDF)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bm = folder.getPdfSide(BigDisplayImage.this, page - 1, 0, 0);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            siv.setImage(ImageSource.bitmap(bm));
                            ProgressBar p = findViewById(R.id.progress_circular);
                            p.setVisibility(View.GONE);
                            siv.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }).start();
        } else {
            finish();
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        // supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.big_image_display);

        /*
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


         */
        Bundle extras = getIntent().getExtras();
        assert extras != null;

        siv = findViewById(R.id.touch_view);

        SpeedDialView speedDialView = findViewById(R.id.speed_dial_view);
        speedDialView.inflate(R.menu.dial_view_items);

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                if (actionItem.getId() == R.id.dial_view_next) {
                    int nextpage;
                    if ((page + 1) > folder.length) {
                        nextpage = 1;
                    } else nextpage = page + 1;

                    page = nextpage;
                    loadImage();

                } else if (actionItem.getId() == R.id.dial_view_minus_90) {
                    int rotation = siv.getAppliedOrientation();
                    int newRot;
                    if (rotation == 0) {
                        newRot = 270;
                    } else {
                        newRot = rotation - 90;
                    }
                    siv.setOrientation(newRot);
                } else if (actionItem.getId() == R.id.dial_view_plus_90) {
                    int rotation = siv.getAppliedOrientation();
                    int newRot;
                    if (rotation == 270) {
                        newRot = 0;
                    } else {
                        newRot = rotation + 90;
                    }
                    siv.setOrientation(newRot);
                } else if (actionItem.getId() == R.id.dial_view_prev) {
                    int nextpage;
                    if ((page - 1) == 0) {
                        nextpage = folder.length;
                    } else nextpage = page - 1;

                    page = nextpage;
                    loadImage();
                }
                return true;
            }
        });

        folder = (FolderObject) extras.getSerializable(FOLDER_PARCEL);

        page = extras.getInt(PAGE_INTENT, 1);
        loadImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_display, menu);
        return true;
    }

    /*
    @Override
    public boolean onSupportNavigateUp() {
        if (siv.isImageLoaded()) {
            siv.recycle();
        }
        finish();
        overridePendingTransition(0, 0);
        return true;
    }

     */
}

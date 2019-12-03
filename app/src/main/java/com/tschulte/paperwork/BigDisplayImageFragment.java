package com.tschulte.paperwork;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class BigDisplayImageFragment extends Fragment {

    static String FOLDER_PARCEL = "FOLDER_PARCEL";
    static String PAGE_INTENT = "PAGE_INTENT";
    private SubsamplingScaleImageView siv;
    private ProgressBar p;
    private FolderObject folder;
    private SpeedDialView speedDialView;
    private int page;

    private void loadImage() {
        if (folder.type.equals(FolderObject.TYPE_IMAGE)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final File imageFile = folder.requestFullImage(page);

                    if (imageFile != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                siv.setImage(ImageSource.uri(Uri.fromFile(imageFile)));
                                p.setVisibility(View.GONE);
                                siv.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                }
            }).start();
        } else if (folder.type.equals(FolderObject.TYPE_PDF)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bm = folder.getPdfSide(getActivity().getApplicationContext(), page - 1, 0, 0);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            siv.setImage(ImageSource.bitmap(bm));
                            p.setVisibility(View.GONE);
                            siv.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }).start();
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // super.onCreate(bundle);

        Bundle extras = getActivity().getIntent().getExtras();
        assert extras != null;

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
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.big_image_display, container, false);
        siv = view.findViewById(R.id.touch_view);
        speedDialView = view.findViewById(R.id.speed_dial_view);
        p = view.findViewById(R.id.progress_circular);

        return view;
    }
}

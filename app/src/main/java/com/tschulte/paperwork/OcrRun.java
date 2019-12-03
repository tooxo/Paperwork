package com.tschulte.paperwork;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessPdfRenderer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OcrRun extends Fragment {

    private SubsamplingScaleImageView siv;
    private Animation animation;
    private View bar;
    private ProgressBar progressBar;

    private void moveFile(File input, File output) throws IOException {
        if (output.createNewFile()) {
            try (FileChannel inputChannel = new FileInputStream(input).getChannel();
                 FileChannel outputChannel = new FileOutputStream(output).getChannel()) {
                inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            }
        }
    }

    private Bitmap createThumbnail(Bitmap bm) {
        if (bm.getWidth() > 400) {
            int thumbnailHeight = (int) (bm.getHeight() * ((float) 400.0 / bm.getWidth()));
            return ThumbnailUtils.extractThumbnail(bm, 400, thumbnailHeight);
        } else return bm;
    }

    private Bitmap createScanImage(Bitmap bm) {
        if (bm.getWidth() > 3000) {
            int scanHeight = (int) (bm.getHeight() * ((float) 3000.0 / bm.getWidth()));
            return ThumbnailUtils.extractThumbnail(bm, 3000, scanHeight);
        } else return bm;
    }

    private Bitmap fileToBitmap(File file) {
        return BitmapFactory.decodeFile(file.getPath());
    }

    private void saveHOCR(String hOCR, File output) throws IOException {
        if (!output.exists()) {
            if (!output.createNewFile()) {
                throw new IOException("File creation failed.");
            }
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(output));
        bw.write(hOCR);
        bw.flush();
        bw.close();
    }

    private boolean validityScan(File file) {
        try {
            return Arrays.asList("jpeg", "jpg", "png", "gif", "webp")
                    .contains(file.getName().split("\\.")[file.getName().split("\\.").length - 1]);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ocr_run, container, false);

        siv = view.findViewById(R.id.image);
        bar = view.findViewById(R.id.line);
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.scanline);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        return view;
    }

    private void massScan(List<File> files) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String folder = preferences.getString(MainActivity.STORAGE_DIRECTORY, "");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm_ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

        File newFolder = new File(folder + "/" + timeStamp);

        ((OcrContainer) getActivity()).folder = newFolder;

        Log.v(newFolder.getAbsolutePath(), newFolder.getAbsolutePath());
        // TODO : Prepare folder

        if (!newFolder.exists()) {
            if (!newFolder.mkdir()) {
                ; // TODO throw error missing permissions
            }
        }

        // if (1 == 1) return;
        int i = 1;
        OcrHelper ocrHelper = new OcrHelper();
        ocrHelper.prepareOcr(getActivity());
        for (File f : files) {
            // move image to new folder
            File image = new File(newFolder.getAbsolutePath() + "/" + "paper." + i + ".jpg");
            try {
                moveFile(f, image);
            } catch (IOException io) {
                io.printStackTrace();
            }

            // PREPARE THE BITMAP
            Bitmap bitmap = fileToBitmap(f);
            final Bitmap thumbnail = createThumbnail(bitmap);
            Bitmap scanImage = createScanImage(bitmap);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    siv.setImage(ImageSource.bitmap(thumbnail));
                    bar.startAnimation(animation);
                }
            });

            // RUN THE MAGICAL OcrHelper
            String hocr = ocrHelper.runOcr(scanImage);
            File words = new File(newFolder.getAbsolutePath() + "/" + "paper." + i + ".words");
            try {
                saveHOCR(hocr, words);
            } catch (IOException io) {
                io.printStackTrace();
                ; // TODO: Create Error
            }

            bar.clearAnimation();

            float p = (float) i / (float) files.size();
            int progress = (int) (p * 100.0);
            progressBar.setProgress(progress);

            i++;
        }

        onFinishListener();
    }

    private void onFinishListener() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                siv.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        });
        Log.v("Done!", "Going to Labels");
        ((OcrContainer) getActivity()).stepThree();
    }

    private void showUnsupportedDialog(List<File> files) {
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            sb.append(f.getName()).append(" is from an unsupported format, it will be skipped.\n\n");
        }

        if (sb.toString().equals("")) {
            return;

        }
        new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage(sb.toString())
                .setNeutralButton("ok", null)
                .show();
    }

    private void pdfScan(File pdf) {
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        TessPdfRenderer pdfRenderer = new TessPdfRenderer(tessBaseAPI, "hallo.pdf");
    }


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        List<File> files = ((OcrContainer) getActivity()).getFilesToOcr();

        final List<File> _files = new ArrayList<>();
        List<File> _failed_files = new ArrayList<>();
        for (File file : files) {
            if (validityScan(file)) {
                _files.add(file);
            } else _failed_files.add(file);
        }

        showUnsupportedDialog(_failed_files);

        new Thread(new Runnable() {
            @Override
            public void run() {
                massScan(_files);
            }
        }).start();
    }

}

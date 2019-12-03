package com.tschulte.paperwork;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kofigyan.stateprogressbar.StateProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class OcrContainer extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private StateProgressBar spb;
    private OcrRun ocrRun = new OcrRun();
    private OcrSelection ocrSelection = new OcrSelection();
    private OcrLabels ocrLabels = new OcrLabels();
    private OcrDone ocrDone = new OcrDone();
    private List<File> filesToOcr = new ArrayList<>();
    boolean pdf = false;
    File pdfToOcr = new File("");


    File folder = null;

    List<File> getFilesToOcr() {
        return filesToOcr;
    }

    void stepTwo(List<File> files) {

        filesToOcr = files;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(ocrSelection);
        fragmentTransaction.add(R.id.container, ocrRun);
        fragmentTransaction.commit();

        spb.setCurrentStateNumber(StateProgressBar.StateNumber.TWO);
    }

    void stepTwoPdf(File pdf) {
        this.pdf = true;
        pdfToOcr = pdf;
    }

    void stepThree() {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(ocrRun);
        fragmentTransaction.add(R.id.container, ocrLabels);
        fragmentTransaction.commit();

        spb.setCurrentStateNumber(StateProgressBar.StateNumber.THREE);

    }

    void stepFour() {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(ocrLabels);
        fragmentTransaction.add(R.id.container, ocrDone);
        fragmentTransaction.commit();
        spb.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ocr_container);

        String[] description = {getString(R.string.selection), getString(R.string.ocr), getString(R.string.labels), getString(R.string.done)};

        spb = findViewById(R.id.progress);
        final FrameLayout frameLayout = findViewById(R.id.container);

        spb.setStateDescriptionData(description);

        spb.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp.setMargins(0, 0, 0, spb.getHeight() + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
                frameLayout.setLayoutParams(lp);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                moveTessdata();
            }
        }).start();

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.container, ocrSelection);
        fragmentTransaction.commit();

    }


    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void moveTessdata() {
        File bigDir = new File(getFilesDir().getAbsolutePath() + "/Tesseract");
        File dir = new File(getFilesDir().getAbsolutePath() + "/Tesseract/tessdata");
        File ger = new File(getFilesDir().getAbsolutePath() + "/Tesseract/tessdata/deu.traineddata");
        File eng = new File(getFilesDir().getAbsolutePath() + "/Tesseract/tessdata/eng.traineddata");

        if (!bigDir.exists()) {
            bigDir.mkdir();
        }
        if (!dir.exists()) {
            dir.mkdir();
        }

        if (!ger.exists()) {
            try {
                InputStream is = getResources().openRawResource(R.raw.deu);
                FileOutputStream fileOutputStream = new FileOutputStream(ger);
                copyStream(is, fileOutputStream);
                fileOutputStream.close();
                is.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }

        if (!eng.exists()) {
            try {
                InputStream is = getResources().openRawResource(R.raw.eng);
                FileOutputStream fileOutputStream = new FileOutputStream(eng);
                copyStream(is, fileOutputStream);
                fileOutputStream.close();
                is.close();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }
}

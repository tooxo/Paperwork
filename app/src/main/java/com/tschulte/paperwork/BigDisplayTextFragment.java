package com.tschulte.paperwork;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.fragment.app.Fragment;

import com.leinardi.android.speeddial.SpeedDialView;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import kotlin.text.Charsets;

public class BigDisplayTextFragment extends Fragment {

    WebView wv;
    StringBuilder htmlPage;
    SpeedDialView speedDialView;
    static String IMAGE_FILE_PATH = "IMAGE_FILE_PATH";
    static String WORDS_FILE_PATH = "WORDS_FILE_PATH";


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ocr_display, container, false);
        wv = view.findViewById(R.id.webView1);
        speedDialView = view.findViewById(R.id.speed_dial_view);
        return view;
    }

    ImageObject imageObject;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onStart() {

        super.onStart();

        final FolderObject folder = (FolderObject) getActivity().getIntent().getExtras().getSerializable(BigDisplayImageFragment.FOLDER_PARCEL);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    imageObject = folder.imageObject(getActivity().getIntent().getIntExtra(BigDisplayImageFragment.PAGE_INTENT, 1), getActivity());
                } catch (IOException io) {
                    imageObject = new ImageObject("", "");
                    io.printStackTrace();
                }


                if (imageObject.imagePath.equals("") || imageObject.wordsPath.equals("")) {
                    ; // TODO: add not yet implemented error
                }

                Log.v("IMAGE-PATH", imageObject.imagePath);
                Log.v("WORDS-PATH", imageObject.wordsPath);

                htmlPage = new StringBuilder();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wv.getSettings().setJavaScriptEnabled(true);
                        wv.getSettings().setLoadWithOverviewMode(true);
                        wv.getSettings().setUseWideViewPort(true);
                        wv.getSettings().setBlockNetworkLoads(true);
                        wv.getSettings().setSupportZoom(true);
                        wv.getSettings().setBuiltInZoomControls(true);
                        wv.getSettings().setDisplayZoomControls(false);

                        wv.setInitialScale(1);
                    }
                });


                try {
                    htmlPage.append(getTextFile(imageObject.wordsPath))
                            .append(getHocrJS())
                            .append(generateImageInsert(imageObject.imagePath))
                            .append(getInsertFile());
                } catch (IOException ignored) {
                }

                File f = new File(getActivity().getFilesDir().getAbsolutePath() + "/test.html");

                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                    bw.write(htmlPage.toString());
                    bw.flush();
                    bw.close();
                }catch (IOException ignored) {
                    ignored.printStackTrace();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speedDialView.inflate(R.menu.dial_view_items);
                        wv.loadDataWithBaseURL(imageObject.imagePath.split("/")[imageObject.imagePath.split("/").length - 1], htmlPage.toString(), "text/html", Charsets.UTF_8.toString(), "");
                    }
                });

            }
        }).start();


    }

    private String generateImageInsert(String filePath) {
        String imageUrl = "file://" + filePath;

        return "<style>body { background: url('"+imageUrl+"'); background-repeat: no-repeat;}</style>";
    }

    private String getHocrJS() throws IOException {
        return "<script>" + getRawFile(R.raw.hocr) + "</script>";
    }

    private String getRawFile(int resourceId) throws IOException {
        InputStream raw = getResources().openRawResource(resourceId);
        BufferedReader is = new BufferedReader(new InputStreamReader(raw, Charsets.UTF_8));

        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = is.readLine()) != null) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    private String getTextFile(String path) throws IOException {
        File f = new File(path);
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }
        r.close();
        return sb.toString();

    }

    private String getInsertFile() throws IOException {
        return getRawFile(R.raw.insert);
    }
}

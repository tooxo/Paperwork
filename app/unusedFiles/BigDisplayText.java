package com.tschulte.paperwork;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import kotlin.text.Charsets;

public class BigDisplayText extends FragmentActivity {

    WebView wv;
    StringBuilder htmlPage;
    static String IMAGE_FILE_PATH = "IMAGE_FILE_PATH";
    static String WORDS_FILE_PATH = "WORDS_FILE_PATH";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);


        setContentView(R.layout.big_display_container);

        if (1 == 1) {
            return;
        }

        setContentView(R.layout.ocr_display);

        String imagePath = bundle.getString(IMAGE_FILE_PATH, "");
        String wordsPath = bundle.getString(WORDS_FILE_PATH, "");

        if (imagePath.equals("") || wordsPath.equals("")) {
            ; // TODO: add not yet implemented error
        }

        wv = findViewById(R.id.webView1);
        htmlPage = new StringBuilder();

        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setBlockNetworkLoads(true);
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setBuiltInZoomControls(true);

        wv.setInitialScale(1);

        try {
            htmlPage.append(getTextFile())
                    .append(generateImageInsert(imagePath))
                    .append(getInsertFile())
                    .append(getHocrJS());
        } catch (IOException ignored) {
            finish();
        }

        wv.loadData(htmlPage.toString(), "text/html", Charsets.UTF_8.toString());
    }

    String generateImageInsert(String filePath) {
        String imageUrl = "file://" + filePath;
        return "<style>" +
                ".hocr-viewer {" +
                "background: url('" + imageUrl + "');" +
                "}" +
                ".ocrx_word {" +
                "opacity: 0 !important;" +
                "}" +
                ".ocrx_word:hover {" +
                "opacity: 1 !important;" +
                "background: white !important;" +
                "}" +
                "</style>";
    }

    String getHocrJS() throws IOException {
        return "<script>" + getRawFile(R.raw.hocr) + "</script>";
    }

    String getTextFile() throws IOException {
        return getRawFile(R.raw.test);
    }

    String getRawFile(int resourceId) throws IOException {
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

    String getInsertFile() throws IOException {
        return getRawFile(R.raw.insert);
    }
}

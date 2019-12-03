package com.tschulte.paperwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;


public class OcrHelper {

    private String FOLDER = "";

    static String languageString(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(Settings.TESSDATA_LANGUAGE, "eng+ger");
    }

    public boolean tesseractExists() {
        return new File(FOLDER).exists();
    }

    private TessBaseAPI tessBaseAPI;

    void prepareOcr(Context context) {
        FOLDER = context.getFilesDir().getAbsolutePath() + "/Tesseract/";
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(FOLDER, languageString(context));
    }

    String runOcr(Bitmap bitmap) {
        String extracted = extractTextFromImage(bitmap);

        Log.v("OcrHelper", "Finished!");
        return extracted;
    }

    private String extractTextFromImage(Bitmap inputImage) {
        // tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
        tessBaseAPI.setImage(inputImage);
        String extractedText = "";
        try {
            Log.v("OcrHelper", "Extracting Text");
            extractedText = tessBaseAPI.getHOCRText(1);
        } catch (Exception ignored) {
        }
        Log.v("OcrHelper", "Cleaning up!");
        tessBaseAPI.clear();
        return extractedText;
    }

    void cleanUp() {
        tessBaseAPI.clear();
        tessBaseAPI.end();
    }
}
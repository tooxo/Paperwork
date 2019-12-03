package com.tschulte.paperwork;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class OcrSelection extends Fragment {

    MaterialButton addImage;
    MaterialButton addPdf;

    private static final int READ_REQ_CODE_IMAGE = 123;
    private static final int READ_REQ_CODE_PDF = 567;

    private void openImageFileBrowser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        startActivityForResult(intent, READ_REQ_CODE_IMAGE);
    }

    private void openPdfFileBrowser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("document/pdf");

        startActivityForResult(intent, READ_REQ_CODE_PDF);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQ_CODE_IMAGE && resultCode == RESULT_OK) {
            if (resultData != null) {

                List<File> files = new ArrayList<>();

                // ClipData clipData = resultData.getClipData();
                Uri _path;
                if ((_path = resultData.getData()) != null) {
                    String p = FileUtils.getPath(getActivity(), _path);
                    Log.w("p", p);
                    if (p != null) {
                        files.add(new File(p));
                    }
                }

                ClipData clipData;

                if ((clipData = resultData.getClipData()) != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item path = clipData.getItemAt(i);
                        Uri uri = path.getUri();
                        String p = FileUtils.getPath(getActivity(), uri);
                        if (p == null) {
                            Log.w("Continued", uri.toString());
                            continue;
                        }
                        files.add(new File(p));
                    }

                }


                onFinished(files);
            }
        } else if (requestCode == READ_REQ_CODE_PDF && resultCode == RESULT_OK) {
            Uri _path;
            if ((_path = resultData.getData()) != null) {
                String p = FileUtils.getPath(getActivity(), _path);
                if (p != null) {
                    onPdfFinished(new File(p));
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ocr_selection, container, false);
        addImage = view.findViewById(R.id.addImage);
        addPdf = view.findViewById(R.id.addPdf);
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageFileBrowser();
            }
        });
        addPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return view;

    }

    private void onFinished(List<File> files) {
        if (files.size() > 0) {
            ((OcrContainer) getActivity()).stepTwo(files);
        }
    }

    private void onPdfFinished(@NonNull File pdf) {
        ((OcrContainer) getActivity()).stepTwoPdf(pdf);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}

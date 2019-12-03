package com.tschulte.paperwork;

import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OcrDone extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.ocr_done, container, false);

        final VideoView vw = view.findViewById(R.id.videoView);
        Uri uri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.haken);

        vw.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {

                Log.wtf("t", i + "");
                if (i == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    Log.v("GO", "GO");
                    view.findViewById(R.id.block).animate().alpha(0).setDuration(200).start();
                    return true;
                }
                return false;
            }
        });

        vw.setVideoURI(uri);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vw.setAudioFocusRequest(AudioManager.AUDIOFOCUS_NONE);
        }


        vw.setZOrderOnTop(true);
        SurfaceHolder surfaceHolder = vw.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        vw.start();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}

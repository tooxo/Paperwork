package com.tschulte.paperwork;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

class FileUtils {
    private static String pathResolver(final Context context, final Uri uri) {
        Log.w("Uri", uri.toString());
        Log.w("prov", uri.getAuthority());
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else if (uri.toString().contains("home%3A/document/home%3A")) {
                    return Environment.getExternalStorageDirectory() + "/Documents";
                }
                else {
                    final int splitIndex = docId.indexOf(':', 1);
                    final String tag = docId.substring(0, splitIndex);
                    final String path = docId.substring(splitIndex + 1);

                    String nonPrimaryVolume = getPathToNonPrimaryVolume(context, tag);
                    if (nonPrimaryVolume != null) {
                        String result = nonPrimaryVolume + "/" + path;
                        File file = new File(result);
                        if (file.exists() && file.canRead()) {
                            return result;
                        }
                        return null;
                    }
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    static String getPath(final Context context, final Uri uri) {
        try {
            return pathResolver(context, uri);
        } catch (Exception e) {
            try {
                InputStream is = context.getContentResolver().openInputStream(uri);
                if (is == null) return null;
                File tempOutput = File.createTempFile("temp_ocr", ".jpg", context.getCacheDir());
                OutputStream os = new FileOutputStream(tempOutput);
                copyStream(is, os);
                is.close();
                os.close();
                tempOutput.deleteOnExit();
                return tempOutput.getAbsolutePath();
            } catch (IOException fnf) {
                fnf.printStackTrace();
            }
        }
        return null;
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getPathToNonPrimaryVolume(Context context, String tag) {
        File[] volumes = context.getExternalCacheDirs();
        if (volumes != null) {
            for (File volume : volumes) {
                if (volume != null) {
                    String path = volume.getAbsolutePath();
                    int index = path.indexOf(tag);
                    if (index != -1) {
                        return path.substring(0, index) + tag;

                    }
                }
            }
        }
        return null;
    }

}
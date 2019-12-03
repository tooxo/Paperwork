package com.tschulte.paperwork;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FolderObject implements Serializable {

    static String TYPE_PDF = "PDF PDF";
    static String TYPE_IMAGE = "IMAGE IMAGE";
    static String TYPE_UNKNOWN = "UNKNOWN UNKNOWN";

    String fileName;
    boolean valid;
    List<String[]> labels;
    private File file;
    String type;
    private String text = "";
    private List<List<String[]>> ocr;
    int length;

    FolderObject(File file) {
        this.file = file;
        Log.w("File", file.getAbsolutePath());
        fileName = file.getName();
        valid = isValid();
        labels = getLabel(file);
        type = determineType();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ocr = determineOcr();
                StringBuilder sb = new StringBuilder();
                for (List<String[]> l : ocr) {
                    for (String[] ls : l) {
                        sb.append(ls[5]);
                    }
                }
                text = sb.toString();
            }
        }).start();
        if (type.equals(TYPE_IMAGE)) {
            length = getImagePagesLength();
        } else if (type.equals(TYPE_PDF)) {
            length = getPDFLength();
        }
    }

    private String determineType() {
        if (valid) {
            for (File file : file.listFiles()) {
                if (file.getName().equals("doc.pdf")) {
                    return TYPE_PDF;
                }
                if (file.getName().equals("paper.1.jpg")) {
                    return TYPE_IMAGE;
                }
            }
        }
        return TYPE_UNKNOWN;
    }

    private List<List<String[]>> determineOcr() {
        List<File> words = getPagesImage();

        List<List<String[]>> ocrs = new ArrayList<>();
        int i = 1;
        for (File f : words) {
            List<String[]> ocr = getOCRText(i);
            ocrs.add(ocr);
            i++;
        }
        return ocrs;
    }

    String getText() {
        if (text != null) {
            return text.toLowerCase();
        } else return "";
    }

    File getFile_() {
        return file;
    }

    List<Integer> search(String term) {
        if (text.toLowerCase().contains(term.toLowerCase())) {
            int i = 1;
            List<Integer> ints = new ArrayList<>();
            for (List<String[]> ls : ocr) {
                for (String[] l : ls) {
                    if (l[5].toLowerCase().contains(term.toLowerCase())) {
                        if (!ints.contains(i)) {
                            ints.add(i);
                        }
                    }
                }
                i++;
            }
            return ints;

        }
        return new ArrayList<>();
    }

    File imageByPage(int page) {
        if (page > length) {
            return null;
        }
        File f = new File(file.getAbsolutePath() + "/paper." + page + ".jpg");
        if (f.exists()) {
            return f;
        } else {
            return null;
        }
    }

    File pdfByPage(int page, Context context) throws IOException {
        File file = File.createTempFile("pdf_page", ".jpg", context.getCacheDir());

        int[] xy = getPdfSize();

        Bitmap bitmap = getPdfSide(context, page - 1, xy[0], xy[1]);
        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        bitmap.recycle();
        file.deleteOnExit();
        return file;
    }

    File imageFile(int page, Context context) throws IOException {
        if (this.type.equals(FolderObject.TYPE_IMAGE)) {
            return imageByPage(page);
        } else if (this.type.equals(FolderObject.TYPE_PDF)) {
            return pdfByPage(page, context);
        }
        return null;
    }

    File wordsByPage(int page) {
        return new File(file.getAbsolutePath() + "/" + "paper." + page + ".words");
    }

    ImageObject imageObject(int page, Context context) throws IOException {
        return new ImageObject(imageFile(page, context).getAbsolutePath(), wordsByPage(page).getAbsolutePath());
    }

    private List<String[]> getLabel(File folder) {
        if (!folder.exists()) {
            return new ArrayList<>();
        }
        File[] files = folder.listFiles();
        File labels = null;
        for (File file : files) {
            if (file.getName().endsWith("labels")) {
                labels = file;
            }
        }
        List<String[]> labelList = new ArrayList<>();
        if (labels != null) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(labels));
                String line;


                while ((line = br.readLine()) != null) {
                    Pattern label_regex = Pattern.compile("(\\S*),(rgb\\(\\d{1,3},\\d{1,3},\\d{1,3}\\))");
                    Matcher m = label_regex.matcher(line);
                    while (m.find()) {
                        String labelName = m.group(1);
                        String labelColor = m.group(2);
                        String[] label = new String[2];
                        label[0] = labelName;
                        label[1] = labelColor;
                        labelList.add(label);
                    }

                }

                return labelList;
            } catch (IOException fnf) {
                fnf.printStackTrace();
                return labelList;
            }
        }
        return labelList;
    }


    private boolean isValid() {
        if (file.isFile()) {
            return false;
        }

        try {
            new SimpleDateFormat("yyyyMMdd_HHmm_ss", Locale.getDefault()).parse(fileName);
        } catch (ParseException pe) {
            return false;
        }

        File[] files = file.listFiles();
        Log.wtf("Files", file.getAbsolutePath());
        Log.wtf("length", String.valueOf(files.length));

        try {
            for (File file : files) {
                if (file.getName().contains("paper.1")) {
                    return true;
                }
                if (file.getName().equals("doc.pdf")) {
                    return true;
                }
            }
        } catch (NullPointerException ne) {
            Log.wtf("ne", ne.toString());
        }
        return false;
    }

    private File getFirstFile() {
        for (File file : file.listFiles()) {
            if (file.getName().contains("paper.1.") && !file.getName().contains("thumb")) {
                return file;
            } else if (file.getName().contains(".pdf")) {
                return file;
            }
        }
        return new File("");
    }

    private File getPdf() {
        for (File file : file.listFiles()) {
            if (file.getName().contains(".pdf")) {
                return file;
            }
        }
        return null;
    }

    Bitmap getThumbnail() throws FileNotFoundException {
        for (File file : file.listFiles()) {
            if (file.getName().contains("thumb")) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                return BitmapFactory.decodeStream(new FileInputStream(file), null, options);
            }
        }
        File toThumbnail = getFirstFile();
        if (toThumbnail.getName().contains(".pdf")) {
            // TODO PDF THUMBNAIL
        } else if (!toThumbnail.equals(new File(""))) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(toThumbnail.getAbsolutePath(), options);

            int desiredWidth = 150;
            int desiredHeight = 200;
            float widthScale = (float) options.outWidth / desiredWidth;
            float heightScale = (float) options.outHeight / desiredHeight;
            float scale = Math.min(widthScale, heightScale);

            int sampleSize = 1;
            while (sampleSize < scale) {
                sampleSize *= 2;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(toThumbnail.getAbsolutePath(), options);
        }


        int w = 150, h = 200;
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        return Bitmap.createBitmap(w, h, config);
    }

    List<String[]> getOCRText(int pageNumber) {
        List<String[]> list = new ArrayList<>();
        File file = new File(this.file.getAbsolutePath() + "/paper." + String.valueOf(pageNumber) + ".words");
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                String contents = sb.toString();
                Pattern pattern = Pattern.compile(
                        "<span class=\"ocrx_word\" title=\"bbox (\\d+) (\\d+) (\\d+) (\\d+); x_wconf (\\d+)\">([A-z ]*)<\\/span>");
                Matcher m = pattern.matcher(contents);

                while (m.find()) {
                    String topleft = m.group(1);
                    String topright = m.group(2);
                    String bottomleft = m.group(3);
                    String bottomright = m.group(4);

                    String confidence = m.group(5);
                    String text = m.group(6);

                    String[] box = new String[6];
                    box[0] = topleft;
                    box[1] = topright;
                    box[2] = bottomleft;
                    box[3] = bottomright;
                    box[4] = confidence;
                    box[5] = text;

                    list.add(box);
                }

            } catch (IOException ignored) {

            }
        }
        return list;
    }

    private List<File> getPagesImage() {
        File[] filenames = file.listFiles();
        List<File> pages = new ArrayList<>();
        for (File file : filenames) {
            if (file.getName().matches("paper\\.\\d+\\.words")) {
                pages.add(file);
            }
        }
        return pages;
    }

    static int determineTextColor(int r, int g, int b) {
        if ((r * 0.299 + g * 0.587 + b * 0.114) > 186) {
            return Color.BLACK;
        } else return Color.WHITE;
    }


    List<Bitmap> getImageThumbnails() {
        List<Bitmap> thumbnails = new ArrayList<>();
        for (File file : file.listFiles()) {
            String f = file.getName();
            Pattern p = Pattern.compile("paper\\.(\\d+)\\.jpg");
            Matcher m = p.matcher(f);
            if (!f.matches(p.toString())) {
                continue;
            }
            boolean con = false;
            while (m.find()) {
                String number = m.group(1);
                String eventualThumbnailName = "paper." + number + ".thumb.jpg";
                String eventualThumbnailLocation = file.getAbsolutePath() + "/" + eventualThumbnailName;
                File eventualThumbnail = new File(eventualThumbnailLocation);
                if (eventualThumbnail.exists()) {
                    try {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bm = BitmapFactory.decodeStream(new FileInputStream(eventualThumbnail), null, options);
                        thumbnails.add(bm);
                        con = true;
                    } catch (IOException ignored) {
                    }
                }
            }
            if (con) {
                continue;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            int desiredWidth = 300;
            int desiredHeight = 400;
            float widthScale = (float) options.outWidth / desiredWidth;
            float heightScale = (float) options.outHeight / desiredHeight;
            float scale = Math.min(widthScale, heightScale);

            int sampleSize = 1;
            while (sampleSize < scale) {
                sampleSize *= 2;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;

            thumbnails.add(BitmapFactory.decodeFile(file.getAbsolutePath(), options));
        }

        return thumbnails;
    }

    private int getImagePagesLength() {
        int i = 0;
        for (File file : file.listFiles()) {
            if (file.getName().matches("paper\\.\\d+\\.jpg")) {
                i++;
            }
        }
        return i;
    }

    File requestFullImage(int page) {
        if (page > getImagePagesLength()) {
            return null;
        }

        // File object creation
        String path = file.getAbsolutePath();
        String file_suffix = "/paper." + String.valueOf(page) + ".jpg";
        Log.v("Recieving", file_suffix);

        File file = new File(path + file_suffix);

        if (!file.exists()) {
            return null;
        }

        return file;
    }

    int getPDFLength() {
        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(getPdf(), ParcelFileDescriptor.MODE_READ_ONLY));
            return renderer.getPageCount();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    List<Bitmap> getPDFThumbnails(Context context) {
        List<Bitmap> thumbnails = new ArrayList<>();

        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(getPdf(), ParcelFileDescriptor.MODE_READ_ONLY));

            Bitmap bm;
            final int pageCount = renderer.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);

                int width = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                int height = context.getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();

                float multiplier = Float.valueOf("400") / width;

                bm = Bitmap.createBitmap((int) (width * multiplier), (int) (height * multiplier), Bitmap.Config.ARGB_8888);
                page.render(bm, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                thumbnails.add(bm);

                page.close();
            }
            renderer.close();
        } catch (IOException io) {
            io.printStackTrace();
        }

        return thumbnails;
    }

    Bitmap getPdfSide(Context context, int page, int w, int h) {
        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(getPdf(), ParcelFileDescriptor.MODE_READ_ONLY));
            Bitmap bm;
            PdfRenderer.Page page1 = renderer.openPage(page);

            int width;
            int height;
            if (w == 0) {
                width = context.getResources().getDisplayMetrics().densityDpi / 72 * page1.getWidth();
            } else {
                width = w;
            }

            if (h == 0) {
                height = context.getResources().getDisplayMetrics().densityDpi / 72 * page1.getHeight();
            } else {
                height = h;
            }

            bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            page1.render(bm, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            page1.close();
            return bm;
        } catch (IOException io) {
            return null;
        }
    }

    int[] getPdfSize() throws IOException {
        File f = this.file;

        Pattern PATTERN = Pattern.compile("bbox \\d{1,5} \\d{1,5} (\\d{1,5}) (\\d{1,5})");


        int x = 0;
        int y = 0;
        for (File s : f.listFiles()) {
            if (!s.getName().contains("words")) {
                continue;
            }
            BufferedReader br = new BufferedReader(new FileReader(s));
            StringBuilder sb = new StringBuilder();
            String l;
            while ((l = br.readLine()) != null) {
                sb.append(l);
            }

            Matcher m = PATTERN.matcher(sb.toString());

            while (m.find()) {
                String ex = m.group(1);
                String yp = m.group(2);

                if (Integer.valueOf(ex) >= x) {
                    x = Integer.valueOf(ex);
                }
                if (Integer.valueOf(yp) >= y) {
                    y = Integer.valueOf(yp);
                }
            }
        }
        int[] xy = {x, y};
        return xy;
    }

}

package com.tschulte.paperwork;

import java.io.File;
import java.io.Serializable;

public class ImageObject implements Serializable {

    String imagePath;
    String wordsPath;
    int page;

    ImageObject(String imagePath, String wordsPath) {
        this.imagePath = imagePath;
        this.wordsPath = wordsPath;
    }

    ImageObject(FolderObject folder, int page) {
        this.page = page;
        if (page > folder.length) {
            throw new IndexOutOfBoundsException("Page " + String.valueOf(page) + " isn't there!");
        }

        if (folder.type.equals(FolderObject.TYPE_IMAGE)) {
            imagePath = folder.getFile_().getAbsolutePath();
        } else if (folder.type.equals(FolderObject.TYPE_PDF)) {

        }
    }

    File imageFile() {
        return new File(imagePath);
    }

    File wordsPath() {
        return new File(wordsPath);
    }
}

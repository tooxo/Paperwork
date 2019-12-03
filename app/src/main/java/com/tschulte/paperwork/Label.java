package com.tschulte.paperwork;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class Label {
    private String text;
    private int color;

    Label(String text, int color) {
        this.text = text;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public String getText() {
        return text;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setText(String text) {
        this.text = text;
    }

    String toLabelFile() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.text).append(",rgb(");
        int red = Color.red(this.color);
        int green = Color.green(this.color);
        int blue = Color.blue(this.color);
        sb.append(red).append(",").append(green).append(",").append(blue).append(")").append("\n");
        return sb.toString();
    }

    static List<String> labelListToStringList(List<Label> labels) {
        List<String> ret = new ArrayList<>();
        for (Label l : labels) {
            ret.add(l.getText());
        }
        return ret;
    }

    static List<int[]> labelListToColorList(List<Label> labels) {
        List<int[]> colors = new ArrayList<>();
        for (Label l : labels) {
            int[] color = {l.getColor(), Color.BLACK, Color.WHITE, Color.WHITE};
            colors.add(color);
        }
        return colors;
    }


}

package xyz.victorolaitan.scholar.util;

import android.graphics.Color;

public enum SubjectHue {

    PINK("#f8bbd0", "#e91e63", "#c2185b"),
    PURPLE("#e1bee7", "#673ab7", "#7b1fa2"),
    BLUE("#bbdefb", "#2196f3", "#1976d2"),
    TEAL("#b2dfdb", "#009688", "#00796b"),
    ORANGE("#ffe0b2", "#ff9800", "#f57c00"),
    BROWN("#d7ccc8", "#795548", "#5d4037"),
    GREY("#cfd8dc", "#607d8b", "#455a64");

    String[] values;

    SubjectHue(String... values) {
        this.values = values;
    }

    public int getLightColor() {
        return Color.parseColor(values[0]);
    }

    public int getColor() {
        return Color.parseColor(values[1]);
    }

    public int getDarkColor() {
        return Color.parseColor(values[2]);
    }
}

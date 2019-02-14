package xyz.victorolaitan.scholar.util;

import android.support.annotation.NonNull;

import java.util.UUID;

public interface Indexable extends ScholarModel {

    @NonNull
    UUID getId();
}

package xyz.victorolaitan.scholar.util;

import android.text.Editable;
import android.text.TextWatcher;

public interface TextChangeListener extends TextWatcher {

    @Override
    default void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    default void afterTextChanged(Editable s) {
    }
}

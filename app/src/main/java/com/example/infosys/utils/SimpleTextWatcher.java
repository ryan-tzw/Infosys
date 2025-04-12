package com.example.infosys.utils;

import android.text.Editable;
import android.text.TextWatcher;

public class SimpleTextWatcher implements TextWatcher {

    private final CharSequenceCallback charSequenceCallback;

    public SimpleTextWatcher(Runnable runnable) {
        this.charSequenceCallback = charSequence -> runnable.run();
    }

    public SimpleTextWatcher(CharSequenceCallback charSequenceCallback) {
        this.charSequenceCallback = charSequenceCallback;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        if (charSequenceCallback != null) {
            charSequenceCallback.onTextChanged(charSequence);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    public interface CharSequenceCallback {
        void onTextChanged(CharSequence charSequence);
    }
}

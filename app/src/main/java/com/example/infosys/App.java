package com.example.infosys;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle()
                .addObserver(new AppLifecycleObserver(this));
    }
}


package com.example.infosys.interfaces;

public interface LoginCallback {
    void onLoginSuccess();

    void onLoginFailure(Exception e);
}

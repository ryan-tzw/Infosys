package com.example.infosys.interfaces;

public interface LoginNavCallback {
    void onLoginSuccess();

    void onLoginFailure(Exception e);
}

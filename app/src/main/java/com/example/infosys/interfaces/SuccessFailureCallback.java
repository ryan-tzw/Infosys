package com.example.infosys.interfaces;

public interface SuccessFailureCallback {
    void onSuccess();

    void onFailure(Exception e);
}

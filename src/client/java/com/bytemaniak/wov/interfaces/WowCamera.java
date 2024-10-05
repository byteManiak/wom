package com.bytemaniak.wov.interfaces;

public interface WowCamera {
    void wov$applyRotation(double dx, double dy, boolean reset);
    float wov$getOffsetYaw();
    void wov$applyZoom(double amount);
}

// OnShootListener.aidl
package com.jpb.libchecker.services;

// Declare any non-default types here with import statements

interface OnShootListener {
    void onShootFinished(in long timestamp);
    void onProgressUpdated(in int progress);
}

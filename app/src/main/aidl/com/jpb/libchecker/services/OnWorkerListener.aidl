package com.jpb.libchecker.services;

interface OnWorkerListener {
    void onReceivePackagesChanged(in String packageName, in String action);
}

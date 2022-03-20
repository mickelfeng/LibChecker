package com.jpb.libchecker.services;
import com.jpb.libchecker.services.OnWorkerListener;

interface IWorkerService {
    void initKotlinUsage();
    void registerOnWorkerListener(in OnWorkerListener listener);
    void unregisterOnWorkerListener(in OnWorkerListener listener);
}

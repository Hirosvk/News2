package com.example.news2;

import android.net.NetworkInfo;

public interface DownloadCallback<T> {
    interface Progress {
        int ERROR = -1;
        int CONNECT_SUCCESS = 0;
        int GET_INPUT_STREAM_SUCCESS = 1;
    }

    void updateFromDownloads(T result);
    void finishedDownloading();
    NetworkInfo getActiveNetworkInfo();
    void onProgressUpdate(int progressCode);

}

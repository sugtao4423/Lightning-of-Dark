package com.tao.lightning_of_dark;

import android.os.Handler;
import android.os.Looper;

public abstract class UiHandler extends Handler implements Runnable {

    public UiHandler() {
        super(Looper.getMainLooper());
    }

    public boolean post() {
        return post(this);
    }

    @Override
    public abstract void run();
}

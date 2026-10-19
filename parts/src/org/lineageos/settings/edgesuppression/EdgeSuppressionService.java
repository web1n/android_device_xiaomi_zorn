/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.edgesuppression;

import android.app.Service;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;

public class EdgeSuppressionService extends Service {

    private static final String TAG = "XiaomiPartsEdgeSuppressionService";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private EdgeSuppressionManager mEdgeSuppressionManager;
    private DisplayManager mDisplayManager;
    private int mRotation = -1;

    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {}

        @Override
        public void onDisplayRemoved(int displayId) {}

        @Override
        public void onDisplayChanged(int displayId) {
            if (displayId != Display.DEFAULT_DISPLAY) {
                return;
            }
            Display display = mDisplayManager.getDisplay(displayId);
            if (display != null && display.getRotation() != mRotation) {
                applyCurrentSettings();
            }
        }
    };

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "Creating service");
        super.onCreate();
        mEdgeSuppressionManager = EdgeSuppressionManager.getInstance(getApplicationContext());
        mDisplayManager = getSystemService(DisplayManager.class);
        mDisplayManager.registerDisplayListener(mDisplayListener, new Handler(getMainLooper()));
        getPackageManager().setComponentEnabledSetting(
            new ComponentName(this, "org.lineageos.settings.edgesuppression.EdgeSuppressionSettingsActivity"),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "onStartCommand");
        applyCurrentSettings();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        mDisplayManager.unregisterDisplayListener(mDisplayListener);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (DEBUG) Log.d(TAG, "onConfigurationChanged");
        applyCurrentSettings();
    }

    private void applyCurrentSettings() {
        Display display = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY);
        if (display != null) {
            mRotation = display.getRotation();
        }
        mEdgeSuppressionManager.handleEdgeSuppressionChange();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

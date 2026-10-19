/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.edgesuppression;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import com.android.settingslib.widget.MainSwitchPreference;

import org.lineageos.settings.R;

public class EdgeSuppressionSettingsFragment extends PreferenceFragmentCompat implements OnPreferenceChangeListener {

    private EdgeSuppressionManager mEdgeSuppressionManager;
    private SharedPreferences mSharedPreferences;

    private MainSwitchPreference mSwitchBar;
    private SeekBarPreference mWidthPreference;

    private EdgeSuppressionPreviewView mPreview;
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
                updateEdgeSuppression();
            }
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        mEdgeSuppressionManager = EdgeSuppressionManager.getInstance(getActivity().getApplicationContext());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        mDisplayManager = getContext().getSystemService(DisplayManager.class);

        setPreferencesFromResource(R.xml.settings_edgesuppression, rootKey);

        mSwitchBar = (MainSwitchPreference) findPreference(EdgeSuppressionManager.PREF_ENABLED);
        if (mSwitchBar != null) {
            mSwitchBar.setDefaultValue(mEdgeSuppressionManager.getDefaultState());
            mSwitchBar.setOnPreferenceChangeListener(this);
        }

        mWidthPreference = (SeekBarPreference) findPreference(EdgeSuppressionManager.PREF_WIDTH);
        if (mWidthPreference != null) {
            mWidthPreference.setUpdatesContinuously(true);
            mWidthPreference.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup decorView = (ViewGroup) getActivity().getWindow().getDecorView();
        mPreview = new EdgeSuppressionPreviewView(getContext(),
                mEdgeSuppressionManager.getPhysicalWidth(),
                mEdgeSuppressionManager.getPhysicalHeight());
        decorView.addView(mPreview, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        updateEdgeSuppression();
    }

    @Override
    public void onStart() {
        super.onStart();
        mDisplayManager.registerDisplayListener(mDisplayListener, new Handler(getContext().getMainLooper()));
        mDisplayListener.onDisplayChanged(Display.DEFAULT_DISPLAY);
    }

    @Override
    public void onStop() {
        mDisplayManager.unregisterDisplayListener(mDisplayListener);
        super.onStop();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (preference == mSwitchBar) {
            editor.putBoolean(EdgeSuppressionManager.PREF_ENABLED, (Boolean) newValue);
        } else if (preference == mWidthPreference) {
            editor.putInt(EdgeSuppressionManager.PREF_WIDTH, (Integer) newValue);
        } else {
            return false;
        }
        // Preference listeners run before the framework persists the new value.
        editor.apply();
        updateEdgeSuppression();
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateEdgeSuppression();
    }

    @Override
    public void onDestroyView() {
        if (mPreview != null) {
            ViewGroup parent = (ViewGroup) mPreview.getParent();
            if (parent != null) {
                parent.removeView(mPreview);
            }
            mPreview = null;
        }
        super.onDestroyView();
    }

    private void updateEdgeSuppression() {
        Display display = mDisplayManager.getDisplay(Display.DEFAULT_DISPLAY);
        if (display != null) {
            mRotation = display.getRotation();
        }
        int[] rectangles = mEdgeSuppressionManager.handleEdgeSuppressionChange();
        if (mPreview != null) {
            mPreview.setRectangles(rectangles);
        }
    }
}

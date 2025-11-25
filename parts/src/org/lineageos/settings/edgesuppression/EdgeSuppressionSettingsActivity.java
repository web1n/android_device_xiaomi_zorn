/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.edgesuppression;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;
import com.android.settingslib.collapsingtoolbar.R;

public class EdgeSuppressionSettingsActivity extends CollapsingToolbarBaseActivity {

    private EdgeSuppressionSettingsFragment mEdgeSuppressionSettingsFragment;
    private static final String TAG = "edgesuppression";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment == null) {
            mEdgeSuppressionSettingsFragment = new EdgeSuppressionSettingsFragment();
            getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame, mEdgeSuppressionSettingsFragment, TAG)
                .commit();
        } else {
            mEdgeSuppressionSettingsFragment = (EdgeSuppressionSettingsFragment) fragment;
        }
    }
}

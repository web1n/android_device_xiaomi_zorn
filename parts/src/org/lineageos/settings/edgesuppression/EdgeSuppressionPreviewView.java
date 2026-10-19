/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.edgesuppression;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.RotationUtils;
import android.view.Display;
import android.view.Surface;
import android.view.View;

import org.lineageos.settings.R;

final class EdgeSuppressionPreviewView extends View {
    private final int mPhysicalWidth;
    private final int mPhysicalHeight;
    private final Paint mPaint = new Paint();
    private final Path mPath = new Path();
    private final Rect mBounds = new Rect();
    private final DisplayMetrics mMetrics = new DisplayMetrics();
    private final int[] mLocation = new int[2];
    private int[] mRectangles;

    EdgeSuppressionPreviewView(Context context, int physicalWidth, int physicalHeight) {
        super(context);
        mPhysicalWidth = physicalWidth;
        mPhysicalHeight = physicalHeight;
        mPaint.setColor(context.getColor(R.color.restricted_tip_area_color));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void setRectangles(int[] rectangles) {
        mRectangles = rectangles.clone();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Display display = getDisplay();
        if (display == null || mRectangles == null) {
            return;
        }

        int rotation = display.getRotation();
        boolean horizontal = rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;
        int rotatedWidth = horizontal ? mPhysicalHeight : mPhysicalWidth;
        int rotatedHeight = horizontal ? mPhysicalWidth : mPhysicalHeight;
        display.getRealMetrics(mMetrics);
        getLocationOnScreen(mLocation);

        mPath.rewind();
        for (int i = 0; i < mRectangles.length; i += 8) {
            mBounds.set(mRectangles[i + 2], mRectangles[i + 3],
                    mRectangles[i + 4], mRectangles[i + 5]);
            if (mBounds.isEmpty()) {
                continue;
            }
            RotationUtils.rotateBounds(mBounds, mPhysicalWidth, mPhysicalHeight, rotation);
            mPath.addRect(mBounds.left, mBounds.top, mBounds.right, mBounds.bottom,
                    Path.Direction.CW);
        }

        canvas.save();
        canvas.translate(-mLocation[0], -mLocation[1]);
        canvas.scale((float) mMetrics.widthPixels / rotatedWidth,
                (float) mMetrics.heightPixels / rotatedHeight);
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
    }
}

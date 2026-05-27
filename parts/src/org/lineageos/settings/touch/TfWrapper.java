/*
 * Copyright (C) 2023-2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.touch;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import vendor.xiaomi.hw.touchfeature.ITouchFeature;

public class TfWrapper {

    private static final String TAG = "XiaomiPartsTouchFeatureWrapper";
    private static final int TRANSACTION_SET_MODE_LONG_VALUE = IBinder.FIRST_CALL_TRANSACTION + 7;

    private static ITouchFeature mTouchFeature;

    public static ITouchFeature getTouchFeature() {
        if (mTouchFeature == null) {
            Log.d(TAG, "getTouchFeature: mTouchFeature=null");
            try {
                var name = "default";
                var fqName = vendor.xiaomi.hw.touchfeature.ITouchFeature.DESCRIPTOR + "/" + name;
                var binder = android.os.Binder.allowBlocking(android.os.ServiceManager.waitForDeclaredService(fqName));
                mTouchFeature = vendor.xiaomi.hw.touchfeature.ITouchFeature.Stub.asInterface(binder);
            } catch (Exception e) {
                Log.e(TAG, "getTouchFeature failed!", e);
            }
        }
        return mTouchFeature;
    }

    public static void setTouchFeature(TfParams params) {
        final ITouchFeature touchfeature = getTouchFeature();
        if (touchfeature == null) {
            Log.e(TAG, "setTouchFeatureParams: touchfeature is null!");
            return;
        }
        Log.d(TAG, "setTouchFeatureParams: " + params);
        try {
            if (params.valueArray != null) {
                setModeLongValue(touchfeature.asBinder(), params.mode, params.valueArray);
            } else {
                touchfeature.setTouchMode(0, params.mode, params.value);
            }
        } catch (Exception e) {
            Log.e(TAG, "setTouchFeatureParams failed!", e);
        }
    }

    // Stock transaction 8 takes the length before the array and returns an integer status.
    private static boolean setModeLongValue(IBinder binder, int mode, int[] values) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(ITouchFeature.DESCRIPTOR);
            data.writeInt(0);
            data.writeInt(mode);
            data.writeInt(values.length);
            data.writeIntArray(values);

            if (!binder.transact(TRANSACTION_SET_MODE_LONG_VALUE, data, reply, 0)) {
                Log.e(TAG, "setModeLongValue transact failed");
                return false;
            }

            reply.readException();
            int ret = reply.readInt();
            if (ret != 0) {
                Log.w(TAG, "setModeLongValue returned " + ret);
                return false;
            }
            return true;
        } catch (RemoteException | RuntimeException e) {
            mTouchFeature = null;
            Log.e(TAG, "Failed to apply edge suppression", e);
            return false;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public static class TfParams {
        /* touchfeature parameters */
        final int mode, value;
        final int[] valueArray;

        // Constructor for single int value
        public TfParams(int mode, int value) {
            this.mode = mode;
            this.value = value;
            this.valueArray = null;
        }

        // Constructor for int array (int[])
        public TfParams(int mode, int[] valueArray) {
            this.mode = mode;
            this.value = 0;
            this.valueArray = valueArray;
        }

        @Override
        public String toString() {
            if (valueArray != null) {
                return "TouchFeatureParams(" + mode + ", " + arrayToString(valueArray) + ")";
            } else {
                return "TouchFeatureParams(" + mode + ", " + value + ")";
            }
        }

        // Helper method to convert int[] to String for logging purposes
        private String arrayToString(int[] array) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < array.length; i++) {
                sb.append(array[i]);
                if (i < array.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
    }
}

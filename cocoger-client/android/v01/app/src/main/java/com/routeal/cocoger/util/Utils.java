package com.routeal.cocoger.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.provider.Settings;

import com.routeal.cocoger.MainApplication;
import com.routeal.cocoger.R;
import com.routeal.cocoger.model.Device;

/**
 * Created by nabe on 7/3/17.
 */

public class Utils {

    public static ProgressDialog spinBusyCursor(Activity activity) {
        ProgressDialog dialog = ProgressDialog.show(activity, null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progressbar_spinner);
        return dialog;
    }

    public static String getDeviceUniqueID() {
        return Settings.Secure.getString(MainApplication.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static final boolean isEmulator() {
        int rating = 0;

        if ((Build.PRODUCT.equals("sdk")) || (Build.PRODUCT.equals("google_sdk"))
                || (Build.PRODUCT.equals("sdk_x86")) || (Build.PRODUCT.equals("vbox86p"))) {
            rating++;
        }
        if ((Build.MANUFACTURER.equals("unknown")) || (Build.MANUFACTURER.equals("Genymotion"))) {
            rating++;
        }
        if ((Build.BRAND.equals("generic")) || (Build.BRAND.equals("generic_x86"))) {
            rating++;
        }
        if ((Build.DEVICE.equals("generic")) || (Build.DEVICE.equals("generic_x86")) || (Build.DEVICE.equals("vbox86p"))) {
            rating++;
        }
        if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"))
                || (Build.MODEL.equals("Android SDK built for x86"))) {
            rating++;
        }
        if ((Build.HARDWARE.equals("goldfish")) || (Build.HARDWARE.equals("vbox86"))) {
            rating++;
        }
        if ((Build.FINGERPRINT.contains("generic/sdk/generic"))
                || (Build.FINGERPRINT.contains("generic_x86/sdk_x86/generic_x86"))
                || (Build.FINGERPRINT.contains("generic/google_sdk/generic"))
                || (Build.FINGERPRINT.contains("generic/vbox86p/vbox86p"))) {
            rating++;
        }

        return rating > 4;
    }

    public static int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    // For now, the device is saved into the memory
    public static Device getDevice() {
        Device mDevice = new Device();
        mDevice.setDeviceId(Utils.getDeviceUniqueID());
        mDevice.setBrand(Build.BRAND);
        mDevice.setModel(Build.MODEL);
        mDevice.setPlatformVersion(Build.VERSION.RELEASE);
        mDevice.setSimulator(Utils.isEmulator());
        mDevice.setToken(""); // empty for now
        mDevice.setStatus(Device.FOREGROUND);
        mDevice.setAppVersion(MainApplication.getApplicationVersion());
        return mDevice;
    }
}

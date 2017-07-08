package com.routeal.cocoger.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.routeal.cocoger.R;

/**
 * Created by nabe on 7/3/17.
 */

public class Utils {
    public static ProgressDialog spinBusyCurosr(Activity activity) {
        ProgressDialog dialog = ProgressDialog.show(activity, null, null, false, true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.progress_bar);
        return dialog;
    }
}

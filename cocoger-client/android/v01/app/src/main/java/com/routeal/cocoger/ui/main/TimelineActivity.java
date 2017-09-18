package com.routeal.cocoger.ui.main;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.routeal.cocoger.R;

/**
 * Created by nabe on 9/16/17.
 */

public class TimelineActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle(R.string.sharing_timeline);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //selectDateTime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save:
                return true;
            case R.id.action_date_change:
                selectDateTime();
                return true;
            default:
                return false;
        }
    }

    private void selectDateTime() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(TimelineActivity.this);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_datetime, null);
        new AlertDialog.Builder(TimelineActivity.this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}

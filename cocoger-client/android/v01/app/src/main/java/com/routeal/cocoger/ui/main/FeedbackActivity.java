package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;

import com.routeal.cocoger.R;

/**
 * Created by nabe on 7/25/17.
 */

public class FeedbackActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    RadioGroup radioGroup;
    RadioButton problemButton;
    RadioButton suggestionButton;
    RadioButton feedbackButton;
    TextInputEditText editText;
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(true);
            ab.setTitle(R.string.feedback);
        }

        radioGroup = (RadioGroup) findViewById(R.id.input_button_group);
        problemButton = (RadioButton) findViewById(R.id.input_button_problem);
        suggestionButton = (RadioButton) findViewById(R.id.input_button_suggestion);
        feedbackButton = (RadioButton) findViewById(R.id.input_button_feedback);
        editText = (TextInputEditText) findViewById(R.id.feedback_description);
        ratingBar = (RatingBar) findViewById(R.id.feedback_rating);
        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send:
                sendFeedback();
                return true;
            default:
                return false;
        }
    }

    private void sendFeedback() {
        RadioButton button = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());
        String about = button.getText().toString();
        String description = editText.getText().toString();
        int numStars = ratingBar.getNumStars();
        finish();
    }
}

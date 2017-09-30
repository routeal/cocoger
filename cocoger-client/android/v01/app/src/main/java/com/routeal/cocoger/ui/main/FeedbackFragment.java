package com.routeal.cocoger.ui.main;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;

import com.franmontiel.fullscreendialog.FullScreenDialogContent;
import com.franmontiel.fullscreendialog.FullScreenDialogController;
import com.routeal.cocoger.R;

/**
 * Created by hwatanabe on 9/28/17.
 */

public class FeedbackFragment extends Fragment implements FullScreenDialogContent,
        RadioGroup.OnCheckedChangeListener {

    RadioGroup radioGroup;
    RadioButton problemButton;
    RadioButton suggestionButton;
    RadioButton feedbackButton;
    TextInputEditText editText;
    RatingBar ratingBar;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_feedback, container, false);
        radioGroup = (RadioGroup) view.findViewById(R.id.input_button_group);
        problemButton = (RadioButton) view.findViewById(R.id.input_button_problem);
        suggestionButton = (RadioButton) view.findViewById(R.id.input_button_suggestion);
        feedbackButton = (RadioButton) view.findViewById(R.id.input_button_feedback);
        editText = (TextInputEditText) view.findViewById(R.id.feedback_description);
        ratingBar = (RatingBar) view.findViewById(R.id.feedback_rating);
        radioGroup.setOnCheckedChangeListener(this);
        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        RadioButton button = (RadioButton) view.findViewById(checkedId);
        if (button == problemButton) {
            suggestionButton.setChecked(false);
            feedbackButton.setChecked(false);
        } else if (button == suggestionButton) {
            feedbackButton.setChecked(false);
            problemButton.setChecked(false);
        } else if (button == feedbackButton) {
            suggestionButton.setChecked(false);
            problemButton.setChecked(false);
        }
    }

    @Override
    public void onDialogCreated(FullScreenDialogController dialogController) {
    }

    @Override
    public boolean onConfirmClick(FullScreenDialogController dialogController) {
        RadioButton button = (RadioButton) view.findViewById(radioGroup.getCheckedRadioButtonId());
        String about = button.getText().toString();
        String description = editText.getText().toString();
        int numStars = ratingBar.getNumStars();
        return false;
    }

    @Override
    public boolean onDiscardClick(FullScreenDialogController dialogController) {
        return false;
    }
}

package com.sreemenon.opensesame;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Sree on 14/12/2015.
 *
 * Template Logic for Custom Progress Dialog
 */
public class CustomProgressDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.progress_dialog_layout, container);

        TextView tvDialogText = (TextView)view.findViewById(R.id.tvDialogText);
        Bundle bundle = getArguments();
        String message = bundle.getString("message");
        tvDialogText.setText(message);

        ImageView imgSpinner = (ImageView)view.findViewById(R.id.imgSpinner);

        String currAction = message.split(" ")[0];

        RotateAnimation rotateAnimation;
        switch (currAction){
            case "Peeking":
            case "Opening":
                rotateAnimation = new RotateAnimation(0, -360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(600);
                rotateAnimation.setRepeatCount(100);
                imgSpinner.startAnimation(rotateAnimation);
                break;
            case "Locking":
                rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(600);
                rotateAnimation.setRepeatCount(100);
                imgSpinner.startAnimation(rotateAnimation);
                break;
        }
        //pbarDialog.setIndeterminateDrawable();

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}

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
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Admin on 14/12/2015.
 */
public class PasswordDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password, container);

        TextView tvPass = (TextView)view.findViewById(R.id.tvPassword);
        Bundle bundle = getArguments();
        tvPass.setText(bundle.getString("pass"));

        Button btnClose = (Button)view.findViewById(R.id.btnPassDialogClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordDialogFragment.this.dismiss();
            }
        });

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

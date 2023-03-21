package com.asdzheng.openchat.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.asdzheng.openchat.R;
import com.asdzheng.openchat.util.PreferencesManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import cn.iwgang.simplifyspan.SimplifySpanBuild;
import cn.iwgang.simplifyspan.unit.SpecialClickableUnit;
import cn.iwgang.simplifyspan.unit.SpecialTextUnit;

/**
 * @author zhengjb
 * @date on 2023/3/21
 */
public class SetupKeyDialog extends DialogFragment {

        private EditText mEditText;
        private Button mOkButton;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Inflate the dialog layout
            View view = getLayoutInflater().inflate(R.layout.dialog_init, null);

            // Get references to the views in the dialog
            TextView introTextView = view.findViewById(R.id.tv_key_step_introduction);
            mEditText = view.findViewById(R.id.et_input);
            mOkButton = view.findViewById(R.id.dialog_ok);

            // Set the text for the views
            introTextView.setText(new SimplifySpanBuild(getString(R.string.step_one))
                    .append(new SpecialTextUnit(getString(R.string.api_keys)).setClickableUnit(new SpecialClickableUnit(introTextView,
                            (tv, clickableSpan) -> startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse("https://platform.openai.com/account/api-keys"))))).setTextColor(Color.BLUE))
                    .append("\n\n")
                    .append(getString(R.string.step_two))
                    .append("\n\n")
                    .append(getString(R.string.step_three))
                    .build()

            );

            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mOkButton.setEnabled(s != null && !s.toString().isEmpty());
                }
            });


            // Set up the OK button click listener
            mOkButton.setOnClickListener(v -> {
                PreferencesManager.setOpenAIAPIKey(mEditText.getText().toString());
                dismiss();
            });

            // Create the dialog with the custom layout
            Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(view);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);

            return dialog;
        }
}

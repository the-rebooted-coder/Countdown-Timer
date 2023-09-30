package com.onesilicondiode.anumi;

import static androidx.core.content.ContextCompat.startActivity;
import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PasswordBottomSheetDialog extends BottomSheetDialogFragment {

    private TextInputEditText passwordEditText;
    private TextInputLayout passwordHolder;
    private Button submitButton;
    private static final String LOCK_UNLOCK = "appLocked";
    private static final String IS_UNLOCKED_KEY = "isUnlocked";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_dialog, container, false);

        passwordEditText = view.findViewById(R.id.passwordEditText);
        submitButton = view.findViewById(R.id.submitButton);
        passwordHolder = view.findViewById(R.id.pinTextInputLayout);
        submitButton.setOnClickListener(v -> {
            String enteredPassword = passwordEditText.getText().toString();

            if (enteredPassword.equals("1709")) {
                SharedPreferences sharedPreferences = getContext().getSharedPreferences(LOCK_UNLOCK, Context.MODE_PRIVATE);
                sharedPreferences.edit()
                        .putBoolean(IS_UNLOCKED_KEY, true)
                        .apply();
                Intent intent = new Intent(getContext(), LockApp.class);
                startActivity(intent);
                dismiss();
                getActivity().finish();
            } else {
                shakeTextInputLayout();
                passwordEditText.setError("Incorrect Password");
            }
        });

        return view;
    }

    private void shakeTextInputLayout() {
        Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
        passwordHolder.startAnimation(shake);
    }
}
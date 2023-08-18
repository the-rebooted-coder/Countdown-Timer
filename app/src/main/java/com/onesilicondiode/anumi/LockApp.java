package com.onesilicondiode.anumi;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class LockApp extends AppCompatActivity {
    private static final String CORRECT_PIN = "2908";
    private static final String KEY_NAME = "my_key_name";
    public static final String APP_LOCK = "lockedApp";
    public static final String APP_IS_UNLOCKED = "appUnlocked";
    private Vibrator vibrator;
    private FingerprintManagerCompat fingerprintManager;
    private MaterialButton usePinButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_app);
        setStatusBarColor(getResources().getColor(R.color.orange));
        fingerprintManager = FingerprintManagerCompat.from(this);
        boolean isUnlocked = getSharedPreferences(APP_LOCK, MODE_PRIVATE)
                .getBoolean(APP_IS_UNLOCKED, false);
        if (isUnlocked) {
            navigateToNextScreen();
        }
        else{
            if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
                authenticateWithFingerprint();
            } else {
                // Fingerprint authentication not available, use PIN as a fallback
                authenticateWithPIN(CORRECT_PIN);
            }
            usePinButton = findViewById(R.id.btnUsePin);

            usePinButton.setOnClickListener(v -> showPinInputDialog());
        }
    }
    private void showPinInputDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_pin_input, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Enter PIN")
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputEditText pinEditText = dialogView.findViewById(R.id.appUnlockPin);
                        String enteredPin = pinEditText.getText().toString();
                        if (enteredPin.equals("2908")) {
                            navigateToMainActivity();
                        } else {
                            long[] pattern = {0, 100, 100, 100, 200, 100};
                            if (vibrator != null && vibrator.hasVibrator()) {
                                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                            }
                            shakeDialogAndShowError();
                        }
                    }
                })
                .setNegativeButton("CANCEL", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void shakeDialogAndShowError() {
        Toast.makeText(this,"Uh-huh Wrong PIN 🤦‍♂️", Toast.LENGTH_SHORT).show();
    }
    private void authenticateWithFingerprint() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            keyGenerator.init(builder.build());
            SecretKey secretKey = keyGenerator.generateKey();

            Cipher cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            FingerprintManagerCompat.CryptoObject cryptoObject =
                    new FingerprintManagerCompat.CryptoObject(cipher);

            fingerprintManager.authenticate(cryptoObject, 0, null,
                    new FingerprintManagerCompat.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(
                                @NonNull FingerprintManagerCompat.AuthenticationResult result) {
                            // Fingerprint authentication successful, navigate to the next screen
                            navigateToNextScreen();
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                            // Handle authentication errors
                            Toast.makeText(LockApp.this, "Fingerprint authentication failed: " + errString, Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
        }
    }
    private void authenticateWithPIN(String enteredPIN) {
        if (enteredPIN.equals(CORRECT_PIN)) {
            // PIN authentication successful, navigate to the next screen
            navigateToNextScreen();
        } else {
            // Incorrect PIN
            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToNextScreen() {
        getSharedPreferences(APP_LOCK, MODE_PRIVATE).edit()
                .putBoolean(APP_IS_UNLOCKED, true)
                .apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void setStatusBarColor(int color) {
        getWindow().setStatusBarColor(color);
    }
}
package com.onesilicondiode.anumi;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class LockApp extends AppCompatActivity {
    private static final String CORRECT_PIN = "2908";
    private static final String KEY_NAME = "my_key_name";
    public static final String APP_LOCK = "lockedApp";
    public static final String APP_IS_UNLOCKED = "appUnlocked";
    private FingerprintManagerCompat fingerprintManager;

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
        }
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
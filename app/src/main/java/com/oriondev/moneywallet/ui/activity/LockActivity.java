/*
 * Copyright (c) 2018.
 *
 * This file is part of MoneyWallet.
 *
 * MoneyWallet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MoneyWallet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MoneyWallet.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.oriondev.moneywallet.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.LockMode;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.activity.base.ThemedActivity;

import java.util.List;

/**
 * Created by andrea on 24/07/18.
 */
public class LockActivity extends ThemedActivity {

    public static final String MODE = "LockActivity::Arguments::Mode";
    public static final String ACTION = "LockActivity::Arguments::Action";

    private static final String SS_NEW_CODE = "LockActivity::SavedState::NewCode";
    private static final String SS_CURRENT_STEP = "LockActivity::SavedState::CurrentStep";
    private static final String SS_CURRENT_LOCK_MODE = "LockActivity::SavedState::CurrentLockMode";

    /**
     * Default action if not specified, it will prompt the user to insert the key
     * and return an intent with RESULT_OK if the user entered the correct secret.
     */
    public static final int ACTION_UNLOCK = 0;

    /**
     * This action will prompt the user to insert the current key to verify his
     * identity, then if the correct secret has been provided, the lock mode will
     * be set to DISABLED state and an intent with RESULT_OK will be returned.
     */
    public static final int ACTION_DISABLE = 1;

    /**
     * This action will prompt the user to create a key using the specified MODE
     * and will verify it before setting the current lock mode to the specified
     * mode. An intent with RESULT_OK will be returned if the operation have
     * success.
     */
    public static final int ACTION_ENABLE = 2;

    /**
     * This action will prompt the user to insert the current key to verify his
     * identity, then if the correct secret has been provided, it will be prompted
     * to create (and verify) a new key keeping the same lock mode. An intent with
     * RESULT_OK will be returned if the operation have success.
     */
    public static final int ACTION_CHANGE_KEY = 3;

    /**
     * This action will prompt the user to insert the current key to verify his
     * identity, then if the correct secret has been provided, it will be prompted
     * to create (and verify) a new key using the provided MODE. An intent with
     * RESULT_OK will be returned if the operation have success.
     */
    public static final int ACTION_CHANGE_MODE = 4;

    private static final int PIN_CODE_LENGTH = 5;

    private static final int STEP_INSERT_OLD_CODE = 1;
    private static final int STEP_INSERT_NEW_CODE = 2;
    private static final int STEP_VERIFY_NEW_CODE = 3;

    public static Intent unlock(Activity activity) {
        Intent intent = new Intent(activity, LockActivity.class);
        intent.putExtra(ACTION, ACTION_UNLOCK);
        return intent;
    }

    public static Intent enableLock(Activity activity, LockMode lockMode) {
        Intent intent = new Intent(activity, LockActivity.class);
        intent.putExtra(MODE, lockMode);
        intent.putExtra(ACTION, ACTION_ENABLE);
        return intent;
    }

    public static Intent disableLock(Activity activity) {
        Intent intent = new Intent(activity, LockActivity.class);
        intent.putExtra(ACTION, ACTION_DISABLE);
        return intent;
    }

    public static Intent changeKey(Activity activity) {
        Intent intent = new Intent(activity, LockActivity.class);
        intent.putExtra(ACTION, ACTION_CHANGE_KEY);
        return intent;
    }

    public static Intent changeMode(Activity activity, LockMode lockMode) {
        Intent intent = new Intent(activity, LockActivity.class);
        intent.putExtra(MODE, lockMode);
        intent.putExtra(ACTION, ACTION_CHANGE_MODE);
        return intent;
    }

    private int mAction;
    private LockMode mTargetLockMode;

    private ViewGroup mPinLayout, mSequenceLayout, mFingerprintLayout;

    private TextView mPinHelpTextView;
    private PinLockView mPinLockView;

    private TextView mSequenceHelpTextView;
    private PatternLockView mPatternLockView;

    private TextView mFingerprintHelpTextView;

    private String mNewCode = null;
    private int mCurrentStep = 0;
    private LockMode mCurrentLockMode;
    private FingerPrintAuthHelper mFingerprintAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unpackIntent(getIntent(), savedInstanceState);
        initializeUi(savedInstanceState);
        mFingerprintAuth = FingerPrintAuthHelper.getHelper(this, new FingerPrintAuthCallback() {

            @Override
            public void onNoFingerPrintHardwareFound() {
                // not handled because the fingerprint availability is checked
                // before starting this activity.
            }

            @Override
            public void onNoFingerPrintRegistered() {
                mFingerprintHelpTextView.setText(R.string.help_fingerprint_not_initialized);
            }

            @Override
            public void onBelowMarshmallow() {
                // not handled because the fingerprint availability is checked
                // before starting this activity.
            }

            @Override
            public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
                onFingerprintScan(true, 0, null);
            }

            @Override
            public void onAuthFailed(int errorCode, String errorMessage) {
                onFingerprintScan(false, errorCode, errorMessage);
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFingerprintAuth.startAuth();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SS_NEW_CODE, mNewCode);
        outState.putInt(SS_CURRENT_STEP, mCurrentStep);
        outState.putSerializable(SS_CURRENT_LOCK_MODE, mCurrentLockMode);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mFingerprintAuth.stopAuth();
        }
    }

    private void unpackIntent(Intent intent, Bundle savedInstanceState) {
        mAction = intent.getIntExtra(ACTION, ACTION_UNLOCK);
        mTargetLockMode = (LockMode) intent.getSerializableExtra(MODE);
        if (mTargetLockMode == null) {
            mTargetLockMode = LockMode.NONE;
        }
        if (savedInstanceState != null) {
            mNewCode = savedInstanceState.getString(SS_NEW_CODE);
            mCurrentStep = savedInstanceState.getInt(SS_CURRENT_STEP);
            mCurrentLockMode = (LockMode) savedInstanceState.getSerializable(SS_CURRENT_LOCK_MODE);
        } else {
            switch (mAction) {
                case ACTION_UNLOCK:
                case ACTION_DISABLE:
                case ACTION_CHANGE_KEY:
                case ACTION_CHANGE_MODE:
                    mCurrentLockMode = PreferenceManager.getCurrentLockMode();
                    break;
                case ACTION_ENABLE:
                    mCurrentLockMode = mTargetLockMode;
                    break;
            }
        }
    }

    private void initializeUi(Bundle savedInstanceState) {
        setContentView(R.layout.activity_lock);
        mPinLayout = findViewById(R.id.pin_layout);
        mPinHelpTextView = findViewById(R.id.pin_help_text_view);
        IndicatorDots indicatorDotsView = findViewById(R.id.indicator_dots);
        mPinLockView = findViewById(R.id.pin_lock_view);
        mSequenceLayout = findViewById(R.id.sequence_layout);
        mSequenceHelpTextView = findViewById(R.id.sequence_help_text_view);
        mPatternLockView = findViewById(R.id.pattern_lock_view);
        mFingerprintLayout = findViewById(R.id.fingerprint_layout);
        mFingerprintHelpTextView = findViewById(R.id.fingerprint_help_text_view);
        // enable ui
        indicatorDotsView.setPinLength(PIN_CODE_LENGTH);
        mPinLockView.setPinLength(PIN_CODE_LENGTH);
        mPinLockView.attachIndicatorDots(indicatorDotsView);
        mPinLockView.setPinLockListener(new PinLockListener() {

            @Override
            public void onComplete(String pin) {
                onPinCodeComplete(pin);
            }

            @Override
            public void onEmpty() {
                // not used
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
                // not used
            }

        });
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {

            @Override
            public void onStarted() {
                // not used
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
                // not used
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                onPatternComplete(PatternLockUtils.patternToString(mPatternLockView, pattern));
            }

            @Override
            public void onCleared() {
                // not used
            }

        });
        // setup the views
        if (savedInstanceState == null) {
            if (mCurrentLockMode == LockMode.PIN) {
                switch (mAction) {
                    case ACTION_UNLOCK:
                    case ACTION_DISABLE:
                        mPinHelpTextView.setText(R.string.help_insert_pin_code);
                        break;
                    case ACTION_ENABLE:
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mPinHelpTextView.setText(R.string.help_create_new_pin_code);
                        break;
                    case ACTION_CHANGE_KEY:
                    case ACTION_CHANGE_MODE:
                        mCurrentStep = STEP_INSERT_OLD_CODE;
                        mPinHelpTextView.setText(R.string.help_insert_old_pin_code);
                        break;
                }
            } else if (mCurrentLockMode == LockMode.SEQUENCE) {
                switch (mAction) {
                    case ACTION_UNLOCK:
                    case ACTION_DISABLE:
                        mSequenceHelpTextView.setText(R.string.help_insert_sequence);
                        break;
                    case ACTION_ENABLE:
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mSequenceHelpTextView.setText(R.string.help_create_new_sequence);
                        break;
                    case ACTION_CHANGE_KEY:
                    case ACTION_CHANGE_MODE:
                        mCurrentStep = STEP_INSERT_OLD_CODE;
                        mSequenceHelpTextView.setText(R.string.help_insert_old_sequence);
                        break;
                }
            } else if (mCurrentLockMode == LockMode.FINGERPRINT) {
                switch (mAction) {
                    case ACTION_UNLOCK:
                    case ACTION_DISABLE:
                        mFingerprintHelpTextView.setText(R.string.help_insert_fingerprint);
                        break;
                    case ACTION_ENABLE:
                    case ACTION_CHANGE_KEY:
                    case ACTION_CHANGE_MODE:
                        mCurrentStep = STEP_INSERT_OLD_CODE;
                        mFingerprintHelpTextView.setText(R.string.help_create_fingerprint);
                        break;
                }
            }
        }
        showLayout(mCurrentLockMode);
    }

    private void showLayout(LockMode lockMode) {
        mPinLayout.setVisibility(lockMode == LockMode.PIN ? View.VISIBLE : View.GONE);
        mSequenceLayout.setVisibility(lockMode == LockMode.SEQUENCE ? View.VISIBLE : View.GONE);
        mFingerprintLayout.setVisibility(lockMode == LockMode.FINGERPRINT ? View.VISIBLE : View.GONE);
    }

    private void onPinCodeComplete(String code) {
        switch (mAction) {
            case ACTION_UNLOCK:
                if (TextUtils.equals(code, PreferenceManager.getCurrentLockCode())) {
                    PreferenceManager.setLastLockTime(System.currentTimeMillis());
                    setResult(RESULT_OK);
                    finish();
                } else {
                    mPinLockView.resetPinLockView();
                    mPinHelpTextView.setText(R.string.help_insert_pin_code_failed);
                }
                break;
            case ACTION_DISABLE:
                if (TextUtils.equals(code, PreferenceManager.getCurrentLockCode())) {
                    PreferenceManager.setCurrentLockMode(LockMode.NONE);
                    PreferenceManager.setLastLockTime(System.currentTimeMillis());
                    PreferenceManager.setCurrentLockCode(null);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    mPinLockView.resetPinLockView();
                    mPinHelpTextView.setText(R.string.help_insert_pin_code_failed);
                }
                break;
            case ACTION_ENABLE:
                if (mCurrentStep == STEP_INSERT_NEW_CODE) {
                    mNewCode = code;
                    mCurrentStep = STEP_VERIFY_NEW_CODE;
                    mPinLockView.resetPinLockView();
                    mPinHelpTextView.setText(R.string.help_verify_created_pin_code);
                } else if (mCurrentStep == STEP_VERIFY_NEW_CODE) {
                    if (TextUtils.equals(mNewCode, code)) {
                        PreferenceManager.setCurrentLockMode(LockMode.PIN);
                        PreferenceManager.setCurrentLockCode(code);
                        PreferenceManager.setLastLockTime(System.currentTimeMillis());
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        // the user has inserted two different codes, simply alert him and restart
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mPinLockView.resetPinLockView();
                        mPinHelpTextView.setText(R.string.help_verify_created_pin_code_failed);
                    }
                }
                break;
            case ACTION_CHANGE_KEY:
                if (mCurrentStep == STEP_INSERT_OLD_CODE) {
                    if (TextUtils.equals(code, PreferenceManager.getCurrentLockCode())) {
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mPinLockView.resetPinLockView();
                        mPinHelpTextView.setText(R.string.help_create_new_pin_code);
                    } else {
                        mPinLockView.resetPinLockView();
                        mPinHelpTextView.setText(R.string.help_insert_pin_code_failed);
                    }
                } else if (mCurrentStep == STEP_INSERT_NEW_CODE) {
                    mNewCode = code;
                    mCurrentStep = STEP_VERIFY_NEW_CODE;
                    mPinLockView.resetPinLockView();
                    mPinHelpTextView.setText(R.string.help_verify_created_pin_code);
                } else if (mCurrentStep == STEP_VERIFY_NEW_CODE) {
                    if (TextUtils.equals(mNewCode, code)) {
                        PreferenceManager.setCurrentLockMode(LockMode.PIN);
                        PreferenceManager.setCurrentLockCode(code);
                        PreferenceManager.setLastLockTime(System.currentTimeMillis());
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        // the user has inserted two different codes, simply alert him and restart
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mPinLockView.resetPinLockView();
                        mPinHelpTextView.setText(R.string.help_verify_created_pin_code_failed);
                    }
                }
                break;
            case ACTION_CHANGE_MODE:
                if (mCurrentStep == STEP_INSERT_OLD_CODE) {
                    if (TextUtils.equals(code, PreferenceManager.getCurrentLockCode())) {
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mCurrentLockMode = mTargetLockMode;
                        if (mCurrentLockMode == LockMode.SEQUENCE) {
                            mSequenceHelpTextView.setText(R.string.help_create_new_sequence);
                        } else if (mCurrentLockMode == LockMode.FINGERPRINT) {
                            mFingerprintHelpTextView.setText(R.string.help_create_fingerprint);
                        }
                        showLayout(mCurrentLockMode);
                    } else {
                        mPinHelpTextView.setText(R.string.help_insert_pin_code_failed);
                    }
                    mPinLockView.resetPinLockView();
                } else if (mCurrentStep == STEP_INSERT_NEW_CODE) {
                    mNewCode = code;
                    mCurrentStep = STEP_VERIFY_NEW_CODE;
                    mPinLockView.resetPinLockView();
                    mPinHelpTextView.setText(R.string.help_verify_created_pin_code);
                } else if (mCurrentStep == STEP_VERIFY_NEW_CODE) {
                    if (TextUtils.equals(mNewCode, code)) {
                        PreferenceManager.setCurrentLockMode(LockMode.PIN);
                        PreferenceManager.setCurrentLockCode(code);
                        PreferenceManager.setLastLockTime(System.currentTimeMillis());
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        // the user has inserted two different codes, simply alert him and restart
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mPinLockView.resetPinLockView();
                        mPinHelpTextView.setText(R.string.help_verify_created_pin_code_failed);
                    }
                }
                break;
        }
    }

    private void onPatternComplete(String pattern) {
        switch (mAction) {
            case ACTION_UNLOCK:
                if (TextUtils.equals(pattern, PreferenceManager.getCurrentLockCode())) {
                    PreferenceManager.setLastLockTime(System.currentTimeMillis());
                    setResult(RESULT_OK);
                    finish();
                } else {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    mSequenceHelpTextView.setText(R.string.help_insert_sequence_failed);
                }
                break;
            case ACTION_DISABLE:
                if (TextUtils.equals(pattern, PreferenceManager.getCurrentLockCode())) {
                    PreferenceManager.setCurrentLockMode(LockMode.NONE);
                    PreferenceManager.setLastLockTime(System.currentTimeMillis());
                    PreferenceManager.setCurrentLockCode(null);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    mSequenceHelpTextView.setText(R.string.help_insert_sequence_failed);
                }
                break;
            case ACTION_ENABLE:
                if (mCurrentStep == STEP_INSERT_NEW_CODE) {
                    mNewCode = pattern;
                    mCurrentStep = STEP_VERIFY_NEW_CODE;
                    mPatternLockView.clearPattern();
                    mSequenceHelpTextView.setText(R.string.help_verify_created_sequence);
                } else if (mCurrentStep == STEP_VERIFY_NEW_CODE) {
                    if (TextUtils.equals(mNewCode, pattern)) {
                        PreferenceManager.setCurrentLockMode(LockMode.SEQUENCE);
                        PreferenceManager.setCurrentLockCode(pattern);
                        PreferenceManager.setLastLockTime(System.currentTimeMillis());
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        // the user has inserted two different codes, simply alert him and restart
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        mSequenceHelpTextView.setText(R.string.help_verify_created_sequence_failed);
                    }
                }
                break;
            case ACTION_CHANGE_KEY:
                if (mCurrentStep == STEP_INSERT_OLD_CODE) {
                    if (TextUtils.equals(pattern, PreferenceManager.getCurrentLockCode())) {
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mPatternLockView.clearPattern();
                        mSequenceHelpTextView.setText(R.string.help_create_new_sequence);
                    } else {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        mSequenceHelpTextView.setText(R.string.help_insert_sequence_failed);
                    }
                } else if (mCurrentStep == STEP_INSERT_NEW_CODE) {
                    mNewCode = pattern;
                    mCurrentStep = STEP_VERIFY_NEW_CODE;
                    mPatternLockView.clearPattern();
                    mSequenceHelpTextView.setText(R.string.help_verify_created_sequence);
                } else if (mCurrentStep == STEP_VERIFY_NEW_CODE) {
                    if (TextUtils.equals(mNewCode, pattern)) {
                        PreferenceManager.setCurrentLockMode(LockMode.SEQUENCE);
                        PreferenceManager.setCurrentLockCode(pattern);
                        PreferenceManager.setLastLockTime(System.currentTimeMillis());
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        // the user has inserted two different codes, simply alert him and restart
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        mSequenceHelpTextView.setText(R.string.help_verify_created_sequence_failed);
                    }
                }
                break;
            case ACTION_CHANGE_MODE:
                if (mCurrentStep == STEP_INSERT_OLD_CODE) {
                    if (TextUtils.equals(pattern, PreferenceManager.getCurrentLockCode())) {
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mCurrentLockMode = mTargetLockMode;
                        if (mCurrentLockMode == LockMode.PIN) {
                            mPinHelpTextView.setText(R.string.help_create_new_pin_code);
                        } else if (mCurrentLockMode == LockMode.FINGERPRINT) {
                            mFingerprintHelpTextView.setText(R.string.help_create_fingerprint);
                        }
                        mPatternLockView.clearPattern();
                        showLayout(mCurrentLockMode);
                    } else {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        mSequenceHelpTextView.setText(R.string.help_insert_pin_code_failed);
                    }
                } else if (mCurrentStep == STEP_INSERT_NEW_CODE) {
                    mNewCode = pattern;
                    mCurrentStep = STEP_VERIFY_NEW_CODE;
                    mPatternLockView.clearPattern();
                    mSequenceHelpTextView.setText(R.string.help_verify_created_sequence);
                } else if (mCurrentStep == STEP_VERIFY_NEW_CODE) {
                    if (TextUtils.equals(mNewCode, pattern)) {
                        PreferenceManager.setCurrentLockMode(LockMode.SEQUENCE);
                        PreferenceManager.setCurrentLockCode(pattern);
                        PreferenceManager.setLastLockTime(System.currentTimeMillis());
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        // the user has inserted two different codes, simply alert him and restart
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        mSequenceHelpTextView.setText(R.string.help_verify_created_sequence_failed);
                    }
                }
                break;
        }
    }

    private void onFingerprintScan(boolean recognized, int errorType, CharSequence message) {
        switch (mAction) {
            case ACTION_UNLOCK:
                if (recognized) {
                    PreferenceManager.setLastLockTime(System.currentTimeMillis());
                    setResult(RESULT_OK);
                    finish();
                } else {
                    mFingerprintHelpTextView.setText(R.string.help_insert_fingerprint_failed);
                }
                break;
            case ACTION_DISABLE:
                if (recognized) {
                    PreferenceManager.setCurrentLockMode(LockMode.NONE);
                    PreferenceManager.setLastLockTime(System.currentTimeMillis());
                    PreferenceManager.setCurrentLockCode(null);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    mFingerprintHelpTextView.setText(R.string.help_insert_fingerprint_failed);
                }
                break;
            case ACTION_ENABLE:
                if (recognized) {
                    PreferenceManager.setCurrentLockMode(LockMode.FINGERPRINT);
                    PreferenceManager.setLastLockTime(System.currentTimeMillis());
                    setResult(RESULT_OK);
                    finish();
                } else {
                    mFingerprintHelpTextView.setText(R.string.help_insert_fingerprint_failed);
                }
                break;
            case ACTION_CHANGE_KEY:
                // not supported, use android settings to change or add another fingerprint
                break;
            case ACTION_CHANGE_MODE:
                if (mCurrentStep == STEP_INSERT_OLD_CODE) {
                    if (recognized) {
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mCurrentLockMode = mTargetLockMode;
                        if (mCurrentLockMode == LockMode.PIN) {
                            mPinHelpTextView.setText(R.string.help_create_new_pin_code);
                        } else if (mCurrentLockMode == LockMode.SEQUENCE) {
                            mSequenceHelpTextView.setText(R.string.help_create_new_sequence);
                        }
                        showLayout(mCurrentLockMode);
                    } else {
                        mFingerprintHelpTextView.setText(R.string.help_insert_fingerprint_failed);
                    }
                } else if (mCurrentStep == STEP_INSERT_NEW_CODE) {
                    if (recognized) {
                        PreferenceManager.setCurrentLockMode(LockMode.FINGERPRINT);
                        PreferenceManager.setLastLockTime(System.currentTimeMillis());
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        // the user has inserted two different codes, simply alert him and restart
                        mCurrentStep = STEP_INSERT_NEW_CODE;
                        mFingerprintHelpTextView.setText(R.string.help_insert_fingerprint_failed);
                    }
                }
                break;
        }
    }
}

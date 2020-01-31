package com.liskovsoft.smartyoutubetv.misc.appstatewatcher;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv.BuildConfig;
import com.liskovsoft.smartyoutubetv.CommonApplication;
import com.liskovsoft.smartyoutubetv.prefs.SmartPreferences;
import com.liskovsoft.smartyoutubetv.receivers.DeviceWakeReceiver;

import java.util.ArrayList;

public class AppStateWatcherBase {
    private static final String TAG = AppStateWatcherBase.class.getSimpleName();
    private final ArrayList<StateHandler> mHandlers;
    private final Activity mContext;
    private DeviceWakeReceiver mReceiver;

    public AppStateWatcherBase(Activity context) {
        mContext = context;
        mHandlers = new ArrayList<>();
        
        registerReceiver();
    }

    private void unregisterReceiver() {
        if (mReceiver != null) {
            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Oops. Receiver not registered.");
                e.printStackTrace();
            }
        }
    }

    private void registerReceiver() {
        try {
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            mReceiver = new DeviceWakeReceiver(this);
            mContext.registerReceiver(mReceiver, intentFilter);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "Oops. Receiver already registered.");
            e.printStackTrace();
        }
    }

    public void run() {
        SmartPreferences prefs = CommonApplication.getPreferences();

        if (prefs == null) {
            return;
        }

        int code = prefs.getPreviousAppVersionCode();

        Log.d(TAG, "App just started... Calling handlers...");

        for (StateHandler handler : mHandlers) {
            handler.onInit();

            if (code != BuildConfig.VERSION_CODE) {
                prefs.setPreviousAppVersionCode(BuildConfig.VERSION_CODE);

                if (code != 0) {
                    // put cleanup logic here
                    // FileHelpers.deleteCache(mContext);
                    Log.d(TAG, "App has been updated... Clearing cache, doing backup...");

                    handler.onUpdate();
                } else {
                    // put restore logic here
                    Log.d(TAG, "App just installed... Restoring data...");

                    handler.onFirstRun();
                }
            }
        }
    }

    public void addHandler(StateHandler handler) {
        mHandlers.add(handler);
    }

    public void removeHandler(StateHandler handler) {
        mHandlers.remove(handler);
    }

    public void onLoad() {
        Log.d(TAG, "App has been loaded... Calling handlers...");

        for (StateHandler handler : mHandlers) {
            handler.onLoad();
        }
    }

    public void onNewIntent(Intent intent) {
        Log.d(TAG, "New intent received... Calling handlers...");

        for (StateHandler handler : mHandlers) {
            handler.onNewIntent(intent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (StateHandler handler : mHandlers) {
            handler.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onResume() {

    }

    public void onWake() {
        Log.d(TAG, "Device waking up... Calling handlers...");

        for (StateHandler handler : mHandlers) {
            handler.onWake();
        }
    }

    public void onExit() {
        unregisterReceiver();
    }

    public static abstract class StateHandler {
        public void onNewIntent(Intent intent) {
            
        }

        public void onUpdate() {
        }

        public void onFirstRun() {
        }

        public void onInit() {
        }

        public void onLoad() {
            
        }

        public void onWake() {

        }

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            
        }
    }
}

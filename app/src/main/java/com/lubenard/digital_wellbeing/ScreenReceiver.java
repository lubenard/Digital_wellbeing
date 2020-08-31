package com.lubenard.digital_wellbeing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Used to detect if screen is on or off
 * (We do not count the time if the screen is off)
 */
public class ScreenReceiver extends BroadcastReceiver {

    // That code is coming from https://thinkandroid.wordpress.com/2010/01/24/handling-screen-off-and-screen-on-intents/
    public static boolean wasScreenOn = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            wasScreenOn = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            wasScreenOn = true;
        }
    }
}

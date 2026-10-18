package com.turfmatch.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.appcompat.app.AlertDialog;

// CO6 - BroadcastReceiver for Offline Alerts and Connectivity Monitoring
public class NetworkReceiver extends BroadcastReceiver {
    private AlertDialog activeDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isOnline(context)) {
            showOfflineDialog(context);
        } else {
            dismissOfflineDialog();
        }
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
        return true;
    }

    private void showOfflineDialog(Context context) {
        if (activeDialog == null || !activeDialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Connection Lost");
            builder.setMessage("You're offline — live turf bookings and roster updates can't be processed right now.");
            builder.setPositiveButton("Dismiss", (dialog, which) -> dialog.dismiss());
            builder.setCancelable(false);
            try {
                activeDialog = builder.show();
            } catch (Exception e) {
                // Window token might not be available if called from background without UI context
            }
        }
    }

    private void dismissOfflineDialog() {
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
            activeDialog = null;
        }
    }
}

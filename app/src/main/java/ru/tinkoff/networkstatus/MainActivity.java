package ru.tinkoff.networkstatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ConnectivityManager";
    private ConnectivityManager connectivityManager;
    private TextView tvMobile;
    private TextView tvWifi;
    private TextView tvActiveNetwork;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMobile = findViewById(R.id.tvMobile);
        tvWifi = findViewById(R.id.tvWifi);
        tvActiveNetwork = findViewById(R.id.tvActiveNetwork);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        printLogNetworks();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
        registerReceiver(
                broadcastReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, String.format("Receive action:%s", intent.toString()));
            updateUI();
        }
    };

    private void updateUI() {
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        tvMobile.setText(String.format("Mobile\n%s", mobileInfo.getState()));
        tvWifi.setText(String.format("Wifi\n%s", wifiInfo.getState()));

        tvActiveNetwork.setText(
                String.format(
                        "Active network: %s",
                        activeNetworkInfo != null ? activeNetworkInfo.getTypeName() : "undefined"
                )
        );
    }

    private void printLogNetworks() {
        Log.d(TAG, "------ Networks -----");
        NetworkInfo[] networkInfoArray = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo networkInfo : networkInfoArray) {
            Log.d(TAG, networkInfo.getTypeName());
        }
        Log.d(TAG, "------ Networks -----");
        new ScanNetworkInterface().execute();
    }

    private class ScanNetworkInterface extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            printNetworkInterface();
            return null;
        }

        private void printNetworkInterface() {
            Log.d(TAG, "------ NetworkInterface -----");
            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface thisInterface = networkInterfaces.nextElement();
                    Enumeration<InetAddress> inetAddresses = thisInterface.getInetAddresses();
                    if (inetAddresses.hasMoreElements()) {
                        String niInfo = thisInterface.getDisplayName() + "\n";
                        while (inetAddresses.hasMoreElements()) {
                            InetAddress thisAddress = inetAddresses.nextElement();
                            if (thisAddress instanceof Inet4Address) {
                                niInfo += "HostAddress: " + thisAddress.getHostAddress() + "\n";
                            }
                        }
                        Log.d(TAG, niInfo);
                    }
                }
            } catch (SocketException e) {
                Log.e(TAG, e.toString());
            }
            Log.d(TAG, "------ NetworkInterface -----");
        }
    }
}

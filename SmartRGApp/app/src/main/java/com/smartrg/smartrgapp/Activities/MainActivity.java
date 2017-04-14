package com.smartrg.smartrgapp.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.FloatProperty;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.smartrg.smartrgapp.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    WifiManager wifiManager;
    WifiReceiver wifiReceiver;

    StringBuilder stringBuilder = new StringBuilder();

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        RelativeLayout card_speed_test = (RelativeLayout)findViewById(R.id.card_speed_test);
        card_speed_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SpeedTestActivity.class);
                startActivity(intent);

            }
        });

        RelativeLayout card_heat_map = (RelativeLayout)findViewById(R.id.card_heat_map);
        card_heat_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HeatMapSelectActivity.class);
                startActivity(intent);

            }
        });


        RelativeLayout card_devices = (RelativeLayout)findViewById(R.id.card_devices);
        card_devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DevicesActivity.class);
                startActivity(intent);

            }
        });


        RelativeLayout card_settings = (RelativeLayout)findViewById(R.id.card_settings);
        card_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

            }
        });

        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            String s = getString.invoke(null, "net.hostname").toString();
            Log.d("ANDROID", "android wifi name: " + s);
        } catch (Exception e) {
            e.printStackTrace();
        }



        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            Log.d("WIFI INFO", "SSID: " + wifiInfo.getSSID());
            Log.d("WIFI INFO", "MAC: " + getMacAddr());
            Log.d("WIFI INFO", "Hidden SSID: " + wifiInfo.getHiddenSSID());
            Log.d("WIFI INFO", "IP: " + wifiInfo.getIpAddress());
            Log.d("WIFI INFO", "Network ID: " + wifiInfo.getNetworkId());
            Log.d("WIFI INFO", "Frequency: " + wifiInfo.getFrequency());

           // doInBack();
            new NetworkTrafficTask(getApplicationContext()).execute();


        } else Log.d("WIFI INFO", "Wifi info is null!");


    }

    public void doInBack() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

                wifiReceiver = new WifiReceiver();
                registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
                doInBack();
            }
        }, 30000);
    }

    class WifiReceiver extends  BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> conns = new ArrayList<>();
            ArrayList<Float> sig_strength = new ArrayList<>();

            stringBuilder = new StringBuilder();
            List<ScanResult> wifiList;
            wifiList = wifiManager.getScanResults();
            for (int i = 0; i < wifiList.size(); i++) {
                conns.add(wifiList.get(i).SSID);
                Log.d("WIFI", "connection found for SSID: " + wifiList.get(i).SSID);
            }
        }
    }


    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    class NetworkTrafficTask extends AsyncTask<Void, Void, Void> {

        final String TAG = "NETWORK TASK";

        WeakReference<Context> mContextRef;
        int connectedDevices = 0;

        public NetworkTrafficTask(Context context) {
            mContextRef = new WeakReference<Context>(context);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "Let's sniff the network");
            try {
                Context context = mContextRef.get();

                if (context != null) {
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo activeNet = cm.getActiveNetworkInfo();
                    WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);

                    WifiInfo connectionInfo = wm.getConnectionInfo();
                    int ipAddress = wm.getDhcpInfo().gateway;
                    String ipString = Formatter.formatIpAddress(ipAddress);

                    Log.d(TAG, "active network: " + String.valueOf(activeNet));
                    Log.d(TAG, "ipString: " + String.valueOf(ipString));

                    String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                    Log.d(TAG, "prefix: " + prefix);

                    for (int i =0; i < 255; i++) {
                        String testIP = prefix + String.valueOf(i);

                        InetAddress address = InetAddress.getByName(testIP);
                        boolean reachable = address.isReachable(10);
                        String hostName = address.getCanonicalHostName();
                       // Log.d("NAME", "name: " + hostName);

                        if (reachable) {
                           // InetAddress ip = InetAddress.getLocalHost();
                            NetworkInterface network = NetworkInterface.getByInetAddress(address);
                            byte[] mac = network.getHardwareAddress();
                            StringBuilder sb = new StringBuilder();
                            for (int j = 0; j < mac.length; j++)
                                sb.append(String.format("%02X%s", mac[j], (i < mac.length - 1) ? "-" : ""));

                            Log.d(TAG, "Host: " + String.valueOf(hostName) + "(" + String.valueOf(testIP) + ") is reachable!");
                            Log.d(TAG, "MAC: " + sb.toString());
                            connectedDevices++;
                        }
                    }
                    Log.d(TAG, "Scan complete! Number of connected devices: " + connectedDevices);
                }
            } catch (Throwable t) {
                Log.d(TAG, "Well thats not good...");
            }
            return  null;
        }

    }

}
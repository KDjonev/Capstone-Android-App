package com.smartrg.smartrgapp.Activities;

import android.app.admin.DevicePolicyManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.smartrg.smartrgapp.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
                Intent intent = new Intent(MainActivity.this, VideoStreamAnalyzerActivity.class);
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
            Log.d("WIFI INFO", "Hidden SSID: " + wifiInfo.getHiddenSSID());
            Log.d("WIFI INFO", "Device MAC: " + getMacAddr());
            Log.d("WIFI INFO", "Device IP: " + android.text.format.Formatter.formatIpAddress(wifiInfo.getIpAddress()));
            Log.d("WIFI INFO", "Network ID: " + wifiInfo.getNetworkId());
            Log.d("WIFI INFO", "Frequency: " + wifiInfo.getFrequency());
            Log.d("WIFI INFO", "RSSI: " + wifiInfo.getRssi());
            //doInBack();
        } else Log.d("WIFI INFO", "Wifi info is null!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.speed_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, HomeSettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> conns = new ArrayList<>();
            ArrayList<Float> sig_strength = new ArrayList<>();

            stringBuilder = new StringBuilder();
            List<ScanResult> wifiList;
            wifiList = wifiManager.getScanResults();
            for (int i = 0; i < wifiList.size(); i++) {
                String net_id = wifiList.get(i).SSID;


                if (conns.contains(net_id)) {
                    Log.d("WIFI", "Duplicate SSID found -----> " + net_id);
                    if (wifiList.get(i).frequency > 3000) conns.add(net_id + " (5Ghz)");
                    else conns.add(net_id + " (2.4Ghz)");
                }
                else {
                    if (wifiList.get(i).frequency > 3000) conns.add(net_id + " (5Ghz)");
                    else conns.add(net_id + " (2.4Ghz)");
                }

                //conns.add(wifiList.get(i).SSID);
                /*Log.d("WIFI", "-----> SSID: " + wifiList.get(i).SSID + ", BSSID: " + wifiList.get(i).BSSID
                        + ", Signal: " + wifiList.get(i).level + ", CH: " + wifiList.get(i).channelWidth
                        + ", Freq: " + wifiList.get(i).frequency + ", Capabilities: " + wifiList.get(i).capabilities );*/

            }
            /// Log.d("WIFI", "--------- No more connections found ----------");

            Collections.sort(wifiList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult o1, ScanResult o2) {
                    if (o1.level == o2.level) return 0;
                    return o1.level < o2.level ? 1 : -1;
                }
            });
            Log.d("WIFI", "---------- Top WiFi Connections ----------");
            for (int k = 0; k < conns.size(); k++) {
                Log.d("WIFI", "" + k + ". " + conns.get(k) + " ---> " + wifiList.get(k).level + " dBm");
            }
            Log.d("WIFI", "---------- End ----------");
            unregisterReceiver(wifiReceiver);
        }
    }

    public void doInBack() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

                wifiReceiver = new WifiReceiver();
                registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
            }
        }, 100);
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
                    res1.append(String.format("%02X:", b));
                }

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
}
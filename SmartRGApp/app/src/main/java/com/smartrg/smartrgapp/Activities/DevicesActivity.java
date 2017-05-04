package com.smartrg.smartrgapp.Activities;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.smartrg.smartrgapp.Classes.Device;
import com.smartrg.smartrgapp.Classes.MyRecyclerViewAdapter;
import com.smartrg.smartrgapp.R;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DevicesActivity extends AppCompatActivity {

    ArrayList<Device> deviceList;
    MyRecyclerViewAdapter adapter;
    Handler handler = new Handler();
    TextView device_count;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        device_count = (TextView) findViewById(R.id.device_count);
        deviceList = new ArrayList<>();
        adapter = new MyRecyclerViewAdapter(deviceList);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(lm);
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), 0));
        recyclerView.setAdapter(adapter);

        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        new NetworkTrafficTask(getApplicationContext()).execute();
        doInBack();
    }

    public void doInBack() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new NetworkTrafficTask(getApplicationContext()).execute();
                doInBack();
            }
        }, 14000);
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
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "Not Available";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.devices, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class NetworkTrafficTask extends AsyncTask<Void, Void, Void> {

        final String TAG = "NETWORK TASK";

        WeakReference<Context> mContextRef;
        int connectedDevices = 0;

        public NetworkTrafficTask(Context context) {
            mContextRef = new WeakReference<Context>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            device_count.setText("Scanning for devices . . .");
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "--------- Let's sniff the network ---------");
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
                        // Log.d(TAG, "testIP: " + testIP);

                        InetAddress address = InetAddress.getByName(testIP);
                        String hostName = address.getCanonicalHostName();
                        boolean reachable = address.isReachable(10);
                        // Log.d("NAME", "name: " + hostName);

                        if (reachable) {
                            StringBuilder sb = new StringBuilder();
                            InetAddress ip = address;
                            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                            if (network == null) {
                                //String s = getMacAddr();
                                //Log.d(TAG, "network null, mac: " + s);
                            }
                            else {
                                byte[] mac = network.getHardwareAddress();
                                Log.d("NAME", "network is not null. Getting MAC...");
                                for (int j = 0; j < mac.length; j++)
                                    sb.append(String.format("%02X%s", mac[j], (i < mac.length - 1) ? "-" : ""));
                            }
                            Log.d(TAG, "Device: " + String.valueOf(hostName) + " (" + String.valueOf(testIP) + ") is reachable!");
                            Log.d(TAG, "MAC: " + sb.toString());
                            connectedDevices++;
                            Device device = new Device(String.valueOf(hostName), String.valueOf(testIP), sb.toString());
                            if (deviceList.isEmpty()) {
                                Log.d("ADAPTER", "List empty. adding --------> " + device.getName());
                                deviceList.add(device);
                            }
                            boolean new_device = true;
                            for (int j = 0; j < deviceList.size(); j++) {
                                if (deviceList.get(j).getName().equals(device.getName())) {
                                    Log.d("ADAPTER", "Device already found: " + device.getName());
                                    new_device = false;
                                }
                            }
                            if (new_device) {
                                deviceList.add(device);
                                Log.d("ADAPTER", "New device found! --------> " + device.getName());
                            }
                        }
                    }
                    Log.d(TAG, "--------- Scan complete! Number of connected devices ---> " + connectedDevices);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "Well that's not good...");
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            device_count.setText("" + deviceList.size() + " devices connected");
        }
    }
}

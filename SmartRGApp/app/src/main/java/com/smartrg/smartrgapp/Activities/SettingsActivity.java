package com.smartrg.smartrgapp.Activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.smartrg.smartrgapp.R;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;


public class SettingsActivity extends AppCompatActivity {

    boolean wpsComplete;
    int wpsSetup = 0;
    WifiManager.WpsCallback wpsCallback;
    WifiManager wifiManager;
    private WebSocketClient mWebSocketClient;
    boolean loginSuccessful = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Button button = (Button) findViewById(R.id.wps_button);
        Button router_button = (Button) findViewById(R.id.router_wps_button);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

        wpsCallback = new WifiManager.WpsCallback() {
            @Override
            public void onStarted(String pin) {
                Log.d("WPS", "onStarted()!");
                /*if (pin != null) {
                    Log.d("WPS", "String PIN not null!");
                } else Log.d("WPS", "String PIN is null!");*/
            }

            @Override
            public void onSucceeded() {
                wpsComplete = true;
                Log.d("WPS", "---------- WPS Successful! ----------");
                //Toast.makeText(getApplicationContext(), "Connected to WiFi: " + wifiManager.getConnectionInfo().getSSID(), Toast.LENGTH_SHORT).show();
                //displayPortal();
            }

            @Override
            public void onFailed(int reason) {
                Log.d("WPS", "onFailed()!");
                wpsComplete = true;
                String errorMessage;
                switch (reason) {
                    case WifiManager.WPS_OVERLAP_ERROR:
                        errorMessage = "WPS OVERLAP ERROR";
                        break;
                    case WifiManager.WPS_WEP_PROHIBITED:
                        errorMessage = "WPS WEP PROHIB";
                        break;
                    case WifiManager.WPS_TKIP_ONLY_PROHIBITED:
                        errorMessage = "WPS TKIP ONLY";
                        break;
                    case WifiManager.WPS_TIMED_OUT:
                        Log.d("WPS", "Timed out!");
                        break;
                    default:
                        errorMessage = "Generic error message";
                        break;
                }
            }
        };

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new wpsTask().execute();
            }
        });

        router_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new socketTask().execute();
                //Redirect to new activity page
                Intent webViewRouterConfig = new Intent(SettingsActivity.this, webView.class);
                startActivity(webViewRouterConfig);
            }
        });
    }


    public class wpsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            startWPS();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public class socketTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            connectWebSocket();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }


    public void startWPS() {
        wpsComplete = false;
        WpsInfo wpsConfig = new WpsInfo();
        wpsConfig.setup = wpsSetup;
        if (!wpsComplete) {
            Log.d("WPS", "Starting wps...");
            wifiManager.startWps(wpsConfig, wpsCallback);
        }
    }

    public void displayPortal() {
        String url = "ws://192.168.1.1/websocket/";
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));

        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    // uses org.java.WebSocket
    public void connectWebSocket() {
        final String TAG = "WEB_SOCKET";

        URI uri;
        try {
            uri = new URI("ws://192.168.1.1/websocket/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        mWebSocketClient = new WebSocketClient(uri, new Draft_17()) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.d(TAG, "New connection opened");
                String message = "{\"jsonrpc\":\"2.0\",\"id\":16,\"method\":\"login\",\"params\":[\"admin\",\"admin\"]}";
                mWebSocketClient.send(message);
                Log.d(TAG, "Sent: " + message);
            }

            @Override
            public void onMessage(String s) {
                Log.d(TAG, "received message: " + s);
                if (!loginSuccessful) {
                    String hex = s.substring(46);
                    String hexActual = hex.substring(0, hex.length() - 3);
                    //String message = "{\"jsonrpc\":\"2.0\",\"id\":28,\"method\":\"call\",\"params\":[\"" + hexActual+ "\",\"/juci/wireless\",\"station\",{\"mac\":\"E8:50:8B:ED:A7:FA\" }]}";
                    String message = "{\"jsonrpc\":\"2.0\",\"id\":28,\"method\":\"call\",\"params\":[\"" + hexActual+ "\",\"/juci/wps\",\"press_wps\",{}]}";
                    mWebSocketClient.send(message);
                    Log.d(TAG, "Sent: " + message);
                    loginSuccessful = true;
                } else {
                    Log.d(TAG, "Nothing more to do, closing socket...");
                    loginSuccessful = false;
                    mWebSocketClient.close();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                loginSuccessful = false;
                if (i == 1000) {
                    Log.d(TAG, "Graceful close of socket!");
                } else {
                    Log.d(TAG, "Closed with exit code " + i + " additional info: " + s);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "Error :" + e.getMessage());
            }
        };

        Log.d(TAG, "trying to connect...");
        mWebSocketClient.connect();
    }
}
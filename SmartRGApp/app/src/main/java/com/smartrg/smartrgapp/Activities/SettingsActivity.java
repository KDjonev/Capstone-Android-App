package com.smartrg.smartrgapp.Activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;

public class SettingsActivity extends AppCompatActivity {

    boolean wpsComplete;
    int wpsSetup = 0;
    WifiManager.WpsCallback wpsCallback;
    WifiManager wifiManager;
    WebView webView;
    private WebSocketClient mWebSocketClient;
    Socket socket;
    SSLContext sslContext;

    private WebSocket mConnection = new WebSocketConnection();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Button button = (Button) findViewById(R.id.wps_button);

        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

        wpsCallback = new WifiManager.WpsCallback() {
            @Override
            public void onStarted(String pin) {
                Log.d("WPS", "onStarted()!");
                if (pin != null) {
                    Log.d("WPS", "String PIN not null!");
                } else Log.d("WPS", "String PIN is null!");
            }

            @Override
            public void onSucceeded() {
                wpsComplete = true;
                Log.d("WPS", "onSucceeded()!");
                Toast.makeText(getApplicationContext(), "Connected to WiFi: " + wifiManager.getConnectionInfo().getSSID(), Toast.LENGTH_SHORT).show();
                displayPortal();
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
                startWPS();
            }
        });

       connectWebSocket();
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

    // uses Socket.IO
    /*public void connectWebSocket() {
        final String TAG = "WEB_SOCKET";
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        System.out.println("checkClientTrusted =============");
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        System.out.println("checkClientTrusted =============");
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        IO.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });


        try {
            IO.Options opts = new IO.Options();
            //opts.sslContext = sslContext;
            opts.secure = false;
            Socket sock = IO.socket("http://192.168.1.1/websocket/", opts);
            sock.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "onConnect");
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "onDisconnect");

                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "onError");

                }
            }).on("event", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "onEvent");

                }
            }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "onConnectError! " + args[0].toString());
                }
            }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "onConnectTimeout " + args[0].toString());
                }
            });
            Log.d(TAG, "trying to connect...");
            sock.connect();

            sock.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Transport transport = (Transport) args[0];
                    transport.on(Transport.EVENT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Exception e = (Exception) args[0];
                            Log.e(TAG, "Transport error " + e);
                            e.printStackTrace();
                            e.getCause().printStackTrace();
                        }
                    });
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/



    // uses autobahn
    /*public void connectWebSocket() {
        final String wsUri = "ws://192.168.1.1/websocket/";
        final String TAG = "WEB_SOCKET";

        try {
            mConnection.connect(wsUri, new WebSocket.ConnectionHandler() {
                @Override
                public void onOpen() {
                    Log.d(TAG, "onOpen");
                    mConnection.sendTextMessage("hi");
                    mConnection.sendTextMessage("yo");
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.d(TAG, "onClose(), exit code: " + code + " ---> reason: " + reason);

                }

                @Override
                public void onTextMessage(String payload) {
                    Log.d(TAG, "onTextMessage: " + payload);

                }

                @Override
                public void onRawTextMessage(byte[] payload) {
                    Log.d(TAG, "onRawText");

                }

                @Override
                public void onBinaryMessage(byte[] payload) {
                    Log.d(TAG, "onOpen");

                }
            });
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }*/


    // uses socket.IO
    /*public void connectWebSocket() {
        final String TAG = "WEB_SOCKET";
        try {
            socket = IO.socket("http://192.182.1.1/websocket/");
        }catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Websocket", "connected");
                socket.emit("foo", "hi");
                //socket.disconnect();
            }
        }).on("foo", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Websocket", "received");
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Websocket", "dis-connected");
            }
        });
        socket.connect();
    }*/

    // uses org.java.WebSocket
    public void connectWebSocket() {
        final String TAG = "WEB_SOCKET";
        java.net.Socket socket = new java.net.Socket();
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        System.out.println("checkClientTrusted =============");
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        System.out.println("checkClientTrusted =============");
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            //HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

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
                String hex = s.substring(46);
              //  Log.d(TAG, "hex: " + hex);
                String hexActual = hex.substring(0, hex.length() - 3);
            //    Log.d(TAG, "hex: " + hexActual);
                String message = "{\"jsonrpc\":\"2.0\",\"id\":28,\"method\":\"call\",\"params\":[\"" + hexActual+ "\",\"/juci/wireless\",\"station\",{\"mac\":\"E8:50:8B:ED:A7:FA\" }]}";
                mWebSocketClient.send(message);
                Log.d(TAG, "Sent: " + message);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.d(TAG, "Closed with exit code " + i + " additional info: " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "Error :" + e.getMessage());
            }
        };

      /* try {
            mWebSocketClient.setSocket(sslContext.getSocketFactory().createSocket());
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Log.d(TAG, "trying to connect...");
        mWebSocketClient.connect();


    }

    public void sendMessage(View view) {
        mWebSocketClient.send("");
    }




}
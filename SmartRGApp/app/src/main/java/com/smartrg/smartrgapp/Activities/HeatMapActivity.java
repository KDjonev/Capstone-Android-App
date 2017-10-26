package com.smartrg.smartrgapp.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.smartrg.smartrgapp.Classes.TestPoint;
import com.smartrg.smartrgapp.Classes.HeatMap;
import com.smartrg.smartrgapp.Classes.RouterPoint;
import com.smartrg.smartrgapp.R;

public class HeatMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Hernan For MAP
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;


    private GoogleMap mMap;
    private MapFragment mMapFragment;
    protected GoogleApiClient mGoogleApiClient;
    private HeatMap mHeatMap;
    private TestPoint mCurrentTestPoint;
    private RouterPoint mCurrentRouterPoint;

    private boolean isPlacingTest = false;
    private boolean isPlacingRouter = false;

    private String mDeviceIp;
    private String mDeviceMac;

    private final static int MY_REQUEST = 6;


    private double lat;
    private double lon;
    private Location mLocation;
    private Marker routerMarker, testMarker;
    private FloatingActionMenu fab_general_menu, fab_test_menu;
    private com.github.clans.fab.FloatingActionButton fab1, fab2, fab3, fab_test_1, fab_test_2;

    private HeatmapTileProvider provider;
    private TileOverlay overlay;
    private ArrayList<WeightedLatLng> list, mDynamicList, mTestPinList;
    private LatLng currentPinLocation;
    protected static final String TAG = "HEAT MAP ACTIVITY";
    private LatLng routerLoaction;
    private WifiManager wifiManager;
    private String ipAddress;
    private IperfTask iperfTask;
    private ArrayList<JSONObject> heatmapPointList = new ArrayList<JSONObject>();
    private JSONObject heatmap;
    private JSONArray routers;
    private JSONObject residence;
    private JSONObject save;


    private String acct_num;

    private ArrayList<Marker> mTestMarkerList;
    private boolean isMarkersVisible = true;

    private ProgressDialog testingDialog;

    String ip;
    String bad_test_value;
    Boolean settingsChanged = false, hasRouterBeenPlaced = false;

    int circle_radius = 150;

    boolean isLoading;
    int pos;

    WifiReceiver wifiReceiver;
    private final Handler handler = new Handler();
    StringBuilder stringBuilder = new StringBuilder();

    Handler hand = new Handler();

    boolean tcpDone = false, udpDone = false, scanDone = false;
    int progress = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);

        // setup the toolbar with colors, icons, etc.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // set up google maps
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        // check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSIONS", "Requesting Location permission!");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_REQUEST);
        }
        else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(new Criteria(), true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, HeatMapActivity.this);
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }

        // initialize data structures
        mHeatMap = new HeatMap();
        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        mDeviceMac = wifiInfo.getBSSID();

        // Initialize click listeners for FAB main menu
        fab_general_menu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        fab1 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.menu_item1);
        fab2 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.menu_item2);
        fab3 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.menu_item3);

        // place router pin
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_general_menu.close(true);
                if (!isPlacingRouter) {
                    isPlacingRouter = true;
                } else {
                }
                Toast.makeText(getApplicationContext(), "Tap to place pin at router location", Toast.LENGTH_SHORT).show();
            }
        });
        // place test point pin
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasRouterBeenPlaced) {
                    Toast.makeText(getApplicationContext(), "Error! Place a router pin before you can create test points!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    fab_general_menu.close(true);
                    if (isPlacingRouter) {
                        isPlacingRouter = false;
                        isPlacingTest = true;
                    }
                    else {
                        isPlacingTest = true;
                    }
                    fab_general_menu.animate().translationY(fab_general_menu.getHeight()).setInterpolator(new LinearInterpolator()).start();
                    fab_test_menu.animate().translationY(fab_general_menu.getHeight()).setInterpolator(new LinearInterpolator()).start();
                    Toast.makeText(getApplicationContext(), "Tap to place pin at test location", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // toggle marker visibility
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_general_menu.close(true);
            }
        });

        // initialize on click listeners for test pin menu
        fab_test_menu = (FloatingActionMenu) findViewById(R.id.fab_menu_test_pin);
        fab_test_1 = (FloatingActionButton) findViewById(R.id.menu_item_test_1);
        fab_test_2 = (FloatingActionButton) findViewById(R.id.menu_item_test_2);

        // Begin test
        fab_test_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_test_menu.close(true);
                initializeIperf();
                fab_test_menu.animate().translationY(fab_general_menu.getHeight()).setInterpolator(new LinearInterpolator()).start();
                fab_general_menu.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            }
        });
        // Cancel test
        fab_test_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_test_menu.close(true);
                if (testMarker != null) {
                    testMarker.remove();
                }
                fab_test_menu.animate().translationY(fab_general_menu.getHeight()).setInterpolator(new LinearInterpolator()).start();
                fab_general_menu.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
            }
        });

        // try to get info if loading a heat map case
        isLoading = getIntent().getBooleanExtra("load", false);
        pos = getIntent().getIntExtra("pos", 0);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                ip =data.getStringExtra("ip");
                Log.d("HEATMAP SETTINGS RESULT", "collected ip: " + ip + " from heatmap settings");
                bad_test_value = data.getStringExtra("bad_test");
                Log.d("HEATMAP SETTINGS RESULT", "collected bad rssi value: " + bad_test_value + " from heatmap settings");
                settingsChanged = true;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                // no result
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("BACK PRESSED", "(Physical) phone back button pressed!");
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.heat_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_heat_map:
                // selected the save option,  should pop up a dialog asking to confirm save
               // new SaveHeatMapAsync().execute("");
                return true;
            case R.id.action_settings:
                // selected the settings option, open new Settings Activity
                Intent intent = new Intent(HeatMapActivity.this, HeatMapSettingsActivity.class);
                startActivityForResult(intent, 1);
                return true;
            case android.R.id.home:
                // selected back button
                Log.d("BACK PRESSED", "back pressed from toolbar");
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getBaseContext());
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Log.d("PERMISSIONS", "Permission Granted!");
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    String provider = locationManager.getBestProvider(new Criteria(), true);
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, HeatMapActivity.this);
                        buildGoogleApiClient();
                        mGoogleApiClient.connect();
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("PERMISSIONS", "Permission Denied!");
                    Toast.makeText(getApplicationContext(), "Allow location access to view map features", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * Creates a new instance of Google API client
     */
    protected synchronized void buildGoogleApiClient() {
        Log.d("GOOGLE API CLIENT", "Builing API client. . .");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Method to do something when user location is changed
     *  - gets called often (like 1 per sec)
     * @param location
     *  - Location object containing new latitude and longitude
     */
    @Override
    public void onLocationChanged(Location location) {
    }

    /**
     * Connect to Google API client on start
     */
   /* @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(HeatMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleApiClient.connect();
        }
        else
            return;
    }*/

    /**
     * Disconnect from Google API client on stop
     */
    @Override
    protected void onStop() {
        super.onStop();
        if(ContextCompat.checkSelfPermission(HeatMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGoogleApiClient.disconnect();
        }
        else
            return;
    }

    /**
     * Do something on activity pause
     */
    @Override
    protected  void onPause() {
        super.onPause();
    }

    /**
     * Called when Google API client is successfully connected
     * @param connectionHint
     * Contains information about the connection
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Connected to GoogleApiClient");
        // Get current location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            lat = mLocation.getLatitude();
            lon = mLocation.getLongitude();
            Log.d(TAG, "lat :" + mLocation.getLatitude() + " lon: " + mLocation.getLongitude());
        }
        // update camera to zoom in closer
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 20.8f));
        // Loading a heatmap case
        if (isLoading) {
            Log.d("LOAD HEAT MAP", "loading a heat map condition, displying now...");
            if (pos == 0) addHeatMapGood();
            else addHeatMapBad();
            fab_general_menu.setVisibility(View.INVISIBLE);
        }
        // Click listener for the map when dropping pins
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                fab_general_menu.close(true);
                // case for placing a test point
                if (isPlacingTest) {
                    mCurrentTestPoint = new TestPoint(latLng.latitude, latLng.longitude);
                    // add marker to map
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude))
                            .title("Test Point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    Log.d(TAG, "New test pin placed at lat: " + latLng.latitude + " lon: " + latLng.longitude);
                    testMarker = mMap.addMarker(markerOptions);
                    // animate to show test menu
                    fab_test_menu.setVisibility(View.VISIBLE);
                    fab_test_menu.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
                    isPlacingTest = false;
                }
                // case for placing a router point
                else if (isPlacingRouter) {
                    mCurrentRouterPoint = new RouterPoint(latLng.latitude, latLng.longitude);
                    // add marker to map
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude))
                            .title("Router").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    Log.d(TAG, "New router pin placed at lat: " + latLng.latitude + " lon: " + latLng.longitude);
                    testMarker = mMap.addMarker(markerOptions);
                    // begin 5 second test at router location to get baseline rssi for future test points
                    isPlacingRouter = false;
                    hasRouterBeenPlaced = true;
                    new initializeRouterBaseRssi().execute("");
                }
            }
        });
        // Touch listener for Google map to handle zooming
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("MAP_CAMERA_ZOOM", "Zoom: " + cameraPosition.zoom);
            }
        });
        // Click listener for test/router pins currently on the map
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
    }

    /**
     * Called when failed connection to Google API client occurs
     * @param result
     * Contains the result information for failed connection
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    /**
     * Connection to Google API client is suspended
     * @param cause
     * Reason for suspension
     */
    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");
        // onConnected() will be called again automatically when the service reconnects
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("PROVIDER", "onProviderDisabled() called");
        //Toast.makeText(this.context, "GPS Disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("PROVIDER", "onProviderEnabled() called");
        //Toast.makeText(this.context, "GPS Enabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle bundle) {
        Log.d("PROVIDER", "onStatusChanged() called");
    }

    /**
     * Method that tries to begin execution of iperf using runtime binary executable
     *  - Checks if iperf binary exists, copies it in if it does not exist
     *  - creates a new async task to read iperf output
     */
    public void initializeIperf() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // get the device's ip address for use in iperf command
        if (wifiInfo != null) {
            //This should return the right IP address if DHCP is enabled
            ipAddress = android.text.format.Formatter.formatIpAddress(wifiManager.getDhcpInfo().gateway);
            //ipAddress = "192.168.0.2";
            Log.d("INIT_IPERF", "This is your IP: " + ipAddress);
            if (settingsChanged) {
                ipAddress = ip;
                Log.d("INIT_IPERF", "Settings changed! This is your new ip: " + ipAddress);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Test failed! Verify your device is connected to wifi and try again", Toast.LENGTH_SHORT).show();
            return;
        }
        // copy the iperf executable into device's internal storage
        InputStream inputStream;
        try {
            inputStream = getResources().getAssets().open("iperf9");
        }
        catch (IOException e) {
            Log.d("Init Iperf error!", "Error occurred while accessing system resources, no iperf3 found in assets");
            e.printStackTrace();
            return;
        }
        try {
            //Checks if the file already exists, if not copies it.
            new FileInputStream("/data/data/com.smartrg.smartrgapp/iperf9");
        }
        catch (FileNotFoundException f) {
            try {
                OutputStream out = new FileOutputStream("/data/data/com.smartrg.smartrgapp/iperf9", false);
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                inputStream.close();
                out.close();
                Process process =  Runtime.getRuntime().exec("/system/bin/chmod 744 /data/data/com.smartrg.smartrgapp/iperf9");
                process.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
            IperfTask iperfTask = new IperfTask();
            iperfTask.execute();
            return;
        }
         IperfTask iperfTask = new IperfTask();
        iperfTask.execute();
    }

    /**
     * Async Task to communicate and receive output from iperf
     *
     */
    private class IperfTask extends AsyncTask<Void, String, String> {
        Process p = null;
        String command = "iperf3 -c " + ipAddress;
        String tcp_command = "iperf3 -c " + ipAddress + " -R -J -t 5";
        String udp_command = "iperf3 -c " + ipAddress + " -u -J -t 5";
        String[] which_command = {tcp_command, udp_command};
        int max;

        Double downstream = 0.0;
        Double upstream = 0.0;
        Integer retransmits = 0;
        Double jitter = 0.0;
        Double lost_percent = 0.0;
        Integer rssi = 0;

        @Override
        protected void onPreExecute() {
            testingDialog = new ProgressDialog(HeatMapActivity.this);
            testingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            testingDialog.setTitle("Speed Test");
            testingDialog.setMessage("Running TCP test. . . ");
            testingDialog.setCancelable(false);
            testingDialog.show();
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            max = wifiInfo.getLinkSpeed();
            Log.d("ON_PRE_EXECUTE", "link speed: " + max);
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (!command.matches("(iperf3 )?((-[s,-server])|(-[c,-client] ([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))|(-[c,-client] \\w{1,63})|(-[h,-help]))(( -[f,-format] [bBkKmMgG])|(\\s)|( -[l,-len] \\d{1,5}[KM])|( -[B,-bind] \\w{1,63})|( -[r,-tradeoff])|( -[v,-version])|( -[N,-nodelay])|( -[T,-ttl] \\d{1,8})|( -[U,-single_udp])|( -[d,-dualtest])|( -[w,-window] \\d{1,5}[KM])|( -[n,-num] \\d{1,10}[KM])|( -[p,-port] \\d{1,5})|( -[L,-listenport] \\d{1,5})|( -[t,-time] \\d{1,8})|( -[i,-interval] \\d{1,4})|( -[u,-udp])|( -[b,-bandwidth] \\d{1,20}[bBkKmMgG])|( -[m,-print_mss])|( -[P,-parallel] d{1,2})|( -[M,-mss] d{1,20}))*")) {
                Log.d("DO_IN_BACKGROUND", "Error! Invalid syntax for iperf3 command!");
                publishProgress("Error: invalid syntax \n\n");
                return null;
            }
            try {
                updateProg();
                registerWiFiSearchReceiver();
                for (String c : which_command) {
                    String[] commands = c.split(" ");
                    List<String> commandList = new ArrayList<>(Arrays.asList(commands));
                    commandList.add(0, "/data/data/com.smartrg.smartrgapp/iperf9");
                    p = new ProcessBuilder().command(commandList).redirectErrorStream(true).start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    int read;
                    char[] buffer = new char[4096];
                    StringBuffer output = new StringBuffer();
                    while ((read = reader.read(buffer)) > 0) {
                        output.append(buffer, 0, read);
                        publishProgress(output.toString());
                        output.delete(0, output.length());
                    }
                    reader.close();
                    p.destroy();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("DO_IN_BACKGROUND", "Error! Failed retrieving iperf3 results");
            }
            return null;
        }

        @Override
        public void onProgressUpdate(String... strings) {
            JSONObject json = new JSONObject();
            String protocol = null;
            String output = strings[0];
            try {
                json = new JSONObject(output);
                protocol = json.getJSONObject("start").getJSONObject("test_start").getString("protocol");
            } catch (org.json.JSONException e) {
                Log.d("JSONERROR", "Could not convert to JSONObject" + output);
            }
            if (protocol.equals("TCP")) {
                try {
                    JSONObject end = json.getJSONObject("end");
                    Double downbits = end.getJSONObject("sum_sent").getDouble("bits_per_second");
                    Double upbits = end.getJSONObject("sum_received").getDouble("bits_per_second");
                    retransmits = end.getJSONObject("sum_sent").getInt("retransmits");
                    downstream = downbits * Math.pow(10, -6);
                    upstream = upbits * Math.pow(10, -6);
                    runOnUiThread(changeDialogTitle);
                } catch (org.json.JSONException e) {
                    Log.d("JSONERROR", "Could not convert to JSONObject: " + output);
                }
            }
            if (protocol.equals("UDP")) {
                try {
                    JSONObject sum = json.getJSONObject("end").getJSONObject("sum");
                    jitter = sum.getDouble("jitter_ms");
                    lost_percent = sum.getDouble("lost_percent");
                    udpDone = true;

                } catch (org.json.JSONException e) {
                    Log.d("JSONERROR", "Could not convert to JSONObject" + output);
                }
            }
            Log.d("ON_PROGRESS_UPDATE", "upstream: " + upstream.toString() + "\ndownstream: " + downstream.toString()
                    + "\nretransmits: " + retransmits.toString() + "\njitter: " + jitter.toString() +
                    "\nlost_percent: " + lost_percent.toString());
        }

        @Override
        public void onPostExecute(String result) {
            // get the rssi value at the end of iperf tests
            rssi = wifiManager.getConnectionInfo().getRssi();
            //The running process is destroyed and system resources are freed.
            if (p != null) {
                p.destroy();
                try {
                    p.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                testingDialog.dismiss();
                //Toast.makeText(getApplicationContext(), "test has finished", Toast.LENGTH_SHORT).show();
            }

            fancyShadingAlgorithm(rssi, downstream, upstream, jitter, lost_percent, retransmits);
        }
    }

    private Runnable changeDialogTitle = new Runnable() {
        @Override
        public void run() {
            testingDialog.setMessage("Running UDP test . . .");
        }
    };

    public void updateProg() {
        hand.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!udpDone || progress < 100) {
                    progress = progress + 10;
                    testingDialog.setProgress(progress);
                    updateProg();
                } else {
                    udpDone = false;
                    progress = 0;
                }
            }
        }, 1000);
    }

    /**
     * Method to color in the test point based on iperf3 data output
     *
     * @param rssi RSSI value at test point
     * @param up upstream in mbps/sec
     * @param down downstream in mbps/sec
     * @param jit jitter rate
     * @param lost lost percentage rate
     * @param ret number of retransmits
     */
    public void fancyShadingAlgorithm(Integer rssi, Double up, Double down, Double jit, Double lost, Integer ret) {
        mCurrentTestPoint.setRssi(rssi);
        mCurrentTestPoint.setDownstream(down);
        mCurrentTestPoint.setUpstream(up);
        mCurrentTestPoint.setJitter(jit);
        mCurrentTestPoint.setLostPercentage(lost);
        mCurrentTestPoint.setRetransmits(ret);

        Integer base_rssi = mCurrentRouterPoint.getRssi();
        Integer test_rssi = rssi;
        if (test_rssi + 5 >= base_rssi) {
            mCurrentTestPoint.setIntensity(1.0);
        }
        else if (test_rssi + 10 >= base_rssi) {
            mCurrentTestPoint.setIntensity(0.8);
        }
        else if (test_rssi + 15 >= base_rssi) {
            mCurrentTestPoint.setIntensity(0.7);
        }
        else if (test_rssi + 20 >= base_rssi) {
            mCurrentTestPoint.setIntensity(0.6);
        }
        else if (test_rssi + 25 >= base_rssi) {
            mCurrentTestPoint.setIntensity(0.5);
        }
        else if (test_rssi + 30 >= base_rssi) {
            mCurrentTestPoint.setIntensity(0.4);
        }
        else if (test_rssi + 35 >= base_rssi) {
            mCurrentTestPoint.setIntensity(0.3);
        }
        else if (test_rssi + 40 >= base_rssi) {
            mCurrentTestPoint.setIntensity(0.2);
        }
        else if (test_rssi + 45 >= base_rssi) {
            mCurrentTestPoint.setIntensity(0.0);
        }

        mHeatMap.addTestPin(mCurrentTestPoint);
        ArrayList<WeightedLatLng> testList = mHeatMap.createWeightedList();

        if (overlay != null) {
            overlay.remove();
        }
        Log.d("ABOUT TO ADD HEATMAP", "test pin rssi: " + rssi);
        Log.d("ABOUT TO ADD HEATMAP", "test pin intensity: " + mCurrentTestPoint.getIntensity());

        int[]colors = { Color.rgb(255, 0, 0), Color.rgb(102,255,0)};
        float[] startPoints = { 0.2f, 1f};
        Gradient gradient = new Gradient(colors, startPoints);

        provider = new HeatmapTileProvider.Builder().weightedData(testList).radius(50).opacity(0.5).gradient(gradient).build();
        //provider.setRadius(100);
        provider.setRadius(circle_radius);
        overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

        int rssi_limit = 75;

        if (settingsChanged) {
            if (bad_test_value.equals("")) {
                bad_test_value = "75";
            }
            rssi_limit = Integer.parseInt(bad_test_value);
            Log.d("HEAT_MAP_ALGORITHM", "rssi limit has been changed from default! It is now: " + rssi_limit);
        }

        if (test_rssi < -rssi_limit) {
            final AlertDialog alertDialog = new AlertDialog.Builder(HeatMapActivity.this).create();
            alertDialog.setTitle("Attention");
            alertDialog.setIcon(R.mipmap.ic_warning_black_24dp);
            alertDialog.setMessage("Your wifi signal at this spot is poor! You should think about adding an extender near this location.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok, check it out", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //alertDialog.dismiss();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.smartrg.com/we65ac"));
                    startActivity(browserIntent);
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No thanks", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }

    /**
     * Async Task to get average Rssi value at router location over 5 seconds (1 per sec)
     */
    private class initializeRouterBaseRssi extends AsyncTask<String, String, String> {
        ProgressDialog dialog;
        Integer rssi;
        Integer rssiAvg = 0;
        ArrayList<Integer> rssiList;
        int prog = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rssiList = new ArrayList<>();
            dialog = new ProgressDialog(HeatMapActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setTitle("Router Speed Test");
            dialog.setMessage("Running tests on router . . .");
            dialog.setCancelable(false);
            dialog.setProgress(0);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            for(int i = 0; i < 5; i++) {
                try {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    rssi = wifiInfo.getRssi();
                    rssiList.add(rssi);
                    Log.d("ROUTER_RSSI_INIT", "rssi: " + rssi);
                    prog += 20;
                    dialog.setProgress(prog);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            prog = 0;
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            for (int i = 0; i < rssiList.size(); i++) {
                rssiAvg += Math.abs(rssiList.get(0));
                Log.d("ROUTER_RSSI_INIT", "rssi avg: " + rssiAvg);
            }
            int overllAvg = rssiAvg / rssiList.size();
            mCurrentRouterPoint.setRssi(-overllAvg);
            Log.d("ROUTER INFO", "base router rssi: " + -overllAvg);

            mHeatMap.addRouterPin(mCurrentRouterPoint);
            ArrayList<WeightedLatLng> testList = mHeatMap.createWeightedList();

            int[]colors = { Color.rgb(255, 0, 0), Color.rgb(102,255,0)};
            float[] startPoints = { 0.2f, 1f};
            Gradient gradient = new Gradient(colors, startPoints);

            provider = new HeatmapTileProvider.Builder().weightedData(testList).radius(50).opacity(0.5).gradient(gradient).build();
            //provider.setRadius(100);
            provider.setRadius(circle_radius);
            overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

            dialog.dismiss();
        }
    }


    public void registerWiFiSearchReceiver() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

                wifiReceiver = new WifiReceiver();
                registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
                //doInBack();
            }
        }, 100);
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
            }
            Collections.sort(wifiList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult o1, ScanResult o2) {
                    if (o1.level == o2.level) return 0;
                    return o1.level < o2.level ? 1 : -1;
                }
            });
            Log.d("WIFI", "---------- Top WiFi Connections ----------");
            for (int k = 0; k < conns.size(); k++) {
                Log.d("WIFI", "" + k + ". " + conns.get(k) + " ---> " + wifiList.get(k).level);
            }
            Log.d("WIFI", "---------- End ----------");
            unregisterReceiver(wifiReceiver);
        }
    }



    /**
     * Temporary method that loads an example of a good result heatmap (mostly green) onto the map
     *  - centers on map location
     */
    private void addHeatMapGood() {
        Log.d("ADDED HEAT MAP", "addHeatMap called!");
        list = new ArrayList<>();
        list.add(new WeightedLatLng(new LatLng(34.409400564638496, -119.86459124833344), 1.0));
        list.add(new WeightedLatLng(new LatLng(34.409395585662374, -119.86453995108603), 0.8));
        list.add(new WeightedLatLng(new LatLng(34.40935851994197, -119.86458420753479), 0.7));
        list.add(new WeightedLatLng(new LatLng(34.40931758166454, -119.86458085477352), 0.7));
        list.add(new WeightedLatLng(new LatLng(34.40929849556889, -119.86454866826534), 0.8));
        list.add(new WeightedLatLng(new LatLng(34.40934551927497, -119.86453525722028), 0.7));

        int[]colors = { Color.rgb(255, 0, 0), Color.rgb(102,255,0)};
        float[] startPoints = { 0.2f, 1f};
        Gradient gradient = new Gradient(colors, startPoints);

        provider = new HeatmapTileProvider.Builder().weightedData(list).radius(50).opacity(0.5).gradient(gradient).build();
        provider.setRadius(100);
        overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

        double load_lat, load_lon;
        load_lat = 34.40935851994197;
        load_lon = -119.86458420753479;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(load_lat, load_lon), 20.8f));
    }
    /**
     * Temporary method that loads an example of a bad result heatmap (some red patches) onto the map
     *  - centers on map location
     */
    private void addHeatMapBad() {

        Log.d("ADDED HEAT MAP", "addHeatMap called!");
        list = new ArrayList<>();
        list.add(new WeightedLatLng(new LatLng(34.409400564638496, -119.86459124833344), 1.0));
        list.add(new WeightedLatLng(new LatLng(34.409395585662374, -119.86453995108603), 0.8));
        list.add(new WeightedLatLng(new LatLng(34.40935851994197, -119.86458420753479), 0.7));
        list.add(new WeightedLatLng(new LatLng(34.40931758166454, -119.86458085477352), 0.5));
        list.add(new WeightedLatLng(new LatLng(34.40929849556889, -119.86454866826534), 0.4));
        list.add(new WeightedLatLng(new LatLng(34.40934551927497, -119.86453525722028), 0.3));

        int[]colors = { Color.rgb(255, 0, 0), Color.rgb(102,255,0)};
        float[] startPoints = { 0.2f, 1f};
        Gradient gradient = new Gradient(colors, startPoints);

        provider = new HeatmapTileProvider.Builder().weightedData(list).radius(50).opacity(0.5).gradient(gradient).build();
        provider.setRadius(100);
        overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));

        double load_lat, load_lon;
        load_lat = 34.40935851994197;
        load_lon = -119.86458420753479;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(load_lat, load_lon), 20.8f));
    }

   /*
    private class SaveHeatMapAsync extends AsyncTask<String, String, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(HeatMapActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Saving Heat Map. . .");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {

            for(int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            iperfTask.buildHeatmap(heatmapPointList);
            iperfTask.makeFakeData();
            iperfTask.buildJSON(heatmap, routers, residence);
            iperfTask.PostRequest();
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Heat Map successfully saved!", Toast.LENGTH_SHORT).show();
        }
    }


    private class LoadHeatMapAsync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            testingDialog = new ProgressDialog(HeatMapActivity.this);
            testingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            testingDialog.setMessage("Test is in progress. . . ");
            testingDialog.setCancelable(true);
            testingDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            // getHeatMapPointsFromBackend();
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            HeatMapService();
            return null;
        }

        @Override
        protected void onPostExecute(String unused) {
            // progressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Heat Map successfully loaded!", Toast.LENGTH_SHORT).show();
        }

        private void HeatMapService() {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            String url = "http://cs1.smartrg.link:3000/heatmaps/1.json";
            try {
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject json = new JSONObject(response);
                                    String s = json.optString("created_at");
                                    Log.d("JSON IS HERE: ", s);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });
                requestQueue.add(stringRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void getHeatMapPointsFromBackend() {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            //String url = "http://cs1.smartrg.link:3000/heatmap_points/1.json";
            final ArrayList<WeightedLatLng> testPointList = new ArrayList<>();
            String url = "http://cs1.smartrg.link:3000/heatmap_points?id=1";
            try {
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    HeatMap heatMap = new HeatMap();
                                    JSONObject json = new JSONObject(response);
                                    JSONArray points = json.getJSONArray("heatmap_points");
                                    for (int i = 0; i < points.length(); i++) {
                                        JSONObject jsonObject = points.getJSONObject(i);
                                        String id = jsonObject.optString("id");
                                        String client_info = jsonObject.optString("client_info");
                                        Log.d("FFFFFFFFFFFFFFFFFFF", "id: " + id + " client: " + client_info);
                                        String lat = jsonObject.optString("latitude");
                                        String lon = jsonObject.optString("longitude");
                                        double latitude = Double.parseDouble(lat);
                                        double longitude = Double.parseDouble(lon);
                                        double intens = 0.5;
                                        mDynamicList.add(new WeightedLatLng(new LatLng(latitude, longitude), intens));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });
                requestQueue.add(stringRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void buildHeatmapPoint() {
            JSONObject heatmapPoint = new JSONObject();
            try {
                heatmapPoint.put("latitude", pin_latitude);
                heatmapPoint.put("longitude", pin_longitude);
                heatmapPoint.put("client_info", ""); //not determined
                heatmapPoint.put("upstream_bps", upstream);
                heatmapPoint.put("downstream_bps", downstream);
                heatmapPoint.put("jitter", jitter);
                heatmapPoint.put("client_rssi", rssi);
                heatmapPoint.put("router_rssi", 0.0); //filler-probably just need the one rssi
                heatmapPoint.put("num_active_clients", 1); //filler
                heatmapPoint.put("client_tx_speed", 0.0); //filler
                heatmapPoint.put("client_rx_speed", 0.0); //filler
                heatmapPoint.put("client_tx_retries", 0); //filler
                heatmapPoint.put("client_rx_retries", 0); //filler
                heatmapPoint.put("retransmits", retransmits);
                heatmapPoint.put("lost_percent", lost_percent);
            } catch (org.json.JSONException e) {
                Log.d("JSONERROR", "Could not convert to JSONObject in buildHeatmapPoint");
            }
            Log.d("HEATMAP BUILDING", "RSSI: " + rssi);
            //Log.d("JSON", heatmapPoint.toString());
            heatmapPointList.add(heatmapPoint);
        }

        public void buildHeatmap(ArrayList<JSONObject> heatmapPointList) {
            heatmap = new JSONObject();
            JSONArray heatmap_points = new JSONArray();
            for (JSONObject point : heatmapPointList) {
                heatmap_points.put(point);
            }
            try {
                heatmap.put("channel", ""); //filler
                heatmap.put("radio", ""); //filler
                heatmap.put("heatmap_points", heatmap_points);
            } catch (org.json.JSONException e) {
                Log.d("JSONERROR", "Could not convert to JSONObject in buildHeatmap");
            }
            //Log.d("JSON", heatmap.toString());
        }

        //TEMPORARY FUNCTION
        public void makeFakeData() {
            residence = new JSONObject();
            acct_num = "1234567";
            routers = new JSONArray();
            JSONObject router = new JSONObject();
            try {
                residence.put("address", "6745 Del Playa Dr");
                residence.put("account_number", acct_num);
                router.put("mac_address", mDeviceMac);
                router.put("serial_number", "12345678");
                router.put("router_model", "SR400ac");
                router.put("name", "name");
                router.put("latitude", router_lat);
                router.put("longitude", router_long);
            } catch (org.json.JSONException e) {
                Log.d("JSONERROR", "Could not convert to JSONObject in makeFakeData");
            }
            routers.put(router);
        }

        public void buildJSON(JSONObject heatmap, JSONArray routers, JSONObject residence) {
            save = new JSONObject();
            try {
                save.put("residence", residence);
                save.put("routers", routers);
                save.put("heatmap", heatmap);
            } catch (org.json.JSONException e) {
                Log.d("JSONERROR", "Could not convert to JSONObject in buildJSON");
            }
            Log.d("FULL JSON", save.toString());
        }

        public void PostRequest() {
            String url = "http://cs1.smartrg.link:3000/process_residence_information";
            try {
                URL object = new URL(url);
                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestMethod("POST");
                con.connect();
                DataOutputStream printout = new DataOutputStream(con.getOutputStream());
                printout.writeBytes(save.toString());
                printout.flush();
                printout.close();

                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    Log.d("HTTP", "HTTP_OK");
                } else {
                    Log.d("HTTP", "Bad response: " + HttpResult);
                }
            } catch (MalformedURLException e) {
                Log.d("Exception", "MalformedURLException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("Exception", "IOException");
                e.printStackTrace();
            }
        }
    }*/


}

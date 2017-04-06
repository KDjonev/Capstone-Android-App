package com.smartrg.smartrgapp.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.smartrg.smartrgapp.R;
import com.smartrg.smartrgapp.Views.ColorArcProgressBar;

public class SpeedTestActivity extends AppCompatActivity {

    private ColorArcProgressBar colorArcProgressBar;
    private WifiManager wifiManager;
    private String ipAddress;
    private IperfTask iperfTask;
    private TextView button_start, rssi, current_ip, current_link_speed;
    Runnable runnable;
    Handler handler = new Handler();

    private String ip;
    private boolean settingsChanged=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        rssi = (TextView)findViewById(R.id.speed_test_rssi_value);
        current_ip = (TextView)findViewById(R.id.speed_test_ip_value);
        current_link_speed = (TextView)findViewById(R.id.speed_test_linkspeed_value);
        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            String s = android.text.format.Formatter.formatIpAddress(wifiManager.getDhcpInfo().gateway);
            current_ip.setText(s);
        }
        colorArcProgressBar = (ColorArcProgressBar) findViewById(R.id.speed_meter);
        button_start = (TextView)findViewById(R.id.button_start);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initIperf();
                //fakeBadSpeedTest();
            }
        });
    }

    int [] numbers = {20, 19, 22, 25, 20, 18, 17, 19, 21, 20, 21, 18};

    public void fakeBadSpeedTest() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runnable = this;
                Random r = new Random();
                int randomActual = r.nextInt(60 - 50) + 50;
                int randomMax = r.nextInt(100 - 80) + 80;
                colorArcProgressBar.setCurrentValues(randomActual);
                colorArcProgressBar.setMaxValues(70);
                handler.postDelayed(this, 800);
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.speed_test, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                ip =data.getStringExtra("ip");
                settingsChanged = true;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(SpeedTestActivity.this, SpeedTestSettingsActivity.class);
            startActivityForResult(intent, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initIperf() {
        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        // get the device's ip address for use in iperf command
        if (wifiInfo != null) {
            ipAddress = android.text.format.Formatter.formatIpAddress(wifiManager.getDhcpInfo().gateway);
            Log.d("INIT_IPERF", "This is your IP: " + ipAddress);
            //ipAddress = "192.168.0.2";
            if (settingsChanged) {
                ipAddress = ip;
                Log.d("INIT_IPERF", "Settings changed! Your manually entered ip is: " + ipAddress);
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

            iperfTask = new IperfTask();
            iperfTask.execute();
            return;
        }

        iperfTask = new IperfTask();
        iperfTask.execute();
    }


    public class IperfTask extends AsyncTask<Void, String, String> {
        Process p = null;
        String command = "iperf3 -c " + ipAddress + " -R -t 20";
        int max;
        WifiInfo wifiInfo;
        BufferedReader reader;
        boolean isError = false;

        @Override
        protected void onPreExecute() {
            wifiInfo = wifiManager.getConnectionInfo();
            max = wifiInfo.getLinkSpeed();
            Log.d("ON_PRE_EXECUTE", "link speed: " + max);
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (!command.matches("(iperf3 )?((-[s,-server])|(-[c,-client] ([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))|(-[c,-client] \\w{1,63})|(-[h,-help]))(( -[f,-format] [bBkKmMgG])|(\\s)|( -[l,-len] \\d{1,5}[KM])|( -[B,-bind] \\w{1,63})|( -[r,-tradeoff])|( -[v,-version])|( -[N,-nodelay])|( -[T,-ttl] \\d{1,8})|( -[U,-single_udp])|( -[d,-dualtest])|( -[w,-window] \\d{1,5}[KM])|( -[n,-num] \\d{1,10}[KM])|( -[p,-port] \\d{1,5})|( -[L,-listenport] \\d{1,5})|( -[t,-time] \\d{1,8})|( -[i,-interval] \\d{1,4})|( -[u,-udp])|( -[R, -reverse]) | ( -[b,-bandwidth] \\d{1,20}[bBkKmMgG])|( -[m,-print_mss])|( -[P,-parallel] d{1,2})|( -[M,-mss] d{1,20}))*"))
            {
                Log.d("DO_IN_BACKGROUND", "Error! Invalid syntax for iperf3 command!");
                publishProgress("Error: invalid syntax \n\n");
                return null;
            }
            try {
                String[] commands = command.split(" ");
                List<String> commandList = new ArrayList<>(Arrays.asList(commands));
                commandList.add(0, "/data/data/com.smartrg.smartrgapp/iperf9");
                p = new ProcessBuilder().command(commandList).redirectErrorStream(true).start();
                reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                    publishProgress(output.toString());
                    output.delete(0, output.length());
                }
                reader.close();
                p.destroy();
            }
            catch (IOException e) {
                e.printStackTrace();
                Log.d("DO_IN_BACKGROUND", "Error! Failed retrieving iperf3 results");
            }
            return null;
        }

        @Override
        public void onProgressUpdate(String... strings) {
            String output = strings[0];
            Log.d("ON_PROGRESS_UPDATE", "Iperf output: " + output);
            String[] s = output.split("\\s+");
            ArrayList<String> outList = new ArrayList<>(Arrays.asList(s));
            for (int i = 0; i < outList.size(); i++) {
                // Log.d("ON_PROGRESS_UPDATE", "list: " + outList.get(i));

                if (outList.get(i).equals("-")) {
                    if (outList.get(i + 1).equals("-")) {
                        Log.d("ON_PROGRESS_UPDATE", "Should be end of iperf, should exit");
                        return;
                    }
                }
            }

            // check if any error occurs
            if (outList.contains("error")) {
                Log.d("ERROR", "Error found in iperf output! Exiting. . . ");
                isError = true;
                try {
                    reader.close();
                    p.destroy();
                    return;
                } catch (IOException e) {
                    Log.d("IOEXCEPTION", "IO exception thrown from onProgressUpdate in speedtest");
                    e.printStackTrace();
                }
            }

            // parse the mbits/sec value from iperf output
            if (outList.contains("sec")) {
                String st = outList.get(outList.size() - 2);
                Log.d("ON_PROGRESS_UPDATE", "string speed value: " + st);
                if (st.equals("iperf")) {
                    return;
                }
                //wifiInfo = wifiManager.getConnectionInfo();
                //max = wifiInfo.getLinkSpeed();
                int speed = 0;
                if (st.equals("")) {
                    Log.d("IPERF ERROR", "case where mbits/sec value from iperf is empty. shouldnt reallt have this happening");
                    return;
                }
                else {
                    speed = (int) Double.parseDouble(st);
                }

                Log.d("ON_PROGRESS_UPDATE", "speed: " + speed + " Mbits/sec" + ", max: " + max);
                if (speed > max) {
                    max = speed;
                }
                colorArcProgressBar.setCurrentValues(speed);
                colorArcProgressBar.setMaxValues(max);
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            rssi.setText(wifiInfo.getRssi() + " dBm");
            current_link_speed.setText(max + " Mbits/sec");

        }

        @Override
        public void onPostExecute(String result) {
            //The running process is destroyed and system resources are freed.
            if (p != null) {
                p.destroy();

                try {
                    p.waitFor();
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                if (isError) {
                    Toast.makeText(getApplicationContext(), "ERROR! Verify IP is correct and WiFi is connected and try again", Toast.LENGTH_LONG).show();
                }
                else Toast.makeText(getApplicationContext(), "Test has finished!", Toast.LENGTH_LONG).show();
                //  button.setText("TEST");
            }
        }
    }
}
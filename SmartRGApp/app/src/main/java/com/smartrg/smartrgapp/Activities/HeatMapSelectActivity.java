package com.smartrg.smartrgapp.Activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.smartrg.smartrgapp.R;

public class HeatMapSelectActivity extends AppCompatActivity {

    private JSONObject heatmap;
    private String acct_num;
    private JSONObject heatmap_and_points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextView button_new = (TextView)findViewById(R.id.button_create_new);
        TextView button_load = (TextView)findViewById(R.id.button_load);
        button_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HeatMapSelectActivity.this, HeatMapActivity.class);
                startActivity(intent);
            }
        });
        button_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HeatMapSelectActivity.this, HeatMapLoadActivity.class);
                startActivity(intent);
            }
        });



    }

    private class HeatmapSelectAsync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... urls) {
            acct_num = "1234567"; //get from user
            GetHeatmap(acct_num);

            for(int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        private void GetHeatmap(String acct_num) {
            String url="http://cs1.smartrg.link:3000/heatmaps/search?account_number=" + acct_num;
            StringBuilder result = new StringBuilder();
            try {
                URL object = new URL(url);
                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                InputStream in = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                int HttpResult=con.getResponseCode();
                if (HttpResult==HttpURLConnection.HTTP_OK){
                    Log.d("HTTP", "HTTP_OK");
                }else{
                    Log.d("HTTP", "Bad response: " + HttpResult);
                }
            } catch (MalformedURLException e) {
                Log.d("Exception", "MalformedURLException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("Exception", "IOException");
                e.printStackTrace();
            }
            //Log.d("RESULT", result.toString());
            try {
                JSONArray heatmap_array = new JSONArray(result.toString());
                heatmap = new JSONObject(heatmap_array.get(0).toString());
                //Log.d("JSON", heatmap.toString());
                GetHeatmapPoints(heatmap.getInt("id"));
            } catch (JSONException e) {
                Log.d("JSONERROR", "Could not convert to JSON in GetHeatmap");
            }
        }
        private void GetHeatmapPoints(int id) {
            String url1="http://cs1.smartrg.link:3000/heatmaps/heatmap_and_points?id=" + id;
            StringBuilder result = new StringBuilder();
            try {
                URL object = new URL(url1);
                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                InputStream in = new BufferedInputStream(con.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                int HttpResult=con.getResponseCode();
                if (HttpResult== HttpURLConnection.HTTP_OK){
                    Log.d("HTTP", "HTTP_OK");
                }else{
                    Log.d("HTTP", "Bad response: " + HttpResult);
                }
            } catch (MalformedURLException e) {
                Log.d("Exception", "MalformedURLException");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("Exception", "IOException");
                e.printStackTrace();
            }
            //Log.d("RESULT", result.toString());
            try {
                heatmap_and_points = new JSONObject(result.toString());
                Log.d("FULL_JSON", heatmap_and_points.toString());
                JSONArray points = heatmap_and_points.getJSONArray("heatmap_points");
                Log.d("JSON_POINTS", points.toString());
            } catch (JSONException e) {
                Log.d("JSONERROR", "Could not convert to JSON in GetHeatmapPoints");
            }
        }
    }


}
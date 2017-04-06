package com.smartrg.smartrgapp.Activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import com.smartrg.smartrgapp.Classes.LoadAdapter;
import com.smartrg.smartrgapp.Classes.HeatMap;
import com.smartrg.smartrgapp.R;

public class HeatMapLoadActivity extends AppCompatActivity {

    ArrayList<HeatMap> heatMaps;
    ListView listView;
    private static LoadAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map_load);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ListView listView = (ListView)findViewById(R.id.load_list_view);

        heatMaps = new ArrayList<>();
        heatMaps.add(new HeatMap("2/24/2017, 3:39PM"));
        heatMaps.add(new HeatMap("1/1/2017, 9:09AM"));
        heatMaps.add(new HeatMap("12/22/2016, 4:20PM"));
        // heatMaps.add(new HeatMap("12/22/2016"));
        // heatMaps.add(new HeatMap("12/22/2016"));
        // heatMaps.add(new HeatMap("12/22/2016"));
        //heatMaps.add(new HeatMap("12/22/2016"));
        //heatMaps.add(new HeatMap("12/22/2016"));


        adapter = new LoadAdapter(getApplicationContext(), heatMaps);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HeatMap heatMap = heatMaps.get(position);
                //Toast.makeText(getApplicationContext(), "Touched ", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HeatMapLoadActivity.this, HeatMapActivity.class);
                intent.putExtra("load", true);
                startActivity(intent);
            }
        });
    }

}
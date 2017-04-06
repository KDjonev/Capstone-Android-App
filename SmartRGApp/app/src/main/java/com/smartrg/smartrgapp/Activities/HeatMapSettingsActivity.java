package com.smartrg.smartrgapp.Activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.smartrg.smartrgapp.R;

/**
 * Created by brand_000 on 2/28/2017.
 */
public class HeatMapSettingsActivity extends PreferenceActivity {

    String ip;
    String bad_rssi;
    Boolean dash_r;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.heat_map_settings);
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        //ip  = PreferenceManager.getDefaultSharedPreferences(this).getString("ip", null);
        //bad_rssi  = PreferenceManager.getDefaultSharedPreferences(this).getString("bad_test", null);

        bar.setNavigationIcon(upArrow);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("ip", null);
                bad_rssi  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("bad_test", null);
                Log.d("String", "Saving settings value for ip:: " + ip);
                Log.d("String", "Saving settings value for bad_test rssi: "+ bad_rssi);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("ip", ip);
                returnIntent.putExtra("bad_test", bad_rssi);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

}
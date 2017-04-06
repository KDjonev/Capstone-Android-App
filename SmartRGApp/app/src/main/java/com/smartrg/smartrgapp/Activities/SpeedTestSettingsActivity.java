package com.smartrg.smartrgapp.Activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.smartrg.smartrgapp.R;

public class SpeedTestSettingsActivity extends PreferenceActivity {

    String ip;
    Boolean dash_r;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.speed_test_settings);
        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0); // insert at top
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        ip  = PreferenceManager.getDefaultSharedPreferences(this).getString("ip", null);
        dash_r = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("enable_reverse", true);

        ListPreference duration = (ListPreference) findPreference("duration");


        bar.setNavigationIcon(upArrow);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("String", "String: " +ip);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("ip", ip);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

}
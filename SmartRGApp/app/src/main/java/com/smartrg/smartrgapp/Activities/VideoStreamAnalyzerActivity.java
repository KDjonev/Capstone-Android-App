package com.smartrg.smartrgapp.Activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.smartrg.smartrgapp.R;

import java.util.Calendar;
import java.util.Date;

public class VideoStreamAnalyzerActivity extends AppCompatActivity {

    private GraphView graph;
    private GridLabelRenderer gridLabelRenderer;
    private Viewport viewport;
    private  LineGraphSeries<DataPoint> dataPoints;

    private Double graphLastXVal = 0d;
    private final Double delta_x = 0.1;

    private final Double minX = 0d, maxX = 10d, minY = -1d, maxY = 1d;

    private final Handler handler = new Handler();
    private Runnable timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_stream_analyzer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        /*grab the graph from the view */

        graph = (GraphView) findViewById(R.id.graph);
        gridLabelRenderer = graph.getGridLabelRenderer();
        viewport = graph.getViewport();

        /*Set Main Graph Options */
        graph.setTitle("SIN GRAPH");
        graph.setTitleColor(Color.WHITE);
        graph.setTitleTextSize(75);
        /*Grid options*/
        gridLabelRenderer.setGridColor(Color.WHITE);

        /*Set Axis Options */
        gridLabelRenderer.setHorizontalLabelsColor(Color.WHITE);
        gridLabelRenderer.setVerticalLabelsColor(Color.WHITE);

        /*Set up datapoints */
        dataPoints = new LineGraphSeries<>();
        dataPoints.setColor(Color.rgb(18, 189, 98));
        graph.addSeries(dataPoints);

        /*Viewport options */
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinX(minX);
        viewport.setMaxX(maxX);
        viewport.setMinY(minY);
        viewport.setMaxY(maxY);


    }

    @Override
    public void onResume() {
        super.onResume();

        timer = new Runnable() {
            @Override
            public void run() {
                Double y_value = Math.sin(graphLastXVal);

                //should it start to move the view
                Boolean scrollToEnd = graphLastXVal > maxX;

                dataPoints.appendData(new DataPoint(graphLastXVal, y_value), scrollToEnd, 100);
                graphLastXVal += delta_x;
                handler.postDelayed(this, 200);
            }

        };

        handler.postDelayed(timer, 1000);
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(timer);
        super.onPause();
    }

}

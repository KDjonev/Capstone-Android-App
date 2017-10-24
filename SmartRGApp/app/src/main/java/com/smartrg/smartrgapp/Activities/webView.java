package com.smartrg.smartrgapp.Activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.smartrg.smartrgapp.R;

public class webView extends AppCompatActivity {

    //initializing WebView
    WebView mwebView;
    //Assign the url to load
    String urlToLoad = "https://www.google.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Display Back Arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Init mwebView with WebView Window declared in activity_web_view
        mwebView = (WebView) findViewById(R.id.myWebView);
        WebSettings webSettings = mwebView.getSettings();
        //Tell the WebView to enable JavaScript execution.
        webSettings.setJavaScriptEnabled(true);
        //improve webView performance
        mwebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        mwebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mwebView.getSettings().setAppCacheEnabled(true);
        mwebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        //Attempt to store user creds for future use
        webSettings.setSavePassword(true);
        webSettings.setSaveFormData(true);
        webSettings.setEnableSmoothTransition(true);

        //force links open in webview only
        mwebView.setWebViewClient(new WebViewClient() {
            //Default fundtion is to open URL in browser
            //We have to override that function
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

        });
        //Load and open up the URL
        mwebView.loadUrl(urlToLoad);


    }

    // Inflate the menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.devices, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Create a new intent to take you back to main
        //Activity page
        Intent goHome = new Intent(getApplicationContext(), MainActivity.class);
        //Execute the goHome intent
        startActivityForResult(goHome, 0);
        return true;
    }

}

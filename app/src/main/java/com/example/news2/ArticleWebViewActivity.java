package com.example.news2;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ArticleWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_web_view);

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        WebView wv = (WebView) findViewById(R.id.webview);
        WebSettings settings = wv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

/* This enables in-WebView navigation
        WebViewClient wvc = new WebViewClient();
        wv.setWebViewClient(wvc);
*/

        wv.loadUrl(url);

        /* may not be practical, but leaving here for reference */
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
    }
}

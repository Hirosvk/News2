package com.example.news2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void changeText(View view){
       TextView textView = (TextView) findViewById(R.id.textView3);
       int newText;
       if(textView.getText().equals(getResources().getString(R.string.about_text))){
           newText = R.string.about_text_alt;
       } else {
           newText = R.string.about_text;
       }
       textView.setText(newText);
    }
}

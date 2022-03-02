package io.github.qzcsfchh.deeplinkgenerator.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import io.github.qzcsfchh.deeplinkgenerator.annotation.DeepLink;

@DeepLink(scheme = "native", host = "test")
public class MainActivity extends AppCompatActivity {
    private static int counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("第"+(++counter)+"个界面");
        findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent("secondActivity", Uri.parse("native://test2/mainActivity")));
            }
        });
    }
}
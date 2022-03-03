package io.github.qzcsfchh.deeplinkgenerator.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.qzcsfchh.deeplinkgenerator.annotation.DeepLink;

@DeepLink(exported = false, host = "test2", scheme = "native")
public class SecondActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("我是新的页面");
        Button button = new Button(this);
        button.setText("点击");
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        button.setLayoutParams(lp);
        setContentView(button);
        button.setOnClickListener(view ->{
            startActivity(new Intent().setData(Uri.parse("native://test2/mainActivity")));
        });
    }
}

package com.openproject.xiaojie.android_nfc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.openproject.xiaojie.android_nfc.nfc.NFCManager;

public class MainActivity extends AppCompatActivity {

    private TextView tvCardId;
    private int num = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NFCManager.getInstance().init(this);
        initView();
    }

    private void initView() {
        tvCardId = (TextView) findViewById(R.id.card_id);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String id = NFCManager.readID(intent);
        tvCardId.setText("第 " + num++ + " 次读取，id：" + id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NFCManager.getInstance().onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NFCManager.getInstance().onPause(this);
    }
}

package com.openproject.xiaojie.android_nfc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.openproject.xiaojie.android_nfc.nfc.NFCManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NFCManager.getInstance().init(this);
    }
}

package com.openproject.xiaojie.android_nfc.nfc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.util.Log;

/**
 * NFC 管理类
 * Created by xxj on 09/20.
 */
public class NFCManager {
    private static final NFCManager ourInstance = new NFCManager();

    private NfcAdapter nfcAdapter;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private Activity mActivity;
    private PendingIntent mPendingIntent;

    public static NFCManager getInstance() {
        return ourInstance;
    }

    private NFCManager() {
    }

    public void init(Context context) {
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        } catch (Exception e1) {
            e1.printStackTrace();
            nfcAdapter = null;
            return;
        }
        // 判断2
        if (nfcAdapter == null) {
            // 如果手机不支持NFC，或者NFC没有打开就直接返回
            Log.d(this.getClass().getName(), "手机不支持NFC功能！");
            return;
        }

        // 三种Activity NDEF_DISCOVERED ,TECH_DISCOVERED,TAG_DISCOVERED
        // 指明的先后顺序非常重要， 当Android设备检测到有NFC Tag靠近时，会根据Action申明的顺序给对应的Activity
        // 发送含NFC消息的 Intent.
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
//        IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        try {
            ndef.addDataType("*/*");
//            tech.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
//        mFilters = new IntentFilter[]{ndef, tech, tag};
        mFilters = new IntentFilter[]{ndef, tech};

        //我必须开喷了
        //Orz，这个mimeType 必须全满足才行了，直接 new String[]{...} 的意思是全满足，妈的我没见过
//        mTechLists = new String[][]{new String[]{Ndef.class.getName(), MifareClassic.class.getName(), NfcA.class.getName(), NfcB.class.getName(), NfcV.class.getName(), NfcF.class.getName()}};
        //我去，银行卡都刷出来了
//        mTechLists = new String[][]{{IsoDep.class.getName()}, {NfcA.class.getName()},{MifareClassic.class.getName()}};
        mTechLists = new String[][]{{NfcA.class.getName()},{MifareClassic.class.getName()}};
        if (!nfcAdapter.isEnabled()) {
            Log.e(this.getClass().getName(), "手机NFC功能没有打开！");
            enableDialog(context);
            return;
        }
    }

    private void enableDialog(final Context context) {
        AlertDialog.Builder ab = new AlertDialog.Builder(context);
        ab.setTitle("提醒");
        ab.setMessage("手机NFC开关未打开，是否现在去打开？");
        ab.setNeutralButton("否", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ab.setNegativeButton("是", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                dialog.dismiss();
            }
        });
        ab.create().show();
    }

    /**
     * 在要使用 nfc 的 activity 的生命周期中初始化
     */
    public void onResume(Activity activity) {
        if (nfcAdapter != null) {
            if (mActivity == null) {
                mActivity = activity;
                mPendingIntent = PendingIntent.getActivity(mActivity, 0,
                        new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                nfcAdapter.enableForegroundDispatch(activity, mPendingIntent, mFilters, mTechLists);
            } else {
                if (!mActivity.getClass().equals(activity.getClass())) {
                    mActivity = activity;
                    mPendingIntent = PendingIntent.getActivity(mActivity, 0,
                            new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
                    nfcAdapter.enableForegroundDispatch(activity, mPendingIntent, mFilters, mTechLists);
                }
            }
        }
    }

    /**
     * 在要使用 nfc 的 activity 的生命周期中初始化
     */
    public void onPause(Activity activity) {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
        mActivity = null;
    }

    public boolean isEnabled() {
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    /**
     * 读取卡的 id
     * 使用：
     *      在 activity 的 onNewIntent 方法中使用
     */
    public static String readID(Intent intent) {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String CardId = Utils.ByteArrayToHexString(tagFromIntent.getId());
        Log.e("tagFromIntent", "tagFromIntent" + tagFromIntent + "  intent.getAction():" + intent.getAction() + "        ID     " + CardId);
        return CardId;
    }
}

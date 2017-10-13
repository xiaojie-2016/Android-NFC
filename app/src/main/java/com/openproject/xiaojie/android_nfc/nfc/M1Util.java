package com.openproject.xiaojie.android_nfc.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import com.zhyd.wangpos.custom.MyToast;
import com.zhyd.wangpos.utils.UnitUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * M1 卡的读写工具类
 * Created by xxj on 09/20.
 */

public class M1Util {

    public static MifareClassic getMifareClassic(Intent intent) {
        MifareClassic mfc = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            mfc = MifareClassic.get(tagFromIntent);
        }
        return mfc;
    }

    public static String readID(Intent intent) {
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String CardId = UnitUtil.ByteArrayToHexString(tagFromIntent.getId());
        Log.e("tagFromIntent", "tagFromIntent" + tagFromIntent + "  intent.getAction():" + intent.getAction() + "        ID     " + CardId);
        return CardId;
    }

    public static void doRead(MifareClassic mfc) {
        boolean isDoingM1 = true;
        MifareClassCard mifareClassCard = null;
        try {
            if (mfc == null) {
                updateLogInfo("不支持读写M1卡，请检查设备类型是否支持M1卡读写");
                isDoingM1 = false;
                return;
            }
            if (!mfc.isConnected()) {
                //默认连接超时时间是1s,这里读卡寻卡一般要1-2s，所以要重新设置超时时间
                mfc.setTimeout(4000);
                mfc.connect();
            }
            boolean auth = false;
            int secCount = mfc.getSectorCount();
            mifareClassCard = new MifareClassCard(secCount);
            int bCount = 0;
            int bIndex = 0;
            for (int j = 0; j < secCount; j++) {
                MifareSector mifareSector = new MifareSector();
                mifareSector.sectorIndex = j;
                byte[] ks = new byte[6];
                Arrays.fill(ks, (byte) 0xFF);
                // auth = mfc.authenticateSectorWithKeyB(j, ks);
                // auth = mfc.authenticateSectorWithKeyA(j,
                // MifareClassic.KEY_DEFAULT);
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_DEFAULT);
                mifareSector.authorized = auth;
                if (auth) {
                    bCount = mfc.getBlockCountInSector(j);
                    bCount = Math.min(bCount, MifareSector.BLOCKCOUNT);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        MifareBlock mifareBlock = new MifareBlock(data);
                        mifareBlock.blockIndex = bIndex;
                        bIndex++;
                        mifareSector.blocks[i] = mifareBlock;
                    }
                    mifareClassCard.setSector(mifareSector.sectorIndex,
                            mifareSector);
                } else {
                    updateLogInfo("M1卡认证失败");
                    //认证失败，直接过滤当次读取
                    isDoingM1 = false;
                    return;
                }
            }
            String readStr = null;
            ArrayList<String> blockData = new ArrayList<>();
            int blockIndex = 0;
            for (int i = 0; i < secCount; i++) {
                //获取那个扇区的内容
                MifareSector mifareSector = mifareClassCard.getSector(i);
                for (int j = 0; j < MifareSector.BLOCKCOUNT; j++) {
                    //一个扇区有4个block块
                    MifareBlock mifareBlock = mifareSector.blocks[j];
                    byte[] data = mifareBlock.getData();
                    blockData.add("Block " + blockIndex++ + " : "
                            + Converter.getHexString(data, data.length));
                    System.out.println("值:" + new String(data));
                }
                //前面是获取所有内容，下面是获取第二扇区的第一个block块的内容
                if (i == 1) {
                    MifareBlock mifareBlock = mifareSector.blocks[0];
                    byte[] data = mifareBlock.getData();
                    readStr = new String(data);
                }
            }
            if (readStr == null) {
                updateLogInfo("读取M1卡内容失败");
            } else {
                updateLogInfo("读取内容信息：" + readStr);
            }

        } catch (IOException e) {
            Log.e("NFC 读卡", e.getLocalizedMessage());
            updateLogInfo("读取M1卡内容失败");
        } finally {
            if (mifareClassCard != null) {
                mifareClassCard.debugPrint();
            }
            isDoingM1 = false;
        }
    }

    /**
     * 读取 NFC 数据
     *
     * @param mfc       M1 卡
     * @param blockData 数据数组
     */
    public static void doRead(MifareClassic mfc, ArrayList<String> blockData) {
        MifareClassCard mifareClassCard = null;
        try {
            if (blockData == null) {
                blockData = new ArrayList<>();
            }
            if (mfc == null) {
                updateLogInfo("不支持读写M1卡，请检查设备类型是否支持M1卡读写");
                return;
            }
            if (!mfc.isConnected()) {
                //默认连接超时时间是1s,这里读卡寻卡一般要1-2s，所以要重新设置超时时间
                mfc.setTimeout(4000);
                mfc.connect();
            }
            boolean auth;
            int secCount = mfc.getSectorCount();    //获取扇区的数目
            mifareClassCard = new MifareClassCard(secCount);
            int bCount;     // 每个扇区块的数目
            int bIndex;     // 扇区角标
            for (int j = 0; j < secCount; j++) {
                MifareSector mifareSector = new MifareSector();
                mifareSector.sectorIndex = j;
                byte[] ks = new byte[6];
                Arrays.fill(ks, (byte) 0xFF);
                // auth = mfc.authenticateSectorWithKeyB(j, ks);
                // auth = mfc.authenticateSectorWithKeyA(j,
                // MifareClassic.KEY_DEFAULT);
                auth = mfc.authenticateSectorWithKeyA(j,
                        MifareClassic.KEY_DEFAULT);
                mifareSector.authorized = auth;
                if (auth) {
                    bCount = mfc.getBlockCountInSector(j);
                    bCount = Math.min(bCount, MifareSector.BLOCKCOUNT);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        MifareBlock mifareBlock = new MifareBlock(data);
                        mifareBlock.blockIndex = bIndex;
                        bIndex++;
                        mifareSector.blocks[i] = mifareBlock;
                    }
                    mifareClassCard.setSector(mifareSector.sectorIndex,
                            mifareSector);
                } else {
                    updateLogInfo("M1卡认证失败：扇区  " + j);
                    //认证失败，直接过滤当次读取
                    return;
                }
            }
            String readStr = null;
            int blockIndex = 0;
            //遍历每个扇区下的每个块
            for (int i = 0; i < secCount; i++) {    //扇区数 16
                //获取那个扇区的内容
                MifareSector mifareSector = mifareClassCard.getSector(i);
                for (int j = 0; j < MifareSector.BLOCKCOUNT; j++) {     //每个扇区4块
                    //一个扇区有4个block块
                    MifareBlock mifareBlock = mifareSector.blocks[j];
                    byte[] data = mifareBlock.getData();
                    //读取卡中的 M1 16进制数据
//                    blockData.add("Block " + blockIndex++ + " : " + Converter.getHexString(data, data.length));
                    blockData.add("Block " + blockIndex++ + " : " + new String(data));
                    System.out.println("值:" + new String(data));
                }
                //前面是获取所有内容，下面是获取第二扇区的第一个block块的内容
                if (i == 1) {
                    MifareBlock mifareBlock = mifareSector.blocks[0];
                    byte[] data = mifareBlock.getData();
                    readStr = new String(data);
                }
            }
            if (readStr == null) {
                updateLogInfo("读取M1卡内容失败");
            } else {
                updateLogInfo("读取内容信息：" + readStr);
            }

        } catch (IOException e) {
            Log.e("NFC 读卡", e.getLocalizedMessage());
            updateLogInfo("读取M1卡内容失败");
        } finally {
            if (mifareClassCard != null) {
                mifareClassCard.debugPrint();
                for (String blockDatum : blockData) {
                    Log.e("NFC ---------------数据 ", blockDatum);
                }
            }
        }
    }

    public static void doWrite(MifareClassic mfc, String writeStr) {
        try {
            if (mfc == null) {
                updateLogInfo("不支持读写M1卡，请检查是否为旺Pos 2S设备");
                return;
            }
            if (!mfc.isConnected()) {
                //默认连接超时时间是1s
                mfc.connect();
            }
            boolean auth;
            auth = mfc.authenticateSectorWithKeyA(1, MifareClassic.KEY_DEFAULT);
//            String writeStr = contentEt.getText().toString();
            if (auth) {
                if (writeStr != null && writeStr.length() != 0) {
                    /**
                     * 每个block只能存放16个字节bytes（不能多不能少） 每4个block为一个扇区，
                     * 0-3的第一个扇区存储M1卡出厂信息 4-7的第二个扇区可存储信息， 其中第7个block存放秘钥和权限信息：
                     * 内容为：000000000000FF078069FFFFFFFFFFFF(byte[]转换成的16进制字符串)
                     * 前面6个byte字节内容：000000000000为秘钥A 中间4个byte字节内容：FF078069为权限信息
                     * 后面6个byte字节内容：FFFFFFFFFFFF为秘钥B 第三至第N个扇区存储的内容和第二个扇区一样
                     */
                    byte[] bs = new byte[16];
                    byte[] bytes = writeStr.getBytes();
                    int len = bytes.length;
                    System.out.println("write len = " + len);
                    if (bytes == null || len > 16) {
                        updateLogInfo("写入内容内容过长,最长写入16字节长度");
                        return;
                    } else if (len < 16) {
                        // 长度小于16，需要补足
                        System.arraycopy(bytes, 0, bs, 0, bytes.length);
                    }

                    mfc.writeBlock(4, bs);
                    mfc.close();
                    updateLogInfo("M1卡成功写入内容：" + writeStr);
                } else {
                    updateLogInfo("请填写你要写入的内容");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            updateLogInfo("M1卡写入失败");
        } finally {
            try {
                if (mfc != null) {
                    mfc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void doWrite(MifareClassic mfc, Map<Integer,String> writedata) {
        try {
            if (mfc == null) {
                updateLogInfo("不支持读写M1卡，请检查是否为旺Pos 2S设备");
                return;
            }
            if (!mfc.isConnected()) {
                //默认连接超时时间是1s
                mfc.connect();
            }
            boolean auth;
            auth = mfc.authenticateSectorWithKeyA(1, MifareClassic.KEY_DEFAULT);
//            String writeStr = contentEt.getText().toString();
            if (auth) {
                if (writedata != null && writedata.size() != 0) {
                    /**
                     * 每个block只能存放16个字节bytes（不能多不能少） 每4个block为一个扇区，
                     * 0-3的第一个扇区存储M1卡出厂信息 4-7的第二个扇区可存储信息， 其中第7个block存放秘钥和权限信息：
                     * 内容为：000000000000FF078069FFFFFFFFFFFF(byte[]转换成的16进制字符串)
                     * 前面6个byte字节内容：000000000000为秘钥A 中间4个byte字节内容：FF078069为权限信息
                     * 后面6个byte字节内容：FFFFFFFFFFFF为秘钥B 第三至第N个扇区存储的内容和第二个扇区一样
                     */
                    Set<Map.Entry<Integer, String>> entries = writedata.entrySet();
                    for (Map.Entry<Integer, String> entry : entries) {
                        String writeStr = entry.getValue();
                        byte[] bs = new byte[16];
                        byte[] bytes = writeStr.getBytes();
                        int len = bytes.length;
                        System.out.println("write len = " + len);
                        if (bytes == null || len > 16) {
                            updateLogInfo("写入内容内容过长,最长写入16字节长度");
                            return;
                        } else if (len < 16) {
                            // 长度小于16，需要补足
                            System.arraycopy(bytes, 0, bs, 0, bytes.length);
                        }

                        mfc.writeBlock(entry.getKey(), bs);
                        updateLogInfo("M1卡成功写入内容：" + writeStr);
                    }
                    mfc.close();
                } else {
                    updateLogInfo("请填写你要写入的内容");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            updateLogInfo("M1卡写入失败");
        } finally {
            try {
                if (mfc != null) {
                    mfc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void updateLogInfo(String msg) {
        MyToast.showShort(msg);
        Log.e("NFC M1Util Print", msg);
    }
}

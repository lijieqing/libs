package com.hualee.lame;

import android.os.Bundle;
import android.util.Log;

public class LameDecode {
    static {
        System.loadLibrary("lame");
    }

    private static final String TAG = "LameDecode";

    public native static void nativeInit();

    private native static void nativeDecode(byte[] mp3Data, int dataLen, Bundle pcmInfo);

    public native static void nativeClose();

    public native static int mp3ToPcm(String mp3, String pcm);

    private static volatile boolean hasInit = false;

    public synchronized static void init() {
        Log.d(TAG, "LameDecode init start");
        nativeInit();
        Log.d(TAG, "LameDecode init end");
        hasInit = true;
    }

    public synchronized static void close() {
        if (hasInit) {
            Log.d(TAG, "LameDecode close start");
            nativeClose();
            Log.d(TAG, "LameDecode close end");
            hasInit = false;
        }
    }

    public static byte[] decode(byte[] mp3Data) {
        if (hasInit) {
            Log.d(TAG, "decode mp3Data len=" + mp3Data.length);
            Bundle data = new Bundle();
            nativeDecode(mp3Data, mp3Data.length, data);
            short[] shortArray = data.getShortArray("pcmData");
            if (shortArray != null) {
                return toByteArray(shortArray);
            } else {
                return new byte[0];
            }
        } else {
            throw new IllegalStateException("call LameDecode init() first");
        }
    }

    /**
     * byte数组转short数组
     *
     * @param src
     * @return
     */
    private short[] toShortArray(byte[] src) {

        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) ((src[i * 2] & 0xff) | ((src[2 * i + 1] & 0xff) << 8));
        }
        return dest;
    }

    /**
     * short数组转byte数组
     *
     * @param src
     * @return
     */
    private static byte[] toByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i]);
            dest[i * 2 + 1] = (byte) (src[i] >> 8);
        }
        return dest;
    }

}
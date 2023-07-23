package com.hualee.lame;


import androidx.core.util.Pair;

public class LameControl {
    public native static void close();

    public native static int encode(short[] buffer_l, short[] buffer_r, int samples, byte[] mp3buf);

    public native static int encodeInterleaved(short[] buffer, int samples, byte[] mp3buf);

    public native static int flush(byte[] mp3buf);

    public native static void init(int inSampleRate, int outChannel, int outSampleRate, int outBitrate, int quality);

    public native static int mp3ToPCM(String mp3File, String pcmFile);

    static {
        System.loadLibrary("lame");
    }

    public static short[] byteArray2ShortArray(byte[] data) {
        short[] retVal = new short[data.length / 2];
        for (int i = 0; i < retVal.length; i++)
            retVal[i] = (short) ((data[i * 2] & 0xff) | (data[i * 2 + 1] & 0xff) << 8);

        return retVal;
    }

    public static Pair<byte[], byte[]> splitPCM(byte[] inputData) {
        int numSamples = inputData.length / 4; // 双声道每个采样点占用4个字节
        int numChannels = 2; // 双声道
        int bytesPerSample = 2; // 16位采样精度，每个采样点占用2个字节

        // 创建左声道和右声道的字节数组
        byte[] leftChannelData = new byte[numSamples * bytesPerSample];
        byte[] rightChannelData = new byte[numSamples * bytesPerSample];

        // 对左声道进行降半处理，并将左右声道数据拆分到各自的字节数组中
        for (int i = 0; i < numSamples; i++) {
            // 计算每个采样点在字节数组中的偏移量
            int offset = i * numChannels * bytesPerSample;
            // 读取左声道数据
            short left = (short) ((inputData[offset] & 0xff) | (inputData[offset + 1] << 8));
            // 读取右声道数据
            short right = (short) ((inputData[offset + 2] & 0xff) | (inputData[offset + 3] << 8));
            // 将左右声道数据写入各自的字节数组中
            offset = i * bytesPerSample;
            leftChannelData[offset] = (byte) (left & 0xff);
            leftChannelData[offset + 1] = (byte) ((left >> 8) & 0xFF);
            rightChannelData[offset] = (byte) (right & 0xFF);
            rightChannelData[offset + 1] = (byte) ((right >> 8) & 0xFF);
        }
        return new Pair<>(leftChannelData, rightChannelData);
    }

    public static byte[] convertMono16ToStereo16(byte[] monoData) {
        int monoLength = monoData.length / 2;
        int stereoLength = monoLength * 4;
        byte[] stereoData = new byte[stereoLength];

        for (int i = 0; i < stereoLength; i += 4) {
            short sample = (short) ((monoData[i / 2] & 0xFF) | (monoData[i / 2 + 1] << 8));
            stereoData[i] = (byte) (sample & 0xFF);
            stereoData[i + 1] = (byte) (sample >> 8);
            stereoData[i + 2] = stereoData[i];
            stereoData[i + 3] = stereoData[i + 1];
        }

        return stereoData;
    }
}
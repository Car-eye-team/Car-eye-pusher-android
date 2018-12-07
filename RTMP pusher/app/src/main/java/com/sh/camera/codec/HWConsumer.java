/*  car eye 车辆管理平台 
 * car-eye管理平台   www.car-eye.cn
 * car-eye开源网址:  https://github.com/Car-eye-team
 * Copyright 2018
 */

package com.sh.camera.codec;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import org.push.hw.EncoderDebugger;
import org.push.hw.NV21Convertor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.push.push.Pusher;
import com.sh.camera.service.MainService;
import com.sh.camera.util.Constants;

/**
 * Created by apple on 2017/5/13.
 */
public class HWConsumer extends Thread implements VideoConsumer {

    public Muxer mMuxer;
    private static final String TAG = "CMD";
    private final Context mContext;
    private final Pusher mPusher;
    private int mHeight;
    private int mWidth;
    private MediaCodec mMediaCodec;
    private FFmpegNative m_ffmpeg;
    private long m_filter;
    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private NV21Convertor mVideoConverter;
    private volatile boolean mVideoStarted;
    private long  m_handle;
    public HWConsumer(Context context,Pusher pusher, long handle){
        mContext = context;
        mPusher = pusher;
        m_handle = handle;
        Log.d(TAG, " m_handle"+handle);
        m_filter = 0;
        if(Constants.ffmpeg == true) {
            m_ffmpeg = new FFmpegNative();
            m_ffmpeg.FFMPEG_init();
        }
    }
    @Override
    public void onVideoStart(int width, int height) throws IOException {
        this.mWidth = width;
        this.mHeight = height;
        startMediaCodec();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP + 1) {
            inputBuffers = outputBuffers = null;
        } else {
            inputBuffers = mMediaCodec.getInputBuffers();
            outputBuffers = mMediaCodec.getOutputBuffers();
        }
        start();
        mVideoStarted = true;
        if(Constants.filter == true && Constants.ffmpeg == true)
        {
              m_filter = m_ffmpeg.InitOSD(width,height, 10, 10,28, 0x00ff00, mContext.getFileStreamPath("msyh.ttf").getAbsolutePath(),"test");
              Log.d(TAG, "write data sucessful:"+mContext.getFileStreamPath("msyh.ttf").getAbsolutePath());
        }
    }
    final int millisPerframe = 1000 / 20;
    long lastPush = 0;

    @Override
    public int onVideo(byte[] data, int format) {
        if (!mVideoStarted)return 0;

        if(mPusher.CarEyePusherIsReadyRTMP(m_handle)==0)
        {
            Log.d(TAG, " onVideo not ready ");
            return 0;
            }
       data = mVideoConverter.convert(data);
        try {
            if (lastPush == 0) {
                lastPush = System.currentTimeMillis();
            }
            long time = System.currentTimeMillis() - lastPush;
            if (time >= 0) {
                time = millisPerframe - time;
                if (time > 0) Thread.sleep(time / 2);
            }
            int bufferIndex = mMediaCodec.dequeueInputBuffer(0);
            if (bufferIndex >= 0) {
                ByteBuffer buffer = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    buffer = mMediaCodec.getInputBuffer(bufferIndex);
                } else {
                    buffer = inputBuffers[bufferIndex];
                }
                buffer.clear();
                buffer.put(data);
                buffer.clear();
                mMediaCodec.queueInputBuffer(bufferIndex, 0, data.length, System.nanoTime() / 1000, MediaCodec.BUFFER_FLAG_KEY_FRAME);
            }
            if (time > 0) Thread.sleep(time / 2);
            lastPush = System.currentTimeMillis();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        return 0;
    }
    @SuppressLint("WrongConstant")
    @Override
    public void run(){
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = 0;
        byte[] mPpsSps = new byte[0];
        byte[]h264 = new byte[mWidth*mHeight*3/2];        

        do {
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                outputBuffers = mMediaCodec.getOutputBuffers();

            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
               /* EasyMuxer muxer = mMuxer;
                if (muxer != null) {
                    // should happen before receiving buffers, and should only happen once
                    MediaFormat newFormat = mMediaCodec.getOutputFormat();
                    muxer.addTrack(newFormat, true);
                }*/
            } else if (outputBufferIndex < 0) {
                // let's ignore it
            } else {
                ByteBuffer outputBuffer;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                } else {
                    outputBuffer = outputBuffers[outputBufferIndex];
                }
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);

                Muxer muxer = mMuxer;
                if (muxer != null) {
                    muxer.pumpStream(outputBuffer, bufferInfo, true);
                }
                boolean sync = false;
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {// sps
                    sync = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
                    if (!sync) {
                        byte[] temp = new byte[bufferInfo.size];
                        outputBuffer.get(temp);
                        mPpsSps = temp;
                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        continue;
                    } else {
                        mPpsSps = new byte[0];
                    }
                }
                sync |= (bufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) != 0;
                int len = mPpsSps.length + bufferInfo.size;
                if (len > h264.length){
                    h264 = new byte[len];
                }
                if (sync) {
                    System.arraycopy(mPpsSps, 0, h264, 0, mPpsSps.length);
                    outputBuffer.get(h264, mPpsSps.length, bufferInfo.size);
                     mPusher.SendBuffer_org( h264,  mPpsSps.length + bufferInfo.size, (bufferInfo.presentationTimeUs / 1000),0, m_handle);
                     }else{
                    outputBuffer.get(h264, 0, bufferInfo.size);
                    mPusher.SendBuffer_org( h264,  bufferInfo.size,  (bufferInfo.presentationTimeUs / 1000), 0, m_handle);
                }
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            }
        }
        while (mVideoStarted);
    }

    @Override
    public void onVideoStop() {
        do {
            mVideoStarted = false;
            try {
            	join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while (isAlive());
        if (mMediaCodec != null) {
            stopMediaCodec();
        }
        if(m_filter!=0)
        {
            m_ffmpeg.DelOSD(m_filter);
        }
    }


    /**
     * 初始化编码器
     */
    @SuppressLint("NewApi")
	private void startMediaCodec() throws IOException {
            /*
        SD (Low quality) SD (High quality) HD 720p
1 HD 1080p
1
Video resolution 320 x 240 px 720 x 480 px 1280 x 720 px 1920 x 1080 px
Video frame rate 20 fps 30 fps 30 fps 30 fps
Video bitrate 384 Kbps 2 Mbps 4 Mbps 10 Mbps
        */
          int framerate = 20;
//        if (width == 640 || height == 640) {
//            bitrate = 2000000;
//        } else if (width == 1280 || height == 1280) {
//            bitrate = 4000000;
//        } else {
//            bitrate = 2 * width * height;
//        }
        int bitrate = (int) (mWidth*mHeight*20*2*0.05f);
       // int bitrate = 2 * mWidth * mHeight / 3;
        
        EncoderDebugger debugger = EncoderDebugger.debug(mContext, mWidth, mHeight);
        mVideoConverter = debugger.getNV21Convertor();
        mMediaCodec =  MediaCodec.createEncoderByType("Video/AVC");
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", mWidth, mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, debugger.getEncoderColorFormat());
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        Bundle params = new Bundle();
        params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mMediaCodec.setParameters(params);
        }
    }

    /**
     * 停止编码并释放编码资源占用
     */
    private void stopMediaCodec() {
        mMediaCodec.stop();
        mMediaCodec.release();
    }

}

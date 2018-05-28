package com.sh.camera.codec;
public class FFmpegNative {
    static{
          System.loadLibrary("ffmpegjni");
    }
    //JNI api
    private native int CarEyeMPEGInit();
    private native int AddOSD(long handle,byte[] buffer, String txt);
    private native int CloseOSD(long handle);
    private native long OpenOSD(int width, int height, int startX, int startY, int fontsize, int color, String  filename, String  content);
    private native long CreateEncode(EncodeParamInfo param);
    private native int Encode(long handle,int flag, int pts, byte[] YUV, byte[]out);
    private native int ReleaseEncode(long handle);
    private native long CreateDecode(DecoderParamInfo param);
    private native int Decode(long handle,int flag, byte[] YUV, byte[]out);
    private native int ReleaseDecode(long handle);
    public int FFMPEG_init() { return CarEyeMPEGInit();}
    public long InitOSD(int width, int height, int startX, int startY, int fontsize, int color, String  filename, String  content)  {  return OpenOSD(width,height,startX,startY,fontsize,color, filename,content );}
    public int DelOSD(long handle) {  return CloseOSD(handle); }
    public int blendOSD(long handle,byte[] buffer,String txt)
    {
        return AddOSD(handle,buffer,txt);
    }
    public long InitEncode(EncodeParamInfo info)
    {
        return CreateEncode(info);
    }
    public int  EncodeData( long handle,int flag, int pts, byte[] buffer, byte[]out ){return Encode(handle,flag,pts, buffer,out );}
    public int  DestroyEncode(long handle)   { return ReleaseEncode(handle); }
    public long InitDecode(DecoderParamInfo info)
    {
        return CreateDecode(info);
    }
    public int  DecodeData( long handle,int flag,  byte[] buffer, byte[]out ){return Decode(handle,flag, buffer,out );}
    public int  DestroyDecode(long handle)   { return ReleaseEncode(handle); }

}

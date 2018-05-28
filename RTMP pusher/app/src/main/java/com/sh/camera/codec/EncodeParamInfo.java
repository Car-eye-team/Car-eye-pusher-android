package com.sh.camera.codec;

public class EncodeParamInfo  {
    int  InVedioType;
    int  OutVedioType;
    int  fps;
    int  width;
    int  height;
    int  VideoBitrate;
    int  InputAuidoType;
    int  OutAudioType;
    int  SampleRate;
    int  AudioBitrate;
    int  Encodetype;
};
/*
OutVedioType
InVedioType
InputAuidoType
OutAudioType

enum CarEye_CodecType
{
    // 不进行编码
    CAREYE_CODEC_NONE = 0,
    // H264编码
    CAREYE_CODEC_H264 = 0x1C,
    // H265编码
    CAREYE_CODEC_H265 = 0xAE,
    // MJPEG编码
    CAREYE_CODEC_MJPEG = 0x08,
    // MPEG4编码
    CAREYE_CODEC_MPEG4 = 0x0D,
    // AAC编码
    CAREYE_CODEC_AAC = 0x15002,
    // G711 Ulaw编码 对应FFMPEG中的AV_CODEC_ID_PCM_MULAW定义
    CAREYE_CODEC_G711U = 0x10006,
    // G711 Alaw编码 对应FFMPEG中的AV_CODEC_ID_PCM_ALAW定义
    CAREYE_CODEC_G711A = 0x10007,
    // G726编码 对应FFMPEG中的AV_CODEC_ID_ADPCM_G726定义
    CAREYE_CODEC_G726 = 0x1100B,
};






*/
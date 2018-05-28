package com.sh.camera.codec;

public class DecoderParamInfo
{
	// 视频编码格式
	int VCodec;
	// 音频解码格式，无音频则置为CAREYE_CODEC_NONE
	int ACodec;
	// 视频帧率(FPS)
	int	FramesPerSecond;
	// 视频宽度像素
	int	Width;
	// 视频的高度像素
	int Height;
	// 视频码率，越高视频越清楚，相应体积也越大 如：4000000
	int	VideoBitrate;
	// 音频采样率
	int	SampleRate;
	// 音频声道数
	int	Channels;
	// 音频采样精度 16位 8位等，库内部固定为16位
	int	BitsPerSample;
	// 音频比特率 如：64000，越高声音越清楚，相应体积也越大
	int	AudioBitrate;
};






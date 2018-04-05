car-push-android 是car-eye开源团队开发的一个推送库。demo程序分成RTSP和RTMP推送两个版本。是car-eye-device anddroid版的简化版本。支持实时监控和远程回放，接口跟Car-eye-device一样。更方便用户了解推送库：

Car-eye Camera 视频监控API RTSP版本：
说明:
public native int  CarEyeInitNetWork(Context context,String serverIP, String serverPort, String streamName, int videoformat, int fps,int audioformat, int audiochannel, int audiosamplerate);  

功能：初始化流媒体通道    
参数：context：应用句柄server  
IP:流媒体服务器的IP，可以是域名如www.car-eye.cn    
serverPort:RTSP流媒体的端口号  
streamName： 设备名：如手机号码13510671870 是设备的唯一标识   
videoformat： 视频格式，支持H264，265 MJPEG
fps： 帧频率  
audioformat： 音频格式支持AAC,G711,G726等   
public native int 	 CarEyePusherIsReady(int channel);   
功能：判断通道是否准备好，用来开启推送1：已经准备好，0还没准备好。
channel：通道号，
public native long   CarEyeSendBuffer(int time, byte[] data, int lenth, int type, int channel);

功能：填充流媒体数据到RTSP服务器 
参数time: 推送时间数，毫秒单位
data:多媒体数据   
lenth：数据长度    
type ：视频还是音频   
channel：推送的通道号   
public native int    CarEyeStopNativeFileRTSP(int channel);

功能：结束文件的推送   
参数:channel:通道号  
public native int    CarEyeStartNativeFileRTSPEX(Context context, String serverIP, String serverPort, String streamName,  String fileName,int start, int end);

功能：启动文件的推送 
参数:context：应用句柄
serverIP:流媒体服务器的IP，可以是域名如www.car-eye.cn     
serverPort:RTSP流媒体的端口号   
streamName： 设备名：如手机号码13510671870 是设备的唯一标识  
fileName：文件的绝对路径      
start：推送的文件相对偏移的开始时间     
end：  推送文件的相对偏移的结束时间     
返回：通道号   
public void  CarEyeCallBack(int channel, int Result)     
功能：推送文件的callback函数      
参数:channel：通道号     
Result:返回码，一般为结束或者错误码      

RTMP 推送接口跟RTSP一样，只是使用的库和URL的组织格式不一样。RTSP发送的URL为：rtsp://IP(或者域名):端口/设备编号?channel=1.sdp,
发送历史文件的URL:rtsp://IP(或者域名):端口/设备编号-channel=1.sdp. 可以使用car-eye-player或者其他的RTSP客户端进行视频播放
car-eye车辆管理平台：www.car-eye.cn; car-eye开源平台网址：https://github.com/Car-eye-team/Car-eye-device 有关car-eye 咨询可以加QQ群590411159。

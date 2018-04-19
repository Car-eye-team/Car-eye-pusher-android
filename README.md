# Car-eye-pusher-android        

car-push-android 是car-eye开源团队开发的一个推送库。demo程序分成RTSP和RTMP推送两个版本,是car-eye-device android版的简化版本,支持实时监控和远程回放，接口跟Car-eye-device一样。

## 库接口说明

接口原型： public native int  CarEyeInitNetWork(Context context,String serverIP, String serverPort, String streamName, int videoformat, int fps,int audioformat, int audiochannel, int audiosamplerate);    
接口功能：初始化流媒体通道  
参数说明：   
context：应用句柄   
server IP:流媒体服务器的IP，可以是域名如www.car-eye.cn  
serverPort:RTSP流媒体的端口号     
streamName： 设备名：如手机号码13510671870 是设备的唯一标识    
videoformat： 视频格式，支持H264，265 MJPEG    
fps： 帧频率  
audioformat： 音频格式支持AAC,G711,G726等    
返回：通道号

接口原型：public native int 	 CarEyePusherIsReady(int channel);     
接口功能：判断通道是否准备好，用来开启推送1：已经准备好，0还没准备好。   
参数说明：   
channel：通道号
返回：1 通道已经准备好 0 通道还没准备好

接口原型： public native long   CarEyeSendBuffer(long time, byte[] data, int lenth, int type, int channel);   
接口功能：填充流媒体数据到RTSP服务器 
参数说明：   
time: 推送时间数，毫秒单位
data:  多媒体数据   
lenth：数据长度    
type ：视频还是音频   
channel：推送的通道号  
返回：0 为发送数据成功  其他 为错误码


接口原型 public native int    CarEyeStopNativeFileRTSP(int channel);   
接口功能：结束文件的推送   
参数说明:   
channel:通道号  

接口原型： public native int   CarEyeStartNativeFileRTSPEX(Context context, String serverIP, String serverPort, String streamName,  String fileName,int start, int end);          

接口功能：启动文件的推送 
参数说明:context：应用句柄  
serverIP:流媒体服务器的IP，可以是域名如www.car-eye.cn     
serverPort:RTSP流媒体的端口号      
streamName： 设备名：如手机号码13510671870 是设备的唯一标识  
fileName：文件的绝对路径      
start：推送的文件相对偏移的开始时间     
end：  推送文件的相对偏移的结束时间     
返回：通道号（1-8） 其他为错误  

接口原型   public void  CarEyeCallBack(int channel, int Result)   
接口功能：推送文件的callback函数      
参数说明:    
channel：通道号     
Result:返回码，一般为结束或者错误码      


## 其他说明
RTMP 推送接口跟RTSP一样，只是使用的库和URL的组织格式不一样。RTSP发送的URL为：rtsp://IP(或者域名):端口/设备编号?channel=1.sdp,
发送历史文件的URL:rtsp://IP(或者域名):端口/设备编号-channel=1.sdp. RTMP实时播放地址是rtmp://IP(或者域名):端口/live/设备编号?channel=1,
发送历史文件的URL:rtmp://IP(或者域名):端口/live/设备编号-channel=1 ,可以使用car-eye-player或者其他的RTSP客户端进行视频播放. 

## 特别注意

请开手机的启悬浮窗权限


# 联系我们car-eye    

开源官方网址：www.car-eye.cn       

car-eye 流媒体平台网址：www.liveoss.com    

car-eye 技术官方邮箱: support@car-eye.cn    

car-eye技术交流QQ群: 590411159      

![](https://github.com/Car-eye-team/Car-eye-server/blob/master/car-server/doc/QQ.jpg)  



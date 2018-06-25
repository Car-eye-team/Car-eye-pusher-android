# Car-eye-pusher-android        

car-push-android 是car-eye开源团队开发的一个推送程序demo。程序分成RTSP和RTMP推送两个版本,是car-eye-device android版的简化版本,支持实时监控和远程回放，接口跟Car-eye-device一样。

## 工作原理

Car-eye-pusher RTSP 和RTMP 各个版本通过摄像头采集数据，将数据流发送到服务器，然后在通过car-eye-player或者第三方播放器进行播放
推流器的主要特点是稳定性好，延迟少，非常适合监控，医疗，教育等行业
![](https://github.com/Car-eye-team/Car-eye-pusher-android/blob/master/%E6%8E%A8%E6%B5%81/pusher-machine.png)

## 功能说明
目前支持的功能有：

* 录像和拍照

* 实时推送音视频数据

* 推送历史记录，精确到MP4文件内部毫秒级

* 推送服务断开重连

* 支持水印字幕



## 功能说明

目前支持的功能有：
* 录像和拍照
* 实时推送音视频数据
* 推送历史记录，精确到MP4文件内部毫秒级
* 推送服务断开重连
* 支持水印字幕

## 操作界面

![](https://github.com/Car-eye-team/Car-eye-pusher-android/blob/master/%E6%8E%A8%E6%B5%81/pusher.jpg)

图上的按钮功能依次为：拍照，录像，上传，打开历史记录，设置和返回


## 库接口说明

接口原型： public native int  CarEyeInitNetWork(RTMP/RTSP)(Context context,String serverIP, String serverPort, String streamName, int videoformat, int fps,int audioformat, int audiochannel, int audiosamplerate);    
接口功能：初始化流媒体通道     
参数说明：   
context：应用句柄     
server IP: 流媒体服务器的IP，可以是域名如www.car-eye.cn    
serverPort: 流媒体服务器的端口号       
streamName： 设备名：如手机号码13510671870 是设备的唯一标识        
videoformat：视频格式，支持H264，265 MJPEG       
fps： 帧频率     
audioformat： 音频格式支持AAC,G711,G726等        
返回：通道号   

接口原型：public native int 	 CarEyePusherIsReady(RTMP/RTSP)(int channel);     
接口功能：判断通道是否准备好，用来开启推送1：已经准备好，0还没准备好。   
参数说明：   
channel：通道号
返回：1 通道已经准备好 0 通道还没准备好

接口原型： public native long   CarEyeSendBuffer(RTMP/RTSP)(long time, byte[] data, int lenth, int type, int channel);   
接口功能：填充流媒体数据到服务器 
参数说明：   
time: 推送时间数，毫秒单位
data:  多媒体数据   
lenth：数据长度    
type ：视频还是音频   
channel：推送的通道号  
返回：0 为发送数据成功  其他 为错误码


接口原型 public native int    CarEyeStopNativeFile返回：通道号

接口原型：public native int 	 CarEyePusherIsReady(RTMP/RTSP)(int channel);     
接口功能：判断通道是否准备好，用来开启推送1：已经准备好，0还没准备好。   
参数说明：   
channel：通道号
返回：1 通道已经准备好 0 通道还没准备好

接口原型： public native long   CarEyeSendBuffer(RTMP/RTSP)(long time, byte[] data, int lenth, int type, int channel);   
接口功能：填充流媒体数据到服务器 
参数说明：   
time: 推送时间数，毫秒单位
data:  多媒体数据   
lenth：数据长度    
type ：视频还是音频      
channel：推送的通道号      
返回：0 为发送数据成功  其他 为错误码


接口原型 public native int    CarEyeStopNativeFile(RTMP/RTSP)(int channel);   

接口功能：结束文件的推送   
参数说明:   
channel:通道号  

接口原型： public native int   CarEyeStartNativeFile(RTSP/RTMP)EX(Context context, String serverIP, String serverPort, String streamName,  String fileName,int start, int end);          

接口功能：启动文件的推送 
参数说明:context：应用句柄  
serverIP:流媒体服务器的IP，可以是域名如www.car-eye.cn     
serverPort:流媒体的端口号      
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


# 联系我们     

car-eye开源官方网址：www.car-eye.cn       

car-eye 流媒体平台网址：www.liveoss.com    

car-eye 技术官方邮箱: support@car-eye.cn    

car-eye技术交流QQ群: 590411159      

![](https://github.com/Car-eye-team/Car-eye-server/blob/master/car-server/doc/QQ.jpg)  


CopyRight©  car-eye 开源团队 2018


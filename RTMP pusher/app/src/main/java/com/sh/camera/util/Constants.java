/*  car eye 车辆管理平台 
 * car-eye管理平台   www.car-eye.cn
 * car-eye开源网址:  https://github.com/Car-eye-team
 * Copyright
 */

package com.sh.camera.util;

import android.content.Context;
import android.content.Intent;


/**    
 *     
 * 项目名称：DSS_CAMERA    
 * 类名称：Constants    
 * 类描述：    
 * 创建人：Administrator    
 * 创建时间：2016年9月30日 下午2:03:55    
 * 修改人：Administrator    
 * 修改时间：2016年9月30日 下午2:03:55    
 * 修改备注：    
 * @version 1.0  
 *     
 */

public class Constants {

	//升级参数
	public static final String UPDATE_APK_TYPE = "322";
	public static final String UPDATE_APK_NAME = "SH_CAMERA.apk";
	public static final String UPDATE_APK_AK = "zhvvc2vuz2f0zte1mtm4ndy5mzg0mjm=";//正式
	public static final String BASE_URL = "http://39.108.246.45:801";
	public static final String UPDATE_IP = "39.108.246.45";
	public static final String UPDATE_PORT = "801";
	public static final String UPDATE_URL = "/api/loadNewVersion.action";
	/**上传视频宽度*/
	public static final int UPLOAD_VIDEO_WIDTH = 640;
	/**上传视频高度*/
	public static final int UPLOAD_VIDEO_HEIGHT = 480;
	/**录制视频宽度*/
	public static final int RECORD_VIDEO_WIDTH = 640;
	/**录制视频高度*/
	public static final int RECORD_VIDEO_HEIGHT = 480;
	//四路
	public static final int MAX_NUM_OF_CAMERAS = 1;
	/**服务器IP*/
	public static final String SERVER_IP = "www.car-eye.cn";
	/**handle apk 升级消息*/
	public static final int  MSG_APK_NEW = 1001;
	/**服务器端口*/
	public static final String SERVER_PORT = "10085";
	public static final String SERVER_ADDPORT = "10000";
	/**设备号*/
	public static final String STREAM_NAME = "13510671870";
	public static final String RTMP_APP = "live";
		
	public static String ip = "ip";
	public static String port = "port";
	public static String name = "name";
	public static String fps = "fps";
	public static String rule = "rule";
	public static String mode = "mode";
	public static String addPort = "add_port";
	public static String URL = "URL";
	public static String application = "application";
	public static int CAREYE_VCODE_H264 = 0x1C;
	public static int CAREYE_VCODE_H265 = 0x48323635;

	public static int CAREYE_ACODE_AAC = 0x15002;
	public static int CAREYE_ACODE_G711U = 0x10006;
	public static int CAREYE_ACODE_G711A = 0x10007;
	public static int CAREYE_ACODE_G726 = 0x10007;
	/**录像时长 分钟*/
	public static final int VIDEO_TIME = 10;//10
	/**SD卡路径*/
	public static String CAMERA_FILE_PATH = "";
	public static String CAMERA_FILE_DIR = "";
	public static String SD_CARD_PATH = "";
	public static String INNER_CARD_PATH = "";
	public static String SNAP_FILE_PATH = "";
	public static  boolean AudioRecord = true;
	public static  final boolean ExtPlayer = false;
	public static  final boolean ffmpeg = false;
	public static  final boolean filter = false;
	/**帧速率*/
	public static int FRAMERATE = 20;
	public static int MODE = 20;
	/**摄像头ID*/
	public static int[] CAMERA_ID = {0,1,5,6};
	/**录像状态 true录像中 false 未录像*/
	public static boolean CAMERA_RECORD[] = {false,false,false,false};
	public static final String Default_URL = "rtmp://www.car-eye.cn:10085/live/13510671870&channel=1";
	//public static final String Key = "k15gTHU$)g+$6H+1t1U1{I71t1TiVHT1uj)1shSHTHSH7ir$THRirG)GVg7GtGSGt$+$THRH+$)3B";
	public static final String Key = ")10h8IViuhtisItG+GVGrHRG+G8$UI8G)(uG6gsI8IqIR${i8I7${1u1UhR1+1q1+iti8I7Itiu63";
	/**
	 * 设置参数	 */
	public static void setParam(Context context){
		
		SD_CARD_PATH = "/mnt/extsd/";
		INNER_CARD_PATH = "/mnt/sdcard/";
		CAMERA_FILE_PATH = SD_CARD_PATH +"CarDVR/";
		SNAP_FILE_PATH = INNER_CARD_PATH+"CarDVR/";
		CAMERA_FILE_DIR = "/CarDVR/";			
		FRAMERATE = 20;
		CAMERA_ID[0] = 0;
		CAMERA_ID[1] = 3;
		CAMERA_ID[2] = 9;
		CAMERA_ID[3] = 8;		
		CAMERA_RECORD[0] = false;
		CAMERA_RECORD[1] = false;
		CAMERA_RECORD[2] = false;
		CAMERA_RECORD[3] = false;		
		try {
			//发送广播给设置的应用，传递视频路径
			Intent intent = new Intent("com.dss.camera.ACTION_VIDEO_PATH");
			intent.putExtra("EXTRA_VIDEO_PATH",CAMERA_FILE_PATH);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public static final String CAMERA_PATH ="Careye_pusher/";
	/**应用启动自动检测一次版本信息*/
	public static boolean checkVersion = true;
}

/*  car eye 车辆管理平台 
 * car-eye管理平台   www.car-eye.cn
 * car-eye开源网址:  https://github.com/Car-eye-team
 * Copyright
 */

package com.sh.camera.service;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import com.sh.RTMP_Pusher.R;
import com.sh.camera.FileActivity;
import com.sh.camera.ServerManager.ServerManager;
import com.sh.camera.SessionLinearLayout;
import com.sh.camera.SetActivity;
import com.sh.camera.codec.MediaCodecManager;
import com.sh.camera.util.AppLog;
import com.sh.camera.util.CameraFileUtil;
import com.sh.camera.util.CameraUtil;
import com.sh.camera.util.Constants;
import com.sh.camera.util.ExceptionUtil;
import com.sh.camera.version.VersionBiz;
import org.push.push.Pusher;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

@SuppressLint("NewApi")
@SuppressWarnings("unused")
public class MainService extends Service {
	private static final String TAG = "CMD";
	public static Context c;
	private static MainService instance;
	public static Context application;	
	LayoutInflater inflater;
	public static boolean isrun = false;
	/**主界面是否在最前端显示状态*/
	public static boolean isWindowViewShow = true;
	public static String ACTION = "com.dss.car.dvr";
	//控制悬浮窗全屏
	public static String FULLSCREEN = "fullscreen";
	//控制悬浮窗全屏且跳过一次窗口化指令
	public static String PASSWINFULL = "passwinfullscreen";
	//控制悬浮窗窗口化
	public static String WINDOW = "window";
	//控制悬浮窗最小化
	public static String MINIMIZE = "minimize";
	//控制预览界面重启
	public static String RESTART = "restart";
	//通知开始录像
	public static String STARTRECORDER = "startrecorder";
	//通知开始上传
	public static String STARTPUSH = "startpush";
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~初始化主要功能控件~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//主要显示控件
	public TextureView[] ttvs;
	private SurfaceTexture[] stHolder;
	//按钮容器
	private LinearLayout ly_bts;
	//摄像头数组
	public static Camera[] camera;
	static PreviewCallback[] preview;
	private MediaRecorder[] mrs;
	private String[] MrTempName;
	private ContentValues[] mCurrentVideoValues;
	public static SurfaceTextureListener[] stListener;
	//记录当前录制视屏的起点，未录制时-1；
	long recoTime = -1;
	private int[] ttvids = {R.id.tv_one};
	private ImageView btiv1,btiv2;
	private LinearLayout[] lys;
	private int[] lyids = {R.id.ly_1_0, R.id.ly_1_1};
	public static int[] StreamIndex;
	public static boolean clickLock = false;
	int framerate = Constants.FRAMERATE;
	int bitrate;
	public static Pusher mPusher;
	//通知结束录像
	public static String STOPRECORDER = "stoprecorder";
	//通知结束上传
	public static String STOPPUSH = "stoppush";
	BroadcastReceiver 	SYSBr;
	boolean usbcameraConnect = true;
	boolean sd_inject = false;
	private Button btn_app_minimize,btn_app_exit;
	private FrameLayout   inc_alertaui;
	private FrameLayout   inc_url;
	private TextView  text_url;
	// 获取本地application的对象
	private boolean isTabletDevice = true;
	public static MainService getInstance() {
		if (instance == null) {
			instance = new MainService();
		}
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		isTabletDevice = isTabletDevice(this);
		instance = this;
		c = MainService.this;
		application = getApplicationContext();
		mPusher = new Pusher();
		StreamIndex = new int[Constants.MAX_NUM_OF_CAMERAS];
		camera = new Camera[Constants.MAX_NUM_OF_CAMERAS];
		mrs = new MediaRecorder[Constants.MAX_NUM_OF_CAMERAS];
		MrTempName = new String[Constants.MAX_NUM_OF_CAMERAS];
		mCurrentVideoValues = new ContentValues[Constants.MAX_NUM_OF_CAMERAS];
		framerate = ServerManager.getInstance().getFramerate();
		CreateView();
		isrun = true;
		Constants.setParam(c);
		inflater = LayoutInflater.from(c);
		registerReceiver(br, filter);
		File youyuan = getFileStreamPath("msyh.ttf");
		if (!youyuan.exists()){
			AssetManager am = getAssets();
			try {
				InputStream is = am.open("msyh.ttf");
				FileOutputStream os = openFileOutput("msyh.ttf", MODE_PRIVATE);
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = is.read(buffer)) != -1) {
					os.write(buffer, 0, len);
				}
				os.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Log.d("CMD", "onCreate");
	}	

	public int  onStart(Intent intent,int flags,  int startId) {
		// TODO Auto-generated method stub
		//super.onStart(intent, startId);
		isrun = true;
		final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
		SYSBr = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) { 
				String action = intent.getAction();
				if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
				{
					inc_alertaui.setVisibility(View.VISIBLE);
					//MainService.getInstance().setWindowMin();
				}
			}
		};
		IntentFilter localIntentFilter = new IntentFilter();
		localIntentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		registerReceiver(SYSBr, localIntentFilter);
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		isrun = false;
		Log.d("CMD", "onDestroy");
		System.exit(0);
	};
	
	//pass一次window
	boolean passwin = false;
	IntentFilter filter = new IntentFilter(ACTION);
	BroadcastReceiver br = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent intent) {
			String type = intent.getStringExtra("type");			
			if(type.equals(MINIMIZE)){
				setWindowMin();
			}
			if(type.equals(FULLSCREEN)){
				setWindowFull();
			}
			if(type.equals(PASSWINFULL)){
				passwin = true;
				setWindowFull();
			}
			if(type.equals(RESTART)){
				passwin = true;
				restart();
			}
			if(type.equals(STARTRECORDER)){				
				int index = intent.getIntExtra("index", 0);
				/*if(!isRecording){
					click(R.id.bt_ly_2);
				}*/
				prepareRecorder(index, 1);
			}
			if(type.equals(STOPRECORDER)){
				if(isRecording){
					click(R.id.bt_ly_2);
				}
			}
			if(type.equals(STARTPUSH)){
				if(!isSC){
					click(R.id.bt_ly_3);
				}
			}
			if(type.equals(STOPPUSH)){
				if(isSC){
					click(R.id.bt_ly_3);
				}
			}
			if(type.equals("EXIT"))
			{
				StopCameraprocess();
				removeView();
				stopSelf();
			}
		}
	};

	private void restart() {
		isrun = true;
		Constants.setParam(MainService.getInstance());
		StopCameraprocess();
		removeView();
		addView();
	}
	//最小化
	void setWindowMin(){
		ismatch = true;
		ly_bts.setVisibility(view.VISIBLE);
		wmParams.x = 1;
		wmParams.y = 1;		
		wmParams.width = 1;
		wmParams.height = 1;
		//最小化到后台，需要设置LayoutParams.FLAG_NOT_FOCUSABLE，才能取消对返回键的拦截，并且移除layout
		wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | 
				LayoutParams.FLAG_NOT_FOCUSABLE | 
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
		if(layoutPoint != null && layoutPoint.isShown()){
			mWindowManager.removeView(layoutPoint);
		}
		mWindowManager.updateViewLayout(view, wmParams);
		isWindowViewShow = false;
	}	

	//最大化
	void setWindowFull(){
		ismatch = true;
		ly_bts.setVisibility(view.VISIBLE);
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.width =  WindowManager.LayoutParams.MATCH_PARENT;
		wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;		
		//最大化，不要设置LayoutParams.FLAG_NOT_FOCUSABLE，才能拦截返回键	
		wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | 
				WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
		mWindowManager.updateViewLayout(view, wmParams);

		//最大化，添加layout，才能拦截返回键，长宽为1，才不会挡住界面
		wmParams.width = 1;
		wmParams.height = 1;

		if(layoutPoint != null && layoutPoint.isShown()){
			mWindowManager.updateViewLayout(layoutPoint, wmParams);
		}else{
			mWindowManager.addView(layoutPoint, wmParams);
		}	
		isWindowViewShow = true;
		if(Constants.checkVersion){
			Constants.checkVersion = false;
			VersionBiz.doCheckVersionFirst(c, handler);
		}
	}

	//窗口化
	void setWindowWin(){
		ismatch = false;
		ly_bts.setVisibility(view.GONE);
		wmParams.x = 1;
		wmParams.y = 1;
		wmParams.width = 1;
		wmParams.height = 1;
		mWindowManager.updateViewLayout(view, wmParams);
	}

	boolean ismatch = false;
	LayoutParams wmParams;
	WindowManager mWindowManager;
	View view;
	// 一个点，叠加在Window中，用来监听返回键，最小化后移除，最大化时叠加到window中。
	SessionLinearLayout layoutPoint;
	// 触屏监听  
	float lastX, lastY;  
	int oldOffsetX, oldOffsetY;  
	private void CreateView() {
		mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		wmParams = new WindowManager.LayoutParams();
		wmParams.type = LayoutParams.TYPE_TOAST;
		//wmParams.type = LayoutParams.TYPE_PHONE;
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		addView();
	}
	private void addView() {
		if(inflater==null){
			inflater = LayoutInflater.from(c);
		}
		view = inflater.inflate(R.layout.activity_main, null);
		layoutPoint = (SessionLinearLayout) inflater.inflate(R.layout.layout_point, null);
		layoutPoint.setDispatchKeyEventListener(mDispatchKeyEventListener);
		initView();
		mWindowManager.addView(view, wmParams);
		view.measure(View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		setWindowFull();
	}
	public void StopCameraprocess()
	{
		if(isRecording){
			btiv1.setImageResource(R.drawable.a02);			
			stoprecorder(0,0);			
			isRecording = false;
		}
		if(isSC){
			stopSC();
		}		
		try {
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			stopMrs(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			closeCamera(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void removeView() {
		try {
			mWindowManager.removeView(view);
			view = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	/**
	 * 返回鍵监听
	 */
	private SessionLinearLayout.DispatchKeyEventListener mDispatchKeyEventListener = new SessionLinearLayout.DispatchKeyEventListener() {

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && isWindowViewShow) {
				//setWindowMin();
				inc_alertaui.setVisibility(View.VISIBLE);
				return true;
			}
			return false;
		}
	};

	public Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what==1001){
				boolean lock = false;
				Toast.makeText(c, "执行拍照成功", Toast.LENGTH_LONG * 1000).show();
				if(!lock){
					clickLock = false;
				}
			}
			if(msg.what==1003){
				boolean lock = false;
				Toast.makeText(c, "执行拍照失败", Toast.LENGTH_LONG * 1000).show();
				if(!lock){
					clickLock = false;
				}
			}
			//录制达到规定时长，重录
			if(msg.what==1002){
				clickLock = true;
				try {
					stoprecorder(0,0);					
					if(camera[0]!=null) startRecorder(0);					

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				recoTime = new Date().getTime();
				clickLock = false;
			}
			else if(msg.what==1022){
				postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						setWindowMin();
						Intent intent_set = new Intent(c, SetActivity.class);
						intent_set.putExtra("fromUpdateVersion", true);
						intent_set.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent_set);
					}
				}, 8000);
			}
		};
	};

	private void initView() {
		lys = new LinearLayout[2];
		for (int i = 0; i < lys.length; i++) {
			lys[i] = (LinearLayout) view.findViewById(lyids[i]);
		}
		ttvs = new TextureView[Constants.MAX_NUM_OF_CAMERAS];
		stHolder = new SurfaceTexture[Constants.MAX_NUM_OF_CAMERAS];		
		preview = new PreviewCallback[Constants.MAX_NUM_OF_CAMERAS];	
		stListener = new SurfaceTextureListener[Constants.MAX_NUM_OF_CAMERAS];
		if(isTabletDevice){
			ly_bts = (LinearLayout) view.findViewById(R.id.main_right_btly);
		}else{
			ly_bts = (LinearLayout) view.findViewById(R.id.main_bottom_btly);
		}
		ly_bts.setVisibility(View.VISIBLE);
		if(isTabletDevice){
			btiv1 = (ImageView) view.findViewById(R.id.imageView1);
			btiv2 = (ImageView) view.findViewById(R.id.imageView2);
		}else{
			btiv1 = (ImageView) view.findViewById(R.id.imageView1_bottom);
			btiv2 = (ImageView) view.findViewById(R.id.imageView2_bottom);
		}
		btn_app_minimize = (Button) view.findViewById(R.id.btn_app_minimize);
		btn_app_exit = (Button) view.findViewById(R.id.btn_app_exit);
		inc_alertaui = (FrameLayout) view.findViewById(R.id.inc_alertaui);
		//预览回调
		preview[0] = new PreviewCallback() {

			@Override
			public void onPreviewFrame(byte[] data, Camera camera1) {
				// TODO Auto-generated method stub
				MediaCodecManager.getInstance().onPreviewFrameUpload(data,0,camera[0]);
			}
		};
		//初始化摄像头、开始预览
		for (int i = 0; i < Constants.MAX_NUM_OF_CAMERAS; i++) {
			initPreview(i);
		}
		inc_url = (FrameLayout) view.findViewById(R.id.inc_url);
		text_url = (TextView) view.findViewById(R.id.text_url);
	}
	/**
	 * 初始化预览
	 * @param i
	 */	
	public void initPreview(int i){

		final int index = i;
		ttvs[i] = (TextureView) view.findViewById(ttvids[i]);
		stListener[i] = new SurfaceTextureListener() {
			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
			}
			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1, int arg2) {
			}
			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
				colseCamera(index);
				return true;
			}
			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1,int arg2) {
				stHolder[index] = arg0;	
				openCamera(index, 1);
			}
		};
		ttvs[i].setSurfaceTextureListener(stListener[i]);	
	}
	/**
	 * 关闭释放摄像头
	 * @param @i
	 */
	public void colseCamera(int index){
		try {			
			if(camera[index]!=null){
				camera[index].stopPreview();
				camera[index].release();
				camera[index] = null;
			}			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public void stopRecoders_SD_ERR()
	{		
		if(isRecording){
			btiv1.setImageResource(R.drawable.a02);			
			stoprecorder(0,0);			
			isRecording = false;
			sd_inject = true;
		}	
	}

	public void startRecoders_SD_ERR()
	{
		if(!isRecording && sd_inject){
			btiv1.setImageResource(R.drawable.a02);			
			prepareRecorder(0,1);			
			isRecording = true;
			sd_inject = false;
		}	
	}	
	/**
	 * 打开摄像头并预览
	 * @param @i
	 * @param type 1 正常启动  2 重启
	 */
	public void openCamera(int index,int type){
		try {
			boolean falg = true;			
			if(falg){
				try {
					camera[index] = Camera.open(index);
				} catch (Exception e) {
					e.printStackTrace();
					camera[index] = null;
				}	
				if (camera[index] != null) {
					try {
						camera[index].setPreviewTexture(stHolder[index]);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Camera.Parameters parameters = camera[index].getParameters();
					parameters.setPreviewSize(Constants.RECORD_VIDEO_WIDTH, Constants.RECORD_VIDEO_HEIGHT);
					camera[index].setErrorCallback(new CameraErrorCallback(index));					
					camera[index].setParameters(parameters);					
					camera[index].startPreview();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			AppLog.d(TAG, ExceptionUtil.getInfo(e));
		}
	}
	public static void TakePictureAll(int type)
	{
		if((camera[0]!= null) ) {
			MediaCodecManager.PrepareTakePicture();
			camera[0].setPreviewCallback(preview[0]);
		}else
		{
			Handler handler = MainService.getInstance().handler;
			if(handler != null){
				handler.sendMessage(handler.obtainMessage(1003));
			}
		}
	}
	//控制某路缩放
	boolean isgone = false;	
	void setAllView(){
		for (int i = 0; i < lys.length; i++) {
				lys[i].setVisibility(View.VISIBLE);
			}	
	}
	//释放摄像头资源
	public void closeCamera(int index){
		if(camera[index]!=null){
			camera[index].setPreviewCallback(null);
			camera[index].stopPreview();
			camera[index].release();
			camera[index] = null;
		}
	}
	//释放录像资源
	public void stopMrs(int index){
		if (mrs[index]!=null) { 
			mrs[index].stop(); 
			mrs[index].release(); 
			mrs[index] = null; 
		}
	}
	//右边六个按键的点击事件
	public static int picid = -1;
	boolean isRecording = false;
	boolean isSC = false;
	public void click(View v){
		click(v.getId());
	}
	public void click(int id){
		if(clickLock) return;
		switch (id) {
			case R.id.bt_ly_1://拍照
			case R.id.bt_ly_1_bottom://拍照
				//检查SD卡是否存在
				clickLock = true;
				TakePictureAll(1);
				clickLock = false;
				break;
			case R.id.bt_ly_2://录像
			case R.id.bt_ly_2_bottom://录像
				clickLock = true;
				//先判断是否录制中
				if (isRecording) {
					btiv1.setImageResource(R.drawable.a02);
					//遍历受控数组,停止录像
					stoprecorder(0, 0);
					isRecording = false;
				} else {
					btiv1.setImageResource(R.drawable.b02);
					//遍历受控数组,开始录像
					if (camera[0] != null) startRecorder(0);
					recoTime = new Date().getTime();
					isRecording = true;
				}
				clickLock = false;

				break;
			case R.id.bt_ly_3://上传
			case R.id.bt_ly_3_bottom://上传
				clickLock = true;
				if (isSC) {
					inc_url.setVisibility(View.GONE);
					stopSC();
				} else {
					//处理上传
					btiv2.setImageResource(R.drawable.b03);
					startVideoUpload2(ServerManager.getInstance().getIp(), ServerManager.getInstance().getPort(),ServerManager.getInstance().getapp(), ServerManager.getInstance().getStreamname(), 0);
					isSC = true;
				}
				clickLock = false;
				break;
			case R.id.bt_ly_4://回放
			case R.id.bt_ly_4_bottom://回放
				setWindowMin();
				Intent intent_file = new Intent(c, FileActivity.class);
				intent_file.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent_file);
				break;
			case R.id.bt_ly_5://设置
			case R.id.bt_ly_5_bottom://设置
				setWindowMin();
				Intent intent_set = new Intent(c, SetActivity.class);
				intent_set.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent_set);
				break;
			case R.id.bt_ly_6://退出
			case R.id.bt_ly_6_bottom://退出
				inc_alertaui.setVisibility(View.VISIBLE);
				break;
			case R.id.btn_app_minimize://閫€鍑?
				//娣诲姞涓€夋嫨绐?
				try {
					inc_alertaui.setVisibility(View.GONE);
					setWindowMin();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case R.id.btn_app_exit://閫€鍑?
				try {
						inc_alertaui.setVisibility(View.GONE);
						Intent intent = new Intent(ACTION);
						intent.putExtra("type", "EXIT");
						sendBroadcast(intent);
				}
					 catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				break;
			}

		}
	//结束上传
	private void stopSC() {
		btiv2.setImageResource(R.drawable.a03);

			stopVideoUpload(0);
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
		isSC = false;

	}
	public   void setCallback(int index, Camera camera)
	{
		camera.setPreviewCallback(preview[index]);	
	}	
	int m_index_channel;
	public void startVideoUpload2(final String ipstr,final String portstr, final String app, final String serialno, final int index){
		final int CameraId;
		CameraId = index+1;
		if(camera[index]==null) {
			return;
		}	
		if(serialno.equals(Constants.STREAM_NAME))
		{
			Toast.makeText(MainService.getInstance(), "请修改设备名", Toast.LENGTH_LONG * 1000).show();
		}
		try {

			if(camera[index]!=null){

			//初始化推流工具
					m_index_channel = mPusher.CarEyeInitNetWorkRTMP( getApplicationContext(),Constants.Key,ipstr, portstr, String.format("%s/%s&channel=%d",app,serialno,CameraId), Constants.CAREYE_VCODE_H264,20,Constants.CAREYE_ACODE_AAC,1,8000);
			//控制预览回调
					if(m_index_channel < 0)
					{
						Log.d("CMD", " init error, error number"+m_index_channel);
						//Toast.makeText(MainService.getInstance(), "链接服务器失败："+m_index_channel, 1000).show();
						return;
					}
					CameraUtil.VIDEO_UPLOAD[index] = true;
					StreamIndex[index] = m_index_channel;
					MediaCodecManager.getInstance().StartUpload(index,camera[index]);
					camera[index].setPreviewCallback(preview[index]);
				}
				inc_url.setVisibility(View.VISIBLE);
				//text_url.setText("rtmp://"+ipstr+":"+portstr+"/"+Constants.RTMP_APP+"/"+Constants.STREAM_NAME+"&channel="+CameraId);
				text_url.setText(ServerManager.getInstance().getURL());

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	/**
	 * 结束视频上传
	 * @param i
	*/
	public void stopVideoUpload(int i){
		try {
			Log.d("SERVICE", " stop upload"+i);
			CameraUtil.VIDEO_UPLOAD[i] = false;
			if(camera[i]!=null){				
				MediaCodecManager.getInstance().StopUpload(i);
				camera[i].setPreviewCallback(null);
				mPusher.stopPush(StreamIndex[i]);	
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	/**
	 * 准备录像
	 * @param index
	 */
	public void prepareRecorder(int index,int type){
		try {
			btiv1.setImageResource(R.drawable.b02);
			if(type == 1){
				recoTime = new Date().getTime();
			}
			isRecording = true;
			startRecorder(0);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	/**
	 * 开始录像
	 * @param @index
	 */
	private  String convertOutputFormatToFileExt(int outputFileFormat) {
		if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
			return ".mp4";
		}
		return ".3gp";
	}

	public static String convertOutputFormatToMimeType(int outputFileFormat) {
		if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
			return "video/mp4";
		}
		return "video/3gpp";
	}

	public static void addVideo(final String path,final ContentValues values)
	{
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				try
				{
					String finalName  = values.getAsString(Video.Media.DATA);
					new File(path).renameTo(new File(finalName));
				}
				catch(Exception e)
				{
				}
			}
		});
	}
	private void generateVideoFilename(int index,  int outputFileFormat) {
		File mFile;
		try {
			mFile = CameraFileUtil.CreateText(CameraFileUtil.getRootFilePath() + Constants.CAMERA_PATH);
			String title = String.format("%d-%d", index+1, new Date().getTime()) ;
			String filename = title + convertOutputFormatToFileExt(outputFileFormat);
			File file = new File(mFile,filename);
			String path = file.getPath();
			String tmpPath = path + ".tmp";
			String mime = convertOutputFormatToMimeType(outputFileFormat);
			mCurrentVideoValues[index] = new ContentValues(4);
			mCurrentVideoValues[index].put(Video.Media.TITLE, title);
			mCurrentVideoValues[index].put(Video.Media.DISPLAY_NAME, filename);
			mCurrentVideoValues[index].put(Video.Media.MIME_TYPE, mime);
			mCurrentVideoValues[index].put(Video.Media.DATA, path);
			MrTempName[index] = tmpPath;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void startRecorder(int index){
		try { 
			camera[index].unlock();
			mrs[index] = new MediaRecorder(); 
			mrs[index].reset();
			mrs[index].setCamera(camera[index]);
			mrs[index].setVideoSource(MediaRecorder.VideoSource.CAMERA);
			String starttime;
			String endtime;
			mrs[index].setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); 
			mrs[index].setVideoEncoder(MediaRecorder.VideoEncoder.H264); 			
			mrs[index].setVideoSize(Constants.RECORD_VIDEO_WIDTH, Constants.RECORD_VIDEO_HEIGHT); 
			mrs[index].setVideoEncodingBitRate(3*Constants.RECORD_VIDEO_WIDTH*Constants.RECORD_VIDEO_HEIGHT/2);			
			mrs[index].setVideoFrameRate(framerate); 
			mrs[index].setOnErrorListener(new MediaRecorderErrorListener(index));		
			generateVideoFilename(index, MediaRecorder.OutputFormat.MPEG_4 );
			mrs[index].setOutputFile( MrTempName[index]);			
			Log.d("CMD", "generate filename"+MrTempName[index]);	
			mrs[index].prepare(); 
			mrs[index].start(); 
			Constants.CAMERA_RECORD[index] = true;
		} catch (Exception e) { 
			e.printStackTrace(); 
		}
	}
	//根据摄像头id停止录像
	void stoprecorder(int index,int i){
		try {
			if(camera[0]!=null){
				recoTime = -1;
				if (mrs[index] != null) { 
					try {
						mrs[index].setOnErrorListener(null);  
						mrs[index].setOnInfoListener(null);    
						mrs[index].setPreviewDisplay(null);  
						mrs[index].stop(); 
					} catch (Exception e) {
						e.printStackTrace();
					}
					Log.d("CMD", String.format(" stop record:"));
					mrs[index].release(); 
					mrs[index] = null; 
					camera[index].lock();
					addVideo(MrTempName[index], mCurrentVideoValues[index]);
				} 
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public class CameraErrorCallback implements android.hardware.Camera.ErrorCallback {
		private int mCameraId = -1;
		private Object switchLock = new Object();
		public CameraErrorCallback(int cameraId) {
			mCameraId = cameraId;
		}
		@Override
		public void onError(int error, android.hardware.Camera camera) {
			if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {        //底层camera实例挂掉了
				// We are not sure about the current state of the app (in preview or snapshot or recording). Closing the app is better than creating a new Camera object.                                 
				//如果是mipi挂掉了，usb断电，然后杀掉自己所在的进程，监听心跳广播启动自己
				//usb camera挂掉了，先断电然后再上电
				//Toast.makeText(c, "摄像头：error="+error+",mCameraId="+mCameraId, Toast.LENGTH_LONG).show();
			}
			Log.d("	error!!!", "code!!!!:"+error);	
		}
	}

	private class MediaRecorderErrorListener implements MediaRecorder.OnErrorListener {                 //底层mediaRecorder上报错误信息
		private int mCameraId = -1;
		public MediaRecorderErrorListener(int cameraId) {
			mCameraId = cameraId;
		}    
		@Override
		public void onError(MediaRecorder mr, int what, int extra) {                              
			//先停止掉录制
			if(what == MediaRecorder.MEDIA_ERROR_SERVER_DIED){      //MediaRecorder.MEDIA_ERROR_SERVER_DIED--100，说明mediaService死了，需要释放MediaRecorder
				btiv1.setImageResource(R.drawable.a02);
				//遍历受控数组，停止录像
				stoprecorder(0,0);
				openCamera(0,1);
				isRecording = false;
			}

		}
	}
	/**
	 * 判断是否平板设备
	 * @param context
	 * @return true:平板,false:手机
	 */
	private boolean isTabletDevice(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
				Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
}

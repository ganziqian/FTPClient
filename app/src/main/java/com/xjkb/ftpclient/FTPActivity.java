package com.xjkb.ftpclient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.xjkb.ftpclient.fore.FTP;
import com.xjkb.ftpclient.utils.ShowDiaUtils;
import com.xjkb.ftpclient.utils.WifiHelper;

/**
 * 测试Activity.
 * 
 * @author cui_tao
 *
 */
public class FTPActivity extends Activity implements OnClickListener{

	/**
	 * 标签.
	 */
	static final String TAG = "FTPActivity";

	/**
	 * FTP.
	 */
	private FTP ftp;

	/**
	 * FTP文件集合.
	 */
	private List<FTPFile> remoteFile = new ArrayList<FTPFile>();


	/**
	 * 本地根目录.
	 */
	private static final String LOCAL_PATH = "/mnt/sdcard";

	/**
	 * 当前选中项.
	 */
	private int position = -1;

	/**
	 * ListView.
	 */
	private ListView listMain;


	private int isduan=1;

	/**
	 * 下载按钮.
	 */
	private Button buttonDownload;

	/**
	 * 断开连接按钮.
	 */
	private Button buttonClose;

	/**
	 * 服务器名.
	 */
	private String hostName;

	/**
	 * 用户名.
	 */
	private String userName;

	/**
	 * 密码.
	 */
	private String password;

	private TextView jiazaiTv;

	private Button downallBtn;

	private RemoteAdapter adapter;


	private int iia=0;

	private String downName="";


	private String xianstr="";

	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);


			int hh=msg.what;
			if(dialog!=null){
				if(hh!=10){
					dialog.dismiss();
				}
			}

			switch (hh){

				case 1:
					listMain.setAdapter(adapter);
					jiazaiTv.setText("");
					jiazaiTv.setVisibility(View.GONE);
					break;
				//单个
				case 2:
					Log.e("------>","下载完成");
					Log.e("-=-=-=-",downName);
					ShowDiaUtils.showDeatial("下载成功",FTPActivity.this);


					buttonDownload.setBackgroundColor(getResources().getColor(R.color.bluebtn));
					buttonDownload.setClickable(true);
					break;
				case 3:
					ShowDiaUtils.showDeatial("下载失败",FTPActivity.this);
					buttonDownload.setBackgroundColor(getResources().getColor(R.color.bluebtn));
					buttonDownload.setClickable(true);
					break;


				case 4:
					Toast.makeText(FTPActivity.this,"FTP连接失败",Toast.LENGTH_SHORT).show();
					jiazaiTv.setText("");
					jiazaiTv.setVisibility(View.GONE);
					break;

				//全部
				case 5:
					ShowDiaUtils.showDeatial("下载成功",FTPActivity.this);
					downallBtn.setBackgroundColor(getResources().getColor(R.color.bluebtn));
					downallBtn.setClickable(true);
					Log.e("------>","下载完成");
					break;
				case 6:

					ShowDiaUtils.showDeatial("下载失败",FTPActivity.this);
					downallBtn.setBackgroundColor(getResources().getColor(R.color.bluebtn));
					downallBtn.setClickable(true);
					break;
				case 7:
					++iia;
					downAll();


					break;
				case 10:
					tv_dialoginfo.setText("正在下载..."+xianstr);
					break;



			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ftp_main);
		// 初始化视图
		initView();
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 关闭服务
		new Thread(){
			@Override
			public void run() {
				super.run();
				try {

					if(isduan==1) {
						ftp.closeConnect();
					}
				} catch (IOException e) {
					Log.e("eeeeeeee",e.toString());
					e.printStackTrace();
				}
			}
		}.start();
	}



	/**
	 * 初始化视图.
	 */
	private void initView() {
		// 初始化控件
		listMain = (ListView) findViewById(R.id.list);
		buttonDownload = (Button) findViewById(R.id.button_download);
	//	buttonUploading = (Button) findViewById(R.id.button_uploading);
		buttonClose = (Button) findViewById(R.id.button_close);
		// 获取登录信息
		loginConfig();
		initDialog(this,"正在准备下载...");

		jiazaiTv= (TextView) findViewById(R.id.ftp_jiazai_tv);


		// ListView单击
		listMain.setOnItemClickListener(listMainItemClick);
		// ListView选中项改变
		listMain.setOnItemSelectedListener(listMainItemSelected);


		downallBtn= (Button) findViewById(R.id.button_dowmall);
		// 下载
		buttonDownload.setOnClickListener(this);
		downallBtn.setOnClickListener(this);
		// 断开FTP服务
		buttonClose.setOnClickListener(this);
		// 加载FTP视图
		jiazaiTv.setText("正在连接服务器,请稍后.....");
		new Thread(){
			@Override
			public void run() {
				super.run();
				loadRemoteView();
			}
		}.start();












	}



	/**
	 * 获取登录信息.
	 */
	private void loginConfig() {
		Intent intent = getIntent();
		hostName = intent.getStringExtra("hostName");
		userName = intent.getStringExtra("userName");
		password = intent.getStringExtra("password");
	}







	/**
	 * 加载FTP视图.
	 */
	private void loadRemoteView() {
		try {
			if (ftp != null) {
				// 关闭FTP服务
				ftp.closeConnect();
			}
			// 初始化FTP
			Log.e("hostName", hostName);
			Log.e("userName", userName);
			Log.e("passwrod", password);
			ftp = new FTP(hostName, userName, password);
			// 打开FTP服务
			ftp.openConnect();
			// 初始化FTP列表

			// 更改控件可见

			buttonDownload.setVisibility(Button.VISIBLE);
			//buttonUploading.setVisibility(Button.INVISIBLE);
			// 加载FTP列表
			remoteFile.addAll(ftp.listFiles(FTP.REMOTE_PATH));
			// FTP列表适配器
			adapter = new RemoteAdapter(this, remoteFile,"");
			// 加载数据到ListView
		    handler.sendEmptyMessage(1);
		} catch (IOException e) {
			handler.sendEmptyMessage(4);
			e.printStackTrace();
		}

	}


	/**
	 * ListView单击事件.
	 */
	private OnItemClickListener listMainItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int location,
				long arg3) {
			// 获取当前选中项
			FTPActivity.this.position = location;



			for (int i=0;i<remoteFile.size();i++){
				//View v=arg0.getChildAt(i);
				if(i==position){
					adapter.setStr(Util.convertString(remoteFile.get(position).getName(), "GBK"));
					adapter.notifyDataSetChanged();
					break;
				//	v.setBackgroundResource(R.color.colorAccent);//点击的Item项背景设置
				}/*else {
					v.setBackgroundResource(R.color.wite);//点击的Item项背景设置
				}*/
			}



			//Toast.makeText(FTPActivity.this,"您选中了第"+(position+1)+"项",Toast.LENGTH_SHORT).show();
			buttonDownload.setText("下载第"+(position+1)+"项");
		}
	};

	/**
	 * ListView选中项改变事件.
	 */
	private OnItemSelectedListener listMainItemSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View view,
				int location, long arg3) {

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}

	};



	/**
	 * 下载.
	 */
	private void downOne(){
		if(remoteFile.size()==0){
			Toast.makeText(FTPActivity.this,"没有可下载文件",Toast.LENGTH_SHORT).show();
			return;
		}

		if(position==-1){
			Toast.makeText(FTPActivity.this,"请选择需要下载的文件或文件目录",Toast.LENGTH_SHORT).show();
			return;
		}

		//Toast.makeText(FTPActivity.this,"开始下载",Toast.LENGTH_SHORT).show();
		buttonDownload.setBackgroundColor(getResources().getColor(R.color.grey));
		buttonDownload.setClickable(false);

		if(dialog!=null){
			if(!dialog.isShowing()){
				dialog.show();
			}
		}
		new AsyncTask<String,String,String>(){

			@Override
			protected String doInBackground(String... params) {
				// 下载
				try {
					Result result = null;

					Log.e("------>","开始");
					downName= remoteFile.get(position).getName();
					result = ftp.download(FTP.REMOTE_PATH, remoteFile.get(position).getName(), LOCAL_PATH, new FTP.DownNameLinten() {
						@Override
						public void oneName(String path) {
							xianstr=path;
							handler.sendEmptyMessage(10);
						}
					});

				//	ftp.downFileOrDir(remoteFile.get(position).getName(), LOCAL_PATH,1);
					if(result!=null) {
						if (result.isSucceed()) {
							Log.e(TAG, "download ok...time:" + result.getTime()
									+ " and size:" + result.getResponse());
							/*Toast.makeText(FTPActivity.this, "下载成功", Toast.LENGTH_SHORT)
									.show();*/
							handler.sendEmptyMessage(2);
						} else {
							Log.e(TAG, "download fail");
							/*Toast.makeText(FTPActivity.this, "下载失败", Toast.LENGTH_SHORT)
									.show();*/
							handler.sendEmptyMessage(3);
						}
					}else {
						handler.sendEmptyMessage(3);
					}

				} catch (IOException e) {
					e.printStackTrace();
					Log.e("eeeeee--->",e.toString());
				}

				return null;
			}
		}.execute();

/*
		new Thread(){
			@Override
			public void run() {
				super.run();

			}
		}.start();*/

	}

	/**
	 * 下载全部
	 */

	private void downAll(){
		if(remoteFile.size()==0){
			Toast.makeText(FTPActivity.this,"没有可下载文件",Toast.LENGTH_SHORT).show();
			return;
		}

		//Toast.makeText(FTPActivity.this,"开始下载",Toast.LENGTH_SHORT).show();

		if(dialog!=null){
			if(!dialog.isShowing()){
				dialog.show();
			}
		}
		Log.e("------>","开始");
		downallBtn.setBackgroundColor(getResources().getColor(R.color.grey));
		downallBtn.setClickable(false);
		new Thread(){
			@Override
			public void run() {
				super.run();
				// 下载
				try {
					Result result = null;
					//for (int i=0;i<remoteFile.size();i++){
					result = ftp.download(FTP.REMOTE_PATH, "", LOCAL_PATH, new FTP.DownNameLinten() {
						@Override
						public void oneName(String path) {
							xianstr=path;
							handler.sendEmptyMessage(10);
						}
					});
				//	}
					//ShowDiaUtils.setTitle(remoteFile.get(iia).getName());
					if(result!=null) {
						if (result.isSucceed()) {
							Log.e(TAG, "download ok...time:" + result.getTime()
									+ " and size:" + result.getResponse());
							/*Toast.makeText(FTPActivity.this, "下载成功", Toast.LENGTH_SHORT)
									.show();*/
							//if(iia==(remoteFile.size()-1)) {
								handler.sendEmptyMessage(5);
							/*}else{
								handler.sendEmptyMessage(7);
							}*/
						} else {
							Log.e(TAG, "download fail");
							/*Toast.makeText(FTPActivity.this, "下载失败", Toast.LENGTH_SHORT)
									.show();*/
							handler.sendEmptyMessage(6);
						}
					}else {
						handler.sendEmptyMessage(6);
					}

				} catch (IOException e) {
					e.printStackTrace();
					Log.e("----=-=",e.toString());
					handler.sendEmptyMessage(6);
				}

			}
		}.start();
	}

	/**
	 * 断开服务.
	 */
	private void clsseFtp(){
		Log.e("--------------------","");
		finish();
		new Thread(){
			@Override
			public void run() {
				super.run();
				try {
					ftp.closeConnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
				isduan=2;
			}
		}.start();
	}


	@Override
	public void onClick(View v) {
		int id=v.getId();
		switch (id){
			case R.id.button_download:
				downOne();
				break;
			case R.id.button_dowmall:
				iia=0;
				downAll();
				break;
			case R.id.button_close:
				clsseFtp();
				break;
		}
	}


	/**
	 * 删除单个文件
	 * @param   filePath    被删除文件的文件名
	 * @return 文件删除成功返回true，否则返回false
	 */
	public boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			return file.delete();
		}
		return false;
	}

	/**
	 * 进度dialog
	 * @param dialoginfo
	 */

	private Dialog dialog;
	private TextView tv_dialoginfo;
	public  void initDialog(Context context, String dialoginfo)
	{

		dialog = new Dialog(context, R.style.dialog);
		View view = View.inflate(context, R.layout.loading_dialog, null);
		tv_dialoginfo = (TextView) view.findViewById(R.id.tv_dialoginfo);
		tv_dialoginfo.setText(dialoginfo);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setContentView(view);

	}

}
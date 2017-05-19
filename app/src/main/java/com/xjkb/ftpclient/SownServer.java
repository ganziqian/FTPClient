package com.xjkb.ftpclient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.xjkb.ftpclient.hh.FTP;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public class SownServer extends Service {
    /**
     * 标签.
     */
    static final String TAG = "DOWNSERVER";


    /**
     * FTP文件集合.
     */
    private List<FTPFile> remoteFile;

    /**
     * FTP.
     */
    private FTP ftp;

    /**
     * 本地根目录.
     */
    private static final String LOCAL_PATH = "/mnt/sdcard/";


    public SownServer (FTP ftp){
        this.ftp=ftp;
    }

    public SownServer(){
        super();
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int hh=msg.what;
            stopSelf();
            switch (hh){
                case 1:
                //    listMain.setAdapter(adapter);
                    break;
                case 2:
                    Toast.makeText(getBaseContext(), "下载成功", Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 3:
                    Toast.makeText(getBaseContext(), "下载失败", Toast.LENGTH_SHORT)
                            .show();
                    break;

            }

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        new Thread(){

            @Override
            public void run() {
                super.run();
                loadRemoteView();
            }
        }.start();



        return super.onStartCommand(intent, flags, startId);
    }



    /**
     * 加载FTP
     */
    private void loadRemoteView() {
        try {

            remoteFile = new ArrayList<FTPFile>();

            remoteFile.addAll( ftp.listFiles(FTP.REMOTE_PATH));

            if(remoteFile.size()==0){

            }else {
                // 下载
                try {
                    Result result = null;
                    for (int i=0;i<remoteFile.size();i++){
                        result = ftp.download(FTP.REMOTE_PATH, remoteFile.get(i).getName(), LOCAL_PATH, new FTP.DownLoadProgressListener() {
                            @Override
                            public void onDownLoadProgress(String currentStep, long downProcess, File file) {

                            }
                        });
                    }

                    if (result.isSucceed()) {
                        Log.e(TAG, "download ok...time:" + result.getTime()
                                + " and size:" + result.getResponse());
						/*	Toast.makeText(getBaseContext(), "下载成功", Toast.LENGTH_SHORT)
									.show();*/
                        handler.sendEmptyMessage(2);
                    } else {
                        Log.e(TAG, "download fail");
							/*Toast.makeText(getBaseContext(), "下载失败", Toast.LENGTH_SHORT)
									.show();*/
                        handler.sendEmptyMessage(3);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

            if(ftp!=null) {
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            ftp.closeConnect();
                            interrupt();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.e("222222","===================");
                    }
                }.start();

            }

    }
}

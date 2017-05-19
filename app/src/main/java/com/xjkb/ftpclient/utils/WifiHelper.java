package com.xjkb.ftpclient.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.lang.reflect.Method;
import java.util.List;

/**
 * @ClassName: WifiHelper
 * @Description:
 * @Author: YHD
 * @Date: 2016/7/18
 * @Copyright: (c) 2016 Wenshanhu.Co.Ltd. All rights reserved.
 */
public class WifiHelper {
    private final static String TAG="WifiHelper";
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
    private  BroadcastReceiver mReceiver;
    private  IntentFilter mFilter;
    private static WifiHelper instance;
    private WifiManager mWifiManager;
    private Context mApplicationContext;
    private WifiHelperListener mWifiHelperListener;
    private Scanner mScanner;


    //Wifi操作状态回调
    public static enum WifiState {
        WIFI_STATE_DISABLED("Wifi已经断开"),
        WIFI_STATE_DISABLING("Wifi正在断开"),
        WIFI_STATE_ENABLED("Wifi已经打开"),
        WIFI_STATE_ENABLING("Wifi正在打开"),
        WIFI_STATE_CONNECTING("正在连接"),
        WIFI_STATE_CONNECTED("已连接"),
        WIFI_STATE_CONNECT_FAILED("连接失败"),
        WIFI_STATE_DISCONNECTED("Wifi断开连接"),
        CONNET_STATE_WIFI("正在获取ip地址"),
        CONNET_IDCHANGE_WIFI("配置的网络标识已经更改"),
        CONNET_JIANLI_WIFI("连接已经建立或者丢失"),
        SCAN_WIFI_TIMEOUT("扫描WIFI超时"),
        SCAN_WIFI_BEGIN("开始扫描WIFI"),
        NO_WIFI_FOUND("扫描WIFI结束"),
        RSSI_CHANGED_ACTION("wifi信号发生改变"),
        WIFI_CONFIG_FAILED("wifi信息配置出错");
        private final String state;

        WifiState(String var) {
            this.state = var;
        }
        public String toString() {
            return this.state;
        }
    }

    public static synchronized WifiHelper getInstance(Context getApplicationContext){
        if(instance==null){
            instance=new WifiHelper(getApplicationContext);
        }
        return instance;
    }

    public WifiHelper(Context context){
        Log.v(TAG, "created");
        this.mApplicationContext=context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mScanner = new Scanner();
    }

    //打开wifi
    public void openWifi(){
        if(mWifiManager==null){

        }else {
            if(!mWifiManager.isWifiEnabled()){
                mWifiManager.setWifiEnabled(true);
            }
        }

    }
    //关闭wifi
    public void closeWifi(){
        if(mWifiManager==null){

        }else {
            if (mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(false);
            }
        }
    }


    // 检查当前wifi状态
    public int checkState() {
        return mWifiManager.getWifiState();
    }

    /**
     * 注册广播
     * @param listener
     * @return
     */
    public WifiHelper registBroadcast(WifiHelperListener listener){
        Log.v(TAG,"registBroadcast");
        this.mWifiHelperListener=listener;
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);

        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION); //信号强度变化
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION); //网络状态变化
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); //wifi状态，是否连上，密码
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);  //是不是正在获得IP地址
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//连上与否


        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(mWifiHelperListener != null){
                    handleEvent(intent);
                }
            }
        };
        try {
            mApplicationContext.registerReceiver(mReceiver, mFilter);
        }catch (Exception e){
            e.printStackTrace();
        }

        return instance;
    }

    /**
     * 取消广播注册
     * @return
     */
    public WifiHelper unRegistBroadcast() {
        Log.v(TAG,"unRegistBroadcast");

            mApplicationContext.unregisterReceiver(mReceiver);


        return instance;
    }

    /**
     * 获得wifi服务管理器
     * @return
     */
    public WifiManager getWifiManager(){
        return mWifiManager;
    }

    /**
     * 获得扫描器
     * @return
     */
    public Scanner getScanner(){
        return mScanner;
    }

    /**
     * 尝试连接配置过的wifi
     * @param results
     * @return
     */
    public boolean tryAutoConnect(List<ScanResult> results){
        Log.v(TAG,"tryAutoConnect");
        if(!mWifiManager.isWifiEnabled()){
            return false;
        }
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for(ScanResult scanResult:results){
            for(WifiConfiguration wifiConfiguration:existingConfigs){
                if(scanResult.SSID.equals(wifiConfiguration.SSID.replace("\"",""))){
                    connect(wifiConfiguration, true);
                }
            }

        }
        return true;
    }

    /**
     * 连接已经配置过的Wifi
     * @param configuration
     * @param reconnect
     * @return
     */
    public boolean connect(WifiConfiguration configuration,boolean reconnect){
        Log.v(TAG,"Connect");
        if(!mWifiManager.isWifiEnabled()){
            return false;
        }
        if(mWifiHelperListener!=null){
            mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_CONNECTING,configuration.SSID);
        }
        boolean result= BaseWifiUtil.connectToConfiguredNetwork(mWifiManager, configuration, reconnect);
        if(mWifiHelperListener!=null){
            if(!result) {
                mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_CONNECT_FAILED, configuration.SSID);
            }
        }
        return result;
    }

    /**
     * 连接没有配置过的Wifi
     * @param scanResult
     * @param password
     * @return
     */
    public boolean connect(ScanResult scanResult,String password){
        Log.v(TAG,"Connect:");
        if(!mWifiManager.isWifiEnabled()){
            return false;
        }
        if(mWifiHelperListener!=null){
            mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_CONNECTING,scanResult.SSID);
        }
        boolean result=BaseWifiUtil.connectToNewNetwork(mWifiManager, scanResult, password);
        if(mWifiHelperListener!=null){
            if(!result) {
                mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_CONNECT_FAILED, scanResult.SSID);
            }
        }
        return result;
    }

    /**
     *
     * @param scanResult
     * @param reconnect
     * @return
     */
    public boolean connect(ScanResult scanResult,boolean reconnect){
        Log.v(TAG,"Connect:");
        if(!mWifiManager.isWifiEnabled()){
            return false;
        }
        if(mWifiHelperListener!=null){
            mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_CONNECTING,scanResult.SSID);
        }
        boolean result=BaseWifiUtil.connectToConfiguredNetwork(mWifiManager, BaseWifiUtil.getWifiConfiguration(mWifiManager, scanResult, BaseWifiUtil.getScanResultSecurity(scanResult)), reconnect);
        if(mWifiHelperListener!=null){
            if(!result) {
                mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_CONNECT_FAILED, scanResult.SSID);
            }
        }
        Log.e("res------------",result+"");
        return result;
    }

    /**
     * 对应的Wifi是否已经配置过
     * @param SSID Wifi名
     * @return 配置信息
     */
    public WifiConfiguration getConfiguredNetwork(String SSID) {
        Log.v(TAG,"getConfiguredNetwork:"+SSID);
        if(!SSID.contains("\"")){
            SSID=BaseWifiUtil.convertToQuotedString(SSID);
        }
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals(SSID)) {
                return existingConfig;
            }
        }
        return null;
    }






    /**
     * 查看对应Wifi是否连接
     * @param SSID  Wifi名
     * @return 是否连接
     */
    public boolean isConnected(String SSID){
        if(!SSID.contains("\"")){
            SSID=BaseWifiUtil.convertToQuotedString(SSID);
        }
        WifiInfo connectInfo = mWifiManager.getConnectionInfo();
        if(connectInfo!=null && connectInfo.getSSID().equals(SSID)){
            return true;
        }
        return false;
    }

    /**
     * 扫描wifi帮助类
     */
    public class Scanner extends Handler {
        public boolean isScanning=false;
        public void start() {
            isScanning=true;
            Log.v(TAG,"Scanner:start");
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        public void forceScan() {
            isScanning=true;
            Log.v(TAG,"Scanner:forceScan");
            if (!mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(true);
            }
            removeMessages(0);
            sendEmptyMessage(0);
        }

        public void stop() {
            isScanning=false;
            Log.v(TAG,"Scanner:stop");
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            Log.v(TAG,"Scanner:handleMessage");
            if (mWifiManager.isWifiEnabled()) {
                mWifiManager.startScan();
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }

    /**
     * 处理广播数据
     * @param intent
     */
    private void handleEvent(Intent intent) {




    /*    mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);


        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION); //信号强度变化
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION); //网络状态变化
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); //wifi状态，是否连上，密码
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);  //是不是正在获得IP地址
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//连上与否
*/




        String action = intent.getAction();
       // Log.e("------action>",action);
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            Log.e("----------->",state+"");
            switch (state) {
                case WifiManager.WIFI_STATE_ENABLED:
                    mScanner.forceScan();
                    mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_ENABLED,null);
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_ENABLING,null);
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    if(mScanner.isScanning){
                        mScanner.stop();
                    }
                    mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_DISABLED,null);
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_DISABLING,null);
                    break;
                default:
                    mWifiHelperListener.onWifiState(WifiState.RSSI_CHANGED_ACTION,null);
                    break;
            }
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            List<ScanResult> results = mWifiManager.getScanResults();
            if(results.size()>0){
                mWifiHelperListener.onScanSuccess(results);
                if(mWifiManager.getConnectionInfo()==null){

                    /*    for (int i = 0; i <results.size(); i++) {
                        if (isConnected(results.get(i).SSID)) {
                            ConnectivityManager connManager = (ConnectivityManager) mApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            if (mWifi.isConnected()) {
                                return;
                            }
                        }
                    }
                    Log.e("--------","尝试连接");
*/
                    tryAutoConnect(results);
                }
            }
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);
            mWifiHelperListener.onSupplicantState(supplicantState,error);
            mWifiHelperListener.onWifiState(WifiState.CONNET_STATE_WIFI,mWifiManager.getConnectionInfo().getSSID());
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
          //  if(info.isConnected()){
            if(info.getState().equals(NetworkInfo.State.CONNECTED)){
                mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_CONNECTED,mWifiManager.getConnectionInfo().getSSID());
            }else{
                mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_DISCONNECTED,null);
            }
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            mWifiHelperListener.onWifiState(WifiState.RSSI_CHANGED_ACTION,"88888");
        }else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){

                NetworkInfo ni = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

                if (ni.getState() == NetworkInfo.State.CONNECTED && ni.getType() == ConnectivityManager.TYPE_WIFI) {
                    mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_CONNECTED,"666");
                } /*else if(ni.getState() == NetworkInfo.State.DISCONNECTED && ni.getType() == ConnectivityManager.TYPE_WIFI){
                    mWifiHelperListener.onWifiState(WifiState.WIFI_STATE_CONNECT_FAILED,mWifiManager.getConnectionInfo().getSSID());
                }*/


        }else if(WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(action)){
            mWifiHelperListener.onWifiState(WifiState.CONNET_IDCHANGE_WIFI,null);
        }else if(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)){
            mWifiHelperListener.onWifiState(WifiState.CONNET_JIANLI_WIFI,null);
        }
    }

    /**
     * 供外部使用的回调接口
     */
    public interface WifiHelperListener {
        void onWifiState(WifiState wifiState, String SSID);
        void onSupplicantState(SupplicantState SupplicantState, int error);
        void onScanSuccess(List<ScanResult> mWifiList);
    }




    /**
     * 通过反射出不同版本的connect方法来连接Wifi
     *
     * @author jiangping.li
     * @param netId
     * @return
     * @since MT 1.0
     *
     */
    public Method connectWifiByReflectMethod(int netId) {
        Method connectMethod = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Log.e(TAG, "connectWifiByReflectMethod road 1");
            // 反射方法： connect(int, listener) , 4.2 <= phone's android version
            for (Method methodSub : mWifiManager.getClass()
                    .getDeclaredMethods()) {
                if ("connect".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(mWifiManager, netId, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "connectWifiByReflectMethod Android "
                            + Build.VERSION.SDK_INT + " error!");
                    return null;
                }
            }
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            // 反射方法: connect(Channel c, int networkId, ActionListener listener)
            // 暂时不处理4.1的情况 , 4.1 == phone's android version
            Log.e(TAG, "connectWifiByReflectMethod road 2");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Log.e(TAG, "connectWifiByReflectMethod road 3");
            // 反射方法：connectNetwork(int networkId) ,
            // 4.0 <= phone's android version < 4.1
            for (Method methodSub : mWifiManager.getClass()
                    .getDeclaredMethods()) {
                if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(mWifiManager, netId);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "connectWifiByReflectMethod Android "
                            + Build.VERSION.SDK_INT + " error!");
                    return null;
                }
            }
        } else {
            // < android 4.0
            return null;
        }
        return connectMethod;
    }


}

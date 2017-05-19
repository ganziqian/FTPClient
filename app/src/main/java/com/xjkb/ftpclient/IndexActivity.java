package com.xjkb.ftpclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xjkb.ftpclient.utils.SaveUtils;
import com.xjkb.ftpclient.utils.WifiHelper;

import java.util.ArrayList;
import java.util.List;

public class IndexActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private EditText txtHostName;

    private EditText txtUserName;

    private EditText txtPasswrod;
    private GridView lv;

    private Button buttonLogin;
    private TextAdapetr adapetr;

    private WifiHelper wifiHelper;
    private WifiHelper.WifiHelperListener wifiHelperListener;


    private List<String> wifilist = new ArrayList<String>();
    private Spinner mySpinner;
    private ArrayAdapter<String> adapter;

    private String wifiSSID="xingjikubao";

    private int oneDIAN=0;


    private List<String> list=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.main);
       // startService(new Intent(this,SownServer.class));
        initView();
    }


    private void initView() {
        txtHostName = (EditText) findViewById(R.id.txt_host_name);
        txtUserName = (EditText) findViewById(R.id.txt_user_name);
        txtPasswrod = (EditText) findViewById(R.id.txt_password);
        buttonLogin = (Button) findViewById(R.id.button_login);


        mySpinner = (Spinner)findViewById(R.id.Spinner_city);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);


        lv= (GridView) findViewById(R.id.index_lv);


        lv.setOnItemClickListener(this);
        adapetr=new TextAdapetr(this,list);
        lv.setAdapter(adapetr);
        insertData();

        if(SaveUtils.getLoginMesg(this,"ip").equals("")){

        }else {
            txtHostName.setText(SaveUtils.getLoginMesg(this, "ip"));
            txtUserName.setText(SaveUtils.getLoginMesg(this, "name"));
            txtPasswrod.setText(SaveUtils.getLoginMesg(this, "psw"));
        }

        buttonLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(txtHostName.getText().toString().equals("")|| txtUserName.getText().toString().equals("")||txtPasswrod.getText().toString().equals("")){
                    Toast.makeText(IndexActivity.this,"请填写完整的信息",Toast.LENGTH_SHORT).show();
                }else {
                    if(isNetworkAvailable(IndexActivity.this)) {
                        SaveUtils.saveInfo(IndexActivity.this, txtHostName.getText().toString(), txtUserName.getText().toString(), txtPasswrod.getText().toString());
                        Intent intent = new Intent(IndexActivity.this, FTPActivity.class);
                        intent.putExtra("hostName", txtHostName.getText().toString());
                        intent.putExtra("userName", txtUserName.getText().toString());
                        intent.putExtra("password", txtPasswrod.getText().toString());
                        startActivity(intent);
                    }else {
                        Toast.makeText(IndexActivity.this,"网络未连接",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        TextView tv= (TextView) findViewById(R.id.tui_tv);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });











        wifiHelper=new WifiHelper(this);
        wifiHelperListener=new WifiHelper.WifiHelperListener() {

            @Override
            public void onWifiState(WifiHelper.WifiState wifiState,String SSID) {

                if(wifiState== WifiHelper.WifiState.WIFI_STATE_CONNECTED){

                    List<ScanResult> mWifiList =  wifiHelper.getWifiManager().getScanResults();
                    for (int i = 0; i <mWifiList.size(); i++) {
                        if (wifiHelper.isConnected(mWifiList.get(i).SSID)) {
                            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            if(mWifi.isConnected()) {
                                Toast.makeText(IndexActivity.this,"wifi已连接",Toast.LENGTH_SHORT).show();
                                break;
                            }

                        }


                    }





                }else if(wifiState== WifiHelper.WifiState.WIFI_STATE_CONNECT_FAILED){

                }




            }

            @Override
            public void onSupplicantState(SupplicantState SupplicantState, int error) {
                if (SupplicantState != null) {
                    if(SupplicantState==SupplicantState.COMPLETED){
                        List<ScanResult> mWifiList =  wifiHelper.getWifiManager().getScanResults();
                        for (int i = 0; i <mWifiList.size(); i++) {
                            if (wifiHelper.isConnected(mWifiList.get(i).SSID)) {
                                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                                if(mWifi.isConnected()) {
                                    Toast.makeText(IndexActivity.this,"wifi已连接",Toast.LENGTH_SHORT).show();
                                    break;
                                }

                            }


                        }
                    }
                    // Log.e("--ww---onSupplicantState----", SupplicantState + "-" + error);
                }
            }

            @Override
            public void onScanSuccess(List<ScanResult> wifiList) {



            }
        };
        wifiHelper.registBroadcast(wifiHelperListener);

      //  new WifiThread().start();



        if(isNetworkAvailable(IndexActivity.this)) {
            oneDIAN=0;
        }else {
            oneDIAN=11;
        }


        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, wifilist);
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        mySpinner.setAdapter(adapter);
        //第五步：为下拉列表设置各种事件的响应，这个事响应菜单被选中
        mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                /* 将所选mySpinner 的值带入myTextView 中*/
                /* 将mySpinner 显示*/

                if(oneDIAN!=0) {
                    wifiSSID = wifilist.get(arg2);
                    Toast.makeText(IndexActivity.this, "正在连接" + wifilist.get(arg2), Toast.LENGTH_SHORT).show();
                    new WifiThread().start();
                }
                oneDIAN=109;
               // arg0.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
              //  arg0.setVisibility(View.VISIBLE);
            }
        });

    }






    private class WifiThread extends Thread{
        @Override
        public void run() {
            super.run();

            if ( wifiHelper.checkState() == 1) {
                //未打开
                wifiHelper.openWifi();
            } else if (wifiHelper.checkState() == 3) {
                //已打开
            }
            //尝试连接已经连接过得wifi
            wifiHelper.getWifiManager().startScan();

            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            List<ScanResult> mWifiList =  wifiHelper.getWifiManager().getScanResults();
            wifiHelper.tryAutoConnect(mWifiList);

            // Toast.makeText(getBaseContext(),"尝试连接wifi",Toast.LENGTH_LONG).show();


            for (int i = 0; i <mWifiList.size(); i++) {

                if(mWifiList.get(i).SSID.equals(wifiSSID)) {
                    wifiHelper.connect(mWifiList.get(i), "xjkb8888");
                    Log.e("===========","尝试连接wifi");
                }
			/*WifiConfiguration wifiConfiguration = WifiHelper.getInstance(getBaseContext()).getConfiguredNetwork(mWifiList.get(i).SSID);
			if (wifiConfiguration != null) {
				WifiHelper.getInstance(getBaseContext()).connect(wifiConfiguration, false);

SSID: xjkb_DT2, BSSID: ec:6c:9f:39:cf:68, capabilities: [WPA2-PSK-TKIP][ESS], level: -56, frequency: 2412, timestamp: 80590553873, distance: ?(cm), distanceSd: ?(cm)
			}*/
            }

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiHelper.unRegistBroadcast();
    }

    private void insertData() {







        wifilist.add("xingjikubao");
        wifilist.add("xingjikubao01");
        wifilist.add("xingjikubao02");
        wifilist.add("xingjikubao03");
        wifilist.add("xingjikubao04");
        wifilist.add("xingjikubao05");
        wifilist.add("xingjikubao06");
        wifilist.add("xingjikubao07");
        wifilist.add("xingjikubao08");
        wifilist.add("xingjikubao09");
        wifilist.add("xingjikubao10");
        wifilist.add("xingjikubao11");



        list.add("192.168.1.36-kubao-kubao");
        list.add("192.168.1.36-meihao-meihao");
 /*       list.add("192.168.1.65-kubao-kubao");
        list.add("192.168.1.66-kubao-kubao");
        list.add("192.168.1.67-kubao-kubao");
        list.add("192.168.1.68-kubao-kubao");
        list.add("192.168.1.69-kubao-kubao");
        list.add("192.168.1.70-kubao-kubao");
        list.add("192.168.1.71-kubao-kubao");
        list.add("192.168.1.72-kubao-kubao");
        list.add("192.168.1.73-kubao-kubao");
        list.add("192.168.1.74-kubao-kubao");
        list.add("192.168.1.75-kubao-kubao");
        list.add("192.168.1.76-kubao-kubao");
        list.add("192.168.1.77-kubao-kubao");
        list.add("192.168.1.78-kubao-kubao");
        list.add("192.168.1.79-kubao-kubao");*/

       adapetr.notifyDataSetChanged();

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        try{
            String str[]=list.get(position).split("-");
            txtHostName.setText(str[0]);
            txtUserName.setText(str[1]);
            txtPasswrod.setText(str[2]);


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 检查当前网络是否可用
     *
     * @return
     */

    public static boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    System.out.println(i + "===状态===" + networkInfo[i].getState());
                    System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

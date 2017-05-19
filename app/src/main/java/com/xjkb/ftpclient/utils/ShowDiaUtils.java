package com.xjkb.ftpclient.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.xjkb.ftpclient.R;


/**
 * 创建人：  ganziqian
 * 作用：
 * 时间：2015/11/19
 */
public class ShowDiaUtils {
    public static Dialog dialog;

    private static  TextView tv_dialoginfo;

    public static void showDeatial(String ms,Context context){
        final Dialog dialog = new Dialog(context, R.style.NobackDialog);
        View view = View.inflate(context,
                R.layout.quding_layout, null);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        Window dialogWindow = dialog.getWindow();
        Activity activity= (Activity) context;
        WindowManager m =activity.getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (d.getWidth() * 0.3); // 宽度设置为屏幕的
        dialogWindow.setAttributes(p);
        dialog.setCanceledOnTouchOutside(false);//设置点击Dialog外部任意区域关闭Dialog
        Button quedingBtn = (Button) view
                .findViewById(R.id.zhifu_tishi_btn);
        TextView tv= (TextView) view.findViewById(R.id.zhifu_tishi_tv);
        tv.setText(ms);
        quedingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * 进度dialog
     * @param dialoginfo
     */
    public static void showDialog(Context context,String dialoginfo)
    {



            dialog = new Dialog(context, R.style.dialog);
            View view = View.inflate(context, R.layout.loading_dialog, null);
            tv_dialoginfo = (TextView) view.findViewById(R.id.tv_dialoginfo);
            tv_dialoginfo.setText(dialoginfo);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(view);
            dialog.show();

    }

  /*  public static void setTitle(String str){
        if(tv_dialoginfo!=null){
            tv_dialoginfo.setText("正在下载..."+str);
        }
    }*/
    /**
     * 进度dialog
     */
    public static void dimssLoadDia()
    {
        if(dialog!=null)
        {
            dialog.dismiss();
        }
    }
}

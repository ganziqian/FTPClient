package com.xjkb.ftpclient;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * FTP列表适配器.
 * 
 * @author cui_tao
 */
public class RemoteAdapter extends BaseAdapter {
    /**
     * FTP文件列表.
     */
    private List<FTPFile> list = new ArrayList<FTPFile>();

    /**
     * 布局.
     */
    private LayoutInflater inflater;
    private String str;

    /**
     * 文件夹显示图片.
     */
    private Bitmap icon1;

    /**
     * 文件显示图片.
     */
    private Bitmap icon2;

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    /**
     * 构造函数.
     * @param context 当前环境
     * @param li FTP文件列表
     */


    public RemoteAdapter(Context context, List<FTPFile> li,String str) {
        this.list = li;
        this.inflater = LayoutInflater.from(context);
        // 文件夹显示图片
        icon1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder);
        // 文件显示图片
        icon2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.doc);

        this.str=str;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            // 设置视图
            view = this.inflater.inflate(R.layout.ftp_list, null);
            // 获取控件实例
            holder = new ViewHolder();
            holder.icon = (ImageView) view.findViewById(R.id.image_icon);
            holder.fileName = (TextView) view.findViewById(R.id.text_name);
            holder.fileSize = (TextView) view.findViewById(R.id.text_size);
            // 设置标签
            view.setTag(holder);
        } else {
            // 获取标签
            holder = (ViewHolder) view.getTag();
        }
        // 获取文件名


        holder.fileName.setText(Util.convertString(list.get(position).getName(), "GBK"));
        if (!list.get(position).isDirectory()) {
            // 获取显示文件图片
            holder.icon.setImageBitmap(icon2);
            // 获取文件大小
            holder.fileSize.setText(Util.getFormatSize(list.get(position).getSize()));
        } else {
            // 获取显示文件夹图片
            holder.icon.setImageBitmap(icon1);
            holder.fileSize.setText("");
        }

        if(str.equals(Util.convertString(list.get(position).getName(), "GBK"))){
            view.setBackgroundResource(R.color.colorAccent);//点击的Item项背景设置
        }else{
            view.setBackgroundResource(R.color.wite);//点击的Item项背景设置
        }

        return view;
    }

    /**
     * 获取控件.
     */
    private class ViewHolder {
        /**
         * 图片.
         */
        private ImageView icon;

        /**
         * 文件名.
         */
        private TextView fileName;

        /**
         * 文件大小.
         */
        private TextView fileSize;
    }
}

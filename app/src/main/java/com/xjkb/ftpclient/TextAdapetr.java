package com.xjkb.ftpclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2016/12/2.
 */
public class TextAdapetr extends BaseAdapter{

    private Context context;
    private List<String> list;

    public TextAdapetr(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh=null;
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.tv_layout,null);
            vh=new ViewHolder(convertView);
            convertView.setTag(vh);
        }else{
            vh= (ViewHolder) convertView.getTag();
        }
        vh.tv.setText(list.get(position));


        return convertView;
    }

    private class ViewHolder{
        private TextView tv;

        public ViewHolder(View  view) {
            this.tv = (TextView) view.findViewById(R.id.item_jj_tv);
        }
    }
}

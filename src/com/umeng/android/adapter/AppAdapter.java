package com.umeng.android.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.android.bean.AppInformation;
import com.umeng.android.util.StringUtil;
import com.umeng.client.R;

public class AppAdapter extends BaseAdapter {

	private Context context;
	private List<AppInformation> lists;
	
	public AppAdapter(Context c, List<AppInformation> list) {
		this.context = c;
		this.lists = new ArrayList<AppInformation>();
		this.lists.addAll(list);
	}

	@Override
	public int getCount() {
		return lists == null?0:lists.size();
	}

	@Override
	public Object getItem(int position) {
		return lists == null?null:lists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return lists == null?0:position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		if(convertView == null){
			LayoutInflater factory = LayoutInflater.from(context);
			convertView = factory.inflate(R.layout.applistview, null);
			holder = new ViewHolder();
			holder.imageView  = (ImageView) convertView.findViewById(R.id.applistview_image_1);
			holder.linearLayout_in = (LinearLayout) convertView.findViewById(R.id.applistview_1);
			holder.textView = (TextView) convertView.findViewById(R.id.applistview_text_1);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.linearLayout_in.setBackgroundResource(R.drawable.applist_midle_back_2);
		holder.textView.setText(StringUtil.cutString(lists.get(position).getName(),21));
		if (lists.get(position).getPlatform().equals("iphone")) {
			holder.imageView.setImageResource(R.drawable.platform_icon_iphone);
		} else if (lists.get(position).getPlatform().equals("ipad")) {
			holder.imageView.setImageResource(R.drawable.platform_icon_ipad);
		} else if (lists.get(position).getPlatform().equals("wphone")) {
			holder.imageView.setImageResource(R.drawable.platform_icon_wphone);
		} else if (lists.get(position).getPlatform().equals("wphone8")) {
			holder.imageView.setImageResource(R.drawable.platform_icon_wphone);
		} else {
			holder.imageView.setImageResource(R.drawable.platform_icon_android);
		}
		return convertView;
	}

	/**
	 * @return the lists
	 */
	public List<AppInformation> getLists() {
		return lists;
	}

	/**
	 * @param lists the lists to set
	 */
	public void setLists(List<AppInformation> lists) {
		if(lists == null){
			return;
		}
		this.lists = new ArrayList<AppInformation>();
		this.lists.addAll(lists);
		notifyDataSetChanged();
	}

	private static class ViewHolder{
		LinearLayout linearLayout_in;
		TextView textView;
		ImageView imageView;
	}
}
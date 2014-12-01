package com.umeng.android.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.umeng.android.bean.VesinAndChannelBean;
import com.umeng.client.R;
/**
 * a adapter to fill Top10 version or chanel listview
 */
public class Top10ListAdapter extends BaseAdapter {

	private Context context;
	private List<VesinAndChannelBean> lists;
	
	public Top10ListAdapter(Context context,List<VesinAndChannelBean> lists){
		this.context = context;
		this.lists = lists;
	}
	@Override
	public int getCount() {
		
		return lists == null? 0 : lists.size();
	}

	@Override
	public Object getItem(int position) {
		return lists == null ? null : lists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder = null;
		if(view == null){
			LayoutInflater factory = LayoutInflater.from(context);
			view  = factory.inflate(R.layout.top10_listview_item, null);
			holder = new ViewHolder();
			holder.activeUserTextView = (TextView) view.findViewById(R.id.top10_channel_text_3);
			holder.nameTextView = (TextView) view.findViewById(R.id.top10_channel_text_1);
			holder.newUserTextView = (TextView) view.findViewById(R.id.top10_channel_text_2);
			holder.rateTextView = (TextView) view.findViewById(R.id.top10_channel_text_4);
			view.setTag(holder);
		}else{
			holder = (ViewHolder) view.getTag();
		}
		
		holder.nameTextView.setText(lists.get(position).getName());
		holder.nameTextView.setTextColor(Color.parseColor("#64a1c7"));
		holder.activeUserTextView.setText(""+lists.get(position).getActiveUser());
		holder.newUserTextView.setText(""+lists.get(position).getNewUser());
		holder.rateTextView.setText(lists.get(position).getRate()+"%");
		return view;
	}
	
	private class ViewHolder{
		TextView nameTextView;
		TextView activeUserTextView;
		TextView newUserTextView;
		TextView rateTextView;
	}
	
}

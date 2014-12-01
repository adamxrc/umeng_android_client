package com.umeng.android.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.umeng.android.bean.LabelEventBean;
import com.umeng.client.R;

public class LabelEventAdapter extends BaseAdapter {

	private Context context;
	private List<LabelEventBean> lists;
	
	
	public LabelEventAdapter(Context context, List<LabelEventBean> lists) {
		super();
		this.context = context;
		this.lists = lists;
	}

	public void setLists(List<LabelEventBean> lists) {
		this.lists = lists;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
	
		return lists == null? 0: lists.size();
	}

	@Override
	public Object getItem(int position) {
		return (lists == null || position >= lists.size()) ? 0:lists.size() ;
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
			view = LayoutInflater.from(context).inflate(R.layout.duration_time_listitem, null);
			holder = new ViewHolder();
			holder.durationTimeTextView = (TextView) view.findViewById(R.id.duration_time);
			holder.durationTimeTextView.setTextColor(context.getResources().getColor(R.color.clickableTextColor));
			holder.launchTimeTextView = (TextView) view.findViewById(R.id.launch_times);
			holder.proportionTextView = (TextView) view.findViewById(R.id.proportion);
			view.setTag(holder);
		}else{
			holder = (ViewHolder) view.getTag();
		}
		view.setBackgroundResource(R.drawable.applist_midle_back_2);
		holder.durationTimeTextView.setText(lists.get(position).getLabel());
		holder.launchTimeTextView.setText(lists.get(position).getNum());
		holder.proportionTextView.setText(lists.get(position).getPercent()+"%");
		return view;
	}
	static class ViewHolder{
		TextView durationTimeTextView;
		TextView launchTimeTextView;
		TextView proportionTextView;
	}
}

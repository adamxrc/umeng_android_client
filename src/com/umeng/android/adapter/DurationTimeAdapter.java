package com.umeng.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.umeng.android.bean.DurationTimeBean;
import com.umeng.client.R;

public class DurationTimeAdapter extends BaseAdapter {

	private Context context;
	private DurationTimeBean.Data[] datas;
	public DurationTimeAdapter(Context context,DurationTimeBean durationTimeBean){
		this.context = context;
		if(durationTimeBean!=null&&durationTimeBean.getDatas()!=null){
			datas = durationTimeBean.constructArrays(durationTimeBean.getDatas().length);
			int i=0;
			for(DurationTimeBean.Data data:durationTimeBean.getDatas()){
				datas[i++] = data; 
			}
		}
	}
	@Override
	public int getCount() {
		return datas == null?0:datas.length;
	}

	@Override
	public Object getItem(int position) {
		return (datas == null||position>=datas.length)?null:datas[position];
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
			holder.launchTimeTextView = (TextView) view.findViewById(R.id.launch_times);
			holder.proportionTextView = (TextView) view.findViewById(R.id.proportion);
			view.setTag(holder);
		}else{
			holder = (ViewHolder) view.getTag();
		}
		holder.durationTimeTextView.setText(datas[position].key);
		holder.launchTimeTextView.setText(datas[position].num+"");
		holder.proportionTextView.setText((float)datas[position].percent+"%");
		return view;
	}
	static class ViewHolder{
		TextView durationTimeTextView;
		TextView launchTimeTextView;
		TextView proportionTextView;
	}
}

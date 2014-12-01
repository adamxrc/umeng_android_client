package com.umeng.android.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.umeng.android.bean.FeedbackBean;
import com.umeng.client.R;

public class FeedbackAdapter extends BaseAdapter {

	private Context context;
	private List<FeedbackBean> lists;
	
	/**
	 * @param context
	 * @param lists
	 */
	public FeedbackAdapter(Context context, List<FeedbackBean> lists) {
		super();
		this.context = context;
		this.lists = lists;
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
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.feedback_listitem, null);
		TextView fbNameTextView = (TextView) view.findViewById(R.id.fb_name);
		TextView phoneOSTextView = (TextView) view.findViewById(R.id.phone_os);
		TextView timeTextView = (TextView) view.findViewById(R.id.fb_time);
		fbNameTextView.setText(lists.get(position).getThread());
		phoneOSTextView.setText(lists.get(position).getDevice_model());
		String time = lists.get(position).getDatetime();

		if(time != null && time.indexOf("-")>=0 && time.lastIndexOf(":")>=0){
			timeTextView.setText(time.subSequence(time.indexOf("-")+1, time.lastIndexOf(":")));
		}else{
			System.out.println(time);
		}
		if(!lists.get(position).isIsreplied()){
			view.setBackgroundResource(R.drawable.fb_list_new);
		}
		return view;
	}
	/**
	 * @return the lists
	 */
	public List<FeedbackBean> getLists() {
		return lists;
	}

	/**
	 * @param lists the lists to set
	 */
	public void setLists(List<FeedbackBean> lists) {
		synchronized (this) {
			this.lists = lists;
			this.notifyDataSetChanged();
		}
	}
	
}

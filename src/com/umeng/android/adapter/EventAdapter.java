package com.umeng.android.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.umeng.android.bean.GroupBean;
import com.umeng.android.util.StringUtil;
import com.umeng.client.R;

public class EventAdapter extends BaseAdapter {

	private Context context;
	private List<GroupBean> lists;
	
	/**
	 * @param context
	 * @param lists
	 */
	public EventAdapter(Context context, List<GroupBean> lists) {
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
		View view = convertView;
		ViewHolder holder;
		if(view == null){
			view = LayoutInflater.from(context).inflate(R.layout.event_listview_item, null);
			holder = new ViewHolder();
			holder.eventNameTextView = (TextView) view.findViewById(R.id.trend_single_listview_text_1);
			holder.eventCounTextView = (TextView) view.findViewById(R.id.trend_single_listview_text_2);
			view.setTag(holder);
		}else{
			holder = (ViewHolder) view.getTag();
		}
//		view.setBackgroundResource(R.drawable.applist_midle_back_2);
		if(StringUtil.isEmpty(lists.get(position).getDisplay_name())){
			if(StringUtil.isEmpty(lists.get(position).getName())){
				holder.eventNameTextView.setText(lists.get(position).getGroup_id());
			}else{
				holder.eventNameTextView.setText(lists.get(position).getName());
			}
		}else{
			holder.eventNameTextView.setText(lists.get(position).getDisplay_name());
		}
		holder.eventCounTextView.setText(StringUtil.cutInteger(lists.get(position).getCount()));
		return view;
	}
	static class ViewHolder{
		TextView eventNameTextView;
		TextView eventCounTextView;
	}
	public void setLists(List<GroupBean> lists) {
		this.lists = lists;
		notifyDataSetChanged();
	}
}

package com.umeng.android.adapter;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.umeng.android.activity.MessagDetailsActivity;
import com.umeng.android.bean.MessageInfo;
import com.umeng.android.dialog.DialogManager;
import com.umeng.client.R;

public class MessageCenterAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mInflater;
	private ViewHolder viewHolder;
	private ArrayList<MessageInfo> list;

	public MessageCenterAdapter(Context context, ArrayList<MessageInfo> list) {
		this.mContext = context;
		this.list = list;
		mInflater = LayoutInflater.from(mContext);

	}

	public void notifyChanged(ArrayList<MessageInfo> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {

		return list.size();
	}

	public MessageInfo getItem(int arg0) {

		return list.get(arg0);
	}

	public long getItemId(int arg0) {

		return arg0;
	}

	public View getView(final int position, View view, ViewGroup parent) {
		if (view == null) {

			viewHolder = new ViewHolder();
			view = mInflater.inflate(R.layout.message_item, null);
			viewHolder.message_title = (TextView) view
					.findViewById(R.id.message_title);
			viewHolder.message_date = (TextView) view
					.findViewById(R.id.message_date);

			view.setTag(viewHolder);

		} else {

			viewHolder = (ViewHolder) view.getTag();
		}

		final MessageInfo messageInfo = list.get(position);
		if (messageInfo != null) {

			viewHolder.message_title.setText(messageInfo.getTitle());
			viewHolder.message_date.setText(messageInfo.getCreated_at());
			view.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub

					Dialog dialog = DialogManager.getDeleteMessageDialog(
							mContext, messageInfo.getId());

					dialog.show();
					System.out.println("OnLongClickListener:" + position);

					return true;
				}
			});

			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(mContext,
							MessagDetailsActivity.class);
					intent.putExtra("from", "messageCenter");
					intent.putExtra("id", messageInfo.getId());
					list.remove(position);
					messageInfo.setReaded(true);
					list.add(position, messageInfo);
					mContext.startActivity(intent);
				}
			});

			if (!messageInfo.isReaded()) {

				view.setBackgroundResource(R.color.message_item_color);
			} else {

				view.setBackgroundResource(R.color.message_item_bgcolor);
			}
		}
		return view;
	}

	public static class ViewHolder {

		TextView message_title;
		TextView message_date;

	}

}

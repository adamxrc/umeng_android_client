package com.umeng.android.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.android.bean.MarketCommentInfo;
import com.umeng.client.R;

public class MarketCommentAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mInflater;
	private ViewHolder viewHolder;
	private ArrayList<MarketCommentInfo> list;

	public MarketCommentAdapter(Context context,
			ArrayList<MarketCommentInfo> list) {
		this.mContext = context;
		this.list = list;
		mInflater = LayoutInflater.from(mContext);

	}

	public void notifyChanged(ArrayList<MarketCommentInfo> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount() {

		return list.size();
	}

	public MarketCommentInfo getItem(int arg0) {

		return list.get(arg0);
	}

	public long getItemId(int arg0) {

		return arg0;
	}

	public View getView(final int position, View view, ViewGroup parent) {
		if (view == null) {

			viewHolder = new ViewHolder();
			view = mInflater.inflate(R.layout.market_comment_item, null);
			viewHolder.author_name = (TextView) view
					.findViewById(R.id.comment_author);
			viewHolder.content = (TextView) view
					.findViewById(R.id.comment_content);
			viewHolder.time = (TextView) view.findViewById(R.id.comment_time);
			viewHolder.market_icon = (ImageView) view
					.findViewById(R.id.market_icon);

			view.setTag(viewHolder);

		} else {

			viewHolder = (ViewHolder) view.getTag();
		}

		final MarketCommentInfo marketCommentInfo = list.get(position);
		if (marketCommentInfo != null) {
			viewHolder.author_name.setText(marketCommentInfo.getAuthor_name());
			viewHolder.content.setText(marketCommentInfo.getContent());
			viewHolder.time.setText(marketCommentInfo.getTime());
			setIcon(marketCommentInfo.getMarket_id(), viewHolder.market_icon);

		}
		return view;
	}

	private void setIcon(String market_id, ImageView imageView) {

		if (TextUtils.isEmpty(market_id)) {
			return;
		} else if (market_id.equals("360")) {
			market_id = "qihu";
		}

		switch (MarketType.valueOf(market_id.toUpperCase())) {
		case QIHU:
			imageView.setImageResource(R.drawable.qihu);
			break;
		case WANDOUJIA:
			imageView.setImageResource(R.drawable.wandoujia);
			break;
		case JIFENG:
			imageView.setImageResource(R.drawable.jifeng);
			break;
		case BAIDU:
			imageView.setImageResource(R.drawable.baidu);
			break;
		case ANZHI:
			imageView.setImageResource(R.drawable.anzhi);
			break;
		case ANZHUOSHICHANG:
			imageView.setImageResource(R.drawable.anzhuo);
			break;
		case XIAOMI:
			imageView.setImageResource(R.drawable.xiaomi);
			break;
		case UC:
			imageView.setImageResource(R.drawable.uc);
			break;
		case YINGYONGHUI:
			imageView.setImageResource(R.drawable.yingyonghui);
			break;

		}
	}

	enum MarketType {
		QIHU, WANDOUJIA, JIFENG, BAIDU, ANZHI, ANZHUOSHICHANG, XIAOMI, UC, YINGYONGHUI
	}

	public static class ViewHolder {

		TextView author_name;
		TextView content;
		TextView time;
		ImageView market_icon;

	}

}

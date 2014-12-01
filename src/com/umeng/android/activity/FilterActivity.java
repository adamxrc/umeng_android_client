package com.umeng.android.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;
import com.umeng.client.R;

public class FilterActivity extends ActionBarActivity {

	public static final String INTENT_KEY_TIMESLOT_LIST = "INTENT_KEY_TIMESLOT_LIST";
	public static final String INTENT_KEY_CHANNEL_LIST = "INTENT_KEY_CHANNEL_LIST";
	public static final String INTENT_KEY_VERSION_LIST = "INTENT_KEY_VERSION_LIST";
	public static final String INTENT_KEY_TIMESLOT_SELECTED_INDEX = "INTENT_KEY_TIMESLOT_SELECTED_INDEX";
	public static final String INTENT_KEY_CHANNEL_SELECTED_INDEX = "INTENT_KEY_CHANNEL_SELECTED_INDEX";
	public static final String INTENT_KEY_VERSION_SELECTED_INDEX = "INTENT_KEY_VERSION_SELECTED_INDEX";
	public static final String INTENT_KEY_FROM_PAGE = "INTENT_KEY_FROM_PAGE";

	public static final int[] TIMESLOT_TYPE = new int[] { 7, 15, 30, 90, 180,
			360, 360000 };

	private List<Integer> filerConditionList;

	private List<String> timeslotList;
	private List<String> channelList;
	private List<String> versionList;
	private List<List<String>> lists = new ArrayList<List<String>>();
	private int selectedTimeslotIndex;
	private int selectedVersionIndex;
	private int selectedChannelIndex;

	private String fromPage;

	private ExpandableListView filterConditionListView;
	private FilterConditionListAdapter filterConditionListViewAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_filter);
		filerConditionList = new ArrayList<Integer>();
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			timeslotList = extras.getStringArrayList(INTENT_KEY_TIMESLOT_LIST);
			versionList = extras.getStringArrayList(INTENT_KEY_VERSION_LIST);
			channelList = extras.getStringArrayList(INTENT_KEY_CHANNEL_LIST);
			
			selectedTimeslotIndex = extras.getInt(
					FilterActivity.INTENT_KEY_TIMESLOT_SELECTED_INDEX, 0);
			selectedVersionIndex = extras.getInt(
					FilterActivity.INTENT_KEY_VERSION_SELECTED_INDEX, 0);
			selectedChannelIndex = extras.getInt(
					FilterActivity.INTENT_KEY_CHANNEL_SELECTED_INDEX, 0);

			fromPage = extras.getString(FilterActivity.INTENT_KEY_FROM_PAGE);
		}
		
		if(timeslotList!=null){
			filerConditionList.add(R.string.timeslot);
			lists.add(timeslotList);
		}
		if(channelList!=null){
			filerConditionList.add(R.string.channel_text);
			lists.add(channelList);
		}
		if(versionList!=null){
			filerConditionList.add(R.string.version_text);
			lists.add(versionList);
		}
		
		filterConditionListView = (ExpandableListView)findViewById(R.id.list);
		filterConditionListViewAdapter = new FilterConditionListAdapter(this);
		filterConditionListView.setAdapter(filterConditionListViewAdapter);

		filterConditionListView
				.setOnGroupExpandListener(new OnGroupExpandListener() {
					@Override
					public void onGroupExpand(int groupPosition) {
						int len = filterConditionListViewAdapter.getGroupCount();
						for (int i = 0; i < len; i++) {
							if (i != groupPosition) {
								filterConditionListView.collapseGroup(i);
							}
						}
					}
				});

		filterConditionListView
				.setOnGroupCollapseListener(new OnGroupCollapseListener() {
					@Override
					public void onGroupCollapse(int groupPosition) {
					}
				});
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
        getSupportActionBar().setTitle(R.string.filter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.memu_accept, menu);
        MenuItem refreshItem = menu.findItem(R.id.accept);
        MenuItemCompat.setShowAsAction(refreshItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.accept:
                Intent returnIntent = new Intent();
                returnIntent.putExtra(INTENT_KEY_TIMESLOT_SELECTED_INDEX,
                        selectedTimeslotIndex);
                returnIntent.putExtra(INTENT_KEY_CHANNEL_SELECTED_INDEX,
                        selectedChannelIndex);
                returnIntent.putExtra(INTENT_KEY_VERSION_SELECTED_INDEX,
                        selectedVersionIndex);
                setResult(RESULT_OK, returnIntent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

	class FilterConditionListAdapter extends BaseExpandableListAdapter {

		private Context myContext;

		public FilterConditionListAdapter(Context context) {
			myContext = context;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			boolean isSelected = false;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) myContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(
						R.layout.filter_condition_child_listview_item, null);
			}
			TextView itemName = (TextView) convertView
					.findViewById(R.id.item_name);
			itemName.setText(lists.get(groupPosition).get(childPosition));
			if(filerConditionList.get(groupPosition) == R.string.timeslot){
				isSelected = (selectedTimeslotIndex == childPosition);
			}else if(filerConditionList.get(groupPosition) == R.string.channel_text){
				isSelected = (selectedChannelIndex == childPosition);
			}else if(filerConditionList.get(groupPosition) == R.string.version_text){
				isSelected = (selectedVersionIndex == childPosition);
			}
			ImageView radioButton = (ImageView) convertView
					.findViewById(R.id.item_radio);
			if(isSelected)
				radioButton.setImageResource(R.drawable.btn_radio_on);
			else
				radioButton.setImageDrawable(null);

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					HashMap<String, String> condition_sift = new HashMap<String, String>();
					condition_sift.put("from_page", fromPage);
					if(filerConditionList.get(groupPosition) == R.string.timeslot){
						selectedTimeslotIndex = childPosition;
						condition_sift.put("type", "timeslot");
						condition_sift.put("timeslot_type", timeslotList.get(selectedTimeslotIndex));
					}else if(filerConditionList.get(groupPosition) == R.string.channel_text){
						selectedChannelIndex = childPosition;
						condition_sift.put("type", "channels");
					}else if(filerConditionList.get(groupPosition) == R.string.version_text){
						selectedVersionIndex = childPosition;
						condition_sift.put("type", "versions");
					}
					MobclickAgent.onEvent(FilterActivity.this,
							"condition_sift", condition_sift);
					notifyDataSetChanged();
					filterConditionListView.collapseGroup(groupPosition);
				}

			});

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return lists.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public int getGroupCount() {
			return lists.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) myContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(
						R.layout.filter_condition_listview_item, null);
			}
			TextView sectionName = (TextView) convertView
					.findViewById(R.id.section_name);
			TextView selectedChildName = (TextView) convertView
					.findViewById(R.id.selected_child_name);

			sectionName.setText(filerConditionList.get(groupPosition));
			if(filerConditionList.get(groupPosition) == R.string.timeslot){
				selectedChildName.setText(lists.get(groupPosition).get(selectedTimeslotIndex));
			}else if(filerConditionList.get(groupPosition) == R.string.channel_text){
				selectedChildName.setText(lists.get(groupPosition).get(selectedChannelIndex));
			}else if(filerConditionList.get(groupPosition) == R.string.version_text){
				selectedChildName.setText(lists.get(groupPosition).get(selectedVersionIndex));
			}

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
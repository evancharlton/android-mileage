package com.evancharlton.mileage.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.evancharlton.mileage.R;
import com.evancharlton.mileage.dao.Vehicle;
import com.evancharlton.mileage.provider.Statistics;
import com.evancharlton.mileage.provider.Statistics.Statistic;
import com.evancharlton.mileage.provider.Statistics.StatisticsGroup;

public class VehicleStatisticsAdapter extends BaseAdapter {
	private static final int TYPE_STATISTIC = 0;
	private static final int TYPE_GROUP = 1;
	private static final ArrayList<StatisticHolder> mObjects = new ArrayList<StatisticHolder>();

	private final LayoutInflater mInflater;

	public VehicleStatisticsAdapter(Context context, Vehicle vehicle) {
		mInflater = LayoutInflater.from(context);

		for (StatisticsGroup group : Statistics.GROUPS) {
			mObjects.add(new StatisticHolder(context, group));
			for (Statistic statistic : group.getStatistics()) {
				mObjects.add(new StatisticHolder(context, statistic, vehicle));
			}
		}
	}

	@Override
	public int getCount() {
		return mObjects.size();
	}

	@Override
	public Object getItem(int position) {
		return mObjects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StatisticHolder holder = mObjects.get(position);

		if (convertView == null) {
			switch (holder.type) {
				case TYPE_STATISTIC:
					convertView = mInflater.inflate(R.layout.statistic, parent, false);
					break;
				case TYPE_GROUP:
					convertView = mInflater.inflate(R.layout.divider, parent, false);
					break;
			}
		}

		ViewHolder viewHolder = (ViewHolder) convertView.getTag();
		if (viewHolder == null) {
			viewHolder = new ViewHolder(convertView);
		}

		switch (holder.type) {
			case TYPE_STATISTIC:
				viewHolder.text.setText(holder.text);
				break;
			case TYPE_GROUP:
				viewHolder.text.setText(holder.text);
				break;
		}

		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return mObjects.get(position).type;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) == TYPE_STATISTIC;
	}

	private static class StatisticHolder {
		public final String text;
		public final int type;

		public StatisticHolder(Context context, Statistic statistic, Vehicle vehicle) {
			text = statistic.getLabel(context, vehicle);
			type = TYPE_STATISTIC;
		}

		public StatisticHolder(Context context, StatisticsGroup group) {
			text = context.getString(group.getLabel());
			type = TYPE_GROUP;
		}
	}

	private static class ViewHolder {
		public final TextView text;

		public ViewHolder(View convertView) {
			text = (TextView) convertView.findViewById(android.R.id.text1);
			convertView.setTag(this);
		}
	}
}

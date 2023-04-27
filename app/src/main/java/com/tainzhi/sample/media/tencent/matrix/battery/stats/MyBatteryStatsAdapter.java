package com.tainzhi.sample.media.tencent.matrix.battery.stats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.matrix.batterycanary.stats.BatteryRecord;
import com.tencent.matrix.batterycanary.stats.ui.BatteryStatsAdapter;


import com.tencent.matrix.batterycanary.R;
/**
 * @author Kaede
 * @since 2021/12/16
 */
class MyBatteryStatsAdapter extends BatteryStatsAdapter {
    public static final int VIEW_TYPE_EVENT_SIMPLE_CUSTOM = 101;

    @Override
    public int getItemViewType(int position) {
        Item item = dataList.get(position);
        if (item instanceof Item.EventSimpleItem) {
            if ("自定义事件".equals(((BatteryRecord.EventStatRecord) item).event)) {
                return VIEW_TYPE_EVENT_SIMPLE_CUSTOM;
            }
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EVENT_SIMPLE_CUSTOM) {
            return new MyEventSimpleHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.stats_item_event_simple, parent, false));
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    static class MyEventSimpleHolder extends ViewHolder.EventSimpleHolder {
        public MyEventSimpleHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected String getDetailInfo() {
            return "String=" + mItem.record.extras.get("String") +
                    "\nDigit=" + mItem.record.extras.get("Digit") +
                    "\nBoolean=" + mItem.record.extras.get("Boolean");
        }
    }
}

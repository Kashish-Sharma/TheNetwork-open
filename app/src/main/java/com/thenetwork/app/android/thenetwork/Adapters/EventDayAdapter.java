package com.thenetwork.app.android.thenetwork.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thenetwork.app.android.thenetwork.HelperClasses.EventPerDay;
import com.thenetwork.app.android.thenetwork.R;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.List;

/**
 * Created by Kashish on 15-06-2018.
 */

public class EventDayAdapter extends ExpandableRecyclerViewAdapter<EventDayAdapter.DayViewHolder, EventDayAdapter.EventPerDayViewHolder> {

    private Context mContext;

    public EventDayAdapter(List<? extends ExpandableGroup> groups, Context context) {
        super(groups);
        this.mContext = context;
    }

    @Override
    public DayViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item_event_day,parent,false);
        return new DayViewHolder(view);
    }

    @Override
    public EventPerDayViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item_event_day_plan,parent,false);
        return new EventPerDayViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(EventPerDayViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {

        EventPerDay eventPerDay = (EventPerDay) group.getItems().get(childIndex);
        holder.eventStartTime.setText(eventPerDay.getTimeFrom());
        holder.eventName.setText(eventPerDay.getName());
        holder.eventEndTime.setText(eventPerDay.getTimeTo());

    }

    @Override
    public void onBindGroupViewHolder(DayViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setEventDay(group.getTitle());
    }

    public class DayViewHolder extends GroupViewHolder {

    private TextView eventDay;

    public DayViewHolder(View itemView) {
        super(itemView);
        eventDay = itemView.findViewById(R.id.event_map_day);
    }

    public void setEventDay(String name) {
        eventDay.setText(name);
    }
}

    public class EventPerDayViewHolder extends ChildViewHolder {

        private TextView eventEndTime;
        private TextView eventStartTime;
        private TextView eventName;

        public EventPerDayViewHolder(View itemView) {
            super(itemView);

            eventEndTime = itemView.findViewById(R.id.event_end_time_plan);
            eventStartTime = itemView.findViewById(R.id.event_start_time_plan);
            eventName = itemView.findViewById(R.id.event_plan_name);

        }
    }

}

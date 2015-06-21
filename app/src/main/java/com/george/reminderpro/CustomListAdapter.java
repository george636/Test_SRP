package com.george.reminderpro;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomListAdapter extends ArrayAdapter<Reminder> {

    private final Context context;
    private final List<Reminder> reminders;


    public CustomListAdapter(Activity context, List<Reminder> reminders) {
        super(context, R.layout.list_view_item, reminders);

        this.context    = context;
        this.reminders  = reminders;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.list_view_item, parent, false);

        TextView reminderTitleTextView  = (TextView) rowView.findViewById(R.id.reminderTitle);
        TextView reminderTimeTextView   = (TextView) rowView.findViewById(R.id.reminderTime);
        TextView reminderDateTextView   = (TextView) rowView.findViewById(R.id.reminderDate);
        TextView reminderStatus         = (TextView) rowView.findViewById(R.id.reminderStatus);

        reminderTitleTextView.setText(reminders.get(position).getDescription());
        reminderTimeTextView.setText(reminders.get(position).getTime());
        reminderDateTextView.setText(reminders.get(position).getDate());
        if (reminders.get(position).getStatus() == 0) {
            reminderStatus.setText(R.string.inactive);
        }
        else {
            reminderStatus.setText("");
        }

        return rowView;
    }
}
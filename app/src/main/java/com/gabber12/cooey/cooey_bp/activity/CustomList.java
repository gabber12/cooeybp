package com.gabber12.cooey.cooey_bp.activity;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gabber12.cooey.cooey_bp.R;

import java.util.List;

/**
 * Created by shubham.sharma on 26/12/17.
 */


public class CustomList extends ArrayAdapter<String> {
    private final Activity context;
    private final List<Device> web;

    public CustomList(Activity context,
                      List<Device> web) {
        super(context, R.layout.list_single);
        this.context = context;
        this.web = web;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.deviceName);

        txtTitle.setText(web.get(position).deviceName);
        TextView txtTitle1 = (TextView) rowView.findViewById(R.id.macAddress);

        txtTitle1.setText(web.get(position).address);
        return rowView;
    }

    @Override
    public int getCount() {
        return web.size();
    }
}
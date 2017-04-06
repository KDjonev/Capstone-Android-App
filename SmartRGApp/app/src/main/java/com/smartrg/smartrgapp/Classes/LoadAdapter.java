package com.smartrg.smartrgapp.Classes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.smartrg.smartrgapp.R;

/**
 * Created by brand_000 on 2/25/2017.
 */
public class LoadAdapter extends ArrayAdapter<HeatMap> {

    private ArrayList<HeatMap> dataSet;
    Context mContext;

    private static class ViewHolder {
        TextView textDate;
    }

    public LoadAdapter(Context c, ArrayList<HeatMap> items) {
        super(c, R.layout.load_list_item, items);
        this.dataSet = items;
        this.mContext = c;

    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HeatMap heatMap = getItem(position);
        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.load_list_item, parent, false);
            viewHolder.textDate = (TextView) convertView.findViewById(R.id.load_date_text);

            result = convertView;
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        lastPosition = position;

        viewHolder.textDate.setText(heatMap.getCreatedDate());

        return convertView;
    }
}
package com.smartrg.smartrgapp.Classes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartrg.smartrgapp.R;

import java.util.ArrayList;

/**
 * Created by root on 5/4/17.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>{

    private ArrayList<Device> devices;
    private Context context;

    public MyRecyclerViewAdapter(ArrayList<Device> d) {
        devices = d;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_device, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Device device = devices.get(position);
        holder.setIsRecyclable(false);
        holder.device_ip.setText("IP: " + device.getIp());
        holder.device_mac.setText("MAC: " +device.getMac());
        holder.device_name.setText(device.getName());
        String d_name = device.getName();
        String android = "android";
        String laptop = "latitude";
        String router = "sr400";
        if (d_name.toLowerCase().indexOf(android.toLowerCase()) != -1) {
            holder.image.setImageResource(R.drawable.android2);
        } else if (d_name.toLowerCase().indexOf(laptop.toLowerCase()) != -1) {
            holder.image.setImageResource(R.drawable.laptop2);
        } else if (d_name.toLowerCase().indexOf(router.toLowerCase()) != -1) {
            holder.image.setImageResource(R.drawable.smart_router);
        } else holder.image.setImageResource(R.drawable.question);
    }



    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {


        public ImageView image;
        public TextView device_ip, device_name, device_mac;

        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.device_image);
            device_ip = (TextView)v.findViewById(R.id.device_ip);
            device_mac = (TextView)v.findViewById(R.id.device_mac);
            device_name = (TextView)v.findViewById(R.id.device_name);

        }
    }

}

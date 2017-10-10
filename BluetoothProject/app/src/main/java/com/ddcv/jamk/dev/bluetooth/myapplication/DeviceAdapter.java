package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.bluetooth.BluetoothDevice;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by K1967 on 10.10.2017.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private ArrayMap<String, BluetoothDevice> deviceArrayMap;

    getSelectedDevice mCallback;

    public interface getSelectedDevice {
        public void selectDevice();
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mCallback = (getSelectedDevice) recyclerView;
    }

    public DeviceAdapter(ArrayMap<String, BluetoothDevice> devList)
    {
        deviceArrayMap = devList;
    }

    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceAdapter.ViewHolder holder, int position) {
        String DeviceAddress = deviceArrayMap.keyAt(position);
        holder.deviceImage.setImageResource(R.drawable.ic_bluetooth_connected_black_24dp);
        holder.deviceAddress.setText(DeviceAddress);
        holder.deviceStatus.setText("NOPE!");
    }

    @Override
    public int getItemCount() {
        return deviceArrayMap.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView deviceImage;
        public TextView deviceAddress;
        public TextView deviceStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            deviceImage = (ImageView)itemView.findViewById(R.id.logo);
            deviceAddress = (TextView)itemView.findViewById(R.id.Address);
            deviceStatus = (TextView)itemView.findViewById(R.id.Status);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //we got a click
                    Log.w("Got CLICK", "We received a click from you! " + getAdapterPosition());



                    return false;
                }
            });

        }
    }

}

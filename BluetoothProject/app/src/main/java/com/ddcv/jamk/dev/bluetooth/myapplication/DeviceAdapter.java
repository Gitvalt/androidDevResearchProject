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
 * @class   DeviceAdapter   Display data in a RecyclerView element
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    /**
     * @member  deviceArrayMap   Map of found devices
     */
    private ArrayMap<String, BluetoothDevice> deviceArrayMap;
    public DeviceListener callback;



    public interface DeviceListener {
        void selectDevice(BluetoothDevice selectedDevice, BluetoothConnectionManager.DeviceAction action);
    }

    //Constructor
    public DeviceAdapter(ArrayMap<String, BluetoothDevice> devList, DeviceListener deviceListener)
    {
        deviceArrayMap = devList;
        this.callback = deviceListener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {

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

        int state = deviceArrayMap.valueAt(position).getBondState();

        switch (state){
            case BluetoothDevice.BOND_BONDING:
                holder.deviceStatus.setText("Bonding...");
                break;
            case BluetoothDevice.BOND_BONDED:
                holder.deviceStatus.setText("Bonded with device!");
                break;
            case BluetoothDevice.BOND_NONE:
                holder.deviceStatus.setText("Not bonded");
                break;
        }


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
                    Log.w("Got CLICK", "We received a long click from you! At position " + getAdapterPosition());

                    BluetoothDevice selectedDevice = deviceArrayMap.valueAt(getAdapterPosition());
                    boolean continueActions = true;
                    BluetoothConnectionManager.DeviceAction action = null;


                    switch (selectedDevice.getBondState()){
                        case BluetoothDevice.BOND_BONDED:
                            action = BluetoothConnectionManager.DeviceAction.connect;
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            continueActions = false;
                            break;
                        case BluetoothDevice.BOND_NONE:
                            action = BluetoothConnectionManager.DeviceAction.Bond;
                            break;
                    }

                    if(continueActions) {
                        callback.selectDevice(selectedDevice, action);
                    }
                    else {
                        Log.w("Bluetooth", "Device is still bonding");
                    }
                    return false;
                }
            });

        }
    }

}

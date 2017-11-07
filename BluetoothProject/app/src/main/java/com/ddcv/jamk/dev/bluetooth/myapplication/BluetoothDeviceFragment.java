package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by K1967 on 7.11.2017.
 */

public class BluetoothDeviceFragment extends Fragment {

    /**
     * @member  BluetoothDevice  mBluetoothDevice    The selected bluetooth device
     */

    private BluetoothDevice mBluetoothDevice;

    /**
     * @return  Response with copy of the selected mBluetoothDevice
     */
    public BluetoothDevice getSelectedBluetoothDevice() { return mBluetoothDevice; }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bluetooth_device_fragment, container, false);
        return view;
    }


    public void setBluetoothDevice(BluetoothDevice device){
        mBluetoothDevice = device;
    }

    /**
     * Return to devicelist fragment
     */
    public void onReturn(){

    }

}

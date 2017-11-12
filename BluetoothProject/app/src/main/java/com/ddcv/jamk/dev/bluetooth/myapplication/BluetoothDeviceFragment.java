package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by K1967 on 7.11.2017.
 */

public class BluetoothDeviceFragment extends android.support.v4.app.Fragment {

    /**
     * @member  BluetoothDevice  mBluetoothDevice    The selected bluetooth device
     */
    private BluetoothDevice mBluetoothDevice;

    private TextView deviceAddress;
    private TextView deviceBondStatus;
    private TextView deviceConnectionStatus;


    /**
     * @return  Response with copy of the selected mBluetoothDevice
     */
    public BluetoothDevice getSelectedBluetoothDevice() { return mBluetoothDevice; }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bluetooth_device_fragment, container, false);

        //setup UI elements
        deviceAddress = view.findViewById(R.id.deviceName);
        deviceBondStatus = view.findViewById(R.id.deviceStatus);
        deviceConnectionStatus = view.findViewById(R.id.connectionStatus);

        //setup device name
        String deviceName = getArguments().getString("Name");

        if(deviceName == null)
        {
            deviceAddress.setText("Name not available");
        }
        else
        {
            deviceAddress.setText(deviceName);
        }


        //setup current device to device bluetooth bond status
        int bondStatus = getArguments().getInt("Bond_Status");
        int bondStatus_text;

        switch (bondStatus)
        {
            case BluetoothDevice.BOND_BONDED:
                bondStatus_text = R.string.Bonded;
                break;

            case BluetoothDevice.BOND_NONE:
                bondStatus_text = R.string.noBond;
                break;

            default:
                bondStatus_text = R.string.unkown_status;
                break;
        }

        deviceBondStatus.setText(bondStatus_text);

        //setup "try connecting to device"-button
        Button button = (Button) view.findViewById(R.id.retry_connection_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeReload();
            }
        });

        //onclick change fragment to messaging fragment
        Button messagingButton = (Button)view.findViewById(R.id.messaging_button);
        messagingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity().getClass() == MainActivity.class)
                {
                    MainActivity activity = (MainActivity)getActivity();
                    BluetoothMessagingFragment messagingFragment = new BluetoothMessagingFragment();

                    Bundle data = new Bundle();
                    data.putString("DeviceMAC", getArguments().getString("Address"));

                    messagingFragment.setArguments(data);

                    activity.changeMainFragment(messagingFragment);
                }
            }
        });


        executeReload();

        return view;
    }

    private void setConnectionStatus(boolean status)
    {
        if(status)
        {
            deviceConnectionStatus.setText(R.string.conn_available);
            deviceConnectionStatus.setTextColor(Color.GREEN);
        }
        else
        {
            deviceConnectionStatus.setText(R.string.conn_unavailable);
            deviceConnectionStatus.setTextColor(Color.RED);
        }
    }

    private void reloadConnectionStatus()
    {
        if(getActivity().getClass() == MainActivity.class)
        {
            MainActivity activity = (MainActivity)getActivity();

            final BluetoothConnectionManager manager = activity.getBluetoothController();
            final String deviceMAC = getArguments().getString("Address");
            final BluetoothDevice device = manager.getDeviceWithMac(deviceMAC);

            boolean connResponse = manager.testConnection(device);
            setConnectionStatus(connResponse);
            /*
            Handler handler = new Handler();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    boolean connResponse = manager.testConnection(device);
                    setConnectionStatus(connResponse);
                }
            });
            */
        }
    }

    private void executeReload()
    {
        deviceConnectionStatus.setText(R.string.conn_testing);

        Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run()
            {
                reloadConnectionStatus();
            }
        });

        return;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public Object getExitTransition() {
        return super.getExitTransition();
    }
}

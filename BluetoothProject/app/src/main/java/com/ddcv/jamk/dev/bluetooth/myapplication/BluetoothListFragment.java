package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Created by K1967 on 7.11.2017.
 */

public class BluetoothListFragment extends Fragment implements DeviceAdapter.DeviceListener {

    /**
     * @member  ProgressBar                 spinningCircle      Shown when devices are searched
     *
     * @member  RecyclerView                deviceList          List of found devices
     * @member  RecyclerView.Adapter        mAdapter            Manages devices shown in the list
     * @member  RecyclerView.LayoutManager  layoutManager       Manager the overlay of the list
     *
     * @member  MainActivity                mainActivity        link to main activity
     */
    public ProgressBar spinningCircle;

    private RecyclerView deviceList;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private MainActivity mainActivity;

    public SelectionListener callback;

    public interface SelectionListener {
        void setSelectedDevice(BluetoothDevice selectedDevice, BluetoothConnectionManager.DeviceAction action);
    }

    /**
     * When View is created on the activity, setup basic stuff
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //setup inflater
        View view = inflater.inflate(R.layout.bluetooth_list_fragment, container, false);

        //setup progressbar for scanning devices
        spinningCircle = (ProgressBar)view.findViewById(R.id.progressBar);

        //list of shown devices
        deviceList = (RecyclerView)view.findViewById(R.id.listView);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());

        deviceList.setLayoutManager(layoutManager);

        return view;
    }

    /**
     * After fragment has been created, add reference to parent activity
     * @param activity  the main activity that has create this fragment
     */
    public void setBluetoothManager(MainActivity activity, SelectionListener callbackFunction)
    {
        mainActivity = activity;
        DeviceAdapter deviceListAdapter = new DeviceAdapter(activity.getBluetoothController().foundDevices, this);
        deviceList.setAdapter(deviceListAdapter);

        this.callback = callbackFunction;
    }


    /**
     * Clear current list and start looking for new bluetooth devices
     */
    public void lookForDevices(){

        ArrayMap<String, BluetoothDevice> foundDevices = mainActivity.getBluetoothController().foundDevices;

        boolean isListEmpty = foundDevices.isEmpty();

        //Empty list
        if(!isListEmpty)
        {
            foundDevices.clear();
            deviceList.getAdapter().notifyDataSetChanged();
        }

        mainActivity.getBluetoothController().FindNewDevices();
        //program continues in BroadcastReceiver.ActionDiscovery finished
    }


    //onClick functions:

        /**
         * Clear list, search new devices
         */
        public void onRefreshListClick(View parent){
            /*
            Log.i("Bluetooth", "Finding new devices");
            BluetoothController.FindNewDevices();
            */
        }


        public void onExitClick(View parent){
            Intent leave = new Intent(Intent.ACTION_MAIN);
            leave.addCategory(Intent.CATEGORY_HOME);
            leave.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(leave);
        }

    //---

    /**
     * Update shown recyclerView when new bluetooth devices has been found
     */
    public void updateDeviceList(){


        //deviceList.setAdapter(mAdapter);
        ArrayMap<String, BluetoothDevice> foundDevices = mainActivity.getBluetoothController().foundDevices;


        if(foundDevices == null)
        {
            Log.e("Bluetooth", "updating devicelist, but the found devices array is null");
            return;
        }
        else if(foundDevices.size() <= 0)
        {
            Log.e("Bluetooth", "No devices available");
            return;
        }
        else
        {
            Log.i("Bluetooth", "Notifying bluetooth_list data has changed.");
            //refresh layout. Without refresh recyclerview will keep strange unnaturally long padding.
            deviceList.setLayoutManager(layoutManager);
            deviceList.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * check if the activity has bluetoothmanager
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


    //----


    //Handling recycler view events:
    /**
     * Get callback from DeviceAdapter and execute pairing
     */
    @Override
    public void selectDevice(BluetoothDevice selectedDevice, BluetoothConnectionManager.DeviceAction action) {

        Log.i("Bluetooth", "Bluetooth list fragment sents a callback to main activity");
        callback.setSelectedDevice(selectedDevice, action);
    }

}

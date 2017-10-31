package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.ddcv.jamk.dev.bluetooth.myapplication.BluetoothConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends Activity implements DeviceAdapter.DeviceListener {


    /**
     * @member  ProgressBar                 Shown when devices are searched
     * @member  RecyclerView                List of found devices
     * @member  RecyclerView.Adapter        Manages devices shown in the list
     * @member  RecyclerView.LayoutManager  Manager the overlay of the list
     *
     * @member  BluetoothConnectionManager  Class that handles bluetooth connections and found bluetooth devices
     *
     * @member  ENABLE_BLUETOOTH_CODE       Tag for permissions
     * @member  ALLOW_BLUETOOTH_CODE        Tag for permissions
     *
     */

    private ProgressBar spinningCircle;

    private RecyclerView deviceList;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private BluetoothConnectionManager BluetoothController;

    public static final int ENABLE_BLUETOOTH_CODE = 123;
    public static final int ALLOW_BLUETOOTH_CODE = 456;


    /**
     * App is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setup progressbar for scanning devices
        spinningCircle = (ProgressBar)findViewById(R.id.scanningSpinner);

        //list of shown devices
        deviceList = (RecyclerView)findViewById(R.id.listView);
        layoutManager = new LinearLayoutManager(this);

        BluetoothController = new BluetoothConnectionManager(getApplicationContext(), this, mReceiver);

        deviceList.setLayoutManager(layoutManager);
        DeviceAdapter deviceListAdapter = new DeviceAdapter(BluetoothController.foundDevices, this);
        deviceList.setAdapter(deviceListAdapter);

        //check if phone supports bluetooth communication
        if (BluetoothController.mBluetoothAdapter == null)
        {

            //Bluetooth is not supported
            Log.e("Bluetooth", "Bluetooth is not supported by this device");

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Bluetooth not supported");
                alert.setMessage("I guess this phone cannot use Bluetooth..." + "\n" + "\n" + "Hint: Android emulator cannot emulate bluetooth, use real phone instead");
                alert.setCancelable(false);
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //close application
                        System.exit(0);
                    }
                });
            AlertDialog dialog = alert.create();

            dialog.show();
        }
        else
        {
            //bluetooth is supported
            Log.i("Bluetooth", "Bluetooth is implemented!");
            boolean isEnabled = BluetoothController.mBluetoothAdapter.isEnabled();

            /**
             * is bluetooth on?
             * if yes, then we register what we want to listen and start looking for devices
             * if no, then ask to enable from user and wait for onActivityResult
             */
            if(isEnabled){
                BluetoothController.registerBluetoothService();
                lookForDevices();
            } else {
                BluetoothController.setBluetoothEnabled(true);
                Log.i("Bluetooth_status", "Bluetooth is not Enabled");
            }
        }



    }

    /**
     * Clear current list and start looking for new bluetooth devices
     */
    private void lookForDevices(){

        ArrayMap<String, BluetoothDevice> foundDevices = BluetoothController.foundDevices;

        boolean isListEmpty = foundDevices.isEmpty();

        //Empty list
        if(!isListEmpty)
        {
            foundDevices.clear();
            deviceList.getAdapter().notifyDataSetChanged();
        }

        BluetoothController.FindNewDevices();
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

    //Receivers:

    /**
     * @callback mReceiver
     * When new bluetooth device is detected, while we are scanning for new devices
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {

            String action = intent.getAction();
            //BluetoothController.mBluetoothAdapter.cancelDiscovery();

            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    Log.i("Bluetooth_broadcast", "staring discovery...");
                    spinningCircle.setVisibility(View.VISIBLE);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Log.i("Bluetooth_broadcast", "discovery fisihed");
                    spinningCircle.setVisibility(View.INVISIBLE);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    //bluetooth device is found.

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    BluetoothClass deviceClass = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);

                    BluetoothController.foundDevices.put(device.getAddress(), device);

                    //hide progressbar
                    spinningCircle.setVisibility(View.INVISIBLE);

                    updateDeviceList();

                    Log.i("Bluetooth_broadcast", "Bluetooth device detected!");
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:

                    Log.i("state_changed", "state has changed!");

                    BluetoothDevice deviceInput = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    switch (deviceInput.getBondState()){
                        case BluetoothDevice.BOND_BONDED:
                            Log.i("state_changed", "Bonded!");
                            break;
                        case BluetoothDevice.BOND_BONDING:
                            Log.i("state_changed", "Bonding!");
                            break;
                        case BluetoothDevice.BOND_NONE:
                            Log.i("state_changed", "No Bond!");
                            break;
                    }

                    updateDeviceList();
                    break;
            }
        }
    };


    /**
     * If permissions were granted
     * @param requestCode   Code to indetify send request
     * @param permissions      what permissions were asked
     * @param grantResults      what was the response from phone
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case ALLOW_BLUETOOTH_CODE:
                BluetoothController.FindNewDevices();
                break;
            default:
                break;
        }
    }


    /**
     * Was bluetooth enabled?
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case ENABLE_BLUETOOTH_CODE:
                //Bluetooth request accepted?
                if (resultCode == RESULT_OK) {
                    //if accepted register bluetooth service
                    BluetoothController.registerBluetoothService();
                    lookForDevices();
                }
                else
                {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Permission required");
                    builder.setMessage("You need to give permissions for bluetooth to used");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            BluetoothController.setBluetoothEnabled(true);
                        }
                    });

                    builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i("App", "Exiting program");
                            System.exit(0);
                        }
                    });


                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
                break;

            case ALLOW_BLUETOOTH_CODE:
                if (resultCode == RESULT_OK) {
                    BluetoothController.FindNewDevices();
                } else {
                    //bluetooth off
                    Toast.makeText(getApplicationContext(), "Bluetooth access permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    //----


    //Handling recycler view events:
    /**
     * Get callback from DeviceAdapter and execute pairing
     */
    @Override
    public void selectDevice(BluetoothDevice selectedDevice, BluetoothConnectionManager.DeviceAction action) {
        Log.i("Bluetooth", "Item has been selected + " + action);
        Toast.makeText(getApplicationContext(), "Got interface", Toast.LENGTH_SHORT).show();

        switch (action){
            case Bond:
                Log.i("Bluetooth", "Bonding with device '" + selectedDevice.getName() + "' at " + selectedDevice.getAddress());
                selectedDevice.createBond();
                break;

            case unBond:
                Log.i("Bluetooth", "Breaking bond with device '" + selectedDevice.getName() + "' at " + selectedDevice.getAddress());
                try {
                    Method m = selectedDevice.getClass().getMethod("removeBond", (Class[]) null);
                    m.invoke(selectedDevice, (Object[]) null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Warning");
                    builder.setMessage("Currently only the phone is unpaired. Other device still thinks there is a connection. For reconnection remove pairing manually from other device");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                }
                catch(Exception e)
                {
                    Log.e("Bluetooth error", "Removing bond failed");
                }
                break;
            case connect:
                //BluetoothController.sendMessage("Hello server!", selectedDevice);
                BluetoothController.getMessages(selectedDevice);

                break;

            //sends a message to the bluetooth device
            case message:
                //BluetoothController.sendMessage("Hello server!", selectedDevice);
                break;
        }

    }

    /**
     * Update shown recyclerView when new bluetooth devices has been found
     */
    private void updateDeviceList(){


        //deviceList.setAdapter(mAdapter);
        ArrayMap<String, BluetoothDevice> foundDevices = BluetoothController.foundDevices;


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
            Log.i("Bluetooth", "Notifying data has changed.");
            //refresh layout. Without refresh recyclerview will keep strange unnaturally long padding.
            deviceList.setLayoutManager(layoutManager);
            deviceList.getAdapter().notifyDataSetChanged();
        }
    }

    //----

    /**
     * When app is closed, remove registers
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
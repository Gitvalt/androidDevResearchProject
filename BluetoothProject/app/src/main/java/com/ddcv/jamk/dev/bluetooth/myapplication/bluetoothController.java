package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;

import java.util.Set;

/**
 * Created by K1967 on 25.10.2017.
 */

public class bluetoothController {

    private Context mParent;
    private Activity mActivity;

    private BroadcastReceiver mReceiver;

    public ArrayMap<String, BluetoothDevice> foundDevices;
    public BluetoothAdapter mBluetoothAdapter;

    private static final int ENABLE_BLUETOOTH_CODE = 123;
    private static final int ALLOW_BLUETOOTH_CODE = 456;

    public enum DeviceAction {
        Bond,
        unBond
    }

    public bluetoothController(Context parent, Activity parent_activity, BroadcastReceiver receiver) {
        mParent = parent;
        mReceiver = receiver;
        mActivity = parent_activity;


        //init private foundDevice param
        foundDevices = new ArrayMap<>();

        //check if bluetooth is supported with this device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }



    /**
     * Make this phone discoverable for by other devices
     */
    private void activateDiscoverability(){

        //Discover me
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);

        mParent.startActivity(discoverableIntent);

    }

    /**
     * set app to register thrown bluetooth broadcasts and then move to finding devices
     */
    public void registerBluetoothService(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mParent.registerReceiver(mReceiver, filter);
        mParent.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    /**
     * Check if selected device has been paired with
     * @param device, device under inception
     */
    public boolean IsDevicePaired(BluetoothDevice device)
    {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice[] devices = (BluetoothDevice[])pairedDevices.toArray();

        boolean found = false;
        for(int i = 0; i < devices.length; i++){
            if(devices[i].equals(device)){
                found = true;
            }
        }

        if(found){
            return true;
        }
        else {
            return false;
        }

    }

    /**
     * Read already paired BluetoothDevices from phone's bluetooth adapter
     */
    public Set<BluetoothDevice> getPairedDevices(){
        /**
         Get paired devices
         */
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        return pairedDevices;
    }


    private boolean FindNewDevices_permissions(){
        //check if app has permission to use Bluetooth (X >= Android 6.0)
        int selfPermission = PermissionChecker.checkSelfPermission(mParent.getApplicationContext(), Manifest.permission.BLUETOOTH);
        int adminPermission = PermissionChecker.checkSelfPermission(mParent.getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN);

        int fineLocation = PermissionChecker.checkSelfPermission(mParent.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = PermissionChecker.checkSelfPermission(mParent.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        int granted = PermissionChecker.PERMISSION_GRANTED;

        /**
         * If some of the necessary permissions are not granted, App will ask the user for them...
         */
        if(selfPermission != granted || adminPermission != granted || fineLocation != granted || coarseLocation != granted)
        {
            /**
             * Show alert dialog informing what permissions are required for this app to work
             */
            AlertDialog.Builder alert = new AlertDialog.Builder(mParent);
            alert.setTitle("Permissions have not been granted");
            alert.setMessage("Permissions for coarse/fine location and Bluetooth are required");
            alert.setCancelable(false);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    /**
                     * Send the required permissions permissionRequest
                     */
                    ActivityCompat.requestPermissions(mActivity, new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, ALLOW_BLUETOOTH_CODE);
                }
            });

            alert.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //close application
                    System.exit(0);
                }
            });

            AlertDialog dialog = alert.create();
            dialog.show();
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Check permissions for detecting bluetooth devices and start scanning
     */
    public void FindNewDevices() {

        final android.os.Handler handler = new android.os.Handler();

        //check necessary permissions
        boolean hasPermissions = FindNewDevices_permissions();

        if(hasPermissions) {

            /**
             * start searching devices
             */
            if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON)
            {
                //handle reading bonded devices
                if(getPairedDevices().size() > 0)
                {
                    for(BluetoothDevice bluetoothDevice : getPairedDevices()){
                        if(foundDevices.containsValue(bluetoothDevice)){
                            //do nothing (no duplicates)
                        }
                        else {
                            //add paired device to found devices
                            foundDevices.put(bluetoothDevice.getAddress(), bluetoothDevice);
                        }
                    }
                }

                //start looking for devices
                mBluetoothAdapter.startDiscovery();

                //close search after {10 s} time
                long time = 10 * 1000;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothAdapter.cancelDiscovery();
                        Log.i("Bluetooth_Scanning", "Ending scanning...");
                    }
                }, time);

            }
            else
            {
                Log.e("Bluetooth", "Bluetooth service is not on! Cant start scanning for new devices!");
            }
        } else {
            Log.i("Bluetooth", "Permissions have not been given");
        }
    }


}

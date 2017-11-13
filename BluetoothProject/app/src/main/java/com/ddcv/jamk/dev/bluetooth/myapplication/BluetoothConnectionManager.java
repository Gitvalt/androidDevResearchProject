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

import java.util.Set;
import java.util.UUID;

/**
 * Created by K1967 on 25.10.2017.
 */

public class BluetoothConnectionManager {

    /**
     * @member  Context             mContext             Context of parent Activity
     * @member  Activity            mActivity           Parent Activity
     * @member  BroadcastReceiver   mReceiver           BroadcastReceiver listens for changes is bluetooth connection and found devices. (Connections such as scanning, pairing)
     * @member  foundDevices        foundDevices        List of found BluetoothDevices
     * @member  BluetoothAdapter    mBluetoothAdapter   Adapter that handles current bluetooth connections
     * @member  int                 mScanningTimeout    How many second until stopping discover devices
     * @member  UUID                myUUID               This client - server application indentification id (client detects a server with bluetooth service label with same ID)
     */
    private Context mContext;
    private Activity mActivity;
    private BroadcastReceiver mReceiver;
    public ArrayMap<String, BluetoothDevice> foundDevices;
    public BluetoothAdapter mBluetoothAdapter;
    private static final int mScanningTimeout = 20; //As in 20 seconds
    private final UUID myUUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

    //possible actions for bluetooth
    public enum DeviceAction {
        Bond,
        unBond,
        connect,
        message,
        disconnect
    }



    /**
     * @desc                    Class constructor
     * @param parent            getApplicationContext()
     * @param parent_activity   Activity that created this class
     * @param receiver          Receiver that listens for broadcasts
     */
    public BluetoothConnectionManager(Context parent, Activity parent_activity, BroadcastReceiver receiver) {
        mContext = parent;
        mReceiver = receiver;
        mActivity = parent_activity;


        //init private foundDevice param
        foundDevices = new ArrayMap<>();

        //check if bluetooth is supported with this device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * turn bluetooth on
     */
    public void setBluetoothEnabled(boolean setState){


        if(setState)
        {
            //if bluetooth is wanted to be activated
            if (mBluetoothAdapter.isEnabled()) {
                //if bluetooth is already enabled
                Log.i("Bluetooth", "Bluetooth is already on!");
            }
            else
            {
                //Not on --> ask to be turned on --> get result in onActivityResult
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mActivity.startActivityForResult(enableBluetooth, MainActivity.ENABLE_BLUETOOTH_CODE);
                //A request to turn on bluetooth is sent --> MainActivity onActivityResult
            }
        }
        else
        {
            //if bluetooth is wanted to be shut down
            if (mBluetoothAdapter.isEnabled()) {
                //Bluetooth is on --> shutdown
                mBluetoothAdapter.disable();
                mContext.unregisterReceiver(mReceiver);
            }
            else
            {
                //if bluetooth is already enabled
                Log.i("Bluetooth", "Bluetooth is already off!");
            }
        }
    }

    /**
     * Make this phone discoverable for by other devices
     */
    private void activateDiscoverability(){

        //Discover me
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);
        mContext.startActivity(discoverableIntent);
    }

    /**
     * set app to register thrown bluetooth broadcasts. After completion move to finding devices
     */
    public void registerBluetoothService(){
        //we set the app to listen for bluetooth broadcast's "ACTION_FOUND" = bluetooth device was detected and "ACTION_BOND_STATE_CHANGED" = "Pairing with device has changed"
        mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        mContext.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
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

    /**
     * Check if current devices has given permission for the app use bluetooth
     * @return  (true or false)
     */
    private boolean FindDevices_permissions(){
        //check if app has permission to use Bluetooth (X >= Android 6.0)
        int selfPermission = PermissionChecker.checkSelfPermission(mContext.getApplicationContext(), Manifest.permission.BLUETOOTH);
        int adminPermission = PermissionChecker.checkSelfPermission(mContext.getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN);

        int fineLocation = PermissionChecker.checkSelfPermission(mContext.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = PermissionChecker.checkSelfPermission(mContext.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        int granted = PermissionChecker.PERMISSION_GRANTED;

        /**
         * If some of the necessary permissions are not granted, App will ask the user for them...
         */
        if(selfPermission != granted || adminPermission != granted || fineLocation != granted || coarseLocation != granted)
        {
            /**
             * Show alert dialog informing what permissions are required for this app to work
             */

            AlertDialog.Builder alert = new AlertDialog.Builder(mActivity.getWindow().getContext());
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
                    }, MainActivity.ALLOW_BLUETOOTH_CODE);
                }
            });

            alert.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //close application
                    System.exit(0);
                }
            });

            alert.show();

            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Check permissions for detecting bluetooth devices and start scanning
     */
    public void FindDevices() {


        final android.os.Handler handler = new android.os.Handler();

        //empty the list of found devices
        foundDevices.clear();

        //check necessary permissions
        boolean hasPermissions = FindDevices_permissions();

        //have the permissions been given
        if(hasPermissions)
        {
            //Start looking for devices
            if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON)
            {
                //handle reading bonded devices
                if(getPairedDevices().size() > 0)
                {
                    for(BluetoothDevice foundDevice : getPairedDevices())
                    {
                        foundDevices.put(foundDevice.getAddress(), foundDevice);
                    }
                }

                //start looking for devices
                mBluetoothAdapter.startDiscovery();

                //close search after {mScanningTimeout} time (in seconds) has passed
                long time = mScanningTimeout * 1000;
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

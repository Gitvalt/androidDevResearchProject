package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.Manifest;
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
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
     * @member  UUID                myUID               This client - server application indentification id (client detects a server with bluetooth service label with same ID)
     */
    private Context mContext;
    private Activity mActivity;
    private BroadcastReceiver mReceiver;
    public ArrayMap<String, BluetoothDevice> foundDevices;
    public BluetoothAdapter mBluetoothAdapter;
    private static final int mScanningTimeout = 20; //As in 20 seconds
    private final UUID myUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

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
    private boolean FindNewDevices_permissions(){
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

                //close search after {mScanningTimeout} time (in seconds)
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

    /**
     * 1. create "BluetoothClien"-thread socket to the SelectedDevice
     * 2. send message to device
     *
     * @param message
     * @param mDevice
     */
    public void sendMessage(String message, BluetoothDevice mDevice){
        //create the socket for communication
        BluetoothClient thread = new BluetoothClient(mDevice);

        //start connecting
        thread.start();

        thread.run();

        //for testing
        BluetoothSocket socket = thread.getSocket();


        if(!thread.mSocket.isConnected())
        {
            Log.e("Bluetooth", "Socket connection that should be open, is not open!");
            sendMessage(message, mDevice);
            return;
        }
        else
        {
            BluetoothClient_Write thread1 = new BluetoothClient_Write(thread.mSocket);
            thread1.start();

            byte[] values = message.getBytes();
            thread1.write(values);
            thread1.cancel();
        }

        //closing threads
        thread.cancel();

    }

    public void getMessages(BluetoothDevice device){
        BluetoothClient_Listen listenThread = new BluetoothClient_Listen();
        listenThread.run();
    }


    //When connection to the device has been established:

    /**
     * @class   BluetoothClient     Connect as a client to bluetooth device
     */
    public class BluetoothClient extends Thread {

        /**
         * @member  mSocket     Socket that is used to communicate with the device
         * @member  mDevice     Device that is to be communicated with
         */
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        //Constructor
        public BluetoothClient(BluetoothDevice device)
        {
            BluetoothSocket tmp = null;
            try
            {
                tmp = device.createRfcommSocketToServiceRecord(myUID);
            }
            catch (Exception e){
                Log.e("BLUETOOTH_CONNECTION", "Creating socket has failed: " + e.getMessage());
            }

            //set the thread members
            mSocket = tmp;
            mDevice = device;
        }

        /**
         * get the bluetooth socket
         * @return  BluetoothSocket Socket that is used for communication
         */
        public BluetoothSocket getSocket(){
            return mSocket;
        }

        /**
         * Create a connection to the device
         */
        public void run()
        {
            // Cancel discovery because it otherwise slows down the connection.
            //mBluetoothAdapter.cancelDiscovery();

            try {
                //try to connect to socket until connection is created
                while(!mSocket.isConnected()){
                    mSocket.connect();
                }

            }
            catch (IOException connectException)
            {
                Log.e("BluetoothClient", "Error in connecting to Socket: " + connectException.getMessage());
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            String item = "success!";
            return;
        }

        /**
         * Destroy the connection
         */
        public void cancel()
        {
            try
            {
                mSocket.close();
            }
            catch (IOException e)
            {
                Log.e("BluetoothClient", "Could not close the client socket", e);
            }
        }
    }

    /**
     * @class   BluetoothClient_Listen     receive Bluetooth communication from other devices (act as a server)
     */
    public class BluetoothClient_Listen extends Thread {

        private final BluetoothServerSocket mServerSocket;
        private static final String NAME = "Android phone client";
        private final UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");

        public BluetoothClient_Listen() {

            BluetoothServerSocket tmp = null;
            try
            {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            }
            catch (IOException ex) {
                Log.e("Bluetooth", ex.getMessage());
            }
            mServerSocket = tmp;
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            while(true)
            {
                try {


                    socket = mServerSocket.accept();
                    if(socket != null)
                    {
                        Log.i("Bluetooth", "Communication received!");
                        mServerSocket.close();
                        break;
                    }
                }
                catch (IOException e){
                    break;
                }
            }

        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mServerSocket.close();
            } catch (IOException e) {
            }
        }

    }

    /**
     * @class   BluetoothClient_Write     Manage connection between two devices
     */
    public class BluetoothClient_Write extends Thread{

        /**
         * @member  mSocket         Contains target bluetoothsocket
         * @member  mInputStream    The data received from other device
         * @member  mOutputStream   The data sent to other device
         * @member  mBuffer         Temporary variable contains the data
         * @member  mHandler        Handle data reading
         */
        private final UUID myUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;
        private byte[] mBuffer;
        private android.os.Handler mHandler;

        /**
         * @function     BluetoothClient  Constructor
         * @param       "socket"        Socket assigned to communicate with specified device
         */
        public BluetoothClient_Write(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                if (!mSocket.isConnected()) {
                    mSocket.connect();
                }
            }
            catch (Exception e){
                Log.e("error", e.getMessage());
            }

            try {
                tmpIn = socket.getInputStream();
            }
            catch (IOException error) {
                Log.e("Bluetooth_connector", "BluetoothClient_Write InputStream failed", error);
            }

            try {
                tmpOut = socket.getOutputStream();
            }
            catch (IOException error) {
                Log.e("Bluetooth_connector", "BluetoothClient_Write OutputStream failed", error);
            }

            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }

        /**
         * Get data from remote device
         */
        public void run(){
            mBuffer = new byte[1024];
            int numBytes;

            while(true){
                try {
                    numBytes = mInputStream.read(mBuffer);

                    /**
                     * @param {int}     arg1    What is received
                     * @param {int}     arg2
                     * @param {int}     arg3
                     * @param {byte[]}  arg4    The message
                     */
                    Message mMessage = mHandler.obtainMessage(0, numBytes, -1, mBuffer);

                    mMessage.sendToTarget();

                } catch (IOException e){
                    Log.e("Bluetooth_error", "Error with reading data", e);
                    break;
                }
            }
        }

        /**
         * @function write  Send data "bytes" to remote device
         * @param bytes The data to be send
         */
        public void write(byte[] bytes){
            try {
                mOutputStream.write(bytes);

                Message writtenMsg = mHandler.obtainMessage(1, -1, -1, mBuffer);
                writtenMsg.sendToTarget();

            }catch (IOException e){
                Log.e("Bluetooth_message", "Error in writing to remote device", e);

                //send failure msg to activity
                Message writtenMsg = mHandler.obtainMessage(2);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Could not send data!");
                writtenMsg.setData(bundle);
                mHandler.sendMessage(writtenMsg);
            }
        }

        /**
         * @function cancel Close the thread
         */
        public void cancel(){
            try {
                mSocket.close();
            } catch (IOException e){
                Log.e("Bluetooth_connector", "Closing mSocket failed", e);
            }
        }
    }

}

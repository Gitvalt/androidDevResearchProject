package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Debug;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends Activity implements DeviceAdapter.DeviceListener {


    /**
     * @member  ProgressBar                 Shown when devices are searched
     * @member  RecyclerView                List of found devices
     * @member  RecyclerView.Adapter        Manages devices shown in the list
     * @member  RecyclerView.LayoutManager  Manager the overlay of the list
     *
     * @member  ENABLE_BLUETOOTH_CODE       Tag for permissions
     * @member  ALLOW_BLUETOOTH_CODE        Tag for permissions
     *
     * @member  foundDevices                Array of found BluetoothDevices
     *
     * @member  mBluetoothAdapter           Adapter that handles phones bluetooth connection
     */

    private ProgressBar spinningCircle;

    private RecyclerView deviceList;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private bluetoothController BluetoothController;

    private static final int ENABLE_BLUETOOTH_CODE = 123;
    private static final int ALLOW_BLUETOOTH_CODE = 456;


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

        BluetoothController = new bluetoothController(getApplicationContext(), this, mReceiver);

        deviceList.setLayoutManager(layoutManager);

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
            BluetoothPermission();
        }



    }

    /**
     * Check if bluetooth has been activated in the device
     * if permission is allowed
     */
    private void BluetoothPermission(){

        //is bluetooth on?
        if (!BluetoothController.mBluetoothAdapter.isEnabled()) {
            //Not on --> ask to be turned on --> get result in onActivityResult
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH_CODE);
        }
        else
        {
            //bluetooth is on, move to registering service
            BluetoothController.registerBluetoothService();
            lookForDevices();
        }
    }

    private void lookForDevices(){

        ArrayMap<String, BluetoothDevice> foundDevices = BluetoothController.foundDevices;

        //Empty list
        if(foundDevices.isEmpty() != true){
            foundDevices.clear();
            deviceList.getAdapter().notifyDataSetChanged();
        }

        BluetoothController.FindNewDevices();
        //program continues in BroadcastReceiver.ActionDiscovery finished
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
                    spinningCircle.setVisibility(View.VISIBLE);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    spinningCircle.setVisibility(View.INVISIBLE);
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    //bluetooth device is found.

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

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
                            BluetoothPermission();
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


    //Handling recycler view:
    /**
     * Get callback from DeviceAdapter and execute pairing
     */
    @Override
    public void selectDevice(BluetoothDevice selectedDevice, bluetoothController.DeviceAction action) {
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
        }

    }

    /**
     * Update shown recyclerView when new bluetooth devices has been found
     */
    private void updateDeviceList(){


        deviceList.setAdapter(mAdapter);
        ArrayMap<String, BluetoothDevice> foundDevices = BluetoothController.foundDevices;


        if(foundDevices == null)
        {
            Log.e("Bluetooth", "updating devicelist, but the founddevices array is null");
            return;
        }
        else if(foundDevices.size() <= 0)
        {
            Log.e("Bluetooth", "No devices available");
            return;
        }
        else
        {
            if(deviceList.getAdapter() == null) {
                DeviceAdapter deviceListAdapter = new DeviceAdapter(foundDevices, this);
                deviceList.setAdapter(deviceListAdapter);
            }
            else
            {
                deviceList.getAdapter().notifyDataSetChanged();
            }
        }
    }

    //----



    //When connection to the device has been established:

    /**
     * @class   AcceptThread     receive Bluetooth connections from other devices.
     */
    public class AcceptThread extends Thread {

        private final BluetoothServerSocket mServerSocket;
        private static final String NAME = "MAINSERVER";
        private final UUID MY_UUID = UUID.randomUUID();

        public AcceptThread() {

            BluetoothServerSocket tmp = null;
            try
            {
                tmp = BluetoothController.mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            }
            catch (IOException ex) {

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
     * @class   ConnectThread     Connect to other bluetooth devices
     */
    public class ConnectThread extends Thread {

        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;
        private final UUID myUID = UUID.randomUUID();

        public ConnectThread(BluetoothDevice device)
        {
            BluetoothSocket tmp = null;
            mDevice = device;

            try {
                tmp = device.createRfcommSocketToServiceRecord(myUID);
            }
            catch (IOException e){
                Log.e("BLUETOOTH_CONNECTION", "error in connecting to bluetooth device: " + e.getMessage());
            }

            mSocket = tmp;
        }

        public void run()
        {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothController.mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    Log.e("ERROR", "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            String item = "success!";

        }

        public void cancel()
        {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e("ERROR", "Could not close the client socket", e);
            }
        }
    }

    /**
     * @class   ConnectedThread     Manage connection between two devices
     */
    private class ConnectedThread extends Thread{

        /**
         * @member  mSocket         Contains target bluetoothsocket
         * @member  mInputStream    The data received from other device
         * @member  mOutputStream   The data sent to other device
         * @member  mBuffer         Temporary variable contains the data
         * @member  mHandler        Handle data reading
         */
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;
        private byte[] mBuffer;
        private android.os.Handler mHandler;

        /**
         * @function     ConnectThread  Constructor
         * @param       "socket"        Socket assigned to communicate with specified device
         */
        public ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException error) {
                Log.e("Bluetooth_connector", "ConnectedThread InputStream failed", error);
            }

            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException error) {
                Log.e("Bluetooth_connector", "ConnectedThread OutputStream failed", error);
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


    /**
     * When app is closed, remove registers
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
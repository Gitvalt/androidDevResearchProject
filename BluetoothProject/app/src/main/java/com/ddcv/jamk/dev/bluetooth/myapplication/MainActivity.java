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
import java.util.UUID;
import java.util.logging.Handler;

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

    private static final int ENABLE_BLUETOOTH_CODE = 123;
    private static final int ALLOW_BLUETOOTH_CODE = 456;

    private ArrayMap<String, BluetoothDevice> foundDevices;
    //private ArrayList<BluetoothDevice> foundDevices;
    private BluetoothAdapter mBluetoothAdapter;

    public enum DeviceAction {
        Bond,
        unBond
    }


    /**
     * App is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init private foundDevice param
        foundDevices = new ArrayMap<>();

        //check if bluetooth is supported with this device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //setup progressbar for scanning devices
        spinningCircle = (ProgressBar)findViewById(R.id.scanningSpinner);

        //list of shown devices
        deviceList = (RecyclerView)findViewById(R.id.listView);
        layoutManager = new LinearLayoutManager(this);

        deviceList.setLayoutManager(layoutManager);

        //check if phone supports bluetooth communication
        if (mBluetoothAdapter == null)
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
     */
    private void BluetoothPermission(){
        //is bluetooth on?
        if (!mBluetoothAdapter.isEnabled()) {
            //Not on --> ask to be turned on
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH_CODE);
        }
        else
        {
            //bluetooth is on, move to
            bluetoothIsEnabled();
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
            mBluetoothAdapter.cancelDiscovery();

            switch (action){
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
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
                case BluetoothDevice.ACTION_FOUND:
                    //bluetooth device is found.

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    foundDevices.put(device.getAddress(), device);

                    //hide progressbar
                    spinningCircle.setVisibility(View.INVISIBLE);

                    updateDeviceList();

                    //ConnectThread conThread = new ConnectThread(device);
                    Log.i("Bluetooth_broadcast", "Bluetooth device detected!");
                    break;
            }
        }
    };

    /**
     * Make this phone discoverable for by other devices
     */
    private void activateDiscoverability(){

        //Discover me

        AcceptThread thread = new AcceptThread();
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);
        startActivity(discoverableIntent);

        thread.run();

    }

    /**
     * set app to register thrown bluetooth broadcasts and then move to finding devices
     */
    private void bluetoothIsEnabled(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));

        FindNewDevices();
    }

    /**
     * Check if selected device has been paired with
     * @param device, device under inception
     */
    private boolean IsDevicePaired(BluetoothDevice device)
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
    private void getPairedDevices(){
        /**
            Get paired devices and save them to a array
         */
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0) {
            for(BluetoothDevice bluetoothDevice : pairedDevices){
                if(foundDevices.containsValue(bluetoothDevice)){
                    //do nothing
                }
                else {
                    //add paired device to found devices
                    foundDevices.put(bluetoothDevice.getAddress(), bluetoothDevice);
                }
            }
        }
    }

    /**
     * Check permissions for detecting bluetooth devices and start scanning
     */
    private void FindNewDevices() {

        //check if app has permission to use Bluetooth (X >= Android 6.0)
        int selfPermission = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH);
        int adminPermission = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN);

        int fineLocation = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        int granted = PermissionChecker.PERMISSION_GRANTED;

        /**
         * If some of the necessary permissions are not granted, App will ask the user for them...
         */
        if(selfPermission != granted || adminPermission != granted || fineLocation != granted || coarseLocation != granted)
        {
            /**
             * Show alert dialog informing what permissions are required for this app to work
             */
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Permissions have not been granted");
            alert.setMessage("Permissions for coarse/fine location and Bluetooth required");
            alert.setCancelable(false);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FindNewDevices(); //rerun current method
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

            /**
             * Send the required permissions permissionRequest
             */
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, ALLOW_BLUETOOTH_CODE);
            return;
        }

        /**
         * start searching devices
         */
        if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON)
        {
            spinningCircle.setVisibility(View.VISIBLE);
            getPairedDevices();
            mBluetoothAdapter.startDiscovery();
        }
        else
        {
            Log.e("Bluetooth", "Bluetooth service is not on! Cant start scanning for new devices!");
        }

    }

    /**
     * Get callback from DeviceAdapter and execute pairing
     */
    @Override
    public void selectDevice(BluetoothDevice selectedDevice, DeviceAction action) {
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
                FindNewDevices();
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
                    bluetoothIsEnabled();
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

                    builder.create();
                    AlertDialog dialog = builder.show();

                }
                break;
            case ALLOW_BLUETOOTH_CODE:
                if (resultCode == RESULT_OK) {
                    FindNewDevices();
                } else {
                    //bluetooth off
                    Toast.makeText(getApplicationContext(), "Bluetooth access permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    //When connection to the device has been enstablished:
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
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
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
            mBluetoothAdapter.cancelDiscovery();

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

}
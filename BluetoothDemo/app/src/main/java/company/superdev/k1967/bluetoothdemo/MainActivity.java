package company.superdev.k1967.bluetoothdemo;

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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends Activity {


    private static final int ENABLE_BLUETOOTH_CODE = 123;
    private static final int ALLOW_BLUETOOTH_CODE = 456;
    private static final int ALLOW_ADMIN_BLUETOOTH_CODE = 789;

    private BluetoothDevice foundDevice;
    private BluetoothAdapter mBluetoothAdapter;

    //when bluetooth service has found new device
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device is found.

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address


                ConnectThread conThread = new ConnectThread(device);

                /*
                Discover me

                AcceptThread thread = new AcceptThread();
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);
                startActivity(discoverableIntent);

                thread.run();
                */

                Log.i("what", "complete");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init private foundDevice param
        foundDevice = null;

        //check if bluetooth is supported with this device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



        if (mBluetoothAdapter == null) {
            //not supported

            AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
            alert.setTitle("Bluetooth not supported");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.e("Bluetooth", "Bluetooth is not supported by this device");
                }
            });
            alert.create();
            alert.show();
        }

        else
        {
            //bluetooth is supported
            Toast.makeText(getApplicationContext(), "Bluetooth is implemented!", Toast.LENGTH_SHORT).show();

            //is bluetooth enabled?
            if (!mBluetoothAdapter.isEnabled()) {
                //if not enabled
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH_CODE);
            }
            else {
                bluetoothIsEnabled();
            }


        }



    }

    //set to register found Bluetooth requests
    private void bluetoothIsEnabled(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        FindNewDevices();
    }

    //Check if device is already known
    private void IsDevicePaired() {
    }



    //find previously unkown devices
    private void FindNewDevices() {

        //check if app has permission to use Bluetooth (X >= Android 6.0)
        int selfPermission = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH);
        int adminPermission = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN);

        int fineLocation = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = PermissionChecker.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        int granted = PermissionChecker.PERMISSION_GRANTED;

        if(selfPermission != granted || adminPermission != granted || fineLocation != granted || coarseLocation != granted)
        {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, ALLOW_BLUETOOTH_CODE);
            return;
        }

        //start searching devices
        if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){
            /*
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);
            startActivity(discoverableIntent);
            */
            mBluetoothAdapter.startDiscovery();
        } else {
            Log.e("Bluetooth", "Bluetooth service is not on! Cant start scanning for new devices!");
        }

    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case ENABLE_BLUETOOTH_CODE:
                //Bluetooth request accepted?
                if (resultCode == RESULT_OK) {
                    bluetoothIsEnabled();
                } else {
                    //bluetooth off
                    Toast.makeText(getApplicationContext(), "Turning Bluetooth on not allowed", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    //receive Bluetooth connections from other devices
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

    //connect to other found bluetooth devices
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
}
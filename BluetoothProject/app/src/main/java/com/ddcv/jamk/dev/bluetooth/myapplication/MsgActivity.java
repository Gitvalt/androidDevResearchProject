package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MsgActivity extends AppCompatActivity {


    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int ENABLE_BLUETOOTH_CODE = 123;
    private static final int ALLOW_BLUETOOTH_CODE = 456;
    public static final int PICK_IMAGE = 666;
    private static final String TAG = "BLUETOOTH";
    private BluetoothDevice mmDevice = null;
    private ConnectThread connectThread = null;
    private byte[] byteArray;
    private final UUID myUUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg);


        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_CODE);
        }

        Log.v("bluetooth", "testi");


        Intent intent = getIntent();
        String deviceMAC = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        mmDevice = mBluetoothAdapter.getRemoteDevice(deviceMAC);

        Toast toast = Toast.makeText(getApplicationContext(), deviceMAC, Toast.LENGTH_SHORT);
        toast.show();

    }

    public void createBluetooth(View v) {
        if (connectThread == null) {
            connectThread = new ConnectThread(mmDevice);
            connectThread.start();
        } else if (!connectThread.isConnected) {
            connectThread = new ConnectThread(mmDevice);
            connectThread.start();
        }
    }

    public void cancelBluetooth(View v) {
        if ( connectThread != null)
        {
            connectThread.cancel();
            connectThread = null;
            TextView resp_textview = (TextView)findViewById(R.id.response);
            resp_textview.setText("Disconnected");
        }
    }

    public void sendBluetooth(View v) {
        try {
            connectThread.sendMessage();
        } catch (Exception e) {Log.v("bluetooth", "Could not send message");}
    }

    public void serverBluetooth(View v)
    {
        try
        {
            ActAsServerThread thread = new ActAsServerThread(mmDevice);
            thread.run();
        }
        catch (Exception e)
        {
            Log.e("Error", "Unkown error", e);
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        private OutputStream mmOutStream;
        private InputStream mmInStream;
        public boolean isConnected;
        private byte[] buffer = new byte[2048];

        /**
         * Thread constructor
         * @desc
         * For 10 times try to create socket and connect to the device, retrieve input- and output-streams.
         * if fails then wait for 0.5 seconds and try again.
         * @param device The device that is to be connected to.s
         */
        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;
            mmOutStream = null;
            isConnected = false;
            for (int i = 0; i < 10; i++)
            {
                try
                {
                    // Get a BluetoothSocket to connect with the given BluetoothDevice.
                    // MY_UUID is the app's UUID string, also used in the server code.
                    tmp = device.createRfcommSocketToServiceRecord(myUUID);
                    tmp.connect();
                    Log.v("bluetooth", "SOCKET " + String.valueOf(tmp.isConnected()));
                    mmSocket = tmp;
                    mmOutStream = mmSocket.getOutputStream();
                    mmInStream = mmSocket.getInputStream();
                    getInputstream();
                    isConnected = true;
                    break;
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Socket's create() method failed", e);
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (Exception e2)
                    {
                        Log.e("Thread sleep", "Setting thread to sleep has failed");
                    }

                }
            }
            if (!isConnected) {Log.v(TAG, "Couldn't create BT Socket (there is no server)");}
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
        }

        /**
         * Send message through the mmSocket to the device and listen for response
         */
        public void sendMessage() {
            if (isConnected) {
                try {
                    EditText message = (EditText) findViewById(R.id.editText2);
                    byte[] send = message.getText().toString().getBytes();

                    //send the information
                    mmOutStream.write(send);

                    //start listening for response
                    getInputstream();

                } catch (IOException e) {
                    Log.e(TAG, "Couldn't send msg", e);
                }
            }
        }

        /**
         * listen for communication from the device
         */
        public void getInputstream() {
            try {
                //read data
                int response = mmInStream.read(buffer);

                //parse byte data
                String responseStr = new String(buffer, 0, response);

                //display parsed response
                TextView resp_textview = (TextView)findViewById(R.id.response);
                resp_textview.setText(responseStr);
            }
            catch (IOException e)
            {
                Log.e(TAG, "Couldn't recieve msg", e);
            }
        }


        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class ActAsServerThread extends Thread {
        private BluetoothServerSocket mmServerSocket;
        private BluetoothDevice mmDevice;
        private OutputStream mmOutStream;
        private InputStream mmInStream;

        public boolean isConnected;
        private byte[] buffer = new byte[2048];

        /**
         * Thread constructor
         * @desc
         * For 10 times try to create socket and connect to the device, retrieve input- and output-streams.
         * if fails then wait for 0.5 seconds and try again.
         * @param device The device that is to be connected to.s
         */
        public ActAsServerThread(BluetoothDevice device) {

            BluetoothServerSocket tmp = null;
            mmDevice = device;
            mmOutStream = null;
            isConnected = false;

            for (int i = 0; i < 10; i++)
            {
                try
                {
                    // Get a BluetoothSocket to connect with the given BluetoothDevice.
                    // MY_UUID is the app's UUID string, also used in the server code.
                    tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ServerApp", myUUID);
                    mmServerSocket = tmp;
                    break;
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Socket's create() method failed", e);
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (Exception e2)
                    {
                        Log.e("Thread sleep", "Setting thread to sleep has failed");
                    }

                }
            }

            if (!isConnected) {Log.v(TAG, "Couldn't create BT Socket (there is no server)");}
        }

        public void run() {
            try {
                mBluetoothAdapter.cancelDiscovery();
                BluetoothSocket mSocket;

                while (true) {
                    mSocket = mmServerSocket.accept();
                    mmServerSocket.close();
                    listenFor(mSocket);
                }
            }
            catch (IOException ioExoection)
            {
                Log.e("IOEXECPTION", "Accepting new connection has failed", ioExoection);
            }
        }

        public void listenFor(BluetoothSocket socket)
        {
            try
            {
                mmInStream = socket.getInputStream();
                mmOutStream = socket.getOutputStream();

                while(true)
                {
                    //read data
                    int response = mmInStream.read(buffer);

                    //parse byte data
                    String responseStr = new String(buffer, 0, response);

                    if(buffer.length != 0)
                    {

                    }
                }

            }
            catch (IOException ioe)
            {

            }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

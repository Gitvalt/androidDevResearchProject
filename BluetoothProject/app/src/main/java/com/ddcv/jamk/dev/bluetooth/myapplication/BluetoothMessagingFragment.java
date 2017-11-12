package com.ddcv.jamk.dev.bluetooth.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by Valtteri on 11.11.2017.
 */

public class BluetoothMessagingFragment extends Fragment {

    private RecyclerView messageList;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Button submitButton;
    private TextView userInput;
    private String currentDevice;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    BluetoothConnectionManager.BluetoothClient bluetoothClient;
    BluetoothConnectionManager.BluetoothClient_Listen bluetoothClient_listen;
    BluetoothConnectionManager.BluetoothClient_Write bluetoothClient_write;

    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bluetooth_messaging_fragment, container, false);

        messageList = (RecyclerView)view.findViewById(R.id.messageDisplay);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        messageList.setLayoutManager(layoutManager);

        MessageAdapter messageAdapter = new MessageAdapter();
        messageList.setAdapter(messageAdapter);

        submitButton = (Button)view.findViewById(R.id.submitButton);
        userInput = (TextView)view.findViewById(R.id.messageInput);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testMessage("this");
                testMessage("SYSTEM");
                testMessage("SomeoneElse");
            }
        });

        currentDevice = getArguments().getString("DeviceMAC");
        if(currentDevice == null)
        {
            Log.e("Bluetooth Messaging", "Target device is not defined");
        }
        else
        {
            start_listenForMessages();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void addNewMessage(MessageItem message)
    {
        MessageAdapter adapter = (MessageAdapter)messageList.getAdapter();
        adapter.messageArray.put(message.getDate(), message);
        messageList.setLayoutManager(layoutManager);
        adapter.notifyDataSetChanged();
    }

    private void start_listenForMessages()
    {
        if(getActivity().getClass() == MainActivity.class)
        {
            final MainActivity parent = (MainActivity)getActivity();
            final String targetMAC = currentDevice;

            handler.post(new Runnable() {
                @Override
                public void run() {

                    final BluetoothDevice bluetoothDevice = parent.getBluetoothController().getDeviceWithMac(targetMAC);
                    final BluetoothConnectionManager connectionManager = parent.getBluetoothController();
                    boolean isConnectionPossible = parent.getBluetoothController().testConnection(bluetoothDevice);


                    if(isConnectionPossible)
                    {
                        Log.i("Bluetooth_Messaging", "Connection can be established");
                        //connectionManager.listenForMessages(bluetoothDevice, handler);

                        Thread getMessages = new Thread(){
                            @Override
                            public void run() {
                                connectionManager.listenForMessages(bluetoothDevice, handler);
                            }
                        };
                        getMessages.start();

                    }
                    else
                    {
                        Log.e("Bluetooth_Messaging", "Connection cannot be established");
                        Toast.makeText(parent.getApplicationContext(), "Connection cannot be established", Toast.LENGTH_LONG).show();
                    }

                }
            });

        }
    }



    public void testMessage(String byWhom)
    {
        MessageItem item = new MessageItem(byWhom, "test", new Date());
        addNewMessage(item);
    }



}

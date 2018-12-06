package com.techexchange.mobileapps.assignment3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final boolean BYPASS_WIFI_SCREEN = false;

    Button findPlayersButton;
    ListView playersList;
    ProgressBar progressBar;

    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;

    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    ServerThread serverThread;
    ClientThread clientThread;
    SendReceiveThread sendReceiveThread;

    Host host;
    BattlegroundView battlegroundView;

    // Actions
    static final byte GREEN_TANK_UP = 14;
    static final byte GREEN_TANK_DOWN = 1;
    static final byte GREEN_TANK_LEFT = 2;
    static final byte GREEN_TANK_RIGHT = 3;
    static final byte GREEN_TANK_SHOOT = 4;
    static final byte RED_TANK_UP = 5;
    static final byte RED_TANK_DOWN = 6;
    static final byte RED_TANK_LEFT = 7;
    static final byte RED_TANK_RIGHT = 8;
    static final byte RED_TANK_SHOOT = 9;
    static final byte GREEN_SCORED = 10;
    static final byte RED_SCORED = 11;
    static final byte GREEN_TURN = 12;
    static final byte RED_TURN = 13;

    enum Host {
        SERVER, CLIENT
    }

    public class ServerThread extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceiveThread = new SendReceiveThread(socket);
                sendReceiveThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public class ClientThread extends Thread {
        Socket socket;
        String hostAddress;

        ClientThread(InetAddress hostAddress) {
            this.hostAddress = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAddress, 8888), 500);
                sendReceiveThread = new SendReceiveThread(socket);
                sendReceiveThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class SendReceiveThread extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        SendReceiveThread(Socket socket) {
            this.socket = socket;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket != null) {
                // Listen for message
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        // We received something
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Allow network operations on main thread for simplicity.
        StrictMode.ThreadPolicy policy
                = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initializeComponents();
        executeListener();
    }


    Handler handler = new Handler(msg -> {
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuffer = (byte[]) msg.obj;
                for (int i = 0; i < readBuffer.length; i++) {
                    byte action = readBuffer[i];
                    battlegroundView.handleAction(action);
                }
        }
        return true;
    });

    private void executeListener() {
        findPlayersButton.setOnClickListener(v -> {
            if (BYPASS_WIFI_SCREEN) {
                setContentView(new BattlegroundView(this));
            } else {
                progressBar.setVisibility(View.VISIBLE);

                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Successfully started discovering
                        Toast.makeText(getApplicationContext(),
                                "Discovery started", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        // Failed to start discovering
                        Toast.makeText(getApplicationContext(),
                                "Discovery failed to Start", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        playersList.setOnItemClickListener((parent, view, position, id) -> {
            final WifiP2pDevice device = deviceArray[position];
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;

            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(),
                            "Connection failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register the broadcast receiver
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the broadcast receiver
        unregisterReceiver(receiver);
    }

    WifiP2pManager.PeerListListener peerListListener = peerList -> {
        if (!peerList.getDeviceList().equals(peers)) {
            playersList.setBackgroundColor(Color.WHITE);

            peers.clear();
            peers.addAll(peerList.getDeviceList());

            deviceNameArray = new String[peers.size()];
            deviceArray = new WifiP2pDevice[peers.size()];

            int index = 0;
            for (WifiP2pDevice device : peers) {
                deviceNameArray[index] = device.deviceName;
                deviceArray[index] = device;
                index++;
            }

            ArrayAdapter<String> arrayAdapter
                    = new ArrayAdapter<>(getApplicationContext(),
                    android.R.layout.simple_list_item_1, deviceNameArray);

            progressBar.setVisibility(View.INVISIBLE);
            playersList.setAdapter(arrayAdapter);

            // Dynamically set list view height
            int totalHeight = 0;
            int totalItems = Math.min(5, arrayAdapter.getCount());
            for (int i = 0; i < totalItems; i++) {
                View listItem = arrayAdapter.getView(i, null, playersList);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = playersList.getLayoutParams();
            params.height = totalHeight + (playersList.getDividerHeight() * totalItems - 1);
            playersList.setLayoutParams(params);
            playersList.requestLayout();


            if (peers.size() == 0) {
                Toast.makeText(getApplicationContext(),
                        "No Device Found!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.groupFormed && info.isGroupOwner) {
                battlegroundView = new BattlegroundView(MainActivity.this);
                setContentView(battlegroundView);
                host = Host.SERVER;
                Toast.makeText(getApplicationContext(), "You are the GREEN tank!", Toast.LENGTH_SHORT).show();
                serverThread = new ServerThread();
                serverThread.start();
            } else if (info.groupFormed) {
                battlegroundView = new BattlegroundView(MainActivity.this);
                setContentView(battlegroundView);
                host = Host.CLIENT;
                Toast.makeText(getApplicationContext(), "You are the RED tank!", Toast.LENGTH_SHORT).show();
                clientThread = new ClientThread(groupOwnerAddress);
                clientThread.start();
            }
        }
    };

    private void initializeComponents() {
        findPlayersButton = findViewById(R.id.find_players_button);
        playersList = findViewById(R.id.peer_list);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        receiver = new WifiDirectBroadcastReceiver(wifiP2pManager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

}

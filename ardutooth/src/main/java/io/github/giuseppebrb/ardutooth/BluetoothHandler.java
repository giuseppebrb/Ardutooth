package io.github.giuseppebrb.ardutooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * This singleton class handles the bluetooth connection with Arduino.
 */
class BluetoothHandler {
    /**
     * Represents the state of the connection with Arduino: true if there's connection, false otherwise.
     */
    protected static boolean connected = false;
    private final int REQUEST_ENABLE_BT = 1;
    private static BluetoothHandler mInstanceHandler;
    private BluetoothDevice mBtDevice;
    private BluetoothSocket mSocket;
    private BluetoothHeadset mBluetoothHeadset;
    private BluetoothAdapter mAdapter;
    private AlertDialog.Builder builder;
    private Activity mActivity;

    private OutputStream mOutStream;
    private BufferedReader mReader;

    /**
     * Constructor
     *
     * @param activity Define the {@link android.content.Context} where the object will work.
     */
    private BluetoothHandler(Activity activity) {
        mActivity = activity;

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mActivity.registerReceiver(mReceiver, filter);

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter.getProfileProxy(mActivity, mProfileListener, BluetoothProfile.HEADSET);

        builder = new AlertDialog.Builder(mActivity);
    }

    /**
     * Allows to create or get the unique instance of the component.
     *
     * @param activity Define the {@link android.content.Context} where the object will work.
     * @return
     */
    protected static BluetoothHandler getInstance(Activity activity) {
        if (mInstanceHandler == null) {
            mInstanceHandler = new BluetoothHandler(activity);
        }
        return mInstanceHandler;
    }

    /**
     * Establish a connection with Arduino.
     */
    protected void createConnection() {
        checkBluetoothState();
    }

    /**
     * Open the input and output communication with Arduino.
     *
     * @return true if communication has been set up successfully, else false
     */
    @SuppressLint("NewApi")
    private boolean connect() {
        OutputStream tmpOut = null;
        InputStream tmpIn = null;
        try {
            mSocket.connect();
            try {
                tmpOut = mSocket.getOutputStream();
                tmpIn = mSocket.getInputStream();
            } catch (IOException e) {
                Log.e(Ardutooth.TAG, "Error occurred when creating output stream", e);
            }
            mOutStream = tmpOut;
            mReader = new BufferedReader(new InputStreamReader(tmpIn, StandardCharsets.US_ASCII));
        } catch (IOException e) {
            Log.e(Ardutooth.TAG, "Error opening connection", e);
            closeConnection();
        }
        return mSocket.isConnected();
    }

    /**
     * Close the connection established with Arduino .
     */
    protected void closeConnection() {
        if (connected) {
            try {
                mSocket.close();
                mOutStream.close();
                mReader.close();
            } catch (IOException e) {
                Log.e(Ardutooth.TAG, "Error while closing socket", e);
                Toast.makeText(mActivity.getApplication(), mActivity.getString(R.string.error_occurred_disconnecting), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mActivity.getApplication(), mActivity.getString(R.string.cannot_disconnect), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Retrieve a bluetooth connection if there's one already established.
     */
    protected void retrieveConnection() {

        Log.d(Ardutooth.TAG, "Attempting to retrieve existing connection");
        if (mAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
            List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
            mBtDevice = devices.get(0);
            connected = true;
            Log.d(Ardutooth.TAG, "Already connected with a device");
        } else
            Log.d(Ardutooth.TAG, "Not connected with a device");

    }

    /**
     * Check if Bluetooth is supported on the current device.
     *
     * @return true if Bluetooth is supported, false otherwise.
     */
    private boolean isBluetoothSupported() {
        return mAdapter != null;
    }

    /**
     * Check if bluetooth is on, if no ask the user to turn it on.
     * If bluetooth is on but there's no connection yet, create a list of paired devices and ask
     * the user to pick one to connect with. If there are no paired devices, open bluetooth settings
     * and pair and connect with the Arduino bluetooth module.
     */
    private void checkBluetoothState() {
        if (isBluetoothSupported()) {
            if (!mAdapter.isEnabled()) {
                Log.d(Ardutooth.TAG, "Asking permission to turn on bluetooth");

                builder.setTitle(mActivity.getString(R.string.bluetooth_off));
                builder.setMessage(mActivity.getString(R.string.turn_on_bluetooth_message));
                builder.setCancelable(true);
                builder.setPositiveButton(mActivity.getString(R.string.open_settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent settings_intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        mActivity.startActivityForResult(settings_intent, REQUEST_ENABLE_BT);
                    }
                });
                builder.setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            } else {
                Log.d(Ardutooth.TAG, "Bluetooth is already on");
                retrieveConnection();
                if (!connected) {

                    if (mAdapter.getBondedDevices().size() != 0) {

                         promptUserToConnectWithAPairedDevice();

                    } else{

                        Log.d(Ardutooth.TAG, "There are no bonded devices");

                        promptUserToPairWithADevice();
                    }

                } else {
                    Toast.makeText(mActivity.getApplication(), mActivity.getString(R.string.already_connected) + mBtDevice.getName(), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            builder.setTitle(mActivity.getString(R.string.bluetooth_not_supported));
            builder.setMessage(mActivity.getString(R.string.bluetooth_not_supported_message));
            builder.setPositiveButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }

    /**
     * Creates a list of Bonded (paired) Bluetooth devices and displays their names in a dialog.
     * The user can select a previously paired device to connect with, or pair and connect with
     * a new one.
     */
    private void promptUserToConnectWithAPairedDevice() {

        Set<BluetoothDevice> pairedDevicesSet = mAdapter.getBondedDevices();

        //the names are what is displayed in the dialog list
        String[] pairedDevicesNames = new String[pairedDevicesSet.size()];

        //This is the list of paired BluetoothDevice object references.
        // It is final so that it can be accessed in the anonymous inner-class EventHandler.
        final BluetoothDevice[] pairedDevicesArray = new BluetoothDevice[pairedDevicesSet.size()];

        //Create a key-value association between the list of names and the list of devices.
        int i = 0;
        for (BluetoothDevice device : pairedDevicesSet) {
            pairedDevicesNames[i] = device.getName();
            Log.d(Ardutooth.TAG, "Found bonded Device: " + pairedDevicesNames[i]);
            pairedDevicesArray[i] = device;
            i++;
        }

        //This is a weird "hack" I resorted to. It allows the string to change,
        // while still being able to reference it in the PositiveButton event handler.
        final String[] selectedDeviceAddress = {""};

        builder.setTitle(mActivity.getString(R.string.bluetooth_not_connected));
        builder.setCancelable(true);
        builder.setSingleChoiceItems(pairedDevicesNames, -1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedDeviceAddress[0] = pairedDevicesArray[which].getAddress();
                        Log.d(Ardutooth.TAG, "user selected " + pairedDevicesArray[which].getName());
                    }
                });
        builder.setPositiveButton(mActivity.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBtDevice = mAdapter.getRemoteDevice(selectedDeviceAddress[0]);
                Log.d(Ardutooth.TAG, "mBtDevice = " + mBtDevice.getName() + ", " + mBtDevice.getAddress());
                openRFCOMMSocketWithBondedDevice();
            }
        });
        builder.setNeutralButton(mActivity.getString(R.string.pair_with_new_device), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent settings_intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                mActivity.startActivityForResult(settings_intent, REQUEST_ENABLE_BT);
            }
        });
        builder.setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * If the device has already been bonded, no action will be issued to the BroadcastReceiver
     * until a connection is made (at which point the BluetoothDevice.ACTION_ACL_CONNECTED will be
     * issued). Because of this the BroadcastReceiver will not initiate creating a socket, so we
     * must do that manually here.
     */
    private void openRFCOMMSocketWithBondedDevice() {
        try {
            int counter = 0;
            do {
                mSocket = mBtDevice.createRfcommSocketToServiceRecord(Ardutooth.UUID);
                counter++;
            }
            while (!connect() && counter != 2);
            Log.d(Ardutooth.TAG, "Connected to " + mBtDevice.getName() + ": " + mSocket.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * If Bluetooth is already on, and not already connected, and there are no Devices already
     * bonded (paired), prompt the user to pair with a device in Android's Bluetooth settings.
     */
    private void promptUserToPairWithADevice() {
        Log.d(Ardutooth.TAG, "prompting user to pair with a device");

        builder.setTitle(mActivity.getString(R.string.bluetooth_not_connected));
        builder.setMessage(mActivity.getString(R.string.pair_with_another_device_message));
        builder.setCancelable(true);
        builder.setPositiveButton(mActivity.getString(R.string.open_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent settings_intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                mActivity.startActivityForResult(settings_intent, REQUEST_ENABLE_BT);
            }
        });
        builder.setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Retrieve the device connected with.
     *
     * @return the connected with.
     */
    protected BluetoothDevice getDeviceConnected() {
        return mBtDevice;
    }

    /**
     * Retrieve the socket where's the connection it's happening.
     *
     * @return the socket opened for the connection.
     */
    protected BluetoothSocket getSocket() {
        return mSocket;
    }

    /**
     * Retrieve the {@link OutputStream} of the communication
     *
     * @return the {@link OutputStream} of the communication
     */
    protected OutputStream getOutputStream() {
        return mOutStream;
    }

    /**
     * Retrieve the {@link BufferedReader} which reads the {@link InputStream} of the communication
     *
     * @return the {@link BufferedReader} which reads the {@link InputStream} of the communication
     */
    protected BufferedReader getInputReader() {
        return mReader;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d(Ardutooth.TAG, "Connected from broadcast receiver");
                mBtDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(mActivity.getApplication(), mActivity.getString(R.string.connected_to) + mBtDevice.getName(), Toast.LENGTH_SHORT).show();
                connected = true;
                try {
                    if (!mSocket.isConnected()) { //might already be connected from openRFCOMMSocketWithBondedDevice()
                        mSocket = mBtDevice.createRfcommSocketToServiceRecord(Ardutooth.UUID);
                        connect();
                    }
                } catch (IOException e) {
                    Log.e(Ardutooth.TAG, "Error during socket creation", e);
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.d(Ardutooth.TAG, "Disconnected");
                Toast.makeText(mActivity.getApplication(), mActivity.getString(R.string.disconnected_from) + mBtDevice.getName(), Toast.LENGTH_SHORT).show();
                connected = false;
                mBtDevice = null;
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                    connected = false;
                    mBtDevice = null;
                }
            }
        }
    };

    private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = (BluetoothHeadset) proxy;
            }
        }

        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null;
            }
        }
    };

}
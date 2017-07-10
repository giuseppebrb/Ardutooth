package io.github.giuseppebrb.ardutooth;

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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

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
     * Allows to create or get the unique instance of the compononent.
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
     */
    private void connect() {
        OutputStream tmpOut = null;
        try {
            mSocket.connect();
            try {
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(Ardutooth.TAG, "Error occurred when creating output stream", e);
            }
            mOutStream = tmpOut;
        } catch (IOException e) {
            Log.e(Ardutooth.TAG, "Error opening connection", e);
            closeConnection();
        }
    }

    /**
     * Close the connection established with Arduino .
     */
    protected void closeConnection() {
        if (connected) {
            try {
                mSocket.close();
                mOutStream.close();
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
        if (mAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
            List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
            mBtDevice = devices.get(0);
            connected = true;
            Log.d(Ardutooth.TAG, "Already connected with a device");
        }
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
     * If bluetooth is on but there's no connection yet ask the user to open bluetooth settings and to connect with the Arduino bluetooth module.
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
                if (connected == false) {
                    builder.setTitle(mActivity.getString(R.string.bluetooth_not_connected));
                    builder.setMessage(mActivity.getString(R.string.bluetooth_not_connected_message));
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
     * Retrieve the device connected with.
     * @return the connected with.
     */
    protected BluetoothDevice getDeviceConnected() {
        return mBtDevice;
    }

    /**
     * Retrieve the socket where's the connection it's happaning.
     * @return the socket opened for the connection.
     */
    protected BluetoothSocket getSocket() {
        return mSocket;
    }

    /**
     * Retrieve the {@link OutputStream} of the communication
     * @return the {@link OutputStream} of the communication
     */
    protected OutputStream getOutputStream() {
        return mOutStream;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d(Ardutooth.TAG, "Connected");
                mBtDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(mActivity.getApplication(), mActivity.getString(R.string.connected_to) + mBtDevice.getName(), Toast.LENGTH_SHORT).show();
                connected = true;
                try {
                    mSocket = mBtDevice.createRfcommSocketToServiceRecord(Ardutooth.UUID);
                    connect();
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
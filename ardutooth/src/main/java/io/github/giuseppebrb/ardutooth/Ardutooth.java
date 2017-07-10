package io.github.giuseppebrb.ardutooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * This singleton class represents the main component of the library.
 * It can be used to easily set a stable connection with an Arduino and to send data to it using the Serial Monitor.
 *
 * <p>The first thing you need is to create or get an instance of {@link Ardutooth} using something like
 * {@code Ardutooth mArdutooth = Ardutooth.getInstance(this)} where parameter represents the instance of
 * the current {@link Activity}.</p>
 *
 * <p>Once you've done that, you can set a connection with an Arduino, asking the user to connect with it
 * using bluetooth: {@code mArdutooth.setConnection()}</p>
 *
 * <p>Now you can send data to Arduino using the methods {@code sendInt(value)}, {@code sendLong(value)},
 * {@code sendShort(value)}, {@code sendFloat(value)}, {@code sendDouble(value)}, {@code sendBoolean(value)},
 * {@code sendChar(value)}, {@code sendString(value)}</p>
 *
 * @author Giuseppe Barbato
 * @version 1.0.0
 */
public class Ardutooth {
    protected static final String TAG = "Ardutooth";
    protected static final UUID UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static Activity mActivity;
    private static BluetoothHandler mBtHandler;
    private static Ardutooth instance = null;

    /**
     * Allows to create or get the unique instance of the compononent.
     *
     * @param activity Define the {@link android.content.Context} where the object will work.
     * @return a new {@link Ardutooth} object if there's no one created before or it returns the unique instance of it otherwise.
     */
    public static Ardutooth getInstance(Activity activity) {
        mActivity = activity;
        if (instance == null)
            instance = new Ardutooth();
        return instance;
    }

    /**
     * Constructor
     */
    private Ardutooth() {
        mBtHandler = BluetoothHandler.getInstance(mActivity);
        isConnected();
    }

    /**
     * Check if there's a connection established with Arduino.
     *
     * @return true if there's connection, false otherwise.
     */
    public boolean isConnected() {
        try {
            mBtHandler.retrieveConnection();
        } catch (Exception e) {
            Log.d(Ardutooth.TAG, "An error occurred while retrieving connection", e);
        }
        return mBtHandler.connected;
    }

    /**
     * Allows to get the {@link BluetoothSocket} where the communication happen between Arduino and the current device.
     *
     * @return the {@link BluetoothSocket} if there's a connection established, null otherwise.
     */
    public BluetoothSocket getSocket() {
        return mBtHandler.getSocket();
    }

    public BluetoothDevice getDeviceConnected() {
        return mBtHandler.getDeviceConnected();
    }

    /**
     * Allows user to establish a connection with an Arduino scanning the bluetooth devices nearby.
     */
    public void setConnection() {
        mBtHandler.createConnection();
    }

    /**
     * Close the connection with Arduino if there's already one established.
     */
    public void disconnect() {
        mBtHandler.closeConnection();
    }

    /**
     * Send an {@link int} to Arduino through the Serial Monitor.
     *
     * @param value number to send.
     */
    public void sendInt(int value) {
        if (mBtHandler.getSocket() != null)
            try {
                mBtHandler.getOutputStream().write(String.valueOf(value).concat("\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Send a {@link short} to Arduino through the Serial Monitor.
     *
     * @param value number to send.
     */
    public void sendShort(short value) {
        if (mBtHandler.getSocket() != null)
            try {
                mBtHandler.getOutputStream().write(String.valueOf(value).concat("\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Send a {@link long} to Arduino through the Serial Monitor.
     *
     * @param value number to send.
     */
    public void sendLong(long value) {
        if (mBtHandler.getSocket() != null)
            try {
                mBtHandler.getOutputStream().write(String.valueOf(value).concat("\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Send a {@link float} to Arduino through the Serial Monitor.
     *
     * @param value number to send.
     */
    public void sendFloat(float value) {
        if (mBtHandler.getSocket() != null)
            try {
                mBtHandler.getOutputStream().write(String.valueOf(value).concat("\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Send a {@link double} to Arduino through the Serial Monitor.
     *
     * @param value number to send.
     */
    public void sendDouble(double value) {
        if (mBtHandler.getSocket() != null)
            try {
                mBtHandler.getOutputStream().write(String.valueOf(value).concat("\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Send a {@link char} to Arduino through the Serial Monitor.
     *
     * @param value {@link char} to send.
     */
    public void sendChar(char value) {
        if (mBtHandler.getSocket() != null)
            try {
                mBtHandler.getOutputStream().write(String.valueOf(value).concat("\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Send a {@link String} to Arduino through the Serial Monitor.
     *
     * @param value {@link String} to send.
     */
    public void sendString(String value) {
        if (mBtHandler.getSocket() != null)
            try {
                mBtHandler.getOutputStream().write(String.valueOf(value).concat("\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * Send a {@link boolean} to Arduino through the Serial Monitor.
     *
     * @param value {@link boolean} value to send.
     */
    public void sendBoolean(boolean value) {
        if (mBtHandler.getSocket() != null)
            try {
                mBtHandler.getOutputStream().write(String.valueOf(value).concat("\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
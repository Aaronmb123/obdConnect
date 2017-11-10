package com.bakeaaro.obdconnect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final static String OBD_ADDR = "00:1D:A5:BB:02:1B";
    private final static String OBD_NAME = "OBDII";
    private static final String TAG = "+++++++++++++++++++++++";

    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;

    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private byte[] mBuffer;
    private StringBuilder mStrBuffer = new StringBuilder();

    private Button mOBDButton;
    private Button mConnectButton;
    private Button mDisconnectButton;
    private Button mSendRpmCommandButton;
    private Button mSendSpeedCommandButton;
    private Button mSendGarbageCommandButton;
    private Button mRecvInputButton;

    private TextView mDeviceTV;
    private TextView mPairedDevicesTV;
    private TextView mrfCommTV;
    private TextView mSocketTV;
    private TextView mInputStreamTV;
    private TextView mOutputStreamTV;
    private TextView mSendCommandTV;
    private TextView mRecvInputTV;
    private TextView mBytesReadTV;
    private TextView mErrorTV;

    private EditText mCmdsET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mErrorTV = (TextView) findViewById(R.id.error_text_view);

        mRecvInputTV = (TextView) findViewById(R.id.recv_input_text_view);
        mRecvInputButton = (Button) findViewById(R.id.recv_input_button);
        mRecvInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (mInputStream.available() == 0) {
                        Toast.makeText(getApplicationContext(), "No data to read", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (IOException ioe ) {

                    return;
                }
//                byte b = 0;
//                StringBuilder res = new StringBuilder();
//                char c;
//
//                try {
//                    while (((b = (byte) mInputStream.read()) > -1)) {
//                        c = (char) b;
//                        res.append(c);
//                    }
//                } catch (IOException e) {
//                    mRecvInputTV.setText("Error during receive");
//                    Writer writer = new StringWriter();
//                    e.printStackTrace(new PrintWriter(writer));
//                    String s = writer.toString();
//                    mErrorTV.setText(s);
//                    return;
//                }
//                mRecvInputTV.setText(res);

                mBuffer = new byte[1024];
                int numBytes = 0; // bytes returned from read()

                try {
                    int i =  mInputStream.read();
                    mStrBuffer.append(String.format("%X ", i));
                } catch (IOException e) {
                    mRecvInputTV.setText("error receiving input");
                    return;
                }

                mRecvInputTV.setText(mStrBuffer);


            }
        });

        mSendGarbageCommandButton = (Button) findViewById(R.id.send_garbage_button);
        mSendGarbageCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String cmd = mCmdsET.getText().toString();
                //if (cmd != null) {
                byte[] bytes = new byte[]{0x01, 0x00};

                try {
                    mOutputStream.write(bytes);
                    mSendCommandTV.setText("Bytes sent: " + String.format("%X ", bytes[0]) + " " + String.format("%X ", bytes[1]));
                } catch (IOException e) {
                    mSendCommandTV.setText("Bytes not sent");
                    Writer writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }

                //}
            }
        });

        mSendSpeedCommandButton = (Button) findViewById(R.id.send_speed_button);
        mSendSpeedCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String cmd = mCmdsET.getText().toString();
                //if (cmd != null) {
                byte[] bytes = new byte[]{0x01, 0x0D};

                try {
                    mOutputStream.write(bytes);
                    mSendCommandTV.setText("Bytes sent: " + String.format("%X ", bytes[0]) + " " + String.format("%X ", bytes[1]));
                } catch (IOException e) {
                    mSendCommandTV.setText("Bytes not sent");
                    Writer writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }

                //}
            }
        });
        //mCmdsET = (EditText) findViewById(R.id.cmds_edit_text);
        mSendRpmCommandButton = (Button) findViewById(R.id.send_rpm_button);
        mSendRpmCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String cmd = mCmdsET.getText().toString();
                //if (cmd != null) {
                byte[] bytes = new byte[]{0x01, 0x0C};

                try {
                    mOutputStream.write(bytes);
                    mSendCommandTV.setText("Bytes sent: " + String.format("%X ", bytes[0]) + " " + String.format("%X ", bytes[1]));
                } catch (IOException e) {
                    mSendCommandTV.setText("Bytes not sent");
                    Writer writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }

                //}
            }
        });

        mSendCommandTV = (TextView) findViewById(R.id.send_cmds_text_view);

        mOBDButton = (Button) findViewById(R.id.paired_devices_button);
        mOBDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pairedDevices = mBluetoothAdapter.getBondedDevices();
                mPairedDevicesTV = (TextView) findViewById(R.id.paired_devices_text_view);
                String devices = "";

                for (BluetoothDevice device : pairedDevices) {
                    devices += device.getName() + " " + device.getAddress() + " ";
                    mPairedDevicesTV.setText(devices);
                    if (device.getName().equals(OBD_NAME)) {
                        mDevice = device;
                        mDeviceTV.setText(mDevice.getName());
                        break;
                    }
                }


                if (!mBluetoothAdapter.checkBluetoothAddress(OBD_ADDR)) {
                    Toast.makeText(getApplicationContext(), "Invalid MAC addr", Toast.LENGTH_SHORT).show();
                }

                mDeviceTV.setText(mDevice.getName());

            }
        });

        mDeviceTV = (TextView) findViewById(R.id.device_text_view);

        mrfCommTV = (TextView) findViewById(R.id.rfcomm_text_view);
        mSocketTV = (TextView) findViewById(R.id.connected_text_view);
        mInputStreamTV = (TextView) findViewById(R.id.inputstream_text_view);
        mOutputStreamTV = (TextView) findViewById(R.id.outputstream_text_view);
        mBytesReadTV = (TextView) findViewById(R.id.bytes_read_text_view);

        mConnectButton = (Button) findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    // client device must have UUID of server device (server device UUID is random)
                    mSocket = mDevice.createRfcommSocketToServiceRecord(mDevice.getUuids()[0].getUuid());
                } catch (IOException e) {
                    mrfCommTV.setText("RFComm not created");
                    return;
                }
                mrfCommTV.setText("RFComm created");
                mErrorTV.setText("");

                try {
                    mSocket.connect();
                }catch (IOException e) {
                    mSocketTV.setText("Socket not connected");
                    Writer writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }
                mSocketTV.setText("Socket connected");
                mErrorTV.setText("");
                try {
                    mInputStream = mSocket.getInputStream();
                } catch (IOException e) {
                    mInputStreamTV.setText("Input Stream not set");
                    mSocketTV.setText("Socket not connected");
                    Writer writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }
                mInputStreamTV.setText("Input Stream set");
                mErrorTV.setText("");
                try {
                    mOutputStream = mSocket.getOutputStream();
                } catch (IOException e) {
                    mOutputStreamTV.setText("Output Stream not set");
                    mSocketTV.setText("Socket not connected");
                    Writer writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }
                mOutputStreamTV.setText("Output Stream set");
                mErrorTV.setText("");
            }
        });

        mDisconnectButton = (Button) findViewById(R.id.disconnect_button);
        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),
                            "Close Socket failed", Toast.LENGTH_SHORT).show();
                    return;
                }
                mrfCommTV.setText("");
                mSocketTV.setText("Socket Closed");
                mInputStreamTV.setText("");
                mOutputStreamTV.setText("");
                mErrorTV.setText("");
            }
        });

    }
};

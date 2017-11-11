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

    private Button mResetStringBuffer;
    private Button mOBDButton;
    private Button mConnectButton;
    private Button mResetOBDButton;
    private Button mSelectProtocolButton;
    private Button mVerifyProtocolButton;
    private Button mSendRpmCommandButton;
    private Button mSendSpeedCommandButton;
    private Button mRecvInputButton;

    private Button mDisconnectButton;

    private TextView mDeviceTV;
    private TextView mPairedDevicesTV;
    private TextView mrfCommTV;
    private TextView mSocketTV;
    private TextView mInputStreamTV;
    private TextView mOutputStreamTV;
    private TextView mResetSentTV;
    private TextView mProtocolSentTV;
    private TextView mVerifyProtocolTV;
    private TextView mSendCommandTV;
    private TextView mRecvInputTV;
    private TextView mErrorTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mResetStringBuffer = (Button) findViewById(R.id.reset_str_buffer_button);
        mResetStringBuffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStrBuffer.setLength(0);
                mStrBuffer.trimToSize();
                mRecvInputTV.setText("");
            }
        });

        mDeviceTV = (TextView) findViewById(R.id.device_text_view);
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

        mErrorTV = (TextView) findViewById(R.id.error_text_view);

        mrfCommTV = (TextView) findViewById(R.id.rfcomm_text_view);
        mSocketTV = (TextView) findViewById(R.id.connected_text_view);
        mInputStreamTV = (TextView) findViewById(R.id.inputstream_text_view);
        mOutputStreamTV = (TextView) findViewById(R.id.outputstream_text_view);

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

        mSendCommandTV = (TextView) findViewById(R.id.send_cmds_text_view);

        mResetSentTV = (TextView) findViewById(R.id.reset_sent_text_view);
        mResetOBDButton = (Button) findViewById(R.id.send_reset_button);
        mResetOBDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    mOutputStream.write(("AT Z\r").getBytes());
                } catch (IOException ieo) {
                    mResetSentTV.setText("Error");
                    mSendCommandTV.setText("");
                    Writer writer = new StringWriter();
                    ieo.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }

                try {
                    mOutputStream.flush();
                } catch (IOException ieo) {
                    Toast.makeText(getApplicationContext(), "No flush", Toast.LENGTH_SHORT).show();
                }

                mSendCommandTV.setText("AT Z\\r");
                mResetSentTV.setText("Reset Cmd Sent");

            }
        });

        mProtocolSentTV = (TextView) findViewById(R.id.protocol_sent_text_view);
        mSelectProtocolButton = (Button) findViewById(R.id.select_protocol_button);
        mSelectProtocolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    mOutputStream.write(("AT SP 0\r").getBytes());
                } catch (IOException ieo) {
                    mProtocolSentTV.setText("Error");
                    mSendCommandTV.setText("");
                    Writer writer = new StringWriter();
                    ieo.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }

                try {
                    mOutputStream.flush();
                } catch (IOException ieo) {
                    Toast.makeText(getApplicationContext(), "No flush", Toast.LENGTH_SHORT).show();
                }

                mSendCommandTV.setText("AT SP 0\\r");
                mProtocolSentTV.setText("Protocol Cmd Sent");
            }
        });

        mVerifyProtocolTV = (TextView) findViewById(R.id.verify_text_view);
        mVerifyProtocolButton = (Button) findViewById(R.id.verify_protocol_button);
        mVerifyProtocolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    mOutputStream.write(("AT DP\r").getBytes());
                } catch (IOException ieo) {
                    mVerifyProtocolTV.setText("Error");
                    mSendCommandTV.setText("");
                    Writer writer = new StringWriter();
                    ieo.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }

                try {
                    mOutputStream.flush();
                } catch (IOException ieo) {
                    Toast.makeText(getApplicationContext(), "No flush", Toast.LENGTH_SHORT).show();
                }

                mVerifyProtocolTV.setText("Verify Protocol Cmd Sent");
                mSendCommandTV.setText("AT DP\\r");
            }
        });

        mSendRpmCommandButton = (Button) findViewById(R.id.send_rpm_button);
        mSendRpmCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mOutputStream.write(("01 0C\r").getBytes());
                } catch (IOException e) {
                    mSendCommandTV.setText("Error");
                    Writer writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }

                try {
                    mOutputStream.flush();
                } catch (IOException ieo) {
                    Toast.makeText(getApplicationContext(), "No flush", Toast.LENGTH_SHORT).show();
                }

                mSendCommandTV.setText("01 0C\\r");
            }
        });

        mSendSpeedCommandButton = (Button) findViewById(R.id.send_speed_button);
        mSendSpeedCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    mOutputStream.write(("01 0D\r").getBytes());
                } catch (IOException e) {
                    mSendCommandTV.setText("Error");
                    Writer writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    String s = writer.toString();
                    mErrorTV.setText(s);
                    return;
                }

                try {
                    mOutputStream.flush();
                } catch (IOException ieo) {
                    Toast.makeText(getApplicationContext(), "No flush", Toast.LENGTH_SHORT).show();
                }

                mSendCommandTV.setText("01 0D\\r");

            }
        });

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
                    byte b =  (byte) mInputStream.read();
                    mStrBuffer.append((char) b);
                } catch (IOException e) {
                    mRecvInputTV.setText("error receiving input");
                    return;
                }

                mRecvInputTV.setText(mStrBuffer);


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

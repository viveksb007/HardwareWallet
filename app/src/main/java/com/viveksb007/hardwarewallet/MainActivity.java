package com.viveksb007.hardwarewallet;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final String ACTION_USB_PERMISSION = "com.viveksb007.hardwarewallet.USB_PERMISSION";

    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private UsbSerialDevice serialPort;
    private UsbDeviceConnection connection;

    private TextView tvSerialConsole;
    private String recipientAddress;
    private String amountInBtc;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(final byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String data = null;
                    try {
                        data = new String(bytes, "UTF-8");
                        tvSerialConsole.append(data);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(usbDevice);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(usbDevice, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) {
                            serialPort.setBaudRate(115200);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvSerialConsole.append("Serial Connection Opened\n");
                                }
                            });
                        } else {
                            Log.d(TAG, "Serial Port not opened.");
                        }
                    } else {
                        Log.d(TAG, "Serial Port is NULL.");
                    }
                } else {
                    Log.d(TAG, "Permission not granted.");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextInputEditText etRecipientAddress = findViewById(R.id.et_recipient_address);
        final TextInputEditText etAmountInBtc = findViewById(R.id.et_amount);
        tvSerialConsole = findViewById(R.id.tv_serial_output);
        Button btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testDeviceCommunication(etRecipientAddress.getText().toString());
                /*
                recipientAddress = etRecipientAddress.getText().toString();
                amountInBtc = etAmountInBtc.getText().toString();
                if ("".equals(recipientAddress) || "".equals(amountInBtc)) {
                    Toast.makeText(MainActivity.this, "Invalid Values", Toast.LENGTH_SHORT).show();
                } else {
                    signTransaction();
                }
                */
            }
        });

        usbManager = (UsbManager) getSystemService(USB_SERVICE);

        Button btnInitialize = findViewById(R.id.btn_initialize);
        btnInitialize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
                if (!usbDevices.isEmpty()) {
                    boolean keep = true;
                    for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                        usbDevice = entry.getValue();
                        int deviceVID = usbDevice.getVendorId();
                        Log.v(TAG, deviceVID + "");
                        if (deviceVID == 1027) {
                            Log.v(TAG, "Device Found");
                            PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                            usbManager.requestPermission(usbDevice, pi);
                            keep = false;
                        } else {
                            connection = null;
                            usbDevice = null;
                        }

                        if (!keep)
                            break;
                    }
                    if (keep) {
                        Log.e(TAG, "No device found.");
                    }
                }

            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
    }

    private void testDeviceCommunication(final String data) {
        serialPort.write(data.getBytes());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvSerialConsole.append("Data Sent : " + data);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void signTransaction() {

    }

}

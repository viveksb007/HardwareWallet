package com.viveksb007.hardwarewallet;

import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final String ACTION_USB_PERMISSION = "com.viveksb007.hardwarewallet.USB_PERMISSION";

    private UsbManager usbManager;
    private UsbDevice usbDevice;
    private UsbDeviceConnection connection;
    private UsbSerialPort port;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private TextView tvSerialConsole;
    private ScrollView consoleScrollView;
    private String recipientAddress;
    private String amountInBtc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextInputEditText etRecipientAddress = findViewById(R.id.et_recipient_address);
        final TextInputEditText etAmountInBtc = findViewById(R.id.et_amount);
        tvSerialConsole = findViewById(R.id.tv_serial_output);
        consoleScrollView = findViewById(R.id.scrollConsole);
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
        assert usbManager != null;

        /*
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                usbDevice = entry.getValue();
                int deviceVID = usbDevice.getVendorId();
                Log.v(TAG, deviceVID + " VID");
                Log.v(TAG, usbDevice.getProductId() + " PID");
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
        } else {
            Log.v(TAG, "No Usb device found");
        }
        */


        ProbeTable customProbeTable = new ProbeTable();
        customProbeTable.addProduct(1027, 0, FtdiSerialDriver.class);

        List<UsbSerialDriver> availableSerialDrivers = (new UsbSerialProber(customProbeTable)).findAllDrivers(usbManager);
        if (availableSerialDrivers.isEmpty()) {
            Log.v(TAG, "No serial device found.");
            return;
        }
        UsbSerialDriver driver = availableSerialDrivers.get(0);
        UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
        if (connection == null) {
            Log.v(TAG, "Usb permission not granted");
            return;
        }
        port = driver.getPorts().get(0);
        try {
            port.open(connection);
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            // Deal with error.
        }
    }

    private final SerialInputOutputManager.Listener listener = new SerialInputOutputManager.Listener() {
        @Override
        public void onNewData(final byte[] data) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = null;
                    try {
                        message = "Read " + data.length + " bytes: \n" + (new String(data, "UTF-8")) + "\n";
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        message = "Unsupported encoding";
                    }
                    tvSerialConsole.append(message);
                    consoleScrollView.smoothScrollTo(0, tvSerialConsole.getBottom());
                }
            });
        }

        @Override
        public void onRunError(Exception e) {
            Log.v(TAG, "Running Error");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (port != null) {
            mSerialIoManager = new SerialInputOutputManager(port, listener);
            mExecutor.submit(mSerialIoManager);
        } else {
            Log.v(TAG, "Port is NULL.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
        if (port != null) {
            try {
                port.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            port = null;
        }
    }

    private void testDeviceCommunication(final String data) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void signTransaction() {

    }

}

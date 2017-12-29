package com.viveksb007.hardwarewallet;

import android.hardware.usb.UsbManager;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final String ACTION_USB_PERMISSION = "com.viveksb007.hardwarewallet.USB_PERMISSION";

    private UsbManager usbManager;

    private TextView tvSerialConsole;
    private String recipientAddress;
    private String amountInBtc;

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

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
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

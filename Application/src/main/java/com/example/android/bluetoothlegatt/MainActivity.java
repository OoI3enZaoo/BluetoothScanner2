package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    String dust = null;
    String CO = null;
    String NO2 = null;

    int statusCurrent = 1;
    int counter = 0;

    List<String> arrayDust = new ArrayList<String>();
    List<String> arrayCO = new ArrayList<String>();
    List<String> arrayNO2 = new ArrayList<String>();;
    //xxxxxxx bluetooth le xxxxxxx
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private TextView mDataFieldCO;
    private TextView mDataFieldNO2;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private String hrValue;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private String TAG = "TAG";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //displayDataCO(intent.getStringExtra(BluetoothLeService.EXTRA_DATA1));
                //displayDataNO2(intent.getStringExtra(BluetoothLeService.EXTRA_DATA2));

                dust =intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                CO = intent.getStringExtra(BluetoothLeService.EXTRA_DATA1);
                NO2 = intent.getStringExtra(BluetoothLeService.EXTRA_DATA2);

                Log.d(TAG , "test value dust =" + dust +"," + "CO =" + CO + "," + "NO2 =" + NO2);

                if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA) != null) {
                    arrayDust.add(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    Log.d(TAG , "Dust List = " + String.valueOf(arrayDust));
                }
                if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA1) != null) {
                    arrayCO.add(intent.getStringExtra(BluetoothLeService.EXTRA_DATA1));
                    Log.d(TAG , "CO List = " +String.valueOf(arrayCO));
                }
                if(intent.getStringExtra(BluetoothLeService.EXTRA_DATA2) != null){
                    arrayNO2.add(intent.getStringExtra(BluetoothLeService.EXTRA_DATA2));
                    Log.d(TAG , "NO2 List = " + String.valueOf(arrayNO2));
                }

                Log.d(TAG , "Dust = " + intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                Log.d(TAG , "CO = " + intent.getStringExtra(BluetoothLeService.EXTRA_DATA1));
                Log.d(TAG , "NO2 = " + intent.getStringExtra(BluetoothLeService.EXTRA_DATA2));
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
        hrValue = "";
    }

    @Override
    public void onBackPressed() {

        moveTaskToBack(true);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //xxxxxx bluetoothLE xxxxx
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.textdata);
        mDataFieldCO = (TextView) findViewById(R.id.textdata1);
        mDataFieldNO2 = (TextView) findViewById(R.id.textdata2);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    //xxxxxxxxxxxxxxxxxxxxxxxxx bluetoothLE method xxxxxxxxxxxxxxxxxxxxxxxxx
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {//write textview for Dust
        if (data != null) {
            mDataField.setText(data);

        }
    }
    private void displayDataCO(String data) {// write textview for CO
        if (data != null) {
            mDataFieldCO.setText(data);

        }
    }
    private void displayDataNO2(String data) {// write textview for NO2
        if (data != null) {
            mDataFieldNO2.setText(data);

        }
    }
/*
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;

        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();

            if(uuid.equals("0000180d-0000-1000-8000-00805f9b34fb")){
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if(uuid.equals("00002a37-0000-1000-8000-00805f9b34fb")){
                        mNotifyCharacteristic = gattCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);

                    }
                    if(uuid.equals("00002A29-0000-1000-8000-00805f9b34fb")){
                        mNotifyCharacteristic = gattCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);

                    }
                    if(uuid.equals("00002B29-0000-1000-8000-00805f9b34fb")){
                        mNotifyCharacteristic = gattCharacteristic;
                        mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);

                    }
                }//end for

            }//end if


        }//end for
    }
*/

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);



        }
        Timer myTimer;
        myTimer = new Timer();

        myTimer.schedule(new TimerTask() {
            public void run() {

                if( mConnected == true ) {//if bluttooth is enable
                    final BluetoothGattCharacteristic characteristic =
                            mGattCharacteristics.get(2).get(counter);
                    if (statusCurrent == 1) {
                        counter = 0;
                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(
                                characteristic, true);

                        if (dust != null) {
                            Log.d(TAG, "loop1 !=null");
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                            statusCurrent++;
                        } else {
                            Log.d(TAG, "loop1 ==null");
                        }
                    }
                    if (statusCurrent == 2) {
                        counter = 1;
                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(
                                characteristic, true);
                        if (CO != null) {
                            Log.d(TAG, "loop2 !=null");
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                            statusCurrent++;
                        } else {
                            Log.d(TAG, "loop2 ==null");
                        }
                    }
                    if (statusCurrent == 3) {
                        counter = 2;
                        mNotifyCharacteristic = characteristic;
                        mBluetoothLeService.setCharacteristicNotification(
                                characteristic, true);

                    }
                }
                else if (mConnected == false){// if bluetooth disable
                    try {
                        // thread to sleep for 1000 milliseconds
                        Thread.sleep(3000);
                        Log.d(TAG,"BLE is dead");

                        Random r = new Random();
                        dust = arrayDust.get(r.nextInt(arrayDust.size()));
                        CO = arrayCO.get(r.nextInt(arrayCO.size()));
                        NO2 = arrayNO2.get(r.nextInt(arrayNO2.size()));
                        Log.d(TAG , "Dust random = = " + dust);
                        Log.d(TAG , "CO random = = " +  CO );
                        Log.d(TAG , "NO2 random = = " + NO2);
                    } catch (Exception e) {

                    }


                }


            }
        }, 0, 1000);




    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


}

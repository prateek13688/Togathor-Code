package com.uf.togathor.utils.appservices;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.uf.togathor.db.couchdb.model.Group;
import com.uf.togathor.db.couchdb.model.User;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.model.Message;
import com.uf.togathor.utils.constants.Const;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alok on 1/26/15.
 */
public class CheckInServiceRetro extends Service {

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private final String professorMACID = "84:DD:20:EA:C3:0F";
    private final double larsenLat = 29.643119;
    private final double larsenLong = -82.347155;

    Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private int foundProf = 0;
    private Handler handler;
    private Runnable runnable;
    private boolean inApp = false;

    private List<BluetoothDevice> listOfDevices;
    private BluetoothManager bluetoothManager;

    private Messenger checkInHandler;

    public CheckInServiceRetro() {
        super();
    }

    @Override
    public void onCreate() {

        listOfDevices = new ArrayList<>();
        handler = new Handler();

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        context = this;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        inApp = intent.getBooleanExtra("inapp", false);
        checkInHandler = (Messenger) intent.getExtras().get("handler");

        if(!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();

        while(!mBluetoothAdapter.isEnabled());

        checkIn();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    public void checkIn() {

        if(inApp) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    stopCheckIn();
                }
            };
            handler.postDelayed(runnable, SCAN_PERIOD);
        }

        mBluetoothAdapter.startLeScan(mLeScanCallback);

    }

    public void stopCheckIn()   {
        handler.removeCallbacks(runnable);
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        if(mBluetoothAdapter.isEnabled() && foundProf == 1)
            mBluetoothAdapter.disable();

        android.os.Message message = android.os.Message.obtain();
        message.arg1 = foundProf;
        try {
            checkInHandler.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.d("CheckInService", "BLE address : " + device.getAddress());

                    Message message = new Message();
                    Group toGroup = null;

                    if(inApp) {
                        User fromUser = UsersManagement.getLoginUser();
                        toGroup = UsersManagement.getToGroup();
                        message.setFromUserId(fromUser.getId());
                        message.setFromUserName(fromUser.getName());
                        message.setToGroupId(toGroup.getId());
                        message.setToGroupName(toGroup.getName());
                        message.setMessageType(Const.LOCATION);
                        message.setLatitude(larsenLat + "");
                        message.setLongitude(larsenLong + "");
                    }

                    listOfDevices.add(device);

                    if (device.getAddress().equalsIgnoreCase(professorMACID)) {
                        if(inApp)
                            SyncModule.sendMessage(message, context, null, toGroup);
                        foundProf = 1;

                        if(inApp)
                            stopCheckIn();
                    }
                }
            };
}

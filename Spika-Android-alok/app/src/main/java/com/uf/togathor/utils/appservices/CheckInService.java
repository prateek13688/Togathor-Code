package com.uf.togathor.utils.appservices;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
 * Created by alok on 1/26/15
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class CheckInService extends Service {

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
    private boolean alreadyCheckedIn = false;
    BluetoothLeScanner scanner;

    private ScanSettings settings;
    private List<ScanFilter> filters;

    private Messenger checkInHandler;

    public CheckInService() {
        super();
    }

    @Override
    public void onCreate() {

        handler = new Handler();

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        context = this;

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        inApp = intent.getBooleanExtra("inapp", false);
        checkInHandler = (Messenger) intent.getExtras().get("handler");

        if(!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();

        while(!mBluetoothAdapter.isEnabled());

        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        if(inApp)
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        else
            settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();

        filters = new ArrayList<>();
        checkIn();

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

        scanner.startScan(filters, settings, serviceScanCallback);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopCheckIn()   {
        handler.removeCallbacks(runnable);
        scanner.stopScan(serviceScanCallback);
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

    private ScanCallback serviceScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            Log.d("CheckInService", "BLE address : " + result.getDevice().getAddress());

            Message message = new Message();
            Group toGroup = null;

            if(!alreadyCheckedIn) {
                if (inApp) {
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

                if (result.getDevice().getAddress().equalsIgnoreCase(professorMACID)) {
                    alreadyCheckedIn = true;
                    if (inApp)
                        SyncModule.sendMessage(message, context, null, toGroup);
                    foundProf = 1;

                    if (inApp)
                        stopCheckIn();
                }
            }
        }
    };
}

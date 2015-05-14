package com.uf.togathor.fragments;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.uf.togathor.R;
import com.uf.togathor.Togathor;
import com.uf.togathor.management.UsersManagement;
import com.uf.togathor.management.SyncModule;
import com.uf.togathor.model.Message;
import com.uf.togathor.utils.constants.Const;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class TimelineBLEFragment extends Fragment {

    private static final String TAG = "TimelineFragment";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner scanner;
    private ScanSettings settings;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_groups, container, false);

        bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if(!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();

        while(!mBluetoothAdapter.isEnabled());

        Togathor.getMessagesDataSource().close();
        Togathor.getMessagesDataSource().open();

        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        scanner.startScan(new ArrayList<ScanFilter>(), settings, serviceScanCallback);

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }
        return false;
    }

    private ScanCallback serviceScanCallback =
            new ScanCallback() {
                Message message;
                List<ParcelUuid> parcelUuids;
                @Override
                public void onScanResult(int callbackType, ScanResult result) {

                    try {
                        parcelUuids = result.getScanRecord().getServiceUuids();
                        Log.d(TAG, Arrays.toString(result.getScanRecord().getBytes()));
                    } catch (Exception e)   {
                        return;
                    }

                    message = new Message();
                    message.setId(Const._ID);
                    message.setBody("BLE address : " + "BLE address : " + result.getDevice().getAddress() + " with RSSI: " + result.getRssi());
                    message.setFromUserId(UsersManagement.getLoginUser().getId());
                    message.setFromUserName(UsersManagement.getLoginUser().getName());
                    message.setToUserId(UsersManagement.getLoginUser().getId());
                    message.setToUserName(UsersManagement.getLoginUser().getName());
                    message.setMessageType(Const.TEXT);
                    message.setCreated(1234567);
                    message.setModified(1234567);
                    SyncModule.sendMessage(message, getActivity(),UsersManagement.getLoginUser(), null);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
}
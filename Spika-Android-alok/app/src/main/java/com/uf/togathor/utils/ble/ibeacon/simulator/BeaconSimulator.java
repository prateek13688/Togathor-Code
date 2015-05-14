package com.uf.togathor.utils.ble.ibeacon.simulator;

import com.uf.togathor.utils.ble.ibeacon.IBeacon;

import java.util.List;

/**
 * Created by dyoung on 4/18/14.
 */
public interface BeaconSimulator {
    public List<IBeacon> getBeacons();
}

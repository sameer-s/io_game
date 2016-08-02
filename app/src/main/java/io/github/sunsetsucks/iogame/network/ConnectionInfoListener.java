package io.github.sunsetsucks.iogame.network;

import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import io.github.sunsetsucks.iogame.Util;

/**
 * Created by Sameer on 2016-08-01.
 */
public class ConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener
{
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo)
    {
        Util.toast("Info for connection available!");
    }
}

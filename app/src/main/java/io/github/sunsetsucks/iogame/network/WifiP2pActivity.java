package io.github.sunsetsucks.iogame.network;

import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by ssuri on 8/1/16.
 */
public interface WifiP2pActivity extends WifiP2pManager.ChannelListener
{
    void setIsWifiP2pEnabled(boolean isWifiP2pEnabled);
    void resetData();
}
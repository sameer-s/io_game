package io.github.sunsetsucks.iogame;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.sunsetsucks.iogame.view.IOGameGLSurfaceView;

import static io.github.sunsetsucks.iogame.Util.toast;

public class MainActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener
{
    private IOGameGLSurfaceView glView;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private IOGameBroadcastReciever receiver = null;
    private IntentFilter intentFilter = null;

    private boolean retryChannel = false;

    private ListView deviceList;

    private List<WifiP2pDevice> peers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Util.context = this;

        glView = new IOGameGLSurfaceView(this);
//        setContentView(glView);
        setContentView(R.layout.main);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        intentFilter = new IntentFilter();
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        deviceList = (ListView) findViewById(R.id.device_list);
        deviceList.setAdapter(new DeviceListAdapter());
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                WifiP2pDevice device = (WifiP2pDevice) deviceList.getItemAtPosition(i);

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                manager.connect(channel, config, null);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        receiver = new IOGameBroadcastReciever();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (glView != null) glView.setVisibility(View.GONE);
        unregisterReceiver(receiver);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        manager.removeGroup(channel, null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (glView != null && hasFocus && glView.getVisibility() == View.GONE)
        {
            glView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    @Override
    public void onChannelDisconnected()
    {
        // we will try once more
        if (manager != null && !retryChannel)
        {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else
        {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void setup()
    {
        manager.createGroup(channel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                toast("Created p2p group");
                manager.requestConnectionInfo(channel, MainActivity.this);
            }

            @Override
            public void onFailure(int i)
            {
                toast("Failed to create p2p group. Error code %d", i);
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo)
    {
        if (wifiP2pInfo.isGroupOwner)
        {

        }
    }

    public void host(View view)
    {
        view.setVisibility(View.GONE);
        ((ViewGroup) view.getParent()).findViewById(R.id.button_discover).setVisibility(View.VISIBLE);
        deviceList.setVisibility(View.VISIBLE);

        manager.removeGroup(channel, new WifiP2pManager.ActionListener()
        {
            @Override
            public void onSuccess()
            {
                setup();
            }

            @Override
            public void onFailure(int i)
            {
                setup();
            }
        });
    }

    public void discover(View view)
    {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener()
        {

            @Override
            public void onSuccess()
            {
                toast("Discovery initiated");
            }

            @Override
            public void onFailure(int reasonCode)
            {
                toast("Discovery failed. Error code: %d", reasonCode);
            }
        });
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList)
    {
        peers.clear();
        peers.addAll(wifiP2pDeviceList.getDeviceList());
        ((DeviceListAdapter) deviceList.getAdapter()).notifyDataSetChanged();

        if(peers.size() == 0)
        {
            toast("No devices found!");
        }
    }

    public class IOGameBroadcastReciever extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
            {
                if (manager != null)
                {
                    manager.requestPeers(channel, MainActivity.this);
                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
            {
                if (manager == null)
                {
                    return;
                }

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected())
                {
                    // we are connected with the other device, request connection
                    // info to find group owner IP

                    manager.requestConnectionInfo(channel, MainActivity.this);
                } /*else
                {
                    // It's a Disconnect
                    activity.resetData();
                }*/
            }
        }
    }

    public class DeviceListAdapter extends ArrayAdapter<WifiP2pDevice>
    {
        public DeviceListAdapter()
        {
            super(MainActivity.this, R.layout.device_row, peers);
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v = convertView;
            if (v == null)
            {
                LayoutInflater vi = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.device_row, null);
            }
            WifiP2pDevice device = peers.get(position);
            if (device != null)
            {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null)
                {
                    top.setText(device.deviceName);
                }
                if (bottom != null)
                {
                    bottom.setText(Util.getDeviceStatus(device.status));
                }
            }

            return v;
        }
    }
}

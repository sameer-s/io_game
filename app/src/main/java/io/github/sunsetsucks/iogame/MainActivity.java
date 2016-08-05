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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;


import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.sunsetsucks.iogame.network.NetworkConnection;
import io.github.sunsetsucks.iogame.network.NetworkHandler;
import io.github.sunsetsucks.iogame.network.ServerListeningThread;
import io.github.sunsetsucks.iogame.view.IOGameGLSurfaceView;

import static io.github.sunsetsucks.iogame.Util.toast;

public class MainActivity extends AppCompatActivity implements
		WifiP2pManager.ChannelListener, WifiP2pManager.ConnectionInfoListener,
		WifiP2pManager.PeerListListener, NetworkHandler
{
	private IOGameGLSurfaceView glView;
	private WifiP2pManager manager;
	private WifiP2pManager.Channel channel;
	private IOGameBroadcastReceiver receiver = null;
	private IntentFilter intentFilter = null;
	private ServerListeningThread serverListeningThread = null;
	private boolean retryChannel = false;

    private Button beginGameButton;
	private ListView deviceList;
	private List<WifiP2pDevice> peers = new ArrayList<>();

	private List<NetworkConnection> connections = new ArrayList<>();

    private int unsocketedConnections = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Util.context = this;

		setContentView(R.layout.main);

        glView = (IOGameGLSurfaceView) findViewById(R.id.gl_view);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);

        manager.discoverPeers(channel, null);

		intentFilter = new IntentFilter();
		// intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		// intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        beginGameButton = (Button) findViewById(R.id.button_begin_game);
		deviceList = (ListView) findViewById(R.id.device_list);
		deviceList.setAdapter(new DeviceListAdapter());
		deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int i, long l)
			{
				WifiP2pDevice device = (WifiP2pDevice) deviceList
						.getItemAtPosition(i);

				WifiP2pConfig config = new WifiP2pConfig();
				config.deviceAddress = device.deviceAddress;
				config.wps.setup = WpsInfo.PBC;

				manager.connect(channel, config, null);

                unsocketedConnections++;
                beginGameButton.setVisibility(View.GONE);
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		receiver = new IOGameBroadcastReceiver();
		registerReceiver(receiver, intentFilter);

		if(glView != null)
		{
			glView.onResume();
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		unregisterReceiver(receiver);

		if(glView != null)
		{
			glView.onPause();
		}
	}

	@Override
	public void onStop()
	{
		super.onStop();

		manager.removeGroup(channel, null);
		if (serverListeningThread != null)
		{
			try
			{
				serverListeningThread.close();
			}
			catch (IOException e)
			{
				Log.e("iogame_networking", "Failed to close server socket");
			}
		}

        for(NetworkConnection connection : connections)
        {
            connection.stop();
        }
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.action_items, menu);
		return true;
	}

	@Override
	public void onChannelDisconnected()
	{
		// we will try once more
		if (manager != null && !retryChannel)
		{
			Toast.makeText(this, "Channel lost. Trying again",
					Toast.LENGTH_LONG).show();
			retryChannel = true;
			manager.initialize(this, getMainLooper(), this);
		}
		else
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
			}

			@Override
			public void onFailure(int i)
			{
				toast("Failed to create p2p group. Error code %d", i);
			}
		});
	}

	@Override
	public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo)
	{
		if (!wifiP2pInfo.isGroupOwner)
		{
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Socket socket = new Socket(wifiP2pInfo.groupOwnerAddress, Util.PORT);
                        NetworkConnection conn = new NetworkConnection(socket, MainActivity.this);
                        addNewConnection(conn);
                    } catch (IOException e)
                    {
                        Log.d("iogame_networking",
                                "An error occured in connecting to the server");
                        finish();
                        e.printStackTrace();
                    }
                }
            };
            t.start();
		}
	}

	public void host(View view)
	{
		serverListeningThread = new ServerListeningThread();
		serverListeningThread.setNetworkHandler(this);
		serverListeningThread.start();

		view.setVisibility(View.GONE);
		((ViewGroup) view.getParent()).findViewById(R.id.button_discover)
				.setVisibility(View.VISIBLE);
		beginGameButton.setVisibility(View.VISIBLE);
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

        Util.isHost = true;
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

	public void beginGame(View view)
	{
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
                animator.showNext();
            }
        });

        if(view != null)
        {
            // TODO Reimplement [complete]
            broadcastMessage("begin");
        }
	}

	@Override
	public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList)
	{
		peers.clear();
		peers.addAll(wifiP2pDeviceList.getDeviceList());
		((DeviceListAdapter) deviceList.getAdapter()).notifyDataSetChanged();

		if (peers.size() == 0)
		{
			toast("No devices found!");
		}
	}

	public class IOGameBroadcastReceiver extends BroadcastReceiver
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
			}
			else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
					.equals(action))
			{
				if (manager == null)
				{
					return;
				}

				NetworkInfo networkInfo = intent
						.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

				if (networkInfo.isConnected())
				{
					// we are connected with the other device, request
					// connection
					// info to find group owner IP

					manager.requestConnectionInfo(channel, MainActivity.this);
				}
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
				LayoutInflater vi = (LayoutInflater) MainActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.device_row, null);
			}
			WifiP2pDevice device = peers.get(position);
			if (device != null)
			{
				TextView top = (TextView) v.findViewById(R.id.device_name);
				TextView bottom = (TextView) v
						.findViewById(R.id.device_details);
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

	@Override
	public void receiveNetworkMessage(Serializable message)
	{
        // TODO reimplement
        System.out.println(message.toString());
	}

	@Override
	public void addNewConnection(NetworkConnection connection)
	{
		connections.add(connection);
        if(Util.isHost)
        {
            System.out.println(unsocketedConnections--);
            if(unsocketedConnections <= 0)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        beginGameButton.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
        else
        {
            HashMap<String, String> message = new HashMap<>();
            message.put("test", "test");
            broadcastMessage(message);
        }
	}

    // TODO reimplement [complete]
    public void broadcastMessage(Serializable message)
    {
        for(NetworkConnection connection : connections)
        {
            connection.write(message);
        }
    }
}
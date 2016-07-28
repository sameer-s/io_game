package io.github.sunsetsucks.iogame.network;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by Sameer on 2016-07-27.
 * 
 */
public class GameServer implements NsdManager.RegistrationListener
{
	private GameConnection connection;
	private ServerSocket socket;

	public GameServer(GameConnection connection)
	{
		this.connection = connection;
	}

	public void registerService(Context c)
	{
        NsdManager nsdManager = (NsdManager) c.getSystemService(Context.NSD_SERVICE);

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
		serviceInfo.setServiceName("IOGame");
		serviceInfo.setServiceType("_http._tcp.");
		serviceInfo.setPort(0);


        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, this);

		try
		{
			socket = new ServerSocket(0);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo,
			int errorCode)
	{

	}

	@Override
	public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo,
			int errorCode)
	{

	}

	@Override
	public void onServiceRegistered(NsdServiceInfo nsdServiceInfo)
	{

	}

	@Override
	public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo)
	{

	}
}

package io.github.sunsetsucks.iogame.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import io.github.sunsetsucks.iogame.Util;

/**
 * Created by Sameer on 2016-08-02.
 */
public class ServerListeningThread extends Thread
{
	private ServerSocket serverSocket;

	private NetworkHandler handler;

	public ServerListeningThread setNetworkHandler(NetworkHandler handler)
	{
		this.handler = handler;
		return this;
	}

	@Override
	public void run()
	{
		try
		{
			_run();
		}
		catch (IOException e)
		{
			Util.toast("An error occured in setting up connections");
		}
	}

	private void _run() throws IOException
	{
		serverSocket = new ServerSocket(Util.PORT);
		while (true)
		{
			try
			{
				Socket socket = serverSocket.accept();
                handler.addNewConnection(new NetworkConnection(socket, handler));
			}
			catch (SocketException e)
			{
				break;
			}
		}
	}

	public void close() throws IOException
	{
        if(serverSocket != null && !serverSocket.isClosed())
            serverSocket.close();
	}
}

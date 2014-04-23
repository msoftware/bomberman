package com.cmov.bomberman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;

public class BombermanClient extends AsyncTask<String, Void, Integer> {
	private Socket client;
	private boolean isConnected;
	private PrintWriter printwriter;
	private BufferedReader in;
	
	String hostName;
	int portNumber;
	String msgType;
	
	public BombermanClient(String hostName, int portNumber, String msgType)
	{
		//initialize clients constants
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.msgType = msgType;
	}

	@Override
	protected Integer doInBackground(String... strings) {
		// TODO Auto-generated method stub
		// validate input parameters
		if (strings.length <= 0) {
			return 0;
		}
		// connect to the server and process incoming messages
		try {
			client = new Socket(hostName, portNumber);
			printwriter = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(
	                new InputStreamReader(client.getInputStream()));
			if(msgType.equals("J"))
				sendJoinMsg(strings[0]);	
			isConnected = true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println("Don't know about host " + hostName);
            System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't get I/O for the connection to " +
	                hostName);
	            System.exit(1);
		}
		return 0;
	}

	private void sendJoinMsg(String userName) {
		// TODO Auto-generated method stub
		String msg = constructLoginMsg(userName);
		printwriter.write(msg);
		printwriter.flush();
		printwriter.close();
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	protected void onPostExecute(Long result) {
		return;
	}
	
	protected String constructLoginMsg(String userName)
	{
		String testMsg = "<1=J|2=" + userName + '>';
		//String loginMsg = '<' + BombermanProtocol.MESSAGE_TYPE + '=' + BombermanProtocol.JOIN_MESSAGE + BombermanProtocol.USER_NAME +
		//		 '=' + userName + '>';
		return testMsg;
	}
}
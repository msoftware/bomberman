package com.cmov.bomberman.bombermanclient;

import android.annotation.SuppressLint;
import android.graphics.Bitmap.Config;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RspHandler {
	private String IncomingBuffer = "";
	private static DrawView bomberManView = null;
	public static int playerid = 0;
	private static TextView bombermanScoreView=null;
	public synchronized boolean handleResponse(byte[] rsp) {
		this.IncomingBuffer += new String(rsp);
		this.notify();
		return true;
	}

	public class Cor {
		public Cor(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int x = -1;
		public int y = -1;
	}
	
	public class CorString {
		public CorString(String name, int score) {
			this.name = name;
			this.score = score;
		}

		public String name = "";
		public int score = -1;
	}

	public static void setBombermanview(DrawView bomberManGameView) {
		bomberManView = bomberManGameView;
	}

	public static void setBombermanScoreview(TextView bombermantextview)
	{
		bombermanScoreView = bombermantextview;
	}
	/*
	 * This method will be used to break the string and return easy format .
	 * example input - 1,7.3,10.5,13.9,10 output - 0 - Cor object which contains
	 * 1,7 1 - Cor object which contains 3,10 So caller function can just
	 * iterate and use
	 */
	@SuppressLint("UseSparseArrays")
	public Map<Integer, Cor> breakTheCordinateInToEasyFormat(String smsg) {
		String[] corsplitted = smsg.split("\\."); //

		Map<Integer, Cor> breakCor = new HashMap<Integer, Cor>();
		for (int i = 0; i < corsplitted.length; ++i) {
			String[] furthercorsplitted = corsplitted[i].split(",");
			int x = Integer.parseInt(furthercorsplitted[0]);
			int y = Integer.parseInt(furthercorsplitted[1]);
			breakCor.put(i, new Cor(x, y));

		}
		return breakCor;
	}
	
	@SuppressLint("UseSparseArrays")
	public Map<Integer, CorString> breakTheCordinateInToEasyFormatStrings(String smsg) {
		String[] corsplitted = smsg.split("\\."); //

		Map<Integer, CorString> breakCorString = new HashMap<Integer, CorString>();
		for (int i = 0; i < corsplitted.length; ++i) {
			String[] furthercorsplitted = corsplitted[i].split(",");
			String name = furthercorsplitted[0];
			int score = Integer.parseInt(furthercorsplitted[1]);
			breakCorString.put(i, new CorString(name, score));

		}
		return breakCorString;
	}

	/*
	 * This method will be used to process individual message . example input -
	 * <1=U|2=Group2><1=R|5=1,7.3,10.5,13.9,10|6=2,7.4,10.6,13.10,10> output - 0
	 * , in the map key =1 value =u , key =2 , value =u .... So caller function
	 * can just iterate and use
	 */
	@SuppressLint("UseSparseArrays")
	private List<Map<Integer, String>> processIndividualMessage(String message) {
		// <message1><message2>... // this is out message structure
		List<Map<Integer, String>> processedtokens = new ArrayList<Map<Integer, String>>();
		String[] splittedmessage = message.split(">");
		int numberofmessages = splittedmessage.length;
		for (int i = 0; i < numberofmessages; ++i) {
			Map<Integer, String> processedTok = new HashMap<Integer, String>();
			String processingmessage = splittedmessage[i].substring(1);
			String[] splittedprocessngmessage = processingmessage.split("\\|");
			for (int j = 0; j < splittedprocessngmessage.length; ++j) {
				String[] individualTag = splittedprocessngmessage[j].split("=");
				processedTok.put(Integer.parseInt(individualTag[0]),
						individualTag[1]);
			}
			processedtokens.add(processedTok);
		}

		return processedtokens;

	}

	private void processGridMessage(Map<Integer, String> fieldmap) {
		int row = Integer.parseInt(fieldmap.get(BombermanProtocol.GRID_ROW));
		int col = Integer.parseInt(fieldmap.get(BombermanProtocol.GRID_COLUMN));
		int gameduration = Integer.parseInt(fieldmap.get(BombermanProtocol.GAME_DURATION));
		String gridelements = fieldmap.get(BombermanProtocol.GRID_ELEMENTS);
		playerid = Integer.parseInt(fieldmap.get(BombermanProtocol.PLAYER_ID));
		System.out.println("[INFO] -GridMsg - Row-" + row + "|Col-" + col
				+ "|GridElements-" + gridelements + "|playerid -" + playerid);
		ClientConfigReader.InitializeTheGridFromServer(gameduration,row, col,
				gridelements.getBytes());

	}

	private void processRobotPlacementMessage(Map<Integer, String> fieldmap) {
		Map<Integer, Cor> processedNewRbtCor = breakTheCordinateInToEasyFormat(fieldmap
				.get(BombermanProtocol.ROBOT_NEW_PLACE));
		for (Map.Entry<Integer, Cor> entry : processedNewRbtCor.entrySet()) {
			ClientConfigReader.UpdateGridLayOutCell(entry.getValue().x,
					entry.getValue().y, (byte) 'r');
			System.out.println("[INFO] - RobotMsg NewPos - x-"
					+ entry.getValue().x + "|y-" + entry.getValue().y);
		}
		Map<Integer, Cor> processedOldRbtCor = breakTheCordinateInToEasyFormat(fieldmap
				.get(BombermanProtocol.ROBOT_ORIGINAL_PLACE));
		for (Map.Entry<Integer, Cor> entry : processedOldRbtCor.entrySet()) {
			ClientConfigReader.UpdateGridLayOutCell(entry.getValue().x,
					entry.getValue().y, (byte) '-');
			System.out.println("[INFO] - RobotMsg OldPos - x-"
					+ entry.getValue().x + "|y-" + entry.getValue().y);
		}

		if (bomberManView != null)
			bomberManView.postInvalidate();

	}

	private void processPlayerMovementMessage(Map<Integer, String> fieldmap) {
		String playeroldpos = fieldmap.get(BombermanProtocol.PLAYER_OLD_POS);
		String playernewpos = fieldmap.get(BombermanProtocol.PLAYER_NEW_POS);
		String oldgridelement = fieldmap
				.get(BombermanProtocol.OLD_ELEMENT_TYPE);
		String newgridelement = fieldmap
				.get(BombermanProtocol.NEW_ELEMENT_TYPE);
		System.out.println("[Client] player old pos - " + playeroldpos
				+ "|new pos -" + playernewpos + "|oldgridel -" + oldgridelement
				+ "|newgridelment - " + newgridelement);
		Map<Integer, Cor> breakOldCor = breakTheCordinateInToEasyFormat(playeroldpos);
		if ((byte) Integer.parseInt(oldgridelement) == 'b') {
			ClientConfigReader.UpdateGridLayOutCell(breakOldCor.get(0).x,
					breakOldCor.get(0).y, (byte) 'b');
		} else {
			ClientConfigReader.UpdateGridLayOutCell(breakOldCor.get(0).x,
					breakOldCor.get(0).y, (byte) '-');
		}
		Map<Integer, Cor> breaknewCor = breakTheCordinateInToEasyFormat(playernewpos);
		ClientConfigReader.UpdateGridLayOutCell(breaknewCor.get(0).x,
				breaknewCor.get(0).y, (byte) '1');
		if (bomberManView != null)
			bomberManView.postInvalidate();

	}

	private void processBombPlacementessage(Map<Integer, String> fieldmap) {
		// server sending which element type , but , since client already knew
		// which is the type we can update without being
		// extracted that type, better approch is do the extraction.!!!
		String bombcor = fieldmap.get(BombermanProtocol.BOMB_PLACEMENT);
		Map<Integer, Cor> breakbombCor = breakTheCordinateInToEasyFormat(bombcor);
		ClientConfigReader.UpdateGridLayOutCell(breakbombCor.get(0).x,
				breakbombCor.get(0).y, (byte) 'x');
		bomberManView.postInvalidate();
	}

	private void processNewPlayerJoinMessage(Map<Integer, String> fieldmap) {
		String newplayercor = fieldmap
				.get(BombermanProtocol.NEW_PLAYER_JOIN_COR);
		int playerid = Integer.parseInt(fieldmap
				.get(BombermanProtocol.PLAYER_ID));
		Map<Integer, Cor> breaknewplayerCor = breakTheCordinateInToEasyFormat(newplayercor);
		ClientConfigReader.UpdateGridLayOutCell(breaknewplayerCor.get(0).x,
				breaknewplayerCor.get(0).y, (byte) '1');
		// this is actually part of the wifi direct handover protocol.
		com.cmov.bomberman.wifidirect.ConfigReader.playeridlist.add(playerid);
		if (bomberManView != null)
			bomberManView.postInvalidate();

	}

	public synchronized void waitForResponse() {
		while (this.IncomingBuffer.length() == 0) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
		int posoflastindex = 0;
		String processmessage = "";
		processmessage = IncomingBuffer;
		posoflastindex = this.IncomingBuffer.lastIndexOf('>');
		if (posoflastindex != 0) {
			// // why we put plus one here is , remove whole string other wise
			// this last character will remain in the string.
			IncomingBuffer = IncomingBuffer.substring(posoflastindex + 1);
			processmessage = processmessage.substring(0, posoflastindex + 1);
		} else {
			System.out
					.println("incomming buffer didnt have the expected > end charcter");
		}

		if (!processmessage.isEmpty()) {

			List<Map<Integer, String>> processedmsg = processIndividualMessage(processmessage);
			for (int i = 0; i < processedmsg.size(); ++i) {
				Map<Integer, String> fieldmap = processedmsg.get(i);
				byte msgType = (byte) Integer.parseInt(fieldmap
						.get(BombermanProtocol.MESSAGE_TYPE));
				if (msgType == BombermanProtocol.GRID_MESSAGE) {
					processGridMessage(fieldmap);
				} else if (msgType == BombermanProtocol.ROBOT_PLACEMET_MESSAGE) {
					processRobotPlacementMessage(fieldmap);
				} else if (msgType == BombermanProtocol.PLAYER_MOVEMENT_MESSAGE) {
					processPlayerMovementMessage(fieldmap);
				} else if (msgType == BombermanProtocol.BOMP_PLACEMET_MESSAGE) {
					processBombPlacementessage(fieldmap);
				} else if (msgType == BombermanProtocol.NEW_PLAYER_JOIN) {
					processNewPlayerJoinMessage(fieldmap);
				} else if (msgType == BombermanProtocol.BOMB_EXPLOSION_MESSAGE) {
					processBombExplosionMessage(fieldmap);
				} else if (msgType == BombermanProtocol.GAME_END_MESSAGE) {
					processGameFinishMessage(fieldmap);
				}else if (msgType == BombermanProtocol.GAME_QUIT_MESSAGE){
					processPlayerQuitMessage(fieldmap);
				}else if (msgType == BombermanProtocol.INDIVIDUAL_SCORE_UPDATE){
					processIndividualScoreupdate(fieldmap);
				}else if (msgType == BombermanProtocol.NUMBER_OF_PLAYERS_MESSAGE){
					processNoOfPlayerMessage(fieldmap);
				}
				else if(msgType == BombermanProtocol.GAME_PAUSE_MESSAGE)
				{
					processPlayerPauseMessage(fieldmap);
				}
				else if(msgType == BombermanProtocol.GAME_RESUME_MESSAGE)
				{
					processPlayerResumeMessage(fieldmap);
				}
				else if(msgType == BombermanProtocol.DEAD_PLAYER_MESSAGE)
				{
					processDeadPlayerMessage(fieldmap);
				}
			}

		}
	}

	private void processDeadPlayerMessage(Map<Integer, String> fieldmap) {
		int deadplayerid = Integer.parseInt(fieldmap.get(BombermanProtocol.RESUME_PLAYER_POS));
		com.cmov.bomberman.wifidirect.ConfigReader.deadplayerlist.add(deadplayerid);
	}

	private void processPlayerResumeMessage(Map<Integer, String> fieldmap) {
		// TODO Auto-generated method stub
		String resumeCormsg = fieldmap.get(BombermanProtocol.RESUME_PLAYER_POS);
		Map<Integer, Cor> resumeCordinates = breakTheCordinateInToEasyFormat(resumeCormsg);
		ClientConfigReader.UpdateGridLayOutCell(resumeCordinates.get(0).x, resumeCordinates.get(0).y, (byte) '1');
		if(bomberManView != null)
			bomberManView.postInvalidate();
	}

	private void processPlayerPauseMessage(Map<Integer, String> fieldmap) {
		// TODO server has sent player pause reply!
		String pauseCormsg = fieldmap.get(BombermanProtocol.PAUSE_PLAYER_POS);
		Map<Integer, Cor> pauseCordinates = breakTheCordinateInToEasyFormat(pauseCormsg);
		ClientConfigReader.UpdateGridLayOutCell(pauseCordinates.get(0).x, pauseCordinates.get(0).y, (byte) 'w');
		if(bomberManView != null)
			bomberManView.postInvalidate();
	}

	private void processNoOfPlayerMessage(Map<Integer, String> fieldmap) {
		int noOfPlayers = Integer.parseInt(fieldmap.get(BombermanProtocol.NUMBER_OF_PLAYERS));
		MainActivity.setNumberofPlayer(noOfPlayers);
	}

	private void processIndividualScoreupdate(Map<Integer,String> fieldmap)
	{
		int totalscore = Integer.parseInt(fieldmap.get(BombermanProtocol.SCORE));
		MainActivity.setScoreofThePlayer(totalscore);
	}
	
	private void processPlayerQuitMessage(Map<Integer, String> fieldmap) {
		// TODO server has sent player quit reply!
		String playerCorMsg = fieldmap.get(BombermanProtocol.QUIT_PLAYER_POS);
		Map<Integer, Cor> playerCordinates = breakTheCordinateInToEasyFormat(playerCorMsg);
		ClientConfigReader.UpdateGridLayOutCell(playerCordinates.get(0).x,
					playerCordinates.get(0).y, (byte) '-');
		if (bomberManView != null)
			bomberManView.postInvalidate();
	}

	private void processGameFinishMessage(Map<Integer, String> fieldmap) {
		// sever has sent game finish message , stop the game and show the stat
		// to user
		String stats = "";
		String gameFinishMsg = fieldmap.get(BombermanProtocol.GAME_STAT);
		Map<Integer, CorString> gameStats = breakTheCordinateInToEasyFormatStrings(gameFinishMsg);
		for(Map.Entry<Integer, CorString> entry : gameStats.entrySet()) {
			stats += "Player Name: " + entry.getValue().name + " Score: " + entry.getValue().score + "\n";
		}
		
		BombermanClient.Mainactivity.SetGameStat(stats);
	}

	private void processBombExplosionMessage(Map<Integer, String> fieldmap) {
		// sever has sent affected cor, so update them in the view.
		String bombExplosionCor = fieldmap
				.get(BombermanProtocol.BOMB_EXPLOSION_COR);
		Map<Integer, Cor> bombExplosionCordinates = breakTheCordinateInToEasyFormat(bombExplosionCor);
		for (Map.Entry<Integer, Cor> entry : bombExplosionCordinates.entrySet()) {
			ClientConfigReader.UpdateGridLayOutCell(entry.getValue().x,
					entry.getValue().y, (byte) 'e');
		}
		if (bomberManView != null)
			bomberManView.postInvalidate(); // this is invalidate with explosion
		if (bomberManView != null)
			bomberManView.postInvalidate(); // after the drawing the this will be caled to invalidate -
	}
}
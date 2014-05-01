package com.cmov.bomberman.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Game {
	private String gameName = null;
	private Map<Integer, Player> playersmap = new HashMap<Integer,Player>();
	private LogicalWorld logicalWorld;
	private boolean running = false;

	public Game(String name) {
		this.gameName = name;
		this.logicalWorld = new LogicalWorld();
	}

	public int addPlayer(Player player) {
		
		/*
		 * Note : here we are using player id as map size +1, we are adding one becuase player id in the configuration
		 * is starting from 1, in order to make uniform, this approch will be nice.
		 * */
		Random randomGenerator = new Random();
		// random id will be generated within this range, hope this range would be enough
		int randomInt = randomGenerator.nextInt(10000);
		player.setID(randomInt);	
		// get the modulo here, this is kind of trick :)
		int pos  = playersmap.size() % 3;
		// Adds player to playground view, set starting position
		int x = ConfigReader.playermap.get(pos).getXCor();
		int y = ConfigReader.playermap.get(pos).getYCor();
		System.out.println("[Server] Player is going  to insert in to map x " + x + "|"
				+ "y " + y);

		player.setPosition(x, y);
		this.logicalWorld.setElement(x, y, player.getID(), player);
		

		//players.add(player);
		player.setGame(this);
		playersmap.put(randomInt, player);
		System.out.println("[Server] Player" + player.getID()
				+ "added to logical world (" + player.getNickname() + ")");
		return randomInt;
	}

	public int getPlayerCount() {
		return this.playersmap.size();
	}

	public LogicalWorld getLogicalWorld() {
		return this.logicalWorld;
	}

	public Player getPlayer(int playerid) {
		return this.playersmap.get(playerid);
	}

	public void removePlayer(int x, int y, Player player) {
		if (this.logicalWorld.getElement(x, y).equals(player)) // Removes only
																// the selected
																// player
			this.logicalWorld.setElement(x, y, player.getID(), null);
	}

	public boolean movePlayer(Player player, int dx, int dy) {
		if (dx == 0 && dy == 0)
			return true;

		// Check if we can move in that direction
		int nx = player.getWorldXCor() + dx;
		int ny = player.getWorldYCor() + dy;

		if (nx < 0 || ny < 0 || this.logicalWorld.getWidth() <= ny
				|| this.logicalWorld.getHeight() <= nx)
			return false;
		// player can not move if there is wall or obstacle , have to add here.
		Cell el = this.logicalWorld.getElement(nx, ny)[0];
		if (el == null) // oder Extra
		{
			// Set old position in logical world to null...
			if (player.getBombCounter() !=0) // player has bomb
			{
				if (ConfigReader.gridlayout[player.getWorldXCor()][player
						.getWorldYCor()] == 'x') // bomb is there
				{

					ConfigReader.UpdateGridLayOutCell(player.getWorldXCor(),player.getWorldYCor(), (byte) 'b');
					
				} else {
					ConfigReader.UpdateGridLayOutCell(player.getWorldXCor(),
							player.getWorldYCor(), (byte) '-');
					this.logicalWorld.setElement(player.getWorldXCor(),
							player.getWorldYCor(), player.getID(), null);
				}
			} else {
				ConfigReader.UpdateGridLayOutCell(player.getWorldXCor(),
						player.getWorldYCor(), (byte) '-');
				this.logicalWorld.setElement(player.getWorldXCor(),
						player.getWorldYCor(), player.getID(), null);
			}
			
			// ...and set new position
			player.move(dx, dy);
			this.logicalWorld.setElement(player.getWorldXCor(),
					player.getWorldYCor(), player.getID(), player);
			ConfigReader.UpdateGridLayOutCell(player.getWorldXCor(),
					player.getWorldYCor(), (byte) '1');

			return true;
		} else
			return false;
	}

	@Override
	public String toString() {
		return this.gameName;
	}

	public void setPlayground(LogicalWorld logicalworld) {
		this.logicalWorld = logicalworld;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}

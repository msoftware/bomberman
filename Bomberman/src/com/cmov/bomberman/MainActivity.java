package com.cmov.bomberman;

import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements IExplodable,
		IMoveableRobot {
	private DrawView bomberManView;
	final StandaloneGame standAloneGame = new StandaloneGame();
	private static Bitmap player = null;

	private LogicalWorld logicalworld = null;

	public void Render() {
		RectRender rectrender = new RectRender(ConfigReader.getGameDim().row,
				ConfigReader.getGameDim().column);
		player = BitmapFactory.decodeResource(getResources(), R.drawable.sri);
		rectrender.setPlayerBitMap(player);
		bomberManView.setRenderer(rectrender);
		bomberManView.invalidate();

	}

	public void RobotMovedAtLogicalLayer() {
		RectRender rectrender = new RectRender(ConfigReader.getGameDim().row,
				ConfigReader.getGameDim().column);
		player = BitmapFactory.decodeResource(getResources(), R.drawable.sri);
		rectrender.setPlayerBitMap(player);
		bomberManView.setRenderer(rectrender);
		bomberManView.postInvalidate();
	}

	public void Exploaded(int row, int col) {
		RectRender rectrender = new RectRender(ConfigReader.getGameDim().row,
				ConfigReader.getGameDim().column);
		player = BitmapFactory.decodeResource(getResources(), R.drawable.sri);
		rectrender.setPlayerBitMap(player);
		bomberManView.setRenderer(rectrender);
		bomberManView.postInvalidate();

	}

	private void InitBomberManMap() {
		player = BitmapFactory.decodeResource(getResources(), R.drawable.sri);
		RectRender rectrender = new RectRender(ConfigReader.getGameDim().row,
				ConfigReader.getGameDim().column);
		rectrender.setPlayerBitMap(player);
		bomberManView.setRenderer(rectrender);
		bomberManView.invalidate();

	}

	private void InitStartRobotThred(Activity activity) {
		RobotThread robotThread = new RobotThread(
				ConfigReader.getGameDim().row,
				ConfigReader.getGameDim().column, activity);
		robotThread.setLogicalWord(logicalworld);
		robotThread.setRunning(true);
		robotThread.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context mContext = MainActivity.this;
		try {
			ConfigReader.InitConfigParser(mContext);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		setContentView(R.layout.activity_main);

		standAloneGame.joinGame("Cmove", MainActivity.this);
		logicalworld = standAloneGame.getBombermanGame().getLogicalWorld();

		bomberManView = (com.cmov.bomberman.DrawView) findViewById(R.id.bckg);

		InitBomberManMap();

		InitStartRobotThred(MainActivity.this);

		final Button button = (Button) findViewById(R.id.btnBomb);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (standAloneGame.getBombermanGame().getPlayers().size() == 0) {
					ConfigReader.getLogger().log(
							"Player list is null. Warning.");
				} else {
					List<Player> localPlayerList = standAloneGame
							.getBombermanGame().getPlayers();
					localPlayerList.get(0).placeBomb();
					Render();

				}

			}
		});

		final Button leftbutton = (Button) findViewById(R.id.btnLeft);
		leftbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (standAloneGame.getBombermanGame().getPlayers().size() == 0) {
					ConfigReader.getLogger().log(
							"Player list is null. Warning.");
				} else {
					List<Player> localPlayerList = standAloneGame
							.getBombermanGame().getPlayers();
					boolean ismoved = standAloneGame.getBombermanGame()
							.movePlayer(localPlayerList.get(0), 0, -1);
					if (ismoved)
						ConfigReader.getLogger().log(
								"player has been moved.....");

					Render();
				}

			}
		});

		final Button rightbutton = (Button) findViewById(R.id.btnRight);
		rightbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (standAloneGame.getBombermanGame().getPlayers().size() == 0) {
					ConfigReader.getLogger().log(
							"Player list is null. Warning.");
				} else {
					List<Player> localPlayerList = standAloneGame
							.getBombermanGame().getPlayers();
					boolean ismoved = standAloneGame.getBombermanGame()
							.movePlayer(localPlayerList.get(0), 0, 1);
					if (ismoved)
						ConfigReader.getLogger().log(
								"player has been moved.....");

					Render();
				}

			}
		});

		final Button upbutton = (Button) findViewById(R.id.btnUp);
		upbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (standAloneGame.getBombermanGame().getPlayers().size() == 0) {
					ConfigReader.getLogger().log(
							"Player list is null. Warning.");
				} else {
					List<Player> localPlayerList = standAloneGame
							.getBombermanGame().getPlayers();
					boolean ismoved = standAloneGame.getBombermanGame()
							.movePlayer(localPlayerList.get(0), -1, 0);
					if (ismoved)
						ConfigReader.getLogger().log(
								"player has been moved.....");

					Render();
				}

			}
		});

		final Button downbutton = (Button) findViewById(R.id.btnDown);
		downbutton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (standAloneGame.getBombermanGame().getPlayers().size() == 0) {
					ConfigReader.getLogger().log(
							"Player list is null. Warning.");
				} else {
					List<Player> localPlayerList = standAloneGame
							.getBombermanGame().getPlayers();
					boolean ismoved = standAloneGame.getBombermanGame()
							.movePlayer(localPlayerList.get(0), 1, 0);
					if (ismoved)
						ConfigReader.getLogger().log(
								"player has been moved.....");

					Render();
				}

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
package com.gesturedefence.util;

/**
 * @author Michael Watts
 * @since 22:28:21 - 16 Jun 2011
 */

import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.util.HorizontalAlign;

import com.gesturedefence.GestureDefence;

public class ScreenManager {
	
	private GestureDefence base;
	public Scene MainMenu;
	public Scene GameScreen;
	public Scene NewWaveScene;
	public Scene EndWaveScene;
	public Scene GameOverScene;
	
	private ChangeableText scorebits;
	
	public ScreenManager(GestureDefence base)
	{
		this.base = base;
	}
	
	public void loadMainMenu()
	{
		if(MainMenu == null)
		{
			MainMenu = new Scene(1);
		
			/* Setup the scrolling background, can be removed, was just trying it out */
			
			MainMenu.setBackground(base.autoParallaxBackground);
			/* End of scrolling Background */
			
			int buttonX = (base.getCameraWidth() / 2) - (base.getStartButtonRegion().getWidth() / 2);
			int buttonY = (base.getCameraHeight() / 2)  - (base.getStartButtonRegion().getHeight() / 2);
			
			Sprite startButton = new Sprite(buttonX, buttonY, base.getStartButtonRegion()) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.ButtonPress(1);
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			buttonX = (base.getCameraWidth() / 2) - (base.getQuitButtonRegion().getWidth() / 2);
			buttonY += base.getStartButtonRegion().getHeight();
			
			Sprite quitButton = new Sprite(buttonX, buttonY, base.getQuitButtonRegion()) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.ButtonPress(99);
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			MainMenu.attachChild(startButton);
			MainMenu.attachChild(quitButton);
			MainMenu.registerTouchArea(startButton);
			MainMenu.registerTouchArea(quitButton);
			MainMenu.setTouchAreaBindingEnabled(true);
		}
		
		base.getEngine().setScene(MainMenu);
	}
	
	public void GameScreen()
	{
		if (GameScreen == null)
		{
			GameScreen = new Scene(1);
		
			GameScreen.setBackground(base.autoParallaxBackground);
			GameScreen.registerUpdateHandler(base.sRemoveStuff);
			
			base.loadCastle(base.getCameraWidth() - (base.getCastleTexture().getWidth()), base.getCameraHeight() - 60 - base.getCastleTexture().getHeight());
			base.loadHud();
			
			base.sm.GameScreen.registerUpdateHandler(new IUpdateHandler() {
				@Override
				public void onUpdate(float pSecondsElapsed) {
					/* On every update */
					if (base.sPreviousWaveNum != base.theWave.getWaveNumber() && base.sKillCount != base.sPreviousKillCount)
						if ((base.sKillCount - base.sPreviousKillCount) == base.theWave.getNumberEnemysToSpawn())
						{
							/* Oh they all dead */
							base.theWave.mCashAmountItem.setText("CASH : " + base.sMoney);
							base.theWave.mBuyMenuItem.setText("HEALTH : " + base.sCastle.getHealth());
							//GestureDefence.sMainScreen.setChildScene(mWaveComplete, false, true, true);
							base.sm.EndWaveScreen();
							base.sEndWaveActive = true;
							base.sPreviousWaveNum = base.theWave.getWaveNumber();
							base.sPreviousKillCount += base.theWave.getNumberEnemysToSpawn();
						}
				}

				@Override
				public void reset() {
					// TODO Auto-generated method stub
					
				}
			});
		}
		
		base.getEngine().setScene(GameScreen);
	}
	
	public void NewWaveScreen()
	{
		if (NewWaveScene == null)
		{
			NewWaveScene = new Scene(1);
			
			base.theWave.mWaveNumberMenuItem = new ChangeableTextMenuItem(GestureDefence.MENU_WAVE_NUMBER, base.mFont, "WAVE : " + base.theWave.getWaveNumber(), ("WAVE : " + base.theWave.getWaveNumber()).length());
			NewWaveScene.attachChild(base.theWave.mWaveNumberMenuItem);
		}		
		
		base.theWave.mWaveNumberMenuItem.setPosition((base.getCameraWidth() / 2) - (base.theWave.mWaveNumberMenuItem.getWidth() / 2), (base.getCameraHeight() / 2) - (base.theWave.mWaveNumberMenuItem.getHeight() / 2));
		base.getEngine().setScene(NewWaveScene);
	}
	
	public void EndWaveScreen()
	{
		if (EndWaveScene == null)
		{
			EndWaveScene = new Scene(1);
			
			EndWaveScene.setBackground(new ColorBackground(0.0f, 0.0f, 1.0f));
			
			Sprite buyButton = new Sprite(base.getCameraWidth() - base.getCameraWidth(), base.getCameraHeight() - base.getBuyButton().getHeight() , base.getBuyButtonRegion()) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.ButtonPress(5);
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			Sprite NextWaveButton = new Sprite(base.getCameraWidth() - base.getNextWaveButton().getWidth(), base.getCameraHeight() - base.getNextWaveButton().getHeight(), base.getNextWaveButtonRegion()) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.ButtonPress(3);
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			EndWaveScene.attachChild(base.theWave.mCashAmountItem);
			EndWaveScene.attachChild(base.theWave.mBuyMenuItem);
			EndWaveScene.attachChild(buyButton);
			EndWaveScene.registerTouchArea(buyButton);
			EndWaveScene.attachChild(NextWaveButton);
			EndWaveScene.registerTouchArea(NextWaveButton);
		}
		
		base.theWave.mCashAmountItem.setPosition(100, 100);
		base.theWave.mBuyMenuItem.setPosition(300, 200);
		base.getEngine().setScene(EndWaveScene);
	}
	
	public void GameOverScreen()
	{
		if (GameOverScene == null)
		{
			GameOverScene = new Scene(1);
			
			Text gameOverText = new Text(base.getCameraWidth() / 2, base.getCameraHeight() / 2, base.mFont, "GAME OVER!", HorizontalAlign.CENTER) {
				@Override
				public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
					base.getEngine().setScene(MainMenu);
					base.theWave.setWaveNumber(1);
					base.sKillCount = 0;
					base.sPreviousKillCount = 0;
					base.sPreviousWaveNum = 0;
					base.sMoney = 0;
					base.sEnemyCount = 0;
					base.updateCashValue();
					base.sCastle.increaseHealth(3000);
					base.updateCastleHealth();
					
					/*remove all sprite's still in the game (enemies etc)
					 * This needs optimising, like making it only remove enemies!
					 */
					base.sm.GameScreen.detachChildren();
					
					//Reload the castle, since it has now been removed
					base.loadCastle(base.getCameraWidth() - (base.getCastleTexture().getWidth()), base.getCameraHeight() - 60 - base.getCastleTexture().getHeight());
					
					return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
				}
			};
			
			scorebits = new ChangeableText(gameOverText.getX() - gameOverText.getWidth(), gameOverText.getY() + gameOverText.getHeight(), base.mFont, "Kills = " + base.sKillCount + ", cash = " + base.sMoney);
			GameOverScene.attachChild(gameOverText);
			GameOverScene.attachChild(scorebits);
			GameOverScene.registerTouchArea(gameOverText);
		}		
		
		scorebits.setText("Kills = " + base.sKillCount + ", cash = " + base.sMoney); 
		base.getEngine().setScene(GameOverScene);
	}
}
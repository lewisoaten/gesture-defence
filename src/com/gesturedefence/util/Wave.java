package com.gesturedefence.util;

/**
 * @author Michael Watts
 * @since 18:23:53 - 15 Jun 2011
 */

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.util.MathUtils;

import com.gesturedefence.GestureDefence;

public class Wave {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private GestureDefence base;
		private static int sWaveNumber = 1; //The wave number we are on
		private static int sNumberEnemysToSpawn = 0;
		
		public ChangeableTextMenuItem mCashAmountItem;
		public ChangeableTextMenuItem mBuyMenuItem;
		public ChangeableTextMenuItem mWaveNumberMenuItem;
	
	// ========================================
	// Constructors
	// ========================================
		
		public Wave(GestureDefence base)
		{
			Wave.sWaveNumber = 1;
			this.base = base;
			this.mCashAmountItem = new ChangeableTextMenuItem(base.MENU_CASH, base.mFont, "CASH : XXXXXXX", "CASH : XXXXXXX".length());
			this.mBuyMenuItem = new ChangeableTextMenuItem(base.MENU_HEALTH, base.mFont, "Health : XXXXXX", "Health : XXXXXX".length());
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public int getWaveNumber()
		{
			return Wave.sWaveNumber;
		}
		
		public void NextWave()
		{
			Wave.sWaveNumber++;
		}
		
		public int getNumberEnemysToSpawn()
		{
			return Wave.sNumberEnemysToSpawn;
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	// ========================================
	// Methods
	// ========================================
		
//		public MenuScene createStartWaveScreen(Font theFont)
//		{
//			/* Passing in the font fixes buggy text problems */
//			MenuScene theScene = new MenuScene(GestureDefence.sCamera);
//			
//			mWaveNumberMenuItem = new ChangeableTextMenuItem(GestureDefence.MENU_WAVE_NUMBER, theFont, "WAVE : X", "WAVE : X".length());
//			theScene.addMenuItem(mWaveNumberMenuItem);
//
//			theScene.buildAnimations();
//			theScene.setBackgroundEnabled(false);
//			
//			return theScene;
//		}
//		
//		public MenuScene createEndWaveScreen(Font theFont)
//		{
//			/* Passing in the font fixes buggy text problems */
//			MenuScene theScene = new MenuScene(GestureDefence.sCamera);
//			
//			mCashAmountItem = new ChangeableTextMenuItem(GestureDefence.MENU_CASH, theFont, "CASH : XXXXXXX", "CASH : XXXXXXX".length());
//			theScene.addMenuItem(mCashAmountItem);
//			
//			mBuyMenuItem = new ChangeableTextMenuItem(GestureDefence.MENU_HEALTH, theFont, "Health : XXXXXX", "Health : XXXXXX".length());
//			theScene.addMenuItem(mBuyMenuItem);
//			
//			final IMenuItem buyHealth = new ColorMenuItemDecorator(new TextMenuItem(GestureDefence.MENU_BUY_HEALTH, theFont, "BUY 100 HEALTH = 100 Cash"), 1.0f,0.0f,0.0f,1.0f,1.0f,1.0f);
//			buyHealth.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//			theScene.addMenuItem(buyHealth);
//			
//			final IMenuItem startNext = new ColorMenuItemDecorator(new TextMenuItem(GestureDefence.MENU_START_NEXT_WAVE, theFont, "Start Next Wave"), 1.0f,0.0f,0.0f,1.0f,1.0f,1.0f);
//			buyHealth.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
//			theScene.addMenuItem(startNext);
//
//			theScene.buildAnimations();
//			theScene.setBackgroundEnabled(true);
//			
//			return theScene;
//		}
		
		public boolean startNewWave()
		{
			@SuppressWarnings("unused")
			TimerHandler waveSpawnTimer;
			base.sEnemyCount = 0;
			Wave.sNumberEnemysToSpawn = (Wave.sWaveNumber * 10) + (Wave.sWaveNumber * 2); //Crap difficulty formula, might want work out a more awesome one
			
			base.sm.GameScreen.registerUpdateHandler(waveSpawnTimer = new TimerHandler(1 / 10.0f, true, new ITimerCallback()
			{
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler)
				{
					/* Every 10th of a second do a random spawn check
					 * This could be changed, but for now its this */
					int randomChance = MathUtils.random(1, 10);
					int randomChance2 = MathUtils.random(1, 10);
					
					if (randomChance == randomChance2)
					{
						/* Start a spawn */
						final float xPos = -10;
						/* Allow for future adverts height */
						final float yPos = MathUtils.random(250.0f, base.getCameraHeight() - 60);
						
						base.loadNewEnemy(xPos, yPos);
					}
					if (base.sEnemyCount == Wave.sNumberEnemysToSpawn)
					{
						base.sm.GameScreen.unregisterUpdateHandler(pTimerHandler);
					}
				}
			}));
			return true;
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
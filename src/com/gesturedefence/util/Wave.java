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
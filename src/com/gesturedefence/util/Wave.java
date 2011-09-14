package com.gesturedefence.util;

/**
 * @author Michael Watts
 * @since 18:23:53 - 15 Jun 2011
 */

import java.util.Random;

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
	
		private GestureDefence base; //Instance of GestureDefence
		private static int sWaveNumber = 1; //The wave number we are on
		private static int sNumberEnemysToSpawn = 0; //Number of enemies that will spawn this wave
		private int MaxRandom = 0;
		private int MaxRandom2 = 0;
		private Random randomChance = new Random();
		private Random randomChance2 = new Random();
		private Random difficultyChance = new Random();
		
		public ChangeableTextMenuItem mCashAmountItem; //Text Item to show cash (used on end wave screen)
		public ChangeableTextMenuItem mBuyMenuItem; //Text item to show health (used on end wave screen)
		public ChangeableTextMenuItem mWaveNumberMenuItem; //Text item to show wave number (Used on newWave screen?)
		
		private static int SpawnedEnemies_Default = 0;
		private static int SpawnedEnemies_Giant = 0;
		
		final float xPos = 0 - 64; // Default Xpos for spawn
	
	// ========================================
	// Constructors
	// ========================================
		
		public Wave(GestureDefence base)
		{ //Do this when an instance of Wave is created
			Wave.sWaveNumber = 1;
			this.base = base;
			// Initialise the following, otherwise crash occurs from no instance of them exisiting
			this.mCashAmountItem = new ChangeableTextMenuItem(base.MENU_CASH, base.mFont2, "CASH : XXXXXXX", "CASH : XXXXXXX".length());
			this.mBuyMenuItem = new ChangeableTextMenuItem(base.MENU_HEALTH, base.mFont2, "Health : XXXXXX / XXXXXX", "Health : XXXXXX / XXXXXX".length());
			this.mWaveNumberMenuItem = new ChangeableTextMenuItem(GestureDefence.MENU_WAVE_NUMBER, base.mFont, "WAVE : XXXX", ("WAVE : XXXX").length());
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public int getWaveNumber()
		{ //Returns the current wave number
			return Wave.sWaveNumber;
		}
		
		public void NextWave()
		{ //Increase the wave number for next wave
			Wave.sWaveNumber++;
		}
		
		public int getNumberEnemysToSpawn()
		{ //Returns number of enemies being spawned
			return Wave.sNumberEnemysToSpawn;
		}
		
		public void setWaveNumber(int theNumber)
		{ //Sets the current wave number (used on reset and load games)
			Wave.sWaveNumber = theNumber;
			this.mWaveNumberMenuItem.setText("WAVE : " + Wave.sWaveNumber);
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	// ========================================
	// Methods
	// ========================================
		
		public boolean startNewWave()
		{
			@SuppressWarnings("unused") //Remove warnings about 'unused' variables
			TimerHandler waveSpawnTimer; //Create a timer
			base.sEnemyCount = 0; //Set enemy count 0..?
			SpawnedEnemies_Default = 0;
			SpawnedEnemies_Giant = 0;
			
			MaxRandom = 10 - Wave.sWaveNumber;
			if (MaxRandom <= 1)
				MaxRandom = 2;
			MaxRandom2 = 25 - Wave.sWaveNumber; //Too high and it sucks!
			if (MaxRandom2 <= 1)
				MaxRandom2 = 2;
			
			randomChance.nextInt(MaxRandom);
			randomChance2.nextInt(MaxRandom);
			difficultyChance.nextInt(MaxRandom2);
			
			Wave.sNumberEnemysToSpawn = Wave.sWaveNumber * 10;
			if (Wave.sWaveNumber >= 5)
				Wave.sNumberEnemysToSpawn += (Wave.sWaveNumber - 4) * 2; // Add on additional giant enemies!
			
			//Create a timer attached to the game screen for spawning
			base.sm.GameScreen.registerUpdateHandler(waveSpawnTimer = new TimerHandler(1 / 10.0f, true, new ITimerCallback()
			{ //Timer settings,
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler)
				{
					//pTimerHandler.reset();
					/* Every 10th of a second, do a random spawn check */
					if (base.mOnScreenEnemies <= base.mOnScreenEnemyLimit) //Add a fixed limit to enemies on screen, Prevent LAG!
					{
						if (randomChance.nextInt(MaxRandom) == randomChance2.nextInt(MaxRandom))
						{
							final float yPos = MathUtils.random(250.0f, base.getCameraHeight() - 60);
							
							if ( (difficultyChance.nextInt(MaxRandom2) == randomChance.nextInt(MaxRandom)) && SpawnedEnemies_Giant <= ((Wave.sWaveNumber - 4) * 2) )
							{ //Spawn harder enemy
								base.loadNewEnemy(xPos, yPos, 2);
								SpawnedEnemies_Giant++;
							}
							else if (SpawnedEnemies_Default <= (Wave.sWaveNumber * 10) )
							{ //Spawn default enemy
								base.loadNewEnemy(xPos, yPos, 1);
								SpawnedEnemies_Default++;
							}
						}
						if (base.sEnemyCount == Wave.sNumberEnemysToSpawn)
						{ //Once all the enemies have been spawned, remove the timer
							base.sm.GameScreen.unregisterUpdateHandler(pTimerHandler);
						}
					}
				}
			}));
			return true;
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
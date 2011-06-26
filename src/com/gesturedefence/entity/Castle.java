package com.gesturedefence.entity;

/**
 * @author Michael Watts
 * @since 14:20:32 - 15 Jun 2011
 */

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

import com.gesturedefence.GestureDefence;

public class Castle extends Sprite {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private static float mHealth = 3000.0f; //The amount of health the castle has to start with
		private static float mMaxHealth = 3000.0f; //The maximum amount of health the castle can have
		private static GestureDefence base; //Instance of GestureDefance, allows accessing the class from here!
	
	// ========================================
	// Constructors
	// ========================================
	
		public Castle(final float pX, final float pY, final TextureRegion pTextureRegion){
			super(pX, pY, pTextureRegion);
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public void setCastleBase(GestureDefence base)
		{ //Set the base to the GestureDefence object passed in
			Castle.base = base;
		}
		
		public int getCurrentHealth()
		{
			//Returns the current health of the castle
			return (int) Castle.mHealth;
		}
		
		public void setCurrentHealth(int amount)
		{
			//Set's the current health of the castle (used for reset's and load games)
			Castle.mHealth = amount;
		}
		
		public int getMaxHealth()
		{
			//Returns the max health the castle can have
			return (int) Castle.mMaxHealth;
		}
		
		public boolean increaseHealth(int amount)
		{
			//Increase's the current health by X amount
			if ( (Castle.mHealth + amount) <= Castle.mMaxHealth)
			{
				Castle.mHealth += amount;
			}
			else
			{
				Castle.mHealth = Castle.mMaxHealth;
			}
			return true;
		}
		
		public boolean increaseMaxHealth(int amount)
		{
			//Increase's max health by X amount
			Castle.mMaxHealth += amount;
			Castle.mHealth += amount;
			return true;
		}
		
		public void setMaxHealth(int amount)
		{
			//Set max health of castle (used in reset and load games)
			Castle.mMaxHealth = amount;
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	// ========================================
	// Methods
	// ========================================

		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
		{	//Could be used at future point (touch castle sprite)
			return true;
		}		
		
		public static boolean damageCastle(float damgeValue)
		{//Do some damage to the castle, X amount
			//Check to make sure subtracting health wont cause game over
			if (Castle.mHealth - damgeValue > 0)
			{
				Castle.mHealth -= damgeValue;
			}
			else
			{ //YOU DEAD! game over!
				Castle.mHealth = 0;
				base.sm.GameOverScreen();
			}
			return true;
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
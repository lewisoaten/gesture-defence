package com.gesturedefence.entity;

/**
 * Author: Mike Since: 14:20:32 - 15 Jun 2011
 */

import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

public class Castle extends Sprite {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private static float mHealth = 3000.0f; //The amount of health the castle has to start with
	
	// ========================================
	// Constructors
	// ========================================
	
		public Castle(final float pX, final float pY, final TextureRegion pTextureRegion){
			super(pX, pY, pTextureRegion);
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public int getHealth()
		{
			return (int) Castle.mHealth;
		}
		
		public boolean increaseHealth(int amount)
		{
			Castle.mHealth += amount;
			return true;
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	// ========================================
	// Methods
	// ========================================

		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
		{
			return true;
		}		
		
		public static boolean damageCastle(float damgeValue)
		{
			Castle.mHealth -= damgeValue;
			return true;
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
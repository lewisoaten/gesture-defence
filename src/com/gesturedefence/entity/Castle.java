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
		private static GestureDefence base;
	
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
		{
			Castle.base = base;
		}
		
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
			if (Castle.mHealth - damgeValue > 0)
			{
				Castle.mHealth -= damgeValue;
			}
			else
			{
				Castle.mHealth = 0;
				base.sm.GameOverScreen();
			}
			return true;
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
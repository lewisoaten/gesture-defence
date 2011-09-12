package com.gesturedefence.entity;

/**
 * @author Michael Watts
 * @since 12:05:40 - 15 Jun 2011
 */

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.pool.EntityDetachRunnablePoolItem;

import com.gesturedefence.GestureDefence;

public class Mana extends Sprite {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
	private GestureDefence base;
	private final PhysicsHandler mPhysicsHandler;
	
	private float mInitialY;
	private float mGravity = 9.86f; //Gravity value for velocity
	private boolean killIT = false;
	
	
	// ========================================
	// Constructors
	// ========================================
	
		public Mana(final float pX, final float pY, final TextureRegion pTextureRegion, GestureDefence base)
		{
			super(pX, pY, pTextureRegion);
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			this.base = base;
			this.mInitialY = pY;
			this.setPosition(this.mX, this.mY - 10);
			this.mPhysicsHandler.setVelocityY(-300);
			if (this.mX > (base.getCameraWidth() / 2) )
			{
				this.mPhysicsHandler.setVelocityX(-40.0f);
			}
			else if (this.mX <= (base.getCameraWidth() / 2) )
			{
				this.mPhysicsHandler.setVelocityX(40.0f);
			}
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
		
		@Override
		public void onManagedUpdate(final float pSecondsElapsed)
		{
			if (killIT)
			{
				base.sm.GameScreen.unregisterTouchArea(this);
				final EntityDetachRunnablePoolItem pPoolItem = base.sRemoveStuff.obtainPoolItem();
				pPoolItem.setEntity(this.getParent());
				base.sRemoveStuff.postPoolItem(pPoolItem);
			}
			
//			if (this.mX < 5.0f) //If it's off the screen (left)
//				this.mPhysicsHandler.setVelocityX(10.0f);
//			
//			if (this.mX > (base.getCameraWidth() - 20.0f) ) //If it's off the screen (right)
//				this.mPhysicsHandler.setVelocityX(-10.0f);
//			
//			if (this.mX > 5.0f && this.mX < (base.getCameraWidth() - 5.0f) ) //If it's NOT off the screen
//				this.mPhysicsHandler.setVelocityX(0.0f);
			
			if(this.mY < this.mInitialY)
			{
				this.mPhysicsHandler.setVelocityY(this.mPhysicsHandler.getVelocityY() + mGravity);
			}
			else
			{
				this.mPhysicsHandler.setVelocity(0.0f);
			}
			super.onManagedUpdate(pSecondsElapsed);
		}
		
		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
		{
			switch(pSceneTouchEvent.getAction())
			{
				case TouchEvent.ACTION_UP:
					if (killIT == false)
					{
						this.killIT = true;
						base.mana += 250;
						base.CustomHUD.updateManaValue();
					}
					break;
			}
			return true;
		}
	
	// ========================================
	// Methods
	// ========================================
		
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
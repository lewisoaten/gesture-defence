package com.gesturedefence.entity;

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TextureRegion;

import com.gesturedefence.GestureDefence;

/**
 * @author Michael Watts
 * @since 23:52:18 - 14 Sep 2011
 */

public class Gold extends Sprite {
	// ========================================
	// Constants
	// ========================================
		
		private static float mGravity = 9.86f; //Gravity value for velocity
	
	// ========================================
	// Fields
	// ========================================
		
		private GestureDefence base;
		private PhysicsHandler mPhysicsHandler;
		
		private float baseY = 0.0f;
		private boolean killIT = false;
		private int cashWorth = 0; // Set to how much this coin is worth!
	
	// ========================================
	// Constructors
	// ========================================
	
		public Gold(final TextureRegion pTextureRegion, GestureDefence base)
		{
			super(0.0f, 0.0f, pTextureRegion);
			this.base = base;
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public void setup(final float pX, final float pY, int worth) {
			this.baseY = pY;
			this.cashWorth = worth;
			
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			
			this.setPosition(pX, pY - 10);
			this.mPhysicsHandler.setVelocityY(-300);
			
			if (pX > (base.getCameraWidth() / 2) )
			{
				this.mPhysicsHandler.setVelocityX(-40.0f);
			}
			else if (pX <= (base.getCameraWidth() / 2) )
			{
				this.mPhysicsHandler.setVelocityX(40.0f);
			}
		}
		
		public void completeReset() {
			this.baseY = 0.0f;
			this.killIT = false;
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
		
		@Override
		public void onManagedUpdate(final float pSecondsElapsed)
		{
			if (killIT)
			{
				base.sm.GameScreen.unregisterTouchArea(this);
				base.getGoldPool().recyclePoolItem(this);
			}
			else
			{
				if(this.mY < this.baseY)
				{
					this.mPhysicsHandler.setVelocityY(this.mPhysicsHandler.getVelocityY() + mGravity);
				}
				else
				{
					this.mPhysicsHandler.setVelocity(0.0f, 0.0f);
				}
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
						goldText gText = base.getGoldTextPool().obtainPoolItem();
						gText.setup(this.mX, this.mY, Integer.toString(cashWorth));
						if (!gText.hasParent())
							base.sm.GameScreen.getChild(3).attachChild(gText);
						if (!gText.isVisible())
							gText.setVisible(true);
						base.increaseGold(this.cashWorth);
						base.mMoneyEarned += cashWorth;
						base.CustomHUD.updateCashValue();
						this.killIT = true;
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
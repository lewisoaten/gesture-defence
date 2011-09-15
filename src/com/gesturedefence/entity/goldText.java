package com.gesturedefence.entity;

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.text.ChangeableText;

import com.gesturedefence.GestureDefence;

/**
 * @author Michael Watts
 * @since 00:41:09 - 15 Sep 2011
 */

public class goldText extends ChangeableText {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
		
		private GestureDefence base;
		private PhysicsHandler mPhysicsHandler;
		private boolean invisible = false;
	
	// ========================================
	// Constructors
	// ========================================
	
		public goldText(GestureDefence base)
		{
			super(0.0f, 0.0f, base.mFont2, "NILL");
			this.base = base;
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public void setup(final float pX, final float pY, String amount) {
			this.setText("+" + amount);
			this.invisible = false;
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			
			this.setPosition(pX, pY);
			this.mPhysicsHandler.setVelocityY(-25.0f);
			this.registerUpdateHandler(new TimerHandler(3.0f, new ITimerCallback() {
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler) { //Timer, once every second hit the castle!
					invisible = true;
					unregisterUpdateHandler(pTimerHandler);
				}
		}));
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
		
		@Override
		public void onManagedUpdate(final float pSecondsElapsed)
		{
			if (invisible)
			{
				base.getGoldTextPool().recyclePoolItem(this);
			}
			super.onManagedUpdate(pSecondsElapsed);
		}
	
	// ========================================
	// Methods
	// ========================================
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
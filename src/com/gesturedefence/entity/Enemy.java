package com.gesturedefence.entity;

/**
 * Author: Mike Since: 12:05:40 - 15 Jun 2011
 */

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.MathUtils;
import org.anddev.andengine.util.pool.EntityDetachRunnablePoolItem;

import com.gesturedefence.GestureDefence;

public class Enemy extends AnimatedSprite {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private final PhysicsHandler mPhysicsHandler;
		private TimerHandler mTimeMoved;
		
		private float mSpeed = MathUtils.random(0.5f,25.0f); //Each enemy is given a random speed when they are created (need to move min and max values to global variables!
		private float mGravity = 9.86f; //Gravity value for velocity, may move to global variable, 1 enemy = no problem, 2+ enemies = need global gravity
		private float mMoveDelay = 0.0f; //Used to store how long enemy was dragged
		private float mMoveX = 0.0f; //Used when the enemy is grabbed and moved, were did they move too
		private float mGroundHitSpeed = 0.0f; //Used for damage calculations
		private float mInitialY = this.getY(); //used to keep track of each enemy's ground levels
		private boolean mTimerHandler = false; //Used to track how long they were moved for
		private boolean mIsAirbourne = false; //Is the enemy airborne? (were they dragged up!)
		private int lastSetAnimation = 0; //0 = none, 1 = running, 2 = falling, 3 = death
		private float mHealth = 300.0f; //The amount of health the enemy has to start with
		private boolean mSetDeathAnimation = false; //Used to get death animation working properly
		private boolean mCanAttackCastle = false; //Used for attacking castle
		private float mAttackDamage = 10.0f; //The amount of damage each attack does to the castle
		private boolean mAttackedTheCastle = false; //Used to prevent enemy's doing loads of damage (caused by the period the frame is shown)
	
	// ========================================
	// Constructors
	// ========================================
	
		public Enemy(final float pX, final float pY, final TiledTextureRegion pTiledTextureRegion){
			super(pX, pY, pTiledTextureRegion);
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	// ========================================
	// Methods
	// ========================================
			
		@Override
		public void onManagedUpdate(final float pSecondsElapsed)
		{
			if (isEnemyDead())
			{
				if (this.mSetDeathAnimation == false)
				{
					this.stopAnimation();
					this.mPhysicsHandler.setEnabled(false);
					this.animate(new long[] {200, 200, 200}, new int[] {3, 4, 5}, 0);
					this.mSetDeathAnimation = true;
				}
				else if (this.isAnimationRunning() == false)
				{
					/* The following few lines remove the sprite's safely
					 * Should not cause any errors with removal
					 * recommenced by andengine author */
					final EntityDetachRunnablePoolItem pPoolItem = GestureDefence.RemoveStuff.obtainPoolItem();
					//Use set, NOT setEntity, if no parent null pointer exception!
					pPoolItem.set(this,this.getParent());
					GestureDefence.RemoveStuff.postPoolItem(pPoolItem);
				}
			}
			else
			{
				if (this.mCanAttackCastle)
				{
					if (this.getCurrentTileIndex() == 5 && this.mAttackedTheCastle == false)
					{
						//Do ONE set of damage when the frame is in the right place (animation frame)
						Castle.damageCastle(this.mAttackDamage);
						GestureDefence.updateCastleHealth();
						this.mAttackedTheCastle = true;
					}
					if (this.getCurrentTileIndex() != 5 && this.mAttackedTheCastle)
					{
						//Prevents the damage being done in overzealous amounts (ONCE per the frame)  
						this.mAttackedTheCastle = false;
					}
				}
				if(this.mPhysicsHandler.isEnabled())
				{					
					if (this.mIsAirbourne == true)
					{
						//Airborne code
						if (lastSetAnimation != 2)
						{
							this.animate(new long[] {200,0}, 3, 4, true);
							lastSetAnimation = 2;
						}					
						
						if(this.mX > (GestureDefence.CAMERA_WIDTH - this.getWidth()))
						{
							this.mPhysicsHandler.setVelocityX(0.0f);
							this.mX = (GestureDefence.CAMERA_WIDTH - this.getWidth());
						}
						if(this.mX < (0.0f + this.getWidth()))
						{
							this.mPhysicsHandler.setVelocityX(0.0f);
							this.setPosition(0.0f + this.getWidth(), this.mY);
						}
						
						if(this.mY > this.mInitialY)
						{
							this.mIsAirbourne = false;
							//Hurt them
							mGroundHitSpeed = this.mPhysicsHandler.getVelocityY();
							EnemySubtractHealth();
							
							this.mPhysicsHandler.setVelocityY(0.0f);
							this.setPosition(this.mX, this.mInitialY);
						}
						else if(this.mY < this.mInitialY)
						{
							this.mPhysicsHandler.setVelocityY(this.mPhysicsHandler.getVelocityY() + mGravity);
						}
						//End of airborne section
					}
					else
					{
						// Non airborne code!
						if(this.mX < (GestureDefence.CAMERA_WIDTH - 160) && this.mY >= this.mInitialY)
						{
							this.mPhysicsHandler.setVelocityX(mSpeed);
							if(lastSetAnimation != 1)
							{
								this.animate(new long[] {200, 200, 200}, 0, 2, true);
								lastSetAnimation = 1;
							}
						} 
						else if(this.mX + this.getWidth() > (GestureDefence.CAMERA_WIDTH - 160))
						{
							this.mPhysicsHandler.setVelocityX(0.0f);
							this.setPosition(GestureDefence.CAMERA_WIDTH - 160 + (this.getWidth() / 2), this.mY);
							if(lastSetAnimation != 3)
							{
								/* Attacking Castle
								 * Using wrong animation, this is all testing */
								this.animate(new long[] {200,200}, 4, 5, true);
								lastSetAnimation = 3;
								this.mCanAttackCastle = true;
							}
						}
						
						if(this.mY < this.mInitialY)
						{
							if((this.mY - this.mPhysicsHandler.getVelocityY() - mGravity) < this.mInitialY)
							{
								this.mPhysicsHandler.setVelocityY(this.mPhysicsHandler.getVelocityY() + mGravity);
							}
							else
							{
								this.mPhysicsHandler.setVelocityY(0.0f);
								this.setPosition(this.mX, this.mInitialY);
							}
						}
						else if(this.mY + this.getHeight() / 2 >= this.mInitialY)
						{
							this.mPhysicsHandler.setVelocityY(0.0f);
						}
					}
					//End of non-airborne section
				}
			}				
			super.onManagedUpdate(pSecondsElapsed);
		}
		
		public boolean isEnemyDead()
		{
			if(this.mHealth < 0.0f)
			{
				return true;
			}
			else
				return false;
		}
		
		public void EnemySubtractHealth()
		{
			this.mHealth -= (mGroundHitSpeed / 3);
			mGroundHitSpeed = 0;
		}
		
		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
		{
			this.mPhysicsHandler.setEnabled(false);
			switch(pSceneTouchEvent.getAction())
			{
				case TouchEvent.ACTION_DOWN:
					if (mTimerHandler == false)
					{
						this.mCanAttackCastle = false;
						mMoveDelay = 0.0f;
						mMoveX = this.mX;
						mTimeMoved = new TimerHandler(1 / 20.0f, true, new ITimerCallback()
						{
							@Override
							public void onTimePassed(final TimerHandler pTimerHandler)
							{
								//mMoveDelay++;
							}
						});
						this.registerUpdateHandler(this.mTimeMoved);
						mTimerHandler = true;
					}
					break;
				case TouchEvent.ACTION_MOVE:
					if(pSceneTouchEvent.getY() - this.getHeight() / 2 < mInitialY)
					{
						this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
						if (this.mIsAirbourne == false)
							this.mIsAirbourne = true;
					}
					else
					{
						this.mIsAirbourne = false;
					}
					break;
				case TouchEvent.ACTION_UP:
					if(mTimerHandler == true)
					{
						mMoveDelay = this.mTimeMoved.getTimerSecondsElapsed();
						this.unregisterUpdateHandler(this.mTimeMoved);
						
						float mDiffX = this.mX - mMoveX;
						float mDiffY = this.mY - mInitialY;
						float mVelocityX = mDiffX * 10;
						float mVelocityY = mDiffY * 10;
						
						this.mPhysicsHandler.setEnabled(true);						
						this.mPhysicsHandler.setVelocityX(mVelocityX * mMoveDelay * 10);
						this.mPhysicsHandler.setVelocityY(mVelocityY * mMoveDelay * 10);
						
						mTimerHandler = false;
					}
					break;
			}
			return true;
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
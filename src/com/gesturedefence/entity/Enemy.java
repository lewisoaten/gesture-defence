package com.gesturedefence.entity;

/**
 * @author Michael Watts
 * @since 12:05:40 - 15 Jun 2011
 */

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.MathUtils;

import com.gesturedefence.GestureDefence;

public class Enemy extends AnimatedSprite {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private GestureDefence base;
		
		private PhysicsHandler mPhysicsHandler;
		private TimerHandler mTimeMoved;
		
		private float mSpeed = 0.0f; //Each enemy is given a random speed when they are created (need to move min and max values to global variables!
		private float mGravity = 9.86f; //Gravity value for velocity, may move to global variable, 1 enemy = no problem, 2+ enemies = need global gravity
		private float mMoveDelay = 0.0f; //Used to store how long enemy was dragged
		private float mMoveX = 0.0f; //Used when the enemy is grabbed and moved, were did they move too
		private float mMoveY = 0.0f; //Used when the enemy is grabbed and moved, were did they move too
		private float mGroundHitSpeed = 0.0f; //Used for damage calculations
		private float mInitialY = this.getY(); //used to keep track of each enemy's ground level
		
		private boolean mTimerHandler = false; //Used to track how long they were moved for
		
		private boolean mIsAirbourne = false; //Is the enemy airborne? (were they dragged up!)
		private int lastSetAnimation = 0; //0 = none, 1 = running, 2 = falling, 3 = death, 4 = attacking, 5 = tripping
		
		private float mHealth = 0.0f; //The amount of health the enemy has to start with
		private boolean mSetDeathAnimation = false; //Used to get death animation working properly
		
		private boolean mCanAttackCastle = false; //Used for attacking castle
		private float mAttackDamage = 0.0f; //The amount of damage each attack does to the castle
		private boolean mAttackedTheCastle = false; //Used to prevent enemy's doing loads of damage (caused by the period the frame is shown)
		
		protected int mCashWorth = 0; //The amount each kill of this enemy is worth
		
		private int mEnemyType = 0;
		
		private boolean mTripping = false; //Is the enemy tripping
		
		private float mMaxspeed = 0.0f; //Each enemy is given a top top speed, as wave's progress, don't want streaks of lightning as motion :)
		
		private boolean mWasAirborne = false; //Used to track some achievements
	
	// ========================================
	// Constructors
	// ========================================
	
		public Enemy(final TiledTextureRegion pTiledTextureRegion, GestureDefence base)
		{
			super(0.0f, 0.0f, pTiledTextureRegion);
			this.base = base;
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public void setX(final float pX) {
			this.setX(pX);
		}
		
		public void setY(final float pY) {
			this.setY(pY);
			this.mInitialY = pY;
		}
		
		public void setXY(final float pX, final float pY) {
			this.setPosition(pX, pY);
			this.mInitialY = pY;
		}
		
		public void setType1() {
			float speed = 0.0f;
			
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			
			this.mCashWorth = 40;
			this.mAttackDamage = 10.0f;
			this.mHealth = 150.0f;
			this.mMaxspeed = 80.0f;
			
			speed = MathUtils.random(6.5f + base.theWave.getWaveNumber(),25.0f  + base.theWave.getWaveNumber());
			if (speed < this.mMaxspeed)
				this.mSpeed = speed;
			else
				this.mSpeed = this.mMaxspeed;
			
			this.mEnemyType = 1;
		}
		
		public void setType2() {
			float speed = 0.0f;
			
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			
			this.mCashWorth = 150;
			this.mAttackDamage = 100.0f;
			this.mHealth = 670.0f;
			this.mMaxspeed = 90.0f;
			
			speed = MathUtils.random(18.0f + base.theWave.getWaveNumber(), 40.0f + base.theWave.getWaveNumber());
			if (speed < this.mMaxspeed)
				this.mSpeed = speed;
			else
				this.mSpeed = this.mMaxspeed;
			
			this.mEnemyType = 2;
		}
		
		public void completeReset() {
			this.mSpeed = 0.0f;
			this.mGravity = 9.86f;
			this.mMoveDelay = 0.0f;
			this.mMoveX = 0.0f;
			this.mMoveY = 0.0f;
			this.mGroundHitSpeed = 0.0f;
			//this.mInitialY = this.getY();
			this.mTimerHandler = false;
			this.mIsAirbourne = false;
			this.lastSetAnimation = 0;
			this.mHealth = 0.0f;
			this.mSetDeathAnimation = false;
			this.mCanAttackCastle = false;
			this.mAttackDamage = 0.0f;
			this.mAttackedTheCastle = false;
			this.mCashWorth = 0;
			//this.mEnemyType = 0;
			this.mTripping = false;
			this.mMaxspeed = 0.0f;
			this.mWasAirborne = false;
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
		
		@Override
		public void onManagedUpdate(final float pSecondsElapsed)
		{
			if (base.mEarthQuaking)
			{
				this.mTripping = true;
			}
			if ((base.mLightningBolt == true)
					&& (this.mX <= base.mLightningBoltX + 100)
					&& (this.mX >= base.mLightningBoltX - 100)
					&& (this.mY >= base.mLightningBoltY - 70)
					&& (this.mY <= base.mLightningBoltY + 20))
			{
				this.EnemyHurtFace(1000.0f); //Lol's, gave it a random name, no idea why! Probably because I could
			}
			if (isEnemyDead())
			{
				if (this.mSetDeathAnimation == false)
				{
					this.stopAnimation();
					this.mPhysicsHandler.setEnabled(false);
					this.base.sm.GameScreen.unregisterTouchArea(this);
					this.animate(new long[] {200, 200, 200}, new int[] {6, 7, 8}, 0);
					this.lastSetAnimation = 3;
					this.mSetDeathAnimation = true;
					base.splat.play();
					
					int randomChance = MathUtils.random(1, 5); //20% Chance?
					if (randomChance == 3)
					{
						Mana mMana = this.base.getManaPool().obtainPoolItem();
						mMana.setup(this.mX, this.mY);
						if (!mMana.hasParent())
							base.sm.GameScreen.getChild(3).attachChild(mMana);
						base.sm.GameScreen.registerTouchArea(mMana);
					}
				}
				else if (this.isAnimationRunning() == false)
				{
					/* Add value of enemy to current cash amount
					 * Then update it
					 * Also increase global kill count */
					base.sMoney += this.mCashWorth;
					base.mMoneyEarned += this.mCashWorth;
					base.CustomHUD.updateCashValue();
					base.sKillCount++;
					base.mOnScreenEnemies--;
					
					base.AchieveTracker.firstKill(); //Boom kill
					
					/*
					 * New sprite removal code
					 */
					this.SendEnemyToEnemyPool(this.mEnemyType, this);
				}
			}
			else
			{
				if (this.mTripping)
				{
					if (this.lastSetAnimation != 5)
					{
						this.mPhysicsHandler.setEnabled(false);
						this.animate(new long[] {150,150,250, 150, 150}, new int[] {9, 10, 11, 10, 9}, 0);
						this.lastSetAnimation = 5;
						base.hurt.play();
						base.sm.GameScreen.unregisterTouchArea(this);
						this.EnemyHurtFace(50.0f);
					}
					else if (this.isAnimationRunning() == false)
					{
						this.mTripping = false;
						this.mPhysicsHandler.setEnabled(true);
						base.sm.GameScreen.registerTouchArea(this);
						if (this.mWasAirborne == false)
						{
							base.AchieveTracker.Trips(); //Trip that bitch!							
						}
						else
							this.mWasAirborne = false;
					}
				}
				
				if (this.mCanAttackCastle)
				{
					if (this.getCurrentTileIndex() == 4 && this.mAttackedTheCastle == false)
					{
						//Do ONE set of damage when the frame is in the right place (animation frame)
						Castle.damageCastle(this.mAttackDamage);
						base.CustomHUD.updateCastleHealth();
						this.mAttackedTheCastle = true;
						base.attack.play();
					}
					if (this.getCurrentTileIndex() != 4 && this.mAttackedTheCastle)
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
						base.sm.GameScreen.unregisterTouchArea(this);
						if (lastSetAnimation != 2)
						{
							this.animate(new long[] {200,0}, 6, 7, true);
							lastSetAnimation = 2;
						}					
						
						if(this.mX > (base.getCameraWidth() - (this.getWidth() / 2) ))
						{
							this.mPhysicsHandler.setVelocityX(0.0f);
							this.mX = (base.getCameraWidth() - (this.getWidth() / 2) );
						}
						if(this.mX < (0.0f - (this.getWidth() / 2) ))
						{
							this.mPhysicsHandler.setVelocityX(0.0f);
							this.setPosition(0.0f - (this.getWidth() / 2), this.mY);
						}
						
						if(this.mY > this.mInitialY)
						{
							this.mIsAirbourne = false;
							base.sm.GameScreen.registerTouchArea(this);
							//Hurt them
							mGroundHitSpeed = this.mPhysicsHandler.getVelocityY();
							EnemySubtractHealth();
							
							this.mPhysicsHandler.setVelocityY(0.0f);
							this.mPhysicsHandler.setVelocityX(0.0f);
							this.setPosition(this.mX, this.mInitialY);
							this.mTripping = true;
							this.mPhysicsHandler.setEnabled(false);
						}
						else if(this.mY < this.mInitialY)
						{
							if (this.mY < 0.0f) //Check if it's above the screen (screen top is 0.0)
								if (this.mPhysicsHandler.getVelocityY() < -1000) //Negative because Y is backwards
									this.mPhysicsHandler.setVelocityY(-1000); //If it's going to fast slow it down!
							this.mPhysicsHandler.setVelocityY(this.mPhysicsHandler.getVelocityY() + mGravity);
						}
						//End of airborne section
					}
					else
					{
						// Non airborne code!
						if(this.mX < (base.sCastle.getX() - base.sCastle.getWidth() / 6) && this.mY >= this.mInitialY)
						{
							this.mPhysicsHandler.setVelocityX(mSpeed);
							if(lastSetAnimation != 1)
							{
								this.animate(new long[] {200, 200, 200}, 0, 2, true);
								lastSetAnimation = 1;
							}
						} 
						else if(this.mX >= (base.sCastle.getX() - base.sCastle.getWidth() / 6))
						{
							this.mPhysicsHandler.setVelocityX(0.0f);
							this.setPosition( (base.sCastle.getX() - base.sCastle.getWidth() / 6), this.mY);
							if(lastSetAnimation != 4)
							{
								/* Attacking Castle animation */
								this.animate(new long[] {200,200,200}, 3, 5, true);
								lastSetAnimation = 4;
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
						mMoveY = this.mY;
						mTimeMoved = new TimerHandler(1 / 4.0f, true, new ITimerCallback()
						{
							@Override
							public void onTimePassed(final TimerHandler pTimerHandler)
							{
								//Do nothing, simply a timer for use in Action_UP
							}
						});
						this.registerUpdateHandler(this.mTimeMoved);
						mTimerHandler = true;
					}
					break;
				case TouchEvent.ACTION_MOVE:
					if(pSceneTouchEvent.getY() - this.getHeight() / 2 < mInitialY)
					{
						//Only move the enemy if the finger is moved further than a XX (allows for touch enemy, tripping)
						if (pSceneTouchEvent.getY() - this.mInitialY < -5.0f)
						{
							this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
							if (this.mIsAirbourne == false)
								this.mIsAirbourne = true;
						}
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
						float mDiffY = this.mY - mMoveY;
						mDiffX = mDiffX / mMoveDelay;
						mDiffY = mDiffY / mMoveDelay;
						
						float mVelocityX = mMoveDelay * (mDiffX / 2);
						float mVelocityY = mMoveDelay * (mDiffY / 2);
						
						//Check to see how far it moved, if it didn't move far at all, make them trip up instead
						if ( (mDiffY > -10.0f) )
						{
							mVelocityX = 0;
							mVelocityY = 0;
							this.mTripping = true;
						}
						else
						{
							this.mPhysicsHandler.setEnabled(true);						
							this.mPhysicsHandler.setVelocityX(mVelocityX);
							this.mPhysicsHandler.setVelocityY(mVelocityY);
							this.mWasAirborne = true;
						}
						
						mTimerHandler = false;
					}
					break;
			}
			return true;
		}
	
	// ========================================
	// Methods
	// ========================================
		
		public boolean isEnemyDead()
		{
			if(this.mHealth <= 0.0f)
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
		
		public void EnemyHurtFace(float amount)
		{
			this.mHealth -= amount;
		}
		
		public void SendEnemyToEnemyPool(int type, Enemy pEnemy) {
			if ( type == 1) {
				base.GetEnemyPool(type).recyclePoolItem(pEnemy);
			}
			if (type == 2) {
				base.GetEnemyPool(type).recyclePoolItem(pEnemy);
			}
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
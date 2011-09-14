package com.gesturedefence.entity;

/**
 * @author Michael Watts
 * @since 12:05:40 - 15 Jun 2011
 */

import java.util.Random;

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.MathUtils;

import com.gesturedefence.GestureDefence;

public class Enemy extends AnimatedSprite {
	// ========================================
	// Constants
	// ========================================
	
		private GestureDefence base;
		private static float mGravity = 9.86f; //Static value for gravity  
	
	// ========================================
	// Fields
	// ========================================
		
		private PhysicsHandler mPhysicsHandler;
		private TimerHandler mTimeMoved; // Used to work out how long enemy was moved
		
		private float baseY = 0.0f; //Stores the base ground level for each enemy entity
		private float mSpeed = 0.0f; //Stores the speed of the enemy (X direction)
		private float mMaxSpeed = 0.0f; //Stores the maximum speed of an enemy (as speed increases with difficulty, this prevents overly speedy entities)
		private float mHealth = 0.0f; //Stores the health of the enemy
		private float mAttackDamage = 0.0f; //Stores the attack damage of the enemy
		private float mGroundHitSpeed = 0.0f; //Stores the speed the enemy hit the ground at
		private float mMoveDelay = 0.0f; //Stores the time the enemy was moved for
		private float mInitialMoveX = 0.0f; //Stores the initial X value from once the enemy came
		private float mInitialMoveY = 0.0f; //Stores the initial Y value from once the enemy came
		private float mArrivedAtCastle = 0.0f; //Stores the time of arrival at the castle
		
		private int mCashWorth = 0; //Stores the cash worth of the enemy
		private int mEnemyType = 0; // Stores the enemy type
		private int currentAnimationCycle = 0; //Stores the current animation cycle being used (prevent's unnecessary over animation)
		
		private boolean mTimerHandler = false; // Used to track how long an enemy was moved
		private boolean mCanAttackCastle = false; // Used to work out if the enemy is allowed to attack the castle
		private boolean mIsAirborne = false; //Tells us if the enemy is airborne or not
		private boolean mTripTracker = false; //Tracks the triping of enemy's for the achievement!
		private boolean mTripping = false; //Stores whether the enemy is being tripped or not
		
		private Random manaChance = new Random(6); //Used in random mana spawn chance
	
	// ========================================
	// Constructors
	// ========================================
		
		public Enemy(final TiledTextureRegion pTiledTectureRegion, GestureDefence base) {
			super(0.0f, 0.0f, pTiledTectureRegion);
			this.base = base;
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public void setXY(final float pX, final float pY) {
			this.setPosition(pX,pY);
			this.baseY = pY;
		}
		
		public void setType1() {
			float speed = 0.0f; //Used to calculate speed increase based on level
			
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			
			this.mCashWorth = 40;
			this.mAttackDamage = 10.0f;
			this.mHealth = 150.0f;
			this.mMaxSpeed = 80.0f;
			
			//Calculate speed
			speed = MathUtils.random(6.5f + base.theWave.getWaveNumber(), 25.0f + base.theWave.getWaveNumber());
			if (speed < this.mMaxSpeed)
				this.mSpeed = speed;
			else
				this.mSpeed = this.mMaxSpeed;
			
			this.mEnemyType = 1;
		}
		
		public void setType2() {
			float speed = 0.0f; //Used to calculate speed increase based on level
			
			this.mPhysicsHandler = new PhysicsHandler(this);
			this.registerUpdateHandler(this.mPhysicsHandler);
			
			this.mCashWorth = 150;
			this.mAttackDamage = 100.0f;
			this.mHealth = 670.0f;
			this.mMaxSpeed = 90.0f;
			
			//Calculate speed
			speed = MathUtils.random(18.0f + base.theWave.getWaveNumber(), 40.0f + base.theWave.getWaveNumber());
			if (speed < this.mMaxSpeed)
				this.mSpeed = speed;
			else
				this.mSpeed = this.mMaxSpeed;
			
			this.mEnemyType = 2;
		}
		
		public void completeReset() { //Called when an enemy is being re-used from the pool
			this.baseY = 0.0f;
			this.mSpeed = 0.0f;
			this.mMaxSpeed = 0.0f;
			this.mHealth = 0.0f;
			this.mAttackDamage = 0.0f;
			this.mCashWorth = 0;
			this.mMoveDelay = 0.0f;
			this.mInitialMoveX = 0.0f;
			this.mInitialMoveY = 0.0f;
			this.mCanAttackCastle = false;
			this.mIsAirborne = false;
			this.mTripTracker = false;
			this.currentAnimationCycle = 0;
			this.mTripping = false;
			this.unregisterUpdateHandler(mPhysicsHandler);
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
		
		@Override
		public void onManagedUpdate(final float pSecondsElapsed) {
			//Run's every frame!!
			if (base.mLightningBolt //Lightning Strike Check
					&& (mX <= base.mLightningBoltX + 100)
					&& (mX >= base.mLightningBoltX - 100)
					&& (mY >= base.mLightningBoltY - 70)
					&& (mY <= base.mLightningBoltY + 20)) {
				hurtEnemy(1000.0f);
			}
			
			if (base.mEarthQuaking) //EarthQuake Check
				tripEnemy();
			
			if (mCanAttackCastle) { //The enemy is at the castle!
				if (pSecondsElapsed >= (mArrivedAtCastle + 1.0f) ) { //At least one second (I think) has elapsed!
					mArrivedAtCastle = pSecondsElapsed; //Reset for next time loop
					Castle.damageCastle(mAttackDamage);
					base.CustomHUD.updateCastleHealth();
					setAnimationCycle(4);
					base.attack.play();
					
				if (mY + getHeight() / 2 >= baseY) {
					setPosition(mX, baseY);
					mPhysicsHandler.setVelocityY(0.0f);
				}
			}
			} else if (!checkEnemyDeath()) { //Else to prevent running all this code when the enemy is already at the castle?
				//Enemy is alive do this
				if (!mTripping) { //Enemy is not tripping
					
					if (mPhysicsHandler.isEnabled()) {
						if (mIsAirborne) {
							// Airborne Section
							base.sm.GameScreen.unregisterTouchArea(this);
							setAnimationCycle(2);
							
							if (mX > (base.getCameraWidth() - (getWidth() / 2))) { //Prevents enemy leaving screen to the right
								mPhysicsHandler.setVelocityX(0.0f);
								mX = (base.getCameraWidth() - (getWidth() / 2));
							}
							
							if (mX < (0.0f - (getWidth() / 2))) { //Prevents enemy leaving screen to the left
								mPhysicsHandler.setVelocityX(0.0f);
								setPosition(0.0f - (getWidth() / 2), mY);
							}
							
							if (mY > baseY) { //Enemy just hit floor!
								mIsAirborne = false;
								mGroundHitSpeed = mPhysicsHandler.getVelocityY();
								enemyFallDamage();
							} else if (mY < baseY) { //Enemy is still falling!
								if (mPhysicsHandler.getVelocityY() < -1000)
									mPhysicsHandler.setVelocityY(-1000);
								mPhysicsHandler.setVelocityY(mPhysicsHandler.getVelocityY() + mGravity);
							}
							// End of Airborne Section
						} else {
							// Non Airborne Section
							if (!mCanAttackCastle) {
								if (mPhysicsHandler.getVelocityX() == 0.0f) {
									if (mX < (base.sCastle.getX() - base.sCastle.getWidth() / 6) && mY >= baseY) {
										mPhysicsHandler.setVelocityX(mSpeed);
										setAnimationCycle(1);
									}
								}
								//if (mPhysicsHandler.getVelocityX() > 0.0f){
									if (mX >= (base.sCastle.getX() - base.sCastle.getWidth() / 6)) {
										mPhysicsHandler.setVelocityX(0.0f);
										setPosition( (base.sCastle.getX() - base.sCastle.getWidth() / 6), mY);
										//Attack castle
										enemyAtCastle(pSecondsElapsed);
									}
								//}
								if (mY < mInitialMoveY){
									if ((mY - mPhysicsHandler.getVelocityY() - mGravity) < baseY)
										mPhysicsHandler.setVelocityY(mPhysicsHandler.getVelocityY() + mGravity);
									else {
										mPhysicsHandler.setVelocityY(0.0f);
										setPosition(mX, baseY);
									}
								} else if (mY + getHeight() / 2 >= baseY)
									mPhysicsHandler.setVelocityY(0.0f);
							}
							// End of Non airborne Section
						}
					}
				} else { //Enemy is tripping
					if (!isAnimationRunning()) {
						mTripping = false;
						mPhysicsHandler.setEnabled(true);
						setAnimationCycle(1);
						base.sm.GameScreen.registerTouchArea(this);
						if (!mTripTracker)
							base.AchieveTracker.Trips();
						else
							mTripTracker = false;
					}
				}
			} else {
				//Enemy is dead, get rid of it
				killEnemy();
				
				if (!isAnimationRunning()) {
					base.sMoney += mCashWorth;
					base.mMoneyEarned += mCashWorth;
					base.CustomHUD.updateCashValue();
					base.sKillCount++;
					base.mOnScreenEnemies--;
					
					base.AchieveTracker.firstKill();
					
					sendEnemyToPool(mEnemyType, this);
				}
			}
			super.onManagedUpdate(pSecondsElapsed);
		}
		
		@Override
		public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY){
			
			this.mPhysicsHandler.setEnabled(false); // Turn off the physics (we're dragging this bad boy!)
			
			switch(pSceneTouchEvent.getAction()) {
				case TouchEvent.ACTION_DOWN:
					if (!mTimerHandler) {
						mCanAttackCastle = false;
						mMoveDelay = 0.0f;
						mInitialMoveX = mX;
						mInitialMoveY = mY;
						mTimeMoved = new TimerHandler( 1 / 4.0f, true, new ITimerCallback() {
							@Override
							public void onTimePassed(final TimerHandler pTimerHandler) {
								// Do nothing. Simply a timer used in ACTION_UP
							}
						});
						registerUpdateHandler(this.mTimeMoved);
						mTimerHandler = true;
					}
					break;
				case TouchEvent.ACTION_MOVE:
					if (pSceneTouchEvent.getY() - getHeight() / 2 < baseY) {
						// Only move the enemy if the finger is moved further than XX (allows for enemy's to be tripped)
						if (pSceneTouchEvent.getY() - baseY < -5.0f) {
							setPosition(pSceneTouchEvent.getX() - getWidth() / 2, pSceneTouchEvent.getY() - getHeight() / 2);
							mIsAirborne = true; //Set airborne true
						}
					} else 
						mIsAirborne = false;
					break;
				case TouchEvent.ACTION_UP:
					if (mTimerHandler) {
						// Get the time dragged, then remove the timer
						mMoveDelay = mTimeMoved.getTimerSecondsElapsed();
						unregisterUpdateHandler(mTimeMoved);
						
						float DiffX = mX - mInitialMoveX;
						float DiffY = mY - mInitialMoveY;
						DiffX = DiffX / mMoveDelay;
						DiffY = DiffY / mMoveDelay;
						
						float mVelocityX = mMoveDelay * (DiffX / 2);
						float mVelocityY = mMoveDelay * (DiffY / 2);
						
						//Check to see how far it moved, if it didn't go far make it trip instead
						if ( ( DiffY > -10.0f) ) {
							mVelocityX = 0.0f;
							mVelocityY = 0.0f;
							tripEnemy();
						} else {
							mPhysicsHandler.setEnabled(true);
							mPhysicsHandler.setVelocity(mVelocityX, mVelocityY);
							mTripTracker = true; //Let it know that this enemy is falling
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
		
		public void hurtEnemy(float amount) {
			this.mHealth -= amount;
		}
		
		public void enemyFallDamage() {
			this.mHealth -= (mGroundHitSpeed / 3);
			mGroundHitSpeed = 0.0f;
			mPhysicsHandler.setVelocityX(0.0f);
			mPhysicsHandler.setVelocityY(0.0f);
			setPosition(mX, baseY);
			tripEnemy();
		}
		
		/**
		 * Checks to see if the enemy is dead.
		 * @return True if the enemy is dead.
		 */
		public boolean checkEnemyDeath() {
			if (mHealth <= 0.0f)
				return true;
			else
				return false;
		}
		
		public void lightningStrike() {
			// Lightning Strike
		}
		
		public void earthQuake() {
			// Earthquake
		}
		
		public void killEnemy() {
			// The enemy has been killed
			if (currentAnimationCycle != 3) {
				mPhysicsHandler.setEnabled(false);
				base.sm.GameScreen.unregisterTouchArea(this);
				setAnimationCycle(3);
				base.splat.play();
				
				//Mana Drop code
				if (manaChance.nextInt(7) == 6) { //Has to be one less?
					Mana mMana = base.getManaPool().obtainPoolItem();
					mMana.setup(mX, mY);
					if (!mMana.hasParent())
						base.sm.GameScreen.getChild(3).attachChild(mMana);
					base.sm.GameScreen.registerTouchArea(mMana);
				}
			}
		}
		
		public void setAnimationCycle(int ID) {
			// Set's the required animation cycle going
			switch (ID) {
			case 1: //Running
				if (currentAnimationCycle != 1) {
					animate(new long[] {200, 200, 200}, 0, 2, true);
					currentAnimationCycle = 1;
				}
				break;
			case 2: //Falling?
				if (currentAnimationCycle != 2) {
					animate(new long[] {200, 0}, 6, 7, true);
					currentAnimationCycle = 2;
				}
				break;
			case 3: // Death
				if (currentAnimationCycle != 3) {
					animate(new long[] {200, 200, 200}, new int[] {6, 7, 8}, 0);
					currentAnimationCycle = 3;
				}
				break;
			case 4: //attacking
				if (currentAnimationCycle != 4) {
					animate(new long[] {200, 200, 200}, 3, 5, true);
					currentAnimationCycle = 4;
				}
				break;
			case 5: //tripping
				if (currentAnimationCycle != 5) {
					animate(new long[] {150, 150, 250, 150, 150}, new int[] {9, 10, 11, 10, 9}, 0);
					currentAnimationCycle = 5;
				}
				
				break;
			}
		}
		
		public int getAnimationCycle() {
			return currentAnimationCycle;
		}
		
		public void tripEnemy() {
			// Trip the enemy ?
			if (!mTripping) {
				mPhysicsHandler.setEnabled(false);
				mTripping = true;
				setAnimationCycle(5);
				base.hurt.play();
				base.sm.GameScreen.unregisterTouchArea(this);
				hurtEnemy(50.0f);
			}
		}
		
		public void enemyAtCastle(float secondselapsed) {
			// Enemy has arrived at the castle
			if (!mCanAttackCastle) {
				mCanAttackCastle = true;
				setAnimationCycle(4);
				mArrivedAtCastle = secondselapsed;
				
			}
		}
		
		public void sendEnemyToPool(int type, Enemy pEnemy) {
			switch (type) {
			case 1:
				if (mPhysicsHandler != null)
					unregisterUpdateHandler(mPhysicsHandler);
				base.GetEnemyPool(type).recyclePoolItem(pEnemy);				
				break;
			case 2:
				if (mPhysicsHandler != null)
					unregisterUpdateHandler(mPhysicsHandler);
				base.GetEnemyPool(type).recyclePoolItem(pEnemy);
				break;
			default:
				base.GetEnemyPool(type).recyclePoolItem(pEnemy); // Redundant statement? Might work / might not
				break;
			}
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
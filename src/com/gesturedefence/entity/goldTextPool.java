package com.gesturedefence.entity;

import org.anddev.andengine.util.pool.GenericPool;

import com.gesturedefence.GestureDefence;

/**
 * @author Michael Watts
 * @since 00:44:45 - 15 Sep 2011
 */

public class goldTextPool extends GenericPool<goldText> {
	
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
		
		private GestureDefence base;
	
	// ========================================
	// Constructors
	// ========================================
		
		public goldTextPool(GestureDefence base) {
			this.base = base;
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
		
		/**
		 * Called when an enemy is required but there isn't one in the pool
		 */
		@Override
		protected goldText onAllocatePoolItem() {
			return new goldText(base); // Clone(), because it's an animated sprite!
		}
		
		/**
		 * Called when an enemy is sent to the pool
		 */
		@Override
		protected void onHandleRecycleItem(final goldText pGoldText) {
			pGoldText.setIgnoreUpdate(true);
			pGoldText.setVisible(false);
		}
		
		/**
		 * Called just before an enemy is returned to the caller, this is where we could initialise it
		 * i.e. Set position, size etc
		 */
		@Override
		protected void onHandleObtainItem(final goldText pGoldText) {
			pGoldText.reset();
		}
	
	// ========================================
	// Methods
	// ========================================
			
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
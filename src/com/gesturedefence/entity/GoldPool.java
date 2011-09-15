package com.gesturedefence.entity;

import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.pool.GenericPool;

import com.gesturedefence.GestureDefence;
import com.gesturedefence.entity.Gold;

/**
 * @author Michael Watts
 * @since 00:22:46 - 15 Sep 2011
 */

public class GoldPool extends GenericPool<Gold> {
	
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private TextureRegion mTextureRegion;
		private GestureDefence base;
	
	// ========================================
	// Constructors
	// ========================================
		
		public GoldPool(TextureRegion pTextureRegion, GestureDefence base) {
			if (pTextureRegion == null) {
				// Need to be able to create a sprite so the pool needs to have a TextureRegion
				throw new IllegalArgumentException("The Texture region must not be NULL");
			}
			mTextureRegion = pTextureRegion;
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
		protected Gold onAllocatePoolItem() {
			return new Gold(mTextureRegion.deepCopy(), base); // Clone(), because it's an animated sprite!
		}
		
		/**
		 * Called when an enemy is sent to the pool
		 */
		@Override
		protected void onHandleRecycleItem(final Gold pGold) {
			pGold.setIgnoreUpdate(true);
			pGold.completeReset(); // Initialise / reset
			pGold.setVisible(false);
		}
		
		/**
		 * Called just before an enemy is returned to the caller, this is where we could initialise it
		 * i.e. Set position, size etc
		 */
		@Override
		protected void onHandleObtainItem(final Gold pGold) {
			pGold.reset();
		}
	
	// ========================================
	// Methods
	// ========================================
			
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
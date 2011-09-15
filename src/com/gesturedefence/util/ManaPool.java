package com.gesturedefence.util;

import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.util.pool.GenericPool;

import com.gesturedefence.GestureDefence;
import com.gesturedefence.entity.Mana;

/**
 * @author Michael Watts
 * @since 19:19:20 - 13 Sep 2011
 */

public class ManaPool extends GenericPool<Mana> {
	
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
		
		public ManaPool(TextureRegion pTextureRegion, GestureDefence base) {
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
		protected Mana onAllocatePoolItem() {
			return new Mana(mTextureRegion.deepCopy(), base); // Clone(), because it's an animated sprite!
		}
		
		/**
		 * Called when an enemy is sent to the pool
		 */
		@Override
		protected void onHandleRecycleItem(final Mana pMana) {
			pMana.setIgnoreUpdate(true);
			pMana.completeReset(); // Initialise / reset
			pMana.setVisible(false);
		}
		
		/**
		 * Called just before an enemy is returned to the caller, this is where we could initialise it
		 * i.e. Set position, size etc
		 */
		@Override
		protected void onHandleObtainItem(final Mana pMana) {
			pMana.reset();
		}
	
	// ========================================
	// Methods
	// ========================================
			
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
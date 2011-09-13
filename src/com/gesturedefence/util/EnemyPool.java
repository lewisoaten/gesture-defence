package com.gesturedefence.util;

import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.pool.GenericPool;

import com.gesturedefence.GestureDefence;
import com.gesturedefence.entity.Enemy;

/**
 * @author Michael Watts
 * @since 22:42:28 - 12 Sep 2011
 */

public class EnemyPool extends GenericPool<Enemy> {
	
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private TiledTextureRegion mTextureRegion;
		private GestureDefence base;
	
	// ========================================
	// Constructors
	// ========================================
		
		public EnemyPool(TiledTextureRegion pTextureRegion, GestureDefence base) {
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
		protected Enemy onAllocatePoolItem() {
			return new Enemy(mTextureRegion.clone(), base); // Clone(), because it's an animated sprite!
		}
		
		/**
		 * Called when an enemy is sent to the pool
		 */
		@Override
		protected void onHandleRecycleItem(final Enemy pEnemy) {
			pEnemy.setIgnoreUpdate(true);
			pEnemy.completeReset(); // Initialise / reset
			pEnemy.setVisible(false);
		}
		
		/**
		 * Called just before an enemy is returned to the caller, this is where we could initialise it
		 * i.e. Set position, size etc
		 */
		@Override
		protected void onHandleObtainItem(final Enemy pEnemy) {
			pEnemy.reset();
		}
	
	// ========================================
	// Methods
	// ========================================
			
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
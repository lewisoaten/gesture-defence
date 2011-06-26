package com.gesturedefence.util;

/**
 * @author Michael Watts
 * @since 12:16:38 - 22 Jun 2011
 */

import android.app.Activity;
import android.widget.Toast;

import com.gesturedefence.GestureDefence;
import com.openfeint.api.resource.Achievement;

public class Atracker {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private GestureDefence base; //Instance of GestureDefence
		
		private boolean of1048612 = false; //Track's completion of 'Arghh My Legs!!'
		private boolean of1048612Loaded  = false;
		
		private boolean of1048622 = false; //Track's completion of 'Sir Trips a lot'
		private float ofTripCount = 0.0f; //Percent Complete!
		private boolean of1048622Loaded = false;
	
	// ========================================
	// Constructors
	// ========================================
		
		public Atracker(GestureDefence baseThing)
		{ 
			this.base = baseThing;
			
			final Achievement a = new Achievement("1048622");
			a.load(new Achievement.LoadCB() {
				
				@Override
				public void onSuccess() {
					//Long Delay?
					ofTripCount = a.percentComplete;
					of1048622 = a.isUnlocked;
					of1048622Loaded = true;
				}
			});
			
			
			final Achievement a2 = new Achievement("1048612");
			a2.load(new Achievement.LoadCB() {				
				@Override
				public void onSuccess() {
					//Long Delay?
					of1048612 = a.isUnlocked;
					of1048612Loaded = true;
				}
			});
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
		
		/*
		 * Using variable for each acheivement to check progress
		 * should prevent excessive network spam (usage!)
		 * Once a achievement is done it wont query the openfeint server's again!
		 */
		
		public void firstKill() {
			if (this.of1048612 == false && this.of1048612Loaded)
			{
				new Achievement("1048612").unlock(new Achievement.UnlockCB () {
					@Override public void onSuccess(boolean complete) {
						base.setResult(Activity.RESULT_OK);
						of1048612 = complete;
					}
	
					@Override public void onFailure(final String exceptionMessage) {
						base.handler.post(new Runnable() {
							public void run() {
								Toast.makeText(base.getApplicationContext(), "Error (" + exceptionMessage + ") unlocking achievement.", Toast.LENGTH_SHORT).show();
							}
						});
						base.setResult(Activity.RESULT_CANCELED);
					}
				});
			}
		}
		
		public void Trips() {
			if (this.of1048622 == false && this.of1048622Loaded)
			{
				if (this.ofTripCount < 100.0f)
					this.ofTripCount+= 10.0f;
				else
					this.ofTripCount = 100.0f;
				
				new Achievement("1048622").updateProgression(this.ofTripCount, new Achievement.UpdateProgressionCB() {
					@Override
					public void onSuccess(boolean complete) {
						of1048622 = complete;
					}
					
					@Override
					public void onFailure(final String exceptionMessage) {
						base.handler.post(new Runnable() {
							public void run() {
								Toast.makeText(base.getApplicationContext(), "Error (" + exceptionMessage + ") updating achievement.", Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
			}
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
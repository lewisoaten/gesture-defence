package com.gesturedefence.util;

/**
 * @author Michael Watts
 * @since 12:16:38 - 22 Jun 2011
 */

import java.util.ArrayList;

import android.app.Activity;

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
		
		private ArrayList<String> mSaveSettings = new ArrayList<String>();
		private ArrayList<Boolean> achieveProgress = new ArrayList<Boolean>();
		
		// Number: 1
		//private boolean of1048612 = false; //Track's completion of 'Arghh My Legs!!' (Drop and kill 1 enemy)
		private boolean of1048612Loaded  = false;
		
		// Number: 2
		//private boolean of1048622 = false; //Track's completion of 'Sir Trips a lot' (10 Trips)
		private float ofTripCount = 0.0f; //Percent Complete!
		private boolean of1048622Loaded = false;
	
	// ========================================
	// Constructors
	// ========================================
		
		public Atracker(GestureDefence baseThing)
		{ 
			this.base = baseThing;
			achieveProgress.add(0, false); // of1048612
			achieveProgress.add(1, false); // of1048622
			loadAchievements();
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public void setAchieve1(boolean state) {
			if (state) //If state is true then the Achievement is complete!
			{
				achieveProgress.set(0, true);
				of1048612Loaded = true;
			}
		}
		
		public void setAchieve2(boolean state) {
			if (state) //If state is true then the Achievement is complete!
			{
				achieveProgress.set(1, true);
				of1048622Loaded = true;
				ofTripCount = 100.0f;
			}
		}
		
		public ArrayList<String> getSaveInfo() {
			mSaveSettings.clear();
			
			//Save Number: 1
			mSaveSettings.add("1.1:" + achieveProgress.get(0));
			mSaveSettings.add("");
			
			//Save Number: 2
			mSaveSettings.add("2.1:" + achieveProgress.get(1));
			//mSaveSettings.add("2.2:" + ofTripCount);
			mSaveSettings.add("");
			
			//Return the results
			return mSaveSettings;
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
		
		private void loadAchievements() {
			if (base.isOnline())
			{//Network available
				
				if (!of1048622Loaded) {
					final Achievement a = new Achievement("1048622");
					a.load(new Achievement.LoadCB() {
						
						@Override
						public void onSuccess() {
							//Long Delay?
							ofTripCount = a.percentComplete;
							achieveProgress.set(1, a.isUnlocked);
							of1048622Loaded = true;
						}
					});
				}
				
				if (!of1048612Loaded) {
					final Achievement a2 = new Achievement("1048612");
					a2.load(new Achievement.LoadCB() {				
						@Override
						public void onSuccess() {
							//Long Delay?
							achieveProgress.set(0, a2.isUnlocked);
							of1048612Loaded = true;
						}
					});
				}
			}
			
			else
			{// Do a check for a local file instead
				if (base.fileThingy.CheckForAchieveProgressFile(base)) {
					base.fileThingy.loadAchieveProgress(base);
				}
			}
		}
	
	// ========================================
	// Methods
	// ========================================
		
		/*
		 * Using variable for each achievement to check progress
		 * should prevent excessive network spam (usage!)
		 * Once a achievement is done it wont query the openfeint server's again!
		 */
		
		public void firstKill() {
			if (this.achieveProgress.get(0) == false && this.of1048612Loaded)
			{
				if (base.isOnline()) {
					new Achievement("1048612").unlock(new Achievement.UnlockCB () {
						@Override public void onSuccess(boolean complete) {
							achieveProgress.set(0, complete);
						}
					});
				}
			}
		}
		
		public void Trips() {
			if (this.achieveProgress.get(1) == false && this.of1048622Loaded)
			{
				if (this.ofTripCount < 100.0f)
					this.ofTripCount+= 10.0f;
				else
					this.ofTripCount = 100.0f;
				
				if (base.isOnline() && this.ofTripCount == 100.0f) {
					new Achievement("1048622").updateProgression(this.ofTripCount, new Achievement.UpdateProgressionCB() {
						@Override
						public void onSuccess(boolean complete) {
							achieveProgress.set(1, complete);
						}
					});
				}
			}
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
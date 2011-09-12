package com.gesturedefence.util;

import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.entity.text.ChangeableText;

import com.gesturedefence.GestureDefence;

/**
 * @author Michael Watts
 * @since 21:26:03 - 12 Sep 2011
 */

public class HUD_revamp extends HUD {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private GestureDefence base;
		private HUD NewHud;
		
		/* Text fields */
		private static ChangeableText sCastleHealth; //Changeable text item for castle health
		private static ChangeableText sMoneyText; //Changeable text item for Current money
		private static ChangeableText sManaText; //Mana HUD text
	
	// ========================================
	// Constructors
	// ========================================
		
		public HUD_revamp(GestureDefence basething)
		{
			this.base = basething; //Set the base window
			
			if (base.sCamera.hasHUD() != true) //Check there isn't already a HUD for some reason
			{
				this.NewHud = new HUD(); //Create a new HUD instance
				
				//Set castle health text
				sCastleHealth = new ChangeableText(base.getCameraWidth() - 200, 0 + 20, base.mFont2, "XXXXXX / XXXXXX", "XXXXXX / XXXXXX".length());
				this.NewHud.attachChild(sCastleHealth);
				this.NewHud.getChild(this.NewHud.getChildIndex(sCastleHealth)).setVisible(false); //Hide it
				
				//Set Money text
				sMoneyText = new ChangeableText(0 + 100, 0 + 20, base.mFont2, "" + base.sMoney, "XXXXXX".length());
				this.NewHud.attachChild(sMoneyText);
				this.NewHud.getChild(this.NewHud.getChildIndex(sMoneyText)).setVisible(false); // Hide it
				
				//Set Mana text
				sManaText = new ChangeableText(sCastleHealth.getX(), sCastleHealth.getY() + sCastleHealth.getHeight(), base.mFont2, "XXXXXX", "XXXXXX".length());
				sManaText.setColor(0.0f, 0.0f, 0.8f);
				this.NewHud.attachChild(sManaText);
				this.NewHud.getChild(this.NewHud.getChildIndex(sManaText)).setVisible(false); // Hide it
				
				base.sCamera.setHUD(NewHud); //Finally attach the new HUD to the game
			}
			
			this.updateCashValue();
			this.updateCastleHealth();
			this.updateManaValue();
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
				
		public HUD getHud() {
			return this.NewHud;
		}
		
		public void updateCastleHealth()
		{ //Refresh's the castle's health display
			sCastleHealth.setText(base.sCastle.getCurrentHealth() + " / " + base.sCastle.getMaxHealth());
		}
		
		public void updateCashValue()
		{ //Refresh's the current money display
			sMoneyText.setText("" + base.sMoney);
		}
		
		public void updateManaValue()
		{ //Refresh's the current money display
			sManaText.setText("" + base.mana);
		}
		
		public void RefreshHUD() {
			this.updateCashValue();
			this.updateCastleHealth();
			this.updateManaValue();
		}
		
		public void HideValues(boolean hide)
		{
			if (hide) // Hide the values
			{
				this.getHud().getChild(this.getHud().getChildIndex(sCastleHealth)).setVisible(false);
				this.getHud().getChild(this.getHud().getChildIndex(sMoneyText)).setVisible(false);
				this.getHud().getChild(this.getHud().getChildIndex(sManaText)).setVisible(false);
			}
			else
			{ //Else show them
				this.getHud().getChild(this.getHud().getChildIndex(sCastleHealth)).setVisible(true);
				this.getHud().getChild(this.getHud().getChildIndex(sMoneyText)).setVisible(true);
				this.getHud().getChild(this.getHud().getChildIndex(sManaText)).setVisible(true);
			}
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	// ========================================
	// Methods
	// ========================================
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
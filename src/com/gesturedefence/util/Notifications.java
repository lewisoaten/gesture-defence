package com.gesturedefence.util;

/**
 * @author Michael Watts
 * @since 19:14:23 - 26 Jun 2011
 */

import java.util.ArrayList;

import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;

import android.graphics.Color;

import com.gesturedefence.GestureDefence;

public class Notifications {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		/* Adjustable Settings */
		private int Duration = 3; //Duration to show the notifications
		private int Xpos = 0; // X position of notifications (0 = Far left)
		private int Ypos = 4; // Y position of notifications (0 = Top, >0 = Towards-Bottom)
	
		private GestureDefence base; //Instance of GestureDefence
		private ArrayList<String> mMessages = new ArrayList<String>();
		
		private Texture mFontTexture3; //Font for openfeint notifications
		private Font mFont3; //Font for openfeint notifications
		
		private ChangeableText theNotification;
		
		public boolean mAlreadyShowing = false; // Used to work out if a message is already showing
		private int lastShowedMessage = -1;
		
		private Scene theSceneShowing;
		
		private Texture OFLogo;
		private TextureRegion OFLogoRegion;
		private Sprite smallOFLogo;
		private Texture backDrop;
		private TextureRegion BackDropRegion;
		private Sprite NotificationBackDrop;
		
		TimerHandler notificationDuration; // Timer handler for each individual message!
	
	// ========================================
	// Constructors
	// ========================================
		
		public Notifications(GestureDefence baseThing)
		{ 
			this.base = baseThing;
			
			// Notification font
			this.mFontTexture3 = new Texture(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			FontFactory.setAssetBasePath("font/");
			this.mFont3 = FontFactory.createFromAsset(this.mFontTexture3, base, "CGF Locust Resistance.ttf", 12, true, Color.WHITE);
			base.getEngine().getTextureManager().loadTexture(this.mFontTexture3);
			base.getEngine().getFontManager().loadFont(mFont3);
			
			this.OFLogo = new Texture(32,32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.OFLogoRegion = TextureRegionFactory.createFromAsset(this.OFLogo, base, "gfx/logo.small.of.png", 10, 10);
			base.getEngine().getTextureManager().loadTexture(OFLogo);
			
			smallOFLogo = new Sprite(Xpos + 10, Ypos + 2, this.OFLogoRegion);
			
			this.backDrop = new Texture(1024, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
			this.BackDropRegion = TextureRegionFactory.createFromAsset(this.backDrop, base, "gfx/notification_backdrop.png", 0, 0);
			base.getEngine().getTextureManager().loadTexture(backDrop);
			
			NotificationBackDrop = new Sprite(Xpos, Ypos, this.BackDropRegion);
			
			theNotification = new ChangeableText(Xpos + 15 + smallOFLogo.getWidth(), Ypos + 4, mFont3, "", base.getCameraWidth());
			
			if (base.gethud() != null)
			{
				base.gethud().getChild(1).attachChild(NotificationBackDrop);
				base.gethud().getChild(1).attachChild(theNotification);
				base.gethud().getChild(1).attachChild(smallOFLogo);
				base.gethud().getChild(1).setVisible(false);
			}
			
			TimerHandler NotificationChecks; //Register a new timer, check every second check for new messages!
			base.getEngine().registerUpdateHandler(NotificationChecks = new TimerHandler(1, true, new ITimerCallback() {

				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					// TODO Auto-generated method stub
					base.CustomNotifications.checkMessages();
				}
			}));
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public void addNotification(String noti) {
			mMessages.add(noti);
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	// ========================================
	// Methods
	// ========================================
		
		public void checkMessages() {
			if (mAlreadyShowing == false)
			{ // No message currently on the screen. Lets do THIS!
				try {
				if (mMessages.size() > 0) //Make sure we have some messages!
				{
					if (base.gethud() != null)
					{ //Makes sure not to crash if it hasn't been made yet!
						showMessage(0);
						lastShowedMessage = 0;
					}
				}
				} catch (NullPointerException e)
				{
					//ERROR! , Do Nothing ;)
				}
			}
		}
		
		public void showMessage(int indicator) {
			// Outputs the message
			this.mAlreadyShowing = true; // Tell it that we are now showing a message
			theNotification.setText(mMessages.get(indicator));
			if (base.gethud().getChild(1).getChildCount() <= 0)
			{
				base.gethud().getChild(1).attachChild(NotificationBackDrop);
				base.gethud().getChild(1).attachChild(theNotification);
				base.gethud().getChild(1).attachChild(smallOFLogo);
			}
			base.gethud().getChild(1).setVisible(true);

			base.gethud().registerUpdateHandler(notificationDuration = new TimerHandler(Duration,new ITimerCallback() {
				@Override
				public void onTimePassed(TimerHandler pTimerHandler) {
					base.gethud().unregisterUpdateHandler(pTimerHandler); // Remove the timer (prevent duplicates/issues)
					if (lastShowedMessage != -1)
					{
						mMessages.remove(lastShowedMessage);
						lastShowedMessage = -1;
					}
					base.gethud().getChild(1).setVisible(false);
					mAlreadyShowing = false; // Set to make sure it knows the current notification is over
				}
			}));

		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
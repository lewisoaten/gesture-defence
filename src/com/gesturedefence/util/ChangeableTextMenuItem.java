package com.gesturedefence.util;

import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.opengl.font.Font;

/**
 * @author Michael Watts
 * @since: 19:28:21 - 15 Jun 2011
 */


public class ChangeableTextMenuItem extends ChangeableText implements IMenuItem {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
	private final int mID;
	
	// ========================================
	// Constructors
	// ========================================
	
	public ChangeableTextMenuItem(final int pID, final Font pFont, final String pText)
	{
		super(0, 0, pFont, pText);
		this.mID = pID;
	}
	
	public ChangeableTextMenuItem(final int pID, final Font pFont, final String pText, final int pMaxLength)
	{
		super(0, 0, pFont, pText, pMaxLength);
		this.mID = pID;
	}
	
	// ========================================
	// Getter & Setter
	// ========================================
	
	public void setText(String pText)
	{
		super.setText(pText);
	}
	
	@Override
	public int getID()
	{
		return this.mID;
	}
		
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	@Override
	public void onSelected()
	{
		/* Do Nothing, it's text god damn it! */
	}
	
	@Override
	public void onUnselected()
	{
		/* Do Nothing, it's text god damn it! */
	}
	
	// ========================================
	// Methods
	// ========================================
		
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
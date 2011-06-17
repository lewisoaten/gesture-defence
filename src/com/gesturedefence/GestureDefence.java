package com.gesturedefence;

/**
 * @author Michael Watts
 * @since 12:43:16 - 12 Jun 2011
 */

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.AutoParallaxBackground;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.TextMenuItem;
import org.anddev.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.pool.EntityDetachRunnablePoolUpdateHandler;

import android.graphics.Color;
import android.view.KeyEvent;

import com.gesturedefence.entity.Castle;
import com.gesturedefence.entity.Enemy;
import com.gesturedefence.util.ScreenManager;
import com.gesturedefence.util.Wave;

public class GestureDefence extends BaseGameActivity implements IOnMenuItemClickListener {
	// ========================================
	// Constants
	// ========================================
	
	public final static int CAMERA_WIDTH = 720;
	public final static int CAMERA_HEIGHT = 480;
	
	private static final int MENU_START = 0;
	private static final int MENU_QUIT = MENU_START + 1;
	private static final int MENU_RESTART = MENU_QUIT + 1;
	public static final int MENU_BUY_HEALTH = MENU_RESTART + 1;
	public static final int MENU_START_NEXT_WAVE = MENU_BUY_HEALTH + 1;
	public static final int MENU_HEALTH = MENU_START_NEXT_WAVE + 1;
	public static final int MENU_CASH = MENU_HEALTH + 1;
	public static final int MENU_WAVE_NUMBER = MENU_CASH + 1;
	
	// ========================================
	// Fields
	// ========================================
	
	public static Camera sCamera;
	
	public EntityDetachRunnablePoolUpdateHandler sRemoveStuff; //Used to safely remove Animated Sprite's (enemies)
	
	private Texture mAutoParallaxBackgroundTexture;
	
	private TextureRegion mParallaxLayerBack;
	private TextureRegion mParallaxLayerFront;
	
	public AutoParallaxBackground autoParallaxBackground;
	
	private Texture mFontTexture;
	public Font mFont;
	
	private Texture newEnemyTexture;
	private TiledTextureRegion sEnemyTextureRegion;
	
	private Texture mCastleTexture;
	private TextureRegion mCastleTextureRegion;
	private static ChangeableText sCastleHealth;
	public Castle sCastle;
	
	private HUD hud;
	private static ChangeableText sMoneyText;
	
	public Wave theWave;
	
	public boolean sEndWaveActive = false; //Remove?
	
	/* These need to be reset/loaded for each game */
	public int sPreviousWaveNum = 0;
	public int sKillCount = 0;
	public int sPreviousKillCount = 0;
	public int sMoney = 0; //This is the amount of cash so far
	public int sEnemyCount = 0;
	// ------
	
	public ScreenManager sm;
	
	/* New menu texture's
	 * These currently placeholder's
	 */
	private Texture mStartButton;
	private TextureRegion mStartButtonRegion;
	private Texture mQuitButton;
	private TextureRegion mQuitButtonRegion;
	private Texture mBuyButton;
	private TextureRegion mBuyButtonRegion;
	private Texture mNextWaveButton;
	private TextureRegion mNextWaveButtonRegion;
	
	// ========================================
	// Constructors
	// ========================================
	
	// ========================================
	// Getter & Setter
	// ========================================
	
	public void setParallaxLayerFront(TextureRegion mParallaxLayerFront) {
		this.mParallaxLayerFront = mParallaxLayerFront;
	}

	public TextureRegion getParallaxLayerFront() {
		return mParallaxLayerFront;
	}

	public void setParallaxLayerBack(TextureRegion mParallaxLayerBack) {
		this.mParallaxLayerBack = mParallaxLayerBack;
	}

	public TextureRegion getParallaxLayerBack() {
		return mParallaxLayerBack;
	}
	
	public void setStartButtonRegion(TextureRegion StartButtonRegion) {
		this.mStartButtonRegion = StartButtonRegion;
	}
	
	public TextureRegion getStartButtonRegion() {
		return mStartButtonRegion;
	}
	
	public void setQuitButtonRegion(TextureRegion QuitButtonRegion) {
		this.mQuitButtonRegion = QuitButtonRegion;
	}
	
	public TextureRegion getQuitButtonRegion() {
		return mQuitButtonRegion;
	}
	
	public Texture getBuyButton() {
		return mBuyButton;
	}
	
	public TextureRegion getBuyButtonRegion() {
		return mBuyButtonRegion;
	}
	
	public Texture getNextWaveButton() {
		return mNextWaveButton;
	}
	public TextureRegion getNextWaveButtonRegion() {
		return mNextWaveButtonRegion;
	}
	
	public Texture getCastleTexture() {
		return mCastleTexture;
	}
	
	public int getCameraWidth() {
		return CAMERA_WIDTH;
	}
	
	public int getCameraHeight() {
		return CAMERA_HEIGHT;
	}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================

	@Override
	public Engine onLoadEngine() {
	GestureDefence.sCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), GestureDefence.sCamera));
	}
	
	@Override
	public void onLoadResources() {
		/* Load Font settings*/
		this.mFontTexture = new Texture(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		FontFactory.setAssetBasePath("font/");
		this.mFont = FontFactory.createFromAsset(this.mFontTexture, this, "Capture it.ttf", 48, true, Color.WHITE);
		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
		this.mEngine.getFontManager().loadFont(mFont);
		
		this.sm = new ScreenManager(this);
	}
	
	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		final Scene loadScene = new Scene(1);
		loadScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
		
		final Text textCenter = new Text(100, 60, this.mFont, "LOADING..", HorizontalAlign.CENTER);
		loadScene.attachChild(textCenter);
		
		loadScene.registerUpdateHandler(new TimerHandler(1.0f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				loadScene.unregisterUpdateHandler(pTimerHandler);
				
				/* Initialise the background images, into scrolling background (parallax background) */
				GestureDefence.this.mAutoParallaxBackgroundTexture = new Texture(1024, 1024, TextureOptions.DEFAULT);
				GestureDefence.this.setParallaxLayerBack(TextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture, GestureDefence.this, "gfx/temp_background.png", 0, 0));
				GestureDefence.this.setParallaxLayerFront(TextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture, GestureDefence.this,"gfx/temp_clouds.png", 0, 650));
				
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.mAutoParallaxBackgroundTexture);
				
				GestureDefence.this.autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
				GestureDefence.this.autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, getCameraHeight() - getParallaxLayerBack().getHeight(), getParallaxLayerBack())));
				GestureDefence.this.autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, getParallaxLayerFront())));
				GestureDefence.this.autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(35, 62, getParallaxLayerFront())));
				
				/* New animated Enemy Sprite's */
				GestureDefence.this.newEnemyTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.sEnemyTextureRegion = TextureRegionFactory.createTiledFromAsset(newEnemyTexture, GestureDefence.this, "gfx/new_enemy.png", 0, 0, 3, 2);
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.newEnemyTexture);
				
				/* Load castle sprite */
				GestureDefence.this.mCastleTexture = new Texture(128,128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mCastleTextureRegion = TextureRegionFactory.createFromAsset(mCastleTexture, GestureDefence.this, "gfx/crappy_castle.png", 0, 0);
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.mCastleTexture);
				
				/* Load menu buttons */
				GestureDefence.this.mStartButton = new Texture(256, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mStartButtonRegion = TextureRegionFactory.createFromAsset(mStartButton, GestureDefence.this, "gfx/start_button.png", 0, 0);
				GestureDefence.this.mQuitButton = new Texture(256, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mQuitButtonRegion = TextureRegionFactory.createFromAsset(mQuitButton, GestureDefence.this, "gfx/quit_button.png", 0, 0);
				GestureDefence.this.mBuyButton = new Texture(256, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mBuyButtonRegion = TextureRegionFactory.createFromAsset(mBuyButton, GestureDefence.this, "gfx/buy_button.png", 0, 0);
				GestureDefence.this.mNextWaveButton = new Texture(256, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mNextWaveButtonRegion = TextureRegionFactory.createFromAsset(mNextWaveButton, GestureDefence.this, "gfx/next_wave_button.png", 0, 0);
				GestureDefence.this.getEngine().getTextureManager().loadTextures(GestureDefence.this.mStartButton, GestureDefence.this.mQuitButton, GestureDefence.this.mBuyButton, GestureDefence.this.mNextWaveButton);
				
				GestureDefence.this.sRemoveStuff = new EntityDetachRunnablePoolUpdateHandler();
				GestureDefence.this.sMoney = 0; //Initialise the money value, 0 for now (change this once saves working)
				GestureDefence.this.theWave = new Wave(GestureDefence.this);
				
				GestureDefence.this.sm.loadMainMenu();
			}
		}));
		
		return loadScene;
	}
	
	@Override
	public void onLoadComplete() {
	}
	
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent)
	{
		/* Menu key pressed, load in-game menu */
		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN)
		{
			return true;
		}
		else
		{
			return super.onKeyDown(pKeyCode, pEvent);
		}
	
	}
	
	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
		return true;
	}
	
	
	// ========================================
	// Methods
	// ========================================
	
	public boolean ButtonPress(int ButtonID) {
		switch(ButtonID) {
		case 1:
			/* DO some start game code at some point */			
			GestureDefence.this.sm.NewWaveScreen();
			
			GestureDefence.this.sm.NewWaveScene.registerUpdateHandler(new TimerHandler(3.0f, true, new ITimerCallback()
			{
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler)
				{
					GestureDefence.this.sm.NewWaveScene.unregisterUpdateHandler(pTimerHandler);
					GestureDefence.this.sm.GameScreen();
					GestureDefence.this.theWave.startNewWave();
				}
			}));
			return true;
		case 3:
			/* Start the next wave */
			GestureDefence.this.theWave.NextWave();
			GestureDefence.this.theWave.mWaveNumberMenuItem.setText("WAVE : " + theWave.getWaveNumber());
			GestureDefence.this.sm.NewWaveScreen();
			GestureDefence.this.sm.NewWaveScene.registerUpdateHandler(new TimerHandler(4.0f, true, new ITimerCallback()
			{
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler)
				{
					GestureDefence.this.sm.NewWaveScene.unregisterUpdateHandler(pTimerHandler);
					GestureDefence.this.sm.GameScreen();
					GestureDefence.this.theWave.startNewWave();
				}
			}));
			return true;
		case 5:
			/* Buying 100 health */
			if (sMoney - 100 >= 0)
			{
				sMoney -= 100;
				sCastle.increaseHealth(100);
				GestureDefence.this.theWave.mCashAmountItem.setText("CASH : " + sMoney);
				GestureDefence.this.theWave.mBuyMenuItem.setText("HEALTH : " + sCastle.getHealth());
				GestureDefence.this.updateCashValue();
				GestureDefence.this.updateCastleHealth();
			}
			return true;			
		case 99:
			/* Quits the Game */
			this.finish();
			return true;
		default:
			return false;
		}
	}
	
	protected MenuScene createMenuScene(int whatMenu) {
		final MenuScene menuScene = new MenuScene(GestureDefence.sCamera);
		
		if (whatMenu == 0)
		{
			final IMenuItem startMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_START, mFont, "START"), 1.0f,0.0f,0.0f,0.0f,0.0f,0.0f);
			startMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			menuScene.addMenuItem(startMenuItem);
		} else if (whatMenu == 1)
		{
			final IMenuItem restartMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_RESTART, mFont, "RESTART"), 1.0f,0.0f,0.0f,0.0f,0.0f,0.0f);
			restartMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			menuScene.addMenuItem(restartMenuItem);
		}
		
		final IMenuItem quitMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_QUIT, mFont, "QUIT"), 1.0f,0.0f,0.0f,0.0f,0.0f,0.0f);
		quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(quitMenuItem);

		menuScene.buildAnimations();
		
		menuScene.setBackgroundEnabled(false);
		
		menuScene.setOnMenuItemClickListener(this);
		return menuScene;
	}
	
	public void loadCastle(float X, float Y) {
		GestureDefence.this.sCastle = new Castle(X, Y, this.mCastleTextureRegion);
		GestureDefence.this.sCastle.setCastleBase(GestureDefence.this);
		GestureDefence.this.sm.GameScreen.attachChild(sCastle);
	}
	
	public void loadHud()
	{
		if (this.hud == null)
		{
			this.hud = new HUD();
			sCastleHealth = new ChangeableText(CAMERA_WIDTH - 100, 0 + 20, mFont, "XXXXXX", "XXXXXX".length());
			this.hud.getLastChild().attachChild(sCastleHealth);
			GestureDefence.sCamera.setHUD(hud);
			
			sMoneyText = new ChangeableText(0 + 100, 0 + 20, mFont, "" + sMoney, "XXXXXX".length());
			this.hud.getLastChild().attachChild(sMoneyText);
		}
		
		updateCastleHealth();
		updateCashValue();
	}
	
	public void loadNewEnemy(float X, float Y, int type) {
		final Enemy newEnemy = new Enemy(X, Y, GestureDefence.this.sEnemyTextureRegion.clone(), GestureDefence.this);
		/* Note the clone() above,
		 * without this all sprite's using the same texture will always be on the same frame,
		 * change one change them all
		 * This was a 3 hour bitch to find...
		 * now it creates a clone of the sprite for each enemy,
		 * this allows each enemy to be its own sprite animation! */;
		 if (type == 2)
		 {
			 newEnemy.setScale(1.5f);
		 }
		GestureDefence.this.sm.GameScreen.attachChild(newEnemy);
		GestureDefence.this.sm.GameScreen.registerTouchArea(newEnemy);
		GestureDefence.this.sm.GameScreen.setTouchAreaBindingEnabled(true);
		GestureDefence.this.sEnemyCount++;
	}
	
	public void updateCastleHealth()
	{
		sCastleHealth.setText("" + GestureDefence.this.sCastle.getHealth());
	}
	
	public void updateCashValue()
	{
		sMoneyText.setText("" + GestureDefence.this.sMoney);
	}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
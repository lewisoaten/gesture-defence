package com.gesturedefence;

/**
 * Author: Mike
 * Since: 12:43:16 - 12 Jun 2011
 */

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.AutoParallaxBackground;
import org.anddev.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.TextMenuItem;
import org.anddev.andengine.entity.scene.menu.item.decorator.ColorMenuItemDecorator;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.MathUtils;
import org.anddev.andengine.util.pool.EntityDetachRunnablePoolUpdateHandler;

import android.graphics.Color;
import android.view.KeyEvent;

import com.gesturedefence.entity.Castle;
import com.gesturedefence.entity.Enemy;

public class GestureDefence extends BaseGameActivity implements IOnMenuItemClickListener {
	// ========================================
	// Constants
	// ========================================
	
	public static final int CAMERA_WIDTH = 720;
	public static final int CAMERA_HEIGHT = 480;
	
	private static final int MENU_START = 0;
	private static final int MENU_QUIT = MENU_START + 1;
	private static final int MENU_RESTART = MENU_QUIT + 1;
	
	// ========================================
	// Fields
	// ========================================
	
	private Camera mCamera;
	
	private static Scene mMainScreen;
	
	public static EntityDetachRunnablePoolUpdateHandler RemoveStuff; //Used to safely remove Animated Sprite's (enemies)
	
	private Texture mAutoParallaxBackgroundTexture;
	
	private TextureRegion mParallaxLayerBack;
	private TextureRegion mParallaxLayerFront;
	
	private Texture mFontTexture;
	private Font mFont;
	
	protected MenuScene mMenuScene;
	protected MenuScene inGameMenu;
	
	private Texture newEnemyTexture;
	private TiledTextureRegion mEnemyTextureRegion;
	private int enemyCount = 0;
	
	private Texture mCastleTexture;
	private TextureRegion mCastleTextureRegion;
	private static ChangeableText castleHealth;
	
	private HUD hud;
	
	// ========================================
	// Constructors
	// ========================================
	
	// ========================================
	// Getter & Setter
	// ========================================
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	@Override
	public Engine onLoadEngine() {
	this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
	}
	
	@Override
	public void onLoadResources() {
		/* Load Font settings*/
		this.mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		FontFactory.setAssetBasePath("font/");
		this.mFont = FontFactory.createFromAsset(this.mFontTexture, this, "Capture it.ttf", 48, true, Color.WHITE);
		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
		this.mEngine.getFontManager().loadFont(this.mFont);
		
		/* Initialise the background images, into scrolling background (parallax background) */
		this.mAutoParallaxBackgroundTexture = new Texture(1024, 1024, TextureOptions.DEFAULT);
		this.mParallaxLayerBack = TextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "gfx/temp_background.png", 0, 0);
		this.mParallaxLayerFront = TextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this,"gfx/temp_clouds.png", 0, 650);
		
		this.mEngine.getTextureManager().loadTexture(this.mAutoParallaxBackgroundTexture);
		
		/* New animated Enemy Sprite's */
		this.newEnemyTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mEnemyTextureRegion = TextureRegionFactory.createTiledFromAsset(this.newEnemyTexture, this, "gfx/new_enemy.png", 0, 0, 3, 2);
		this.mEngine.getTextureManager().loadTexture(this.newEnemyTexture);
		
		/* Load castle sprite */
		this.mCastleTexture = new Texture(128,128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mCastleTextureRegion = TextureRegionFactory.createFromAsset(this.mCastleTexture, this, "gfx/crappy_castle.png", 0, 0);
		this.mEngine.getTextureManager().loadTexture(this.mCastleTexture);
	}
	
	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		this.mMenuScene = this.createMenuScene(0);
		this.inGameMenu = this.createMenuScene(1);
		
		//final Scene scene = new Scene(1);
		GestureDefence.mMainScreen = new Scene(1);
		
		/* Setup the scrolling background, can be removed, was just trying it out */
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerBack.getHeight(), this.mParallaxLayerBack)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, this.mParallaxLayerFront)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(35, 62, this.mParallaxLayerFront)));
		GestureDefence.mMainScreen.setBackground(autoParallaxBackground);
		/* End of scrolling Background */
		
		/* Load the main menu on startup */
		GestureDefence.mMainScreen.setChildScene(this.mMenuScene, false, false, false);
		
		RemoveStuff = new EntityDetachRunnablePoolUpdateHandler();
		GestureDefence.mMainScreen.registerUpdateHandler(RemoveStuff);
		
		return GestureDefence.mMainScreen;
		//return scene;
	}
	
	@Override
	public void onLoadComplete() {
	}
	
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent)
	{
		/* Menu key pressed, load in-game menu */
		if(pKeyCode ==KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN)
		{
			if(GestureDefence.mMainScreen.hasChildScene())
			{
				this.inGameMenu.back();
			}
			else
			{
				GestureDefence.mMainScreen.setChildScene(this.inGameMenu, false, true, true);
			}
		return true;
		}
		else
		{
			return super.onKeyDown(pKeyCode, pEvent);
		}
	
	}
	
	@Override
	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
		switch(pMenuItem.getID()) {
			case MENU_START:
				/* DO some start game code at some point */
				GestureDefence.mMainScreen.clearChildScene();
				this.enemySpawnTimeHandler(2.0f);
				this.loadCastle(CAMERA_WIDTH - (mCastleTexture.getWidth()), CAMERA_HEIGHT - 60 - mCastleTexture.getHeight());
				
				GestureDefence.mMainScreen.registerUpdateHandler(new IUpdateHandler() {
					@Override
					public void onUpdate(float pSecondsElapsed) {
						/* On every update */
					}

					@Override
					public void reset() {
						// TODO Auto-generated method stub
						
					}
				});
				
				return true;
			case MENU_QUIT:
				/* Quit's the Game */
				this.finish();
				return true;
			case MENU_RESTART:
				/* Restarts the game */
				return true;
			default:
				return false;
		}
	}
	
	
	// ========================================
	// Methods
	// ========================================
	
	protected MenuScene createMenuScene(int whatMenu) {
		final MenuScene menuScene = new MenuScene(this.mCamera);
		
		if (whatMenu == 0)
		{
			final IMenuItem startMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_START, this.mFont, "START"), 1.0f,0.0f,0.0f,0.0f,0.0f,0.0f);
			startMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			menuScene.addMenuItem(startMenuItem);
		} else if (whatMenu == 1)
		{
			final IMenuItem restartMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_RESTART, this.mFont, "RESTART"), 1.0f,0.0f,0.0f,0.0f,0.0f,0.0f);
			restartMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			menuScene.addMenuItem(restartMenuItem);
		}
		
		final IMenuItem quitMenuItem = new ColorMenuItemDecorator(new TextMenuItem(MENU_QUIT, this.mFont, "QUIT"), 1.0f,0.0f,0.0f,0.0f,0.0f,0.0f);
		quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		menuScene.addMenuItem(quitMenuItem);

		menuScene.buildAnimations();
		
		menuScene.setBackgroundEnabled(false);
		
		menuScene.setOnMenuItemClickListener(this);
		return menuScene;
	}
	
	private void loadCastle(float X, float Y) {
		final Castle mCastle = new Castle(X, Y, this.mCastleTextureRegion);
		this.mEngine.getScene().getLastChild().attachChild(mCastle);
		
		hud = new HUD();
		castleHealth = new ChangeableText(CAMERA_WIDTH - 100, 0 + 20, this.mFont, "XXXXXX", "XXXXXX".length());
		hud.getLastChild().attachChild(castleHealth);
		mCamera.setHUD(hud);
		
		updateCastleHealth();
	}
	
	private void loadNewEnemy(float X, float Y) {
		final Enemy newEnemy = new Enemy(X, Y, this.mEnemyTextureRegion.clone());
		/* Note the clone() above,
		 * without this all sprite's using the same texture will always be on the same frame,
		 * change one change them all
		 * This was a 3 hour bitch to find...
		 * now it creates a clone of the sprite for each enemy,
		 * this allows each enemy to be its own sprite animation! */
		
		this.mEngine.getScene().getLastChild().attachChild(newEnemy);
		this.mEngine.getScene().registerTouchArea(newEnemy);
		this.mEngine.getScene().setTouchAreaBindingEnabled(true);
		enemyCount++;
	}
	
	public static void updateCastleHealth()
	{
		castleHealth.setText("" + Castle.getHealth());
	}
	
	private void enemySpawnTimeHandler(float mSpawnDelay) {
		TimerHandler enemyTimerHandler;
		
		GestureDefence.mMainScreen.registerUpdateHandler(enemyTimerHandler = new TimerHandler(mSpawnDelay, true, new ITimerCallback()
		{
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler)
			{
				/* After Delay time has passed */
				if (enemyCount < 20)
				{
					final float xPos = 5;
					final float yPos = MathUtils.random(240.0f, CAMERA_HEIGHT - 32);
				
					loadNewEnemy(xPos, yPos);
				}
			}
		}));
	}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
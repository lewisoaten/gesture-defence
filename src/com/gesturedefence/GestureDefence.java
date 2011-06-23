package com.gesturedefence;

/**
 * @author Michael Watts
 * @since 12:43:16 - 12 Jun 2011
 */

import java.io.IOException;
import java.util.ArrayList;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
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
import org.anddev.andengine.entity.sprite.AnimatedSprite;
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
import org.anddev.andengine.ui.activity.LayoutGameActivity;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.util.pool.EntityDetachRunnablePoolUpdateHandler;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.widget.Toast;

import com.gesturedefence.entity.Castle;
import com.gesturedefence.entity.Enemy;
import com.gesturedefence.util.Atracker;
import com.gesturedefence.util.FileOperations;
import com.gesturedefence.util.ScreenManager;
import com.gesturedefence.util.Wave;
import com.openfeint.api.OpenFeint;
import com.openfeint.api.OpenFeintDelegate;
import com.openfeint.api.OpenFeintSettings;

public class GestureDefence extends LayoutGameActivity implements IOnMenuItemClickListener, OnGesturePerformedListener {
	// ========================================
	// Constants
	// ========================================
	
	private static int CAMERA_WIDTH = 800; //Default camera width of the window
	private static int CAMERA_HEIGHT = 480; //Default camera height of the window
	
	//buttons below, arnt really used now, may/will be removed at a later date
	public static final int MENU_HEALTH = 1;
	public static final int MENU_CASH = MENU_HEALTH + 1;
	public static final int MENU_WAVE_NUMBER = MENU_CASH + 1;
	
	// ========================================
	// Fields
	// ========================================
	
	public static Camera sCamera;
	
	public EntityDetachRunnablePoolUpdateHandler sRemoveStuff; //Used to safely remove Animated Sprite's (enemies)
	
	private Texture mAutoParallaxBackgroundTexture; //Background scrolling texture, holds each of the textures for the background
	
	private TextureRegion mParallaxLayerBack; //Back layer for Parallax background
	private TextureRegion mParallaxLayerFront; //Front layer for Parallax background
	
	public AutoParallaxBackground autoParallaxBackground; //Parallax background
	
	private Texture mFontTexture; //Font one texture
	public Font mFont; //Font one settings
	
	private Texture mFontTexture2; //Font two texture
	public Font mFont2; //Font two settings
	
	private Texture newEnemyTexture; //Enemy one Texture & region
	private TiledTextureRegion sEnemyTextureRegion;
	
	private Texture newEnemyTexture2; //Enemy two Texture & region
	private TiledTextureRegion sEnemyTextureRegion2;
	
	private Texture mCastleTexture; //Castle texture & region
	private TextureRegion mCastleTextureRegion;
	private static ChangeableText sCastleHealth; //Changeable text item for castle health
	public Castle sCastle; //Instance of custom castle entity
	
	private Texture mLightningTexture;
	private TiledTextureRegion mLightningTextureRegion;
	public AnimatedSprite lightning;
	
	private Texture mManaTexture;
	private TextureRegion mManaTextureRegion;
	
	private HUD hud; //In-game hud
	private static ChangeableText sMoneyText; //Changeable text item for Current money
	private ChangeableText sManaText; //Mana hud text
	
	public Wave theWave; // Instance of custom Wave class
	
	public boolean sEndWaveActive = false; //Remove??
	
	/* Checks for resolution and layout */
	public ScreenOrientation orientation = ScreenOrientation.LANDSCAPE;
	public boolean mCheckedRes = false; //Bah doesn't work
	
	/* These need to be reset/loaded for each game */
	public int sPreviousWaveNum = 0;
	public int sKillCount = 0;
	public int sPreviousKillCount = 0;
	public int sMoney = 0; //This is the amount of cash so far
	public int mMoneyEarned = 0; //Total cash earned throughout the game
	public int sEnemyCount = 0;
	// ------
	
	public ScreenManager sm; //Instance of custom class screenmanager
	
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
	
	/* Sounds */
	public Sound splat;
	public Sound attack;
	public Sound hurt;
	public Sound complete;
	public Sound game_over;
	public Sound ligtningStrike;
	
	/* Music */
	public Music ambient;
	
	public int mOnScreenEnemyLimit = 40; //A hard cap on the total number of enemies at any one time
	public int mOnScreenEnemies = 0; //Used with above in the Wave class
	
	protected GestureLibrary mLibrary;
	public GestureOverlayView gestures;
	public boolean mLightningBolt = false; //Used to track lightning strike
	public float mLightningBoltX; //Used to track lightning strike
	public float mLightningBoltY; //Used to track lightning stirke
	
	public int mana = 0; //Mana levels
	
	public Atracker AchieveTracker;
	
	public FileOperations fileThingy;
	
	public Handler handler = new Handler(); //Used to fix null context problems! (Toast's...I'm looking at you)
	
	public boolean mEarthquake = false; //Do an earthquake
	public boolean mEarthQuaking = false; //EarthQuake currently running!
	
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
	
	public TextureRegion getManaTextureRegion() {
		return mManaTextureRegion;
	}
	
	public int getCameraWidth() {
		return CAMERA_WIDTH;
	}
	
	public int getCameraHeight() {
		return CAMERA_HEIGHT;
	}
	
	public HUD gethud() {
			return GestureDefence.this.hud;
	}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	@Override
	protected int getLayoutID() { //Have to include, game will not function otherwise
		return R.layout.main;
	}
	
	@Override
	protected int getRenderSurfaceViewID() { //Have to include, game will not function otherwise
		return R.id.rendersurfaceview;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { //Purely to get gestures loading and detecting!
		super.onCreate(savedInstanceState);		
		GestureDefence.this.mLibrary = GestureLibraries.fromRawResource(GestureDefence.this, R.raw.spells);
		if (!mLibrary.load()) {
			Toast.makeText(this, "Failed to load gesture library", Toast.LENGTH_SHORT).show();
		};
		
		gestures = (GestureOverlayView) findViewById(R.id.gestures);
		gestures.setWillNotDraw(true);
		gestures.setWillNotCacheDrawing(true);
		gestures.addOnGesturePerformedListener(this);
		
		/* OpenFeint Setup */
		final String OFgameName = "Gesture_Defence";
		final String OFgameId = "311002";
		final String OFgameKey = "kbzd0VQKbwIcYAPF8sQRIg";
		final String OFgameSecret = "j09MMl5XeCtYZMHFRBolC8ESCB08QZVjFYBbzhgn8";
		
		OpenFeintSettings settings = new OpenFeintSettings(OFgameName, OFgameKey, OFgameSecret, OFgameId);
		OpenFeint.initialize(this, settings, new OpenFeintDelegate() {});
	}

	@Override
	public Engine onLoadEngine() {
		/* Do some quick checks to get the actual screen resolution 
		 * This code runs twice, the call at the end for a new engine
		 * causes this to run again,
		 * second time through the correct display is setup */		
		/* Set-up the game engine and default camera location */
		if (GestureDefence.this.mCheckedRes == false) //Check doesn't work??
		{
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			int width = (int) (dm.widthPixels); //Gets the actual width of the screen
			int height = (int) (dm.heightPixels); //Gets the actual height of the screen

			
			
			
			/* Check the width, if it's one of the following change the camera width for it to fit the screen */
				if (width == 320)
					GestureDefence.this.CAMERA_WIDTH = 640;
				if (width == 400)
					GestureDefence.this.CAMERA_WIDTH = 800;
				if (width == 432)
					GestureDefence.this.CAMERA_WIDTH = 864;
				if (width == 480)
					GestureDefence.this.CAMERA_WIDTH = 720;
				if (width == 800)
					GestureDefence.this.CAMERA_WIDTH = 800;
				if (width == 854)
					GestureDefence.this.CAMERA_WIDTH = 854;
				
				if (height == 1280) //Test for tablet sized devices....Performance issues with AVD's prevent decent testing!
					GestureDefence.this.CAMERA_HEIGHT = 1280;
				
			GestureDefence.this.mCheckedRes = true;//working??
		}
	GestureDefence.sCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, orientation, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), GestureDefence.sCamera).setNeedsSound(true).setNeedsMusic(true));
	}
	
	@Override
	public void onLoadResources() {
		/* Load Font settings*/
		this.mFontTexture = new Texture(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		FontFactory.setAssetBasePath("font/");
		this.mFont = FontFactory.createFromAsset(this.mFontTexture, this, "Capture it.ttf", 48, true, Color.WHITE);
		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
		this.mEngine.getFontManager().loadFont(mFont);
		
		//Then create an instance of a Screenamanger
		this.sm = new ScreenManager(this);
	}
	
	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		//Create a load screen
		final Scene loadScene = new Scene(1);
		loadScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
		
		//Show loading text on new load screen
		final Text textCenter = new Text(100, 60, this.mFont, "LOADING..", HorizontalAlign.CENTER);
		loadScene.attachChild(textCenter);
		
		//Create a timer that after 1 second  begins loading all other textures
		loadScene.registerUpdateHandler(new TimerHandler(1.0f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				loadScene.unregisterUpdateHandler(pTimerHandler); //Unload the timer, save resources and prevents running twice!
				
				/* Smaller font texture */
				GestureDefence.this.mFontTexture2 = new Texture(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				FontFactory.setAssetBasePath("font/");
				GestureDefence.this.mFont2 = FontFactory.createFromAsset(GestureDefence.this.mFontTexture2, GestureDefence.this, "Capture it.ttf", 24, true, Color.WHITE);
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.mFontTexture2);
				GestureDefence.this.getEngine().getFontManager().loadFont(mFont2);
				
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
				GestureDefence.this.sEnemyTextureRegion = TextureRegionFactory.createTiledFromAsset(newEnemyTexture, GestureDefence.this, "gfx/enemy_1.png", 0, 0, 3, 4);
				GestureDefence.this.newEnemyTexture2 = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.sEnemyTextureRegion2 = TextureRegionFactory.createTiledFromAsset(newEnemyTexture2, GestureDefence.this, "gfx/enemy_2.png", 0, 0, 3, 4);
				GestureDefence.this.getEngine().getTextureManager().loadTextures(GestureDefence.this.newEnemyTexture, GestureDefence.this.newEnemyTexture2);
				
				/* Lightning texture sprite */
				GestureDefence.this.mLightningTexture = new Texture(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mLightningTextureRegion = TextureRegionFactory.createTiledFromAsset(mLightningTexture, GestureDefence.this, "gfx/lightning.png", 0, 0, 6, 1);
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.mLightningTexture);
				
				/* Mana texture */
				GestureDefence.this.mManaTexture = new Texture(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mManaTextureRegion = TextureRegionFactory.createFromAsset(mManaTexture, GestureDefence.this, "gfx/mana.png", 0, 0);
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.mManaTexture);
				
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
				
				//Setup the castle, but don't actually attach it yet!
				GestureDefence.this.sCastle = new Castle(0, 0, GestureDefence.this.mCastleTextureRegion);
				GestureDefence.this.sCastle.setCastleBase(GestureDefence.this);
				
				GestureDefence.this.AchieveTracker = new Atracker(GestureDefence.this);
				
				GestureDefence.this.fileThingy = new FileOperations(GestureDefence.this);
				
				SoundFactory.setAssetBasePath("sfx/");
				try {
					//Sound effects
					GestureDefence.this.splat = SoundFactory.createSoundFromAsset(GestureDefence.this.getEngine().getSoundManager(), GestureDefence.this, "splat.ogg");
					GestureDefence.this.splat.setVolume(1.0f);
					GestureDefence.this.attack = SoundFactory.createSoundFromAsset(GestureDefence.this.getEngine().getSoundManager(), GestureDefence.this, "attack.ogg");
					GestureDefence.this.attack.setVolume(0.1f);
					GestureDefence.this.hurt = SoundFactory.createSoundFromAsset(GestureDefence.this.getEngine().getSoundManager(), GestureDefence.this, "hurt.ogg");
					GestureDefence.this.hurt.setVolume(0.5f);
					GestureDefence.this.complete = SoundFactory.createSoundFromAsset(GestureDefence.this.getEngine().getSoundManager(), GestureDefence.this, "complete.ogg");
					GestureDefence.this.complete.setVolume(2.0f);
					GestureDefence.this.game_over = SoundFactory.createSoundFromAsset(GestureDefence.this.getEngine().getSoundManager(), GestureDefence.this, "gameOver.ogg");
					GestureDefence.this.game_over.setVolume(5.0f);
					GestureDefence.this.ligtningStrike = SoundFactory.createSoundFromAsset(GestureDefence.this.getEngine().getSoundManager(), GestureDefence.this, "lightning.ogg");
					GestureDefence.this.ligtningStrike.setVolume(5.0f);
					
					//Music
					GestureDefence.this.ambient = MusicFactory.createMusicFromAsset(GestureDefence.this.getEngine().getMusicManager(), GestureDefence.this, "sfx/ambient.ogg");
					GestureDefence.this.ambient.setVolume(0.5f);
				} catch (final IOException e) {
					//File not found
				}
				
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
			if (GestureDefence.this.getEngine().getScene() == GestureDefence.this.sm.GameScreen)
			{ //Show pause screen (only if in game screen!)
				GestureDefence.this.sm.loadPauseScreen();
			}
			else if (GestureDefence.this.getEngine().getScene() == GestureDefence.this.sm.PauseScreen)
			{ //Close pause screen and return to game
				GestureDefence.this.sm.GameScreen();
			}			
			return true;
		}
		if (pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN)
		{ //Back key pressed!
			if (GestureDefence.this.getEngine().getScene() != GestureDefence.this.sm.MainMenu && GestureDefence.this.getEngine().getScene() != GestureDefence.this.sm.QuitMenu)
			{ //Ensure that it is not on the main menu or already on the Quit screen, Infinite loop for the fail!
				GestureDefence.this.sm.loadQuitMenu(GestureDefence.this.getEngine().getScene());
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
		return true;
	}
	
	
	// ========================================
	// Methods
	// ========================================
	
	public boolean ButtonPress(int ButtonID) { //Custom Button press function ;)
		switch(ButtonID) {
		case 1:
			//Start's a new game!			
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
			if ( (sMoney - 100 >= 0) && (GestureDefence.this.sCastle.getCurrentHealth() < GestureDefence.this.sCastle.getMaxHealth()) )
			{
				sMoney -= 100;
				sCastle.increaseHealth(100);
				GestureDefence.this.theWave.mCashAmountItem.setText("CASH : " + sMoney);
				GestureDefence.this.theWave.mBuyMenuItem.setText("HEALTH : " + GestureDefence.this.sCastle.getCurrentHealth() + "/ " + GestureDefence.this.sCastle.getMaxHealth());
				GestureDefence.this.updateCashValue();
				GestureDefence.this.updateCastleHealth();
			}
			return true;
		case 7:
			/* Buying max health increase */
			if (sMoney - 1000 >= 0)
			{
				sMoney -= 1000;
				sCastle.increaseMaxHealth(250);
				GestureDefence.this.theWave.mCashAmountItem.setText("CASH : " + sMoney);
				GestureDefence.this.theWave.mBuyMenuItem.setText("HEALTH : " + GestureDefence.this.sCastle.getCurrentHealth() + "/ " + GestureDefence.this.sCastle.getMaxHealth());
				GestureDefence.this.updateCashValue();
				GestureDefence.this.updateCastleHealth();
			}
			return true;
		case 9:
			/* Restart the game */
			GestureDefence.this.theWave.setWaveNumber(1);
			GestureDefence.this.sKillCount = 0;
			GestureDefence.this.sPreviousKillCount = 0;
			GestureDefence.this.sPreviousWaveNum = 0;
			GestureDefence.this.sMoney = 0;
			GestureDefence.this.mMoneyEarned = 0;
			GestureDefence.this.sEnemyCount = 0;
			GestureDefence.this.updateCashValue();
			GestureDefence.this.sCastle.setCurrentHealth(3000);
			GestureDefence.this.sCastle.setMaxHealth(3000);
			GestureDefence.this.updateCastleHealth();
			GestureDefence.this.mana = 0;
			GestureDefence.this.updateManaValue();
			
			/*remove all sprite's still in the game (enemies etc)
			 * This needs optimising, like making it only remove enemies!
			 */
			GestureDefence.this.sm.GameScreen.getChild(1).detachChildren();
			GestureDefence.this.sm.GameScreen.getChild(3).detachChildren();
			
			//Reload the castle, since it has now been removed
			//GestureDefence.this.loadCastle(GestureDefence.this.getCameraWidth() - (GestureDefence.this.getCastleTexture().getWidth()), GestureDefence.this.getCameraHeight() - 60 - GestureDefence.this.getCastleTexture().getHeight());
			
			GestureDefence.this.getEngine().setScene(GestureDefence.this.sm.MainMenu);
			return true;
		case 99:
			/* Quits the Game */
			this.finish();
			return true;
		default:
			return false;
		}
	}
	
	public void loadCastle(float X, float Y) { //Load the castle sprite at X/Y cords
		GestureDefence.this.sCastle.setPosition(X, Y);
		GestureDefence.this.sm.GameScreen.getChild(3).attachChild(sCastle);
	}
	
	
	
	public void loadHud()
	{ //Load the hud
		if (this.hud == null) //If hud hasn't been loaded yet, run this
		{
			this.hud = new HUD();
			sCastleHealth = new ChangeableText(CAMERA_WIDTH - 200, 0 + 20, mFont2, "XXXXXX / XXXXXX", "XXXXXX / XXXXXX".length());
			this.hud.getLastChild().attachChild(sCastleHealth);
			GestureDefence.sCamera.setHUD(hud);
			
			sMoneyText = new ChangeableText(0 + 100, 0 + 20, mFont2, "" + sMoney, "XXXXXX".length());
			this.hud.getLastChild().attachChild(sMoneyText);
			
			sManaText = new ChangeableText(sCastleHealth.getX(), sCastleHealth.getY() + sCastleHealth.getHeight(), mFont2, "XXXXXX", "XXXXXX".length());
			sManaText.setColor(0.0f, 0.0f, 0.8f);
			this.hud.getLastChild().attachChild(sManaText);
		}
		
		updateCastleHealth();
		updateCashValue();
		updateManaValue();
	}
	
	public void loadNewEnemy(float X, float Y, int type) {
		final Enemy newEnemy;
		/* Note the clone(), without this you get ISSUES! */;
		 switch(type)
		 {
		 	case 1:
		 		//Enemy type 1 (standard)
		 		newEnemy = new Enemy(X, Y, GestureDefence.this.sEnemyTextureRegion.clone(), GestureDefence.this, 1);
		 		break;
		 	case 2:
		 		//Enemy type 2
		 		newEnemy = new Enemy(X, Y, GestureDefence.this.sEnemyTextureRegion2.clone(), GestureDefence.this, 2);
				 newEnemy.setScale(1.5f);
		 		break;
		 	default:
		 		//Incase type specified is wrong, default to enemy type 1 texture
		 		newEnemy = new Enemy(X, Y, GestureDefence.this.sEnemyTextureRegion.clone(), GestureDefence.this, 1);
		 		break;
		 }
		GestureDefence.this.sm.GameScreen.getChild(1).attachChild(newEnemy); //Attach it to the screen
		GestureDefence.this.sm.GameScreen.registerTouchArea(newEnemy); //Register a touch area for the enemy
		GestureDefence.this.sm.GameScreen.setTouchAreaBindingEnabled(true); //Enable touch binding
		GestureDefence.this.sEnemyCount++; //Increase the enemy count
		GestureDefence.this.mOnScreenEnemies++;
	}
	
	public void updateCastleHealth()
	{ //Refresh's the castle's health display
		sCastleHealth.setText(GestureDefence.this.sCastle.getCurrentHealth() + " / " + GestureDefence.this.sCastle.getMaxHealth());
	}
	
	public void updateCashValue()
	{ //Refresh's the current money display
		sMoneyText.setText("" + GestureDefence.this.sMoney);
	}
	
	public void updateManaValue()
	{ //Refresh's the current money display
		sManaText.setText("" + GestureDefence.this.mana);
	}
	
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) { //Used to detect the gesture!!
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		
		if (GestureDefence.this.getEngine().getScene() == GestureDefence.this.sm.GameScreen)
		{ //Ensure that only the game scene is detecting the gestures!
			if (predictions.size() > 0) {
					if (predictions.get(0).score > 1.0)
					{
						String action = predictions.get(0).name;
						
						if ("Lightning".equals(action)) {
							if ((GestureDefence.this.mana - 1000) >= 0)
							{
								GestureDefence.this.mana -= 1000;
								GestureDefence.this.ligtningStrike.play();
								
								RectF tempThing = gesture.getBoundingBox(); //Get the bounding box of the gesture
								float posX;
								float posY;
								float lightningPosX;
								float lightningPosY;
								/* 
								 * Check the left/right and top/bottom values
								 * The returned bounding box rectangle is not aware which side is which
								 * ie: Left < Right, Top > Bottom
								 * (or in gaming case's, Top < Bottom , Y is reversed)
								 */
								
								if (tempThing.left < tempThing.right)
								{ //Work out the left hand side, the X position
									posX = tempThing.left;
									lightningPosX = posX + ((tempThing.right - tempThing.left) / 2);
								}
								else
								{ //Bounding box has right as left!
									posX = tempThing.right;
									lightningPosX = posX + ((tempThing.left - tempThing.left) / 2);
								}
								
								if (tempThing.bottom > tempThing.top) 
								{ //Take the bottom minus the height of the lightning texture (because I drew it badly 330 is roughly were the animation would stop)
									posY = tempThing.bottom - 330;
									lightningPosY = tempThing.bottom; //End of the sprite animation!
								}
								else
								{ //Bounding box has bottom as top!
									posY = tempThing.top - 330;
									lightningPosY = tempThing.top; //End of the sprite animation!
								}
								
								lightning = new AnimatedSprite(posX, posY, GestureDefence.this.mLightningTextureRegion.clone());
								lightning.animate(new long[] {50, 50, 50, 50, 50, 50}, new int[] {0, 1, 2, 3, 4, 5}, 0);
								GestureDefence.this.sm.GameScreen.attachChild(lightning);
								GestureDefence.this.mLightningBoltX = lightningPosX;
								GestureDefence.this.mLightningBoltY = lightningPosY;
								GestureDefence.this.mLightningBolt = true;
								
								GestureDefence.this.updateManaValue();
							}
						}
						
						else if ("Earthquake".equals(action)){
							if (mEarthQuaking == false && ((GestureDefence.this.mana - 500) >= 0) )
							{
								GestureDefence.this.mEarthquake = true;
								GestureDefence.this.handler.post(new Runnable() {
									public void run() {
										Toast.makeText(GestureDefence.this.getApplicationContext(), "Earthquake", Toast.LENGTH_SHORT).show();
									}
								});	
							}//end
						}
					}
			}
		}
	}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
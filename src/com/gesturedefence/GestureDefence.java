package com.gesturedefence;

/**
 * @author Michael Watts
 * @since 12:43:16 - 12 Jun 2011
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.anddev.andengine.audio.music.Music;
import org.anddev.andengine.audio.music.MusicFactory;
import org.anddev.andengine.audio.sound.Sound;
import org.anddev.andengine.audio.sound.SoundFactory;
import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.handler.timer.ITimerCallback;
import org.anddev.andengine.engine.handler.timer.TimerHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.AutoParallaxBackground;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.opengl.view.RenderSurfaceView;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.HorizontalAlign;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.gesturedefence.billing.BillingService;
import com.gesturedefence.billing.BillingService.RequestPurchase;
import com.gesturedefence.billing.BillingService.RestoreTransactions;
import com.gesturedefence.billing.PurchaseDatabase;
import com.gesturedefence.billing.PurchaseObserver;
import com.gesturedefence.billing.ResponseHandler;
import com.gesturedefence.billing.consts;
import com.gesturedefence.billing.consts.PurchaseState;
import com.gesturedefence.billing.consts.ResponseCode;
import com.gesturedefence.entity.Castle;
import com.gesturedefence.entity.Enemy;
import com.gesturedefence.entity.GoldPool;
import com.gesturedefence.entity.goldTextPool;
import com.gesturedefence.util.Atracker;
import com.gesturedefence.util.EnemyPool;
import com.gesturedefence.util.FileOperations;
import com.gesturedefence.util.HUD_revamp;
import com.gesturedefence.util.ManaPool;
import com.gesturedefence.util.Notifications;
import com.gesturedefence.util.ScreenManager;
import com.gesturedefence.util.Wave;
import com.openfeint.api.Notification;
import com.openfeint.api.Notification.Delegate;
import com.openfeint.api.OpenFeint;
import com.openfeint.api.OpenFeintDelegate;
import com.openfeint.api.OpenFeintSettings;

public class GestureDefence extends BaseGameActivity implements IOnMenuItemClickListener, OnGesturePerformedListener {
	// ========================================
	// Constants
	// ========================================
	
	private static int CAMERA_WIDTH = 800; //Default camera width of the window
	private static int CAMERA_HEIGHT = 480; //Default camera height of the window
	
	//buttons below, aren't really used now, may/will be removed at a later date
	public static final int MENU_HEALTH = 1;
	public static final int MENU_CASH = MENU_HEALTH + 1;
	public static final int MENU_WAVE_NUMBER = MENU_CASH + 1;
	
	//New enemy pool's
	private static EnemyPool ENEMY_POOL1; //Enemy type 1
	private static EnemyPool ENEMY_POOL2; //Enemy type 2
	private static ManaPool MANA_POOL; // Mana Pool
	private static GoldPool GOLD_POOL; // Gold Pool
	private static goldTextPool GOLD_TEXT_POOL; //Gold Text Pool
	
	// ========================================
	// Fields
	// ========================================
	
	public static Camera sCamera;
	
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture; //Background scrolling texture, holds each of the textures for the background
	
	private TextureRegion mParallaxLayerBack; //Back layer for Parallax background
	private TextureRegion mParallaxLayerFront; //Front layer for Parallax background
	
	public AutoParallaxBackground autoParallaxBackground; //Parallax background
	
	private BitmapTextureAtlas mFontTexture; //Font one texture
	public Font mFont; //Font one settings
	
	/* Font Texture */
	private BitmapTextureAtlas mFontTexture2; //Font two texture
	public Font mFont2; //Font two settings
	
	private BitmapTextureAtlas newEnemyTexture; //Enemy one Texture & region
	private TiledTextureRegion sEnemyTextureRegion;
	
	private BitmapTextureAtlas newEnemyTexture2; //Enemy two Texture & region
	private TiledTextureRegion sEnemyTextureRegion2;
	
	private BitmapTextureAtlas mCastleTexture; //Castle texture & region
	private TextureRegion mCastleTextureRegion;
	
	public Castle sCastle; //Instance of custom castle entity
	
	private BitmapTextureAtlas mLightningTexture;
	private TiledTextureRegion mLightningTextureRegion;
	public AnimatedSprite lightning;
	
	private BitmapTextureAtlas mManaTexture;
	private TextureRegion mManaTextureRegion;
	private BitmapTextureAtlas mGoldTexture;
	private TextureRegion mGoldTextureRegion;
	
	public Wave theWave; // Instance of custom Wave class
	
	public boolean sEndWaveActive = false; //Remove??
	
	/* Checks for resolution and layout */
	public ScreenOrientation orientation = ScreenOrientation.LANDSCAPE;
	public boolean mCheckedRes = false; //Bah doesn't work
	
	/* These need to be reset/loaded for each game */
	public int sPreviousWaveNum = 0;
	public int sKillCount = 0; //Total number of enemies killed (overall)
	public int sPreviousKillCount = 0;
	public int sMoney = 0; //This is the amount of cash so far
	public int mMoneyEarned = 0; //Total cash earned throughout the game
	public int sEnemyCount = 0; //Enemies killed this wave
	// ------
	
	public ScreenManager sm; //Instance of custom class screen manager
	
	/* New menu texture's
	 * These currently placeholder's
	 */
	private BitmapTextureAtlas mStartButton;
	private TextureRegion mStartButtonRegion;
	private BitmapTextureAtlas mQuitButton;
	private TextureRegion mQuitButtonRegion;
	private BitmapTextureAtlas mBuyButton;
	private TextureRegion mBuyButtonRegion;
	private BitmapTextureAtlas mNextWaveButton;
	private TextureRegion mNextWaveButtonRegion;
	
	/* Sounds */
	public Sound splat;
	public Sound attack;
	public Sound hurt;
	public Sound complete;
	public Sound game_over;
	public Sound lightningStrike;
	
	/* Music */
	public Music ambient;
	
	public int mOnScreenEnemyLimit = 100; //A hard cap on the total number of enemies at any one time
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
	
	public Sprite backgroundSprite1;
	public Sprite backgroundSprite2;
	public Sprite backgroundSprite3;
	
	private static final String OFgameName = "Gesture_Defence";
	private static final String OFgameId = "311002";
	private static final String OFgameKey = "kbzd0VQKbwIcYAPF8sQRIg";
	private static final String OFgameSecret = "j09MMl5XeCtYZMHFRBolC8ESCB08QZVjFYBbzhgn8";
	
	public Notifications CustomNotifications;
	public HUD_revamp CustomHUD;
	
	private boolean hasGameLoaded = false;
	private int xpProgression = 0; // Tracks XP progression!
	private ArrayList<Line> line = new ArrayList<Line>();
	
	//Test STUFF
	private static final String TAG = "GestureDefence";
	
	private static final String DB_INITIALIZED = "db_initialized";
	
	public BillingService mBillingService;
	public gesturedefencebillingPurchaseObserver mPurchaseObserver;
	public PurchaseDatabase mPurchaseDatabase;
	public Cursor mOwnedItemsCursor;
	public SimpleCursorAdapter mOwnedItemsAdapter;
	
	public Set<String> mOwnedItems = new HashSet<String>();
	
	public String mPayloadContent = null;
	
	public static final int DIALOG_CANNOT_CONNECT_ID = 1;
	public static final int DIALOG_BILLING_NOT_SUPPORTED_ID = 2;
	
	public enum Managed { MANAGED, UNMANAGED }
	
	public static final CatalogEntry[] CATALOG = new CatalogEntry[] {
        new CatalogEntry("android.test.purchased", R.string.android_test_purchased,
                Managed.UNMANAGED),
        new CatalogEntry("android.test.canceled", R.string.android_test_canceled,
                Managed.UNMANAGED),
        new CatalogEntry("android.test.refunded", R.string.android_test_refunded,
                Managed.UNMANAGED),
        new CatalogEntry("android.test.item_unavailable", R.string.android_test_item_unavailable,
                Managed.UNMANAGED),
	};
	
	public String mItemName;
	public String mSku;
	public CatalogAdapter mCatalogAdapter;
	
	private ListView mOwnedItemstable;
	private FrameLayout.LayoutParams OwnedItemsLayoutParams;
	
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
	
	public TextureRegion getGoldTextureRegion() {
		return mGoldTextureRegion;
	}
	
	public int getCameraWidth() {
		return CAMERA_WIDTH;
	}
	
	public void setCameraWidth(int amount) {
		CAMERA_WIDTH = amount;
	}
	
	public void setCameraHeight(int amount) {
		CAMERA_HEIGHT = amount;
	}
	
	public int getCameraHeight() {
		return CAMERA_HEIGHT;
	}
	
	public Camera getCamera() {
		return sCamera;
	}
	
	public void setManaPool(ManaPool thePool) {
		MANA_POOL = thePool;
	}
	
	public ManaPool getManaPool() {
		return MANA_POOL;
	}
	
	public void setGoldPool(GoldPool thePool) {
		GOLD_POOL = thePool;
	}
	
	public GoldPool getGoldPool() {
		return GOLD_POOL;
	}
	
	public void setGoldTextPool(goldTextPool thePool) {
		GOLD_TEXT_POOL = thePool;
	}
	
	public goldTextPool getGoldTextPool() {
		return GOLD_TEXT_POOL;
	}
	
	public void setEnemyPool(int type, EnemyPool thePool) {
		if (type == 1)
			ENEMY_POOL1 = thePool;
		if (type == 2)
			ENEMY_POOL2 = thePool;
	}
	
	public EnemyPool getEnemyPool(int type) {
		if (type == 1)
			return ENEMY_POOL1;
		if (type == 2)
			return ENEMY_POOL2;
		return ENEMY_POOL1; // Return deafult 1 if doesn't match (Prevent errors)
	}
	
	public int getXpProgress() {
		return this.xpProgression;
	}
	
	public void increaseXpProgress (int amount) {
		this.xpProgression += amount;
	}
	
	public void setXpProgress (int amount) { //used to set up xp progress (file load etc)
		this.xpProgression = amount;
	}
	
	public void increaseGold (int amount) { //Increases the total gold earned!
		this.sMoney += amount;
	}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	@Override
	protected void onSetContentView() {
		final FrameLayout frameLayout = new FrameLayout(this);
		final FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		
		this.gestures = new GestureOverlayView(this);
		this.gestures.setEventsInterceptionEnabled(false);
		this.gestures.setGestureStrokeType(2);
		final FrameLayout.LayoutParams gestureViewLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		
		
		this.mRenderSurfaceView = new RenderSurfaceView(this);
		mRenderSurfaceView.setRenderer(mEngine);
		
		this.mOwnedItemstable = new ListView(this);
		OwnedItemsLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.RIGHT);
		int height = 100;
		OwnedItemsLayoutParams.topMargin = height / 2;
		OwnedItemsLayoutParams.width = 200;
		mOwnedItemstable.setVisibility(mOwnedItemstable.VISIBLE);
		
		final android.widget.FrameLayout.LayoutParams surfaceViewLayoutParams = new FrameLayout.LayoutParams(super.createSurfaceViewLayoutParams());
		
		frameLayout.addView(this.gestures, gestureViewLayoutParams);
		frameLayout.addView(this.mRenderSurfaceView, surfaceViewLayoutParams);
		frameLayout.addView(this.mOwnedItemstable, OwnedItemsLayoutParams);
		
		this.setContentView(frameLayout, frameLayoutParams);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { //Setup a few onCreate items
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) //Restore any instance saved state information!
		{
			GestureDefence.this.mCheckedRes = savedInstanceState.getBoolean("ResCheck");
			GestureDefence.this.setCameraHeight(savedInstanceState.getInt("screenHeight"));
			GestureDefence.this.setCameraWidth(savedInstanceState.getInt("screenWidth"));
		}
		
		GestureDefence.this.mLibrary = GestureLibraries.fromRawResource(GestureDefence.this, R.raw.spells);
		if (!mLibrary.load()) {
			Toast.makeText(this, "Failed to load gesture library", Toast.LENGTH_SHORT).show();
		};
		
		//gestures = (GestureOverlayView) findViewById(R.id.gestures);
		gestures.setWillNotDraw(true);
		gestures.setWillNotCacheDrawing(true);
		gestures.addOnGesturePerformedListener(this);
		
		//Set the custom notification system up
		CustomNotifications = new Notifications(GestureDefence.this);
		
		/* Setup Billing */
		mPurchaseObserver = new gesturedefencebillingPurchaseObserver(handler); //Slight pause?
		mBillingService = new BillingService(GestureDefence.this);
		mBillingService.setContext(GestureDefence.this);

		mPurchaseDatabase = new PurchaseDatabase(GestureDefence.this);
		setupBillingBits();

		ResponseHandler.register(mPurchaseObserver);
		if (!mBillingService.checkBillingSupported()) {
			CustomNotifications.addNotification("BILLING NOT SUPPORTED, SAY WHAT!!!!?");
		} else {
			CustomNotifications.addNotification("BILLING is supported! WOO!");
		}
		
		/* Set-up the game engine and default camera location */
		if (!GestureDefence.this.mCheckedRes) //Check doesn't work??
		{
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			int width = (int) (dm.widthPixels); //Gets the actual width of the screen
			int height = (int) (dm.heightPixels); //Gets the actual height of the screen
			
			/* Check the width, if it's one of the following change the camera width for it to fit the screen */
			if (width == 320)
				GestureDefence.this.setCameraWidth(640);
			if (width == 400)
				GestureDefence.this.setCameraWidth(800);
			if (width == 432)
				GestureDefence.this.setCameraWidth(864);
			if (width == 480)
				GestureDefence.this.setCameraWidth(720);
			if (width == 800)
				GestureDefence.this.setCameraWidth(800);
			if (width == 854)
				GestureDefence.this.setCameraWidth(854);
			
			if (height == 1280) //Test for tablet sized devices....Performance issues with AVD's prevent decent testing!
				GestureDefence.this.setCameraHeight(1280); //Camera Height? ..Needs testing :)
				
			GestureDefence.this.mCheckedRes = true;
		}		
	}

	@Override
	public Engine onLoadEngine() {
		GestureDefence.sCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new Engine(new EngineOptions(true, orientation, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), GestureDefence.sCamera).setNeedsSound(true).setNeedsMusic(true));
	}
	
	@Override
	public void onLoadResources() {
		/* Load Font settings*/
		this.mFontTexture = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mFontTexture2 = new BitmapTextureAtlas(512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		/* Setup Fonts */
		FontFactory.setAssetBasePath("font/");
		this.mFont = FontFactory.createFromAsset(this.mFontTexture, this, "Capture it.ttf", 48, true, Color.WHITE);
		this.mFont2 = FontFactory.createFromAsset(GestureDefence.this.mFontTexture2, GestureDefence.this, "Capture it.ttf", 24, true, Color.WHITE);
		this.getEngine().getTextureManager().loadTextures(GestureDefence.this.mFontTexture, GestureDefence.this.mFontTexture2);
		this.getEngine().getFontManager().loadFonts(mFont, mFont2);
		
		//Then create an instance of a Screenmanager
		this.sm = new ScreenManager(this);
	}
	
	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		//Create a load screen
		final Scene loadScene = new Scene();
		loadScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
		
		//Show loading text on new load screen
		final Text textCenter = new Text(100, 60, this.mFont, "LOADING..", HorizontalAlign.CENTER);
		loadScene.attachChild(textCenter);
		
		CustomHUD = new HUD_revamp(GestureDefence.this);
		GestureDefence.this.fileThingy = new FileOperations(GestureDefence.this); //Initialise first (for file operations..else crashes ensue!
		
		/* OpenFeint Setup */		
		OpenFeintSettings settings = new OpenFeintSettings(OFgameName, OFgameKey, OFgameSecret, OFgameId);
		OpenFeint.initializeWithoutLoggingIn(GestureDefence.this, settings, new OpenFeintDelegate() {});
		
		hasGameLoaded = true;
		
		//Override openfeint notifications
		Notification.setDelegate(new Delegate() {
			@Override
			public boolean canShowNotification(Notification notification) {
				return false; //Overrides the openFeint notifications
			}
			@Override
			public void displayNotification(Notification notification) {
				GestureDefence.this.CustomNotifications.addNotification(notification.getText()); //Sends the message to the custom method
			}
		});
		
		//Create a timer that after 1 second  begins loading all other textures
		loadScene.registerUpdateHandler(new TimerHandler(1.0f, true, new ITimerCallback() {
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler) {
				loadScene.unregisterUpdateHandler(pTimerHandler); //Unload the timer, save resources and prevents running twice!
				
				/* Initialise the background images, into scrolling background (parallax background) */
				GestureDefence.this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(1024, 1024, TextureOptions.DEFAULT);
				GestureDefence.this.setParallaxLayerBack(BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture, GestureDefence.this, "gfx/temp_background.png", 0, 0));
				GestureDefence.this.setParallaxLayerFront(BitmapTextureAtlasTextureRegionFactory.createFromAsset(mAutoParallaxBackgroundTexture, GestureDefence.this,"gfx/temp_clouds.png", 0, 650));
				
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.mAutoParallaxBackgroundTexture);
				
				GestureDefence.this.autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
				GestureDefence.this.backgroundSprite1 = new Sprite(0, getCameraHeight() - getParallaxLayerBack().getHeight(), getParallaxLayerBack());
				GestureDefence.this.backgroundSprite2 = new Sprite(0, 80, getParallaxLayerFront());
				GestureDefence.this.backgroundSprite3 = new Sprite(35, 62, getParallaxLayerFront());
				GestureDefence.this.autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, GestureDefence.this.backgroundSprite1));
				GestureDefence.this.autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, GestureDefence.this.backgroundSprite2));
				GestureDefence.this.autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, GestureDefence.this.backgroundSprite3));
				
				/* New animated Enemy Sprite's */
				GestureDefence.this.newEnemyTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.sEnemyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(newEnemyTexture, GestureDefence.this, "gfx/enemy_1.png", 0, 0, 3, 4);
				GestureDefence.this.newEnemyTexture2 = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.sEnemyTextureRegion2 = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(newEnemyTexture2, GestureDefence.this, "gfx/enemy_2.png", 0, 0, 3, 4);
				GestureDefence.this.getEngine().getTextureManager().loadTextures(GestureDefence.this.newEnemyTexture, GestureDefence.this.newEnemyTexture2);
				
				/* Lightning texture sprite */
				GestureDefence.this.mLightningTexture = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mLightningTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mLightningTexture, GestureDefence.this, "gfx/lightning.png", 0, 0, 6, 1);
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.mLightningTexture);
				
				/* Mana texture */
				GestureDefence.this.mManaTexture = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mManaTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mManaTexture, GestureDefence.this, "gfx/mana.png", 0, 0);
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.mManaTexture);
				
				/* Gold Texture */
				GestureDefence.this.mGoldTexture = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mGoldTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mGoldTexture, GestureDefence.this, "gfx/gold.png", 0, 0);
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.mGoldTexture);
				
				/* Load castle sprite */
				GestureDefence.this.mCastleTexture = new BitmapTextureAtlas(128,128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mCastleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mCastleTexture, GestureDefence.this, "gfx/crappy_castle.png", 0, 0);
				GestureDefence.this.getEngine().getTextureManager().loadTexture(GestureDefence.this.mCastleTexture);
				
				/* Load menu buttons */
				GestureDefence.this.mStartButton = new BitmapTextureAtlas(256, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mStartButtonRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mStartButton, GestureDefence.this, "gfx/start_button.png", 0, 0);
				GestureDefence.this.mQuitButton = new BitmapTextureAtlas(256, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mQuitButtonRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mQuitButton, GestureDefence.this, "gfx/quit_button.png", 0, 0);
				GestureDefence.this.mBuyButton = new BitmapTextureAtlas(256, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mBuyButtonRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuyButton, GestureDefence.this, "gfx/buy_button.png", 0, 0);
				GestureDefence.this.mNextWaveButton = new BitmapTextureAtlas(256, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
				GestureDefence.this.mNextWaveButtonRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mNextWaveButton, GestureDefence.this, "gfx/next_wave_button.png", 0, 0);
				GestureDefence.this.getEngine().getTextureManager().loadTextures(GestureDefence.this.mStartButton, GestureDefence.this.mQuitButton, GestureDefence.this.mBuyButton, GestureDefence.this.mNextWaveButton);
				
				GestureDefence.this.sMoney = 0; //Initialise the money value, 0 for now
				GestureDefence.this.theWave = new Wave(GestureDefence.this);
				
				//Setup the castle, but don't actually attach it yet!
				GestureDefence.this.sCastle = new Castle(0, 0, GestureDefence.this.mCastleTextureRegion);
				GestureDefence.this.sCastle.setCastleBase(GestureDefence.this);
				
				GestureDefence.this.handler.post(new Runnable()
				{
					public void run() {
						GestureDefence.this.AchieveTracker = new Atracker(GestureDefence.this); //Offload it so it doesn't cause a slight jitter as it connects?
						GestureDefence.this.AchieveTracker.loadAchievements();
					}
				});
				
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
					GestureDefence.this.lightningStrike = SoundFactory.createSoundFromAsset(GestureDefence.this.getEngine().getSoundManager(), GestureDefence.this, "lightning.ogg");
					GestureDefence.this.lightningStrike.setVolume(5.0f);
					
					//Music
					GestureDefence.this.ambient = MusicFactory.createMusicFromAsset(GestureDefence.this.getEngine().getMusicManager(), GestureDefence.this, "sfx/ambient.ogg");
					GestureDefence.this.ambient.setVolume(0.5f);
				} catch (final IOException e) {
					//File not found
				}
				
				GestureDefence.this.setEnemyPool(1, new EnemyPool(GestureDefence.this.sEnemyTextureRegion, GestureDefence.this));
				GestureDefence.this.setEnemyPool(2, new EnemyPool(GestureDefence.this.sEnemyTextureRegion2, GestureDefence.this));
				GestureDefence.this.setManaPool(new ManaPool(GestureDefence.this.mManaTextureRegion, GestureDefence.this));
				GestureDefence.this.setGoldPool(new GoldPool(GestureDefence.this.mGoldTextureRegion, GestureDefence.this));
				GestureDefence.this.setGoldTextPool(new goldTextPool(GestureDefence.this));
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
	
	@Override
	protected void onStart() {
		super.onStart();
		ResponseHandler.register(mPurchaseObserver);
		initializeOwnedItems();
	}
	
	@Override
	protected void onResume() {
		soundsCheck(true); //Resume sound effects
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		soundsCheck(false); //Pause sounds
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		ResponseHandler.unregister(mPurchaseObserver);
		if (hasGameLoaded)
			GestureDefence.this.fileThingy.saveAchieveProgress();
	}
	
	@Override
	protected void onDestroy() {
		mPurchaseDatabase.close();
		mBillingService.unbind();
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		/**
		 * Save any information that would be required for application back response!
		 * Needed for when the app loses focus or force closed by android os
		 */
		boolean ResChecked = GestureDefence.this.mCheckedRes;
		int screenWidth = GestureDefence.this.getCameraWidth();
		int screenHeight = GestureDefence.this.getCameraHeight();
		
		outState.putBoolean("ResCheck", ResChecked);
		outState.putInt("screenWidth", screenWidth);
		outState.putInt("screenHeight", screenHeight);
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
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
				GestureDefence.this.CustomHUD.updateCashValue();
				GestureDefence.this.CustomHUD.updateCastleHealth();
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
				GestureDefence.this.CustomHUD.updateCashValue();
				GestureDefence.this.CustomHUD.updateCastleHealth();
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
			GestureDefence.this.CustomHUD.updateCashValue();
			GestureDefence.this.sCastle.setCurrentHealth(3000);
			GestureDefence.this.sCastle.setMaxHealth(3000);
			GestureDefence.this.CustomHUD.updateCastleHealth();
			GestureDefence.this.mana = 0;
			GestureDefence.this.CustomHUD.updateManaValue();
			
			/*remove all sprite's still in the game (enemies etc)
			 * This needs optimising, like making it only remove enemies!
			 */
			//GestureDefence.this.sm.GameScreen.getChild(1).detachChildren();
			GestureDefence.this.mOnScreenEnemies = 0;
			clearGameScene();
			//GestureDefence.this.sm.GameScreen.getChild(3).detachChildren();
			
			GestureDefence.this.sm.loadMainMenu();
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
		GestureDefence.this.sm.GameScreen.attachChild(sCastle);
	}
	
	public void loadNewEnemy(final float X, final float Y, final int type) {
		final Enemy newEnemy;
		
		switch(type)
		{
		case 1:
			//Enemy type 1 (standard)
			newEnemy = GestureDefence.this.getEnemyPool(1).obtainPoolItem();
			newEnemy.setXY(X, Y);
			newEnemy.setType1();
			break;
		case 2:
			//Enemy type 2
			newEnemy = GestureDefence.this.getEnemyPool(2).obtainPoolItem();
			newEnemy.setXY(X, Y);
			newEnemy.setType2();
			newEnemy.setScale(1.5f);
			break;
		default:
			//If type specified is wrong, default to enemy type 1 texture
			newEnemy = GestureDefence.this.getEnemyPool(1).obtainPoolItem();
			newEnemy.setXY(X, Y);
			newEnemy.setType1();
			break;
		}
		
		if (!newEnemy.hasParent())
			GestureDefence.this.sm.GameScreen.attachChild(newEnemy); //Attach it to the screen
		if (!newEnemy.isVisible())
			newEnemy.setVisible(true);
		GestureDefence.this.sm.GameScreen.registerTouchArea(newEnemy); //Register a touch area for the enemy
		GestureDefence.this.sm.GameScreen.setTouchAreaBindingEnabled(true); //Enable touch binding
		GestureDefence.this.sEnemyCount++; //Increase the enemy count
		GestureDefence.this.mOnScreenEnemies++;
	}
	
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) { //Used to detect the gesture!!
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		boolean isItLightning = false;
		
		if (GestureDefence.this.getEngine().getScene() == GestureDefence.this.sm.GameScreen)
		{ //Ensure that only the game scene is detecting the gestures!
			/* Draw the gesture! */
			int i = 0;
			boolean something = true;
			while (something) {
			try
			{
				final float x1 = gesture.getStrokes().get(0).points[i];
				final float y1 = gesture.getStrokes().get(0).points[i + 1];
				final float x2 = gesture.getStrokes().get(0).points[i + 2];
				final float y2 = gesture.getStrokes().get(0).points[i + 3];
				
				final Line tempLine = new Line(x1, y1, x2, y2, 5);
				
				GestureDefence.this.line.add(tempLine);
				
				GestureDefence.this.sm.GameScreen.attachChild(tempLine);
				i += 2; //Makes sure the starting point of the next line is the end point of the previous line
			}
			catch (Throwable e)
			{ //End of drawable cords ;)
				something = false;
				}
			}
			
			GestureDefence.this.sm.GameScreen.registerUpdateHandler(new TimerHandler(1, false, new ITimerCallback()
			{ //Timer settings,
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler)
				{
					for (int i = (GestureDefence.this.line.size() - 1); i >= 0; i --) {
						final Line tempLine = GestureDefence.this.line.get(i);
						GestureDefence.this.sm.GameScreen.detachChild(tempLine);
					}
					GestureDefence.this.line.clear();
					GestureDefence.this.sm.GameScreen.unregisterUpdateHandler(pTimerHandler);
				}
			}));
			
			if (predictions.size() > 0) {
					if (predictions.get(0).score > 1.0)
					{
						String action = predictions.get(0).name;
						
						/* Check the gesture dimensions
						 * We need to decide whether it's a lightning strike (top to bottom, left/right is small)
						 * or Earthquake (Left to right, WIDE)
						 */
						
						RectF GestureCheck = gesture.getBoundingBox();
						
						if (GestureCheck.left < GestureCheck.right)
						{
							if (GestureCheck.right - GestureCheck.left < 200)
								isItLightning = true;
						}
						else if (GestureCheck.right < GestureCheck.left)
							if (GestureCheck.left - GestureCheck.right < 200)
								isItLightning = true;
						
						//Check the action name
						if (isItLightning)
						{
							if (predictions.get(0).name == "Lightning")
								action = predictions.get(0).name;
						}
						else
						{
							for (int blah = 0; blah < predictions.size() - 1; blah++)
							{
								if (predictions.get(blah).name == "Earthquake")
								{
									action = predictions.get(blah).name;
									blah = predictions.size(); //Breaks it out quicker
								}
							}
						}
						
						if ("Lightning".equals(action)) {
							if ((GestureDefence.this.mana - 1000) >= 0)
							{
								GestureDefence.this.mana -= 1000;
								GestureDefence.this.lightningStrike.play();
								
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
								
								lightning = new AnimatedSprite(posX, posY, GestureDefence.this.mLightningTextureRegion.deepCopy());
								lightning.animate(new long[] {50, 50, 50, 50, 50, 50}, new int[] {0, 1, 2, 3, 4, 5}, 0);
								GestureDefence.this.sm.GameScreen.attachChild(lightning);
								GestureDefence.this.mLightningBoltX = lightningPosX;
								GestureDefence.this.mLightningBoltY = lightningPosY;
								GestureDefence.this.mLightningBolt = true;
								
								GestureDefence.this.CustomHUD.updateManaValue();
							}
						}
						
						else if ("Earthquake".equals(action)){
							if (mEarthQuaking == false && ((GestureDefence.this.mana - 500) >= 0) )
							{
								GestureDefence.this.mEarthquake = true;
								GestureDefence.this.handler.post(new Runnable() {
									public void run() {
										CustomNotifications.addNotification("Earthquake");
									}
								});	
							}//end
						}
					}
			}
		}
	}
	
	private void restoreDatabase() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean initialized = prefs.getBoolean(DB_INITIALIZED, false);
        if (!initialized) {
            mBillingService.restoreTransactions();
            CustomNotifications.addNotification("Restoring Transaction?");
        }
    }
	
	private void prependLogEntry(CharSequence cs) {
        SpannableStringBuilder contents = new SpannableStringBuilder(cs);
        contents.append('\n');
        //contents.append(mLogTextView.getText());
        //mLogTextView.setText(contents);
    }
	
	private void logProductActivity(String product, String activity) {
        SpannableStringBuilder contents = new SpannableStringBuilder();
        contents.append(Html.fromHtml("<b>" + product + "</b>: "));
        contents.append(activity);
        prependLogEntry(contents);
    }
	
	private void initializeOwnedItems() {
		new Thread(new Runnable() {
			public void run() {
				doInitializeOwnedItems();
			}
		}).start();
	}
	
	private void doInitializeOwnedItems() {
		Cursor cursor = mPurchaseDatabase.queryAllPurchasedItems();
		if (cursor == null) {
			return;
		}
		
		final Set<String> ownedItems = new HashSet<String>();
		try {
			int productIdCol = cursor.getColumnIndexOrThrow(PurchaseDatabase.PURCHASED_PRODUCT_ID_COL);
			while (cursor.moveToNext()) {
				String productId = cursor.getString(productIdCol);
				ownedItems.add(productId);
			}
		} finally {
			cursor.close();
		}
		
		//Add set of owned items in a new runnable that runs on the Main Thread.
		handler.post(new Runnable() {
			public void run() {
				mOwnedItems.addAll(ownedItems);
				mCatalogAdapter.setOwnedItems(mOwnedItems);
			}
		});
	}
	
	private void setupBillingBits() {
		mCatalogAdapter = new CatalogAdapter(this, CATALOG);
		mOwnedItemsCursor = mPurchaseDatabase.queryAllPurchasedItems();
		startManagingCursor(mOwnedItemsCursor);
		String[] from = new String[] { PurchaseDatabase.PURCHASED_PRODUCT_ID_COL, PurchaseDatabase.PURCHASED_QUANTITY_COL};
		int[] to = new int[] { 0 };
		mOwnedItemsAdapter = new SimpleCursorAdapter(this, 1, mOwnedItemsCursor, from, to);
		mOwnedItemstable.setAdapter(mOwnedItemsAdapter);
	}
	
	/**
	 * Simple check to see if there are any network connections available.
	 * Returns true if there is at least one available or soon to be available (connecting).
	 * Returns false if there are no networks to be found or connected.
	 */
	public boolean isOnline() {
		ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting() ) {
			return true;
		}
		return false;
	}
	
	/**
	 * Used to mute/unmute sound effects when the app goes into pause mode (phone call etc)
	 * @param state - True = sounds on. False = sounds off!
	 */
	public void soundsCheck(boolean state) {
		/*
		 * List of sound effects to check
		 * -- Sounds --
		 * splat - Enemy death
		 * complete - Level complete
		 * attack - enemy attack castle
		 * hurt - hurt enemy (trip etc)
		 * game_over - game failed
		 * lightningStrike - lightning strike sound
		 * 
		 * -- Music --
		 * ambient - Backgroud music
		 */
		if (state)
		{
			if (GestureDefence.this.splat != null)
				GestureDefence.this.splat.resume();
			if (GestureDefence.this.complete != null)
				GestureDefence.this.complete.resume();
			if (GestureDefence.this.attack != null)
				GestureDefence.this.attack.resume();
			if (GestureDefence.this.hurt != null)
				GestureDefence.this.hurt.resume();
			if (GestureDefence.this.game_over != null)
				GestureDefence.this.game_over.resume();
			if (GestureDefence.this.lightningStrike != null)
				GestureDefence.this.lightningStrike.resume();
			if (GestureDefence.this.ambient != null)
				GestureDefence.this.ambient.resume();
		}
		else
		{
			if (GestureDefence.this.splat != null)
				GestureDefence.this.splat.pause();
			if (GestureDefence.this.complete != null)
				GestureDefence.this.complete.pause();
			if (GestureDefence.this.attack != null)
				GestureDefence.this.attack.pause();
			if (GestureDefence.this.hurt != null)
				GestureDefence.this.hurt.pause();
			if (GestureDefence.this.game_over != null)
				GestureDefence.this.game_over.pause();
			if (GestureDefence.this.lightningStrike != null)
				GestureDefence.this.lightningStrike.pause();
			if (GestureDefence.this.ambient != null)
				GestureDefence.this.ambient.pause();
		}
	}
	
	
	public void clearGameScene() { // Remove's all attached children BUT the castle
		int numOfChildren = GestureDefence.this.sm.GameScreen.getChildCount();
		
		for (int i = numOfChildren-1; i > 0; i --) {
			if (GestureDefence.this.sm.GameScreen.getChild(i) == sCastle) {
				//Do Nothing! (I think!)
			}
			else {
				GestureDefence.this.sm.GameScreen.detachChild(GestureDefence.this.sm.GameScreen.getChild(i));
			}
		}
	}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
	
	private class gesturedefencebillingPurchaseObserver extends PurchaseObserver {
		public gesturedefencebillingPurchaseObserver(Handler handler) {
			super(GestureDefence.this, handler);
		}
		
		@Override
		public void onBillingSupported(boolean supported) {
			if (consts.DEBUG) {
				Log.i(TAG, "supported: " + supported);
			}
			if (supported) {
				restoreDatabase();
				//Some button setup
			} else {
				showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
			}
		}
		
		@Override
		public void onPurchaseStateChange(PurchaseState purchaseState, String itemId, int quantity, long purchaseTime, String developerPayLoad) {
			if (consts.DEBUG) {
				Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
			}

			if (developerPayLoad == null) {
				logProductActivity(itemId, purchaseState.toString());
			} else {
				logProductActivity(itemId, purchaseState + "\n\t" + developerPayLoad);
			}

			if (purchaseState == PurchaseState.PURCHASED) {
				mOwnedItems.add(itemId);
			}
			mCatalogAdapter.setOwnedItems(mOwnedItems);
			mOwnedItemsCursor.requery();
		}
		
		@Override
		public void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
			if (consts.DEBUG) {
				Log.d(TAG, request.mProductID + ": " + responseCode);
			}
			if (responseCode == ResponseCode.RESULT_OK) {
				if (consts.DEBUG) {
					Log.i(TAG, "purchase was successfully sent to server");
				}
				logProductActivity(request.mProductID, "send purchase request");
			} else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
				if (consts.DEBUG) {
					Log.i(TAG, "user canceled purchase");
				}
				logProductActivity(request.mProductID, "dismissed pruchase dialog");
			}  else {
                if (consts.DEBUG) {
                    Log.i(TAG, "purchase failed");
                }
                logProductActivity(request.mProductID, "request purchase returned " + responseCode);
            }
        }

        @Override
        public void onRestoreTransactionsResponse(RestoreTransactions request,
                ResponseCode responseCode) {
            if (responseCode == ResponseCode.RESULT_OK) {
                if (consts.DEBUG) {
                    Log.d(TAG, "completed RestoreTransactions request");
                }
                // Update the shared preferences so that we don't perform
                // a RestoreTransactions again.
                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(DB_INITIALIZED, true);
                edit.commit();
            } else {
                if (consts.DEBUG) {
                    Log.d(TAG, "RestoreTransactions error: " + responseCode);
                }
            }
		}
	}
	
	public static class CatalogEntry {
		public String sku;
		public int nameId;
		public Managed managed;
		
		public CatalogEntry(String sku, int nameId, Managed managed) {
			this.sku = sku;
			this.nameId = nameId;
			this.managed = managed;
		}
	}
	
	/**
     * An adapter used for displaying a catalog of products.  If a product is
     * managed by Android Market and already purchased, then it will be "grayed-out" in
     * the list and not selectable.
     */
    private static class CatalogAdapter extends ArrayAdapter<String> {
        private CatalogEntry[] mCatalog;
        private Set<String> mOwnedItems = new HashSet<String>();

        public CatalogAdapter(Context context, CatalogEntry[] catalog) {
            super(context, android.R.layout.simple_spinner_item);
            mCatalog = catalog;
            for (CatalogEntry element : catalog) {
                add(context.getString(element.nameId));
            }
            //setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        public void setOwnedItems(Set<String> ownedItems) {
            mOwnedItems = ownedItems;
            notifyDataSetChanged();
        }

        @Override
        public boolean areAllItemsEnabled() {
            // Return false to have the adapter call isEnabled()
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            // If the item at the given list position is not purchasable,
            // then prevent the list item from being selected.
            CatalogEntry entry = mCatalog[position];
            if (entry.managed == Managed.MANAGED && mOwnedItems.contains(entry.sku)) {
                return false;
            }
            return true;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            // If the item at the given list position is not purchasable, then
            // "gray out" the list item.
            View view = super.getDropDownView(position, convertView, parent);
            view.setEnabled(isEnabled(position));
            return view;
        }
    }
}
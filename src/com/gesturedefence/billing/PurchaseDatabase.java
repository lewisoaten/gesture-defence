package com.gesturedefence.billing;

/**
 * @author Michael Watts
 * @since 16:21:47 - 27 Jun 2011
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gesturedefence.billing.consts.PurchaseState;

/* This is taken from the example for in-app billing
 * NOTE: WE NEED TO USE AN OBFUSCATOR AND ENCRYPT THE DATABASE WITH A USER SPECIFIC KEY 
 * A database that records the state of each purchase.*/
public class PurchaseDatabase {
	// ========================================
	// Constants
	// ========================================
		private static final String TAG = "PurchaseDatabase";
		private static final String DATABASE_NAME = "purchase.db";
		private static final int DATABASE_VERSION = 1;
		private static final String PURCHASE_HISTORY_TABLE_NAME = "history";
		private static final String PURCHASED_ITEMS_TABLE_NAME = "purchased";
	
		/* These are the column names for the purchase history table. We need a 
		 * column named "_id" if we want to use a CursorAdapter. The primary key is the orderId
		 * so that we can be robust against getting multiple messages from the server for the same purchase.*/
		static final String HISTORY_ORDER_ID_COL = "_id";
		static final String HISTORY_STATE_COL = "state";
		static final String HISTORY_PRODUCT_ID_COL = "productId";
		static final String HISTORY_PURCHASE_TIME_COL = "purchaseTime";
		static final String HISTORY_DEVELOPER_PAYLOAD_COL = "developerPayLoad";
	
		private static final String[] HISTORY_COLUMNS = {HISTORY_ORDER_ID_COL, HISTORY_PRODUCT_ID_COL, HISTORY_STATE_COL,
			HISTORY_PURCHASE_TIME_COL, HISTORY_DEVELOPER_PAYLOAD_COL};
	
		/* These are the column names for the "purchased items" table.*/
		static final String PURCHASED_PRODUCT_ID_COL = "_id";
		static final String PURCHASED_QUANTITY_COL = "quantity";
	
		private static final String[] PURCHASED_COLUMNS = {PURCHASED_PRODUCT_ID_COL, PURCHASED_QUANTITY_COL};
		
	// ========================================
	// Fields
	// ========================================
	
		private SQLiteDatabase mDb;
		private DatabaseHelper mDatabaseHelper;
		
	// ========================================
	// Constructors
	// ========================================
	
		public PurchaseDatabase(Context context) {
			mDatabaseHelper = new DatabaseHelper(context);
			mDb = mDatabaseHelper.getWritableDatabase();
		}
	
		public void close() {
			mDatabaseHelper.close();
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
	
		/* Inserts a purchased product into the database. There may be multiple rows in the table for the same product
		 * if it was purchased multiple times or if it was refunded. */
		private void insertOrder(String orderId, String productId, PurchaseState state, long purchaseTime, String developerPayLoad) {
			ContentValues values = new ContentValues();
			values.put(HISTORY_ORDER_ID_COL, orderId);
			values.put(HISTORY_PRODUCT_ID_COL, productId);
			values.put(HISTORY_STATE_COL, state.ordinal());
			values.put(HISTORY_PURCHASE_TIME_COL, purchaseTime);
			values.put(HISTORY_DEVELOPER_PAYLOAD_COL, developerPayLoad);
			mDb.replace(PURCHASE_HISTORY_TABLE_NAME, null /* nullColumnHack */, values);
		}
		
		
		/* Updates the quantity of the given product to the given value. If the given value is zero, then the product is removed from the table.*/
		private void updatePurchasedItem(String productId, int quantity) {
			if (quantity == 0) {
				mDb.delete(PURCHASED_ITEMS_TABLE_NAME, PURCHASED_PRODUCT_ID_COL + "=?", new String[] { productId });
				return;
			}
			ContentValues values = new ContentValues();
			values.put(PURCHASED_PRODUCT_ID_COL, productId);
			values.put(PURCHASED_QUANTITY_COL, quantity);
			mDb.replace(PURCHASED_ITEMS_TABLE_NAME, null /* nullColumnHack */, values);
		}
		
		/* Adds the given purchase information to the database and returns the total number of times that the given product has been purchased.*/
		public synchronized int updatePurchase(String orderId, String productId, PurchaseState purchaseState, long purchaseTime, String developerPayLoad)
		{
			insertOrder(orderId, productId, purchaseState, purchaseTime, developerPayLoad);
			Cursor cursor = mDb.query(PURCHASE_HISTORY_TABLE_NAME, HISTORY_COLUMNS, HISTORY_PRODUCT_ID_COL + "=?", new String[] { productId }, null, null, null, null);
			if (cursor == null) {
				return 0;
			}
			
			int quantity = 0;
			try { // Count the number of times the product was purchased
				while (cursor.moveToNext()) {
					int stateIndex = cursor.getInt(2);
					PurchaseState state = PurchaseState.valueOf(stateIndex);
					/* Note that a refunded purchase is treated as a purchase.
					 * Such a friendly refund policy is nice (I think this is actually giving another item when refunding, instead of taking one away??)*/
					if (state == purchaseState.PURCHASED || state == purchaseState.REFUNDED) {
						quantity += 1;
					}
				}
				
				/* Update the "purchased items" table */
				updatePurchasedItem(productId, quantity);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
			return quantity;
		}
		
		/* Returns a cursor that can be used to read all the rows and columns of the "purchased items" table. */
		public Cursor queryAllPurchasedItems() {
			return mDb.query(PURCHASED_ITEMS_TABLE_NAME, PURCHASED_COLUMNS, null, null, null, null, null);
		}
		
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
	
		/* This is a standard helper class for constructing the database. */
		private class DatabaseHelper extends SQLiteOpenHelper {
			
			public DatabaseHelper(Context context) {
				super(context, DATABASE_NAME, null, DATABASE_VERSION);
			}
			
			@Override
			public void onCreate(SQLiteDatabase db) {
				createPurchaseTable(db);
			}
			
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				/* Production-quality upgrade code should MODIFY the tables INSTEAD of
				 * dropping and re-creating them. (CHANGE THIS!) */
				if (newVersion != DATABASE_VERSION)
				{
					Log.w(TAG, "Database upgrade from old: " + oldVersion + " to: " + newVersion);
					db.execSQL("DROP TABLE IF EXISTS " + PURCHASE_HISTORY_TABLE_NAME);
					db.execSQL("DROP TABLE IF EXISTS " + PURCHASED_ITEMS_TABLE_NAME);
					createPurchaseTable(db);
					return;
				}
			}
			
			private void createPurchaseTable(SQLiteDatabase db)
			{
				db.execSQL("CREATE TABLE " + PURCHASE_HISTORY_TABLE_NAME + "(" +
						HISTORY_ORDER_ID_COL + " TEXT PRIMARY KEY, " +
						HISTORY_STATE_COL + " INTEGER, " +
						HISTORY_PRODUCT_ID_COL + " TEXT, " +
						HISTORY_DEVELOPER_PAYLOAD_COL + " TEXT, " +
						HISTORY_PURCHASE_TIME_COL + " INTEGER)" );
				db.execSQL("CREATE TABLE " + PURCHASED_ITEMS_TABLE_NAME + "(" + PURCHASED_PRODUCT_ID_COL + " TEXT PRIMARY KEY, " + PURCHASED_QUANTITY_COL + " INTEGER)");
			}
		}
}
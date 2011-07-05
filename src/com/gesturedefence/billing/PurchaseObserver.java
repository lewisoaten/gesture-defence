package com.gesturedefence.billing;

/**
 * @author Michael Watts
 * @since 15:32:16 - 27 Jun 2011
 */

import java.lang.reflect.Method;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Handler;
import android.util.Log;

import com.gesturedefence.billing.BillingService.RequestPurchase;
import com.gesturedefence.billing.BillingService.RestoreTransactions;
import com.gesturedefence.billing.consts.PurchaseState;
import com.gesturedefence.billing.consts.ResponseCode;


/* An interface for observing changes related to purchases. The main application extends this class and registers and instance
 * of that derived class with ResponseHandler.
 * The main application implements the callbacks
 * #onBillingSupported(boolean)
 * #onPurchaseStateChange(PurchaseState, String, int, long)
 * These methods are used to update the UI.*/
public abstract class PurchaseObserver {
	// ========================================
	// Constants
	// ========================================
	
		private static final String TAG = "PurchaseObserver";
		private final Activity mActivity;
		private final Handler mHandler;
		private Method mStartIntentSender;
		private Object[] mStartIntentSenderArgs = new Object[5];
		private static final Class[] START_INTENT_SENDER_SIG = new Class[] {IntentSender.class, Intent.class, int.class, int.class, int.class};
		
	// ========================================
	// Fields
	// ========================================
		
	// ========================================
	// Constructors
	// ========================================
	
		public PurchaseObserver(Activity activity, Handler handler)
		{
			mActivity = activity;
			mHandler = handler;
			initCompatibilityLayer();
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
	
		/* This is the callback that is invoked when Android Market responds to the
		 * BillingService#checkBillingSupported()  request.*/
		public abstract void onBillingSupported(boolean supported);
		
		/* This is the callback that is invoked in response to calling BillingService#requestPurchase(String)
		 * It may also be invoked asynchronously when a purchase is made on another device (if the purchase was for a Market-managed item),
		 * or if the purchase was refunded or the charge cancelled.
		 * This handles the UI update. The database update is handled in
		 * ResponseHandler#purchaseResponse(Context, purchaseState, String, String, long) */
		public abstract void onPurchaseStateChange(PurchaseState purchaseState, String itemID, int quantity, long purchaseTime, String developerPayLoad);
		
		/* This is called when we receive a response code from Market for a RequestPurchase request that we made.
		 * This is NOT used for any other purchase state changes. All purchase state changes are received in
		 * #onPurchaseStateChange(PurchaseState, String, int, long)
		 * This is used for reporting various errors, or if the user backed out and didn't purchase the item.
		 * RESULT_OK = the order was sent successfully to the server (The onPurhcaseStateChange() will be invoked later (with either PURCHASED or CANCELED)
		 * RESULT_USER_CANCELED = the user didn't but the item. */
		public abstract void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode);
		
		/* This is called when we receive a response code from the Android Market for a RestoreTransactions request we made.
		 * A response code of RESULT_OK means that the request was successfully sent to the server.*/
		public abstract void onRestoreTransactionsResponse(RestoreTransactions request, ResponseCode responseCode);
		
		private void initCompatibilityLayer() {
			try {
				mStartIntentSender = mActivity.getClass().getMethod("startIntentSender", START_INTENT_SENDER_SIG);
			} catch (SecurityException e) {
				mStartIntentSender = null;
			} catch (NoSuchMethodException e) {
				mStartIntentSender = null;
			}
		}
		
		void startBuyPageActivity(PendingIntent pendingIntent, Intent intent)
		{
			if (mStartIntentSender != null)
			{
				/* This is on Android 2.0 and beyond. The in-app buy page activity must be on the activity stack of the application.*/
				try {
					mStartIntentSenderArgs[0] = pendingIntent.getIntentSender();
					mStartIntentSenderArgs[1] = intent;
					mStartIntentSenderArgs[2] = Integer.valueOf(0);
					mStartIntentSenderArgs[3] = Integer.valueOf(0);
					mStartIntentSenderArgs[4] = Integer.valueOf(0);
					mStartIntentSender.invoke(mActivity, mStartIntentSenderArgs);
				} catch (Exception e) {
					Log.e(TAG, "error starting activity", e);
				}
			} else {
				/* This is on Android 1.6. The in-app buy page activity must be on its own separate activity stack instead of on the activity
				 * stack of the application.*/
				try {
					pendingIntent.send(mActivity, 0 /* code */, intent);
				} catch (CanceledException e) {
					Log.e(TAG, "errror starting activity", e);
				}
			}
		}
		
		/* Updates the UI after the database has been updated. This method runs in a background thread so it has to post a Runnable to the run on the UI thread.*/
		void postPurchaseStateChange(final PurchaseState purchaseState, final String itemID, final int quantity, final long purchaseTime, final String developerPayLoad)
		{
			mHandler.post(new Runnable() {
				public void run() {
					onPurchaseStateChange(purchaseState, itemID, quantity, purchaseTime, developerPayLoad);
				}
			});
		}
		
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
package com.gesturedefence.billing;

/**
 * @author Michael Watts
 * @since 15:02:39 - 27 Jun 2011
 */

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gesturedefence.billing.Billing.RequestPurchase;
import com.gesturedefence.billing.Billing.RestoreTransactions;
import com.gesturedefence.billing.consts.PurchaseState;
import com.gesturedefence.billing.consts.ResponseCode;


/* This class contains the methods that handle response from Android Market.
 * The implementation of these methods is specific to a particular application.
 * The methods in this example update the database and, if the main application has
 * registered a PurchaseObserver, will also update the UI.*/
public class ResponseHandler {
	// ========================================
	// Constants
	// ========================================
	
		private static final String TAG = "ResponseHandler";
	
	// ========================================
	// Fields
	// ========================================
	
		/* This is a static instance of PurchaseObserver that the
		 * application creates and registers with this class.
		 * The PurchaseObserver is used for updating the UI if the UI is visible.*/
		private static PurchaseObserver sPurchaseObserver;
	
	// ========================================
	// Constructors
	// ========================================
		
	// ========================================
	// Getter & Setter
	// ========================================
	
		/* Registers an observer that updates the UI.*/
		public static synchronized void register(PurchaseObserver observer)
		{
			sPurchaseObserver = observer;
		}

		/* Unregisters a previously registered observer.*/
		public static synchronized void unregister(PurchaseObserver observer)
		{
			sPurchaseObserver = null;
		}
		
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
		
	// ========================================
	// Methods
	// ========================================
	
		/* Notifies the application of the availability of the MarketBillingService.
		 * This method is called in response to the application calling
		 * BillingService#checkBillingSupported() */
		public static void checkBillingSupportedResponse(boolean supported)
		{
			if (sPurchaseObserver != null)
			{
				sPurchaseObserver.onBillingSupported(supported);
			}
		}
		
		/* Starts a new activity for the user to buy an item for sale. This method
		 * forwards the intent on to the PurchaseObserver (if it exists) because we need
		 * to start the activity on the activity stack of the application.*/
		public static void buyPageIntentResponse(PendingIntent pendingIntent, Intent intent)
		{
			if (sPurchaseObserver == null)
			{
				if (consts.DEBUG)
					Log.d(TAG, "UI is not running");
				return;
			}
			sPurchaseObserver.startBuyPageActivity(pendingIntent, intent);
		}
		
		
		/* Notifies the application of purchase state changes. The application can offer an item for sale
		 * to the user via
		 * BillingService#requestPurchase(String)
		 * The BillingService calls this method after it gets the response. Another way this method
		 * can be called is if the user brought something on another device running this same app. Then Android Market
		 * notifies the other devices that the user has purchased an item, in which case the BillingService will
		 * also call this method.
		 * Finally this method can be called if the item was refunded.*/
		public static void purchaseResponse(final Context context, final PurchaseState purchaseState, final String productID,
				final String orderID, final long purchaseTime, final String developerPayLoad)
		{
			/* Update the database with the purchase state. We shouldn't do that from the main thread so we
			 * do the work in a background thread. We don't update the UI here. We will update the UI after
			 * we update the database because we need to read and update the current quantity first.*/
			new Thread(new Runnable()
			{
				public void run() {
					PurchaseDatabase db = new PurchaseDatabase(context);
					int quantity = db.updatePurchase(orderID, productID, purchaseState, purchaseTime, developerPayLoad);
					db.close();
					
					/* This needs to be synchronized because the UI thread can change the value of sPurchaseObserver. */
					synchronized (ResponseHandler.class) {
						if (sPurchaseObserver != null)
						{
							sPurchaseObserver.postPurchaseStateChange(purchaseState, productID, quantity, purchaseTime, developerPayLoad);
						}
					}
					
					
				}
			}).start();
		}
		
		/* This is called when we receive a response code from Android Market for a RequestPurchase request we made.
		 * This is used for reporting various errors and also for acknowledging that an order was sent successfully to
		 * the server. This is NOT used for any purchase state changes. All purchase state changes are received in the link BillingReceiver (receiver)
		 * and are handled in Security#verifyPurchase(String, String) */
		public static void responseCodeRecieved(Context context, RequestPurchase request, ResponseCode responseCode)
		{
			if (sPurchaseObserver != null)
			{
				sPurchaseObserver.onRequestPurchaseResponse(request, responseCode);
			}
		}
		
		
		/* This is called when we receive a response code from Android Market for a RestoreTransactions request.*/
		public static void responseCodeRecieved(Context context, RestoreTransactions request, ResponseCode responseCode)
		{
			if (sPurchaseObserver != null)
			{
				sPurchaseObserver.onRestoreTransactionsResponse(request, responseCode);
			}
		}
		
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
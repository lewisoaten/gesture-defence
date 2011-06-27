package com.gesturedefence.billing;

/**
 * @author Michael Watts
 * @since 13:57:41 - 27 Jun 2011
 */

import com.gesturedefence.billing.consts.ResponseCode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/* This class implements the broadcast receiver for in-app billing.
 * All asynchronous messages from Android Market come to this app through this receiver
 * This class forwards all messages to the {@link Billing}.
 * This class runs on the UI thread and must not do any network I/O, database updates or any other tasks
 * that might take a long time to complete.
 * 
 * It must also not start a background thread because that may be killed as soon as 
 * {@link #onReceive(Context, Intent)} returns. 
 */
public class receiver extends BroadcastReceiver {
	// ========================================
	// Constants
	// ========================================
	
		private static final String TAG = "BillingReciever";
		
	// ========================================
	// Fields
	// ========================================
		
	// ========================================
	// Constructors
	// ========================================
		
	// ========================================
	// Getter & Setter
	// ========================================
		
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
	
	
		/* This is the entry point for all asynchronous messages sent from Android Market
		 * to the application. This method forwards the messages on to the BillingService,
		 * which handles the communication back to the Android Market. */
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String action = arg1.getAction();
			
			if (consts.ACTION_PURCHASE_STATE_CHANGED.equals(action))
			{
				String signedData = arg1.getStringExtra(consts.INAPP_SIGNED_DATA);
				String signature = arg1.getStringExtra(consts.INAPP_SIGNATURE);
				purchaseStateChanged(arg0, signedData, signature);
			}
			else if (consts.ACTION_NOTIFY.equals(action))
			{
				String notifyID = arg1.getStringExtra(consts.NOTIFICATION_ID);
				if (consts.DEBUG)
					Log.i(TAG, "notifyID: " + notifyID);
				notify(arg0, notifyID);
			}
			else if (consts.ACTION_RESPONSE_CODE.equals(action))
			{
				long requestID = arg1.getLongExtra(consts.INAPP_REQUEST_ID, -1);
				int responseCodeIndex = arg1.getIntExtra(consts.INAPP_RESPONSE_CODE, ResponseCode.RESULT_ERROR.ordinal());
				checkResponseCode(arg0, requestID, responseCodeIndex);
			}
			else
			{
				Log.w(TAG, "unexpected action: " + action);
			}
		}
		
	// ========================================
	// Methods
	// ========================================
	
		/* This is called when Android Market sends information about a purchase state change.
		 * The signedData parameter is a plain-text JSON string that is signed by the server
		 * with the developer's private key.*/
		private void purchaseStateChanged(Context context, String signedData, String signature)
		{
			Intent intent = new Intent(consts.ACTION_GET_PURCHASE_INFORMATION);
			intent.setClass(context, Billing.class);
			intent.putExtra(consts.INAPP_SIGNED_DATA, signedData);
			intent.putExtra(consts.INAPP_SIGNATURE, signature);
			context.startService(intent);
		}
		
		/* This is called when Android Market sends a "notify" message indication that transaction
		 * information is available. The request includes a nonce (random number used once) that
		 * we generate and Android Market signs and send back to us with the purchase state and other
		 * transaction details.
		 * The BroadcastReciever cannot bind to the MarketBillingService directly so it starts
		 * the BillingService, which does the actual work of sending messages.*/
		private void notify(Context context, String notifyID)
		{
			Intent intent = new Intent(consts.ACTION_GET_PURCHASE_INFORMATION);
			intent.setClass(context, Billing.class);
			intent.putExtra(consts.NOTIFICATION_ID, notifyID);
			context.startService(intent);
		}
		
		
		/* This is called when Android Market sends a server response code. The BillingService can then
		 * report the status of the response if desired. */
		private void checkResponseCode(Context context, long requestID, int responseCodeIndex)
		{
			Intent intent = new Intent(consts.ACTION_RESPONSE_CODE);
			intent.setClass(context, Billing.class);
			intent.putExtra(consts.INAPP_REQUEST_ID, requestID);
			intent.putExtra(consts.INAPP_RESPONSE_CODE, responseCodeIndex);
			context.startService(intent);
		}
		
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
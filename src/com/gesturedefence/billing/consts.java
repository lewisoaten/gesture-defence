package com.gesturedefence.billing;

/**
 * @author Michael Watts
 * @since 13:36:58 - 27 Jun 2011
 */

public class consts {
	// ========================================
	// Constants
	// ========================================
	
	public enum ResponseCode { // Response code for a request, defined by the Android Market
		RESULT_OK,
		RESULT_USER_CANCELED,
		RESULT_SERVICE_UNAVAILABLE, // means that we couldn't connect to the Android Market server (for example, no data connection).
		RESULT_BILLING_UNAVAILABLE, // means that the in-app billing is not supported yet.
		RESULT_ITEM_UNAVAILABLE, // means that the item this app offered for sale does not exist (or is not published) in the server-side catalog. 
		RESULT_DEVEOLPER_ERROR,
		RESULT_ERROR; // is used for any other errors (such as a server error).
		
		/* Convert from an ordinal value to a Response Code */
		public static ResponseCode valueOf(int index) {
			ResponseCode[] values = ResponseCode.values();
			if (index < 0 || index >= values.length)
			{
				return RESULT_ERROR;
			}
			return values[index];
		}
	}
		
	public enum PurchaseState { // The possible state of an in-app purchase, defined by Android Market
		/* Response to requestPurchase or restoreTransactions */
		PURCHASED, // The user was charged for the order
		CANCELED, // The charge failed on the server
		REFUNDED; // User received a refund for the order

		/* Convert from an ordinal value to a PurchaseState */
		public static PurchaseState valueOf(int index) {
			PurchaseState[] values = PurchaseState.values();
			if (index < 0 || index >= values.length)
			{
				return CANCELED;
			}
			return values[index];
		}
	}
		
	/* This is the action we use to bind to the MarketBillingService */
	public static final String MARKET_BILLING_SERVICE_ACTION = "com.android.vending.billing.MarketBillingService.BIND";

	/* Intent actions that we send from the BillingReciever to the
	 * BillingService. Define by THIS application. */
	public static final String ACTION_CONFIRM_NOTIFICATIONS = "com.gesturedefence.CONFIRM_NOTIFICATION";
	public static final String ACTION_GET_PURCHASE_INFORMATION = "com.gesturedefence.GET_PURCHASE_INFORMATION";
	public static final String ACTION_RESTORE_TRANSACTION = "com.gesturedefence.RESTORE_TRANSACTIONS";

	/* Intent actions we receive in the BillingReceiver from Market.
	 * These are defined by Android Market and CANNOT BE CHANGED */
	public static final String ACTION_NOTIFY = "com.android.vending.billing.IN_APP_NOTIFY";
	public static final String ACTION_RESPONSE_CODE = "com.android.vending.billing.RESPONSE_CODE";
	public static final String ACTION_PURCHASE_STATE_CHANGED = "com.android.vending.billing.PURCHASE_STATE_CHANGED";

	/* These are the name of the extras that are passed in an intent from Market to this application
	 * CANNOT BE CHANGED!*/
	public static final String NOTIFICATION_ID = "notification_id";
	public static final String INAPP_SIGNED_DATA = "inapp_signed_data";
	public static final String INAPP_SIGNATURE = "inapp_signature";
	public static final String INAPP_REQUEST_ID = "request_id";
	public static final String INAPP_RESPONSE_CODE = "reponse_code";

	/* These are the names of the fields in the request bundle */
	public static final String BILLING_REQUEST_METHOD = "BILLING_REQUEST";
	public static final String BILLING_REQUEST_API_VERSION = "API_VERSION";
	public static final String BILLING_REQUEST_PACKAGE_NAME = "PACKAGE_NAME";
	public static final String BILLING_REQUEST_ITEM_ID = "ITEM_ID";
	public static final String BILLING_REQUEST_DEVELOPER_PAYLOAD = "DEVELOPER_PAYLOAD";
	public static final String BILLING_REQUEST_NOTIFY_IDS = "NOTIFY_IDS";
	public static final String BILLING_REQUEST_NONCE = "NONCE";

	public static final String BILLING_RESPONSE_REPONSE_CODE = "RESPONSE_CODE";
	public static final String BILLING_RESPONSE_PURCHASE_INTENT = "PURCHASE_INTENT";
	public static final String BILLING_RESPONSE_REQUEST_ID = "REQUEST_ID";
	public static long BILLING_RESPONSE_INVALID_REQUEST_ID = -1;

	public static final boolean DEBUG = false;
	
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
	
	// ========================================
	// Methods
	// ========================================
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
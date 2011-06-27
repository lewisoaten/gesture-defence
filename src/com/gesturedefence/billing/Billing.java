package com.gesturedefence.billing;

/**
 * @author Michael Watts
 * @since 02:09:21 - 27 Jun 2011
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IMarketBillingService;
import com.gesturedefence.GestureDefence;
import com.gesturedefence.billing.Security.VerifiedPurchase;
import com.gesturedefence.billing.consts.ResponseCode;

/* This class sends messages to Android Market on behalf of the application by connecting (binding)
 * to the MarketBillingService.
 * The application creates an instance of this class and invokes billing requests through this service.*/
public class Billing extends Service implements ServiceConnection{
	// ========================================
	// Constants
	// ========================================
	
	private static final String TAG = "BillingService";
	
	// ========================================
	// Fields
	// ========================================
	
		private GestureDefence base; //Instance of GestureDefence
		
		private IMarketBillingService mService; //The service connection to the remote MarketBillingService.
		
		private static LinkedList<BillingRequest> mPendingRequests = new LinkedList<BillingRequest>(); //The list of requests that are pending while we are waiting for the connection to be established.
		
		/* The list of requests we have sent but have not yet received a response code. The HashMap is indexed by the request Id that each request receives when it executes.*/
		private static HashMap<Long, BillingRequest> mSentRequests = new HashMap<Long, BillingRequest>();
	
	// ========================================
	// Constructors
	// ========================================
		
		public Billing(GestureDefence baseThing)
		{
			super();
			this.base = baseThing;
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
		
		public void setContext(Context context) {
			attachBaseContext(context);
		}
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
		
		/* We don't support binding to this service, only starting the service!*/
		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}
		
		@Override
		public void onStart(Intent intent, int startId) {
			handleCommand(intent, startId);
		}
	
	// ========================================
	// Methods
	// ========================================
		
		/* Binds to the MarketBillingService and returns true if the bind succeeded.*/
		private boolean bindToMarketBillingService() {
			try {
				if (consts.DEBUG)
					Log.i(TAG, "binding to Market billing service.");
				
				boolean bindResult = bindService(new Intent(consts.MARKET_BILLING_SERVICE_ACTION), this, Context.BIND_AUTO_CREATE);
				if (bindResult)
				{
					return true;
				} else
				{
					Log.e(TAG, "Could not bind to service.");
				}
			} catch (SecurityException e) 
			{
				Log.e(TAG, "Security exception: " + e);
			}
			return false;
		}
		
		/* This is called when we are connected to the MarketBillingService.*/
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (consts.DEBUG)
				Log.d(TAG, "Billing service connected");
			
			mService = IMarketBillingService.Stub.asInterface(service);
			runPendingRequests();
		}
		
		/* This is called when we are disconnected from the MarketBillingService.*/
		public void onServiceDisconnected(ComponentName name) {
			Log.w(TAG, "Billing service disconnected");
			mService = null;
		}
		
		/* Unbinds from the MarketBillingService. Call this when the application terminates to avoid leaking a ServiceConnection.*/
		public void unbind() {
			try {
				unbindService(this);
			} catch (IllegalArgumentException e) {
				// This might happen if the service was disconnected.
			}
		}
		
		/* The BillingReceiver sends messages to this service using intents.
		 * Each intent has an action and some extra arguments specific to that action.*/
		public void handleCommand(Intent intent, int startId)
		{
			String action = intent.getAction();
			if (consts.DEBUG)
				Log.i(TAG, "handleCommand() action: " + action);
			
			if (consts.ACTION_CONFIRM_NOTIFICATIONS.equals(action))
			{
				String[] notifyIds = intent.getStringArrayExtra(consts.NOTIFICATION_ID);
				confirmNotifications(startId, notifyIds);
			} else if (consts.ACTION_GET_PURCHASE_INFORMATION.equals(action))
			{
				String notifyId = intent.getStringExtra(consts.NOTIFICATION_ID);
				getPurchaseInformation(startId, new String[] { notifyId });
			} else if (consts.ACTION_PURCHASE_STATE_CHANGED.equals(action))
			{
				String signedData = intent.getStringExtra(consts.INAPP_SIGNED_DATA);
				String signature = intent.getStringExtra(consts.INAPP_SIGNATURE);
				purchaseStateChanged(startId, signedData, signature);
			} else if (consts.ACTION_RESPONSE_CODE.equals(action))
			{
				long requestId = intent.getLongExtra(consts.INAPP_REQUEST_ID, -1);
				int responseCodeIndex = intent.getIntExtra(consts.INAPP_RESPONSE_CODE, ResponseCode.RESULT_ERROR.ordinal());
				ResponseCode responseCode = ResponseCode.valueOf(responseCodeIndex);
				checkResponseCode(requestId, responseCode);
			}
		}
		
		/* Checks if in-app billing is supported*/
		public boolean checkBillingSupported() {
			return new CheckBillingSupported().runRequest();
		}
		
		/* Confirms receipt of a purchase state change.
		 * Each notifyId, is an opaque identifier that came from the server.
		 * This method sends those identifiers back to the MarketBillingService, which ACKs them
		 * to the server.*/
		private boolean confirmNotifications(int startId, String[] notifyIds) {
			return new ConfirmNotifications(startId, notifyIds).runRequest();
		}
		
		/* Gets the purchase information. This message includes a list of notification IDs sent to us by Android Market,
		 * which we include in our request. The server responds with the purchase information, encoded as  JSON string,
		 * and sends that to the BillingReceiver in an intent with the action consts#ACTION_PURCHASE_STATE_CHANGED.*/
		private boolean getPurchaseInformation(int startId, String[] notifyIds) {
			return new GetPurchaseInformation(startId, notifyIds).runRequest();
		}
		
		/* Requests taht the given item be offered to the user for purchase.
		 * When the purchase succeeds (or is cancelled) the BillingReceiver receives an intent
		 * with the action consts#ACTION_NOTIFY.
		 * Returns false if there was an error trying to connect to Android Market.*/
		public boolean requestPurchase(String productId, String developerPayLoad) {
			return new RequestPurchase(productId, developerPayLoad).runRequest();
		}
		
		/* Requests transaction information for all managed items. Call this only when the
		 * applicaiton is first installed or after a database wipe. Do NOT call this every time the application is started.*/
		public boolean restoreTransactions() {
			return new RestoreTransactions().runRequest();
		}
		
		/* Verifies that the data was signed with the given signature, and calls
		 * ResponseHandler#purchaseResponse(Context, PurchaseState, String, String, long)
		 * for each verified purchase.*/
		private void purchaseStateChanged(int startId, String signedData, String signature) {
			ArrayList<Security.VerifiedPurchase> purchases;
			purchases = Security.verifyPurchase(signedData, signature);
			if (purchases == null)
				return;
			
			ArrayList<String> notifyList = new ArrayList<String>();
			for (VerifiedPurchase vp : purchases)
			{
				if (vp.notificationID != null)
				{
					notifyList.add(vp.notificationID);
				}
				ResponseHandler.purchaseResponse(this, vp.purchaseState, vp.productID, vp.orderID, vp.purchaseTime, vp.developerPayLoad);
			}
			if (!notifyList.isEmpty())
			{
				String[] notifyIds = notifyList.toArray(new String[notifyList.size()]);
				confirmNotifications(startId, notifyIds);
			}
		}
		
		/* This is called when we receive a reponse code from Android Market for a request we made.
		 * This is used for reporting various errors and for acknowledging taht an order was sent to the server.
		 * This is NOT used for any purchase state changes. All purchase state changes are received in the
		 * BillingReceiver and passed to this service, where they are handled in purchaseStateChanged.*/
		private void checkResponseCode(long requestId, ResponseCode responseCode) {
			BillingRequest request = mSentRequests.get(requestId);
			if (request != null)
			{
				if (consts.DEBUG)
					Log.d(TAG, request.getClass().getSimpleName() + ": " + responseCode);
				
				request.responseCodeRecieved(responseCode);
			}
			mSentRequests.remove(requestId);
		}
		
		/* Runs any pending requests that are waiting for a connection to the service to be established.
		 * This is run in the main UI thread.*/
		private void runPendingRequests() {
			int maxStartId = -1;
			BillingRequest request;
			while ((request = mPendingRequests.peek()) != null)
			{
				if (request.runIfConnected())
				{
					mPendingRequests.remove(); //Remove the request.
					
					/* Remember teh largest startId, which is the most recent request to start this service.*/
					if (maxStartId < request.getStartId())
						maxStartId = request.getStartId();
				} else
				{ 
					/* The service crashed, so restart it. Note taht thus leaves the current request on the queue.*/
					bindToMarketBillingService();
					return;
				}
			}
			
			/* If we get here then all the requests ran successfully.
			 * If maxStartId != -1 then one of the requests started the service, so we can stop it now.*/
			if (maxStartId >= 0)
			{
				if (consts.DEBUG)
					Log.i(TAG, "stopping service, startId: " + maxStartId);
				
				stopSelf(maxStartId);
			}
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
		
		/* The base class for all requests that use the MarketBillingService.
		 * Each derived class overrides the run() method to call the appropriate service interface.
		 * If we are already connected to teh MarketBillingService, then we call the run() method directly.
		 * Otherwise we bind to the service and save the request on a queue to be run later when the service is connected.*/
		abstract class BillingRequest {
			private final int mStartId;
			protected long mRequestId;
			
			public BillingRequest(int startId)
			{
				mStartId = startId;
			}
			
			public int getStartId()
			{
				return mStartId;
			}
			
			/* Run the request, starting the connection if necessary.
			 * Returns false if there was an error starting the connection.*/
			public boolean runRequest()
			{
				if (runIfConnected()) {
					return true;
				}
				
				if (bindToMarketBillingService()) {
					mPendingRequests.add(this); //Add a pending request to run when the service is connected.
					return true;
				}
				return false;
			}
			
			/* Try running the request directly if the service is already connected.
			 * Returns false if the service is not connected or there was an error.*/
			public boolean runIfConnected() {
				if (consts.DEBUG)
					Log.d(TAG, getClass().getSimpleName());
				
				if (mService != null) {
					try {
						mRequestId = run();
						if (consts.DEBUG)
							Log.d(TAG, "request id: " + mRequestId);
						if (mRequestId >= 0 ) {
							mSentRequests.put(mRequestId, this);
						}
						return true;
					} catch (RemoteException e) {
						onRemoteException(e);
					}
				}
				return false;
			}
			
			/* Called when a remote exception occurs while trying to execute the method.*/
			protected void onRemoteException(RemoteException e)
			{
				Log.w(TAG, "remote billing service crashed");
				mService = null;
			}
			
			/* The derived class must implement this method.*/
			abstract protected long run() throws RemoteException;
			
			/* This called when Android Market sends a response code for this request.*/
			protected void responseCodeRecieved(ResponseCode responseCode) {
			}
			
			protected Bundle makeRequestBundle(String method)
			{
				Bundle request = new Bundle();
				request.putString(consts.BILLING_REQUEST_METHOD, method);
				request.putInt(consts.BILLING_REQUEST_API_VERSION, 1);
				request.putString(consts.BILLING_REQUEST_PACKAGE_NAME, getPackageName());
				return request;
			}
			
			protected void logResponseCode(String method, Bundle response) {
				ResponseCode responseCode = ResponseCode.valueOf(response.getInt(consts.BILLING_RESPONSE_REPONSE_CODE));
				if (consts.DEBUG)
					Log.e(TAG, method + " received " + responseCode.toString());
			}
		}
		
		/* Wrapper class that checks if in-app billing is supported.*/
		class CheckBillingSupported extends BillingRequest {
			public CheckBillingSupported() {
				/* The object is never created as a side effect of starting this service.
				 * So we pass -1 as the StartId to indicate that we should not stop this service executing this request.*/
				super(-1);
			}
			
			@Override
			protected long run() throws RemoteException {
				Bundle request = makeRequestBundle("CHECK_BILLING_SUPPORTED");
				Bundle response = mService.sendBillingRequest(request);
				int responseCode = response.getInt(consts.BILLING_RESPONSE_REPONSE_CODE);
				if (consts.DEBUG)
					Log.i(TAG, "CheckBillingSupported response code : " + ResponseCode.valueOf(responseCode));
				
				boolean billingSupported = (responseCode == ResponseCode.RESULT_OK.ordinal());
				ResponseHandler.checkBillingSupportedResponse(billingSupported);
				return consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
			}
		}
		
		/* Wrapper class that requests a purchase.*/
		class RequestPurchase extends BillingRequest {
			public final String mProductID;
			public final String mDeveloperPayLoad;
			
			public RequestPurchase(String itemID)
			{
				this(itemID, null);
			}
			
			public RequestPurchase(String itemID, String developerPayLoad)
			{
				/* The object is never created as a side effect of starting this service.
				 * So we pass -1 as the StartId to indicate that we should not stop this service executing this request.*/
				super(-1);
				mProductID = itemID;
				mDeveloperPayLoad = developerPayLoad;
			}
			
			@Override
			protected long run() throws RemoteException {
				Bundle request = makeRequestBundle("REQUEST_PURCHASE");
				request.putString(consts.BILLING_REQUEST_ITEM_ID, mProductID);
				//NOTE that the developerPayLoad is optional!
				if (mDeveloperPayLoad != null)
					request.putString(consts.BILLING_REQUEST_DEVELOPER_PAYLOAD, mDeveloperPayLoad);
				
				Bundle response = mService.sendBillingRequest(request);
				PendingIntent pendingIntent = response.getParcelable(consts.BILLING_RESPONSE_PURCHASE_INTENT);
				
				if (pendingIntent == null)
				{
					Log.e(TAG, "Error with requestPurchase");
					return consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
				}
				
				Intent intent = new Intent();
				ResponseHandler.buyPageIntentResponse(pendingIntent, intent);
				return response.getLong(consts.BILLING_RESPONSE_REQUEST_ID, consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
			}
			
			@Override
			protected void responseCodeRecieved(ResponseCode responseCode) {
				ResponseHandler.responseCodeRecieved(Billing.this, this, responseCode);
			}
		}
		
		
		/* Wrapper class that confirms a list of notifications to the server*/
		class ConfirmNotifications extends BillingRequest {
			final String[] mNotifyIds;
			
			public ConfirmNotifications(int startId, String[] notifyIds) {
				super(startId);
				mNotifyIds = notifyIds;
			}
			
			@Override
			protected long run() throws RemoteException {
				Bundle request = makeRequestBundle("CONFRIM_NOTIFICATIONS");
				request.putStringArray(consts.BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
				Bundle response = mService.sendBillingRequest(request);
				logResponseCode("confirmNotifications", response);
				return response.getLong(consts.BILLING_RESPONSE_REQUEST_ID, consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
			}
		}
		
		/* Wrapper class that sends a GET_PURCHASE_INFORMATION message to the server.*/
		class GetPurchaseInformation extends BillingRequest {
			long mNonce;
			final String[] mNotifyIds;
			
			public GetPurchaseInformation(int startId, String[] notifyIds) {
				super(startId);
				mNotifyIds = notifyIds;
			}
			
			@Override
			protected long run() throws RemoteException {
				mNonce = Security.generateNonce();
				
				Bundle request = makeRequestBundle("GET_PURCHASE_INFORMATION");
				request.putLong(consts.BILLING_REQUEST_NONCE, mNonce);
				request.putStringArray(consts.BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
				
				Bundle response = mService.sendBillingRequest(request);
				logResponseCode("getPurchaseInformation", response);
				return response.getLong(consts.BILLING_RESPONSE_REQUEST_ID, consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
			}
			
			@Override
			protected void onRemoteException(RemoteException e) {
				super.onRemoteException(e);
				Security.removeNonce(mNonce);
			}
		}
		
		/* Wrapper class that send a RESTORE_TRANSACTIONS message to the server.*/
		class RestoreTransactions extends BillingRequest {
			long mNonce;
			
			public RestoreTransactions() {
				/* The object is never created as a side effect of starting this service.
				 * So we pass -1 as the StartId to indicate that we should not stop this service executing this request.*/
				super(-1);
			}
			
			@Override
			protected long run() throws RemoteException {
				mNonce = Security.generateNonce();
				
				Bundle request = makeRequestBundle("RESTORE_TRANSACTIONS");
				request.putLong(consts.BILLING_REQUEST_NONCE, mNonce);
				
				Bundle response = mService.sendBillingRequest(request);
				logResponseCode("restoreTransactions", response);
				return response.getLong(consts.BILLING_RESPONSE_REQUEST_ID, consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
			}
			
			@Override
			protected void onRemoteException(RemoteException e) {
				super.onRemoteException(e);
				Security.removeNonce(mNonce);
			}
			
			@Override
			protected void responseCodeRecieved(ResponseCode responseCode) {
				ResponseHandler.responseCodeRecieved(Billing.this, this, responseCode);
			}
		}
}
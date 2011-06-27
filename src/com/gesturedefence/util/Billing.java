package com.gesturedefence.util;

/**
 * @author Michael Watts
 * @since 02:09:21 - 27 Jun 2011
 */

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.android.vending.billing.IMarketBillingService;
import com.gesturedefence.GestureDefence;

public class Billing extends Service implements ServiceConnection{
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private GestureDefence base; //Instance of GestureDefence
		
		private IMarketBillingService mService;
	
	// ========================================
	// Constructors
	// ========================================
		
		public Billing(GestureDefence baseThing)
		{ 
			String TAG = "Billing service onCreate: "; 
			this.base = baseThing;
			try {
				boolean bindResult = base.bindService(new Intent("com.android.vending.billing.MarketBillingService.BIND"), this, Context.BIND_AUTO_CREATE);
				if (bindResult)
				{
					Log.i(TAG, "Service bind successful.");
				}
				else
				{
					Log.e(TAG, "Could not bind to the MarketBillingService.");
				}
			} catch (SecurityException e) {
				Log.e(TAG, "Security exception: " + e);
			}
		}
	
	// ========================================
	// Getter & Setter
	// ========================================
	
	// ========================================
	// Methods for/from SuperClass/Interfaces
	// ========================================
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			Log.i("onServiceConnected: ", "MarketBillingService connected.");
			mService = IMarketBillingService.Stub.asInterface(service);
		}

		/* (non-Javadoc)
		 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see android.app.Service#onBind(android.content.Intent)
		 */
		@Override
		public IBinder onBind(Intent intent) {
			// TODO Auto-generated method stub
			return null;
		}
	
	// ========================================
	// Methods
	// ========================================
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
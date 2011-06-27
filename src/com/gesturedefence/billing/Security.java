package com.gesturedefence.billing;

/**
 * @author Michael Watts
 * @since 17:02:14 - 27 Jun 2011
 */

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.gesturedefence.billing.consts.PurchaseState;
import com.gesturedefence.billing.util.Base64;
import com.gesturedefence.billing.util.Base64DecoderException;


/* For secure implementation this should be server side.
 * Because we can't do that we need to make sure it's obfuscated to make it harder to decode.*/
public class Security {
	// ========================================
	// Constants
	// ========================================
	
		private static final String TAG = "Security";
		
		private static final String KEY_FACTORY_ALGORITHM = "RSA";
		private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
		private static final SecureRandom RANDOM = new SecureRandom();
		
	// ========================================
	// Fields
	// ========================================
	
		/* This keeps track of the nonces that we generated and sent to the server.
		 * We need to keep track of these until we get back the purchase state and send a confirmation message
		 * back to Android Market.
		 * If we are killed and lose this list of nonces, it is NOT fatal. Android Market will send us a new
		 * "notify" message and we will re-generate a new nonce.
		 * This HAS to be static so that the BillingReceiver can check if a nonce exists.*/
		private static HashSet<Long> sKnownNonces = new HashSet<Long>();
		
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
	
		/* Generates a nonce (a random number used once).*/
		public static long generateNonce()
		{
			long nonce = RANDOM.nextLong();
			sKnownNonces.add(nonce);
			return nonce;
		}
		
		public static void removeNonce(long nonce)
		{
			sKnownNonces.remove(nonce);
		}
		
		public static boolean isNonceKnown(long nonce)
		{
			return sKnownNonces.contains(nonce);
		}
		
		
		/* Verifies that the data was signed with the given signature, and returns the list of verified purchases.
		 * The data is in JSON format and contains a nonce that we generated and that was signed with a private key.
		 * The data also contains the PurchaseState and product ID of the purchase. In the general case, there may
		 * be an array of purchase transactions because there may be delays in processing the purchase on the backend
		 * and then several purchases can be batched together. */
		public static ArrayList<VerifiedPurchase> verifyPurchase(String signedData, String signature)
		{
			if (signedData == null)
			{
				Log.e(TAG, "data is null");
				return null;
			}
			
			if (consts.DEBUG)
				Log.i(TAG, "signedData: " + signedData);
			
			boolean verified = false;
			if (!TextUtils.isEmpty(signature))
			{
				/* Compute your public key (That we get from Android Market published site).
				 * 
				 * Instead of just sorting the entire literal string here embedded in the program,
				 * we NEED to construct the key at runtime from pieces or use bit manipulation (XOR with some other string)
				 * to hide the actual key. The key itself is not secret information, but we don't want to make it easy
				 * for hackers!
				 * 
				 * Genaerally, encryption keys / passwords should only be kept in memory long enough to perform the operation they need to perform.*/
				String base64EncodedPublicKey = "OUR PUBLIC KEY HERE!";
				PublicKey key = Security.generatePublicKey(base64EncodedPublicKey);
				verified = Security.verify(key, signedData, signature);
				if (!verified)
				{
					Log.w(TAG, "signature does not match data.");
					return null;
				}
			}
			
			JSONObject jObject;
			JSONArray jTransactionsArray = null;
			int numTransactions = 0;
			long nonce = 0L;
			try {
				jObject = new JSONObject(signedData);
				
				/* The nonce might be null if the user backed out of the buy page.*/
				nonce = jObject.optLong("nonce");
				jTransactionsArray = jObject.optJSONArray("orders");
				if (jTransactionsArray != null)
				{
					numTransactions = jTransactionsArray.length();
				}
			} catch (JSONException e) {
				return null;
			}
			
			if (!Security.isNonceKnown(nonce))
			{
				Log.w(TAG, "Nonce not found: " + nonce);
				return null;
			}
			
			ArrayList<VerifiedPurchase> purchases = new ArrayList<VerifiedPurchase>();
			try {
				for (int i = 0; i < numTransactions; i++)
				{
					JSONObject jElement = jTransactionsArray.getJSONObject(i);
					
					int response = jElement.getInt("purchaseState");
					PurchaseState purchaseState = PurchaseState.valueOf(response);
					String productID = jElement.getString("productID");
					String packageName = jElement.getString("packageName");
					long purchaseTime = jElement.getLong("purchaseTime");
					String orderID = jElement.getString("orderID");
					String notifyID = null;
					if (jElement.has("notificationID"))
					{
						notifyID = jElement.getString("notificationID");
					}
					String developerPayLoad = jElement.optString("developerPayLoad", null);
					
					/* If the purchase state is PURCHASED, then we require a verified nonce.*/
					if (purchaseState == PurchaseState.PURCHASED && !verified)
					{
						continue;
					}
					purchases.add(new VerifiedPurchase(purchaseState, notifyID, productID, orderID, purchaseTime, developerPayLoad));
				}
			} catch (JSONException e) {
				Log.e(TAG, "JSON exception: ", e);
				return null;
			}
			removeNonce(nonce);
			return purchases;
		}
		
		
		/* Generates a PublicKey instance from a string containing the Base64-encoded public key.*/
		public static PublicKey generatePublicKey(String encodedPublicKey) {
			try {
				byte[] decodeKey = Base64.decode(encodedPublicKey);
				KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
				return keyFactory.generatePublic(new X509EncodedKeySpec(decodeKey));
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (InvalidKeySpecException e) {
				Log.e(TAG, "Invalid key specification.");
				throw new IllegalArgumentException(e);
			} catch (Base64DecoderException e) {
				Log.e(TAG, "Base64 decoding failed");
				throw new IllegalArgumentException(e);
			}
		}
		
		
		/* Verifies that the signature from the server matches the computed signature on the data.
		 * Returns true if the data is correctly signed. */
		public static boolean verify(PublicKey publicKey, String signedData, String signature)
		{
			if (consts.DEBUG)
				Log.i(TAG, "signature: " + signature);
			
			Signature sig;
			try {
				sig = Signature.getInstance(SIGNATURE_ALGORITHM);
				sig.initVerify(publicKey);
				sig.update(signedData.getBytes());
				if (!sig.verify(Base64.decode(signature)))
				{
					Log.e(TAG, "Signature verification failed");
					return false;
				}
				return true;
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG, "NoSuchAlgorithmExcepetion.");
			} catch (InvalidKeyException e) {
				Log.e(TAG, "Invalid key specification.");
			} catch (SignatureException e) {
				Log.e(TAG, "Signature exception.");
			} catch (Base64DecoderException e) {
				Log.e(TAG, "Base64 decoding failed.");
			}
			return false;
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
	
		/* A class to hold verified purchase information. */
		public static class VerifiedPurchase {
			public PurchaseState purchaseState;
			public String notificationID;
			public String productID;
			public String orderID;
			public long purchaseTime;
			public String developerPayLoad;
			
			public VerifiedPurchase(PurchaseState pruchaseState, String notificationID, String productID, String orderID, long purchaseTime, String developerPayLoad)
			{
				this.purchaseState = purchaseState;
				this.notificationID = notificationID;
				this.productID = productID;
				this.orderID = orderID;
				this.purchaseTime = purchaseTime;
				this.developerPayLoad = developerPayLoad;
			}
		}
}
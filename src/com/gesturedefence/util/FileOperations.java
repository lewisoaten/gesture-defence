package com.gesturedefence.util;

/**
 * @author Michael Watts
 * @since 15:51:56 - 22 Jun 2011
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Environment;

import com.gesturedefence.GestureDefence;

public class FileOperations {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private GestureDefence base; //Instance of GestureDefence
		
		private final String FILENAME = "save_game_file"; //Save file
		private File externalStorageDir; // External storage directory
		private File dir; // Directory for the files
		
		BroadcastReceiver mExternalStorageReceiver;
		private boolean mExternalStorageAvailiable = false;
		private boolean mExternalStorageWritable = false;
	
	// ========================================
	// Constructors
	// ========================================
		
		public FileOperations(GestureDefence baseThing)
		{ 
			this.base = baseThing;
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
		
		public void savegame(Context ctx)
		{ //Self explanatory ?
			ExternalStorageChecks();
			
			if (mExternalStorageAvailiable && mExternalStorageWritable)
			{ // Check to see if we can save
				try {
					File file = new File(dir, FILENAME);
					FileOutputStream fos = new FileOutputStream(file);
					OutputStreamWriter output = new OutputStreamWriter(fos);
					BufferedWriter buf = new BufferedWriter(output);
	
					String killCount = "#1:" + base.sKillCount;
					String waveNumber = "#2:" + (base.theWave.getWaveNumber());
					String currentCash = "#3:" + base.sMoney;
					String totalCash = "#4:" + base.mMoneyEarned;
					String currentHealth = "#5:" + base.sCastle.getCurrentHealth();
					String maxHealth = "#6:" + base.sCastle.getMaxHealth();
					String previousKills = "#7:" + base.sPreviousKillCount;
					String manaLevel = "#8:" + base.mana;
	
					buf.write(killCount);
					buf.newLine();
					buf.write(waveNumber);
					buf.newLine();
					buf.write(currentCash);
					buf.newLine();
					buf.write(totalCash);
					buf.newLine();
					buf.write(currentHealth);
					buf.newLine();
					buf.write(maxHealth);
					buf.newLine();
					buf.write(previousKills);
					buf.newLine();
					buf.write(manaLevel);
					buf.newLine();
	
					buf.close();
					output.close();
					fos.close();
					
					base.CustomNotifications.addNotification("Game Saved!");	
	
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					base.CustomNotifications.addNotification("Game Save failed. Failed to create file!");
				} catch (IOException e) {
					e.printStackTrace();
					base.CustomNotifications.addNotification("Game Save failed. Conversion errors");
				}
			}
		}
		
		public boolean loadSaveFile(Context ctx)
		{ //Self explanatory ?
			ExternalStorageChecks();
			
			if (mExternalStorageAvailiable && mExternalStorageWritable)
			{ //Check to make sure we can load first!
				String string = "";
				String killCount = "0";
				String waveNumber = "0";
				String currentCash = "0";
				String totalCash = "0";
				String currentHealth = "3000";
				String maxHealth = "3000";
				String prevKills = "0";
				String manaLevel = "0";
				
				try {
					File file = new File(dir, FILENAME);
					FileInputStream fis = new FileInputStream(file);
					InputStreamReader inputreader = new InputStreamReader(fis);
					BufferedReader buffreader = new BufferedReader(inputreader);
					string = buffreader.readLine();
					
					while ( string != null)
					{
						// stuff
						if (string.contains("#1:"))
						{
							int pos = string.indexOf(":");
							killCount = string.substring(pos + 1);
						}
						if (string.contains("#2:"))
						{
							int pos = string.indexOf(":");
							waveNumber = string.substring(pos + 1);
						}
						if (string.contains("#3:"))
						{
							int pos = string.indexOf(":");
							currentCash = string.substring(pos + 1);
						}
						if (string.contains("#4:"))
						{
							int pos = string.indexOf(":");
							totalCash = string.substring(pos + 1);
						}
						if (string.contains("#5:"))
						{
							int pos = string.indexOf(":");
							currentHealth = string.substring(pos + 1);
						}
						if (string.contains("#6:"))
						{
							int pos = string.indexOf(":");
							maxHealth = string.substring(pos + 1);
						}
						if (string.contains("#7:"))
						{
							int pos = string.indexOf(":");
							prevKills = string.substring(pos + 1);
						}
						if (string.contains("#8:"))
						{
							int pos = string.indexOf(":");
							manaLevel = string.substring(pos + 1);
						}
						
						string = buffreader.readLine();
					}
					
					buffreader.close();
					inputreader.close();
					fis.close();
					
					base.sKillCount = Integer.parseInt(killCount);
					base.sPreviousKillCount = Integer.parseInt(prevKills);
					base.theWave.setWaveNumber(Integer.parseInt(waveNumber));
					base.sMoney = Integer.parseInt(currentCash);
					base.mMoneyEarned = Integer.parseInt(totalCash);
					base.sCastle.setCurrentHealth(Integer.parseInt(currentHealth));
					base.sCastle.setMaxHealth(Integer.parseInt(maxHealth));
					base.mana = Integer.parseInt(manaLevel);
					
					base.CustomNotifications.addNotification("Game Loaded!");
					
					base.ButtonPress(3);			
					
					return true;
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					base.CustomNotifications.addNotification("Game load failed. No save file found!");
					return false;
				} catch (IOException e) {
					e.printStackTrace();
					base.CustomNotifications.addNotification("Game load failed. Error reading save file!");
					return false;
				}
			} else return false;
		}
		
		public int getLastWaveFromSaveFile(Context ctx) {
			ExternalStorageChecks();
			
			if (mExternalStorageAvailiable && mExternalStorageWritable)
			{ //Only run if we can! (otherwise return 0)
				/* This simply looks for a save file and then finds the last wave value*/
				String string = "";
				String WaveNumber = "-1";
				
				try {
					File file = new File(dir, FILENAME);
					FileInputStream fis = new FileInputStream(file);
					InputStreamReader inputreader = new InputStreamReader(fis);
					BufferedReader buffreader = new BufferedReader(inputreader);
					string = buffreader.readLine();
					
					while ( string != null)
					{
						// stuff
						if (string.contains("#2:"))
						{
							int pos = string.indexOf(":");
							WaveNumber = string.substring(pos + 1);
						}
						
						string = buffreader.readLine();
					}
					
					buffreader.close();
					inputreader.close();
					fis.close();
					
					return Integer.parseInt(WaveNumber);
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					base.CustomNotifications.addNotification("Game load failed. No save file found!");
					return 0;
				} catch (IOException e) {
					e.printStackTrace();
					base.CustomNotifications.addNotification("Game load failed. Error reading save file!");
					return 0;
				}
			} else return 0;
		}
		
		public boolean CheckForSaveFile(Context ctx) {
			ExternalStorageChecks();
			
			if (mExternalStorageAvailiable && mExternalStorageWritable)
			{
				File file = new File(dir, FILENAME);
				try {				
					@SuppressWarnings("unused")
					FileInputStream fis = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return false;
				};
				return true;
			}
			return false;
		}
		
		public void ExternalStorageChecks() {
			String state = Environment.getExternalStorageState();
			
			if (Environment.MEDIA_MOUNTED.equals(state))
			{
				/* We can read and write to the media */
				mExternalStorageAvailiable = mExternalStorageWritable = true;
				externalStorageDir = Environment.getExternalStorageDirectory();
				dir = new File(externalStorageDir.getAbsoluteFile() + "/Android/data/com.gesturedefence/files");
				dir.mkdirs();
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
			{
				/* We can only READ the media */
				mExternalStorageAvailiable = true;
				mExternalStorageWritable = false;
			} else if (Environment.MEDIA_SHARED.equals(state))
			{
				mExternalStorageAvailiable = mExternalStorageWritable = false;
				base.CustomNotifications.addNotification("External Storage is currently in use, please disconnect from computer!");
			} else
			{
				/* Something is wrong. May be one of many other states */
				mExternalStorageAvailiable = mExternalStorageWritable = false;
				base.CustomNotifications.addNotification("Something is wrong with the external storage. Please check it.");
			}
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
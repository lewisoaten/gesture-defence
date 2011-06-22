package com.gesturedefence.util;

/**
 * @author Michael Watts
 * @since 15:51:56 - 22 Jun 2011
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.widget.Toast;

import com.gesturedefence.GestureDefence;

public class FileOperations {
	// ========================================
	// Constants
	// ========================================
	
	// ========================================
	// Fields
	// ========================================
	
		private GestureDefence base; //Instance of GestureDefence
		
		private final String FILENAME = "save_game_file";
	
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
			try {
				FileOutputStream fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE);
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

				base.handler.post(new Runnable() {
					public void run() {
						Toast.makeText(base.getApplicationContext(), "Game Saved!", Toast.LENGTH_SHORT).show();
					}
				});	

			} catch (FileNotFoundException e) {
				e.printStackTrace();
				base.handler.post(new Runnable() {
					public void run() {
						Toast.makeText(base.getApplicationContext(), "Game Save failed. Failed to create file!", Toast.LENGTH_LONG).show();
					}
				});	
			} catch (IOException e) {
				e.printStackTrace();
				base.handler.post(new Runnable() {
					public void run() {
						Toast.makeText(base.getApplicationContext(), "Game Save failed. Conversion errors", Toast.LENGTH_LONG).show();
					}
				});	
			}
		}
		
		public boolean loadSaveFile(Context ctx)
		{ //Self explanatory ?
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
				FileInputStream fis = ctx.openFileInput(FILENAME);
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
				
				base.handler.post(new Runnable() {
					public void run() {
						Toast.makeText(base.getApplicationContext(), "Game Loaded!", Toast.LENGTH_SHORT).show();
					}
				});
				
				base.ButtonPress(3);			
				
				return true;
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				base.handler.post(new Runnable() {
					public void run() {
						Toast.makeText(base.getApplicationContext(), "Game load failed. No save file found!", Toast.LENGTH_LONG).show();
					}
				});
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				base.handler.post(new Runnable() {
					public void run() {
						Toast.makeText(base.getApplicationContext(), "Game load failed. Error reading save file!", Toast.LENGTH_LONG).show();
					}
				});
				return false;
			}
		}
		
		public int getLastWaveFromSaveFile(Context ctx) {
			/* This simply looks for a save file and then finds the last wave value*/
			String FILENAME = "save_game_file";
			String string = "";
			String WaveNumber = "0";
			
			try {
				FileInputStream fis = ctx.openFileInput(FILENAME);
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
				base.handler.post(new Runnable() {
					public void run() {
						Toast.makeText(base.getApplicationContext(), "Game load failed. No save file found!", Toast.LENGTH_LONG).show();
					}
				});
				return 0;
			} catch (IOException e) {
				e.printStackTrace();
				base.handler.post(new Runnable() {
					public void run() {
						Toast.makeText(base.getApplicationContext(), "Game load failed. Error reading save file!", Toast.LENGTH_LONG).show();
					}
				});
				return 0;
			}
		}
		
		public boolean CheckForSaveFile(Context ctx) {
			
			try {
				@SuppressWarnings("unused")
				FileInputStream fis = ctx.openFileInput(FILENAME);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			};
			return true;
		}
	
	// ========================================
	// Inner and Anonymous Classes
	// ========================================
}
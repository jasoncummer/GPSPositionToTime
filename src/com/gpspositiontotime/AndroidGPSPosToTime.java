package com.gpspositiontotime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidGPSPosToTime extends Activity {// implements
													// OnSeekBarChangeListener {
	// UI widgets
	Button btnSetLocation;
	Button btnUseNumberPickerButton;
	SeekBar sbLongitudeSeekBar;
	TextView utcTextView;
	TextView boatTimeClockView;
	TextView manualGPSInUseTextView;
	NumberPicker degrees;
	NumberPicker minutes;
	NumberPicker seconds;
	private Handler mHandler = new Handler();
	
	// booleans
	private boolean showManualGPS = false;
	private boolean useManualGPS = false;
	
	// spacetime variables
	GPSTracker gps;
	long GPSTime = 0;
	long lastKnownGPSTime = 0;
	long currentTime = 0; 
	double manualLongitude;
	private static int startingYearOfVoyage = 112; 
	
	// times in milliseconds
	private static long twelveHours =  43200000;
	private static long oneHour =  3600000;
	private static long oneMinute = 60000;
	private static long oneSecond = 1000;
	
	// time multipliers
	private static int oneMinuteInSeconds = 60;
	private static int oneHourInSeconds = 60;
	private static int oneDayInHours = 24;

	// other multipliers
	private static int invertSign = -1;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_android_gpspos_to_time);

		gps = new GPSTracker(AndroidGPSPosToTime.this);

		btnSetLocation = (Button) findViewById(R.id.btnSetLocation);
		btnUseNumberPickerButton = (Button) findViewById(R.id.UseNumberPickerButton);

		boatTimeClockView = (TextView) findViewById(R.id.boatClockTextView);
		utcTextView = (TextView) findViewById(R.id.utc_clock_textView);
		manualGPSInUseTextView = (TextView) findViewById(R.id.manualGPSInUseTextView);

		degrees = (NumberPicker) findViewById(R.id.gpsDegreesNumberPicker);
		degrees.setMaxValue(180);
		degrees.setMinValue(0);

		minutes = (NumberPicker) findViewById(R.id.gpsMinutesNumberPicker);
		minutes.setMaxValue(59);
		minutes.setMinValue(0);

		seconds = (NumberPicker) findViewById(R.id.gpsSecondsNumberPicker);
		seconds.setMaxValue(59);
		seconds.setMinValue(0);

	}// end function onCreate

	/*
     * updates the time of the TextVew clocks in the activity
     */
	private Runnable mUpdateTimeTask = new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {

			//System.out.println("mUpdateTimeTask");
			
			Date date = new Date();

			try {
				
				// check if GPS enabled
				if (gps != null) {
					gps.getLocation();
					GPSTime = gps.getGPSTime();
					
					if (lastKnownGPSTime == GPSTime){
						currentTime += oneSecond;
					}else{
						lastKnownGPSTime = GPSTime;
						currentTime = GPSTime;
					}
					
					// set UTC Clock
					date.setTime(currentTime);
									
					
					// if time is below 2012 odds are its null and would set to the beginning of epoch time
					if (date.getYear() >= startingYearOfVoyage){
								
						//set UTC time
						if (date.getSeconds() < 10) {
							if (date.getMinutes() < 10) {
								utcTextView.setText("" + date.getHours() + ":0"
										+ date.getMinutes() + ".0"
										+ date.getSeconds());
							} else {
								utcTextView.setText("" + date.getHours() + ":"
										+ date.getMinutes() + ".0"
										+ date.getSeconds());
							}
						} else {
							if (date.getMinutes() < 10) {
								utcTextView.setText("" + date.getHours() + ":0"
										+ date.getMinutes() + "."
										+ date.getSeconds());
							} else {
								utcTextView.setText("" + date.getHours() + ":"
										+ date.getMinutes() + "."
										+ date.getSeconds());
							}
						}
					
						// set boat time clock
						long l = currentTime;
						if (!useManualGPS){
							l -= (((gps.getLongitude() * invertSign) * 4) * oneMinuteInSeconds * oneSecond);// TODO magic number
						}else{
							l -= (((manualLongitude * invertSign) * 4) * oneMinuteInSeconds * oneSecond);// TODO magic number
						}
						date.setTime(l);

						// Set system time
						if (date.getSeconds() < 10) {
							if (date.getMinutes() < 10) {
								boatTimeClockView.setText("" + date.getHours() + ":0"
										+ date.getMinutes() + ".0" + date.getSeconds());
							} else {
								boatTimeClockView.setText("" + date.getHours() + ":"
										+ date.getMinutes() + ".0" + date.getSeconds());
							}
						} else {
							if (date.getMinutes() < 10) {
								boatTimeClockView.setText("" + date.getHours() + ":0"
										+ date.getMinutes() + "." + date.getSeconds());
							} else {
								boatTimeClockView.setText("" + date.getHours() + ":"
										+ date.getMinutes() + "." + date.getSeconds());
							}
						}					
					}
				} else {
					gps = new GPSTracker(AndroidGPSPosToTime.this);
				}
			} catch (Exception e) {
				System.out.println(e);
			}

			
			// updates the time every second
			mHandler.postDelayed(mUpdateTimeTask, oneSecond);
		}
	};

	/*
	 * Sets the system time of the Android OS
	 */
	private Runnable mSetSystemTime = new Runnable(){
		
		/*
		 * 
		 */
		public void  run(){
			
			if (useManualGPS){
				stillUsingManualGPS();
			}else{
				// set the file permissions on /dev/alarm file to 666
				chmodAlarmFile();			
				
				Date date = new Date();
				try {
					
					if (gps == null){
						gps = new GPSTracker(AndroidGPSPosToTime.this);
					}
					
					if (gps != null) {
						gps.getLocation();
						GPSTime = gps.getGPSTime();
						date.setTime(GPSTime);
						
						// if time is below 2012 odds are its null and would set to the beginning of epoch time
						if (date.getYear() >= startingYearOfVoyage){
							// adjust for longitude
							long l = GPSTime;
							l -= (((gps.getLongitude() * -1) * 4) * oneMinuteInSeconds * oneSecond);
							date.setTime(l);
							
							
							setSystemTimeFunction(l);
						}
	
						
					} 				
					
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			
			// At least every Twelve hours the system time should be set
			//mHandler.postDelayed(mSetSystemTime, twelveHours);
			
			//maybe every hour would be better as this would not leave them with an a half day of bad time???
			mHandler.postDelayed(mSetSystemTime, oneHour);
		}
		
	};
	
	/*
	 * Just a notification to tell user they are on manual gps 
	 */
	public void stillUsingManualGPS(){
		Toast.makeText(this, "Still using Manual GPS, disable to test automatic time setting", Toast.LENGTH_LONG).show();
	}
	
	/*
 	 * Records the date time of the system and the UTC time
 	 */
	private Runnable mRecordTimeInTextTask = new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
			System.out.println("record text");
			Date date = new Date();

			// set the UTC time
			try {
				// check if GPS enabled
				if (gps != null) {
					gps.getLocation();
					GPSTime = gps.getGPSTime();

					date.setTime(GPSTime);

					
					// UTC time
					if (date.getSeconds() < 10) {
						if (date.getMinutes() < 10) {
							writeDate("" + date.getYear() + ", "
									+ date.getMonth() + ", " + date.getDate()
									+ ", " + date.getHours() + ":0"
									+ date.getMinutes() + ".0"
									+ date.getSeconds() + ";");
						} else {
							writeDate("" + date.getYear() + ", "
									+ date.getMonth() + ", " + date.getDate()
									+ ", " + date.getHours() + ":"
									+ date.getMinutes() + ".0"
									+ date.getSeconds() + ";");
						}
					} else {
						if (date.getMinutes() < 10) {
							writeDate("" + date.getYear() + ", "
									+ date.getMonth() + ", " + date.getDate()
									+ ", " + date.getHours() + ":0"
									+ date.getMinutes() + "."
									+ date.getSeconds() + ";");
						} else {
							writeDate("" + date.getYear() + ", "
									+ date.getMonth() + ", " + date.getDate()
									+ ", " + date.getHours() + ":"
									+ date.getMinutes() + "."
									+ date.getSeconds() + ";");
						}
					}
				} else {
					gps = new GPSTracker(AndroidGPSPosToTime.this);
				}

				long l = System.currentTimeMillis();
				if (!useManualGPS){
					l -= (((gps.getLongitude() * invertSign) * 4) * oneMinuteInSeconds * oneSecond);// TODO magic number
				}else{
					l -= (((manualLongitude * invertSign) * 4) * oneMinuteInSeconds * oneSecond);// TODO magic number
				}
				date.setTime(l);

				// system time
				if (date.getSeconds() < 10) {
					if (date.getMinutes() < 10) {
						writeDate("\t" + date.getYear() + ", "
								+ date.getMonth() + ", " + date.getDate()
								+ ", " + date.getHours() + ":0"
								+ date.getMinutes() + ".0" + date.getSeconds()
								+ "\n");
					} else {
						writeDate("\t" + date.getYear() + ", "
								+ date.getMonth() + ", " + date.getDate()
								+ ", " + date.getHours() + ":"
								+ date.getMinutes() + ".0" + date.getSeconds()
								+ "\n");
					}
				} else {
					if (date.getMinutes() < 10) {
						writeDate("\t" + date.getYear() + ", "
								+ date.getMonth() + ", " + date.getDate()
								+ ", " + date.getHours() + ":0"
								+ date.getMinutes() + "." + date.getSeconds()
								+ "\n");
					} else {
						writeDate("\t" + date.getYear() + ", "
								+ date.getMonth() + ", " + date.getDate()
								+ ", " + date.getHours() + ":"
								+ date.getMinutes() + "." + date.getSeconds()
								+ "\n");
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			}

			// writes to file every hour, hopefully the time changing doesn't
			// affect this...
			mHandler.postDelayed(mRecordTimeInTextTask, oneHour);
		}
	};

	/*
	 * Sets the values for the degrees minutes and second number pickers if the
	 * gps has been instantiated
	 */
	private void setNumberPickerForDMS() {
		if (gps.canGetLocation()) {

			double degreestemp = gps.getLongitude();

			double minutestemp = degreestemp;
			minutestemp -= (int) degreestemp;
			minutestemp = minutestemp * invertSign;
			minutestemp = minutestemp * 60;

			double secondstemp = minutestemp;
			secondstemp -= (int) minutestemp;
			secondstemp = secondstemp * 60;

			degrees.setValue(Math.abs((int) degreestemp));
			minutes.setValue((int) minutestemp);
			seconds.setValue((int) secondstemp);

			// ex 15.1258 (0.1258*60)/*minutes*/= 7.548 .548*60 = 32.88/*seconds*/

		}
	}

	/*
	 * This function needs a rooted device and the /dev/alarm file needs to be at
	 * least 666 file permission - done with chmod 666 /dev/alarm.
	 * 
	 * With out a rooted systems system time will fail to set as
	 * the permissions on the file won't be correct other wise.
	 */
	public void setSystemTimeFunction(long l) {// view needed if its a button
		//System.out.println("SetSystemTimeFunction");

		if (android.os.SystemClock.setCurrentTimeMillis(l)) {
			System.out.println("success Clock set");
		} else {
			System.out.println("fail clock NOT s3t");
		}

	}// end function setSystemTimeFunction

	/*
     * Writes the current boat time and the UTC time to a file on the sdcard
     * @param String containing the datetimes to be saved
     */
	@SuppressLint("SdCardPath")
	public void writeDate(String message) {
		//System.out.println("writedata");
		
		FileWriter fw;
		try {
			fw = new FileWriter("/sdcard/gpsTimeFile.txt", true);
			fw.write(message);
			fw.flush();
			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/*
	 * 
	 */
	public void manualSetTime(View v){
		Toast.makeText(this, "manualSetTime" , Toast.LENGTH_SHORT).show();
		chmodAlarmFile();
		mHandler.postDelayed(mSetSystemTime, oneSecond);
		
	}
	
	/*
	 * 
	 */
	public void enableManualGPSEntry(View v){
		//Toast.makeText(this, "manualGPS", Toast.LENGTH_SHORT).show();
		if (showManualGPS){
			//cancel
			hideManualGPS();
			showManualGPS = false;
		}else{
			showManualGps();
			showManualGPS = true;
		}
	}
	
	
	
	/*
	 * 
	 */
	public void setLocation(View v ){
		//Toast.makeText(this, "setLocation", Toast.LENGTH_SHORT).show();
		
		hideManualGPS();
		
		if (!useManualGPS){
			useManualGPS = true;
			manualGPSInUseTextView.setText("Manual GPS: Enabled");
			Toast.makeText(this, "Using Manual GPS ", Toast.LENGTH_LONG).show();
			btnSetLocation.setEnabled(true);
			btnSetLocation.setVisibility(View.VISIBLE);
			btnSetLocation.setText("Cancel Manual GPS");
			
			
			btnUseNumberPickerButton.setEnabled(false);
			btnUseNumberPickerButton.setVisibility(View.GONE);
			
		}else{
			useManualGPS = false;
			manualGPSInUseTextView.setText("Manual GPS: Not in use");
			btnSetLocation.setText("Enable Manual GPS");
			showManualGPS = false;
			
			btnUseNumberPickerButton.setEnabled(true);
			btnUseNumberPickerButton.setVisibility(View.VISIBLE);
		}
		
		
	}
	
	/*
	 * Hides the widgets for entering the manual GPS position
	 */
	private void hideManualGPS(){
		//
		btnUseNumberPickerButton.setText("Enable Manual GPS Entry");
		
		//
		btnSetLocation.setEnabled(false);
		btnSetLocation.setVisibility(View.GONE);
		
		// number pickers
		degrees.setEnabled(false);
		degrees.setVisibility(View.GONE);
		minutes.setEnabled(false);
		minutes.setVisibility(View.GONE);
		seconds.setEnabled(false);
		seconds.setVisibility(View.GONE);
		
		// number picker labels
		findViewById(R.id.degreeTextView).setEnabled(false);
		findViewById(R.id.degreeTextView).setVisibility(View.GONE);
		findViewById(R.id.minutesTextView).setEnabled(false);
		findViewById(R.id.minutesTextView).setVisibility(View.GONE);
		findViewById(R.id.secondsTextView).setEnabled(false);
		findViewById(R.id.secondsTextView).setVisibility(View.GONE);
		
		//manual GPS in use TextView
		findViewById(R.id.manualGPSInUseTextView).setVisibility(View.VISIBLE);
	}
	
	
	/*
	 * Shows the widgets for entering the manual GPS position
	 */
	private void showManualGps(){
		btnSetLocation.setEnabled(true);
		btnSetLocation.setVisibility(View.VISIBLE);
		
		btnUseNumberPickerButton.setText("Cancel Manual GPS Entry");
		
		// number pickers 
		degrees.setEnabled(true);
		degrees.setVisibility(View.VISIBLE);
		minutes.setEnabled(true);
		minutes.setVisibility(View.VISIBLE);
		seconds.setEnabled(true);
		seconds.setVisibility(View.VISIBLE);
		
		// number picker labels
		findViewById(R.id.degreeTextView).setEnabled(true);
		findViewById(R.id.degreeTextView).setVisibility(View.VISIBLE);
		findViewById(R.id.minutesTextView).setEnabled(true);
		findViewById(R.id.minutesTextView).setVisibility(View.VISIBLE);
		findViewById(R.id.secondsTextView).setEnabled(true);
		findViewById(R.id.secondsTextView).setVisibility(View.VISIBLE);
		
		//manual GPS in use TextView
		findViewById(R.id.manualGPSInUseTextView).setVisibility(View.GONE);
		
	}
	
	//
	
	/*
	 * Calls the function that changes the file permissions on /dev/alarm to 666
	 * Function requires su, so needs to be rooted. 
	 * This allows the program to change the time when it needs to.
	 */
	public void chmodAlarmFile() {
       
		System.out.println("ChmodAlarmFile Function called");
		
		File alarmFile = new File("/dev/alarm");
		try {
			System.out.println("Chemod Returns: " + chmod(alarmFile, 666) );
		} catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}          
    }

	/*
	 * Sets the file permission on the /dev/alarm file 
	 * Needs to be rooted 
	 * This function works
	 * http://stackoverflow.com/questions/11408154/how-to-get-file-permission-mode-programmatically-in-java
	 * 
	 * @param file you want to have permissions changed on.
	 * @param int the permissions you want to bestow on the file
	 * @returns 1 for success, or -1 for fail.
	 * 
	 * @see http://hi-android.info/src/android/os/FileUtils.java.html
	 * @see http://code.google.com/p/python-for-android/source/browse/android/Utils/src/com/googlecode/android_scripting/FileUtils.java?r=8bd1254c181b9a2caba52a9e1b34074b93c77d37
	 */
	public int chmod(File path, int mode) throws Exception {
		System.out.println("chmod Function");
		try{
		Class<?> fileUtils = Class.forName("android.os.FileUtils");
		Method setPermissions = fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
			// public static native int setPermissions(String file, int mode, int uid, int gid);
		return (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode, -1, -1);
		}catch(Exception e){
			System.out.println("Error: " + e);
			return -1;
		}
		
	}

	/*
	 * gets the permissions for a file 
	 * @param file you want to find permissions for.
	 * @returns int success, or -1 for fail
	 * 
	 * @see http://hi-android.info/src/android/os/FileUtils.java.html
	 * @see http://code.google.com/p/python-for-android/source/browse/android/Utils/src/com/googlecode/android_scripting/FileUtils.java?r=8bd1254c181b9a2caba52a9e1b34074b93c77d37
	 */
	private int getFilePermissions(File path){
		System.out.println("getFilePermissions Function");
		int[] returnedPermissions = new int[1];
		try {
			Class<?> fileUtils = Class.forName("android.os.FileUtils");
			Method getPermissions = fileUtils.getMethod("getPermissions", String.class, int[].class);
				//  public static native int getPermissions(String file, int[] outPermissions);
			getPermissions.invoke(null, path.getAbsolutePath(), returnedPermissions);
			
			for (int i = 0 ; i < returnedPermissions.length; i++){
				System.out.println("in for: "+returnedPermissions[i]);
			}
			return returnedPermissions[0];
		}catch (Exception e){
			System.out.println(e);
			return -1;
		}
	}
	
	// functions for application life cycle

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_android_gpspos_to_time, menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();
		setNumberPickerForDMS();
		// need to set this up so that it only happens once
		mHandler.postDelayed(mRecordTimeInTextTask, oneMinute);
		mHandler.postDelayed(mSetSystemTime, oneMinute);
		
		hideManualGPS();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {
		super.onStop();
		mHandler.removeCallbacks(mUpdateTimeTask);// so I should keep this on if
													// it will keep going

		// removed as the button is a good kill switch for now.
		// mHandler.removeCallbacks(mRecordTimeInTextTask);// does it keep on
		// going or does it dye when the program stops running... no it keeps
		// going :)

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, oneSecond);
		setNumberPickerForDMS();
		hideManualGPS();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	public void onRestart() {
		super.onRestart();
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, oneSecond);
		setNumberPickerForDMS();
		hideManualGPS();
	}

}






























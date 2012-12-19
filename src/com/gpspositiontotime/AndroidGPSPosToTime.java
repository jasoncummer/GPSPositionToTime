package com.gpspositiontotime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

//import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
//import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AndroidGPSPosToTime extends Activity {// implements
													// OnSeekBarChangeListener {
	// UI widgets
	Button btnSetLocation;
	Button btnUseNumberPickerButton;
	Button btnSetGPSTime;
	Button btnChangeTime;
	Button btnChangeDate;
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
	
	// manual date
	Date manualDate = new Date(System.currentTimeMillis());
	
	// times in milliseconds
	//private static long twelveHours =  43200000;
	private static long oneHour =  3600000;
	private static long oneMinute = 60000;
	private static long oneSecond = 1000;
	
	// time multipliers
	private static int oneMinuteInSeconds = 60;
	//private static int oneHourInSeconds = 60;
	//private static int oneDayInHours = 24;

	// other multipliers
	private static int invertSign = -1;
	
	// spacetime variables
	GPSTracker gps;
	long GPSTime = 0;
	long lastKnownGPSTime = 0;
	long currentTime = 0; 
	double manualLongitude;
	private static int startingYearOfVoyage = 112; 
	private static long minutesPerDegree = 4;// (24 hours  * 60 miuntes) / 360
	private static long longitudeToMillisConversionFactor = minutesPerDegree * oneMinuteInSeconds * oneSecond;
	
	
	
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

		btnSetGPSTime = (Button) findViewById(R.id.setGPSTimeButton);
		btnSetLocation = (Button) findViewById(R.id.btnSetLocation);
		btnChangeTime = (Button) findViewById(R.id.btnChangeTime);
		btnChangeDate =(Button) findViewById(R.id.btnChangeDate);	
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
		
		//have this hidden and disabled because I ran out of time
		btnUseNumberPickerButton.setVisibility(View.GONE);
		btnUseNumberPickerButton.setEnabled(false);
		
		manualGPSInUseTextView.setVisibility(View.GONE);
		manualGPSInUseTextView.setAlpha(0f);
		manualGPSInUseTextView.setEnabled(false);

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
		@SuppressWarnings("deprecation")
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
							l -= (((gps.getLongitude() * invertSign) * 4) * oneMinuteInSeconds * oneSecond);
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
	
	
	@SuppressWarnings("deprecation")
	public void onSetTimeButtonClick(View v) {
 
				showDialog(1);
	}
	
	@SuppressWarnings("deprecation")
	public void onSetDateButtonClick(View v) {
		showDialog(2);
		
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			// set time picker as current time
			return new TimePickerDialog(this, timePickerListener, manualDate.getHours(), manualDate.getMinutes(),false);
		case 2:
			return new DatePickerDialog(this, datePickerListener, manualDate.getYear(),manualDate.getMonth(),manualDate.getDate()); //TODO GET a date for this
 
		}
		return null;
	}
	
	/*
	 * Records the a manual time, date, and a time delta in a file for manual time system
 	 */
	private Runnable mRecordManualTimeInTextTask = new Runnable() {
		/*
		 * (non-Javadoc)
		 * Writes to file every minute, hopefully the system time changes don't
		 * affect this...
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
//			System.out.println("mRecordManualTimeInTextTask");
//			
//			Long dateInMillis = 0l;
//			
//			File manualTimeFile = new File(Environment.getExternalStorageDirectory().getPath() + "/ManualTimeFile.txt");
//			if (!manualTimeFile.exists()){
//				System.out.println("createing Time Delta file");
//				writeManualDate(""+ manualDate.getTime()); 
//			}
//			
//			File manualTimeDeltaFile = new File(Environment.getExternalStorageDirectory().getPath() + "/ManualTimeDeltaFile.txt");
//			if(!manualTimeDeltaFile.exists()){
//				System.out.println("createing Time Delta file");
//				writeTimeDelta("0");
//			}
//			
//
//			// get the UTC time
//			try {
//				
//					try {
//						
//						BufferedReader buf = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getPath() + "/ManualTimeFile.txt"));
//						
//						// reads file and casts to a long
//						dateInMillis = Long.parseLong(buf.readLine());
//						
//						
//						buf.close();
//					}catch(Exception e){
//						System.out.println(e);
//					}
//					
//
//					try {
//						//FileReader ManualTimeDeltaFileReader = new FileReader("/sdcard/ManualTimeDeltaFile.txt");
//						BufferedReader buf = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getPath() + "/ManualTimeDeltaFile.txt"));
//						
//						long timeDelta = Long.parseLong(buf.readLine());
//						timeDelta += 1000;// TODO add final value here oneMinute
//						String s = "" + timeDelta;
//						System.out.println(s);
//						
//						if (dateInMillis != 0){
//							dateInMillis += timeDelta;
//							s = "" + dateInMillis;
//							writeTimeDelta("1000");
//						}else {
//							timeDeltaError();
//						}
//						
//						buf.close();
//					}catch(Exception e){
//						System.out.println(e);
//					}
//				
//			} catch (Exception e) {
//				System.out.println(e);
//			}
//			System.out.println("here");
//			// TODO Testing
//			mHandler.postDelayed(mRecordTimeInTextTask, oneSecond);
		}
	};
	
	private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
		@SuppressWarnings("deprecation")
		public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
			manualDate.setHours(selectedHour);
			manualDate.setMinutes(selectedMinute);
		}
	};
	

	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
		
		// when dialog box is closed, below method will be called.
		@SuppressWarnings("deprecation")
		public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
			manualDate.setYear(selectedYear);
			manualDate.setMonth(selectedMonth);
			manualDate.setDate(selectedDay);
		}
	};
	
	 /* Writes the current boat time and the UTC time to a file on the sdcard
     * @param String containing the datetime to be saved
     */
	@SuppressLint("SdCardPath")
	public void writeManualDate(String message) {
//		System.out.println("writeManualdate");
//		
//		FileWriter fw;
//		try {
//			fw = new FileWriter("/sdcard/manualTimeFile.txt", false);
//			fw.write(message);
//			fw.flush();
//			fw.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}

	}
	
	 /* Writes the current boat time and the UTC time to a file on the sdcard
     * @param String containing the time Delta to be saved
     */
	@SuppressLint("SdCardPath")
	public void writeTimeDelta(String message) {
//		System.out.println("writeTimeDelta");
//		
//		FileWriter fw;
//		try {
//			fw = new FileWriter("/sdcard/manualTimeDeltaFile.txt", false);
//			fw.write(message);
//			fw.flush();
//			fw.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
	}


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
		
		if (!useManualGPS){ // as in you were using device gps > sets to manual gps
			useManualGPS = true;
			manualGPSInUseTextView.setText("Manual GPS: Enabled");
			Toast.makeText(this, "Using Manual GPS ", Toast.LENGTH_LONG).show();
			btnSetLocation.setEnabled(true);
			btnSetLocation.setVisibility(View.VISIBLE);
			btnSetLocation.setText("Cancel Manual GPS");
			
			
			btnUseNumberPickerButton.setEnabled(false);
			btnUseNumberPickerButton.setVisibility(View.GONE);
			
			mHandler.postDelayed(mRecordManualTimeInTextTask, oneSecond);
			
		}else{ // as in you have been using manual gps > sets to device gps
			useManualGPS = false;
			manualGPSInUseTextView.setText("Manual GPS: Not in use");
			btnSetLocation.setText("Enable Manual GPS");
			showManualGPS = false;
			
			btnUseNumberPickerButton.setEnabled(true);
			btnUseNumberPickerButton.setVisibility(View.VISIBLE);
			
			mHandler.removeCallbacks(mRecordManualTimeInTextTask);
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
		
		btnSetGPSTime.setVisibility(View.VISIBLE);
		
		btnChangeTime.setVisibility(View.GONE);
		btnChangeDate.setVisibility(View.GONE);
		
		btnChangeTime.setEnabled(false);
		btnChangeDate.setEnabled(false);
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
		
		btnSetGPSTime.setVisibility(View.GONE);
		
		btnChangeTime.setVisibility(View.VISIBLE);
		btnChangeDate.setVisibility(View.VISIBLE);
		btnChangeTime.setEnabled(true);
		btnChangeDate.setEnabled(true);
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

	// messages from runnables 
	private void timeDeltaError(){
		Toast.makeText(this, "time Delta Error" , Toast.LENGTH_LONG).show();
	}
	
	/*
	 * Just a notification to tell user they are on manual gps 
	 */
	public void stillUsingManualGPS(){
		Toast.makeText(this, "Still using Manual GPS, disable to test automatic time setting", Toast.LENGTH_LONG).show();
	}
	
}






























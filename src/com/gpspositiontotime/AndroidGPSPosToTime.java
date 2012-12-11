package com.gpspositiontotime;

import java.io.FileWriter;
import java.io.IOException;
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
	Button btnSetLocation;

	SeekBar sbLongitudeSeekBar;
	TextView utcTextView;
	public TextView boatTimeClockView;
	NumberPicker degrees;
	NumberPicker minutes;
	NumberPicker seconds;
	GPSTracker gps;
	long GPSTime = 0;
	long lastKnownGPSTime = 0;
	long currentTime = 0; 

	private Handler mHandler = new Handler();

	double fakeLongitude;
	
	// times are in milliseconds
	private static long twelveHours =  43200000;
	private static long oneHour =  3600000;
	private static long oneSecond = 1000;

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

		boatTimeClockView = (TextView) findViewById(R.id.boatClockTextView);
		utcTextView = (TextView) findViewById(R.id.utc_clock_textView);

		degrees = (NumberPicker) findViewById(R.id.gpsDegreesNumberPicker);
		degrees.setMaxValue(180);
		degrees.setMinValue(0);

		minutes = (NumberPicker) findViewById(R.id.gpsMinutesNumberPicker);
		minutes.setMaxValue(60);
		minutes.setMinValue(0);

		seconds = (NumberPicker) findViewById(R.id.gpsSecondsNumberPicker);
		seconds.setMaxValue(60);
		seconds.setMinValue(0);

	}// end function onCreate

	/*
     * 
     */
	private Runnable mUpdateTimeTask = new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {

			//System.out.println("mUpdateTimeTask");
			
			Date date = new Date();

			try {
				
				// check if GPS enabled
				if (gps != null) {
					System.out.println("gps !null");
					gps.getLocation();
					GPSTime = gps.getGPSTime();
					
					if (lastKnownGPSTime == GPSTime){
						currentTime += 1000;
					}else{
						lastKnownGPSTime = GPSTime;
						currentTime = GPSTime;
					}
					
					// set UTC Clock
					date.setTime(currentTime);
					//date.setTime(GPSTime);
					System.out.println("gmt "+date.toGMTString());
					
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
					l -= (((gps.getLongitude() * -1) * 4) * 60 * 1000);
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
					
				} else {
					gps = new GPSTracker(AndroidGPSPosToTime.this);
				}

				
				
			} catch (Exception e) {
				System.out.println(e);
			}

			
			// updates the time every second
			//mHandler.postDelayed(mUpdateTimeTask, 1000);
			//test
			mHandler.postDelayed(mUpdateTimeTask, oneSecond);
		}
	};

	/*
	 * 
	 */
	private Runnable mSetSystemTime = new Runnable(){
		
		/*
		 * 
		 */
		public void  run(){
			Date date = new Date();
			try {
				
				if (gps == null){
					gps = new GPSTracker(AndroidGPSPosToTime.this);
				}
				
				if (gps != null) {
					gps.getLocation();
					GPSTime = gps.getGPSTime();
					date.setTime(GPSTime);

					// adjust for longitude
					long l = GPSTime;
					l -= (((gps.getLongitude() * -1) * 4) * 60 * 1000);
					date.setTime(l);
					
					
					setSystemTimeFunction(l);
				} 				
				
			} catch (Exception e) {
				System.out.println(e);
			}
			
			// At least every Twelve hours the system time should be set
			//mHandler.postDelayed(mSetSystemTime, twelveHours);
			
			//maybe every hour would be better as this would not leave them with an a half day of bad time???
			mHandler.postDelayed(mSetSystemTime, oneHour);
		}
		
	};
	
	/*
 	 * 
 	 */
	private Runnable mRecordTimeInTextTask = new Runnable() {
		@SuppressWarnings("deprecation")
		public void run() {
//			System.out.println("record text");
//			Date date = new Date();
//
//			// set the UTC time
//			try {
//				// check if GPS enabled
//				if (gps != null) {
//					gps.getLocation();
//					GPSTime = gps.getGPSTime();
//
//					date.setTime(GPSTime);
//
//					if (date.getSeconds() < 10) {
//						if (date.getMinutes() < 10) {
//							writeDate("" + date.getYear() + ", "
//									+ date.getMonth() + ", " + date.getDate()
//									+ ", " + date.getHours() + ":0"
//									+ date.getMinutes() + ".0"
//									+ date.getSeconds() + ";");
//						} else {
//							writeDate("" + date.getYear() + ", "
//									+ date.getMonth() + ", " + date.getDate()
//									+ ", " + date.getHours() + ":"
//									+ date.getMinutes() + ".0"
//									+ date.getSeconds() + ";");
//						}
//					} else {
//						if (date.getMinutes() < 10) {
//							writeDate("" + date.getYear() + ", "
//									+ date.getMonth() + ", " + date.getDate()
//									+ ", " + date.getHours() + ":0"
//									+ date.getMinutes() + "."
//									+ date.getSeconds() + ";");
//						} else {
//							writeDate("" + date.getYear() + ", "
//									+ date.getMonth() + ", " + date.getDate()
//									+ ", " + date.getHours() + ":"
//									+ date.getMinutes() + "."
//									+ date.getSeconds() + ";");
//						}
//					}
//				} else {
//					gps = new GPSTracker(AndroidGPSPosToTime.this);
//				}
//
//				long l = GPSTime;
//				l -= (((gps.getLongitude() * -1) * 4) * 60 * 1000);
//				date.setTime(l);
//
//				// Set system time
//				if (date.getSeconds() < 10) {
//					if (date.getMinutes() < 10) {
//						writeDate("\t" + date.getYear() + ", "
//								+ date.getMonth() + ", " + date.getDate()
//								+ ", " + date.getHours() + ":0"
//								+ date.getMinutes() + ".0" + date.getSeconds()
//								+ "\n");
//					} else {
//						writeDate("\t" + date.getYear() + ", "
//								+ date.getMonth() + ", " + date.getDate()
//								+ ", " + date.getHours() + ":"
//								+ date.getMinutes() + ".0" + date.getSeconds()
//								+ "\n");
//					}
//				} else {
//					if (date.getMinutes() < 10) {
//						writeDate("\t" + date.getYear() + ", "
//								+ date.getMonth() + ", " + date.getDate()
//								+ ", " + date.getHours() + ":0"
//								+ date.getMinutes() + "." + date.getSeconds()
//								+ "\n");
//					} else {
//						writeDate("\t" + date.getYear() + ", "
//								+ date.getMonth() + ", " + date.getDate()
//								+ ", " + date.getHours() + ":"
//								+ date.getMinutes() + "." + date.getSeconds()
//								+ "\n");
//					}
//				}
//			} catch (Exception e) {
//				System.out.println(e);
//			}
//
//			// writes to file every hour, hopefully the time changing doesn't
//			// affect this...
//			mHandler.postDelayed(mRecordTimeInTextTask, 60000);
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
			minutestemp = minutestemp * -1;
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
     * 
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/*
	 * 
	 */
	public void enableManualGPSEntry(View v){
		Toast.makeText(this, "manualGPS", Toast.LENGTH_SHORT).show();
	}
	
	/*
	 * 
	 */
	public void manualSetTime(View v){
		Toast.makeText(this, "manualSetTime" , Toast.LENGTH_SHORT).show();
		mHandler.postDelayed(mSetSystemTime, 1000);
		//gps.showSettingsAlert();// works
	}
	
	/*
	 * 
	 */
	public void setLocation(View v ){
		Toast.makeText(this, "setLocation", Toast.LENGTH_SHORT).show();
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
		mHandler.postDelayed(mRecordTimeInTextTask, 1000);
		mHandler.postDelayed(mSetSystemTime, 1000);
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
		mHandler.postDelayed(mUpdateTimeTask, 1000);
		setNumberPickerForDMS();
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
		mHandler.postDelayed(mUpdateTimeTask, 1000);
		setNumberPickerForDMS();
	}

}


















































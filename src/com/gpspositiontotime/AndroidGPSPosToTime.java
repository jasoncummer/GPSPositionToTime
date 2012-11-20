package com.gpspositiontotime;

//import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

//import com.gpspositiontotime.R.id;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
//import android.text.format.Time;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.SeekBar;
//import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidGPSPosToTime extends Activity {//implements OnSeekBarChangeListener {
	Button btnShowLocation;
	
	SeekBar sbLongitudeSeekBar;
	TextView longitudeTextView;
	TextView utcTextView;
	DigitalClock dg;
	public TextView text_view_two;
	
	//GPSTracker class
	GPSTracker gps;
	long GPSTime = 0;
	//int timeDelta;
	
	private Handler mHandler = new Handler();
	
	double fakeLongitude;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_gpspos_to_time);
        
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        
        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				// create class object
				gps = new GPSTracker(AndroidGPSPosToTime.this);
				
				//check if GPS enabled
				if(gps.canGetLocation()){
					double latitude = gps.getLatitude();
					double longitude = gps.getLongitude();
					long GPStime = gps.getGPSTime();
					
					// \n is for new line
					Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: "  + longitude + "\nGPStime: " + GPStime, Toast.LENGTH_LONG).show();
				}
				
			}
		} );
        
        text_view_two = (TextView) findViewById(R.id.textView2);
        utcTextView = (TextView) findViewById(R.id.utc_textView);
        sbLongitudeSeekBar = (SeekBar) findViewById(R.id.longitudeSeekBar) ;
        longitudeTextView = (TextView) findViewById(R.id.longitudeTextView);
        
        sbLongitudeSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				//longitudeTextView.setText("SeekBar value is "+ progress);
				fakeLongitude = progress;
				
				calcNewTime(progress);
			}
			public void onStopTrackingTouch(SeekBar seekBar) {}
			public void onStartTrackingTouch(SeekBar seekBar) {}
		});
        
       
       
        
    }// end function onCreate
    
    private Runnable mUpdateTimeTask = new Runnable() {
 	   @SuppressWarnings("deprecation")
		public void run() {
 		   
 		   Date date =  new Date();
 		   long l = System.currentTimeMillis();
 		   l -= ( (fakeLongitude *4 ) * 60 * 1000) ;
 		   date.setTime(l);
 		   
 		   
 		   // Set system time 
 		   if (date.getSeconds() < 10) {
 			   if (date.getMinutes() < 10 ){
 				   text_view_two.setText("" + date.getHours() + ":0" + date.getMinutes() + ".0" + date.getSeconds() );
 			   }else {
 				   text_view_two.setText("" + date.getHours() + ":" + date.getMinutes() + ".0" + date.getSeconds() );
 				   //setSystemTimeFunction
 			   }
 	       } else {
 	    	   if (date.getMinutes() < 10 ){
 	    		  text_view_two.setText("" + date.getHours() + ":0" + date.getMinutes() + "." + date.getSeconds());      
 	    	   }else {
 	    		  text_view_two.setText("" + date.getHours() + ":" + date.getMinutes() + "." + date.getSeconds());      
 	    		  //setSystemTimeFunction
 	    	   }
 	       }
 	     
 		   //set the UTC time
 		   //check if GPS enabled
 		   try {
 			   if( gps != null){
 				  gps.getLocation();
 				  GPSTime = gps.getGPSTime();
 				 System.out.println(GPSTime);
 				  date.setTime(GPSTime);
 				  utcTextView.setText("" + date.getHours() + ":" + date.getMinutes() + "." + date.getSeconds());
 				  	
 			   }else {
 				   gps = new GPSTracker(AndroidGPSPosToTime.this);
 			   }
 			   
 		   }catch (Exception e){
 			   System.out.println(e);
 		   }
			
 		   
 	       mHandler.postDelayed(mUpdateTimeTask, 1000);
 	   }
 	};
   
 
    public void calcNewTime(int longitude){
		
    	int timeDelta = longitude * 4;//<todo> double check conversion times for degrees
    	
		longitudeTextView.setText("longitude: "+ longitude + ", delta: " + timeDelta);
		
		//String timeString = DateFormat.getDateTimeInstance().format(new Date());
		
		//Calendar c = Calendar.getInstance();

		Date date =  new Date();
		long l = System.currentTimeMillis();
		l -=  (timeDelta * 60 * 1000) ;
		date.setTime(l);
		
		System.out.println(date);
		
		text_view_two.setText(date.toString());
		
		// 4min / degree
    }

    /// this function is to set the rooted systems time 
    // will fail to set clock as the permissions on the file wont be correct 
    // other wise
    public void setSystemTimeFunction(View v){// view needed if its a button
    	
    	Calendar c = Calendar.getInstance();
    	Date d = c.getTime();
    	long l = d.getTime();
    	l -= 3*60*60*1000;
    	
    	Toast.makeText(this, d.toString() , Toast.LENGTH_LONG).show();
    	
    	
    	System.out.println(l);
    	
    	// this needs a rooted device and the /dev/alarm file needs to be at least 666 file permission
    	if(android.os.SystemClock.setCurrentTimeMillis(l)){
    		System.out.println("success Clock set");
    	}else{
    		System.out.println("fail clock NOT s3t");
    	}
    		
    }// end function setSystemTimeFunction

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_android_gpspos_to_time, menu);
        return true;
    }
    
    // functions for application life cycle
    @Override
	public void onStop(){
		super.onStop();
		mHandler.removeCallbacks(mUpdateTimeTask);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 1000);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		mHandler.removeCallbacks(mUpdateTimeTask);
	}
	
	@Override
	public void onRestart(){
		super.onRestart();
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 1000);
	}
    
}

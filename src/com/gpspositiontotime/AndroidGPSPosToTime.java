package com.gpspositiontotime;


import java.util.Calendar;
import java.util.Date;


import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidGPSPosToTime extends Activity {//implements OnSeekBarChangeListener {
	Button btnShowLocation;
	
	SeekBar sbLongitudeSeekBar;
	TextView longitudeTextView;
	TextView utcTextView;
	public TextView text_view_two;
	NumberPicker degrees;
	NumberPicker minutes;
	NumberPicker seconds;
	//GPSTracker class
	GPSTracker gps;
	long GPSTime = 0;
	
	
	private Handler mHandler = new Handler();
	
	double fakeLongitude;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_gpspos_to_time);
        
        gps = new GPSTracker(AndroidGPSPosToTime.this);
        
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        
        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
		
			public void onClick(View arg0) {
				
				// this will have to take the current values in the number pickers and make them into a time
				// or 
				// this could be a call to a menu/ view to set them - which I think is a better implementation...
				
				//check if GPS enabled 
				if(gps.canGetLocation()){
					double latitude = gps.getLatitude();
					double longitude = gps.getLongitude();
					long GPStime = gps.getGPSTime();
					
					// \n is for new line
					Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: "  + longitude + "\nGPStime: " + GPStime, Toast.LENGTH_LONG).show();
					
					double degreestemp = gps.getLongitude();
					
					double minutestemp = degreestemp;
					minutestemp -= (int)degreestemp ;
					minutestemp = minutestemp * -1;
					minutestemp = minutestemp * 60;
					
					
					double secondstemp = minutestemp;
					secondstemp -= (int)minutestemp;
					secondstemp = secondstemp * 60;
										
					degrees.setValue(Math.abs((int)degreestemp));
					
					minutes.setValue((int)minutestemp);
					  
					seconds.setValue((int)secondstemp); 
					

				}
				
			}
		} );
        
        //setNumberPickerForDMS();
        
        text_view_two = (TextView) findViewById(R.id.textView2);
        utcTextView = (TextView) findViewById(R.id.utc_textView);
        longitudeTextView = (TextView) findViewById(R.id.longitudeTextView);
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
    
    private Runnable mUpdateTimeTask = new Runnable() {
 	   @SuppressWarnings("deprecation")
		public void run() {
 		   
 		   Date date =  new Date();
 		   	     
 		   //set the UTC time
 		   //check if GPS enabled
 		   try {
 			   if( gps != null){
 				  gps.getLocation();
 				  GPSTime = gps.getGPSTime();
 				 //System.out.println(GPSTime);
 				  date.setTime(GPSTime);
 				  
 				 if (date.getSeconds() < 10) {
 					  if (date.getMinutes() < 10 ){
 						 utcTextView.setText("" + date.getHours() + ":0" + date.getMinutes() + ".0" + date.getSeconds() );
 					  }else {
 						 utcTextView.setText("" + date.getHours() + ":" + date.getMinutes() + ".0" + date.getSeconds() );
 						  //setSystemTimeFunction
 					   }
 			       } else {
 			    	   if (date.getMinutes() < 10 ){
 			    		  utcTextView.setText("" + date.getHours() + ":0" + date.getMinutes() + "." + date.getSeconds());      
 			    	   }else {
 			    		  utcTextView.setText("" + date.getHours() + ":" + date.getMinutes() + "." + date.getSeconds());      
 			    		  //setSystemTimeFunction
 			    	   }
 			       }
 			   }else {
 				   gps = new GPSTracker(AndroidGPSPosToTime.this);
 			   }
 			   
 		   }catch (Exception e){
 			   System.out.println(e);
 		   }
			
 		   
 		  long l = GPSTime;
		  l -= ( ((gps.getLongitude()*-1) *4 ) * 60 * 1000) ;
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
 		   
 		   
 	       mHandler.postDelayed(mUpdateTimeTask, 1000);
 	   }
 	};
 	
 	// sets the values for the degrees minutes and second number pickers
 	//if the gps has been instantiated
 	private void setNumberPickerForDMS(){
 		if(gps.canGetLocation()){
			
			double degreestemp = gps.getLongitude();
			
			double minutestemp = degreestemp;
			minutestemp -= (int)degreestemp ;
			minutestemp = minutestemp * -1;
			minutestemp = minutestemp * 60;
			
			
			double secondstemp = minutestemp;
			secondstemp -= (int)minutestemp;
			secondstemp = secondstemp * 60;
			
			degrees.setValue(Math.abs((int)degreestemp));
			minutes.setValue((int)minutestemp);
			seconds.setValue((int)secondstemp); 
			
			//ex 15.1258 (0.1258*60)/*mins*/= 7.548  .548*60 = 32.88/*secs*/ 

		}
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
    
    @Override
    public void onStart(){
    	super.onStart();
    	setNumberPickerForDMS();
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
		setNumberPickerForDMS();
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
		setNumberPickerForDMS();
	}
    
}

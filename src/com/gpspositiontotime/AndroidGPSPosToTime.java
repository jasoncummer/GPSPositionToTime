package com.gpspositiontotime;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
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
					
					
					double degreestemp = degrees.getValue();
					
					double minutestemp = minutes.getValue();
					minutestemp = minutestemp / 60;
					
					
					double secondstemp = seconds.getValue(); 
					secondstemp = secondstemp / 60;
					
					//writeAlert("this will be a date, this one too!");
					
					// this is to kill the text recording function
					mHandler.removeCallbacks(mRecordTimeInTextTask);
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
 		   
 		   // updates the time every second
 	       mHandler.postDelayed(mUpdateTimeTask, 1000);
 	   }
 	};
 	
 	private Runnable mRecordTimeInTextTask = new Runnable() {
  	   @SuppressWarnings("deprecation")
 		public void run() {
  		   System.out.println("record text");
  		   Date date =  new Date();
  		   	     
  		   //set the UTC time
  		   //check if GPS enabled
  		   try {
  			   if( gps != null){
  				  gps.getLocation();
  				  GPSTime = gps.getGPSTime();
  				 
  				  date.setTime(GPSTime);
  				  
  				 if (date.getSeconds() < 10) {
  					  if (date.getMinutes() < 10 ){
  						  writeDate("" + date.getYear() + ", "+ date.getMonth()+", "+date.getDate() + ", " + date.getHours() + ":0" + date.getMinutes() + ".0" + date.getSeconds() + ";"  );
  					  }else {
  						  writeDate("" + date.getYear() + ", "+ date.getMonth()+", "+date.getDate() + ", " + date.getHours() + ":" + date.getMinutes() + ".0" + date.getSeconds() + ";" );
  					  }
  			       } else {
  			    	   if (date.getMinutes() < 10 ){
  			    		   writeDate("" + date.getYear() + ", "+ date.getMonth()+", "+date.getDate() + ", " + date.getHours() + ":0" + date.getMinutes() + "." + date.getSeconds() + ";");      
  			    	   }else {
  			    		   writeDate("" + date.getYear() + ", "+ date.getMonth()+", "+date.getDate() + ", " + date.getHours() + ":" + date.getMinutes() + "." + date.getSeconds() + ";");
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
 				 writeDate("\t" + date.getYear() + ", "+ date.getMonth()+", "+date.getDate() + ", " + date.getHours() + ":0" + date.getMinutes() + ".0" + date.getSeconds()+ "\n" );
 			  }else {
 				 writeDate("\t" + date.getYear() + ", "+ date.getMonth()+", "+date.getDate() + ", " + date.getHours() + ":" + date.getMinutes() + ".0" + date.getSeconds() + "\n");
 			   }
 	       } else {
 	    	   if (date.getMinutes() < 10 ){
 	    		  writeDate("\t" + date.getYear() + ", "+ date.getMonth()+", "+date.getDate() + ", " + date.getHours() + ":0" + date.getMinutes() + "." + date.getSeconds()+ "\n");      
 	    	   }else {
 	    		  writeDate("\t" + date.getYear() + ", "+ date.getMonth()+", "+date.getDate() + ", " + date.getHours() + ":" + date.getMinutes() + "." + date.getSeconds() + "\n");   
 	    	   }
 	       }
  		   
 		  System.out.println("record text");
 		  
 		// writes to file every hour, hopefully the time changing doesn't affect this...
  	       //mHandler.postDelayed(mRecordTimeInTextTask, 60000);
 		 mHandler.postDelayed(mRecordTimeInTextTask, 1000);
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

    @SuppressLint("SdCardPath")
	public void writeDate(String message){
    	System.out.println("writedata");
    	File gpsTimeFile =  new File("/sdcard/gpsTimeFile.txt");
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_android_gpspos_to_time, menu);
        return true;
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	setNumberPickerForDMS();
    	// need to set this up so that it only happens once
    	mHandler.postDelayed(mRecordTimeInTextTask, 1000);
    }
    
    // functions for application life cycle
    @Override
	public void onStop(){
		super.onStop();
		mHandler.removeCallbacks(mUpdateTimeTask);// so I should keep this on if it will keep going
		
		// removed as the button is a good kill switch for now.
		//mHandler.removeCallbacks(mRecordTimeInTextTask);// does it keep on going or does it dye when the program stops running... no it keeps going :)
		
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

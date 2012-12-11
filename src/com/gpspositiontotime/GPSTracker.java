package com.gpspositiontotime;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

public class GPSTracker extends Service implements LocationListener{

	private final Context mContext;
	
	//flag for GPS status
	boolean isGPSEnabled = false;
	
	//flag for network status
	boolean isNetworkEnabled = false;
	
	// flag for passive provider
	//boolean isPassiveProviderEnabled = false;
	
	boolean canGetLocation  = false;
	
	Location location; // location
	double latitude; // latitude
	double longitude; // longitude
	public long time;
	
	// The minimum distance to change Update in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
	
	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
	
	// Declaring a Location Manager
	protected LocationManager locationManager;

	private Bundle satellites;
	
	public GPSTracker(Context context) {
		this.mContext = context;
		getLocation();
	}
	
	public Location getLocation(){
		try {
			locationManager =  (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
			
			// getting GPS status
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
					
			// getting network status
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			
			// getting passive provider service
			//isPassiveProviderEnabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
			
			if(!isGPSEnabled ){//&& !isNetworkEnabled){
				// no network provider is enabled
				showSettingsAlert();
				
			}else if(!isGPSEnabled){
				Toast.makeText(this,"not enabled", Toast.LENGTH_LONG).show();
			}else{
				this.canGetLocation = true;
				// First get location from Network Provider
				if(isNetworkEnabled){
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
															MIN_TIME_BW_UPDATES,
															MIN_DISTANCE_CHANGE_FOR_UPDATES,
															this);
					//Log.d("Network", "Network");
					if(locationManager != null){
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if(location != null){
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}// if location != null
					}// if locationManager != null
				}// if isNetworkEnabled 
				
				// if GPS Enabled get lat/long using GPS Service
				if(isGPSEnabled){
					if (location == null){
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
															  MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
															  this );
						//Log.d("GPS Enabled","GPS Enabled");
						if(locationManager != null){
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if(location != null){
								latitude = location.getLatitude();
								longitude = location.getLongitude();
								time = location.getTime();
							}// end if (location != null))
						}// end if (locationManager != null)
					}// end if (location == null)
				}// end if isGPSEnabled
			}// end else (!isGPSEnabled && !isNetworkEnabled)
				
		} catch ( Exception e){
			e.printStackTrace();
		}
		
		return location;
	}
	
	/**
	 * Stop using GPS listener
	 * Calling the function will stop using GPS in your app
	 * */
	public void stopUsingGPS(){
		if (locationManager != null){
			locationManager.removeUpdates(GPSTracker.this);
		}
	}
	
	/**
	 * Function to get latitude
	 * */
	public double getLatitude(){
		if(location != null){
			latitude = location.getLatitude();
		}
		
		return latitude;
	}
	
	/**
	 * Function to get longitude
	 * */
	public double getLongitude(){
		if (location != null){
			longitude = location.getLongitude();
		}
		
		return longitude;
	}
	
	/**
	 * Function to get GPS time
	 * */
	public long getGPSTime(){
		if(location != null){
			time = location.getTime();
		}
		
		return time;
	}
	
	/**
	 * Function to get  # of GPS satellites
	 * */
	public Bundle getSatellites(){
		if(location != null){
			satellites = location.getExtras();
		}
		
		return satellites ;
	}
	
	/**
	 * Function to check if best network provider
	 * @return boolean
	 */
	public boolean canGetLocation(){
		return this.canGetLocation;
	}
	
	/**
	 * Function to show settings alert dialog
	 * */
	public void showSettingsAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
		
		//Setting Dialog Title
		alertDialog.setTitle("GPS settings");
		
		//Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled, Do you want to go to settings menu?");
		
		// Setting Icon to Dialog
		// alertDialog.setIcon(R.drawable.delete);
		
		// on pressing Setting button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});
			
		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			public void onClick (DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		
		// Showing Alert Message
		alertDialog.show();
	}
	
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}// end class GPSTracker

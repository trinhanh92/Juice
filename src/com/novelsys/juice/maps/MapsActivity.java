package com.novelsys.juice.maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.novelsys.juice.R;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

public class MapsActivity extends Activity implements LocationListener {
	private final String TAG = MapsActivity.class.getSimpleName();
	private GoogleMap mGoogleMap;
	private GPSTracker mGPSTracker;
	private ArrayList<LatLng> mMarkerPoints = new ArrayList<LatLng>();
	private double mLatitude = 0;
	private double mLongitude = 0;
	Location myLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		try {
			// loading map
			initilizeMap();
		} catch (Exception e) {
		}
		mGPSTracker = new GPSTracker(MapsActivity.this, mGoogleMap);

		if (mGPSTracker.canGetLocation()) {
			myLocation = mGPSTracker.getLocation();
		} else {
			mGPSTracker.showSettingsAlert();
		}
		if (myLocation != null) {
			onLocationChanged(myLocation);
		}
		init_location();
		mGoogleMap.setMyLocationEnabled(true); // false to disable
		// enable get current location button
		mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

		mGoogleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng destPoint) {
				// Already map contain destination location
				if (mMarkerPoints.size() > 1) {

					mMarkerPoints.clear();
					mGoogleMap.clear();
					LatLng startPoint = new LatLng(mLatitude, mLongitude);
					drawMarker(startPoint);
				}

				drawMarker(destPoint);

				// Checks, whether start and end locations are captured
				if (mMarkerPoints.size() >= 2) {
					LatLng origin = mMarkerPoints.get(0);
					LatLng dest = mMarkerPoints.get(1);

					// Getting URL to the Google Directions API
					String url = getDirectionsUrl(origin, dest);

					DownloadTask downloadTask = new DownloadTask();

					// Start downloading json data from Google Directions API
					downloadTask.execute(url);
				}

			}
		});

		mGoogleMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker mMarkerPoint) {
				LatLng myPos  = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
				String url = getDirectionsUrl(myPos, mMarkerPoint.getPosition());

				DownloadTask downloadTask = new DownloadTask();

				// Start downloading json data from Google Directions API
				downloadTask.execute(url);
				return false;
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			onSearchRequested();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * @brief: Function to load map
	 */
	private void initilizeMap() {
		if (mGoogleMap == null) {
			mGoogleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map_view)).getMap();

			// check map if it is created successfully or not
			if (mGoogleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	/**
	 * @brief function to initialize some locations
	 */
	private void init_location() {
		double location_arr[][] = { { 10.849151, 106.650880 },
				{ 10.842168, 106.642420 }, { 10.848532, 106.658041 },
				{ 10.842589, 106.660960 }, { 10.846720, 106.662290 } };
		double mLat, mLong;
		for (int i = 0; i < location_arr.length; i++) {
			mLat = location_arr[i][0];
			mLong = location_arr[i][1];
			Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
					.position(new LatLng(mLat, mLong)));
		}
	}

	/**
	 * @brief
	 * @param origin
	 * @param dest
	 * @return
	 */
	private String getDirectionsUrl(LatLng origin, LatLng dest) {

		// Origin of route
		String str_origin = "origin=" + origin.latitude + ","
				+ origin.longitude;

		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		// Sensor enabled
		String sensor = "sensor=true";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + parameters;

		return url;
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	/** A class to download data from Google Directions URL */
	private class DownloadTask extends AsyncTask<String, Void, String> {

		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			ParserTask parserTask = new ParserTask();

			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);

		}
	}

	/** A class to parse the Google Directions in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		// Parsing the data in non-ui thread
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				DirectionsJSONParser parser = new DirectionsJSONParser();

				// Starts parsing data
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		// Executes in UI thread, after the parsing process
		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> result) {
			ArrayList<LatLng> points = null;
			PolylineOptions lineOptions = null;
			String distance = "";
			String duration = "";

			if (result.size() < 1) {
				Toast.makeText(getBaseContext(), "No Points",
						Toast.LENGTH_SHORT).show();
				return;
			}

			// Traversing through all the routes
			for (int i = 0; i < result.size(); i++) {
				points = new ArrayList<LatLng>();
				lineOptions = new PolylineOptions();
				Log.i("MapsActivity", Integer.toString(result.size()));
				// Fetching i-th route
				List<HashMap<String, String>> path = result.get(i);

				// Fetching all the points in i-th route
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					if (j == 0) { // Get distance from the list
						distance = (String) point.get("distance");
						continue;
					} else if (j == 1) { // Get duration from the list
						duration = (String) point.get("duration");
						continue;
					}

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));

					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				Toast.makeText(getApplicationContext(),
						distance + "-" + duration, Toast.LENGTH_SHORT).show();
				// Adding all the points in the route to LineOptions
				lineOptions.addAll(points);
				lineOptions.width(10);
				lineOptions.color(Color.rgb(91, 127, 255));

			}

			// Drawing polyline in the Google Map for the i-th route
			mGoogleMap.addPolyline(lineOptions);
		}
	}

	private void drawMarker(LatLng point) {
		mMarkerPoints.add(point);

		// Creating MarkerOptions
		MarkerOptions options = new MarkerOptions();

		// Setting the position of the marker
		options.position(point);

		/**
		 * For the start location, the color of marker is GREEN and for the end
		 * location, the color of marker is RED.
		 */
		if (mMarkerPoints.size() == 1) {
			options.icon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
		} else if (mMarkerPoints.size() == 2) {
			options.title("new place");
			options.icon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		}

		// Add new marker to the Google Map Android API V2
		mGoogleMap.addMarker(options);
	}

	@Override
	public void onLocationChanged(Location location) {

		if (mMarkerPoints.size() < 2) {
			// Getting latitude of the current location
			mLatitude = location.getLatitude();

			// Getting longitude of the current location
			mLongitude = location.getLongitude();

			// Creating a LatLng object for the current location
			LatLng point = new LatLng(mLatitude, mLongitude);

			// Showing the current location in Google Map
			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(point));

			// Zoom in the Google Map
			mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

			drawMarker(point);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}

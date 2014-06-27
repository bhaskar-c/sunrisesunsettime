package com.knowpapa.sunrisesunsettime;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.knowpapa.astro.R;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends Activity {

	static final int DATE_PICKER_ID = 1111;

	private String latitude;
	private String longitude;
	private Date reqdDate;
	private String officialSunrise;
	private String officialSunset;
	private String astronomicalSunrise;
	private String astronomicalSunset;
	private String civilSunrise;
	private String civilSunset;
	private String nauticalSunrise;
	private String nauticalSunset;
	private String locationname;

	private String officialTransitTime;
	private String astronomicalTransitTime;
	private String civilTransitTime;
	private String nauticalTransitTime;

	private String selectedtimezoneidentifier;

	private ArrayAdapter<String> idAdapter;
	private TimeZone tz;

	private TextView latitudeTV;
	private TextView longitudeTV;
	private TextView timeZoneNameTV;
	private TextView officialSunriseTimeTV;
	private TextView officialSunsetTimeTV;
	private Spinner timeZoneSpinner;
	private Button dateButton;

	private TextView civilSunriseTV;
	private TextView civilSunsetTV;
	private TextView astronomicalSunriseTV;
	private TextView astronomicalSunsetTV;
	private TextView nauticalSunriseTV;
	private TextView nauticalSunsetTV;
	private TextView officalTransitTimeTV;
	private TextView civilTransitTimeTV;
	private TextView nauticalTransitTimeTV;
	private TextView astronomicalTransitTimeTV;
	EditText latitudeET;
	EditText longitudeET;
	EditText locationnameET;

	private TextView locationNameTV;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dateButton = (Button) findViewById(R.id.set_date_btn);
		latitudeTV = (TextView) findViewById(R.id.latitudeTV);
		longitudeTV = (TextView) findViewById(R.id.longitudeTV);
		timeZoneSpinner = (Spinner) findViewById(R.id.availableID);
		timeZoneNameTV = (TextView) findViewById(R.id.time_zone_name);
		locationNameTV = (TextView) findViewById(R.id.locationname);


		updateDateOnLaunch();
		loadSavedPreferences();
		populateAndUpdateTimeZone();
		updateDataAndCalculateSunriseSunset();

	}

	private void loadSavedPreferences() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		latitude = sharedPreferences.getString("latitude", "00.00");
		longitude = sharedPreferences.getString("longitude", "00.00");
		locationname = sharedPreferences.getString("locationname", "not set");
		selectedtimezoneidentifier = sharedPreferences.getString(
				"selectedtimezoneidentifier", TimeZone.getDefault().getID());

	}

	private void savePreferences() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = sharedPreferences.edit();
		editor.putString("latitude", latitude);
		editor.putString("longitude", longitude);
		editor.putString("locationname", locationname);
		editor.putString("selectedtimezoneidentifier",
				selectedtimezoneidentifier);
		editor.commit();
	}

	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {

			int year = selectedYear;
			int month = selectedMonth;
			int day = selectedDay;

			Calendar cal = Calendar.getInstance();
			cal.set(year, month, day);
			reqdDate = cal.getTime();
			dateButton.setText(formatDate(reqdDate));

		}
	};

	public void DecrementDateByOne(View v) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(reqdDate);
		cal.add(Calendar.DATE, -1);
		reqdDate = cal.getTime();
		dateButton.setText(formatDate(reqdDate));
		updateDataAndCalculateSunriseSunset();

	}

	@SuppressLint("SimpleDateFormat")
	public String formatDate(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
		return sdf.format(date);
	}

	public void IncrementDateByOne(View v) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(reqdDate);
		cal.add(Calendar.DATE, 1);
		reqdDate = cal.getTime();
		dateButton.setText(formatDate(reqdDate));
		updateDataAndCalculateSunriseSunset();
	}

	public void updateDataAndCalculateSunriseSunset() {

		// get data

		SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");

		try {
			reqdDate = sdf.parse(dateButton.getText().toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}

		selectedtimezoneidentifier = timeZoneSpinner.getSelectedItem()
				.toString();

		// process data

		Location location = new Location(latitude, longitude);
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(
				location, selectedtimezoneidentifier);

		Calendar cal = Calendar.getInstance();
		cal.setTime(reqdDate);

		officialSunrise = calculator.getOfficialSunriseForDate(cal);
		officialSunset = calculator.getOfficialSunsetForDate(cal);

		astronomicalSunrise = calculator.getAstronomicalSunriseForDate(cal);
		astronomicalSunset = calculator.getAstronomicalSunsetForDate(cal);
		civilSunrise = calculator.getCivilSunriseForDate(cal);
		civilSunset = calculator.getCivilSunsetForDate(cal);
		nauticalSunrise = calculator.getNauticalSunriseForDate(cal);
		nauticalSunset = calculator.getNauticalSunsetForDate(cal);

		updateUIWithResults();

	}

	@SuppressWarnings("deprecation")
	public void onSetDateBtnClicked(View v) {
		showDialog(DATE_PICKER_ID);
		updateDataAndCalculateSunriseSunset();
	}

	public void onSetLocationBtnClicked(View v) {
		final CharSequence[] items = {
				"Get Location from Map (Requires Internet)",
				"Get GPS Location", "Enter Latitude/Longitude" };
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Set Location:").setItems(items,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

						switch (which) {
						case 0:
							pickLocationMapActivity();
							break;
						case 1:
							pickLocationGPS();
							break;
						case 2:
							enterLatitudeLongitude();
							break;
						default:
							break;

						}

					}
				});
		builder.show();

	}

	private void enterLatitudeLongitude() {

		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.manual_select_latitude);
		dialog.setTitle("Enter Location:");
		dialog.show();

		Button dialogButton = (Button) dialog.findViewById(R.id.okbutton);
		dialogButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				EditText latitudeET = (EditText) dialog
						.findViewById(R.id.dia_latitude);

				EditText longitudeET = (EditText) dialog
						.findViewById(R.id.dia_longitude);

				EditText locationnameET = (EditText) dialog
						.findViewById(R.id.dia_locationname);

				latitude = latitudeET.getText().toString();
				longitude = longitudeET.getText().toString();
				locationname = locationnameET.getText().toString();
				savePreferences();
				updateDataAndCalculateSunriseSunset();
				dialog.dismiss();
			}

		});

	}

	public void pickLocationGPS() {

		GPSTracker gps = new GPSTracker(this);
		if (gps.canGetLocation()) { // gps enabled

			latitude = String.format("%.3f", gps.getLatitude());
			longitude = String.format("%.3f", gps.getLongitude());
			locationname = "not set";
			savePreferences();
			updateDataAndCalculateSunriseSunset();

		} else {
			gps.showSettingsAlert();
		}
		gps.stopUsingGPS();
	}

	private void pickLocationMapActivity() {

		Intent i = new Intent(MainActivity.this, LocationPickerActivity.class);
		MainActivity.this.startActivityForResult(i, 1);
	}

	private void populateAndUpdateTimeZone() {
		String[] idArray = TimeZone.getAvailableIDs();
		idAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout,
				idArray);

		idAdapter.setDropDownViewResource(R.layout.spinner_layout);
		timeZoneSpinner.setAdapter(idAdapter);
		for (int i = 0; i < idAdapter.getCount(); i++) {

			if (idAdapter.getItem(i).equals(selectedtimezoneidentifier)) {
				timeZoneSpinner.setSelection(i);
			}
		}

		timeZoneSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				selectedtimezoneidentifier = (String) (parent
						.getItemAtPosition(position));
				tz = TimeZone.getTimeZone(selectedtimezoneidentifier);
				String TimeZoneName = tz.getDisplayName();

				int TimeZoneOffset = tz.getRawOffset() / (60 * 1000);

				int hrs = TimeZoneOffset / 60;
				int mins = TimeZoneOffset % 60;

				timeZoneNameTV.setText(TimeZoneName + " : GMT " + hrs + "."
						+ mins);
				savePreferences();
				updateDataAndCalculateSunriseSunset();

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

	}

	private void updateDateOnLaunch() {
		reqdDate = new Date();
		dateButton.setText(formatDate(reqdDate));

	}

	private void updateUIWithResults() {
		locationNameTV.setText(locationname);

		latitudeTV.setText(latitude);
		longitudeTV.setText(longitude);

		officialSunriseTimeTV = (TextView) findViewById(R.id.official_sunrise_time);
		officialSunsetTimeTV = (TextView) findViewById(R.id.official_sunset_time);
		String clockfont = "digital-7.ttf";
		Typeface tf = Typeface.createFromAsset(getAssets(), clockfont);
		officialSunriseTimeTV.setTypeface(tf);
		officialSunsetTimeTV.setTypeface(tf);

		officialSunriseTimeTV.setText(officialSunrise);
		officialSunsetTimeTV.setText(officialSunset);

		// update table
		civilSunriseTV = (TextView) findViewById(R.id.civilSunriseTV);
		civilSunsetTV = (TextView) findViewById(R.id.civilSunsetTV);
		astronomicalSunriseTV = (TextView) findViewById(R.id.astronomicalSunriseTV);
		astronomicalSunsetTV = (TextView) findViewById(R.id.astronomicalSunsetTV);
		nauticalSunriseTV = (TextView) findViewById(R.id.nauticalSunriseTV);
		nauticalSunsetTV = (TextView) findViewById(R.id.nauticalSunsetTV);

		civilSunriseTV.setText(civilSunrise);
		civilSunsetTV.setText(civilSunset);
		astronomicalSunriseTV.setText(astronomicalSunrise);
		astronomicalSunsetTV.setText(astronomicalSunset);
		nauticalSunriseTV.setText(nauticalSunrise);
		nauticalSunsetTV.setText(nauticalSunset);

		officalTransitTimeTV = (TextView) findViewById(R.id.officialTransitTimeTV);
		civilTransitTimeTV = (TextView) findViewById(R.id.civilTransitTimeTV);
		nauticalTransitTimeTV = (TextView) findViewById(R.id.nauticalTransitTimeTV);
		astronomicalTransitTimeTV = (TextView) findViewById(R.id.astronomicalTransitTimeTV);

		officialTransitTime = transitTime(officialSunset, officialSunrise);
		astronomicalTransitTime = transitTime(astronomicalSunset,
				astronomicalSunrise);
		civilTransitTime = transitTime(civilSunset, civilSunrise);
		nauticalTransitTime = transitTime(nauticalSunset, nauticalSunrise);

		officalTransitTimeTV.setText(officialTransitTime);
		civilTransitTimeTV.setText(civilTransitTime);
		nauticalTransitTimeTV.setText(nauticalTransitTime);
		astronomicalTransitTimeTV.setText(astronomicalTransitTime);

	}

	protected String transitTime(String endtime, String starttime) {
		SimpleDateFormat dt = new SimpleDateFormat("hh:mm");
		Date startTime;
		Date endTime;
		long timdedifferencemillis = 0;
		try {
			startTime = dt.parse(starttime);
			endTime = dt.parse(endtime);
			timdedifferencemillis = endTime.getTime() - startTime.getTime(); // in
																				// milliseconds
		} catch (ParseException e) {
			e.printStackTrace();
		}

		int minutes = Math
				.abs((int) ((timdedifferencemillis / (1000 * 60)) % 60));
		int hours = Math
				.abs((int) ((timdedifferencemillis / (1000 * 60 * 60)) % 24));
		String hm = String.format("%02d h %02d min", hours, minutes);
		return hm;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				latitude = String.format("%.3f",
						data.getDoubleExtra("latitude", 0.000));
				longitude = String.format("%.3f",
						data.getDoubleExtra("longitude", 0.000));
				locationname = data.getStringExtra("locationname");
				savePreferences();
				updateDataAndCalculateSunriseSunset();

			}
			if (resultCode == RESULT_CANCELED) {
				latitude = "0.00";
				longitude = "0.00";
				locationname = "";
			}
			updateDataAndCalculateSunriseSunset();
		}
	}// on

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_PICKER_ID:
			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(this, datePickerListener, year, month,
					day);
		}
		return null;
	}

}

package qirui.hu.weatherapp.ui.home;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import qirui.hu.weatherapp.Helpers;
import qirui.hu.weatherapp.R;
import qirui.hu.weatherapp.TimeService;

import static qirui.hu.weatherapp.Helpers.getWeatherImage;
import static qirui.hu.weatherapp.Helpers.setWindArrowRotation;

public class HomeFragment extends Fragment implements GetAddress.OnTaskCompleted, GetWeathers.OnWeatherRetrieved {

    public static final double MILE_TO_KM = 1.60934;

    // root
    private View root;

    // display for debugging
    private TextView textHome;

    // display for temperature
    private String tempUnit = "\u2109";
    private int todayHighTemperature = 0;
    private int todayLowTemperature = 0;
    private int currentTemperature = 0;
    private String currentWeather = "";

    private TextView todayHighTempView;
    private TextView todayLowTempView;
    private TextView currentTempView;
    private TextView locationDisplay;
    private TextView todayDate;
    private TextView currentTime;
    private ImageView currentWeatherImage;
    private TextView currentWind;

    // Notification
    private Boolean sendNotification = false;

    // autoupdate
    private Boolean autoUpdate = false;
    private Date lasteUpdateTime;

    // Location service
    private Boolean isCurrentLocationOn = false;
    private FusedLocationProviderClient locationClient;
    private Location currentLocation;

    // Preference editor
    private SharedPreferences.Editor editor;

    // Weather informations
    private JSONObject[] forecast;
    private JSONObject[] hourly;

    // Weekly List view
    private RecyclerView weekdayView;
    private WeekdayAdapter weekdayAdapter;
    private RecyclerView.LayoutManager weekdayLayoutManager;

    // Hourly List view
    private RecyclerView hourlyView;
    private HourlyAdapter hourlyAdapter;
    private RecyclerView.LayoutManager hourlyLayoutManage;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // init weather data
        forecast = new JSONObject[14];
        hourly = new JSONObject[24];


        // init root
        root = inflater.inflate(R.layout.fragment_home, container, false);

        // find display views
        textHome = root.findViewById(R.id.text_home);

        todayHighTempView = root.findViewById(R.id.todayHighTemperature);
        todayLowTempView = root.findViewById(R.id.todayLowTemperature);
        currentTempView = root.findViewById(R.id.currentTemperature);
        locationDisplay = root.findViewById(R.id.home_location_display);
        todayDate = root.findViewById(R.id.todayDate);
        currentTime = root.findViewById(R.id.currentTime);
        currentWeatherImage = root.findViewById(R.id.currentWeather);
        currentWind = root.findViewById(R.id.currentWind);

        textHome.setText("Home page here!");

        // init location service client
        locationClient = LocationServices.getFusedLocationProviderClient(getContext());

        // init Preference editor
        editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Get time service, unregister at onStop()
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                timeServiceReceiver, new IntentFilter("TimeService"));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Q call on: ", "onResume()");

        // Preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String pref = "Reading preference";
        tempUnit = prefs.getString("temperatureUnit", "\u2109");
        sendNotification = prefs.getBoolean("sendNotification", false);
        autoUpdate = prefs.getBoolean("autoUpdate", false);
        isCurrentLocationOn = prefs.getBoolean("isCurrentLocationOn", true);
        retrieveWeatherData();


        pref = "Temp unit: " + tempUnit + "\nsend notification: " + sendNotification + "\nauto update: " + autoUpdate
        + "\ncurrent location: " + isCurrentLocationOn;


        // get last location
        if(isCurrentLocationOn) {
            if (!ensurePermission()) return;

            locationClient.getLastLocation().addOnSuccessListener(this.getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    currentLocation = location;
                    if(location != null){
//                        Log.d("Q get location: ", location.toString());
                        // get Address
                        new GetAddress(getContext(), HomeFragment.this).execute(location);
                        // get weather
                        new GetWeathers(getContext(), HomeFragment.this).getWeathers(location);

                    }
                }
            });
        }else{
            // Load saved current location from preference
            String currentLocationStr = prefs.getString("currentLocation", "Finding location ...");
            locationDisplay.setText(currentLocationStr);
            new GetAddress(getContext(), HomeFragment.this).execute();
            new GetWeathers(getContext(), HomeFragment.this).getWeathers(null);
        }

        // Today's weather display, default showing nothing
        todayHighTempView.setText("--" + tempUnit);
        todayLowTempView.setText("--" + tempUnit);
        currentTempView.setText("--" + tempUnit);
        currentWind.setText("--- -------");

        // set time and date
        setDateTime();

        // set debug information
        textHome.setText(pref);
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister timer service reciever
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(timeServiceReceiver);
    }

    // ! For Location !! CHECK permission on location, MUST HAVE
    private boolean ensurePermission() {
        boolean isGranted = true;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // let the caller know that, at present, we don't have permission.  But
            // get a request started.  The result of that request will be return via
            // the onRequestPermissionsResult() callback.
            isGranted = false;
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        return isGranted;
    }


    // TODO: combine get location, get adderss, get weather here
    public void retrieveWeatherData(){


        lasteUpdateTime = new Date();
    }

    // OnPost Call back for GetAddress
    @Override
    public void onTaskCompleted(String result) {
        String pref = textHome.getText().toString();
        pref += ("\n" + result);
        textHome.setText(pref);

        locationDisplay.setText(result);

        // save current location to preference
        editor.putString("currentLocation", result);
        editor.commit();
    }

    // Call back for GetWeathers
    @Override
    public void onForecastRetrieved(JSONArray forecast) {
        this.forecast = convertJSONArray(forecast);

        // TODO: update the display:
        // 1. today high and low temps DONE
        // 2. weekly weather list view
        try {
            JSONObject firstWeather = forecast.getJSONObject(0);
            JSONObject secondWeather = forecast.getJSONObject(1);

            // 1. high and low temps
            int firstTemp = firstWeather.getInt("temperature");
            int secondTemp = secondWeather.getInt("temperature");

            if(firstTemp > secondTemp){
                this.todayHighTemperature = firstTemp;
                this.todayLowTemperature = secondTemp;
            }else{
                this.todayHighTemperature = secondTemp;
                this.todayLowTemperature = firstTemp;
            }

            todayHighTempView.setText((tempUnit.equals("\u2103") ? Helpers.temperatureConvertor(todayHighTemperature) : todayHighTemperature) + tempUnit);
            todayLowTempView.setText((tempUnit.equals("\u2103") ? Helpers.temperatureConvertor(todayLowTemperature) : todayLowTemperature) + tempUnit);

            // 2.weekly list view
            // init week day weather listview view and adapter
            weekdayView = (RecyclerView) root.findViewById(R.id.daysListView);
            weekdayView.setHasFixedSize(true);
            weekdayLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            weekdayView.setLayoutManager(weekdayLayoutManager);

            weekdayAdapter = new WeekdayAdapter(this.forecast, this.tempUnit);
            weekdayView.setAdapter(weekdayAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onHourlyRetrieved(JSONArray hourly) {
        this.hourly = convertJSONArray(hourly);
        // 1. today temp, weather, wind
        // 2. hourly weather list view
        try {
            JSONObject firstWeather = hourly.getJSONObject(0);

            //1.1. today temp
            int currentTemperature = firstWeather.getInt("temperature");
            currentTempView.setText((tempUnit.equals("\u2103") ? Helpers.temperatureConvertor(currentTemperature) : currentTemperature) + tempUnit);

            //1.2. current wind
            String windSpeed = firstWeather.getString("windSpeed");
            String windDirection = firstWeather.getString("windDirection");
            currentWind.setText(Helpers.speedConvertor(windSpeed, this.tempUnit));
            ImageView windArrow = root.findViewById(R.id.windArrow);
            windArrow.setRotation(setWindArrowRotation(windDirection));

            //1.3. update weather icon
            currentWeather = firstWeather.getString("shortForecast");
            currentWeatherImage.setImageResource(getWeatherImage(currentWeather));

            // 2. TODO: implement list view of hourly weather in next 24 hours.
            hourlyView = (RecyclerView) root.findViewById(R.id.hourListView);
            hourlyView.setHasFixedSize(true);
            hourlyLayoutManage = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            hourlyView.setLayoutManager(hourlyLayoutManage);

            hourlyAdapter = new HourlyAdapter(this.hourly, this.tempUnit);
            hourlyView.setAdapter(hourlyAdapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // helpers
    private void setDateTime(){
        // get time from current time point
        Date now = new Date();
        String localDate = now.toLocaleString();
        Log.d("Q local date", localDate);
        String[] d = localDate.split("\\s(?=\\d\\d:\\d\\d:\\d\\d)");
        String date = d[0];
        String time = d[1].substring(0, 5);

        // Start time service, Receive time service is at onStart()
        if(!isTimeServiceRunning(TimeService.class)){
            Intent intent = new Intent(getContext(), TimeService.class);
            intent.putExtra("start", true);
            JobIntentService.enqueueWork(getContext(), TimeService.class, 0, intent);
        }

        // display time
        todayDate.setText(date);
        currentTime.setText(time);
    }

    private JSONObject[] convertJSONArray(JSONArray jsonArray){
        int length = jsonArray.length();
        JSONObject[] result = new JSONObject[length];
        for(int i = 0 ; i< length; i++ ){
            try {
                result[i] = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private boolean isTimeServiceRunning(Class<?> service){
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo s : manager.getRunningServices(Integer.MAX_VALUE)){
            if(service.getName().equals(s.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    private BroadcastReceiver timeServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentTime.setText(intent.getStringExtra("currentTime"));
        }
    };
}

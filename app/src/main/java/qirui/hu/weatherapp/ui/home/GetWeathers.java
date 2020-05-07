package qirui.hu.weatherapp.ui.home;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetWeathers {

    public interface OnWeatherRetrieved{
        void onForecastRetrieved(JSONArray forecast);
        void onHourlyRetrieved(JSONArray hourly);
    }


    final String metaDataPoints = "https://api.weather.gov/points/";

    private Context context;
    private RequestQueue requestQueue;
    private OnWeatherRetrieved weatherRetrievedListener;
    private JSONArray forecastArr;
    private JSONArray hourlyArr;

    GetWeathers(Context context, OnWeatherRetrieved listener){
//        Log.d("Q create GetWeathers", "");
        this.context = context;
        this.weatherRetrievedListener = listener;
        requestQueue = Volley.newRequestQueue(context);
    }


    public void getWeathers(Location location) {
        if(location == null){
            return;
        }

        String latitude = String.format("%.4f", location.getLatitude());
        String longitude = String.format("%.4f", location.getLongitude());
        String metaDataPointsUrl = metaDataPoints + latitude + "," + longitude;

        StringRequest metaRequest = new StringRequest(Request.Method.GET, metaDataPointsUrl, this::onMetaResponse, this::onError);
        requestQueue.add(metaRequest);
    }


    public void onMetaResponse(String metaResponse){
//        Log.d("Q meta response", metaResponse);
        try {
            JSONObject metaData = new JSONObject(metaResponse);

            String forecastUrl = metaData.getJSONObject("properties").getString("forecast");
            String hourlyUrl = metaData.getJSONObject("properties").getString("forecastHourly");

//            Log.d("Q forecast", forecastUrl);
//            Log.d("Q hourly", hourlyUrl);

            StringRequest forecastRequest = new StringRequest(Request.Method.GET, forecastUrl, this::onForecastResponse , this::onError);
            requestQueue.add(forecastRequest);

            StringRequest hourlyRequest = new StringRequest(Request.Method.GET, hourlyUrl, this::onHourlyResponse , this::onError);
            requestQueue.add(hourlyRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void onForecastResponse(String forecastResponse){
//        Log.d("Q forecast resp", forecastResponse);
        try {
            forecastArr = new JSONObject(forecastResponse).getJSONObject("properties").getJSONArray("periods");
//            Log.d("Q frecast arr", forecastArr.getJSONObject(1).toString());
            this.weatherRetrievedListener.onForecastRetrieved(this.forecastArr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onHourlyResponse(String hourlyResponse){
//        Log.d("Q hourly resp", hourlyResponse);
        try {
            hourlyArr = new JSONObject(hourlyResponse).getJSONObject("properties").getJSONArray("periods");
//            Log.d("Q hourly arr 0", hourlyArr.getJSONObject(0).toString());
//            Log.d("Q hourly arr 1", hourlyArr.getJSONObject(1).toString());
            this.weatherRetrievedListener.onHourlyRetrieved(this.hourlyArr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onError(VolleyError error){
        error.printStackTrace();
    }
}

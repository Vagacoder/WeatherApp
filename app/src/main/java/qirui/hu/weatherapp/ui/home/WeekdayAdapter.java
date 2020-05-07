package qirui.hu.weatherapp.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import qirui.hu.weatherapp.Helpers;
import qirui.hu.weatherapp.R;


public class WeekdayAdapter extends RecyclerView.Adapter<WeekdayAdapter.WeekdayViewHolder> {


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class WeekdayViewHolder extends RecyclerView.ViewHolder{

        // View in view holder( weather_day.xml )
        TextView weatherDayWeekday;
        ImageView weatherDayIcon;
        TextView weatherDayTemperatures;

        public WeekdayViewHolder(@NonNull View v) {
            super(v);
            weatherDayWeekday = v.findViewById(R.id.weather_day_weekday);
            weatherDayIcon = v.findViewById(R.id.weather_day_icon);
            weatherDayTemperatures = v.findViewById(R.id.weather_day_temperatures);
        }
    }

    // context need it?
    private Context context;

    // data from home fragment
    private JSONObject[] forecast;
    private String tempUnit;

    public WeekdayAdapter(JSONObject[] forecast, String tempUnit) {
        this.forecast = forecast;
        this.tempUnit = tempUnit;
    }

    @NonNull
    @Override
    public WeekdayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_day, parent, false);
        WeekdayViewHolder viewHolder = new WeekdayViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull WeekdayViewHolder holder, int position) {
        // single weekday weather ui setup
        JSONObject weather = forecast[position];
        if(weather != null){
            try {
                holder.weatherDayWeekday.setText(getWeekDayName(weather.getString("name"), weather.getBoolean("isDaytime")));
                holder.weatherDayIcon.setImageResource(Helpers.getWeatherImage(
                        weather.getString("shortForecast")
                ));

                int currentTemp = weather.getInt("temperature");
                holder.weatherDayTemperatures.setText((tempUnit.equals("\u2103")
                        ? Helpers.temperatureConvertor(currentTemp)
                        : currentTemp) + tempUnit);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
//            Log.d("Q weather is ", "null");
            holder.weatherDayWeekday.setText("--- -");
            holder.weatherDayIcon.setImageResource(R.drawable.baseline_error_outline_black_18dp);
            holder.weatherDayTemperatures.setText("---");
        }
    }

    @Override
    public int getItemCount() {
        if(forecast!=null) {
            return forecast.length;
        }else{
            return 14;
        }
    }

    private String getWeekDayName(String name, boolean isDaytime){
        if(name.equals("Today") || name.contains("Afternoon")){
            return "Today";
        } else if(name.contains("night")){
            return "Tonight";
        } else {
            return name.substring(0, 3) + (isDaytime ? " D" : " N");
        }
    }

}

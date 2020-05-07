package qirui.hu.weatherapp.ui.home;

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

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.HourlyViewHolder> {


    public static class HourlyViewHolder extends RecyclerView.ViewHolder{

        // View in view holder( weather_hour.xml )
        TextView weatherHourlyTemp;
        ImageView weatherHourlyIcon;
        ImageView weatherHourlyWindDirection;
        TextView weatherHourlyWindSpeed;
        TextView weatherHourlyTime;

        public HourlyViewHolder(@NonNull View v) {
            super(v);
            weatherHourlyTemp = v.findViewById(R.id.weather_hourly_temp);
            weatherHourlyIcon = v.findViewById(R.id.weather_hourly_icon);
            weatherHourlyWindDirection = v.findViewById(R.id.weather_hourly__wind_direction);
            weatherHourlyWindSpeed = v.findViewById(R.id.weather_hourly_wind_speed);
            weatherHourlyTime = v.findViewById(R.id.weather_hourly_time);
        }
    }


    // data from home fragment
    private JSONObject[] hourly;
    private String tempUnit;
    private View root;

    public HourlyAdapter(JSONObject[] hourly, String tempUnit){
        this.hourly = hourly;
        this.tempUnit = tempUnit;
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        root = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_hour, parent, false);
        HourlyViewHolder viewHolder = new HourlyViewHolder(root);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {

        JSONObject weather = hourly[position];
        if(weather != null){
            try {
                int tempF = weather.getInt("temperature");
                holder.weatherHourlyTemp.setText("" + (tempUnit.equals("\u2109")? tempF:Helpers.temperatureConvertor(tempF)));
                holder.weatherHourlyIcon.setImageResource(Helpers.getWeatherImage(
                        weather.getString("shortForecast")
                ));

                ImageView windArrow = root.findViewById(R.id.weather_hourly__wind_direction);

                Log.d("Q wind d: ", weather.getString("windDirection"));
                windArrow.setRotation(Helpers.setWindArrowRotation(weather.getString("windDirection")));
                holder.weatherHourlyWindSpeed.setText(Helpers.speedConvertor(
                        weather.getString("windSpeed"), tempUnit
                ).substring(0,2));
                holder.weatherHourlyTime.setText(weather.getString("startTime").substring(11, 16));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            holder.weatherHourlyTemp.setText("--");
            holder.weatherHourlyIcon.setImageResource(R.drawable.baseline_error_outline_black_18dp);
            holder.weatherHourlyWindDirection.setImageResource(R.drawable.baseline_error_outline_black_18dp);
            holder.weatherHourlyWindSpeed.setText("--");
            holder.weatherHourlyTime.setText("00:00");
        }

    }

    @Override
    public int getItemCount() {
//        return this.hourly.length;
        return 24;
    }

}

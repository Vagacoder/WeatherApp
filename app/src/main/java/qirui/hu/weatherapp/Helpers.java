package qirui.hu.weatherapp;

import static qirui.hu.weatherapp.ui.home.HomeFragment.MILE_TO_KM;

public class Helpers {

    public static int temperatureConvertor(int F){
        return (F-32)*5/9;
    }

    public static String speedConvertor(String mph, String tempUnit){
        int speed;
        String[] temp = mph.split(" ");

        if(mph.indexOf("to") >=0){
            if(tempUnit.equals("\u2109")) {
                speed = (int) Math.round((Integer.parseInt(temp[0]) + Integer.parseInt((temp[2]))) * 1.0 / 2);
            }else{
                speed = (int) Math.round(((Integer.parseInt(temp[0]) + Integer.parseInt((temp[2]))) * 1.0 / 2) * MILE_TO_KM);
            }
        }else{
            if(tempUnit.equals("\u2109")) {
                speed = Integer.parseInt(temp[0]);
            }else {
                speed = (int) Math.round(Integer.parseInt(temp[0]) * MILE_TO_KM);
            }
        }

        return tempUnit.equals("\u2109") ? (speed + " mph"): (speed + " km/h");
    }

    public static float setWindArrowRotation(String direction){
        float result = 0f;
        if(direction.equals("N")){
            result = 180f;
        }else if(direction.equals("S")) {
            result = 0f;
        }else if(direction.equals("W")) {
            result = 90f;
        }else if(direction.equals("E")) {
            result = -90f;
        }else if(direction.equals("NE")) {
            result = -135f;
        }else if(direction.equals("SE")) {
            result = -45f;
        }else if(direction.equals("SW")) {
            result = 45f;
        }else if(direction.equals("NW")) {
            result = 135f;
        }else if(direction.equals("NNE")) {
            result = -157.5f;
        }else if(direction.equals("ENE")) {
            result = -112.5f;
        }else if(direction.equals("ESE")) {
            result = -67.5f;
        }else if(direction.equals("SSE")) {
            result = -22.5f;
        }else if(direction.equals("SSW")) {
            result = 2.5f;
        }else if(direction.equals("WSW")) {
            result = 67.5f;
        }else if(direction.equals("WNW")) {
            result = 112.5f;
        }else if(direction.equals("NNW")) {
            result = 157.5f;
        }

        return result;
    }

    public static int getWeatherImage(String weather){
//        Log.d("Q weather:", weather);
        if (weather.endsWith("Partly Sunny") || weather.endsWith("Partly Clear")){
            return R.drawable.sunny_s_cloudy;
        } else if (weather.startsWith("Sunny") || weather.endsWith("Sunny") || weather.endsWith("Clear")){
            return R.drawable.sunny;
        } else if (weather.endsWith("Partly Cloudy")){
            return R.drawable.partly_cloudy;
        } else if (weather.endsWith("Cloudy") || weather.startsWith("Mostly Cloudy")){
            return R.drawable.cloudy;
        } else if (weather.contains("Thunderstorms")){
            return R.drawable.thunderstorms;
        } else if (weather.endsWith("Rain Showers") || weather.startsWith("Rain Showers")||
                weather.startsWith("Light Rain") || weather.endsWith("Light Rain")){
            return R.drawable.rain_light;
        } else if (weather.contains("Snow")){
            return R.drawable.snow;
        }else if (weather.contains("Fog")){
            return R.drawable.fog;
        }

        return R.drawable.baseline_error_outline_black_18dp;
    }

}

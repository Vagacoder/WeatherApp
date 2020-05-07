package qirui.hu.weatherapp.ui.home;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GetAddress extends AsyncTask<Location, Void, String> {

    interface OnTaskCompleted {
        void onTaskCompleted(String result);
    }

    private Context context;
    private OnTaskCompleted taskCompletedListener;

    GetAddress(Context context, OnTaskCompleted listener){
        this.context = context;
        this.taskCompletedListener = listener;
    }


    @Override
    protected String doInBackground(Location... locations) {
        if(locations == null || locations.length == 0){
            return "Locating ...";
        }
        String addressString = "";

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            Location location = locations[0];
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses == null || addresses.size() == 0) {
                addressString = "Not found";
            } else {
                Address address = addresses.get(0);
                Log.d("Q address", address.toString());
                String countryName = address.getCountryName();
                String adminArea= address.getAdminArea();
                String subAdminArea = address.getSubAdminArea();
                String locality = address.getLocality();

                if(locality != null) {
                    addressString = locality + ", " + adminArea + ", " + countryName;
                }else if (subAdminArea != null){
                    addressString = subAdminArea + ", " + adminArea + ", " + countryName;
                } else {
                    addressString = adminArea + ", " + countryName;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            addressString = "Locating ...";
        }
        Log.d("Q get address: ", addressString);
        return addressString;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        taskCompletedListener.onTaskCompleted(s);
    }
}

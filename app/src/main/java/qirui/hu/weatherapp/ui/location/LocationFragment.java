package qirui.hu.weatherapp.ui.location;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import qirui.hu.weatherapp.R;

public class LocationFragment extends Fragment {


    private Switch currentLocationSwitch;
    private boolean isCurrentLocationOn;
    private SharedPreferences.Editor editor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_location, container, false);
        TextView textView = root.findViewById(R.id.text_location);
        textView.setText("This is location");


        currentLocationSwitch = root.findViewById(R.id.switch1);

        editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // read preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());


        // set up current location switch
        isCurrentLocationOn = prefs.getBoolean("isCurrentLocationOn", false);
        currentLocationSwitch.setChecked(isCurrentLocationOn);
        currentLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isCurrentLocationOn = isChecked;
                editor.putBoolean("isCurrentLocationOn", isCurrentLocationOn);
                editor.commit();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}

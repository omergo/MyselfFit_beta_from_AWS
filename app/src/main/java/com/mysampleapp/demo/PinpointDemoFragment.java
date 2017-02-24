package com.mysampleapp.demo;

import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.pinpoint.targeting.endpointProfile.EndpointProfile;
import com.mysampleapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PinpointDemoFragment extends DemoFragmentBase {
    private static final String PROFILE_KEY_SEASONS = "seasons";
    private static final String SEASON_WINTER = "winter";
    private static final String SEASON_SPRING = "spring";
    private static final String SEASON_SUMMER = "summer";
    private static final String SEASON_FALL = "fall";

    /** This fragment's view. */
    private View mFragmentView;
    private Switch winterSwitch, springSwitch, summerSwitch, fallSwitch;
    private ArrayMap<Switch, String> switchToSeasonMap = new ArrayMap<>();

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.fragment_demo_pinpoint, container, false);
        final TextView profileView = (TextView) mFragmentView.findViewById(R.id.pinpoint_profile_textview);
        final EndpointProfile endpointProfile = AWSMobileClient.defaultMobileClient().getPinpointManager()
            .getTargetingClient().currentEndpoint();

        // Display some parts of the device profile for demonstration purposes.
        profileView.setText(getContext().getString(R.string.user_engagement_profile_format_text,
            endpointProfile.getEndpointId(),
            endpointProfile.getDemographic().getLocale().toString(),
            endpointProfile.getDemographic().getPlatform(),
            endpointProfile.getDemographic().getPlatformVersion()));

        winterSwitch = ((Switch) mFragmentView.findViewById(R.id.pinpoint_switch_winter));
        switchToSeasonMap.put(winterSwitch, SEASON_WINTER);
        springSwitch = ((Switch) mFragmentView.findViewById(R.id.pinpoint_switch_spring));
        switchToSeasonMap.put(springSwitch, SEASON_SPRING);
        summerSwitch = ((Switch) mFragmentView.findViewById(R.id.pinpoint_switch_summer));
        switchToSeasonMap.put(summerSwitch, SEASON_SUMMER);
        fallSwitch = (Switch) mFragmentView.findViewById(R.id.pinpoint_switch_fall);
        switchToSeasonMap.put(fallSwitch, SEASON_FALL);

        loadSwitches(endpointProfile);

        final CompoundButton.OnCheckedChangeListener checkedListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
               saveSwitches();
            }
        };

        for (final Switch theSwitch : switchToSeasonMap.keySet()) {
            theSwitch.setOnCheckedChangeListener(checkedListener);
        }
        return mFragmentView;
    }

    private void loadSwitches(final EndpointProfile endpointProfile) {
        final List<String> seasons = endpointProfile.getAttribute(PROFILE_KEY_SEASONS);
        if (seasons == null) {
            for (final Switch theSwitch : switchToSeasonMap.keySet()) {
                theSwitch.setChecked(false);
            }
            return;
        }
        for (final Map.Entry<Switch, String> entry : switchToSeasonMap.entrySet()) {
            entry.getKey().setChecked(seasons.contains(entry.getValue()));
        }
    }

    private void saveSwitches() {
        final List<String> seasonsList = new ArrayList<>();
        for (final Map.Entry<Switch, String> entry : switchToSeasonMap.entrySet()) {
            if (entry.getKey().isChecked()) {
                seasonsList.add(entry.getValue());
            }
        }
        AWSMobileClient.defaultMobileClient().getPinpointManager()
            .getTargetingClient().addAttribute(PROFILE_KEY_SEASONS, seasonsList);
        AWSMobileClient.defaultMobileClient().getPinpointManager()
            .getTargetingClient().updateEndpointProfile();
    }

}

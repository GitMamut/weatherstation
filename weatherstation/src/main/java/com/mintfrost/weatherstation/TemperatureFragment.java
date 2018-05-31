package com.mintfrost.weatherstation;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class TemperatureFragment extends Fragment implements DateFetchListener {
    public static final String DEGREES_CELSIUS = "°C";

    private static final String EMPTY_VALUE = "--,-" + DEGREES_CELSIUS;

    private static final String TEXT_DATE = "TEXT_DATE";
    private static final String TEXT_TEMPERATURE = "TEXT_TEMPERATURE";
    private static final String TEXT_HUMIDITY = "TEXT_HUMIDITY";

    private ProgressDialog progressDialog;
    private TextView textViewTemperature;
    private TextView textViewHumidity;
    private TextView textViewDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ((TextView) rootView.findViewById(R.id.text_location)).setText(getArguments().getString(MainActivity.EXTRA_LOCATION, ""));

        textViewTemperature = rootView.findViewById(R.id.text_temperature_value);
        if (savedInstanceState == null) {
            textViewTemperature.setText(EMPTY_VALUE);
        }

        textViewHumidity = rootView.findViewById(R.id.text_humidity_value);
        textViewDate = rootView.findViewById(R.id.text_date);

        return rootView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            textViewDate.setText(savedInstanceState.getString(TEXT_DATE));
            textViewTemperature.setText(savedInstanceState.getString(TEXT_TEMPERATURE));
            textViewHumidity.setText(savedInstanceState.getString(TEXT_HUMIDITY));
        }
    }

    @Override
    public void notifyStart() {
        showProgressDialog();
    }

    @Override
    public void notifyComplete(List<ConditionSnapshot> result) {
        dismissProgressDialog();
        if (result.size() > 0) {
            ConditionSnapshot conditionSnapshot = result.get(0);
            setDateValue(conditionSnapshot.getDate());
            setTemperatureValue(conditionSnapshot.getTempValue());
            String humValue = conditionSnapshot.getHumValue();
            if (humValue != null) {
                setHumidityValue(humValue);
            }
        }
    }

    @Override
    public void notifyError(String errorReason) {
        dismissProgressDialog();
        Toast toast = Toast.makeText(this.getContext(), getString(R.string.error_fetching_data) + " " + errorReason, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showProgressDialog() {
        if (this.getContext() != null) {
            progressDialog = new ProgressDialog(this.getContext());
            progressDialog.setMessage(getString(R.string.measuring_conditions));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @SuppressLint("SetTextI18n")
    public void setTemperatureValue(String s) {
        textViewTemperature.setText(s + DEGREES_CELSIUS);
    }

    @SuppressLint("SetTextI18n")
    public void setHumidityValue(String s) {
        textViewHumidity.setText(s + "%");
    }

    @SuppressLint("SetTextI18n")
    public void setDateValue(String date) {
        final int lastDash = date.lastIndexOf("-");
        final String dateDate = date.substring(0, lastDash);
        final String dateHour = date.substring(lastDash + 1, date.length());
        textViewDate.setText(dateDate + "\n" + dateHour);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (textViewDate.getText() != null) {
            outState.putString(TEXT_DATE, textViewDate.getText().toString());
        }
        if (textViewTemperature.getText() != null) {
            outState.putString(TEXT_TEMPERATURE, textViewTemperature.getText().toString());
        }
        if (textViewHumidity.getText() != null) {
            outState.putString(TEXT_HUMIDITY, textViewHumidity.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }
}

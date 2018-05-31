package com.mintfrost.weatherstation;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FetchTemperatureTask extends AsyncTask<String, Void, List<ConditionSnapshot>> {

    private DateFetchListener listener;

    private String errorReason;

    FetchTemperatureTask(DateFetchListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.notifyStart();
    }

    @Override
    protected List<ConditionSnapshot> doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String conditionsJsonStr = null;

        final String endpointUrl = params[0];
        final String service = params[1];

        List<ConditionSnapshot> conditionsList = new ArrayList<>();

        try {
            URL url = new URL(endpointUrl + "/" + service);
            Log.v("RQ", url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(2000);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            conditionsJsonStr = buffer.toString();
            Log.v("RS", conditionsJsonStr);

            JSONArray responseArray = new JSONArray(conditionsJsonStr);
            for (int i = 0; i < responseArray.length(); ++i) {
                JSONObject jsonObject = responseArray.getJSONObject(i);

                String temperature = getStringOrNull(jsonObject, "value");
                if (temperature == null) {
                    temperature = getStringOrNull(jsonObject, "tempValue");
                }

                conditionsList.add(new ConditionSnapshot(
                        getStringOrNull(jsonObject, "date"),
                        temperature,
                        getStringOrNull(jsonObject, "humValue"),
                        getStringOrNull(jsonObject, "pressureValue")));
            }
        } catch (IOException | JSONException e) {
            Log.e(this.getClass().getCanonicalName(), "Error fetching data", e);
            errorReason = e.getLocalizedMessage();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return conditionsList;
    }

    private String getStringOrNull(JSONObject jsonObject, String stringName) {
        String result = null;
        try {
            result = jsonObject.getString(stringName);
        } catch (JSONException ignored) {
            // ignore this
        }

        return result;
    }

    @Override
    protected void onPostExecute(List<ConditionSnapshot> conditionsList) {
        super.onPostExecute(conditionsList);
        if (errorReason != null) {
            listener.notifyError(errorReason);
        }
        if (conditionsList != null) {
            listener.notifyComplete(conditionsList);
        }
    }
}

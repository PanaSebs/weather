package com.example.weather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText cityEditText;
    private Button getWeatherButton;
    private ListView weatherListView;
    private ArrayAdapter<String> weatherAdapter;
    private List<String> weatherData;

    private static final String API_KEY = "4d4952faa58c047e06be5f8d6a7a7fb9\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityEditText = findViewById(R.id.cityEditText);
        getWeatherButton = findViewById(R.id.getWeatherButton);
        weatherListView = findViewById(R.id.weatherListView);

        weatherData = new ArrayList<>();
        weatherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, weatherData);
        weatherListView.setAdapter(weatherAdapter);

        getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEditText.getText().toString().trim();
                String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + API_KEY;

                FetchWeatherTask task = new FetchWeatherTask();
                task.execute(url);
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Leer la respuesta de la API
                StringBuilder buffer = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray forecastArray = jsonObject.getJSONArray("list");

                    weatherData.clear();
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    for (int i = 0; i < 16; i++) {
                        JSONObject forecastObject = forecastArray.getJSONObject(i);
                        long timestamp = forecastObject.getLong("dt");
                        JSONObject mainObject = forecastObject.getJSONObject("main");
                        double temperature = mainObject.getDouble("temp");
                        int humidity = mainObject.getInt("humidity");
                        JSONObject windObject = forecastObject.getJSONObject("wind");
                        double windSpeed = windObject.getDouble("speed");
                        int rainProbability = 0;
                        if (forecastObject.has("rain")) {
                            JSONObject rainObject = forecastObject.getJSONObject("rain");
                            if (rainObject.has("3h")) {
                                rainProbability = rainObject.getInt("3h");
                            }
                        }

                        String weather = "Día " + (i + 1) + ":\n";
                        weather += "Fecha: " + timestampToDate(timestamp) + "\n";
                        weather += "Temperatura: " + decimalFormat.format(kelvinToCelsius(temperature)) + "°C\n";
                        weather += "Humedad: " + humidity + "%\n";
                        weather += "Velocidad del viento: " + decimalFormat.format(windSpeed) + " m/s\n";
                        weather += "Probabilidad de lluvia: " + rainProbability + "%";

                        weatherData.add(weather);
                    }

                    weatherAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        private String timestampToDate(long timestamp) {
            java.util.Date date = new java.util.Date(timestamp * 1000);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(date);
        }

        private double kelvinToCelsius(double temperature) {
            return temperature - 273.15;
        }
    }
}




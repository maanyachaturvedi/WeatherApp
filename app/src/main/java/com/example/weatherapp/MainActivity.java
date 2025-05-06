package com.example.weatherapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    EditText edit;
    Button button;
    ImageView img, img2, img3, img4, img5, img6;
    TextView des, time, temp, high, low, high2, low2, time1, high3, low3, time2, high4, low4, time3, high5, low5, time4, time5;

    interface WeatherCallback {
        void onResult(String result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit = findViewById(R.id.id_main_editText);
        button = findViewById(R.id.id_main_button);

        img = findViewById(R.id.imageView);
        img2 = findViewById(R.id.imageView6);
        img3 = findViewById(R.id.imageView2);
        img4 = findViewById(R.id.imageView3);
        img5 = findViewById(R.id.imageView4);
        img6 = findViewById(R.id.imageView5);

        des = findViewById(R.id.description);
        time = findViewById(R.id.timern);
        time1 = findViewById(R.id.time);
        time2 = findViewById(R.id.time2);
        time3 = findViewById(R.id.time3);
        time4 = findViewById(R.id.time4);
        time5 = findViewById(R.id.time5);

        temp = findViewById(R.id.temp);
        high = findViewById(R.id.high);
        low = findViewById(R.id.low);
        high2 = findViewById(R.id.high2);
        low2 = findViewById(R.id.low2);
        high3 = findViewById(R.id.high3);
        low3 = findViewById(R.id.low3);
        high4 = findViewById(R.id.high4);
        low4 = findViewById(R.id.low4);
        high5 = findViewById(R.id.high5);
        low5 = findViewById(R.id.low5);

        button.setOnClickListener(view -> {
            button.setEnabled(false);
            String zip = edit.getText().toString().trim();

            if (zip.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a ZIP code.", Toast.LENGTH_SHORT).show();
                button.setEnabled(true);
                return;
            }

            try {
                URL url = new URL("https://api.openweathermap.org/data/2.5/forecast?zip=" + zip + ",us&appid=d03059debe23f6d02ad8faf9c698ec2c");
                System.out.println("Fetching from: " + url);

                new DownloadFilesTask(result -> {
                    button.setEnabled(true);
                    if (result == null) {
                        Toast.makeText(MainActivity.this, "Error fetching weather data.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONObject obj = new JSONObject(result);
                        JSONArray arr = obj.getJSONArray("list");

                        // Show current temperature in first forecast entry
                        JSONObject firstMain = arr.getJSONObject(0).getJSONObject("main");
                        double currentTemp = fahrenheit(firstMain.getDouble("temp"));
                        temp.setText(String.valueOf(currentTemp));

                        updateForecastUI(arr.getJSONObject(0), high, low, time, img, des);
                        updateForecastUI(arr.getJSONObject(1), high2, low2, time1, img2, null);
                        updateForecastUI(arr.getJSONObject(2), high3, low3, time2, img3, null);
                        updateForecastUI(arr.getJSONObject(3), high4, low4, time3, img4, null);
                        updateForecastUI(arr.getJSONObject(4), high5, low5, time4, img5, null);
                        updateForecastUI(arr.getJSONObject(5), high5, low5, time5, img6, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Failed to parse weather data", Toast.LENGTH_SHORT).show();
                    }
                }).execute(url);
            } catch (MalformedURLException e) {
                button.setEnabled(true);
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Malformed URL.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateForecastUI(JSONObject hour, TextView high, TextView low, TextView timeText, ImageView imgView, TextView desText) throws JSONException {
        int timestamp = hour.getInt("dt");
        Date date = new Date(timestamp * 1000L);
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        String timeFormatted = formatter.format(date);
        timeText.setText(timeFormatted);

        JSONObject main = hour.getJSONObject("main");
        double tempMax = fahrenheit(main.getDouble("temp_max"));
        double tempMin = fahrenheit(main.getDouble("temp_min"));

        high.setText(String.valueOf(tempMax));
        low.setText(String.valueOf(tempMin));

        JSONArray weatherArray = hour.getJSONArray("weather");
        String iconCode = "w" + weatherArray.getJSONObject(0).getString("icon");

        int imageResId = getResources().getIdentifier(iconCode, "drawable", getPackageName());
        if (imageResId != 0) {
            imgView.setImageResource(imageResId);
            imgView.setVisibility(View.VISIBLE);
        }

        if (desText != null) {
            switch (iconCode) {
                case "w01d":
                    desText.setText("Just like the Strawhats' ship, it's a Thousand Sunny!");
                    break;
                case "w02d":
                    desText.setText("Looks like the Sunny has landed on the Cloud Island!");
                    break;
                case "w13n":
                    desText.setText("Looks like we're on a winter island!");
                    break;
                default:
                    desText.setText("Weather condition: " + iconCode);
                    break;
            }
        }
    }

    public class DownloadFilesTask extends AsyncTask<URL, Void, String> {
        private WeatherCallback callback;

        public DownloadFilesTask(WeatherCallback callback) {
            this.callback = callback;
        }

        @Override
        protected String doInBackground(URL... urls) {
            try {
                URLConnection connect = urls[0].openConnection();
                InputStream input = connect.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
                return read(buffer);
            }catch (IOException e) {
                e.printStackTrace();
                android.util.Log.e("WeatherApp", "Network error: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (callback != null) {
                android.util.Log.d("WeatherApp", "API Response: " + result);
                callback.onResult(result);
            }
        }
    }

    public String read(BufferedReader buffer) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int q;
        while ((q = buffer.read()) != -1) stringBuilder.append((char) q);
        return stringBuilder.toString();
    }

    public double fahrenheit(double kelvins) {
        return Math.round(((kelvins - 273.15) * 9 / 5 + 32) * 100.0) / 100.0;
    }
}
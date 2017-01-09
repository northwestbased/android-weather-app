package com.example.weatherchecker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class City {
    private static String weatherUrl = "https://api.apixu.com/v1/current.json?key=0fc0ae0803da49dcb0635653162812&q=%s";

    public String name;
    public String text;
    public String temperature_c;
    public String temperature_f;
    public String cityImageUrl;
    public Bitmap image;
    public MainActivity UIContext;

    City (MainActivity act, String cityName) {
        name = cityName;
        UIContext = act;
    }

    void downloadWeather() {
        String newName = name.replace(" ", "+");
        new DownloadTask().execute(String.format(weatherUrl, newName));
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            InputStream stream = null;
            String str ="";

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000 );
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Start the query
                conn.connect();
                stream = conn.getInputStream();

                Reader reader = null;
                reader = new InputStreamReader(stream, "UTF-8");
                char[] buffer = new char[1000];
                reader.read(buffer);
                str = new String(buffer);

                if (stream != null) {
                    stream.close();
                }
            }
            catch (IOException e) {
                return "Error retrieveing data";
            }

            //str = "{\"location\":{\"name\":\"Paris\",\"region\":\"Ile-de-France\",\"country\":\"France\",\"lat\":48.87,\"lon\":2.33,\"tz_id\":\"Europe/Paris\",\"localtime_epoch\":1482902099,\"localtime\":\"2016-12-28 5:14\"},\"current\":{\"last_updated_epoch\":1482901980,\"last_updated    \":\"2016-12-28 05:13\",\"temp_c\":0.0,\"temp_f\":32.0,\"is_day\":0,\"condition\":{\"text\":\"Clear\",\"icon\":\"//cdn.apixu.com/weather/64x64/night/113.png\",\"code\":1000},\"wind_mph\":0.0,\"wind_kph\":0.0,\"wind_degree\":86,\"wind_dir\":\"E\",\"pressure_mb\":1043.0,\"pressure_in\":31.3,\"precip_mm\":0.0,\"precip_in\":0.0,\"humidity\":93,\"cloud\":0,\"feelslike_c\":0.0,\"feelslike_f\":32.0}}";
            return str;
        }

        protected void onPostExecute(String result)
        {
            try {
                JSONObject weather = new JSONObject(result);

                String location = weather.getJSONObject("location")
                        .getString("name");
                String tex = weather.getJSONObject("current")
                        .getJSONObject("condition").getString("text");
                String temp_f = weather.getJSONObject("current")
                        .getString("temp_f");
                String temp_c = weather.getJSONObject("current")
                        .getString("temp_c");

                String iconUrl = weather.getJSONObject("current")
                        .getJSONObject("condition").getString("icon");

                text = tex;
                temperature_c = temp_c;
                temperature_f = temp_f;
                cityImageUrl = "https:" + iconUrl;
                new DownloadImageTask().execute();
                UIContext.updateUI();
            }
            catch (JSONException e) {
                return;
            }
        }
    }

    private class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {
        // http://stackoverflow.com/questions/18210700/best-method-to-download-image-from-url-in-android
        @Override
        protected Bitmap doInBackground(Void... none) {
            try {
                java.net.URL url = new java.net.URL(cityImageUrl);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                image = myBitmap;

                //updateList();
                return myBitmap;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        protected void onPostExecute(Bitmap result) {
            UIContext.updateUI();
        }
    }
}

















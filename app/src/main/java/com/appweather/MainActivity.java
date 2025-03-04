package com.appweather;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;
import android.Manifest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTv,temperatureTV,conditionTV,windTV,cloudTV,humidityTV;
    private TextInputEditText CityEdit;
    private ImageView backIV,iconIV,searchIv,countryFlag;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        loadingPB = findViewById(R.id.Loading_id);
        cityNameTv = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        CityEdit = findViewById(R.id.idETCity);
        backIV = findViewById(R.id.IdIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIv = findViewById(R.id.idTVSearch);
        windTV =findViewById(R.id.idTVWindTextMetric);
        cloudTV=findViewById(R.id.idTVCloudTextMetric);
        humidityTV=findViewById(R.id.idTVCHumidTextMetric);
        countryFlag=findViewById(R.id.idIVFlag);

        homeRL = findViewById(R.id.idRLHome);




        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityName);

        setTimeBasedBackground();

        searchIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = CityEdit.getText().toString().trim(); // Trim the input to remove extra spaces
                if (city.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                } else {
                    cityNameTv.setText(city);
                    getWeatherInfo(city);
                }
            }
        });



    }

    private void setTimeBasedBackground() {
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        String currentHourString = sdf.format(currentTime);
        int currentHour = Integer.parseInt(currentHourString);

        int dayImage = R.drawable.day;
        int nightImage = R.drawable.night;
        int backgroundResource;

        if (currentHour >= 18 || currentHour < 6) {
            // Night time (6 PM to 6 AM)
            backgroundResource = nightImage;
        } else {
            // Day time (6 AM to 6 PM)
            backgroundResource = dayImage;
        }

        backIV.setImageResource(backgroundResource);
        backIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }


    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Please provide the permission",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
            for (Address adr: addresses){
                if(adr!=null){
                    String city = adr.getLocality();
                    if( city != null &&  !city.equals("")){
                        cityName = city;
                    }
                    else{
                        Log.d("TAG","City Not Found");
                        Toast.makeText(this,"User City Not Found..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=89852f15bebd043e42effdd09d6aef37&units=metric";
        cityNameTv.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);

                try {
                    JSONObject mainObject = response.getJSONObject("main");
                    String temperature = mainObject.getString("temp");
                    temperatureTV.setText(temperature + "°C");

                    JSONArray weatherArray = response.getJSONArray("weather");
                    if (weatherArray.length() > 0) {
                        JSONObject weatherObject = weatherArray.getJSONObject(0);
                        String condition = weatherObject.getString("main");
                        String description = weatherObject.getString("description");
                        conditionTV.setText(condition + " (" + description + ")");
                        JSONObject windObject = response.getJSONObject("wind");
                        double windSpeed = windObject.getDouble("speed");
                        String windInfo = windSpeed + " m/s";
                        windTV.setText(windInfo);
                        JSONObject cloudObject = response.getJSONObject("clouds");
                        int cloudPercentage = cloudObject.getInt("all");
                        // Update the cloud information in your UI
                        String cloudInfo = cloudPercentage + "%";
                        cloudTV.setText(cloudInfo);
                        double humidity = mainObject.getDouble("humidity");
                        // Update the humidity information in your UI
                        String humidityInfo =  humidity + "%";
                        humidityTV.setText(humidityInfo);
                        String cityName = response.getString("name");
                        cityNameTv.setText(cityName);


                        JSONObject sysObject = response.getJSONObject("sys");
                        String countryCode = sysObject.getString("country");
                        String countryUrl = "https://flagcdn.com/144x108/" + countryCode.toLowerCase() + ".png";
                        Picasso.get().load(countryUrl).into(countryFlag);

                        String iconCode = weatherObject.getString("icon");
                        String iconUrl = "https://openweathermap.org/img/w/" + iconCode + ".png";
                        Picasso.get().load(iconUrl).into(iconIV);
                        int dayImage = R.drawable.day;
                        int sunny = R.drawable.sunny;
                        int fewCloudsDay = R.drawable.fewclouds;
                        int fewCloudsNight = R.drawable.fewcloudsnight;
                        int scatteredCloudDay = R.drawable.scatteredcloudday;
                        int scatteredCloudNight = R.drawable.scatteredcloudnight;
                        int nightClear = R.drawable.nigth2;
                        int hazeDay = R.drawable.haze;
                        int hazeNight = R.drawable.hazenight;
                        int cloudsDay = R.drawable.clouds;
                        int cloudsNight = R.drawable.cloudsnight;
                        int dayRain = R.drawable.rainday;
                        int nightRain = R.drawable.rainnight;
                        int dayThunder = R.drawable.thunderday;
                        int nightThunder = R.drawable.thundernight;
                        int ClearRainDay = R.drawable.rainclearday;
                        int ClearRainNight = R.drawable.rainclearnight;
                        int nightImage = R.drawable.night;
                        int backgroundResource;
                        Calendar calendar = Calendar.getInstance();
                        Date currentTime = calendar.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
                        String currentHourString = sdf.format(currentTime);
                        int currentHour = Integer.parseInt(currentHourString);


                        //day and sunny
                        if (iconCode.equals("01d")) {
                            backgroundResource = sunny;
                        }
                        //night and clear
                        else if (iconCode.equals("01n")) {
                            backgroundResource = nightClear;
                        }
                        //day and few clouds
                        else if (iconCode.equals("02d")) {
                            backgroundResource = fewCloudsDay;
                        }
                        //night and few clouds
                        else if (iconCode.equals("02n")) {
                            backgroundResource = fewCloudsNight;
                        }
                        //day and scattered clouds
                        else if (iconCode.equals("03d")) {
                            backgroundResource = scatteredCloudDay;
                        }
                        //night and scattered clouds
                        else if (iconCode.equals("03n")) {
                            backgroundResource = scatteredCloudNight;
                        }
                        //cloud day
                        else if (iconCode.equals("04d")) {
                            backgroundResource = cloudsDay;
                        }//cloud night
                        else if (iconCode.equals("04n")) {
                            backgroundResource = cloudsNight;
                        }
                        //haze day
                        else if (iconCode.equals("50d")) {
                            backgroundResource = hazeDay;
                        }
                        //haze night
                        else if (iconCode.equals("50n")) {
                            backgroundResource = hazeNight;
                        }
                        // day rain
                        else if (iconCode.equals("13d")) {
                            backgroundResource = dayRain;
                        }
                        // night rain
                        else if (iconCode.equals("13n")) {
                            backgroundResource = nightRain;
                        }
                        //thunder day
                        else if (iconCode.equals("13d")) {
                            backgroundResource = dayThunder;
                        }
                        //thunder night
                        else if (iconCode.equals("13n")) {
                            backgroundResource = nightThunder;
                        }
                        //clear rain day
                        else if (iconCode.equals("10d")) {
                            backgroundResource = ClearRainDay;
                        }
                        //clear rain night
                        else if (iconCode.equals("10n")) {
                            backgroundResource = ClearRainNight;
                        }


                        else {
                            if (currentHour >= 18 || currentHour < 6) {
                                // Night time (6 PM to 6 AM)
                                backgroundResource = nightImage;
                            } else {
                                // Day time (6 AM to 6 PM)
                                backgroundResource = dayImage;
                            }
                        }

                        backIV.setImageResource(backgroundResource);
                        backIV.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Enter a valid city name..", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
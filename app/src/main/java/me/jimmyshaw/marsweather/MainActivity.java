package me.jimmyshaw.marsweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
{
    TextView mTextDegrees;
    TextView mTextWeather;
    TextView mTextError;

    // We can call getInstance outside of onCreate since the singleton will have already
    // been initialized. We don't need to wait for the onStart method to call it.
    MarsWeather helper = MarsWeather.getInstance();

    final static String RECENT_API_ENDPOINT = "http://marsweather.ingenology.com/v1/latest/";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextDegrees = (TextView) findViewById(R.id.degrees);
        mTextWeather = (TextView) findViewById(R.id.weather);
        mTextError = (TextView) findViewById(R.id.error);
    }

    private void loadWeatherData()
    {
        CustomJsonRequest request = new CustomJsonRequest(Request.Method.GET,
                RECENT_API_ENDPOINT,
                null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            String minTemp;
                            String maxTemp;
                            String atmo;
                            int avgTemp;

                            response = response.getJSONObject("report");

                            minTemp = response.getString("min_temp");
                            minTemp = minTemp.substring(0, minTemp.indexOf("."));

                            maxTemp = response.getString("max_temp");
                            maxTemp = maxTemp.substring(0, maxTemp.indexOf("."));

                            avgTemp = (Integer.parseInt(minTemp) + Integer.parseInt(maxTemp)) / 2;

                            atmo = response.getString("atmo_opacity");

                            mTextDegrees.setText(avgTemp + "°");
                            mTextWeather.setText(atmo);
                        }
                        catch (Exception e)
                        {
                            textError(e);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        textError(error);
                    }
                });

        request.setPriority(Request.Priority.HIGH);
        helper.add(request);
    }

    private void textError(Exception e)
    {
        mTextError.setVisibility(View.VISIBLE);
        e.printStackTrace();
    }
}

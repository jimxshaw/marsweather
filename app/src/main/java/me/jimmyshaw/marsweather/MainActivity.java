package me.jimmyshaw.marsweather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

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

    
}

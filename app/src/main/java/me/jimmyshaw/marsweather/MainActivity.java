package me.jimmyshaw.marsweather;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

public class MainActivity extends AppCompatActivity
{
    ImageView mImageView;

    TextView mTextDegrees;
    TextView mTextWeather;
    TextView mTextError;

    int mainColor = getResources().getColor(R.color.mainColor);

    // We can call getInstance outside of onCreate since the singleton will have already
    // been initialized. We don't need to wait for the onStart method to call it.
    MarsWeather helper = MarsWeather.getInstance();

    final static String FLICKR_API_KEY = "17bebf85fa756f5cb381fbcb4724981b";
    // We're telling Flickr to give us results formatted as JSON(format=json) but we don't specify
    // a JSON callback(nojsoncallback=1). We're searching an image(method=flickr.photos.search) and
    // the tags we're interested in are related to Mars(tags=mars,planet,rover).
    final static String IMAGES_API_ENDPOINT = "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&sort=random&method=flickr.photos.search&" +
            "tags=mars,planet,rover&tag_mode=all&api_key=";
    final static String RECENT_API_ENDPOINT = "http://marsweather.ingenology.com/v1/latest/";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.main_bg);
        mTextDegrees = (TextView) findViewById(R.id.degrees);
        mTextWeather = (TextView) findViewById(R.id.weather);
        mTextError = (TextView) findViewById(R.id.error);

        try
        {
            searchRandomImage();
        }
        catch (Exception e)
        {
            imageError(e);
        }

        loadWeatherData();
    }

    private void searchRandomImage() throws Exception
    {
        if (FLICKR_API_KEY.equals(""))
        {
            throw new Exception("Please provide a working Flickr API key.");
        }

        CustomJsonRequest request = new CustomJsonRequest(Request.Method.GET,
                IMAGES_API_ENDPOINT + FLICKR_API_KEY,
                null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            // Flickr sends back a JSONArray containing images. The Random object
                            // retrieves a random image and generates a random number between zero
                            // and the size of the array. It takes the item corresponding to that
                            // index from the array of results and constructs the URL for the image
                            // according to Flickr's guidelines.
                            JSONArray images = response.getJSONObject("photos").getJSONArray("photos");
                            int index = new Random().nextInt(images.length());

                            JSONObject imageItem = images.getJSONObject(index);

                            String imageUrl = "http://farm" + imageItem.getString("farm") +
                                    ".static.flickr.com/" + imageItem.getString("server") +
                                    "/" + imageItem.getString("id") + "_" +
                                    imageItem.getString("secret") + "_" + "c.jpg";

                            //TODO: Do something with *imageUrl*.
                        }
                        catch (Exception e)
                        {
                            imageError(e);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        imageError(error);
                    }
                }
        );

        request.setPriority(Request.Priority.LOW);
        helper.add(request);
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

    private void imageError(Exception e)
    {
        mImageView.setBackgroundColor(mainColor);
    }

    private void textError(Exception e)
    {
        mTextError.setVisibility(View.VISIBLE);
        e.printStackTrace();
    }
}

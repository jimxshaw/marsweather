package me.jimmyshaw.marsweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Random;

public class MarsWeather extends AppCompatActivity
{
    ImageView mImageView;

    TextView mTextDegrees;
    TextView mTextWeather;
    TextView mTextError;

    int mainColor = Color.parseColor("#FF5722");

    // We're bypassing Volley's caching system by retrieving a random image every time
    // our app is launched. We need a way to show the same image on a particular day.
    // The simplest way to achieve this is through Android's SharedPreferences.
    SharedPreferences mSharedPreferences;
    int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    final static String SHARED_PREFS_IMG_KEY = "img";
    final static String SHARED_PREFS_DAY_KEY = "day";

    // We can call getInstance outside of onCreate since the singleton will have already
    // been initialized. We don't need to wait for the onStart method to call it.
    MarsWeatherHelper helper = MarsWeatherHelper.getInstance();

    final static String FLICKR_API_KEY = "17bebf85fa756f5cb381fbcb4724981b";
    // We're telling Flickr to give us results formatted as JSON(format=json) but we don't specify
    // a JSON callback(nojsoncallback=1). We're searching an image(method=flickr.photos.search) and
    // the tags we're interested in are related to Mars(tags=mars,planet,rover).
    final static String IMAGES_API_ENDPOINT = "https://api.flickr.com/services/rest/?format=json&nojsoncallback=1&sort=random&method=flickr.photos.search&" +
            "tags=mars&tag_mode=all&api_key=";
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

        // Change the default font in our views to something else.
        mTextDegrees.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));
        mTextWeather.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-light.ttf"));

        mSharedPreferences = getPreferences(Context.MODE_PRIVATE);

        if (mSharedPreferences.getInt(SHARED_PREFS_DAY_KEY, 0) != today)
        {
            // Search and load a random Mars image.
            try
            {
                searchRandomImage();
            }
            catch (Exception e)
            {
                imageError(e);
            }
        }
        else
        {
            // If we already have an image of the day then we load it.
            loadImage(mSharedPreferences.getString(SHARED_PREFS_IMG_KEY, ""));
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
                            JSONArray images = response.getJSONObject("photos").getJSONArray("photo");
                            int index = new Random().nextInt(images.length());

                            JSONObject imageItem = images.getJSONObject(index);

                            String imageUrl = "http://farm" + imageItem.getString("farm") +
                                    ".static.flickr.com/" + imageItem.getString("server") + "/" +
                                    imageItem.getString("id") + "_" + imageItem.getString("secret") + "_" + "c.jpg";

                            // We store the current day every time we retrieve a new random image.
                            // We store the image URL alongside the day. When our app launches, we
                            // check whether we already have an entry in the SharedPreferences for
                            // the current day. If there's a match, we use the stored URL. Otherwise
                            // we retrieve a random image and store its URL in SharedPreferences.
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putInt(SHARED_PREFS_DAY_KEY, today);
                            editor.putString(SHARED_PREFS_IMG_KEY, imageUrl);
                            editor.apply();

                            loadImage(imageUrl);
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

    private void loadImage(String imageUrl)
    {
        // Retrieves an image specified by the URL and displays it in the UI.
        ImageRequest request = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>()
                {
                    @Override
                    public void onResponse(Bitmap response)
                    {
                        mImageView.setImageBitmap(response);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener()
                {
                    public void onErrorResponse(VolleyError error)
                    {
                        imageError(error);
                    }
                }
        );

        // We don't need to set the priority here. ImageRequest already comes in with
        // priority set to LOW and that's what we need.
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
        e.printStackTrace();
    }

    private void textError(Exception e)
    {
        mTextError.setVisibility(View.VISIBLE);
        e.printStackTrace();
    }
}

package me.jimmyshaw.marsweather;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

// Volley provides three standard request types: StringRequest, ImageRequest
// and JsonRequest. Our app uses the last two to retrieve a list of images and
// weather data.
// By default, Volley sets the priority of a request to NORMAL. Generally that's
// fine but our app has two requests that are different from each other. Therefore,
// we need to have a different priority in the queue. Retrieving weather data needs
// to have a higher priority than retrieving the URL of random images.

public class CustomJsonRequest extends JsonObjectRequest
{

    private Priority mPriority;

    public CustomJsonRequest(int method, String url, JSONObject jsonRequest,
                             Response.Listener<JSONObject> listener,
                             Response.ErrorListener errorListener)
    {
        super(method, url, jsonRequest, listener, errorListener);
    }

    @Override
    public Priority getPriority()
    {
        return mPriority == null ? Priority.NORMAL : mPriority;
    }

    public void setPriority(Priority priority)
    {
        mPriority = priority;
    }
}

package me.jimmyshaw.marsweather;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

// It's better to use a shared request queue if we're firing
// multiple requests. To avoid creating a request queue each time
// we schedule a request by invoking Volley.newRequestQueue, we
// create a class using the singleton pattern. This class is referenced
// using a static, globally visible variable that handles the object
// RequestQueue.

// By extending Application, we're telling the Android OS to generate
// this object at application startup even before the first activity
// is created.
public class MarsWeather extends Application
{
    private RequestQueue mRequestQueue;
    private static MarsWeather mInstance;

    // This is a generic token used to identity the request.
    public static final String TAG = MarsWeather.class.getSimpleName();

    @Override
    public void onCreate()
    {
        super.onCreate();
        mInstance = this;
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public static synchronized MarsWeather getInstance()
    {
        return mInstance;
    }

    public RequestQueue getRequestQueue()
    {
        return mRequestQueue;
    }

    public <T> void add(Request<T> request)
    {
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public void cancel()
    {
        mRequestQueue.cancelAll(TAG);
    }

}

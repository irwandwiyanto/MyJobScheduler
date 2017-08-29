package dicoding.tutorial.myjobscheduler;

import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.icu.text.DecimalFormat;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.loopj.android.http.*;

import cz.msebera.android.*;
import cz.msebera.android.httpclient.;

import org.json.JSONObject;


/**
 * Created by Widyartini on 8/29/2017.
 */

public class GetCurrentWeatherJobService extends JobService {
    public static final String TAG = "GetWeather";
    private final String APP_ID = "c377e3b0902aea0ddb339fa6d2d60ca8";
    private final String CITY = "Bogor";
    
    @Override
    public boolean onStartJob(JobParameters params) {
        getCurrentWeather(params);
        return true;
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void getCurrentWeather(final JobParameters jobParameters) {
        Log.d("GetWeather","Running");
        AsyncHttpClient client =  new AsyncHttpClient();
        String url = "http://api.openweathermap.org/data/2.5/weather?q="+CITY+"&appid="+APP_ID;
        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                jobFinished(jobParameters, false);
                String result = new String(responseBody);
                Log.d(TAG, result);
                try {
                    JSONObject responseObject = new JSONObject(result);
                    String currentWeather = responseObject.getJSONArray("weather").getJSONObject(0).getString("main");
                    String description =  responseObject.getJSONArray("weather").getJSONObject(0).getString("description");
                    double tempInKelvin = responseObject.getJSONObject("main").getDouble("temp");
                    double tempInCelcius = tempInKelvin - 273;
                    String temperature = new java.text.DecimalFormat("##.##").format(tempInCelcius);
                    String title = "Current weather";
                    String message = currentWeather +", "+description+" with "+temperature+" celcius";
                    int notifId = 100;
                    showNotification(getApplicationContext(), title, message, notifId);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                Log.d("GetWeather", "Failed");
                jobFinished(jobParameters, false);
            }
        });
    }

    private void showNotification(Context context, String title, String message, int notifId) {
        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_replay_black_24dp)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.white))
                .setVibrate(new long[]{1000,1000,1000,1000,1000})
                .setSound(alarmSound);
        notificationManagerCompat.notify(notifId, builder.build());
    }
}

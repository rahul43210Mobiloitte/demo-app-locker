package com.gueei.applocker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DetectorService extends Service {
	public static final String ACTION_DETECTOR_SERVICE = "com.gueei.detector.service";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private static final Class<?>[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[] {
        boolean.class};
    
    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    
    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            }
            return;
        }
        
        // Fall back on the old API.
        setForeground(true);
        mNM.notify(id, notification);
    }
    
    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            }
            return;
        }
        
        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        setForeground(false);
    }
	
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
    }
    
    @Override
    public void onDestroy() {
    	
    	mThread.interrupt();
    	
        // Make sure our notification is gone.
        stopForegroundCompat(R.string.service_running);
    }
    
 // This is the old onStart method that will be called on the pre-2.0
    // platform.  On 2.0 or later we override onStartCommand() so this
    // method will not be called.
    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return Service.START_STICKY;
    }
    
    private void handleCommand(Intent intent){
    	// In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.service_running);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.statusbar_icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, AppLockerActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, text,
                       text, contentIntent);
        
        startForegroundCompat(R.string.service_running, notification);
        
        startMonitorThread((ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE));
    }
    
    private void startMonitorThread(final ActivityManager am){
    	if (mThread!=null)
    		mThread.interrupt();
    	
    	mThread = new MonitorLogThread(new ActivityStartingHandler(this));
    	mThread.start();
    }
    
    private static Thread mThread;
    
    private static final Pattern ActivityNamePattern = Pattern.compile(	".*Starting activity.*cmp=((\\w+(\\.\\w+)*\\.\\w+)/(\\.?\\w+(\\.\\w+)*))");
    private static final String LogCatCommand = "logcat ActivityManager:I *:S";
    private static final String ClearLogCatCommand = "logcat -c";
    
    private class MonitorLogThread extends Thread{
    	ActivityStartingListener mListener;
    	public MonitorLogThread(ActivityStartingListener listener){
    		mListener = listener;
    	}
    	
    	BufferedReader br;
    	
		@Override
		public void run() {
			try {
	    		Process process;
	    		process = Runtime.getRuntime().exec(ClearLogCatCommand);
				process = Runtime.getRuntime().exec(LogCatCommand);
				br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				// Check if it matches the pattern
				while(((line=br.readLine()) != null) && !this.isInterrupted()){
					
					// Ignore launchers
					if (line.contains("cat=[" + Intent.CATEGORY_HOME + "]")) continue;
					
					Matcher m = ActivityNamePattern.matcher(line);
					if (!m.lookingAt()) continue;
					if (m.groupCount()<5){
						// Log.d("Detector", "Unknown problem while matching logcat output. Might be SDK version?");
						continue;
					}
					
					
					if (mListener!=null) mListener.onActivityStarting(m.group(2), m.group(4));
					
					Log.i("Detector", "Found activity launching: " + m.group(2) + "  /   " + m.group(4));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    }

}

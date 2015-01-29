package com.onquantum.androidlocalpushnotifications;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;


public class LocalPushNotificationsService extends Service{

	public static final String PUSH_NOTIFICATION_PREFERENCES  = "com.onquantum.localpushnotification.localpushnotificationservice";
	private static final String START_TIME = "start_time";
    private static final String REPEAT = "repeat";
    public static final String NOTIFY = "notify";
	
	private NotificationManager notificationManager;
	private long notificationCounter = 0;
	List<RunnableTask>runnableTasks = Collections.synchronizedList(new ArrayList<RunnableTask>());

    private static LocalPushNotificationsService instance = null;

    public static List getNotificationList() {
        if (instance != null)
            return instance.runnableTasks;
        return null;
    }

    public static void stopNotificationsService() {
        Log.i("info"," STOP LOCAL PUSH NOTIFICATION");
        if (instance != null) {
            Log.i("info"," INSTANCE = " + instance.toString());
            instance.stopSelf();
        }
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
        instance = (LocalPushNotificationsService)this;
		Log.i("info","LocalPushNotificationService onCrete() + " + instance);
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		Log.i("info", "LocalPushNotificationService onDestroy");
        synchronized (runnableTasks) {
            Log.i("info"," List of notification = " + runnableTasks.size());
            for (Iterator<RunnableTask>iterator = runnableTasks.iterator(); iterator.hasNext();) {
                RunnableTask task = iterator.next();
                Log.i("info"," STOP TASK BY ID : " + task.id + " startId = " + task.startId);
                task.stop();
                iterator.remove();
            }
        }
        instance = null;
        super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        String notify = intent.getStringExtra("notify");
        JSONObject jsonObject;
        
        try {
            jsonObject = new JSONObject(notify);
            
            long time = jsonObject.getLong("time_delay");
            boolean repeat = jsonObject.getBoolean("repeat"); 
            int iconId = jsonObject.getInt("icon");
            
            String id =  jsonObject.getString("id");           
            String statusBar = jsonObject.getString("status_bar");
            String title = jsonObject.getString("title");  
            String message = jsonObject.getString("message");   

            Log.i("info"," JObject = " + jsonObject.toString());

            if((flags & Service.START_FLAG_REDELIVERY) != Service.START_FLAG_REDELIVERY) {
                
            	SharedPreferences sharedPreferences = getSharedPreferences(PUSH_NOTIFICATION_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                
                jsonObject.put("startId",startId);
                jsonObject.put(START_TIME,System.currentTimeMillis());
                
                editor.putString(NOTIFY + "_" + startId, jsonObject.toString());
                editor.commit();

                RunnableTask runTask = new RunnableTask(id,time, repeat, startId,title, message,statusBar, iconId);
                synchronized (runnableTasks) {
                    runnableTasks.add(runTask);
                }
                new Thread(runTask).start();
            } else {
                
            	if (!repeat) {
                    JSONObject currentObject;
                    SharedPreferences sharedPreferences = getSharedPreferences(PUSH_NOTIFICATION_PREFERENCES, Context.MODE_PRIVATE);
                    
                    currentObject = new JSONObject(sharedPreferences.getString(NOTIFY + "_" + startId,""));
                    
                    long startTime = currentObject.getLong(START_TIME);
                    long currentTime = System.currentTimeMillis();
                    long deltaTime = (currentTime - startTime) / 1000L;
                    
                    if (deltaTime > time) {
                        time = 0;
                    } else {
                        time = time - deltaTime;
                    }
                    
                    currentObject.put("time_delay", time);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(NOTIFY + "_" + startId, currentObject.toString());
                    editor.commit();
                    
                    RunnableTask runTask = new RunnableTask(id, time, repeat, startId,title, message,statusBar, iconId);
                    runnableTasks.add(runTask);
                    new Thread(runTask).start();
                    
                } else {
                	RunnableTask runnableTask = new RunnableTask(id, time, repeat, startId,title, message, statusBar, iconId);
                    synchronized (runnableTasks) {
                        runnableTasks.add(runnableTask);
                    }
                    new Thread(runnableTask).start();
                }
            }

        } catch (JSONException e) {
            Log.i("info","*************************************************************************");
            Log.i("info"," EXCEPTION " + startId);
            Log.i("info",e.toString());
            Log.i("info","*************************************************************************");
            e.printStackTrace();
        }
		return START_REDELIVER_INTENT;
	}
	
    @SuppressWarnings({ "deprecation", "unused" })
	private void sendNotification(int startId, String title, String message, String statusBar, int iconId) {
        Log.i("info", " Notification : " + startId);
        JSONObject currentObject;
        SharedPreferences sharedPreferences = getSharedPreferences(PUSH_NOTIFICATION_PREFERENCES, Context.MODE_PRIVATE);

        try {
            currentObject = new JSONObject(sharedPreferences.getString(NOTIFY + "_" + startId,""));
            long startTime = currentObject.getLong(START_TIME);
            long endTime = System.currentTimeMillis();
            SimpleDateFormat dateFormater = new SimpleDateFormat("hh:mm:ss");

            Date date = new Date(startTime);
            Date end = new Date(endTime);

            Notification notification = new Notification(iconId, statusBar, System.currentTimeMillis());
            String packageName = getPackageName();
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            //notification.setLatestEventInfo(this, title + " " + startId + " :" + dateFormater.format(date) + " - " + dateFormater.format(end), message + " " + startId, pendingIntent);
            notification.setLatestEventInfo(this, title, message, pendingIntent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notification.defaults |= Notification.DEFAULT_SOUND;
            
            notificationManager.notify(startId, notification);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
	
	class RunnableTask implements Runnable{
		private int startId;
		private long timeDelay;
		private boolean isRunning = false;
		private boolean repeat = false;
		private String title;
		private String statusBar;
		private String message;
		private int iconId;
		private String id = "";
        private boolean enable = true;
		
		public RunnableTask(String id, long timeDelay, boolean repeat, int startId, String title, String message, String statusBar, int iconId) {
			this.timeDelay = timeDelay;
			this.repeat = repeat;
			this.startId = startId;
			this.title = title;
			this.message = message;
			this.statusBar = statusBar;
			this.iconId = iconId;
			this.isRunning = true;
			notificationCounter += 1;
		}
		@Override
		public void run() {
			Log.i("info"," RunnableTask START : startId = " + startId);
            while(isRunning) {
    			long time = timeDelay;
                while (time > 0) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                        time -= 1;
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    if (!isRunning)
                        break;
                }
                if (enable)
                    sendNotification(startId, title, message, statusBar, iconId);
                if(!repeat)
                	break;
            }
            
			Log.i("info"," RunnableTask STOP : startId = " + startId);
            stop();
            notificationCounter -= 1;

            synchronized (runnableTasks) {
                for (Iterator<RunnableTask>iterator = runnableTasks.iterator(); iterator.hasNext();) {
                    RunnableTask task = iterator.next();
                    if (task.startId == startId) {
                        Log.i("info"," STOP TASK BY ID : startId = " + startId);
                        iterator.remove();
                        break;
                    }
                }
            }

            if (notificationCounter <= 0) {
                Log.i("info"," STOP SELF");
                stopSelf();
            }
		}	
        public void stop() {
            SharedPreferences sharedPreferences = getSharedPreferences(PUSH_NOTIFICATION_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(NOTIFY + "_" + startId);
            editor.commit();
            isRunning = false;
            enable = false;
        }
        public void stopRunningTaskByID (String id) {
            synchronized (runnableTasks) {
                int i = 0;
                for (Iterator<RunnableTask>iterator = runnableTasks.iterator(); iterator.hasNext();) {
                    RunnableTask task = iterator.next();
                    if (task.id == id) {
                        Log.i("info"," STOP TASK BY ID : " + id + " startId = " + startId);
                        task.stop();
                        iterator.remove();
                    }
                    i++;
                }
            }
        }
        @Override
        public String toString() {
            return  "tmeDelay = " + timeDelay + " repeat = " + repeat + " startId = " + startId;
        }
	}
}

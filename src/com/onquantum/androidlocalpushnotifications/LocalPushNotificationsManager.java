package com.onquantum.androidlocalpushnotifications;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class LocalPushNotificationsManager {
	private LocalPushNotificationsManager() {}
	
	public static void sendPushNotification(Activity activity, long timeDelay, boolean repeat, String title, String message, String statusBarMessage) {
		if(title == null)
			title = "";
		if(message == null)
			message = "";
		if(statusBarMessage == null)
			statusBarMessage = "";

        int id = activity.getResources().getIdentifier("ic_stat_pn","drawable", activity.getPackageName());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("time_delay",timeDelay);
            jsonObject.put("repeat",repeat);
            jsonObject.put("icon",id);
            
            jsonObject.put("id","notify_id_3");
            jsonObject.put("status_bar", statusBarMessage);
            jsonObject.put("title",title);
            jsonObject.put("message",message);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(activity,LocalPushNotificationsService.class);
        intent.putExtra("notify",jsonObject.toString());
        activity.startService(intent);
	}

    public static List<LocalPushNotificationsService.RunnableTask> getNotificationList() {
        return LocalPushNotificationsService.getNotificationList();
    }
    
    public static void stopNotificationsService() {
    	LocalPushNotificationsService.stopNotificationsService();
    }
}

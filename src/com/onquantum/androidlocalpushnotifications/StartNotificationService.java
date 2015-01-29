package com.onquantum.androidlocalpushnotifications;

import java.util.Map;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class StartNotificationService extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
        Log.i("info"," BROADCAST RECEIVER START Notification service");
        SharedPreferences sharedPreferences = context.getSharedPreferences(LocalPushNotificationsService.PUSH_NOTIFICATION_PREFERENCES,Context.MODE_PRIVATE);
        Map<String, String> map = (Map<String, String>) sharedPreferences.getAll();
        Stack<String>stack = new Stack<>();

        for (String s : map.keySet()) {
            try {
                JSONObject jsonObject = new JSONObject(map.get(s));
                Log.i("info", s + " : JSON = " + jsonObject);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(s);
                editor.commit();
                stack.push(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        while (!stack.empty()) {
            Intent intentService = new Intent(context,LocalPushNotificationsService.class);
            intentService.putExtra("notify",stack.pop());
            context.startService(intentService);
        }
	}
}

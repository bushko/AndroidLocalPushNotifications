package com.onquantum.androidlocalpushnotifications;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import com.onquantum.androidlocalpushnotifications.LocalPushNotificationsManager;

public class MainActivity extends Activity {
	private Context context;
    private boolean repeat = false;
    private EditText editText;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		context = this;
		
        editText = (EditText)findViewById(R.id.editText);

        ((CheckBox)findViewById(R.id.checkBox)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeat = ((CheckBox)v).isChecked();
            }
        });
        ((Button)findViewById(R.id.button4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("info"," Local push notification");
                long timeDelay = Long.parseLong(editText.getText().toString());
                int id = getResources().getIdentifier("ic_launcher","drawable", context.getPackageName());
                LocalPushNotificationsManager.sendPushNotification((Activity) context, timeDelay, repeat, "", "%", "notification");
            }
        });
        
        ((Button)findViewById(R.id.button5)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent((Activity)context, LocalPushNotificationsService.class);
                ((Activity)context).stopService(intent);
            }
        });
        ((Button)findViewById(R.id.button6)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        ((Button)findViewById(R.id.button7)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LocalPushNotificationsService.stopNotificationsService();
                }catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
	}
}

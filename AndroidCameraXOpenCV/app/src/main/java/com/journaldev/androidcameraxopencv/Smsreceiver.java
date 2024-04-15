package com.journaldev.androidcameraxopencv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

//import org.apache.http.NameValuePair;
//import org.apache.http.message.BasicNameValuePair;
//import org.json.JSONObject;


public class Smsreceiver extends BroadcastReceiver {
	String this_msg;
	String this_phone="";
	String imei;
	SharedPreferences sh;

	
	

	String url="";

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Toast.makeText(arg0, "inside receiver", Toast.LENGTH_LONG).show();
		sh= PreferenceManager.getDefaultSharedPreferences(arg0);
		String ip=sh.getString("ip","");
		url="http://"+ip+":8080/Mobihelper/smsin";
		Bundle b = arg1.getExtras();
		Object[] obj = (Object[]) b.get("pdus");//protocol description units
		SmsMessage[] sms_list = new SmsMessage[obj.length];
		for (int i = 0; i < obj.length; i++) {
			sms_list[i] = SmsMessage.createFromPdu((byte[]) obj[i]);
		}
		this_msg = sms_list[0].getMessageBody();
		this_phone = sms_list[0].getOriginatingAddress();


		String str="You have a text message."+ this_msg;

		SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(arg0);
		SharedPreferences.Editor ed=sh.edit();
		ed.putString("Message",str);
		ed.commit();




	}

}

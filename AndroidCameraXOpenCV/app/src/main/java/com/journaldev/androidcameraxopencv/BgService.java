//package com.journaldev.androidcameraxopencv;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.IBinder;
//
//import com.android.volley.toolbox.StringRequest;
//
//public class BgService extends Service {
//    public BgService() {
//    }
//
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//
//        hnd=new android.os.Handler();
//
//
//
//    }
//
//    android.os.Handler hnd;
//
//    Runnable rnmsg=new Runnable() {
//        @Override
//        public void run() {
//
//
//            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//
//
//            String apiURL="http://"+sh.getString("ip","")+":5000/detect_knownperson";
//
//
//            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//            StringRequest postRequest = new StringRequest(Request.Method.POST, apiURL,
//                    new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//                            //  Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
//
//                            // response
//                            try {
//                                JSONObject jsonObj = new JSONObject(response);
//                                if (jsonObj.getString("status").equalsIgnoreCase("ok")) {
//
//
//
//                                }
//
//
//
//                                else {
//                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
//                                }
//
//                            }    catch (Exception e) {
//                                Toast.makeText(getApplicationContext(), "Error" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    },
//                    new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            // error
//                            Toast.makeText(getApplicationContext(), "eeeee" + error.toString(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//            ) {
//                @Override
//                protected Map<String, String> getParams() {
//                    SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                    Map<String, String> params = new HashMap<String, String>();
//
//                    params.put("lati",lati);
//                    params.put("longi",logi);
//                    params.put("bid",sh.getString("bid",""));
//
//                    return params;
//                }
//            };
//
//            int MY_SOCKET_TIMEOUT_MS=100000;
//
//            postRequest.setRetryPolicy(new DefaultRetryPolicy(
//                    MY_SOCKET_TIMEOUT_MS,
//                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//            requestQueue.add(postRequest);
//
//
////
////            SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
////            String msg=sh.getString("Message","");
////
////            if(msg.length()>0)
////            {
////                Speakerbox speakerbox = new Speakerbox(getApplication());
////                speakerbox.play(msg);
////
////                SharedPreferences.Editor ed=sh.edit();
////                ed.putString("Message","");
////                ed.commit();
////            }
//
//
//            hnd.postDelayed(rnmsg,6000);
//
//
//
//        }
//    };
//
//
//
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
//}
package com.journaldev.androidcameraxopencv;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.provider.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RecognitionListener {



    ///for ocr1
    public static final String PACKAGE_NAME = "com.journaldev.androidcameraxopencv";
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/";
    public static final String lang = "eng";
    ///end for ocr1

    private static final String TAG = "SimpleAndroidOCR.java";



    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE","android.permission.RECORD_AUDIO"};
    TextureView textureView;
    ImageView ivBitmap;
    LinearLayout llBottom;

    int currentImageType = Imgproc.COLOR_RGB2GRAY;

    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;
    Preview preview;



    FloatingActionButton btnCapture, btnOk, btnCancel;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }


    Handler hnd;


    public  void preparetessdata()
    {


        String[] paths = new String[] { DATA_PATH, DATA_PATH + "tessdata/" };

        for (String path : paths) {
            File dir = new File(path);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.v(TAG, "Created directory " + path + " on sdcard");
                }
            }

        }

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {

                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }


    }

    Runnable rncounter=new Runnable() {
        @Override
        public void run() {

            SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String msg=sh.getString("Counter","");

            if(msg.equalsIgnoreCase("1"))
            {

                SharedPreferences.Editor ed=sh.edit();
                ed.putString("Counter","");
                ed.commit();

                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (intent.resolveActivity(getPackageManager()) != null) {

                    Toast.makeText(getApplicationContext(),"Inside LL",Toast.LENGTH_SHORT).show();

                    startActivityForResult(intent, 10);
                } else {
                    Toast.makeText(getApplicationContext(), "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
                }
            }

        hnd.postDelayed(rncounter,2000);


        }
    };











    Runnable rnmsg=new Runnable() {
        @Override
        public void run() {


            SharedPreferences sh= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String msg=sh.getString("Message","");

            if(msg.length()>0)
            {
                Speakerbox speakerbox = new Speakerbox(getApplication());
                speakerbox.play(msg);

                SharedPreferences.Editor ed=sh.edit();
                ed.putString("Message","");
                ed.commit();
            }


            hnd.postDelayed(rnmsg,6000);



        }
    };

Runnable rnmsgview=new Runnable() {
        @Override
        public void run() {


            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


            String apiURL="http://"+sh.getString("ip","")+":5000/andviewmsgaaaa";


            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            StringRequest postRequest = new StringRequest(Request.Method.POST, apiURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //  Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

                            // response
                            try {
                                JSONObject jsonObj = new JSONObject(response);
                                if (jsonObj.getString("status").equalsIgnoreCase("ok")) {

                                    String msg= jsonObj.getString("msg");
                                    String msgid= jsonObj.getString("mid");

                                    SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                                    SharedPreferences.Editor ed = sh.edit();

                                    ed.putString("msgid",msgid);
                                    ed.commit();



                                    Speakerbox sp=new Speakerbox(getApplication());
                                    sp.play(msg);





                                }



                                else {
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                                }

                            }    catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Error" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            Toast.makeText(getApplicationContext(), "eeeee" + error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("bid",sh.getString("bid",""));
                    params.put("mid",sh.getString("msgid","0"));

                    return params;
                }
            };

            int MY_SOCKET_TIMEOUT_MS=100000;

            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    MY_SOCKET_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(postRequest);


//            hnd.postDelayed(rnmsgview,6000);



        }
    };


    EditText edip,edblindid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        edip=(EditText) findViewById(R.id.editText);
        edblindid=(EditText) findViewById(R.id.editText2);

//
//        VideoView vd = (VideoView)findViewById(R.id.videoView);
//        Uri uri = Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4");
//
//        vd.setMediaController(null);
//        vd.setVideoURI(uri);
//        vd.start();
//        vd.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                Toast.makeText(getApplicationContext(), "Video completed", Toast.LENGTH_LONG).show();
//            }
//        });




        SettingsContentObserver SettingsContentObserver = new SettingsContentObserver(MainActivity.this,new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, SettingsContentObserver );



        ActivityCompat.requestPermissions
                (MainActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        101);


        start();


        preparetessdata();





        hnd=new Handler();

        hnd.post(rnmsg);

        hnd.post(rncounter);
      //  hnd.post(rnmsgview);





        btnCapture = findViewById(R.id.btnCapture);
        btnOk = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnReject);

        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        llBottom = findViewById(R.id.llBottom);
        textureView = findViewById(R.id.textureView);
        ivBitmap = findViewById(R.id.ivBitmap);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {

        CameraX.unbindAll();
        preview = setPreview();
        imageCapture = setImageCapture();
        imageAnalysis = setImageAnalysis();

        //bind to lifecycle:
        CameraX.bindToLifecycle(this, preview, imageCapture, imageAnalysis);
    }


    private Preview setPreview() {

        Rational aspectRatio = new Rational(textureView.getWidth(), textureView.getHeight());
        Size screen = new Size(textureView.getWidth(), textureView.getHeight()); //size of the screen


        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        Preview preview = new Preview(pConfig);

        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent = (ViewGroup) textureView.getParent();
                        parent.removeView(textureView);
                        parent.addView(textureView, 0);

                        textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                });

        return preview;
    }


    private ImageCapture setImageCapture() {
        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCapture = new ImageCapture(imageCaptureConfig);


        btnCapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                String ipadrs = edip.getText().toString();
                SharedPreferences.Editor ed = sh.edit();
                ed.putString("ip",   ipadrs );
                ed.putString("bid",edblindid.getText().toString());
                ed.putString("id",edblindid.getText().toString());
                ed.commit();
//                String url = "http://"+ipadrs+":5000/android_login";
//                String im = "12222";
//
//                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
//                        new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//                                //  Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
//
//                                // response
//                                try {
//                                    JSONObject jsonObj = new JSONObject(response);
//                                    if (jsonObj.getString("status").equalsIgnoreCase("ok")) {
//
//
//
//                                        String id= jsonObj.getString("id");
//
//                                        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                                        SharedPreferences.Editor ed=sh.edit();
//                                        ed.putString("bid",id);
//                                        ed.putString("id",id);
//                                        ed.commit();
//


//                                        Toast.makeText(getApplicationContext(), id, Toast.LENGTH_SHORT).show();




                                        startService(new Intent(MainActivity.this, LocationService.class));

//
//                                    } else
//                                        Toast.makeText(getApplicationContext(), "Invaild User.....", Toast.LENGTH_SHORT).show();
//
//
//                                    // }
//                                    ///
//                                } catch (Exception e) {
//                                    Toast.makeText(getApplicationContext(), "Error" + e.getMessage().toString(), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        },
//                        new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                // error
//                                Toast.makeText(getApplicationContext(), "eeeee" + error.toString(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                ) {
//                    @Override
//                    protected Map<String, String> getParams() {
//                        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                        Map<String, String> params = new HashMap<String, String>();
//
////                        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
////                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
////                            // TODO: Consider calling
////                            //    ActivityCompat#requestPermissions
////                            // here to request the missing permissions, and then overriding
////                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
////                            //                                          int[] grantResults)
////                            // to handle the case where the user grants the permission. See the documentation
////                            // for ActivityCompat#requestPermissions for more details.
////
////                        }
////                        String a= telephonyManager.getDeviceId();
//
//
//                        params.put("imei","355696118360078");
//
//                        return params;
//                    }
//                };

//                int MY_SOCKET_TIMEOUT_MS=100000;
//
//                postRequest.setRetryPolicy(new DefaultRetryPolicy(
//                        MY_SOCKET_TIMEOUT_MS,
//                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                requestQueue.add(postRequest);




//
//                imgCapture.takePicture(new ImageCapture.OnImageCapturedListener() {
//                    @Override
//                    public void onCaptureSuccess(ImageProxy image, int rotationDegrees) {
//                        Bitmap bitmap = textureView.getBitmap();
//                        showAcceptedRejectedButton(true);
////                        ivBitmap.setImageBitmap(bitmap);
//                    }
//
//                    @Override
//                    public void onError(ImageCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
//                        super.onError(useCaseError, message, cause);
//                    }
//                });


                /*File file = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "" + System.currentTimeMillis() + "_JDCameraX.jpg");
                imgCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        Bitmap bitmap = textureView.getBitmap();
                        showAcceptedRejectedButton(true);
                        ivBitmap.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {

                    }
                });*/
            }
        });

        return imgCapture;
    }


    private ImageAnalysis setImageAnalysis() {

        // Setup image analysis pipeline that computes average pixel luminance
        HandlerThread analyzerThread = new HandlerThread("OpenCVAnalysis");
        analyzerThread.start();


        ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                .setImageQueueDepth(1).build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);

        imageAnalysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(ImageProxy image, int rotationDegrees) {
                        //Analyzing live camera feed begins.

                        final Bitmap bitmap = textureView.getBitmap();



                        if(bitmap==null)
                            return;

                        Mat mat = new Mat();
                        Utils.bitmapToMat(bitmap, mat);


//                        Imgproc.cvtColor(mat, mat, currentImageType);
//                        Utils.matToBitmap(mat, bitmap);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                ivBitmap.setImageBitmap(bitmap);
                            }
                        });

                    }
                });


        return imageAnalysis;

    }

    private void showAcceptedRejectedButton(boolean acceptedRejected) {
        if (acceptedRejected) {
            CameraX.unbind(preview, imageAnalysis);
            llBottom.setVisibility(View.VISIBLE);
            btnCapture.hide();
            textureView.setVisibility(View.GONE);
        } else {
            btnCapture.show();
            llBottom.setVisibility(View.GONE);
            textureView.setVisibility(View.VISIBLE);
            textureView.post(new Runnable() {
                @Override
                public void run() {
                    startCamera();
                }
            });
        }
    }


    private void updateTransform() {
        Matrix mx = new Matrix();
        float w = textureView.getMeasuredWidth();
        float h = textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int) textureView.getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float) rotationDgr, cX, cY);
        textureView.setTransform(mx);
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


//        Toast.makeText(getApplicationContext(),"eeeeeeeee",Toast.LENGTH_SHORT).show();


        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    txvResult.setText(result.get(0));

                    Toast.makeText(getApplicationContext(),result.get(0),Toast.LENGTH_SHORT).show();
                }

                break;
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
                speech.startListening(recognizerIntent);


            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.black_white:
                currentImageType = Imgproc.COLOR_RGB2GRAY;
                startCamera();
                return true;

            case R.id.hsv:
                currentImageType = Imgproc.COLOR_RGB2HSV;
                startCamera();
                return true;

            case R.id.lab:
                currentImageType = Imgproc.COLOR_RGB2Lab;
                startCamera();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnReject:
                showAcceptedRejectedButton(false);
                break;

            case R.id.btnAccept:
                File file = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "" + System.currentTimeMillis() + "_JDCameraX.jpg");
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        showAcceptedRejectedButton(false);

                        Toast.makeText(getApplicationContext(), "Image saved successfully in Pictures Folder", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {

                    }
                });
                break;
        }
    }

    public void start(){


//        Toast.makeText(getApplicationContext(),"111",Toast.LENGTH_LONG).show();


        speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

//        Toast.makeText(getApplicationContext(),"222",Toast.LENGTH_LONG).show();

    }
    @Override
    public void onReadyForSpeech(Bundle bundle) {

//        Toast.makeText(getApplicationContext(),"2",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBeginningOfSpeech() {
//        Toast.makeText(getApplicationContext(),"3",Toast.LENGTH_LONG).show();


    }

    @Override
    public void onRmsChanged(float v) {
//        Toast.makeText(getApplicationContext(),"4",Toast.LENGTH_LONG).show();


    }

    @Override
    public void onBufferReceived(byte[] bytes) {
//        Toast.makeText(getApplicationContext(),"5",Toast.LENGTH_LONG).show();


    }

    @Override
    public void onEndOfSpeech() {
        //Toast.makeText(getApplicationContext(),"6",Toast.LENGTH_LONG).show();


    }

    @Override
    public void onError(int i) {


        Toast.makeText(getApplicationContext(),"Say Again",Toast.LENGTH_SHORT).show();
        speech.startListening(recognizerIntent);

    }

    private void recognizeText(InputImage image) {

        // [START get_detector_default]
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        // [END get_detector_default]



        // [START run_detector]
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // [START_EXCLUDE]
                                // [START get_text]
                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    Rect boundingBox = block.getBoundingBox();
                                    Point[] cornerPoints = block.getCornerPoints();
                                    String text = block.getText();


                                    Speakerbox sp = new Speakerbox(getApplication());
                                    sp.play(text);

                                    String res="";

                                    Toast.makeText(getApplicationContext(),"ocr"+text,Toast.LENGTH_LONG).show();

                                    for (Text.Line line: block.getLines()) {
                                        // ...
                                        for (Text.Element element: line.getElements()) {
                                            // ...
                                            for (Text.Symbol symbol: element.getSymbols()) {

                                                res= res+ symbol.getText().toString();

                                            }
                                        }
                                    }



                              //      Speakerbox sp = new Speakerbox(getApplication());
//                                    sp.play(res);


                                }
                                // [END get_text]
                                // [END_EXCLUDE]
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
        // [END run_detector]
    }


    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> matches = bundle
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches) {
            text += result + "\n";

            String res=result.replace(" ","").toLowerCase();
            SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor ed=sh.edit();

            if(res.equalsIgnoreCase("capture"))
            {
                ed.putString("voice","capture");
                ed.commit();
                capturefortraffic();
            }
            else  if(res.equalsIgnoreCase("traffic"))
            {
                ed.putString("voice","traffic");
                ed.commit();
                capturefortraffic();
            }
            else  if(res.equalsIgnoreCase("recognise") || res.equalsIgnoreCase("recognise") )
            {
                ed.putString("voice","scan");
                ed.commit();
                capturefortraffic();
            }
            else  if(res.equalsIgnoreCase("emergency"))
            {
                ed.putString("voice","emergency");
                ed.commit();
                capturefortraffic();
            }
            else  if(res.equalsIgnoreCase("ocr"))
            {
                File file = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "" + System.currentTimeMillis() + "_JDCameraX.jpg");


                ///////#############################
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        showAcceptedRejectedButton(false);



                        int size = (int) file.length();
                        final byte[] bytes = new byte[size];
                        try {
                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                            buf.read(bytes, 0, bytes.length);
                            buf.close();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                        InputImage im = InputImage.fromBitmap(myBitmap,90);

                        recognizeText(im);










                        Toast.makeText(getApplicationContext(), "Image saved ", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {

                        Toast.makeText(getApplicationContext(), "Error in Pictures Folder", Toast.LENGTH_LONG).show();




                    }

                });

                ///////end##########################










            }
            else  if(res.equalsIgnoreCase("help"))
            {

                String contactNumber = "7034437264"; // to change with real value

                Cursor cursor = getApplicationContext().getContentResolver ()
                        .query (
                                ContactsContract.Data.CONTENT_URI,
                                new String [] { ContactsContract.Data._ID },
                                ContactsContract.RawContacts.ACCOUNT_TYPE + " = 'com.whatsapp' " +
                                        "AND " + ContactsContract.Data.MIMETYPE + " = 'vnd.android.cursor.item/vnd.com.whatsapp.video.call' " +
                                        "AND " + ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE '%" + contactNumber + "%'",
                                null,
                                ContactsContract.Contacts.DISPLAY_NAME
                        );

                if (cursor == null) {
                    // throw an exception
                }

                long id = -1;
                while (cursor.moveToNext()) {
                    id = cursor.getLong (cursor.getColumnIndex (ContactsContract.Data._ID));

                    Toast.makeText(getApplicationContext(),id+"hiiiii",Toast.LENGTH_LONG).show();

                    Intent intent = new Intent ();
                    intent.setAction (Intent.ACTION_VIEW);

                    intent.setDataAndType (Uri.parse ("content://com.android.contacts/data/" + id), "vnd.android.cursor.item/vnd.com.whatsapp.voip.call");
                    intent.setPackage ("com.whatsapp");

                    startActivity (intent);


                }

                if (!cursor.isClosed ()) {
                    cursor.close ();
                }
            }

            break;


        }

        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
        speech.startListening(recognizerIntent);

    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }



    public void capturefortraffic()
    {



        File file = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "" + System.currentTimeMillis() + "_JDCameraX.jpg");
        imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
            @Override
            public void onImageSaved(@NonNull File file) {
                showAcceptedRejectedButton(false);



                int size = (int) file.length();
                final byte[] bytes = new byte[size];
                try {
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


                String apiURL="http://"+sh.getString("ip","")+":5000/android_scan";
                VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, apiURL,
                        new Response.Listener<NetworkResponse>() {
                            @Override
                            public void onResponse(NetworkResponse response) {
                                try {

                                    JSONObject obj = new JSONObject(new String(response.data));
                                    String status= obj.getString("status");
                                    String msg= obj.getString("message");


                                    Speakerbox sp=new Speakerbox(getApplication());
                                    sp.play(msg);

                                    Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                                   //   Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {


                                    Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();

                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }) {


                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        SharedPreferences sh=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        String voice=sh.getString("voice","");
                        params.put("command",voice);
                        params.put("lid",sh.getString("id",""));
                        params.put("lattitude",LocationService.lati);
                        params.put("longitude",LocationService.logi);
                        return params;
                    }


                    @Override
                    protected Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        String imagename = System.currentTimeMillis()+"";

                        params.put("files", new DataPart(imagename, bytes));
                        return params;
                    }
                };

                volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(0000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                Volley.newRequestQueue(getApplicationContext()).add(volleyMultipartRequest);



//                Bitmap bitmap = BitmapFactory. decodeFile(file. getAbsolutePath());
//
//
//
//
//
//                    TessBaseAPI baseApi = new TessBaseAPI();
//                baseApi.setDebug(true);
//                baseApi.init(DATA_PATH, lang);
//                baseApi.setImage(bitmap);
////
//                String recognizedText = baseApi.getUTF8Text();
//
//
//                Toast.makeText(getApplicationContext(),recognizedText,Toast.LENGTH_LONG).show();








                Toast.makeText(getApplicationContext(), "Image saved successfully in Pictures Folder", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {

                Toast.makeText(getApplicationContext(), "Error in Pictures Folder", Toast.LENGTH_LONG).show();




            }

    });
    }


    TessBaseAPI tessBaseApi;

    private String extractText(Bitmap bitmap) {
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
            if (tessBaseApi == null) {
                Log.e("TAG", "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

//        tessBaseApi.init(DATA_PATH, lang);

//       //EXTRA SETTINGS
//        //For example if we only want to detect numbers
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
//
//        //blackList Example
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
//                "YTRWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");

        Log.d("TAG", "Training file loaded");
        tessBaseApi.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e("TAG", "Error in recognizing text.");
        }
        tessBaseApi.end();
        return extractedText;
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}

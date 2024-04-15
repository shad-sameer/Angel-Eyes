package com.journaldev.androidcameraxopencv;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class Cp extends AppCompatActivity implements View.OnClickListener, RecognitionListener {

    public static final String PACKAGE_NAME = "com.journaldev.androidcameraxopencv";
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/";

    // You should have the trained data file in assets folder
    // You can get them at:
    // https://github.com/tesseract-ocr/tessdata
    public static final String lang = "eng";

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


    String _path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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



        // _image = (ImageView) findViewById(R.id.image);


        _path = DATA_PATH + "/ocr.jpg";


        capturefortraffic();

//
//
//
//
//
//        SettingsContentObserver SettingsContentObserver = new SettingsContentObserver(Cp.this,new Handler());
//        getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, SettingsContentObserver );
//
//
//
//        ActivityCompat.requestPermissions
//                (Cp.this,
//                        new String[]{Manifest.permission.RECORD_AUDIO},
//                        101);
//
//
//        start();
//
//
//
//
//
//
//        hnd=new Handler();
//
//        hnd.post(rnmsg);
//
//        hnd.post(rncounter);
//
//
//
//
//        btnCapture = findViewById(R.id.btnCapture);
//        btnOk = findViewById(R.id.btnAccept);
//        btnCancel = findViewById(R.id.btnReject);
//
//        btnOk.setOnClickListener(this);
//        btnCancel.setOnClickListener(this);
//
//        llBottom = findViewById(R.id.llBottom);
//        textureView = findViewById(R.id.textureView);
//        ivBitmap = findViewById(R.id.ivBitmap);

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


                imgCapture.takePicture(new ImageCapture.OnImageCapturedListener() {
                    @Override
                    public void onCaptureSuccess(ImageProxy image, int rotationDegrees) {
                        Bitmap bitmap = textureView.getBitmap();



                        TessBaseAPI baseApi = new TessBaseAPI();
                        baseApi.setDebug(true);
                        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);
//
        String recognizedText = baseApi.getUTF8Text();

                        baseApi.end();

//                        showAcceptedRejectedButton(true);
//                        ivBitmap.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(ImageCapture.UseCaseError useCaseError, String message, @Nullable Throwable cause) {
                        super.onError(useCaseError, message, cause);
                    }
                });


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


      //  Toast.makeText(getApplicationContext(),"eeeeeeeee",Toast.LENGTH_SHORT).show();


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



                        TessBaseAPI baseApi = new TessBaseAPI();
                        baseApi.setDebug(true);
                        baseApi.init(DATA_PATH, lang);
//        baseApi.setImage(bitmap);
//
//        String recognizedText = baseApi.getUTF8Text();

                        baseApi.end();


//                        Toast.makeText(getApplicationContext(), "Image saved successfully in Pictures Folder", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {

                    }
                });
                break;
        }
    }

    public void start(){


      //  Toast.makeText(getApplicationContext(),"111",Toast.LENGTH_LONG).show();


        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

      //  Toast.makeText(getApplicationContext(),"222",Toast.LENGTH_LONG).show();

    }
    @Override
    public void onReadyForSpeech(Bundle bundle) {

    //    Toast.makeText(getApplicationContext(),"2",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBeginningOfSpeech() {
     //   Toast.makeText(getApplicationContext(),"3",Toast.LENGTH_LONG).show();


    }

    @Override
    public void onRmsChanged(float v) {
//        Toast.makeText(getApplicationContext(),"4",Toast.LENGTH_LONG).show();


    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Toast.makeText(getApplicationContext(),"5",Toast.LENGTH_LONG).show();


    }

    @Override
    public void onEndOfSpeech() {
        Toast.makeText(getApplicationContext(),"6",Toast.LENGTH_LONG).show();


    }

    @Override
    public void onError(int i) {


        Toast.makeText(getApplicationContext(),"errr",Toast.LENGTH_SHORT).show();
        speech.startListening(recognizerIntent);

    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> matches = bundle
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches) {
            text += result + "\n";

            String res=result.replace(" ","").toLowerCase();
            if(res.equalsIgnoreCase("capture"))
            {
                capturefortraffic();
            }

            break;


        }

//        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
        speech.startListening(recognizerIntent);

    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }



    public void capturefortraffic()
    {



        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
//        baseApi.setImage(bitmap);
//
//        String recognizedText = baseApi.getUTF8Text();

        baseApi.end();

//        Uri uri = Uri.fromFile(new File("//assets//eng.traineddata.txt"));
//        String testfilepath = uri.getPath();
//        File f = new File(testfilepath);
//        if (f.exists() == true) {
//            Toast.makeText(getApplicationContext(),"valid :" + testfilepath, Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(getApplicationContext(),"invalid :" + testfilepath, Toast.LENGTH_LONG).show();
//        }


//        File file = new File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "" + System.currentTimeMillis() + "_JDCameraX.jpg");
//        imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
//            @Override
//            public void onImageSaved(@NonNull File file) {
//                showAcceptedRejectedButton(false);
//
//
//
//
//
//
//                Toast.makeText(getApplicationContext(), "Image saved successfully in Pictures Folder", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
//
//                Toast.makeText(getApplicationContext(), "Error in Pictures Folder", Toast.LENGTH_LONG).show();
//
//
//
//
//            }
//
//    });
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


    public  void ocrsop(Bitmap bitmap) {

        try {

            // _image.setImageBitmap( bitmap );

            Log.v(TAG, "Before baseApi");

            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.setDebug(true);
            baseApi.init(DATA_PATH, lang);
            baseApi.setImage(bitmap);

            String recognizedText = baseApi.getUTF8Text();

            baseApi.end();

            // You now have the text in recognizedText var, you can do anything with it.
            // We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
            // so that garbage doesn't make it to the display.

            Log.v(TAG, "OCRED TEXT: " + recognizedText);

            if (lang.equalsIgnoreCase("eng")) {
                recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
            }

            recognizedText = recognizedText.trim();
        }
        catch (Exception ex)
        {

         //   Toast.makeText(getApplicationContext(),"Errrrrrrrrrrrrr",Toast.LENGTH_LONG).show();

        }
    }
}

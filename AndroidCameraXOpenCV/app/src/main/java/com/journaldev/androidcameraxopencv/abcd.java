package com.journaldev.androidcameraxopencv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class abcd extends AppCompatActivity implements RecognitionListener {

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abcd);

        start();

        ActivityCompat.requestPermissions
                (abcd.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        101);
        start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(abcd.this, "start talk...", Toast
                            .LENGTH_SHORT).show();
                    speech.startListening(recognizerIntent);
                } else {
                    Toast.makeText(abcd.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
                }
        }
    }

    public void start(){


        Toast.makeText(getApplicationContext(),"111",Toast.LENGTH_LONG).show();


        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

        Toast.makeText(getApplicationContext(),"222",Toast.LENGTH_LONG).show();

    }
    @Override
    public void onReadyForSpeech(Bundle bundle) {

        Toast.makeText(getApplicationContext(),"2",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBeginningOfSpeech() {
        Toast.makeText(getApplicationContext(),"3",Toast.LENGTH_LONG).show();


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
        for (String result : matches)
            text += result + "\n";

        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
        speech.startListening(recognizerIntent);

    }

    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }
}

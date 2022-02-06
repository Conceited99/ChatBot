package com.example.chatbot;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private RecyclerView ChatsRV;
    private EditText userMsgEdit;
    TextView user;
    private FloatingActionButton sendMsgFAB;
    ImageButton voice;
    private final String BOT_Key = "bot";
    private final String User_Key = "user";
    private ArrayList<ChatsModel> chatsModelArrayList;
    private ChatVrAdapter chatVrAdapter;
    public static final Integer RecordAudioRequestCode=1;
    private SpeechRecognizer speechRecognizer;
    AlertDialog.Builder alertSpeechDialog;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChatsRV= findViewById(R.id.idRVChats);
        userMsgEdit = findViewById(R.id.idEditmessage);
        sendMsgFAB = findViewById(R.id.idFABSend);
        voice = findViewById(R.id.voice);
        user = findViewById(R.id.idTVUser);
        chatsModelArrayList = new ArrayList<>();
        chatVrAdapter =  new ChatVrAdapter(chatsModelArrayList,this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        ChatsRV.setLayoutManager(manager);
        ChatsRV.setAdapter(chatVrAdapter);
        sendMsgFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userMsgEdit.getText().toString().isEmpty())
                {
                    Toast.makeText(MainActivity.this,"Please Enter Your Message",Toast.LENGTH_SHORT).show();
                    return;
                }
                getResponse(userMsgEdit.getText().toString());
                userMsgEdit.setText("");
            }
        });

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)!=
                PackageManager.PERMISSION_GRANTED)
        {
            checkPermission();
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent sppechIntent =  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sppechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        sppechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        sppechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"LISTENING...");

        /*speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.alertcustom,viewGroup,false);

                alertSpeechDialog = new AlertDialog.Builder(MainActivity.this);
                alertSpeechDialog.setMessage("LISTENING...");
                alertSpeechDialog.setView(dialogView);
                alertDialog = alertSpeechDialog.create();
                alertDialog.show();
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle bundle) {
                voice.setImageResource(R.drawable.ic_mic);
                ArrayList<String> arrayList = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                user.setText(arrayList.get(0));
                alertDialog.dismiss();
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int eventType, Bundle bundle  ) {
            }
        });

        voice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
               if (event.getAction() == MotionEvent.ACTION_UP)
               {
                   speechRecognizer.stopListening();
               }
               if (event.getAction() == MotionEvent.ACTION_DOWN)
               {
                   voice.setImageResource(R.drawable.ic_mic);
                   speechRecognizer.startListening(sppechIntent);
               }

                return false;
            }
        });*/
    }
    public void getSpeechInput(View view)
    {
        final Intent sppechIntent =  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sppechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        sppechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        sppechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"LISTENING...");
        if (sppechIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(sppechIntent,10);

        }else {
            Toast.makeText(this, "Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case 10:
                if(resultCode == RESULT_OK && data != null)
                {
                    ArrayList<String > result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);;
                    user.setText(result.get(0));
                }
                break;
        }
    }

    private void checkPermission() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.RECORD_AUDIO},
                    RecordAudioRequestCode);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == RecordAudioRequestCode && grantResults.length>0)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getResponse(String message) {
        chatsModelArrayList.add(new ChatsModel(message,User_Key));
        chatVrAdapter.notifyDataSetChanged();
        String url = "http://api.brainshop.ai/get?bid=161267&key=xqLmuLYm7WrUpco5&uid=[uid]&msg="+ message;
        String BASE_URL = "http://api.brainshop.ai";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        Call<MsgModel> call = retrofitAPI.getMessage(url);
        call.enqueue(new Callback<MsgModel>() {
            @Override
            public void onResponse(Call<MsgModel> call, Response<MsgModel> response) {
                if (response.isSuccessful())
                {
                    MsgModel model = response.body();
                    chatsModelArrayList.add(new ChatsModel(model.getCnt(),BOT_Key));
                    chatVrAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<MsgModel> call, Throwable t) {
                chatsModelArrayList.add(new ChatsModel("Please Revert Question",BOT_Key));
                chatVrAdapter.notifyDataSetChanged();
            }
        });
    }
}
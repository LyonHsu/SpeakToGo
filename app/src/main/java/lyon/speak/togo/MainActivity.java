package lyon.speak.togo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import ai.api.model.AIResponse;
import lyon.speak.togo.DialogFlow.DialogFlowInit;
import lyon.speak.togo.NetWork.NetWork;

public class MainActivity extends AppCompatActivity {

    String TAG = MainActivity.class.getSimpleName();
    ListView mainListView;
    private TextToSpeech textToSpeech;
    String SpeakToGO = "SpeakToGO:";
    ArrayList<SpeakToGoItem> arrayList = new ArrayList<>();
    MainListViewAdapter adapter;
    final int TOAST = 1;
    NetWork netWork;
    DialogFlowInit dialogFlowInit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        arrayList = new ArrayList<>();
        final String startString = getString(R.string.this_is_speak_to_go);
        arrayList.add(setSpeakToGo(SpeakToGoItem.SPEAKTOGOTYPE ,startString));
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d(TAG, "TTS init status:" + status);
                if (status != TextToSpeech.ERROR) {
                    int isspeak = textToSpeech.setLanguage(Locale.ENGLISH);
                    LyonTextToSpeech (startString);
                }
            }
        });
//        textToSpeech.speak("Text to speech test", TextToSpeech.QUEUE_FLUSH, null);     //??

        netWork = (NetWork)findViewById(R.id.network);

        mainListView = (ListView)findViewById(R.id.mainListView);
        adapter = new MainListViewAdapter(this,arrayList);
        mainListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        dialogFlowInit = new DialogFlowInit(this){
            @Override
            public void DialogFlowSpeech(String speech) {
                super.DialogFlowSpeech(speech);
                arrayList.add(setSpeakToGo(SpeakToGoItem.SPEAKTOGOTYPE ,speech));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void DialogFlowAction(String action) {
                super.DialogFlowAction(action);
                if(action.equals("recommend")){
                    pleaseToSay();
                }
            }
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(netWork!=null)
            netWork.onResume();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null)
            textToSpeech.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 200){
            if(resultCode == RESULT_OK && data != null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String req = result.get(0);
                arrayList.add(setSpeakToGo(SpeakToGoItem.CUSTOMERTYPE ,req));
                adapter.notifyDataSetChanged();
                LyonTextToSpeech ("你說了"+req);
                if(dialogFlowInit!=null){
                    dialogFlowInit.setAiRequest(req);
                }

            }
        }
    }

    private Handler mMainHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TOAST:
                    String sss = msg.obj.toString();
                    Toast.makeText(getApplicationContext(),"Intent problem:"+sss, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void pleaseToSay(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "???");
        try{
            startActivityForResult(intent,200);
        }catch (ActivityNotFoundException a){
            Message message = new Message();
            message.what=TOAST;
            message.obj = a.toString();
            mMainHandler.sendMessage(message);
            Log.e(TAG,"pleaseToSay ActivityNotFoundException:"+a);
        }
    }


    private SpeakToGoItem setSpeakToGo(int Type , String sss){
        SpeakToGoItem speakToGoItem = new SpeakToGoItem();
        speakToGoItem.Type=Type;
        speakToGoItem.sss=sss;
        return speakToGoItem;
    }

    private ArrayList<HashMap<String,String>> getEngorChingString(String s){
        Log.d(TAG,"20190605 string:"+s);
        HashMap<String,String> hashMap = new HashMap<>();
        ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
        char[] c = s.toCharArray();
        Log.d(TAG,"20190605 c size:"+c.length);
        String word="";
        boolean isEng=false;
        boolean isoldEng=false;
        for(int i=0;i<c.length;i++){
            String cc = c[i]+"";

            if( cc.matches("[a-zA-Z|\\.]*") )//a-zA-Z0-9
            {
                isEng=true;
            }
            else
            {
                isEng=false;
            }
            Log.d(TAG,"20190605 c:"+cc+" isEng:"+isEng+ " / "+isoldEng);

            if(isoldEng!=isEng){
                hashMap.put("word",word);
                hashMap.put("isEng",isoldEng+"");
                arrayList.add(hashMap);
                isoldEng=isEng;
                word="";
                hashMap = new HashMap<>();
            }
            word=word+cc;
            Log.d(TAG,"20190605 word:"+word);
        }
        hashMap.put("word",word);
        hashMap.put("isEng",isoldEng+"");
        arrayList.add(hashMap);

        for(int i=0;i<arrayList.size();i++){
            Log.d(TAG,"20190605 arrayList:"+arrayList.get(i).get("word")+" / "+arrayList.get(i).get("isEng"));
        }

        return arrayList;
    }

    public TextToSpeech getTextToSpeech(){
        //set text to speech
        if(textToSpeech==null) {
            textToSpeech= new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    Log.d(TAG, "getTextToSpeech TTS init status:" + status);
                    if (status != TextToSpeech.ERROR) {
//                        int result = textToSpeech.setLanguage(Locale.getDefault());//Locale.);
                        int result = textToSpeech.setLanguage(Locale.getDefault());

                        Log.d(TAG, "getTextToSpeech speak result init:" + result);
                    }
                }
            });
        }
        return textToSpeech;
    }


    public void LyonTextToSpeech(String sss){
        TextToSpeech textToSpeech =  getTextToSpeech();
        String TAGG = "LyonTextToSpeech "+TAG;
        if(textToSpeech==null)
        {
            Log.e(TAGG,"textToSpeech==null");
            return;
        }
        ArrayList<HashMap<String,String>> arrayList = getEngorChingString(sss);
        for(int i =0;i<arrayList.size();i++){
            int result=-1;
            if(arrayList.get(i).get("isEng").equals("true")){
                result =textToSpeech.setLanguage(Locale.ENGLISH);
                result=2;
            }else{
                result =textToSpeech.setLanguage(Locale.TAIWAN);
                result=1;
            }
            int isSpeak=-2;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (i == 0) {
                    isSpeak = textToSpeech.speak(arrayList.get(i).get("word"), TextToSpeech.QUEUE_FLUSH, null, null);
                } else
                    isSpeak = textToSpeech.speak(arrayList.get(i).get("word"), TextToSpeech.QUEUE_ADD, null, null);
            }else{
                if (i == 0) {
                    isSpeak = textToSpeech.speak(arrayList.get(i).get("word"), TextToSpeech.QUEUE_FLUSH, null);
                } else
                    isSpeak = textToSpeech.speak(arrayList.get(i).get("word"), TextToSpeech.QUEUE_ADD, null);

            }
            if(isSpeak == TextToSpeech.ERROR){
                Log.e(TAGG,"\""+arrayList.get(i).get("word")+"\" speak isSpeak:ERROR");
            }else
                Log.d(TAGG, "\""+arrayList.get(i).get("word")+"\" speak result:" + result+" isSpeak="+isSpeak);
        }
    }
}

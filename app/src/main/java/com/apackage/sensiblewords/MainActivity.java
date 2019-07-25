package com.apackage.sensiblewords;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    EditText et_input;
    TextView tv_output;
    Button bt_go;

    ProgressBar progressBar;

    static int responseCode;
    public List<String> wordsList = new ArrayList<String>();
    public List<String> updatedWordsList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_input = findViewById(R.id.editTextInput);
        tv_output = findViewById(R.id.textViewOutput);
        responseCode=0;

        progressBar = findViewById(R.id.progress_circular);

        bt_go = findViewById(R.id.buttonGo);
        bt_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordsList.clear();
                updatedWordsList.clear();
                tv_output.setText("");
                progressBar.setVisibility(View.VISIBLE);
                String inputWord = et_input.getText().toString();
                if (inputWord.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Enter atleast one character!", Toast.LENGTH_SHORT).show();
                } else {
                    permutations("", inputWord);
                    final int size = wordsList.size();
                    for (int i =0;i<size;i++){
                        final int finalI = i;
                        CallbackTask callbackTask = new CallbackTask(new OnEventListener<Integer>() {
                            @Override
                            public void onSuccess() {
                                Log.v("code in main: ",String.valueOf(MainActivity.responseCode));
                                if (MainActivity.responseCode==200){
                                    updatedWordsList.add(wordsList.get(finalI));
                                }
                                if (finalI==size-1){
                                    progressBar.setVisibility(View.GONE);
                                }
                                tv_output.setText(updatedWordsList.toString());
                            }
                        });
                        callbackTask.execute(inflections(wordsList.get(i)));
                    }
                }
            }
        });
        //new CallbackTask().execute(inflections());
        //permutations("", "abc");
        //Log.v("list: ", wordsList.toString());
    }


    void completed(){
        tv_output.setText(wordsList.toString());
    }

    private void permutations(String candidate, String remaining) {
        if (remaining.length() == 0) {
            wordsList.add(candidate);
            Log.v("indiv",candidate);
        }

        for (int i = 0; i < remaining.length(); i++) {
            String newCandidate = candidate + remaining.charAt(i);

            String newRemaining = remaining.substring(0, i) +
                    remaining.substring(i + 1);

            permutations(newCandidate, newRemaining);
        }
    }

    private String inflections(String wordParam) {
        final String language = "en";
        final String word = wordParam;
        final String word_id = word.toLowerCase();
        return "https://od-api.oxforddictionaries.com:443/api/v2/lemmas/" + language + "/" + word_id;
    }
}

class CallbackTask extends AsyncTask<String, Integer, Integer> {

    public OnEventListener<Integer> mCallBack;

    public CallbackTask(OnEventListener<Integer> mCallBack) {
        this.mCallBack = mCallBack;
    }

    @Override
    protected Integer doInBackground(String... params) {

        int code = 0;
        //TODO: replace with your own app id and app key
        final String app_id = "c2a536cc";
        final String app_key = "0f88a498ff24fa4361977158b7b17f20";
        HttpsURLConnection urlConnection = null;
        try {
            URL url = new URL(params[0]);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("app_id", app_id);
            urlConnection.setRequestProperty("app_key", app_key);

            // read the output from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            //Log.d("code: ", String.valueOf(urlConnection.getResponseCode()));
            return urlConnection.getResponseCode();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                 code = urlConnection.getResponseCode();
                 //Log.d("code inside async task: ",String.valueOf(code));
            } catch (IOException e1) {
                //e1.printStackTrace();
            }
        }
        return code;
    }

    @Override
    protected void onPostExecute(Integer result) {
        MainActivity.responseCode=result;
        Log.d("code: ",String.valueOf(MainActivity.responseCode));
        mCallBack.onSuccess();
        super.onPostExecute(result);
    }
}
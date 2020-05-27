package com.jl74566.tensorflowlite;

import android.app.Application;

import android.os.Handler;
import android.util.Log;

import java.util.List;

public class ModelRepository {
    private String input;
    private TextClassificationClient mClient;
    private Handler handler;


    public ModelRepository(Application application, String input){
        this.mClient = new TextClassificationClient(application);
        this.input = input;
        handler = new Handler();
    }

    public void setInput(String mInput) {
        this.input = mInput;
    }

    protected void doClassify(){
        handler.post(new classifyRunnable(input, mClient));
    }

    protected void loadModels(){
        handler.post(new loadRunnable(mClient));
    }

    protected static class loadRunnable implements Runnable{
        private TextClassificationClient client;

        public loadRunnable(TextClassificationClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            client.load();
        }
    }

    protected static class classifyRunnable implements Runnable{
       private String text;
       private TextClassificationClient client;

        public classifyRunnable(String text, TextClassificationClient client) {
            this.text = text;
            this.client = client;
        }

        @Override
        public void run() {
            List<TextClassificationClient.Result> results = client.classify(text);
            Log.d("runnn", results.get(0).toString());
        }
    }
}

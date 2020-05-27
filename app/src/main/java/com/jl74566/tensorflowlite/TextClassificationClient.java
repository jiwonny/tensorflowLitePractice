package com.jl74566.tensorflowlite;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.WorkerThread;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class TextClassificationClient {
    private static final String TAG = "TextClassificationDemo";
    private static final String MODEL_PATH = "text_classification.tflite";
    private static final String DIC_PATH = "text_classification_vocab.txt";
    private static final String LABEL_PATH = "text_classification_labels.txt";

    private static final int SENTENCE_LEN = 256;
    private static final String SIMPLE_SPACE_OR_PUNCTUATION = "|\\,|\\.|\\!|\\?|\n";

    private static final String START = "<START>";
    private static final String PAD = "<PAD>";
    private static final String UNKNOWN = "<UNKNOWN>";

    private static final int MAX_RESULTS = 3;

    private final Context context;
    private final Map<String, Integer> dic = new HashMap<>();
    private final List<String> labels = new ArrayList<>();
    private Interpreter tflite;

    public static class Result {
        private final String id;
        private final String title;
        private final Float confidence;

        public Result(String id, String title, Float confidence) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Result{");
            sb.append("id='").append(id).append('\'');
            sb.append(", title='").append(title).append('\'');
            sb.append(", confidence=").append(confidence);
            sb.append('}');
            return sb.toString();
        }
    }

    public TextClassificationClient(Context context) {
        this.context = context;
    }

    @WorkerThread
    public void load(){
        loadModel();
        loadDictionary();
        loadLabels();
    }

    @WorkerThread
    private synchronized void loadModel() {
        try{
            ByteBuffer buffer = loadModelFile(this.context.getAssets());
            tflite = new Interpreter(buffer);
            Log.v(TAG, "TFLite Model Loaded");
        } catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
    }

    @WorkerThread
    private synchronized void loadDictionary() {
        try{
            loadDictionaryFile(this.context.getAssets());
            Log.v(TAG, "Dictionary Loaded");
        } catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
    }

    @WorkerThread
    private synchronized void loadLabels() {
        try{
            loadLabelFile(this.context.getAssets());
            Log.v(TAG, "Labels Loaded");
        } catch (IOException e){
            Log.e(TAG, e.getMessage());
        }
    }


    private static MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
        Log.d("loadModelFile", "loadModelFile" + MODEL_PATH);
        AssetFileDescriptor fileDescriptor = assetManager.openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void loadDictionaryFile(AssetManager assetManager) throws IOException {
        InputStream ins = assetManager.open(DIC_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        while (reader.ready()) {
            List<String> line = Arrays.asList(reader.readLine().split(" "));
            if(line.size() < 2){
                continue;
            }

            dic.put(line.get(0), Integer.parseInt(line.get(1)));
        }
    }

    private void loadLabelFile(AssetManager assetManager) throws IOException {
        InputStream ins = assetManager.open(LABEL_PATH);
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        while(reader.ready()) {
            labels.add(reader.readLine());
        }
    }

    @WorkerThread
    public synchronized void unload() {
        tflite.close();
        dic.clear();
        labels.clear();
    }

    // classify an input string and returns the classification results
    @WorkerThread
    public synchronized List<Result> classify(String text){
        float[][] input = tokenizeInputText(text);

        float[][] output = new float[1][labels.size()];
        tflite.run(input, output);

        final ArrayList<Result> results = new ArrayList<>();
        results.add(new Result("" + 1, labels.get(0), output[0][1]));
//        results.add(new Result("" + 1, labels.get(1), output[0][1]));

        // Return the probability of each class.
        return results;
    }

    float[][] tokenizeInputText(String text) {
        float[] tmp = new float[SENTENCE_LEN];
        List<String> array = Arrays.asList(text.split(SIMPLE_SPACE_OR_PUNCTUATION));

        int index = 0;

        if(dic.containsKey(START)){
            tmp[index++] = dic.get(START);
        }

        for(String word: array){
            if(index >= SENTENCE_LEN){
                break;
            }
            tmp[index++] = dic.containsKey(word) ? dic.get(word) : (int) dic.get(UNKNOWN);
        }

        Arrays.fill(tmp, index, SENTENCE_LEN - 1, (int) dic.get(PAD));
        float[][] ans = {tmp};
        return ans;
    }

    Map<String, Integer> getDic() {
        return this.dic;
    }

    Interpreter getTflite() {
        return this.tflite;
    }

    List<String> getLabels() {
        return this.labels;
    }



}

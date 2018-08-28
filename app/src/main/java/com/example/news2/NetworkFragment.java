package com.example.news2;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * A simple {@link Fragment} subclass.
 */
public class NetworkFragment extends Fragment {
    private static final String TAG = "NetworkFragment";

    private DownloadCallback parentContext;
    private DownloadTask downloadTask;


    public static NetworkFragment getInstance(FragmentManager fManager/*, String url*/){
        NetworkFragment fragment = new NetworkFragment();
        /* (Leaving old code for doc purpose)
        Bundle args = new Bundle();
        args.putString('URL_KEY', url)
        fragment.setArgument(url)
        */
        fManager.beginTransaction().add(fragment, TAG).commit();
        return fragment;

        // Because fragment's constructor is parameter-less and called on re-instatiation, when instance
        // needs to take arguments, you need to use a factory method i.e. getInstance() that uses
        // setArguments(). These arguments are saved when the fragment is re-instantiated. See also
        // onCreate().

        // The constructor calls whichever the last setArguments() on re-instantiation.
        // That's why getArguments() works fine in onCreate().

        // Creating another public constructor with parameters are discouraged b/c
        // these won't be called when the fragment is re-instantiated.
        // There are some memory considerations that I still don't fully understand about setArguments

    }

    /* (Leaving old code for doc purpose)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrlString = getArguments().getString(URL_KEY);
    }
    */

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        // Overriding this to update parentContext with context.
        // I guess you can't do this in onCreate.
        parentContext = (DownloadCallback) getParentFragment();
    }

    @Override
    public void onDestroy(){
        // Make sure that download doesn't continue after fragment is destroyed.
        cancelDownload();
        super.onDestroy();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        // This avoids memory leak.
        parentContext = null;
    }

    public void cancelDownload(){
        if(downloadTask != null){
            downloadTask.cancel(true);
            downloadTask = null;
        }
    }

    public void startDownload(String stringUrl){
        cancelDownload();
        downloadTask = new DownloadTask();
        downloadTask.setParentContext(parentContext);
        downloadTask.execute(stringUrl);
    }

    // Generic types AsyncTask<params, progress, result>
    private class DownloadTask extends AsyncTask<String, Integer, DownloadTask.Result>{
        private DownloadCallback parentContext;
        class Result{
            private JSONObject resultValue;
            private Exception exception;
            public void setResult(Exception _exception){
                exception = _exception;
            }
            public void setResult(JSONObject _resultValue){
                resultValue = _resultValue;
            }

            public JSONObject getResultValue() {
                return resultValue;
            }

            public Exception getException() {
                return exception;
            }
        }

        public void setParentContext(DownloadCallback context){
            parentContext = context;
        }

        @Override
        protected void onPreExecute(){
            if (parentContext != null){
                NetworkInfo networkInfo = parentContext.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                    (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                            && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    parentContext.updateFromDownloads(null);
                    cancel(true); // AsyncTask function
                    // Should show some UI msg
                }
            }
        }

        protected DownloadTask.Result doInBackground(String... urls){
            // this argument is the same arguments passed in .execute() function.
            Result result = new Result();
            if (!isCancelled() && urls != null && urls.length > 0) {
                String urlString = urls[0];
                try {
                    URL url = new URL(urlString);
                    JSONObject resultJson = downloadUrl(url);
                    if (resultJson != null) {
                        result.setResult(resultJson);
                    } else {
                        throw new IOException("No response received.");
                    }
                } catch(Exception e) {
                    result.setResult(e);
                    Log.d("HK:doInBackground", e.getMessage());
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            if (result != null && parentContext != null) {
                if (result.getException() != null){
                    String errorMsg = result.getException().getMessage();
                    JSONObject errorResult = new JSONObject();
                    try {
                        errorResult.put("error", errorMsg);
                    } catch (JSONException e) {
                        Log.d("HK:onPostExecute", e.getMessage());
                    }
                    Log.d("HK:onPostExecute", "exception");
                    parentContext.updateFromDownloads(errorResult);
                } else if (result.getResultValue() != null){
                    Log.d("HK:onPostExecute", "resultValue");
                    parentContext.updateFromDownloads(result.getResultValue());
                } else {
                    Log.d("HK:onPostExecute", "result is something else");
                }
                parentContext.finishedDownloading();
            } else {
                Log.d("HK:onPostExecute", "something is null");
                cancel(true);
            }
        }

        private JSONObject downloadUrl(URL url) throws IOException {
            InputStream stream = null;
            HttpsURLConnection connection = null;
            JSONObject result = null;
            try {
                connection = (HttpsURLConnection) url.openConnection();
                connection.setReadTimeout(3000); //3000 is arbitrary.
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("GET");
                // Already true by default but setting just in case; needs to be true since this request
                // is carrying an input (response) body.
                connection.setDoInput(true);
                connection.connect();

                publishProgress(DownloadCallback.Progress.CONNECT_SUCCESS);
                // This triggers onProgressUpdate
                int responseCode = connection.getResponseCode();
                Log.d("HK:DownloadUrl", "connected " + responseCode);

                if (responseCode != HttpsURLConnection.HTTP_OK){
                    throw new IOException("HTTP code: " + responseCode);
                }

                stream = connection.getInputStream();
                publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS);

                if (stream != null){
                    JsonReader reader = new JsonReader(new InputStreamReader(stream));
                    result = JsonParser(reader);
                }
            } finally {
                if (stream != null){
                    stream.close();
                }
                if (connection != null){
                    connection.disconnect();
                }
            }
            return result;
        }

        protected void onProgressUpdate(Integer... progresses){
            Integer status = progresses[0];
            Log.d("HK:NF.onProgressUpdate", "status: " + status);
            parentContext.onProgressUpdate(status);
        }

        private JSONObject JsonParser(JsonReader reader){
            // reader must start with BEGIN_OBJECT
            JSONObject result = new JSONObject();
            try {
                if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                    throw new IllegalAccessException("reader must start with BEGIN_OBJECT");
                }
                reader.beginObject();
                while(reader.hasNext()) {
                    String name = reader.nextName();
                    JsonToken nextToken = reader.peek();

                    if (nextToken.equals(JsonToken.STRING)) {
                        result.put(name, reader.nextString());

                    } else if (nextToken.equals(JsonToken.NUMBER)) {
                        result.put(name, String.valueOf(reader.nextInt()));

                    } else if (nextToken.equals(JsonToken.BEGIN_OBJECT)) {
                        JSONObject subResult = JsonParser(reader);
                        result.put(name, subResult);

                    } else if (nextToken.equals(JsonToken.BEGIN_ARRAY)) {
                        reader.beginArray();
                        result.put(name, new JSONArray());

                        while (reader.hasNext()) {
                            JSONObject subResult = JsonParser(reader);
                            result.accumulate(name, subResult);
                        }
                        reader.endArray();

                    } else if (nextToken.equals(JsonToken.NULL)){
                        result.put(name, "(no description)");
                        reader.skipValue();

                    } else {
                        result.put(name, "Invalid data type");
                        reader.skipValue();
                    }
                }
                reader.endObject();
            } catch (Exception e){
                result = null;
                Log.d("HK:JsonParser", e.getMessage());
            }

            return result;
        }

        @Override
        protected void onCancelled(Result result){}
    }

}

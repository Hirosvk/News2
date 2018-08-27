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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private static final String URL_KEY = "URL_KEY";

    private String stringUrl;
    private DownloadCallback parentContext;
    private DownloadTask downloadTask;

    public NetworkFragment() {
        // Required empty public constructor

        // This is called when the fragment is re-instantiated.
        // Because of the parameter-less requirement, setArguments() is used to
        // update member variable of the class.
        // Creating another public constructor with parameters are discouraged b/c
        // these won't be called when the fragment is re-instantiated.
        // There are some memory considerations that I still don't fully understand about
        // re: use of setArguments
    }
    // Why is NetworkFragment's method is handling the add-transaction instead of the parent?
    // This is cleaner but seems a bit weird design.
    public static NetworkFragment getInstance(FragmentManager fManager, String url){
        NetworkFragment fragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        fragment.setArguments(args);
        fManager.beginTransaction().add(fragment, TAG).commit();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstantState){
        super.onCreate(savedInstantState);
        stringUrl = getArguments().getString(URL_KEY);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        // Overriding this to update parentContext with context.
        // I guess you can't do this in onCreate.
        parentContext = (DownloadCallback) getParentFragment();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        // This avoids memory leak.
        parentContext = null;
    }

    @Override
    public void onDestroy(){
        // Make sure that download doesn't continue after fragment is destroyed.
        cancelDownload();
        super.onDestroy();
    }

    public void cancelDownload(){
        if(downloadTask != null){
            downloadTask.cancel(true);
        }
    }

    public void startDownload(){
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
                    Log.d("doInBackground", e.getMessage());
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
                        Log.d("onPostExecute", e.getMessage());
                    }
                    Log.d("onPostExecute", "exception");
                    parentContext.updateFromDownloads(errorResult);
                } else if (result.getResultValue() != null){
                    Log.d("onPostExecute", "resultValue");
                    parentContext.updateFromDownloads(result.getResultValue());
                } else {
                    Log.d("onPostExecute", "resultValue");
                }
                parentContext.finishedDownloading();
            }
            Log.d("onPostExecute", "is null");
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
                Log.d("DownloadUrl", "connected " + responseCode);

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

        protected void onProgressUpdate(int status){
            // In the example, this class never implements onProgressUpdate.
            // This function is written in the parent fragment, which I believe won't
            // be invoked unless there is one here.
            parentContext.onProgressUpdate(status);
        }

        private JSONObject JsonParser(JsonReader reader){
            JSONObject result = new JSONObject();
            try {
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

                    } else if (nextToken.equals(JsonToken.BEGIN_ARRAY)){
                        reader.beginArray();
                        result.put(name, new JSONArray());

                        while (reader.hasNext()){
                            JSONObject subResult = JsonParser(reader);
                            result.accumulate(name, subResult);
                        }
                        reader.endArray();
                    } else {
                        result.put(name, "Something else");
                        reader.skipValue();
                    }
                }
                reader.endObject();
            } catch (Exception e){
                result = null;
                Log.d("JsonParser", e.getMessage());
            }

            Log.d("JsonParser", "finished parsing " + result.length());
            return result;
        }

        @Override
        protected void onCancelled(Result result){}
    }

}

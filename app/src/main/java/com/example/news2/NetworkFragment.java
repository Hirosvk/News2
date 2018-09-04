package com.example.news2;


import android.content.Context;
import android.graphics.BitmapFactory;
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
    private JSONArray requestQueue = new JSONArray();

    interface DownloadType {
        int JSON = 1;
        int IMAGE = 2;

    }

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
        startDownload(stringUrl, DownloadType.JSON);
    }

    public void startDownload(String stringUrl, int downloadType){
        startDownload(stringUrl, downloadType, null);
    }

    public void startDownload(String stringUrl, int downloadType, JSONObject extra){
        Log.d("DLX", "startDownload || " + stringUrl + " || " + String.valueOf(downloadType));
        if (downloadTask != null &&
            (downloadTask.getStatus().equals(AsyncTask.Status.RUNNING) ||
             downloadTask.getStatus().equals(AsyncTask.Status.PENDING))){
            Log.d("DLX", "startDownload || adding to queue");
            addToQueue(stringUrl, downloadType, extra);
        } else {
            Log.d("DLX", "startDownload || start download");
            startDownloadTask(stringUrl, downloadType, extra);
        }
    }

    public void startDownloadTask(String stringUrl, int downloadType, JSONObject extra){
        Log.d("DLX", "startDownloadTask");
        cancelDownload();
        downloadTask = new DownloadTask();
        downloadTask.setParentContext(parentContext);
        downloadTask.setNetworkFragment(this);
        downloadTask.setDownloadType(downloadType);
        if (extra != null){
            downloadTask.setExtra(extra);
        }
        downloadTask.execute(stringUrl);
    }

    public void onProgressUpdate(int status){
        Log.d("DLX", "NF.onProgressUpdate || status: " + String.valueOf(status));
        if (status == DownloadCallback.Progress.FINISHED && requestQueue.length() > 0){
            JSONObject task = (JSONObject) requestQueue.remove(0);
            try {
                Log.d("DLX", "NF.onProgressUpdate || more to download...");
                startDownloadTask(task.getString("url"), task.getInt("downloadType"), (JSONObject)task.get("extra"));
            } catch (JSONException e){
                Log.d("NF.onProgressUpdate", e.getMessage());
            }
        }
    }

    public void addToQueue(String url, int downloadType, JSONObject extra){
        JSONObject request = new JSONObject();

        try {
            request.put("url", url);
            request.put("downloadType", downloadType);
            request.put("extra", extra);
        } catch (JSONException e){
            Log.d("addToQueue", e.getMessage());
        }

        if (request.has("url") && request.has("downloadType")) {
            requestQueue.put(request);
        }
    }

    // Generic types AsyncTask<params, progress, result>
    private class DownloadTask extends AsyncTask<String, Integer, DownloadTask.Result>{
        private DownloadCallback parentContext;
        private NetworkFragment networkFragment;
        private int downloadType;
        private JSONObject extra = null;

        public void setDownloadType(int _downloadType){
            downloadType = _downloadType;
        }

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

        public void setNetworkFragment(NetworkFragment fragment){
            networkFragment = fragment;
        }

        public void setExtra(JSONObject _extra){
            if (_extra != null && _extra.length() > 0) {
                extra = _extra;
            }
        }

        @Override
        protected void onPreExecute(){
            if (parentContext != null){
                NetworkInfo networkInfo = parentContext.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                    (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                            && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    updateFromDownloads(null);
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
                    updateFromDownloads(errorResult);
                } else if (result.getResultValue() != null){
                    updateFromDownloads(result.getResultValue());
                } else {
                    Log.d("HK:onPostExecute", "result is something else");
                }
                parentContext.finishedDownloading();
            } else {
                Log.d("HK:onPostExecute", "something is null");
                cancel(true);
            }
        }

        private void updateFromDownloads(JSONObject result){
            Log.d("DLX", "finished task, calling updateFromDownloads");
            parentContext.updateFromDownloads(result);
            publishProgress(DownloadCallback.Progress.FINISHED);
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

                if (responseCode != HttpsURLConnection.HTTP_OK){
                    throw new IOException("HTTP code: " + responseCode);
                }

                stream = connection.getInputStream();
                publishProgress(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS);

                if (stream != null){
                    if (downloadType == DownloadType.JSON) {
                        JsonReader reader = new JsonReader(new InputStreamReader(stream));
                        result = JsonParser(reader);

                    } else if (downloadType == DownloadType.IMAGE){
                        result = new JSONObject();
                        try {
                            result.put("downloadedImage", BitmapFactory.decodeStream(stream));

                            if (extra != null){
                                JSONArray names = extra.names();
                                for(int i = 0; i < names.length(); i++){
                                    String name = names.getString(i);
                                    result.put(name, extra.get(name));
                                }
                            }

                        } catch (JSONException e){
                            Log.d("downloadUrl", e.getMessage());
                        }
                    }
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
            networkFragment.onProgressUpdate(status);
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
                        result.put(name, "(null)");
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
        protected void onCancelled(Result result){
            publishProgress(DownloadCallback.Progress.FINISHED);
        }
    }

}

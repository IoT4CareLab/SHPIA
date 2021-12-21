package com.raffaello.nordic.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Meta;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.raffaello.nordic.model.NordicApi;
import com.raffaello.nordic.model.NordicApiService;
import com.raffaello.nordic.model.NordicDeviceDataList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkChangeReceiver extends BroadcastReceiver {

    // App
    private NordicApiService nordicApiService = NordicApiService.getInstance();

    // Async Task
    private AsyncTask<Void, Void, List<NordicDeviceDataList>> retrieveTask;
    private AsyncTask<List<NordicDeviceDataList>, Void, Void> deleteTask;

    // Class
    private String token;


    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                Log.i("messaggio", "Connection lost");
            } else {
                Log.i("messaggio", "Device is online");

                if(retrieveTask != null){
                    retrieveTask.cancel(true);
                    retrieveTask = null;
                }

                token = SharedPreferencesHelper.getInstance(context).getAuthToken();

                File rootFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                File[] files = rootFolder.listFiles();
                List<String> datalist = new ArrayList<>();
                try {
                    for(File file : files){
                        String content = this.readTextFile(file);
                        datalist.add(content);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(files.length > 0){
                    executor.execute(new NetworkChangeReceiver.SubmitRunnable(datalist, files));
                }

            }
        }
    }

    // Submit worker thread
    private Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private class SubmitRunnable implements Runnable {

        private List<String> dataList;
        private File[] files;
        private NordicApi api = nordicApiService.getApi();
        private String header;

        public SubmitRunnable(List<String> dataList, File[] files) {
            this.files = files;
            this.dataList = dataList;
            this.header = "Token " + token;
        }

        @Override
        public void run() {

            Log.i("messaggio", "Pushing cached data data to the server ...");
            Call<Void> call = api.syncData(header, dataList);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        Log.i("messaggio", "Error while saving data");
                    } else {
                        Log.i("messaggio", "Cached data saved");
                        deleteTextFiles(files);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.i("messaggio", "Server temporary unavailable. Caching data ...");
                }
            });
        }
    }

    private String readTextFile(File file) throws IOException {

        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();

        String line = bufferedReader.readLine();
        while (line != null) {
            stringBuilder.append(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();

        return stringBuilder.toString();
    }

    private void deleteTextFiles(File[] files) {
        for(File file : files){
            file.delete();
        }
    }

}
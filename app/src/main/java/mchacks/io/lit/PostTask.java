package mchacks.io.lit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;

public class PostTask extends AsyncTask<Void, Void, String> {

    public static final String TAG = "PostTask";

    public String url;
    public Map<String, String> params = new LinkedHashMap<>();
    public Context context;
    public OkHttpClient client;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;

    public PostTask(String url, Context context) {
        basicSetup(url, context);
    }

    public PostTask(String url, Context context, Map<String, String> params) {
        this.params = params;
        basicSetup(url, context);
    }

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "Request to " + url);
        Log.d(TAG, "Params: " + params.toString());
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.d(TAG, "Got response from " + url);
        Log.d(TAG, "Response: " + (s == null ? "null" : s));
    }

    private void basicSetup(String url, final Context context) {
        this.url = url;
        params.put("secret", Key.SECRET);
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        client = new OkHttpClient.Builder()
                .sslSocketFactory(getPinnedCertSslSocketFactory(context))
                .build();
    }

    private SSLSocketFactory getPinnedCertSslSocketFactory(Context context) {
        try {
            KeyStore trusted = KeyStore.getInstance("BKS");
            InputStream in = context.getResources().openRawResource(R.raw.my_keystore);
            trusted.load(in, Key.SECRET.toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trusted);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            Log.e("MyApp", e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected String doInBackground(Void... params2) {
        try {
            return S.post(url, params, client);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}

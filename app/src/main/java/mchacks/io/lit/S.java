package mchacks.io.lit;

import android.os.Build;

import java.io.IOException;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class S {
    public static final String SITE_ROOT = "https://gator4227.hostgator.com/~baus/";
    public static final String ENDPOINT = "https://gator4227.hostgator.com";

    /**
     * Used to post data to the specified url
     *
     * @param url    the URL to post to
     * @param params the POST parameters
     * @return the response from the URL
     * @throws java.io.IOException
     */
    public static String post(String url, Map<String, String> params, OkHttpClient client) throws IOException {
        RequestBody body = initializeRequest(params);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /**
     * Helper to post
     *
     * @param params
     * @return
     */
    private static RequestBody initializeRequest(Map<String, String> params) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry entry : params.entrySet()) {
            builder.add((String) entry.getKey(), (String) entry.getValue());
        }
        return builder.build();
    }
}

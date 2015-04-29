package test.vc.workhub;

import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HaoZhe Chen on 2015/4/13.
 * Do the network to send request and fetch data.
 */
public class NetHelper {
    final static String PATH = "http://121.43.235.226:82";
    boolean loginValidate;

    /**
     * Perform net request by http post.
     * @param path   sub path of the request.
     * @param keys   the keys in the entity
     * @param values the values in the entity, need to be match the keys in order.
     * @return the result of the request as String.
     * @throws Exception
     */
    public static String requestPost(String path, String[] keys, String[] values) throws Exception {
        HttpPost httpPost = new HttpPost(PATH + path);
        List<NameValuePair> params = new ArrayList<>();
        if (keys != null) {
            if (keys.length < values.length) return null;
            for (int i = 0; i < keys.length; i++)
                params.add(new BasicNameValuePair(keys[i], values[i]));
        }
        HttpEntity httpEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
        httpPost.setEntity(httpEntity);
        httpPost.setHeader("Cookie", MyData.getCookie());
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        HttpResponse httpResponse = httpClient.execute(httpPost);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            String rstsrt = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            Header header = httpResponse.getFirstHeader("Set-Cookie");
            if (header != null) {
                MyData.setCookie(header.getValue());
            }
            return rstsrt;
        } else {
            return null;
        }
    }

    /**
     * perform http request by http get.
     *
     * @param path the path of the request
     * @return the result of the request as string.
     * @throws Exception
     */
    public static String requestGet(String path) throws Exception {
        HttpGet httpGet = new HttpGet(PATH + path);
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            String rstsrc = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            Header header = httpResponse.getFirstHeader("Set-Cookie");
            if (header != null) {
                MyData.setCookie(header.getValue());
            }
            return rstsrc;
        } else {
            return null;
        }
    }

    /**
     * Check the net connection status of the device.
     *
     * @param context The context of the program.
     * @return whether the connection is good.
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mConnectivityManager != null) {
                NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable();
                }
            }
        }
        return false;
    }

}

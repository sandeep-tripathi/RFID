package com.example.uhf.RFIDcache;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.uhf.tools.UIHelper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RfidInfoContainer {
    private static Map<String, RfidInfo> RfidInfoMap = new HashMap<String, RfidInfo>();

    // Making HTTP request to the server
    public static AsyncHttpClient client = new AsyncHttpClient();

    public static RequestQueue queue;

    public static Set<String> rfidTagsSubmittedForResponse = new HashSet<String>();

    public static synchronized void insertRfidInfo(String tagID, Context ctx) throws UnsupportedEncodingException {

        RfidInfo rfidInfo = RfidInfoContainer.RfidInfoMap.get(tagID);
        if(rfidInfo == null && !rfidTagsSubmittedForResponse.contains(tagID)) {
            updateRfidInfoCache(tagID, ctx);
            rfidTagsSubmittedForResponse.add(tagID);
        }
    }

    public static RfidInfo findRfidInfo(String tagID) {
        return RfidInfoMap.get(tagID);
    }

    public static List<RfidInfo> sortedRfidList() {
        List<RfidInfo> infos = new ArrayList<RfidInfo>();
        infos.addAll(RfidInfoMap.values());

        Collections.sort(infos, new Comparator<RfidInfo>() {
            @Override
            public int compare(RfidInfo lhs, RfidInfo rhs) {
                return lhs.getNextInspectionDate().compareTo(rhs.getNextInspectionDate());
            }
        });

        return infos;
    }

    public static synchronized void updateRfidInfoCache(final String tagID, Context ctx) throws UnsupportedEncodingException {
        // Making HTTP request to the server
        final String requestBody = "{\"tagids\":[\"" + tagID + "\"]}";

        if(queue == null)
            queue = Volley.newRequestQueue(ctx); // One time initialization

        StringRequest req = new StringRequest(Request.Method.POST, "http://46.101.232.21:1080/api/app/get", new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                System.out.println("Success Testinggg: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error Testinggg: " + error.toString());
            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    System.out.println("dataaa: " + new String(response.data));

                    try {
                        JSONObject responseJSON = new JSONObject(new String(response.data));
                        JSONObject data = responseJSON.getJSONArray("data").getJSONObject(0);
                        RfidInfoMap.put(data.getString("tagid"), new RfidInfo(data));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        queue.add(req);

    }

    public static void submitDataToBackend(String json, final Context ctx) {
        // Making HTTP request to the server
        final String requestBody = json;

        if(queue == null)
            queue = Volley.newRequestQueue(ctx); // One time initialization

        StringRequest req = new StringRequest(Request.Method.POST, "http://46.101.232.21:1080/api/app/post", new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                UIHelper.ToastMessage(ctx, "Successfully saved!");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                UIHelper.ToastMessage(ctx, "Save operation failed!");
            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                String responseString = "";
//                if (response != null) {
//                    responseString = String.valueOf(response.statusCode);
//                    System.out.println("dataaa: " + new String(response.data));
//
//                    try {
//                        JSONObject responseJSON = new JSONObject(new String(response.data));
//                        JSONObject data = responseJSON.getJSONArray("data").getJSONObject(0);
//                        RfidInfoMap.put(data.getString("tagid"), new RfidInfo(data));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                    // can get more details such as response.headers
//                }
                return null;//Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        queue.add(req);
    }

    public static void flushRfidInfo() {
        RfidInfoMap.clear();
        rfidTagsSubmittedForResponse.clear();
    }
}
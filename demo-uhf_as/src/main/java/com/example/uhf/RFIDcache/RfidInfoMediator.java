package com.example.uhf.RFIDcache;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.uhf.tools.UIHelper;
import com.lidroid.xutils.http.client.multipart.HttpMultipartMode;
import com.lidroid.xutils.http.client.multipart.MultipartEntity;
import com.lidroid.xutils.http.client.multipart.content.FileBody;
import com.loopj.android.http.AsyncHttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RfidInfoMediator {
    private static Map<String, RfidInfo> RfidInfoMap = new HashMap<String, RfidInfo>();

    // Making HTTP request to the server
    public static AsyncHttpClient client = new AsyncHttpClient();

    public static RequestQueue queue;

    public static Set<String> rfidTagsSubmittedForResponse = new HashSet<String>();

    public static synchronized void insertRfidInfo(String tagID, Context ctx) throws UnsupportedEncodingException {

        RfidInfo rfidInfo = RfidInfoMediator.RfidInfoMap.get(tagID);
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
        });///111133B2DDD9014000000000

        return infos;
    }

    public static synchronized void updateRfidInfoCache(final String tagID, Context ctx) throws UnsupportedEncodingException {
        // Making HTTP request to the server
        final String requestBody = "{\"tagids\":[\"" + tagID + "\"]}";

        if(queue == null)
            queue = Volley.newRequestQueue(ctx); // One time initialization

        StringRequest req = new StringRequest(Request.Method.POST, "http://141.44.18.16:1080/api/app/gettagdata", new Response.Listener() {
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

                    try {
                        JSONObject responseJSON = new JSONObject(new String(response.data));
                        JSONObject data = responseJSON.getJSONArray("data").getJSONObject(0);
                        RfidInfo rfidInfoData = new RfidInfo(data);
                        RfidInfoMap.put(data.getString("tagid"), rfidInfoData);
                        if(!rfidInfoData.getFile_name().equals("NA") && !rfidInfoData.getFile_path().equals("NA")) {
                            // Initialize a new ImageRequest
                            String getImageURLstring = "http://141.44.18.16:1080/api/app/images" + rfidInfoData.getFile_path() + rfidInfoData.getFile_name();
                            ImageRequest imageRequest = new ImageRequest(
                                    getImageURLstring, // Image URL
                                    new Response.Listener<Bitmap>() { // Bitmap listener
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            // Do something with response
                                            RfidInfoMap.get(tagID).setPreviousImage(response);
                                        }
                                    },
                                    0, // Image width
                                    0, // Image height
                                    ImageView.ScaleType.CENTER_CROP, // Image scale type
                                    Bitmap.Config.RGB_565, //Image decode configuration
                                    new Response.ErrorListener() { // Error listener
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            // Do something with error response
                                            error.printStackTrace();
                                        }
                                    }
                            );

                            // Add ImageRequest to the RequestQueue
                            queue.add(imageRequest);
                        }
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

    public static void submitDataToBackend(String json, String tagID, boolean submitImage, final String imagePath, final Context ctx) {
        // Making HTTP request to the server
        final String requestBody = json;

        if (queue == null)
            queue = Volley.newRequestQueue(ctx); // One time initialization

        StringRequest req = new StringRequest(Request.Method.POST, "http://141.44.18.16:1080/api/app/updatetagdata", new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                UIHelper.ToastMessage(ctx, "Successfully saved!");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                UIHelper.ToastMessage(ctx, "Save operation failed!");
            }
        }) {
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
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        queue.add(req);


        if (submitImage) {
            String imageUploadURLstring = "http://141.44.18.16:1080/api/imageupload/" + tagID;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(imageUploadURLstring);

            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            if (imagePath != null) {
                File file = new File(imagePath);
                mpEntity.addPart("currentImage.jpg", new FileBody(file, ""));
            }

            httppost.setEntity(mpEntity);
            try {
                HttpResponse response = httpclient.execute(httppost);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void flushRfidInfo() {
        RfidInfoMap.clear();
        rfidTagsSubmittedForResponse.clear();
    }


    /**
     *
     * @param context
     * @throws UnsupportedEncodingException
     */
    public static void updateEntriesFromBackend(final Context context) throws UnsupportedEncodingException {

        flushRfidInfo();

        for(String key : RfidInfoMediator.RfidInfoMap.keySet()) {
            updateRfidInfoCache(key, context);
        }
    }


}
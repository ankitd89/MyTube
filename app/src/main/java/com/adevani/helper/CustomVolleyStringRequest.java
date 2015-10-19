package com.adevani.helper;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ankitdevani on 10/17/15.
 */
public class CustomVolleyStringRequest extends StringRequest {

    public CustomVolleyStringRequest(int method, String url,
                                     Response.Listener<String> listener,
                                     Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return super.getRetryPolicy();
    }
}


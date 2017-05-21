package com.moviepit.utilities;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public class RequestCallback {
    public interface APIRequestCallback {
        void onSuccessJSONResponse(JSONObject response);
        void onErrorResponse(VolleyError error);
    }

}

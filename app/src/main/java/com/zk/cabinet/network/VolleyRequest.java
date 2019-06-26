package com.zk.cabinet.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public abstract class VolleyRequest {

    private RequestQueue requestQueue;


    public void init (Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void add(JsonObjectRequest jsonObjectRequest){
        requestQueue.add(jsonObjectRequest);
    }

    public void add(StringRequest stringRequest){
        requestQueue.add(stringRequest);
    }
}

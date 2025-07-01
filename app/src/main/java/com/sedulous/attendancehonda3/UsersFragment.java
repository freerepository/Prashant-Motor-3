package com.sedulous.attendancehonda3;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sedulous.attendancehonda3.Models.UserDataModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import attendancehonda.R;

public class UsersFragment extends Fragment {

    String DOWNLOAD_USERDATA_API="http://attendance.prashanthospital.in/api/get_alluser";
    String DOWNLOAD_FINGERDATA_API="http://attendance.prashanthospital.in/api/get_alluserfingers";
    Activity activity;
    RecyclerView rv;
    UserAdapter adapter;
    ArrayList<UserDataModel> user_list=new ArrayList<>();
    MyDataBase myDataBase;
    EditText etSearch;
    String from="";
    ProgressDialog mProgressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        activity = getActivity();
        from=getArguments().getString(O.FROM);
        rv=view.findViewById(R.id.rv);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        myDataBase=new MyDataBase(activity);
        adapter = new UserAdapter(activity, user_list );
        rv.setAdapter(adapter);
        rv.setHasFixedSize(false);
        rv.setNestedScrollingEnabled(true);
        user_list=myDataBase.getAllUsersObj();
        adapter.datalist=user_list;
        adapter.notifyDataSetChanged();
        etSearch=view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (user_list.size() > 0) {
                    ArrayList<UserDataModel> mList=new ArrayList();
                    for(int n=0; n<user_list.size(); n++){
                        if(user_list.get(n).user_id.toUpperCase().contains(s.toString().toUpperCase())){
                            mList.add(user_list.get(n));
                        }
                    }
                    adapter.datalist = mList;
                    adapter.notifyDataSetChanged();
                }
            }@Override
            public void afterTextChanged(Editable s) { }
        });
        view.findViewById(R.id.iv_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadUserData();

            }
        });
        return view;
    }
    private void downloadUserData() {

        final JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("depot_code", MainActivity.station_code);
            jsonObject.put("work_type", MainActivity.app_work_type);
        }catch (Exception e){
            e.printStackTrace();
        }
        final String requestBody=jsonObject.toString();
        Log.e("reqbody",requestBody);
        showLoading("downloading user data...");
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST,
                DOWNLOAD_USERDATA_API, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoading();
                Log.e("alluserresponse",response);
                try {
                    JSONObject resObj=new JSONObject(response);
                    JSONArray jarray=resObj.getJSONArray("Getusers");
                    for(int a=0; a<jarray.length(); a++){
                        JSONObject obj=jarray.getJSONObject(a);
                        try {
                            myDataBase.insertToUser(
                            obj.getString("EMPM_userID"),
                            obj.getString("depot_code"),
                            obj.getString("EMPM_name"),
                            obj.getString("EMPM_type"),
                            obj.getString("EMPM_skill_type"),
                            obj.getString("EMPM_login_method"),
                            obj.getString("EMPM_password"),
                            obj.getString("EMPM_phone_no"),
                            obj.getString("EMPM_doj"),
                            obj.getString("police_verification"),
                            obj.getString("EMPM_idcard"), O.YES);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        user_list=myDataBase.getAllUsersObj();
                        adapter.datalist=user_list;
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                try{
                    downloadFingerData();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(com.android.volley.VolleyError error) {
                hideLoading();
                try{
                    downloadFingerData();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() throws com.android.volley.AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        requestQueue.add(stringRequest);
    }
    private void downloadFingerData() {

        final JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("depot_code", MainActivity.station_code);
            jsonObject.put("work_type", MainActivity.app_work_type);
        }catch (Exception e){
            e.printStackTrace();
        }
        final String requestBody=jsonObject.toString();
        Log.e("reqbody",requestBody);
        showLoading("downloading user data...");
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST,
                DOWNLOAD_FINGERDATA_API, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoading();
                Log.e("response",response);
                //O.writeToText(activity, response, "fingers");
                try {
                    JSONObject resObj=new JSONObject(response);
                    JSONArray jarray=resObj.getJSONArray("Getuserfingers");
                    for(int a=0; a<jarray.length(); a++){
                        JSONObject obj=jarray.getJSONObject(a);
                        try {
                            String uid=obj.getString("user_ID");
                            String machine_id=obj.getString("machine_ID");
                            String finger_name=obj.getString("finger_name");
                            byte[] data = Base64.decode(obj.getString("finger_byte"), Base64.DEFAULT);
                            if(!myDataBase.isFingerExist(uid,finger_name)){
                                myDataBase.insertToFinger(uid, MainActivity.station_code, machine_id, O.YES,finger_name, data);
                            }
                        }catch (Exception e){
                            Log.e("error fingers", ""+e);
                        }
                        user_list=myDataBase.getAllUsersObj();
                        adapter.notifyDataSetChanged();
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(com.android.volley.VolleyError error) {
                hideLoading();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
            @Override
            public byte[] getBody() throws com.android.volley.AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    return null;
                }
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        requestQueue.add(stringRequest);
    }
    public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

        private ArrayList<UserDataModel> datalist;
        private LayoutInflater mInflater;

        public UserAdapter(Context context, ArrayList<UserDataModel> data) {
            this.mInflater = LayoutInflater.from(context);
            this.datalist = data;
        }
        @Override
        public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_users, parent, false);
            return new UserAdapter.ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(UserAdapter.ViewHolder h, final int position ) {
            h.setIsRecyclable(false);
            final UserDataModel userDataModel=datalist.get(position);
            h.tv1.setText((position+1)+".");
            h.tv2.setText(userDataModel.user_id);
            h.tv3.setText(userDataModel.user_name);
            h.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(from.equalsIgnoreCase(O.ADMIN)){
                        Fragment f=new UDetailFragment();
                        Bundle b=new Bundle();
                        b.putSerializable(O.DATA, userDataModel);
                        f.setArguments(b);
                        ((MainActivity)getActivity()).loadFragmentForBack(f);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return datalist.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView tv1, tv2, tv3;
            ViewHolder(View itemView) {
                super(itemView);
                tv1=itemView.findViewById(R.id.tv1);
                tv2=itemView.findViewById(R.id.tv2);
                tv3=itemView.findViewById(R.id.tv3);
            }
        }
    }

    protected void showLoading(@NonNull String message0) {
        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setMessage(message0);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }
    protected void hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }
}

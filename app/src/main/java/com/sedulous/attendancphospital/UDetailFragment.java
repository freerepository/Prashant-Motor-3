package com.sedulous.attendancphospital;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sedulous.attendancphospital.Models.UserDataModel;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class UDetailFragment extends Fragment {

    public static String DEL_API="http://attendance.prashanthospital.in/api/delUser";
    TextView tv_delete, tv_edit;
    RecyclerView rv;
    Adapter adapter;
    ArrayList<HashMap<String, String>> list=new ArrayList<>();
    Activity activity;
    UserDataModel userDataModel;
    MyDataBase myDataBase;
    ProgressDialog mProgressDialog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_udetail, container, false);
        activity = getActivity();
        myDataBase = new MyDataBase(activity);
        userDataModel=(UserDataModel) getArguments().getSerializable(O.DATA);

        Field[] fields = UserDataModel.class.getDeclaredFields();
        for (Field f:fields) {
            HashMap<String, String> hashMap=new HashMap<>();
            try {
                hashMap.put(f.getName(), f.get(userDataModel).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            list.add(hashMap);
        }
        tv_delete=view.findViewById(R.id.tv_delete);
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                builder1.setMessage("Delete this user?");
                builder1.setCancelable(false);
                builder1.setNegativeButton(
                    "Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder1.setPositiveButton("Delete user",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(myDataBase.deleteUser(userDataModel.user_id)>0) {
                                    myDataBase.deleteFinger(userDataModel.user_id);
                                    deleteUserF(userDataModel.user_id);
                                    Toast.makeText(activity, "User Deleted successfully.",
                                            Toast.LENGTH_SHORT).show();

                                }
                                dialog.cancel();
                                try {
                                    ((MainActivity)getActivity()).clearFragment(2);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();

            }
        });
        tv_edit=view.findViewById(R.id.tv_edit);
        tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment f=new RegisterFragment();
                Bundle b=new Bundle();
                b.putString(O.TASK, O.EDIT);
                b.putSerializable(O.DATA,userDataModel);
                f.setArguments(b);
                ((MainActivity)getActivity()).clearFragment(2);
                ((MainActivity)getActivity()).loadFragmentForBack(f);
            }
        });

        rv=view.findViewById(R.id.rv);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        adapter = new Adapter(activity, list);
        rv.setAdapter(adapter);
        rv.setHasFixedSize(false);
        rv.setNestedScrollingEnabled(true);
        adapter.notifyDataSetChanged();

        return  view;
    }
    public void deleteUserF(final String user_id){

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", user_id);
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.e("request_login",jsonObject.toString());
        showLoading("Please wait...");
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, DEL_API,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideLoading();
                Log.e("response_login", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error",""+error);
                hideLoading();
            }
        });
        VolleySingleton.getInstance(activity).addToRequestQueue(stringRequest);
    }
    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private ArrayList<HashMap<String, String>> datalist;
        private LayoutInflater mInflater;

        public Adapter(Context context, ArrayList<HashMap<String, String>> data) {
            this.mInflater = LayoutInflater.from(context);
            this.datalist = data;
        }
        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_aboutset, parent, false);
            return new Adapter.ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(Adapter.ViewHolder h, final int position ) {
            h.setIsRecyclable(false);
            HashMap<String, String> hashMap=datalist.get(position);
            for (String key:hashMap.keySet()) {
                h.tv_key.setText(modifyKey(key));
                h.tv_value.setText(hashMap.get(key));
            }

        }
        @Override
        public int getItemCount() {
            return datalist.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView tv_key, tv_value;
            ViewHolder(View itemView) {
                super(itemView);
                tv_key=itemView.findViewById(R.id.tv_key);
                tv_value=itemView.findViewById(R.id.tv_value);
            }
        }
    }
    public static String modifyKey(String str) {
        if(str == null || str.isEmpty()) {
            return str;
        }
        String result= str.substring(0, 1).toUpperCase() + str.substring(1);
        if(result.contains("_"))result=result.replaceAll("_"," ");
        return result;
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

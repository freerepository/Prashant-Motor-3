package com.sedulous.attendancphospital;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.tabs.TabLayout;
import com.mantra.mfs100.BuildConfig;
import com.sedulous.attendancphospital.Models.BaseModel;

import org.json.JSONObject;

import java.util.ArrayList;

public class SettingFragment extends Fragment {

    public static String SAVE_DEVICE_ID_API="http://attendance.prashanthospital.in/api/save_device";
    public static String ACTIVATE_API="http://attendance.prashanthospital.in/api/get_devicestatus";
    public static String API_SEND_SETTING="http://attendance.prashanthospital.in/api/save_devicesetting";

    TabLayout tabLayout;
    View vControl;
    RecyclerView rv_about;
    SAdapter adapter;
    Activity activity;
    TextView tv_activation;
    Button bt_device_id, bt_activation;
    EditText et_device_id, et_activation;
    ArrayList<BaseModel> list=new ArrayList<>();
    Spinner sp_min_quality, sp_min_match, sp_captimeout, sp_minWorkHour, sp_minWorkMinute,
            sp_minExpHour, sp_minExpMinute;
    ArrayList<Integer> quality_list=new ArrayList<>();
    ArrayList<Integer> match_list=new ArrayList<>();
    ArrayList<Integer> captimeout_list=new ArrayList<>();
    ArrayList<Integer> minWorkHour_list=new ArrayList<>();
    ArrayList<Integer> minWorkMinute_list=new ArrayList<>();
    ArrayList<Integer> minExpHour_list=new ArrayList<>();
    ArrayList<Integer> minExpMinute_list=new ArrayList<>();
    ArrayAdapter<Integer> adapter_quality, adapter_match, adapter_captimeout, adapter_minWorkHour,
            adapter_minWorkMinute, adapter_minExpHour, adapter_minExpMinute;
    ProgressDialog mProgressDialog;
    MyDataBase mDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        activity=getActivity();
        vControl=view.findViewById(R.id.sv_control);
        rv_about=view.findViewById(R.id.rv_about);
        tabLayout=view.findViewById(R.id.tb_setting);
        bt_device_id=view.findViewById(R.id.bt_device_id);
        et_device_id=view.findViewById(R.id.et_device_id);
        bt_activation=view.findViewById(R.id.bt_activation);
        et_activation=view.findViewById(R.id.et_activation);
        tv_activation=view.findViewById(R.id.tv_activation);
        mDB=new MyDataBase(activity);
        if(!TextUtils.isEmpty(O.getPreference(activity,O.ACTIVATION_KEY))){
            tv_activation.setText("Device\nActivated");
            tv_activation.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tv_activation.setText("Not\nActivated");
            tv_activation.setTextColor(getResources().getColor(android.R.color.black));
        }
        //worktype-stationname-number
        et_device_id.setText(((MainActivity)getActivity()).getDeviceId());
        TabLayout.Tab tab1=tabLayout.newTab();
        tab1.setText("Control");
        tabLayout.addTab(tab1);
        TabLayout.Tab tab2=tabLayout.newTab();
        tab2.setText("About");
        tabLayout.addTab(tab2);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setSelect(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        rv_about.setLayoutManager(layoutManager);
        adapter = new SAdapter(activity, list );
        rv_about.setAdapter(adapter);
        rv_about.setHasFixedSize(false);
        rv_about.setNestedScrollingEnabled(true);
        list.clear();
        list.add(new HeadItem("Scanner Info"));
        if(MainActivity.isConnected) {
            list.add(new AboutItem("Serial No.", MainActivity.mfs100.GetDeviceInfo().SerialNo()));
            list.add(new AboutItem("Make", MainActivity.mfs100.GetDeviceInfo().Make()));
            list.add(new AboutItem("Model", MainActivity.mfs100.GetDeviceInfo().Model()));
            list.add(new AboutItem("Certificate", MainActivity.mfs100.GetCertification()));

        }else{
            list.add(new AboutItem("Scanner Status", "Not connected"));
        }
        list.add(new HeadItem("Tab/Mobile Info"));
        list.add(new AboutItem("Device", Build.DEVICE));
        list.add(new AboutItem("Model", Build.MODEL));
        list.add(new AboutItem("Product", Build.PRODUCT));
        list.add(new AboutItem("Android", android.os.Build.VERSION.SDK));

        list.add(new HeadItem("App info"));
        list.add(new AboutItem("Made", "Sedulous"));
        list.add(new AboutItem("App version", BuildConfig.VERSION_NAME));

        adapter.notifyDataSetChanged();

        quality_list.add(30);
        quality_list.add(40);
        quality_list.add(50);
        quality_list.add(60);
        quality_list.add(70);
        quality_list.add(80);
        adapter_quality = new ArrayAdapter<Integer>(activity, R.layout.text_v, quality_list);
        adapter_quality.setDropDownViewResource(R.layout.text_v);
        sp_min_quality=view.findViewById(R.id.sp_min_quality);
        sp_min_quality.setAdapter(adapter_quality);
        Log.e("akm value",""+O.getPreferenceInt(activity, O.QUALITY,40));
        Log.e("akm index",""+quality_list.indexOf(O.getPreferenceInt(activity, O.QUALITY,40)));
        sp_min_quality.setSelection(quality_list.indexOf(O.getPreferenceInt(activity, O.QUALITY,40)), true);
        sp_min_quality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                O.savePreferenceInt(activity, O.QUALITY,quality_list.get(i));
                sendSettings();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        match_list.add(20);
        match_list.add(50);
        match_list.add(100);
        match_list.add(200);
        match_list.add(300);
        adapter_match = new ArrayAdapter<Integer>(activity, R.layout.text_v, match_list);
        adapter_match.setDropDownViewResource(R.layout.text_v);
        sp_min_match=view.findViewById(R.id.sp_min_match);
        sp_min_match.setAdapter(adapter_match);
        sp_min_match.setSelection(match_list.indexOf(O.getPreferenceInt(activity, O.MATCH,100)), true);
        sp_min_match.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                O.savePreferenceInt(activity, O.MATCH,match_list.get(i));
                sendSettings();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        captimeout_list.add(4000);
        captimeout_list.add(6000);
        captimeout_list.add(8000);
        captimeout_list.add(10000);
        captimeout_list.add(15000);
        captimeout_list.add(20000);
        captimeout_list.add(30000);
        adapter_captimeout = new ArrayAdapter<Integer>(activity, R.layout.text_v, captimeout_list);
        adapter_captimeout.setDropDownViewResource(R.layout.text_v);
        sp_captimeout=view.findViewById(R.id.sp_capture_timeout);
        sp_captimeout.setAdapter(adapter_captimeout);
        sp_captimeout.setSelection(captimeout_list.indexOf(O.getPreferenceInt(activity, O.TIMEOUT,10000)), true);
        sp_captimeout.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                O.savePreferenceInt(activity, O.TIMEOUT, captimeout_list.get(i));
                sendSettings();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        for(int a=0; a<=12; a++){
            minWorkHour_list.add(a);
        }
        adapter_minWorkHour = new ArrayAdapter<Integer>(activity, R.layout.text_v, minWorkHour_list);
        adapter_minWorkHour.setDropDownViewResource(R.layout.text_v);
        sp_minWorkHour=view.findViewById(R.id.sp_minhour);
        sp_minWorkHour.setAdapter(adapter_minWorkHour);
        sp_minWorkHour.setSelection(minWorkHour_list.indexOf(O.getPreferenceInt(activity, O.MINWORKHOUR,0)), true);
        sp_minWorkHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                O.savePreferenceInt(activity, O.MINWORKHOUR,minWorkHour_list.get(i));
                sendSettings();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        for(int a=0; a<=59; a++){
            minWorkMinute_list.add(a);
        }
        adapter_minWorkMinute = new ArrayAdapter<Integer>(activity, R.layout.text_v, minWorkMinute_list);
        adapter_minWorkMinute.setDropDownViewResource(R.layout.text_v);
        sp_minWorkMinute=view.findViewById(R.id.sp_minminute);
        sp_minWorkMinute.setAdapter(adapter_minWorkMinute);
        sp_minWorkMinute.setSelection(minWorkMinute_list.indexOf(O.getPreferenceInt(activity, O.MINWORKMINUTE,0)), true);
        sp_minWorkMinute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                O.savePreferenceInt(activity, O.MINWORKMINUTE,minWorkMinute_list.get(i));
                sendSettings();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        for(int a=0; a<=24; a++){
            minExpHour_list.add(a);
        }
        adapter_minExpHour = new ArrayAdapter<Integer>(activity, R.layout.text_v, minExpHour_list);
        adapter_minExpHour.setDropDownViewResource(R.layout.text_v);
        sp_minExpHour=view.findViewById(R.id.sp_exphour);
        sp_minExpHour.setAdapter(adapter_minExpHour);
        sp_minExpHour.setSelection(minExpHour_list.indexOf(O.getPreferenceInt(activity, O.MINEXPHOUR,0)), true);
        sp_minExpHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                O.savePreferenceInt(activity, O.MINEXPHOUR,minExpHour_list.get(i));
                sendSettings();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        for(int a=0; a<=59; a++){
            minExpMinute_list.add(a);
        }
        adapter_minExpMinute = new ArrayAdapter<Integer>(activity, R.layout.text_v, minExpMinute_list);
        adapter_minExpMinute.setDropDownViewResource(R.layout.text_v);

        sp_minExpMinute=view.findViewById(R.id.sp_expminute);
        sp_minExpMinute.setAdapter(adapter_minExpMinute);
        sp_minExpMinute.setSelection(minExpMinute_list.indexOf(O.getPreferenceInt(activity, O.MINEXPMINUTE,0)), true);
        sp_minExpMinute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                O.savePreferenceInt(activity, O.MINEXPMINUTE,minExpMinute_list.get(i));
                sendSettings();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        bt_device_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(et_device_id.getText().toString())){
                    String deviceid=et_device_id.getText().toString();
                    if(deviceid.length()>8 && deviceid.contains("-")) {
                        MainActivity.app_work_type = deviceid.substring(0,deviceid.indexOf("-"));
                        MainActivity.station_code = deviceid.substring(deviceid.indexOf("-")+1, deviceid.lastIndexOf("-"));
                        O.savePreference(activity,O.APP_WORK_TYPE, MainActivity.app_work_type);
                        ((MainActivity)getActivity()).tv_name.setText(MainActivity.app_work_type+" "+MainActivity.station_code);
                        O.savePreference(activity,O.STATION_CODE, MainActivity.station_code);
                        sendDeviceId(deviceid);
                    }
                }
            }
        });
        bt_activation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(et_activation.getText().toString())){
                    String actid=et_activation.getText().toString();
                    matchActivation(actid);
                }
            }
        });

        return view;
    }

    public void sendSettings(){
        if(!TextUtils.isEmpty(((MainActivity)activity).getDeviceId())){
        final JSONObject jsonObject = new JSONObject();
        int capture_timeout=O.getPreferenceInt(activity, O.TIMEOUT,10000);
        int min_reg_quality=O.getPreferenceInt(activity, O.QUALITY,40);
        int min_match_for_attandance=O.getPreferenceInt(activity, O.MATCH,100);
        int min_work_hour=O.getPreferenceInt(activity, O.MINWORKHOUR,0);
        int min_work_minute=O.getPreferenceInt(activity, O.MINWORKMINUTE,0);
        int min_exp_hour=O.getPreferenceInt(activity, O.MINEXPHOUR,0);
        int min_exp_minute=O.getPreferenceInt(activity, O.MINEXPMINUTE,0);
        try {
            jsonObject.put("device_id", ((MainActivity)activity).getDeviceId() );
            jsonObject.put("depot_code", MainActivity.station_code);
            jsonObject.put("work_type", MainActivity.app_work_type);
            jsonObject.put("finger_quality",min_reg_quality);
            jsonObject.put("match_percent",min_match_for_attandance);
            jsonObject.put("capture_timeout",capture_timeout);
            jsonObject.put("min_work_hm",min_work_hour+":"+min_work_minute);
            jsonObject.put("expiry_time",min_exp_hour+":"+min_exp_minute);

        }catch (Exception e){
            e.printStackTrace();
        }

        Log.e("request_login",jsonObject.toString());

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, API_SEND_SETTING,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Log.e("response_setting", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("error",""+error);
                Toast.makeText(activity, "Error...",
                        Toast.LENGTH_SHORT).show();
            }
        });
        VolleySingleton.getInstance(activity).addToRequestQueue(stringRequest);
    }}

    public void matchActivation(final String activationkey){

        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("device_id", ((MainActivity)activity).getDeviceId() );
            jsonObject.put("depot_code", MainActivity.station_code);
            jsonObject.put("passcode", activationkey);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.e("request_login",jsonObject.toString());
        showLoading("Please wait...");
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, ACTIVATE_API,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideLoading();
                Log.e("response_login", response.toString());
                try {
                    if(response.getInt("success")==1){
                        O.savePreference(activity, O.ACTIVATION_KEY, "1");
                        mDB.deleteAttandanceRecords();
                        mDB.deleteFingerPrintRecords();
                        mDB.deleteUserRecords();
                        if(!TextUtils.isEmpty(O.getPreference(activity,O.ACTIVATION_KEY))){
                            tv_activation.setText("Device\nActivated");
                            tv_activation.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            et_activation.setText("");
                        }else{
                            tv_activation.setText("Not\nActivated");
                            tv_activation.setTextColor(getResources().getColor(android.R.color.black));
                        }
                        Toast.makeText(activity, "Device Activated",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(activity, "Device Not Activated",
                                Toast.LENGTH_SHORT).show();
                    }
                    sendSettings();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideLoading();
                Log.e("error",""+error);
                Toast.makeText(activity, "Error...", Toast.LENGTH_SHORT).show();
            }
        });
        VolleySingleton.getInstance(activity).addToRequestQueue(stringRequest);
    }
    public void sendDeviceId(final String deviceid){

        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("device_id", deviceid );
            jsonObject.put("depot_code", MainActivity.station_code);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.e("request_login",jsonObject.toString());
        showLoading("Please wait...");
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, SAVE_DEVICE_ID_API,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hideLoading();
                Log.e("response_login", response.toString());
                try {
                    if(response.getInt("success")==1){
                        O.savePreference(activity, O.DEVICE_ID, deviceid);
                        et_device_id.setText(((MainActivity)getActivity()).getDeviceId());
                        Toast.makeText(activity, "Device ID created",
                                Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(activity, "Device ID not created, Some Problem!",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error",""+error);
                hideLoading();
                Toast.makeText(activity, "Error...", Toast.LENGTH_SHORT).show();
            }
        });
        VolleySingleton.getInstance(activity).addToRequestQueue(stringRequest);
    }
    public class SAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<BaseModel> datalist;
        private LayoutInflater mInflater;
        int TYPE_HEAD=0, TYPE_N=1;

        public SAdapter(Context context, ArrayList<BaseModel> data) {
            this.mInflater = LayoutInflater.from(context);
            this.datalist = data;
        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            if(viewType==TYPE_N){
                View view = mInflater.inflate(R.layout.item_aboutset, parent, false);
                viewHolder=new ViewHolderN(view);
            }else{
                View view = mInflater.inflate(R.layout.item_aboutset_head, parent, false);
                viewHolder=new ViewHolderHead(view);
            }
            return viewHolder;
        }
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder h, final int position ) {
            h.setIsRecyclable(false);
            if(datalist.get(position) instanceof AboutItem){
                AboutItem aboutItem=(AboutItem) datalist.get(position);
                ((ViewHolderN)h).tv_key.setText(aboutItem.key+" :");
                ((ViewHolderN)h).tv_value.setText(aboutItem.value);
            } else if(datalist.get(position) instanceof HeadItem){
                HeadItem headItem=(HeadItem) datalist.get(position);
                ((ViewHolderHead)h).tv_head.setText(headItem.headTxt);
            }
        }
        @Override
        public int getItemCount() {
            return datalist.size();
        }
        @Override
        public int getItemViewType(int position)
        {
            if(datalist.get(position) instanceof AboutItem)
                return TYPE_N;
            else
                return TYPE_HEAD;
        }
        public class ViewHolderN extends RecyclerView.ViewHolder{
            TextView tv_key, tv_value;
            ViewHolderN(View itemView) {
                super(itemView);
                tv_key=itemView.findViewById(R.id.tv_key);
                tv_value=itemView.findViewById(R.id.tv_value);
            }
        }
        public class ViewHolderHead extends RecyclerView.ViewHolder{
            TextView tv_head;
            ViewHolderHead(View itemView) {
                super(itemView);
                tv_head=itemView.findViewById(R.id.tv_head);
            }
        }
    }
    public class AboutItem extends BaseModel{
        String key; String value;
        public AboutItem(String key, String value){
            this.key=key; this.value=value;
        }
    }
    public class HeadItem extends BaseModel{
        String headTxt;
        public HeadItem(String headTxt){
            this.headTxt=headTxt;
        }
    }
    public void setSelect(int position){
        if(position==0){
            vControl.setVisibility(View.VISIBLE);
            rv_about.setVisibility(View.GONE);
        }else if(position==1){
            rv_about.setVisibility(View.VISIBLE);
            vControl.setVisibility(View.GONE);
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

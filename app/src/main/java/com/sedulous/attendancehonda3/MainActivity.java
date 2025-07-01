package com.sedulous.attendancehonda3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;
import com.sedulous.attendancehonda3.Models.WorkTypeModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import attendancehonda.R;

public class MainActivity extends AppCompatActivity implements MFS100Event {
    String LOGIN_ADMIN_API="http://attendance.prashanthospital.in/api/adminlogin";
    public static String API_GET_SETTING="http://attendance.prashanthospital.in/api/get_devicedata";
    public static String GET_TRAINS_API="http://attendance.prashanthospital.in/api/get_train_number";
    public static String GET_SHIFT_API="http://attendance.prashanthospital.in/api/get_shifts";
    public static String UPDATE_ONLINE_TIME="http://attendance.prashanthospital.in/api/get_device_datetime";
    public static String GET_WORK_TYPES="http://attendance.prashanthospital.in/api/get_task_type";


    View btn_drawer, header;
    View[] menues;
    TextView tv_name;
    public static boolean isConnected=false;
    public static boolean fastDetection=false;
    public static long mLastClkTime = 0;
    public static long Threshold = 1500;
    public static long mLastDttTime=0l;
    public static long mLastAttTime=0l;
    public enum ScannerAction {
        Capture, Verify
    }
    public static FingerData lastCapFingerData = null;
    public static ScannerAction scannerAction = ScannerAction.Capture;
    public static MFS100 mfs100 = null;
    public static boolean isCaptureRunning = false;
    MyDataBase mDB;
    ProgressDialog mProgressDialog;
    FragmentManager fragmentManager;
    DrawerLayout drawer;
    int PERMISSIONS_REQUEST_LOCATION = 1;
    boolean mLocationPermissionGranted = false;
    public static MyLocation myLocation;
    public static WorkTypeModel workTypeModel;
    public static String station_code ="", work_type="", app_work_type="", shiftrain="";
    int nselection=-1;
    public static int time=0;
    private static ArrayList<String> workin_list =new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDB=new MyDataBase(MainActivity.this);
        getLocationPermission();
        station_code =O.getPreference(MainActivity.this,O.STATION_CODE);
        work_type =O.getPreference(MainActivity.this,O.WORK_TYPE);
        app_work_type =O.getPreference(MainActivity.this,O.APP_WORK_TYPE);
        shiftrain=O.getPreference(MainActivity.this, O.SHIFTRAIN);
        myLocation=new MyLocation(MainActivity.this);
        btn_drawer=findViewById(R.id.iv_drawer);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        header=navigationView.getHeaderView(0);
        menues=new View[]{header.findViewById(R.id.menu_admin_login),
                header.findViewById(R.id.menu_Attendance),
                header.findViewById(R.id.menu_selectworkin),
                header.findViewById(R.id.menu_home),
                header.findViewById(R.id.menu_init)};
        tv_name=header.findViewById(R.id.tv_name);
        tv_name.setText(work_type+" "+station_code);
        for (View v: menues) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(view.getId()==R.id.menu_admin_login){
                        showLoginDialog();
                    } else if(view.getId()==R.id.menu_Attendance){
                        Fragment f=new AtListFragment();
                        Bundle b=new Bundle();
                        b.putString(O.FROM, O.USER);
                        f.setArguments(b);
                        loadFragment(f);
                    } else if(view.getId()==R.id.menu_selectworkin){
                        if(!TextUtils.isEmpty(O.getPreference(MainActivity.this,O.ACTIVATION_KEY))) {
                            if(shiftrain.equalsIgnoreCase("shift"))
                                getShift();
                            else if(shiftrain.equalsIgnoreCase("train"))
                                getTrains();
                        }else{
                            Toast.makeText(MainActivity.this,"First Activate device",Toast.LENGTH_SHORT).show();
                        }
                    } else if(view.getId()==R.id.menu_home){
                        clearFragments();
                    } else if(view.getId()==R.id.menu_init){
                        initScanner();
                    }
                    drawer.closeDrawer(Gravity.LEFT);
                }
            });
        }
        btn_drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });
        try {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
        try {
            mfs100 = new MFS100(this);
            mfs100.SetApplicationContext(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Fragment fragment = new AttendanceFragment();
        fragmentManager=getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fl_container, fragment ).commit();
        getSetting();

        TimerTask timerTask=new TimerTask() {
            public void run() {
                time++;
                Log.e("akm", "in timer ++++  " + time);
                updateOnlineTime();
            }
        };
        new Timer().schedule(timerTask, 1, 60*1000);
        getWorkTypes();
    }

    public void showLoginDialog(){
        final Dialog dialog_login = new Dialog(MainActivity.this, R.style.Dialog);
        dialog_login.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        dialog_login.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog_login.getWindow().setBackgroundDrawable(getResources()
                .getDrawable(R.drawable.transparent96));
        dialog_login.setCancelable(true);
        dialog_login.setContentView(R.layout.dialog_id_password_admin);
        final TextInputEditText et_uid =dialog_login.findViewById(R.id.tit_uid);
        final TextInputEditText et_password =dialog_login.findViewById(R.id.tit_password);
        //et_uid.setText("7878787878");
        //et_password.setText("123456");
        final TextView tv_ok=dialog_login.findViewById(R.id.tv_ok);
        final TextView tv_cancel=dialog_login.findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_login.cancel();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(et_uid.getText().toString())){
                    Toast.makeText(MainActivity.this, "Enter user ID",
                            Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(et_password.getText().toString())){
                    Toast.makeText(MainActivity.this, "Enter Password",
                            Toast.LENGTH_SHORT).show();
                }else{
                    dialog_login.cancel();
                    login(et_uid.getText().toString(), et_password.getText().toString());
                }
            }
        });
        dialog_login.show();
    }
    public void updateOnlineTime(){

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("device_datetime", O.getDateTime() );
            jsonObject.put("device_id", getDeviceId());
        }catch (Exception e){
            e.printStackTrace();
        }
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, UPDATE_ONLINE_TIME,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        VolleySingleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);
    }
    public void login(final String uid, final String password){

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone_no", uid );
            jsonObject.put("password", password);
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.e("request_login",jsonObject.toString());
        showLoading("Please wait...");
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, LOGIN_ADMIN_API,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("response_login", response.toString());
                hideLoading();
                try {
                    if(response.getInt("success")==1){
                        loadFragment(new AdminFragment());
                    }else{
                        Toast.makeText(MainActivity.this, "Wrong ID or Password",
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
                Toast.makeText(MainActivity.this, "Login failled...",
                        Toast.LENGTH_SHORT).show();
            }
        });
        VolleySingleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);
    }
    public void loadFragment(Fragment fragment) {
        if (fragment != null) {
            clearFragments();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fl_container, fragment, "");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }
    public void loadFragmentForBack(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fl_container, fragment, "");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    public  void clearFragments() {

        int count = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < count; ++i) {
            fragmentManager.popBackStack();
        }
    }
    public  void clearFragment(int n) {
        for (int i = 0; i < n; ++i) {
            fragmentManager.popBackStack();
        }
    }
    private void initScanner() {
        try {
            int ret = mfs100.Init();
            if (ret != 0) {
                Log.e("error ret",""+mfs100.GetErrorMsg(ret));
                isConnected=false;
            } else {
                isConnected=true;
                String info = "Serial: " + mfs100.GetDeviceInfo().SerialNo()
                        + " Make: " + mfs100.GetDeviceInfo().Make()
                        + " Model: " + mfs100.GetDeviceInfo().Model()
                        + "\nCertificate: " + mfs100.GetCertification();
                Log.e("info", info);
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Init failed, unhandled exception",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void unInitScanner() {
        try {
            int ret = mfs100.UnInit();
            if (ret != 0) {
                //SetTextOnUIThread(mfs100.GetErrorMsg(ret));
            } else {
                isConnected=false;
                //SetTextOnUIThread("Uninit Success");
                lastCapFingerData = null;
            }
        } catch (Exception e) {
            Log.e("UnInitScanner.EX", e.toString());
        }
    }
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(Gravity.LEFT)) {
            drawer.closeDrawer(Gravity.LEFT);
        }else {
            Fragment currentF = fragmentManager.findFragmentById(R.id.fl_container);
            if (currentF instanceof RegisterFragment) {
                if (((RegisterFragment) currentF).onBackPressed()) {
                    super.onBackPressed();
                } else {

                }
            } else {
                super.onBackPressed();
            }
        }
    }
    @Override
    protected void onStart() {
        try {
            if (mfs100 == null) {
                mfs100 = new MFS100(this);
                mfs100.SetApplicationContext(getApplicationContext());
            } else {
                initScanner();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }
    @Override
    protected void onStop() {
        try {
            if (isCaptureRunning) {
                int ret = mfs100.StopAutoCapture();
            }
            Thread.sleep(500);
            unInitScanner();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            if (mfs100 != null) {
                mfs100.Dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
    @Override
    public void onResume(){
        super.onResume();
        myLocation=new MyLocation(MainActivity.this);
    }
    @Override
    public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
        if (SystemClock.elapsedRealtime() - mLastAttTime < Threshold) {
            return;
        }
        mLastAttTime = SystemClock.elapsedRealtime();
        int ret;
        if (!hasPermission) {
            isConnected=false;
            return;
        }
        try {
            if (vid == 1204 || vid == 11279) {
                if (pid == 34323) {
                    ret = mfs100.LoadFirmware();
                    if (ret != 0) {
                        isConnected=false;
                    } else {
                        isConnected=true;
                    }
                } else if (pid == 4101) {
                    String key = "Without Key";
                    ret = mfs100.Init();
                    if (ret == 0) {
                        isConnected=true;
                    } else {
                        isConnected=false;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void OnDeviceDetached() {
        try {
            if (SystemClock.elapsedRealtime() - mLastDttTime < Threshold) {
                return;
            }
            mLastDttTime = SystemClock.elapsedRealtime();
            unInitScanner();
        } catch (Exception e) {
        }
    }
    @Override
    public void OnHostCheckFailed(String err) {
        try {
            Toast.makeText(getApplicationContext(), err, Toast.LENGTH_LONG).show();
        } catch (Exception ignored) {
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                myLocation=new MyLocation(MainActivity.this);
            }
        }
    }
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            myLocation=new MyLocation(MainActivity.this);
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
    }
    public String getDeviceId(){
        //return MainActivity.mfs100.GetDeviceInfo().SerialNo();
        return O.getPreference(this, O.DEVICE_ID);
    }
    public void getSetting(){
        if(!TextUtils.isEmpty(getDeviceId())){

            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", getDeviceId() );
                jsonObject.put("depot_code", MainActivity.station_code);
            }catch (Exception e){
                e.printStackTrace();
            }

            Log.e("request_getSetting",jsonObject.toString());
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, API_GET_SETTING,
                    jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("response_getsetting", response.toString());

                    try {
                        if(response.getInt("success")==1){
                            JSONObject settingObj=response.getJSONArray("Getdevicedetails").
                                    getJSONObject(0);
                            String fquality=settingObj.getString("finger_quality");
                            String matchpercent=settingObj.getString("match_percent");
                            String ctimeout=settingObj.getString("capture_timeout");
                            String minworkhm=settingObj.getString("min_work_hm");
                            String exptime=settingObj.getString("expiry_time");
                            String[] hms=minworkhm.split(":");
                            String[] exps=exptime.split(":");
                            O.savePreferenceInt(MainActivity.this, O.QUALITY,Integer.parseInt(fquality));
                            O.savePreferenceInt(MainActivity.this, O.MATCH,Integer.parseInt(matchpercent));
                            O.savePreferenceInt(MainActivity.this, O.TIMEOUT,Integer.parseInt(ctimeout));
                            O.savePreferenceInt(MainActivity.this, O.MINWORKHOUR,Integer.parseInt(hms[0]));
                            O.savePreferenceInt(MainActivity.this, O.MINWORKMINUTE,Integer.parseInt(hms[1]));
                            O.savePreferenceInt(MainActivity.this, O.MINEXPHOUR,Integer.parseInt(exps[0]));
                            O.savePreferenceInt(MainActivity.this, O.MINEXPMINUTE,Integer.parseInt(exps[1]));

                        }else{

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("error",""+error);

                }
            });
            VolleySingleton.getInstance(MainActivity.this).addToRequestQueue(stringRequest);
        }}

    private void getShift() {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("depot_code", MainActivity.station_code);
//            jsonObject.put("depot_code", MainActivity.station_code);
        }catch (Exception e){
            e.printStackTrace();
        }
        final JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, GET_SHIFT_API, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //  Toast.makeText(SuperVisorFeedback.this, "success", Toast.LENGTH_SHORT).show();
                            Log.e("response", response.toString());
                            org.json.JSONArray array = response.getJSONArray("shifts");
                            workin_list.clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                workin_list.add(obj.getString("shift_name"));
                            }
                            selectDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(objectRequest);
    }
    private void getTrains() {
        final JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("depot", MainActivity.station_code);
            String tasktypeID="";
            for(int a=0; a<workTypeModel.mWorkTypeItems.size(); a++){
                if(workTypeModel.mWorkTypeItems.get(a).mWorkTypeName.equalsIgnoreCase(MainActivity.work_type)){
                    tasktypeID=workTypeModel.mWorkTypeItems.get(a).mWorkTypeId;
                    break;
                }
            }
            jsonObject.put("task_type", tasktypeID);
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.e("req",jsonObject.toString());
        showLoading("Please wait...");
        final JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, GET_TRAINS_API, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hideLoading();
                            //  Toast.makeText(SuperVisorFeedback.this, "success", Toast.LENGTH_SHORT).show();
                            Log.e("response", response.toString());
                            org.json.JSONArray array = response.getJSONArray("train_number");
                            workin_list.clear();
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                workin_list.add(obj.getString("train_no"));
                            }
                            selectDialog();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(objectRequest);
    }

    public void getWorkTypes() {
        JSONObject jsonObject=new JSONObject();
        try {
//            jsonObject.put("depot_code", MainActivity.station_code);
            jsonObject.put("depot_code", "PMB3");
        }catch (Exception e){
            e.printStackTrace();
        }
        final JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, GET_WORK_TYPES, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("response",response.toString());

                        try {
                            workTypeModel = new Gson().fromJson(response.toString(), WorkTypeModel.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(objectRequest);
    }
    public void selectDialog(){
        nselection=-1;
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Select "+shiftrain);
        CharSequence charSequence[]=new CharSequence[workin_list.size()];
        for(int a=0; a<workin_list.size(); a++){
            charSequence[a]=workin_list.get(a);
        }
        alert.setSingleChoiceItems(charSequence, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nselection=which;
            }
        });
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    if(nselection>=0){

                        Log.e("item", ""+workin_list.get(nselection));
                        Fragment currentF = fragmentManager.findFragmentById(R.id.fl_container);
                        if (currentF instanceof AttendanceFragment) {
                            O.savePreference(MainActivity.this,O.WORKIN,workin_list.get(nselection));
                            ((AttendanceFragment) currentF).tv_workin.
                                    setText(O.getPreference(MainActivity.this, O.WORKIN));
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        alert.show();
    }
    public void showDialog(String message){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage(message);
        builder1.setCancelable(false);
        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    protected void showLoading(@NonNull String message0) {
        mProgressDialog = new ProgressDialog(MainActivity.this);
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

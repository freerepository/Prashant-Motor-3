package com.sedulous.attendancphospital;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.Dialog;
//import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.mantra.mfs100.FingerData;
import com.sedulous.attendancphospital.Models.AtDataModel;
import com.sedulous.attendancphospital.Models.UserDataModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.sedulous.attendancphospital.MainActivity.workTypeModel;
import static com.sedulous.attendancphospital.MainActivity.work_type;

public class AttendanceFragment extends Fragment {

    public static String ATT_API = "http://attendance.prashanthospital.in/api/save_attendancedata";
   // public String GET_locations="http://attendance.prashanthospital.in/api/get_locations";

    View btn_entry, btn_exit;
    TextView tv_workin, dialog_ok, dialog_quality, dialog_fname, dialog_name, dialog_match,
            dialog_message, dialog_inout;
    ImageView dialog_finger, dialog_iv_truefalse;
    MyDataBase mDB;
    TextView tv_worktype;
    EditText et_coachin;
    Spinner sp_location_type;

    ProgressDialog mProgressDialog;
    Dialog dialog_fp;
    Activity activity;
    TextClock txtClock;
    RadioGroup rg_loginwith;
    Runnable runnable;
    Handler handler;
    int nselection = 0;

    UserDataModel userDataModel;

    ArrayAdapter<String> adapter_skilled_type;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance, container, false);
        activity = getActivity();
        mDB = new MyDataBase(activity);
        rg_loginwith = view.findViewById(R.id.rg_loginwith);
        tv_worktype = view.findViewById(R.id.tv_work_type);
        tv_workin = view.findViewById(R.id.tv_work_in);
        btn_entry = view.findViewById(R.id.tv_entry);
        btn_exit = view.findViewById(R.id.tv_exit);
        txtClock = view.findViewById(R.id.txt_clock);


        //txtClock.setTimeZone(TimeZone.getDefault().toString());
        dialog_fp = new Dialog(activity, R.style.Dialog);
        dialog_fp.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        dialog_fp.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog_fp.setCancelable(false);
        dialog_fp.setContentView(R.layout.dialog_attendance);
        dialog_ok = dialog_fp.findViewById(R.id.tv_ok);
        dialog_quality = dialog_fp.findViewById(R.id.tv_quality);
        dialog_fname = dialog_fp.findViewById(R.id.tv_fname);
        dialog_name = dialog_fp.findViewById(R.id.tv_name);
        dialog_match = dialog_fp.findViewById(R.id.tv_match);
        dialog_message = dialog_fp.findViewById(R.id.tv_message);
        dialog_inout = dialog_fp.findViewById(R.id.tv_inout);
        dialog_finger = dialog_fp.findViewById(R.id.iv_fp);
        dialog_iv_truefalse = dialog_fp.findViewById(R.id.iv_truefalse);
        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearData();
                dialog_fp.dismiss();
                try {
                    handler.removeCallbacks(runnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        tv_worktype.setText(O.getPreference(activity, O.WORK_TYPE));
        tv_worktype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(O.getPreference(getContext(), O.ACTIVATION_KEY))) {
                    if (workTypeModel == null) {
                        ((MainActivity) getActivity()).getWorkTypes();
                    } else {
                        nselection = -1;
                        final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Select Work Type");

                        CharSequence charSequence[] = new CharSequence[workTypeModel.mWorkTypeItems.size()];
                        for (int a = 0; a < workTypeModel.mWorkTypeItems.size(); a++) {
                            charSequence[a] = workTypeModel.mWorkTypeItems.get(a).mWorkTypeName;
                        }
                        alert.setSingleChoiceItems(charSequence, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                nselection = which;
                            }
                        });
                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    if (nselection >= 0) {
                                        MainActivity.work_type = workTypeModel.mWorkTypeItems.get(nselection).mWorkTypeName;
                                        O.savePreference(activity, O.WORK_TYPE, MainActivity.work_type);
                                        MainActivity.shiftrain = workTypeModel.mWorkTypeItems.get(nselection).mWorkInType;
                                        O.savePreference(activity, O.SHIFTRAIN, MainActivity.shiftrain);
                                        tv_worktype.setText(O.getPreference(activity, O.WORK_TYPE));

                                        O.savePreference(getContext(), O.WORKIN, "");
                                        tv_workin.setText("");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        alert.show();
                    }
                } else {
                    Toast.makeText(getContext(), "First Activate device", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(O.getPreference(activity,O.ACTIVATION_KEY))) {
                    if (rg_loginwith.getCheckedRadioButtonId() == R.id.rb_pwd) {
                        loginWithPassword(O.IN);
                    } else {
                        onControlClicked(view);
                    }
                }else{
                    Toast.makeText(activity,"First activate device",Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(O.getPreference(activity,O.ACTIVATION_KEY))) {
                    if(rg_loginwith.getCheckedRadioButtonId()==R.id.rb_pwd){
                        loginWithPassword(O.OUT);
                    }else {
                        onControlClicked(view);
                    }
                }else{
                    Toast.makeText(activity,"First activate device",Toast.LENGTH_SHORT).show();
                } }
        });
        tv_workin.setText(O.getPreference(activity, O.WORKIN));
        tv_workin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity,"You can change "+MainActivity.shiftrain+" from menu", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }



    public void loginWithPassword(final String inout){
        final String workin_selected=tv_workin.getText().toString();
        if(TextUtils.isEmpty(workin_selected)){
            Toast.makeText(activity, "Please Select "+MainActivity.shiftrain,
                    Toast.LENGTH_SHORT).show();
        } else {
            final Dialog dialog_login = new Dialog(activity, R.style.Dialog);
            dialog_login.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            dialog_login.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog_login.getWindow().setBackgroundDrawable(getResources()
                    .getDrawable(R.drawable.transparent96));
            dialog_login.setCancelable(true);
            dialog_login.setContentView(R.layout.dialog_id_password);
            final TextInputEditText et_uid =dialog_login.findViewById(R.id.tit_uid);
            final TextInputEditText et_password =dialog_login.findViewById(R.id.tit_password);
            final TextView tv_name=dialog_login.findViewById(R.id.tv_name);
            final TextView tv_ok=dialog_login.findViewById(R.id.tv_ok);
            tv_name.setVisibility(View.GONE);
            tv_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tv_name.getVisibility()!=View.VISIBLE){
                        String uid=et_uid.getText().toString();
                        String password=et_password.getText().toString();
                        if(!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(password)){
                            UserDataModel userDataModel=mDB.getUser(uid, password);
                            if(userDataModel!=null){
                                String latitude="",longitude="", address="";
                                try {
                                    latitude = ""+MainActivity.myLocation.getLocation().getLatitude();
                                    longitude = ""+MainActivity.myLocation.getLocation().getLongitude();
                                    try {
                                        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
                                        List<Address> addresses = geocoder.getFromLocation(MainActivity.myLocation.getLocation().getLatitude(),
                                                MainActivity.myLocation.getLocation().getLongitude(), 1);
                                        address = addresses.get(0).getAddressLine(0);
                                        //stateName = addresses.get(0).getAddressLine(1);
                                        //countryName = addresses.get(0).getAddressLine(2);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                String machine_id=((MainActivity)getActivity()).getDeviceId();
                                //akmstart
                                AtDataModel atDataModel=mDB.getLastAttendance(userDataModel.user_id, workin_selected);
                                if(inout.equalsIgnoreCase(O.IN)){
                                    if(atDataModel==null){
                                        if (mDB.insertToAttendance(userDataModel.user_id, userDataModel.user_name,
                                                userDataModel.user_type, userDataModel.station_code, work_type, workin_selected, inout,
                                                latitude, longitude, address, O.NO ,machine_id) >= 0) {
                                            tv_ok.setText("OK");
                                            tv_name.setText(""+userDataModel.user_name);
                                            tv_name.setVisibility(View.VISIBLE);
                                            uploadAttendance(userDataModel.user_id, userDataModel.user_name, userDataModel.user_type,
                                                    userDataModel.station_code, work_type, workin_selected, inout, latitude, longitude, address);
                                        } else {
                                            Toast.makeText(activity, "Try again!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }else{
                                        if(atDataModel.io_staus.equalsIgnoreCase(O.IN)) {
                                            long timeDif = O.getDateDiffMiliS(new SimpleDateFormat(O.DATE_FORMATE),
                                                    O.getDateTime(), atDataModel.created_time);
                                            if (timeDif > O.getMiliFrom( O.getPreferenceInt(activity, O.MINEXPHOUR,0),
                                                    O.getPreferenceInt(activity, O.MINEXPMINUTE,0), 0)){
                                                if (mDB.insertToAttendance(userDataModel.user_id, userDataModel.user_name,
                                                        userDataModel.user_type, userDataModel.station_code, work_type, workin_selected, inout,
                                                        latitude, longitude,address, O.NO, machine_id) >= 0) {
                                                    uploadAttendance(userDataModel.user_id, userDataModel.user_name, userDataModel.user_type,
                                                            userDataModel.station_code, work_type, workin_selected, inout, latitude, longitude, address);
                                                    tv_ok.setText("OK");
                                                    tv_name.setText(""+userDataModel.user_name);
                                                    tv_name.setVisibility(View.VISIBLE);
                                                } else {
                                                    Toast.makeText(activity, "Try again!", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                tv_name.setText( "You have marked entry(in) attendance\n" +
                                                        "First mark exit(out) then Try again!");
                                                tv_name.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            if (mDB.insertToAttendance(userDataModel.user_id, userDataModel.user_name,
                                                    userDataModel.user_type, userDataModel.station_code, work_type, workin_selected, inout,
                                                    latitude, longitude,address, O.NO, machine_id) >= 0) {
                                                uploadAttendance(userDataModel.user_id, userDataModel.user_name, userDataModel.user_type,
                                                        userDataModel.station_code, work_type, workin_selected, inout, latitude, longitude, address);
                                                tv_ok.setText("OK");
                                                tv_name.setText(""+userDataModel.user_name);
                                                tv_name.setVisibility(View.VISIBLE);
                                            } else {
                                                Toast.makeText(activity, "Try again!",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }else if(inout.equalsIgnoreCase(O.OUT)){
                                    if(atDataModel==null){
                                        if (mDB.insertToAttendance(userDataModel.user_id, userDataModel.user_name,
                                                userDataModel.user_type, userDataModel.station_code, work_type, workin_selected,inout,
                                                latitude, longitude,address, O.NO, machine_id) >= 0) {
                                            uploadAttendance(userDataModel.user_id, userDataModel.user_name, userDataModel.user_type,
                                                    userDataModel.station_code, work_type, workin_selected, inout, latitude, longitude, address);
                                            tv_ok.setText("OK");
                                            tv_name.setText(""+userDataModel.user_name);
                                            tv_name.setVisibility(View.VISIBLE);
                                        } else {
                                            Toast.makeText(activity, "Try again!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if(atDataModel.io_staus.equalsIgnoreCase(O.IN)) {
                                            long timeDif = O.getDateDiffMiliS(new SimpleDateFormat(O.DATE_FORMATE),
                                                    O.getDateTime(), atDataModel.created_time);
                                            if(!work_type.equalsIgnoreCase(atDataModel.work_type) || !workin_selected.equalsIgnoreCase(atDataModel.work_in)){

                                                tv_name.setText("Your 'IN' work-type/depot is "+atDataModel.work_type+
                                                        "\nand 'IN' "+MainActivity.shiftrain+" is "+atDataModel.work_in+
                                                        "\n Please Select 'OUT' work-type/depot and "+MainActivity.shiftrain+" same as last 'IN'");
                                            } else if (timeDif < O.getMiliFrom( O.getPreferenceInt(activity, O.MINWORKHOUR,0),
                                                    O.getPreferenceInt(activity, O.MINWORKMINUTE,0), 0)){
                                                tv_name.setText("You have marked entry(in) on "+atDataModel.created_time+
                                                        "\nTo mark exit(out) minimum working time required "+
                                                        "\n"+O.getPreferenceInt(activity, O.MINWORKHOUR,0)+" hour  "+
                                                        O.getPreferenceInt(activity, O.MINWORKMINUTE,0)+ " minute");
                                            } else {
                                                if (mDB.insertToAttendance(userDataModel.user_id, userDataModel.user_name,
                                                        userDataModel.user_type, userDataModel.station_code, work_type, workin_selected, inout,
                                                        latitude, longitude,address, O.NO, machine_id) >= 0) {
                                                    uploadAttendance(userDataModel.user_id, userDataModel.user_name, userDataModel.user_type,
                                                            userDataModel.station_code, work_type, workin_selected,inout, latitude, longitude, address);
                                                    tv_ok.setText("OK");
                                                    tv_name.setText(""+userDataModel.user_name);
                                                    tv_name.setVisibility(View.VISIBLE);
                                                } else {
                                                    Toast.makeText(activity, "Try again!",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } else {
                                            tv_name.setText("You have not marked entry(IN) time attendance!");
                                        }
                                    }
                                }
                            }else{
                                Toast.makeText(activity, "User ID & Password not match",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(activity, "Enter User ID & Password",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        dialog_login.dismiss();
                    }}
            });
            dialog_login.show();
        }}
    public void onControlClicked(View v) {
        if (SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold) {
            return;
        }
        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
        String workin_selected=tv_workin.getText().toString();
      //  final String location_selected=location_type_list.get(sp_location_type.getSelectedItemPosition());

        try {
            switch (v.getId()) {
                case R.id.tv_entry:{

                    if(TextUtils.isEmpty(workin_selected)){
                        Toast.makeText(activity, "Please Select "+MainActivity.shiftrain+".",
                                Toast.LENGTH_SHORT).show();
                    }else {
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(work_type, workin_selected, O.IN);
                        }
                    }}
                break;
                case R.id.tv_exit:{

                    MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                    if(TextUtils.isEmpty(workin_selected)){
                        Toast.makeText(activity, "Please Select "+MainActivity.shiftrain+".",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(work_type, workin_selected, O.OUT);
                        }
                    }}
                break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void StartSyncCapture(final String wtype, final String work_in, final String inout) {
        clearData();
        dialog_fp.show();
        dialog_inout.setText(inout);
        new Thread(new Runnable() {

            @Override
            public void run() {
                //SetTextOnUIThread("");
                MainActivity.isCaptureRunning = true;
                try {
                    final FingerData fingerData = new FingerData();
                    int ret = MainActivity.mfs100.AutoCapture(fingerData,
                            O.getPreferenceInt(activity, O.TIMEOUT, 10000), MainActivity.fastDetection);
                    Log.e("StartSyncCapture.RET", "" + ret);
                    if (ret != 0) {
                        Log.e("error ret",""+MainActivity.mfs100.GetErrorMsg(ret));
                        //SetTextOnUIThread(mfs100.GetErrorMsg(ret));
                    } else {
                        MainActivity.lastCapFingerData = fingerData;
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0,
                                fingerData.FingerImage().length);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog_finger.setImageBitmap(bitmap);
                                dialog_quality.setText("Quality  "+fingerData.Quality()+" %");
                                markAttendance(wtype, work_in, inout, MainActivity.lastCapFingerData);
                            }
                        });
                        String log = "\nQuality: " + fingerData.Quality()
                                + "\nNFIQ: " + fingerData.Nfiq()
                                + "\nWSQ Compress Ratio: "
                                + fingerData.WSQCompressRatio()
                                + "\nImage Dimensions (inch): "
                                + fingerData.InWidth() + "\" X "
                                + fingerData.InHeight() + "\""
                                + "\nImage Area (inch): " + fingerData.InArea()
                                + "\"" + "\nResolution (dpi/ppi): "
                                + fingerData.Resolution() + "\nGray Scale: "
                                + fingerData.GrayScale() + "\nBits Per Pixal: "
                                + fingerData.Bpp() + "\nWSQ Info: "
                                + fingerData.WSQInfo();
                        Log.e("fp data",log);
                        //SetLogOnUIThread(log);
                        //SetData2(fingerData);
                    }
                } catch (Exception ex) {
                    //SetTextOnUIThread("Error");
                } finally {
                    MainActivity.isCaptureRunning = false;
                }
            }
        }).start();
    }

    public void markAttendance(String wtype, String work_in,String inout, FingerData fingerData){

        if(fingerData==null){
            dialog_message.setText("Please capture finger print");
        } else {
            try {
                byte[] bytes = MainActivity.lastCapFingerData.ISOTemplate();
                ArrayList<HashMap<String, Object>> fp_data= mDB.getAllFingers();
                boolean b_match=false, b_userfound=false;
                for(int a=0; a<fp_data.size(); a++){
                    HashMap<String, Object> data=fp_data.get(a);
                    byte[] store_byte = (byte[]) data.get(O.FINGER_BYTES);
                    int match_percent = MainActivity.mfs100.MatchISO(bytes, store_byte);

                    if(match_percent>O.getPreferenceInt(activity, O.MATCH, 100)){
                        String user_id=(String) data.get(O.USER_ID);
                        AtDataModel atDataModel=mDB.getLastAttendance(user_id, work_in);
                        UserDataModel userDataModel=mDB.getUserData(user_id);
                        String depot=userDataModel.station_code;
                        String user_name=userDataModel.user_name;
                        String utype=  userDataModel.user_type;
                        dialog_fname.setVisibility(View.VISIBLE);
                        dialog_name.setVisibility(View.VISIBLE);
                        dialog_match.setVisibility(View.VISIBLE);
                        b_userfound=true;
                        dialog_fname.setText(data.get(O.FINGER_NAME).toString());
                        dialog_name.setText(""+user_name);
                        dialog_match.setText("Match "+match_percent+" %");

                        String latitude="",longitude="", address="";
                        try {
                            latitude = ""+MainActivity.myLocation.getLocation().getLatitude();
                            longitude = ""+MainActivity.myLocation.getLocation().getLongitude();
                            try {
                                Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(MainActivity.myLocation.getLocation().getLatitude(),
                                        MainActivity.myLocation.getLocation().getLongitude(), 1);
                                address = addresses.get(0).getAddressLine(0);
                                //stateName = addresses.get(0).getAddressLine(1);
                                //countryName = addresses.get(0).getAddressLine(2);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        if(inout.equalsIgnoreCase(O.IN)){
                            if(atDataModel==null){
                                if (mDB.insertToAttendance(user_id, user_name, utype, depot, wtype, work_in, inout,
                                        latitude, longitude, address, O.NO ,((MainActivity)getActivity()).getDeviceId()) >= 0) {
                                    b_match=true;
                                    uploadAttendance(user_id, user_name, utype, depot, wtype, work_in, inout,
                                            latitude, longitude, address);
                                    dismisDialogLatter();
                                } else {
                                    Toast.makeText(activity, "Try again!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                if(atDataModel.io_staus.equalsIgnoreCase(O.IN)) {
                                    long timeDif = O.getDateDiffMiliS(new SimpleDateFormat(O.DATE_FORMATE),
                                            O.getDateTime(), atDataModel.created_time);
                                    if (timeDif > O.getMiliFrom( O.getPreferenceInt(activity, O.MINEXPHOUR,0),
                                            O.getPreferenceInt(activity, O.MINEXPMINUTE,0), 0)){
                                        if (mDB.insertToAttendance(user_id, user_name, utype, depot, wtype, work_in, inout,
                                                latitude, longitude, address, O.NO , ((MainActivity)getActivity()).getDeviceId()) >= 0) {
                                            b_match=true;
                                            uploadAttendance(user_id, user_name, utype, depot, wtype, work_in, inout,
                                                    latitude, longitude, address);
                                            dismisDialogLatter();
                                        } else {
                                            Toast.makeText(activity, "Try again!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        dialog_message.setText( "You have marked entry(in) attendance\n" +
                                                "First mark exit(out) then Try again!");
                                    }
                                } else {
                                    if (mDB.insertToAttendance(user_id, user_name, utype, depot, wtype, work_in, inout,
                                            latitude, longitude, address, O.NO , ((MainActivity)getActivity()).getDeviceId()) >= 0) {
                                        b_match=true;
                                        uploadAttendance(user_id, user_name, utype, depot, wtype, work_in, inout,
                                                latitude, longitude, address);
                                        dismisDialogLatter();
                                    } else {
                                        Toast.makeText(activity, "Try again!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }else if(inout.equalsIgnoreCase(O.OUT)){
                            if(atDataModel==null){
                                if (mDB.insertToAttendance(user_id, user_name, utype, depot, wtype, work_in, inout,
                                        latitude, longitude,address,  O.NO , ((MainActivity)getActivity()).getDeviceId()) >= 0) {
                                    b_match=true;
                                    uploadAttendance(user_id, user_name, utype, depot, wtype, work_in, inout,
                                            latitude, longitude, address);
                                    dismisDialogLatter();
                                } else {
                                    Toast.makeText(activity, "Try again!", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                if(atDataModel.io_staus.equalsIgnoreCase(O.IN)) {
                                    long timeDif = O.getDateDiffMiliS(new SimpleDateFormat(O.DATE_FORMATE),
                                            O.getDateTime(), atDataModel.created_time);
                                    if(!work_type.equalsIgnoreCase(atDataModel.work_type) || !work_in.equalsIgnoreCase(atDataModel.work_in)){
                                        String wt="";
                                        if(atDataModel.work_type.equalsIgnoreCase("1")) wt="OBHS";
                                        dialog_message.setText("Your 'IN' work-type/depot is "+wt+
                                                "\nand 'IN' "+MainActivity.shiftrain+" is "+atDataModel.work_in+
                                                "\n Please Select 'OUT' depot and "+MainActivity.shiftrain+" same as last 'IN'");
                                    } else if (timeDif < O.getMiliFrom( O.getPreferenceInt(activity, O.MINWORKHOUR,0),
                                            O.getPreferenceInt(activity, O.MINWORKMINUTE,0), 0)){
                                        dialog_message.setText( "You have marked entry(in) on "+atDataModel.created_time+
                                                "\nTo mark exit(out) minimum working time required "+
                                                "\n"+O.getPreferenceInt(activity, O.MINWORKHOUR,0)+" hour  "+
                                                O.getPreferenceInt(activity, O.MINWORKMINUTE,0)+ " minute  ");
                                    } else {
                                        if (mDB.insertToAttendance(user_id, user_name, utype, depot, wtype, work_in, inout,
                                                latitude, longitude, address, O.NO , ((MainActivity)getActivity()).getDeviceId()) >= 0) {
                                            uploadAttendance(user_id, user_name, utype, depot, wtype, work_in, inout,
                                                    latitude, longitude, address);
                                            dismisDialogLatter();
                                            b_match=true;
                                        } else {
                                            Toast.makeText(activity, "Try again!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    dialog_message.setText("You have not marked entry(IN) time attendance!");
                                }
                            }
                        }
                        break;
                    }
                }
                if(!b_match){
                    dialog_message.setText("Attandance Not Marked.\n"+dialog_message.getText());
                    dialog_iv_truefalse.setImageDrawable(getResources().getDrawable(R.drawable.cross_sign));
                }else{
                    dialog_message.setText("Attandance marked successfully.");
                    dialog_iv_truefalse.setImageDrawable(getResources().getDrawable(R.drawable.true_sign));
                }
                if(!b_userfound){
                    dialog_name.setVisibility(View.VISIBLE);
                    dialog_name.setText("User Not Found");
                }
                stopCapture();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void uploadAttendance(final String user_id, String user_name, String utype, String depot, String wtype,
                                 final String work_in, String inout, String latitude, String longitude, String address) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RAPD_BioID", user_id);
            jsonObject.put("EMPM_Name", user_name);
            jsonObject.put("EMPM_type", utype);
            jsonObject.put("RAPD_DeviceId", ((MainActivity) getActivity()).getDeviceId());
            jsonObject.put("RAPD_Field1", inout);
            String tasktypeID="";
            for(int a=0; a<workTypeModel.mWorkTypeItems.size(); a++){
                if(workTypeModel.mWorkTypeItems.get(a).mWorkTypeName.equalsIgnoreCase(wtype)){
                    tasktypeID=workTypeModel.mWorkTypeItems.get(a).mWorkTypeId;
                    break;
                }
            }
            jsonObject.put("RAPD_Field2", tasktypeID);
            jsonObject.put("RAPD_Field3", "0");
            jsonObject.put("RAPD_Field4", work_in);
            jsonObject.put("RAPD_Field5", "");
            jsonObject.put("RAPD_PunchDateTime", O.getDateTime());
            jsonObject.put("RAPD_latitude", latitude);
            jsonObject.put("RAPD_longitude", longitude);
            jsonObject.put("address", address);
            jsonObject.put("depot_code", depot);


        } catch (Exception e) {
            e.printStackTrace();
        }
        final String requestBody = jsonObject.toString();
        Log.e("reqbody", requestBody);
        showLoading("Upoading data...");

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST,
                ATT_API, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoading();
                Log.e("response", response);
                try {
                    JSONObject resObj = new JSONObject(response);
                    if (resObj.getInt("success") == 1) {
                        mDB.updateAttedanceStatus(mDB.getLastAttendance(user_id, work_in).id, O.YES);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(activity, "" + response, Toast.LENGTH_LONG).show();
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


    private void stopCapture() {
        try {
            MainActivity.mfs100.StopAutoCapture();
        } catch (Exception e) {
            Log.e("error stop_cap",e.toString());
        }
    }
    public void clearData(){
        try {
            MainActivity.lastCapFingerData = null;
            dialog_match.setText("");
            dialog_fname.setText("");
            dialog_name.setText("");
            dialog_quality.setText("");
            dialog_message.setText("");
            dialog_inout.setText("");
            O.clearImg(dialog_finger);
            O.clearImg(dialog_iv_truefalse);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
    public void dismisDialogLatter(){
        handler=new Handler();
        runnable=new Runnable() {
            @Override
            public void run() {
                try {
                    clearData();
                    dialog_fp.dismiss();
                    handler.removeCallbacks(this);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 8000);
    }
}

package com.sedulous.attendancehonda3;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mantra.mfs100.FingerData;
import com.sedulous.attendancehonda3.Models.FPdataModel;
import com.sedulous.attendancehonda3.Models.UserDataModel;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.sedulous.attendancehonda3.MainActivity.myLocation;

import attendancehonda.R;

public class RegisterFragment extends Fragment {

    String UPLOAD_API="http://attendance.prashanthospital.in/api/attedance_save_user";
    String UPLOAD_IMAGE_API="http://attendance.prashanthospital.in/api/upload_userimage";
    String UPDATE_API="http://attendance.prashanthospital.in/api/update_user";
    String UPLOAD_FINGER_API="http://attendance.prashanthospital.in/api/save_userfinger";
    String GET_PIC_API="http://attendance.prashanthospital.in/api/get_userpic";
    String GET_SKILLED_TYPE="http://attendance.prashanthospital.in/api/get_skilledtype";
    String GET_USER_TYPE="http://attendance.prashanthospital.in/api/get_usertype";

    ImageView ivl_pinky, ivl_ring, ivl_middle, ivl_index, ivl_thumb, ivr_pinky, ivr_ring, ivr_middle, ivr_index, ivr_thumb,
            dialog_finger, iv_mypic;
    Boolean bl_pinky = false, bl_ring = false, bl_middle = false, bl_index = false, bl_thumb = false,
            br_pinky = false, br_ring = false, br_middle = false, br_index = false, br_thumb = false;
    TextView btNext, bt_back, bt_done, dialog_ok, dialog_quality, dialog_fname, tv_pic;
    View v_fields, v_fingerp;
    RadioGroup rg_ltype, rg_policev, rg_idcard;
    TextInputLayout til_pwd;
    TextInputEditText et_name, et_phone, et_employeeId, et_dateOfJoining, et_password;
    int REQUEST_MYPIC = 99;
    Spinner sp_utype, sp_skilled_type;
    ArrayList<String> utype_list = new ArrayList<>();
    ArrayList<String> utype_id_list = new ArrayList<>();
    ArrayList<String> skilled_type_list = new ArrayList<>();
    ArrayList<String> skilled_type_id_list = new ArrayList<>();
    ArrayAdapter<String> adapter_utype;
    ArrayAdapter<String> adapter_skilled_type;
    Bitmap bitmapPic;
    MyDataBase mDB;
    Dialog dialog_fp;
    final Calendar myCalendar = Calendar.getInstance();
    Activity activity;
    String task = "", fileUri, imageresponse_pic="";
    UserDataModel userDataModel = null;
    ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        activity = getActivity();
        initViews(view);
        task = getArguments().getString(O.TASK);
        if (task.equalsIgnoreCase(O.EDIT)) {
            userDataModel = (UserDataModel) getArguments().getSerializable(O.DATA);
        }

        mDB = new MyDataBase(activity);
        dialog_fp = new Dialog(activity, R.style.Dialog);
        dialog_fp.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        dialog_fp.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog_fp.setCancelable(false);
        dialog_fp.setContentView(R.layout.dialog_finger_print);
        dialog_ok = dialog_fp.findViewById(R.id.tv_ok);
        dialog_quality = dialog_fp.findViewById(R.id.tv_quality);
        dialog_fname = dialog_fp.findViewById(R.id.tv_fname);
        dialog_finger = dialog_fp.findViewById(R.id.iv_fp);
        dialog_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_fp.dismiss();
            }
        });
        utype_list.add(0, "Select User Type");

        skilled_type_list.add(0, "Select Skilled Type");

        adapter_utype = new ArrayAdapter<String>(activity, R.layout.text_v, utype_list);
        adapter_utype.setDropDownViewResource(R.layout.text_v);
        sp_utype.setAdapter(adapter_utype);
        sp_utype.setSelection(0, true);

        adapter_skilled_type = new ArrayAdapter<String>(activity, R.layout.text_v, skilled_type_list);
        adapter_skilled_type.setDropDownViewResource(R.layout.text_v);
        sp_skilled_type.setAdapter(adapter_skilled_type);
        sp_skilled_type.setSelection(0, true);

        final DatePickerDialog.OnDateSetListener journeyDate1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "dd-MM-yyyy", Locale.US);
                String dt = "" + dayOfMonth;
                if (dt.length() == 1) dt = "0" + dt;
                String mnth = "" + (monthOfYear + 1);
                if (mnth.length() == 1) mnth = "0" + mnth;
                et_dateOfJoining.setText(year + "-" + mnth + "-" + dt);
            }
        };
        et_dateOfJoining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dpd = new DatePickerDialog(activity, journeyDate1, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                dpd.getDatePicker().setMaxDate(new Date().getTime());
                dpd.show();


            }
        });
        et_dateOfJoining.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(et_dateOfJoining.getText().toString())) ;

            }
        });
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (v_fields.getVisibility() == View.VISIBLE) {
                    if (TextUtils.isEmpty(et_name.getText().toString())) {
                        Toast.makeText(activity, "Enter Name",
                                Toast.LENGTH_SHORT).show();
                    } else if (sp_utype.getSelectedItemPosition() == 0) {
                        Toast.makeText(activity, "Enter UserType",
                                Toast.LENGTH_SHORT).show();
                    } else if (sp_skilled_type.getSelectedItemPosition() == 0) {
                        Toast.makeText(activity, "Enter Skilled Type",
                                Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(et_employeeId.getText().toString())) {
                        Toast.makeText(activity, "Enter Employee Id",
                                Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(et_phone.getText().toString())) {
                        Toast.makeText(activity, "Enter Phone Number ",
                                Toast.LENGTH_SHORT).show();
                    } else if (rg_policev.getCheckedRadioButtonId()==-1) {
                        Toast.makeText(activity, "Select Police Verification",
                                Toast.LENGTH_SHORT).show();
                    } else if (rg_idcard.getCheckedRadioButtonId()==-1) {
                        Toast.makeText(activity, "Select ID Card Status",
                                Toast.LENGTH_SHORT).show();
                    } else if ((rg_ltype.getCheckedRadioButtonId() == R.id.rb_pwd ||
                            rg_ltype.getCheckedRadioButtonId() == R.id.rb_both) &&
                            TextUtils.isEmpty(et_password.getText().toString())) {
                        Toast.makeText(activity, "Enter Password for login",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if(bitmapPic!=null) {
                            if (task.equalsIgnoreCase(O.ADD)) {
                                uploadUserImage(task, bitmapPic, "");
                            } else if (task.equalsIgnoreCase(O.EDIT)) {
                                uploadUserImage(task, bitmapPic, userDataModel.user_id);
                            }
                        }else{
                            if (task.equalsIgnoreCase(O.ADD)) {
                                uploadUserData(task, "", "");
                            }else if (task.equalsIgnoreCase(O.EDIT)) {
                                uploadUserData(task, "", userDataModel.user_id);
                            }
                        }
                    }
                }
            }
        });
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    v_fingerp.setVisibility(View.GONE);
                    v_fields.setVisibility(View.VISIBLE);
            }
        });
        bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)activity).clearFragment(1);
            }
        });
        rg_ltype.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.rb_fp) {
                    til_pwd.setVisibility(View.GONE);
                    btNext.setText("Next");
                } else if (i == R.id.rb_pwd) {
                    til_pwd.setVisibility(View.VISIBLE);
                    btNext.setText("Submit");
                } else if (i == R.id.rb_both) {
                    til_pwd.setVisibility(View.VISIBLE);
                    btNext.setText("Next");
                }
            }
        });
        ivl_pinky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bl_pinky) {
                    if (!(SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold)) {
                        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(O.LPINKY);
                        }
                    }
                }
            }
        });
        ivr_pinky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!br_pinky) {
                    if (!(SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold)) {
                        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(O.RPINKY);
                        }
                    }
                }
            }
        });
        ivl_ring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bl_ring) {
                    if (!(SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold)) {
                        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(O.LRING);
                        }
                    }
                }
            }
        });
        ivr_ring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!br_ring) {
                    if (!(SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold)) {
                        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(O.RRING);
                        }
                    }
                }
            }
        });
        ivl_middle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bl_middle) {
                    if (!(SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold)) {
                        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(O.LMIDDLE);
                        }
                    }
                }
            }
        });
        ivr_middle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!br_middle) {
                    if (!(SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold)) {
                        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(O.RMIDDLE);
                        }
                    }
                }
            }
        });
        ivl_index.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bl_index) {
                    if (!(SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold)) {
                        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(O.LINDEX);
                        }
                    }
                }
            }
        });
        ivr_index.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!br_index) {
                    if (!(SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold)) {
                        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(O.RINDEX);
                        }
                    }
                }
            }
        });
        ivl_thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bl_thumb) {
                    if (!(SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold)) {
                        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(O.LTHUMB);
                        }
                    }
                }
            }
        });
        ivr_thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!br_thumb) {
                    if (!(SystemClock.elapsedRealtime() - MainActivity.mLastClkTime < MainActivity.Threshold)) {
                        MainActivity.mLastClkTime = SystemClock.elapsedRealtime();
                        MainActivity.scannerAction = MainActivity.ScannerAction.Capture;
                        if (!MainActivity.isCaptureRunning) {
                            StartSyncCapture(O.RTHUMB);
                        }
                    }
                }
            }
        });
        tv_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission(REQUEST_MYPIC);
            }
        });

        if (userDataModel != null) {
            getPicture(userDataModel.user_id);
            et_name.setText(userDataModel.user_name);
            et_phone.setText(userDataModel.phone);
            et_employeeId.setText(userDataModel.user_id);
            et_dateOfJoining.setText(userDataModel.dataOfJoining);
            if (!TextUtils.isEmpty(userDataModel.password))
                et_password.setText(userDataModel.password);
            try {
                sp_utype.setSelection(utype_id_list.indexOf(userDataModel.user_type));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sp_skilled_type.setSelection(skilled_type_id_list.indexOf(userDataModel.skilled_type));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (userDataModel.police_verification.equalsIgnoreCase(O.YES)) {
                rg_policev.check(R.id.rb_yes);
            }else if (userDataModel.police_verification.equalsIgnoreCase(O.NO)) {
                rg_policev.check(R.id.rb_no);
            }
            if (userDataModel.id_card.equalsIgnoreCase(O.YES)) {
                rg_idcard.check(R.id.rb_idcard_yes);
            }else if (userDataModel.id_card.equalsIgnoreCase(O.NO)) {
                rg_idcard.check(R.id.rb_idcard_no);
            }
            ArrayList<FPdataModel> fp_list = new ArrayList<>();
            if (userDataModel.login_method.equalsIgnoreCase(O.FP)) {
                rg_ltype.check(R.id.rb_fp);
                et_password.setVisibility(View.GONE);
                fp_list = mDB.getFingersOfUser(userDataModel.user_id);
            } else if (userDataModel.login_method.equalsIgnoreCase(O.PWD)) {
                rg_ltype.check(R.id.rb_pwd);
                et_password.setVisibility(View.VISIBLE);
            } else if (userDataModel.login_method.equalsIgnoreCase(O.BOTH)) {
                rg_ltype.check(R.id.rb_both);
                et_password.setVisibility(View.VISIBLE);
                fp_list = mDB.getFingersOfUser(userDataModel.user_id);
            }
            if (fp_list != null && fp_list.size() > 0) {
                for (int a = 0; a < fp_list.size(); a++) {
                    setFG(fp_list.get(a).fname);
                }
            }
        }
        getUserTypes();
        getSkilledTypes();
        return view;
    }

    public void saveFinger(String finger_name, FingerData lastCapFingerData) {
        try {
            byte[] bytes = lastCapFingerData.ISOTemplate();
            if (lastCapFingerData.Quality() < O.getPreferenceInt(activity, O.QUALITY, 40)) {
                Toast.makeText(activity, "Finger Print quality very low, Try again",
                        Toast.LENGTH_SHORT).show();
            } else if (userDataModel != null && task.equalsIgnoreCase(O.EDIT)) {
                if (mDB.insertToFinger(userDataModel.user_id, userDataModel.station_code, ((MainActivity)getActivity()).getDeviceId(), userDataModel.upload_status,finger_name, bytes) >= 0) {
                    setFG(finger_name);
                    if (dialog_fp.isShowing()) dialog_fp.dismiss();
                    Toast.makeText(activity, "Data saved Successfully",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Failed",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity, "Some problem, Try again",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveUser(String user_id) {
        try {
            String lmethod = "", password = "", policev="", id_card_status="";
            if (rg_ltype.getCheckedRadioButtonId() == R.id.rb_fp) {
                lmethod = O.FP;
            } else if (rg_ltype.getCheckedRadioButtonId() == R.id.rb_pwd) {
                lmethod = O.PWD;
                password = et_password.getText().toString();
            } else if (rg_ltype.getCheckedRadioButtonId() == R.id.rb_both) {
                lmethod = O.BOTH;
                password = et_password.getText().toString();
            }
            if (rg_policev.getCheckedRadioButtonId() == R.id.rb_yes) {
                policev = O.YES;
            } else if (rg_policev.getCheckedRadioButtonId() == R.id.rb_no) {
                policev = O.NO;
            }
            if (rg_idcard.getCheckedRadioButtonId() == R.id.rb_idcard_yes) {
                id_card_status = O.YES;
            } else if (rg_idcard.getCheckedRadioButtonId() == R.id.rb_idcard_no) {
                id_card_status = O.NO;
            }

            long result = mDB.insertToUser(user_id, MainActivity.station_code,et_name.getText().toString(),
                    utype_id_list.get(sp_utype.getSelectedItemPosition()), skilled_type_id_list.get(sp_skilled_type.getSelectedItemPosition()),
                    lmethod, password, et_phone.getText().toString(),
                    et_dateOfJoining.getText().toString(), policev, id_card_status, O.YES);
            if (result > 0) {
                Toast.makeText(activity, "User Created Successfully",
                        Toast.LENGTH_SHORT).show();
                userDataModel = mDB.getUserData(user_id);
                if (userDataModel != null) {
                    task = O.EDIT;
                    if (lmethod.equalsIgnoreCase(O.PWD)) {
                        ((MainActivity) getActivity()).clearFragment(1);
                        Fragment f = new UDetailFragment();
                        Bundle b = new Bundle();
                        b.putSerializable(O.DATA, userDataModel);
                        f.setArguments(b);
                        ((MainActivity) getActivity()).loadFragmentForBack(f);
                    } else {
                        v_fingerp.setVisibility(View.VISIBLE);
                        v_fields.setVisibility(View.GONE);
                    }
                }
            } else if (result == -1) {
                Toast.makeText(activity, "User Already exist",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "User not created, Some Problem",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateUser(String user_id) {
        try {
            String lmethod = "", password = "", policev="", id_card_status="";
            if (rg_ltype.getCheckedRadioButtonId() == R.id.rb_fp) {
                lmethod = O.FP;
            } else if (rg_ltype.getCheckedRadioButtonId() == R.id.rb_pwd) {
                lmethod = O.PWD;
                password = et_password.getText().toString();
            } else if (rg_ltype.getCheckedRadioButtonId() == R.id.rb_both) {
                lmethod = O.BOTH;
                password = et_password.getText().toString();
            }
            if (rg_policev.getCheckedRadioButtonId() == R.id.rb_yes) {
                policev = O.YES;
            } else if (rg_policev.getCheckedRadioButtonId() == R.id.rb_no) {
                policev = O.NO;
            }
            if (rg_idcard.getCheckedRadioButtonId() == R.id.rb_idcard_yes) {
                id_card_status = O.YES;
            } else if (rg_idcard.getCheckedRadioButtonId() == R.id.rb_idcard_no) {
                id_card_status = O.NO;
            }
            long result = mDB.updateUser(user_id, MainActivity.station_code,et_name.getText().toString(),
                    utype_id_list.get(sp_utype.getSelectedItemPosition()),skilled_type_id_list.get(sp_skilled_type.getSelectedItemPosition()),
                    lmethod, password, et_phone.getText().toString(),
                    et_dateOfJoining.getText().toString(), policev, id_card_status, O.YES);
            if (result > 0) {
                Toast.makeText(activity, "User Updated Successfully",
                        Toast.LENGTH_SHORT).show();
                userDataModel = mDB.getUserData(user_id);
                if (userDataModel != null) {
                    task = O.EDIT;
                    if (lmethod.equalsIgnoreCase(O.PWD)) {
                        ((MainActivity) getActivity()).clearFragment(1);
                        Fragment f = new UDetailFragment();
                        Bundle b = new Bundle();
                        b.putSerializable(O.DATA, userDataModel);
                        f.setArguments(b);
                        ((MainActivity) getActivity()).loadFragmentForBack(f);
                    } else {
                        v_fingerp.setVisibility(View.VISIBLE);
                        v_fields.setVisibility(View.GONE);
                    }
                }
            } else {
                Toast.makeText(activity, "User not updated, Some Problem",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void StartSyncCapture(final String finger_name) {
        clearData();
        dialog_fp.show();
        dialog_fname.setText(finger_name);
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
                        //SetTextOnUIThread(mfs100.GetErrorMsg(ret));
                    } else {
                        MainActivity.lastCapFingerData = fingerData;
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0,
                                fingerData.FingerImage().length);

                        ArrayList<HashMap<String, Object>> fp_data= mDB.getAllFingers();
                        boolean b_match=false;
                        for(int a=0; a<fp_data.size(); a++) {
                            final HashMap<String, Object> data = fp_data.get(a);
                            byte[] store_byte = (byte[]) data.get(O.FINGER_BYTES);
                            final int match_percent = MainActivity.mfs100.MatchISO(MainActivity.lastCapFingerData.ISOTemplate(), store_byte);

                            if (match_percent > 40 ) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog_fname.setText("This finger already registered by "+mDB.getUserData(data.get(O.USER_ID).toString()).user_name+". try another");
                                    }
                                });
                                b_match=true;
                                break;
                            }
                        }

                        if(!b_match) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("StartAkm2", "here");
                                    uploadFinger(userDataModel.user_id, finger_name, MainActivity.lastCapFingerData.ISOTemplate());
                                    dialog_finger.setImageBitmap(bitmap);
                                    dialog_quality.setText(fingerData.Quality() + " %");
                                }
                            });
                        }
                        //Log.e("RawImage", Base64.encodeToString(fingerData.RawData(), Base64.DEFAULT));
                        //Log.e("FingerISOTemplate", Base64.encodeToString(fingerData.ISOTemplate(), Base64.DEFAULT));
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
                    }
                } catch (Exception ex) {
                    Log.e("error", "" + ex);
                } finally {
                    MainActivity.isCaptureRunning = false;
                }
            }
        }).start();
    }

    private void uploadFinger( final String user_id, final String finger_name, byte[] finger_byte) {
        //String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        //byte[] data = Base64.decode(base64, Base64.DEFAULT);
        final JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("user_id", user_id);
            jsonObject.put("depot_code", MainActivity.station_code);
            jsonObject.put("work_type", MainActivity.work_type);
            jsonObject.put("machine_ID", ((MainActivity)getActivity()).getDeviceId());
            jsonObject.put("finger_name", finger_name);
            jsonObject.put("finger_byte", Base64.encodeToString(finger_byte, Base64.DEFAULT));

        }catch (Exception e){
            e.printStackTrace();
        }
        final String requestBody=jsonObject.toString();
        Log.e("reqbody",requestBody);
        showLoading("Upoading data...");

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST,
                UPLOAD_FINGER_API, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoading();
                Log.e("response",response);
                try {
                    saveFinger(finger_name, MainActivity.lastCapFingerData);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(activity, ""+response, Toast.LENGTH_LONG).show();

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

    private void uploadUserData(final String task, String res_pic, final String user_id) {
        String lmethod = "", password = "", policev="", id_card_status="";
        if (rg_ltype.getCheckedRadioButtonId() == R.id.rb_fp) {
            lmethod = O.FP;
        } else if (rg_ltype.getCheckedRadioButtonId() == R.id.rb_pwd) {
            lmethod = O.PWD;
            password = et_password.getText().toString();
        } else if (rg_ltype.getCheckedRadioButtonId() == R.id.rb_both) {
            lmethod = O.BOTH;
            password = et_password.getText().toString();
        }
        if (rg_policev.getCheckedRadioButtonId() == R.id.rb_yes) {
            policev = O.YES;
        } else if (rg_policev.getCheckedRadioButtonId() == R.id.rb_no) {
            policev = O.NO;
        }
        if (rg_idcard.getCheckedRadioButtonId() == R.id.rb_idcard_yes) {
            id_card_status = O.YES;
        } else if (rg_idcard.getCheckedRadioButtonId() == R.id.rb_idcard_no) {
            id_card_status = O.NO;
        }
        JSONObject requestObject=new JSONObject();
        final JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("depot_code", MainActivity.station_code);
            jsonObject.put("work_type", MainActivity.work_type);
            jsonObject.put("user_name", et_name.getText().toString());
            jsonObject.put("user_type", utype_id_list.get(sp_utype.getSelectedItemPosition()));
            jsonObject.put("skilled_type", skilled_type_list.get(sp_skilled_type.getSelectedItemPosition()));
            jsonObject.put("login_method", lmethod);
            jsonObject.put("password", password);
            jsonObject.put("phone", et_phone.getText().toString());
            jsonObject.put("employee_id",et_employeeId.getText().toString());
            jsonObject.put("EMPM_doj", et_dateOfJoining.getText().toString());
            jsonObject.put("user_pic", res_pic);
            jsonObject.put("police_verification", policev);
            jsonObject.put("id_card", id_card_status);
            if(task.equalsIgnoreCase(O.EDIT))
                jsonObject.put("user_id", user_id);
            requestObject.put("userdata",jsonObject);

        }catch (Exception e){
            e.printStackTrace();
        }
        final String requestBody=requestObject.toString();
        Log.e("reqbody",requestBody);
        showLoading("Upoading data...");
        String url=UPLOAD_API;
        if(task.equalsIgnoreCase(O.EDIT)){
            url=UPDATE_API;
        }
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST,
                url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoading();
                Log.e("response",response);
                try {
                    if (task.equalsIgnoreCase(O.ADD)) {
                        String uid="";
                        try {
                            JSONObject resObject=new JSONObject(response);
                            uid=resObject.getString("user_id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        saveUser(uid);
                    } else if (task.equalsIgnoreCase(O.EDIT)) {
                        updateUser(user_id);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(activity, ""+response, Toast.LENGTH_LONG).show();

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
    private void uploadUserImage(final String task, final Bitmap newBitmap, final String user_id) {
        showLoading("Upoading image...");
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(com.android.volley.Request.Method.POST, UPLOAD_IMAGE_API,
                new com.android.volley.Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        hideLoading();
                        Log.e("imageresponse", new String(response.data));
                        uploadUserData(task, new String(response.data).substring(new String(response.data).lastIndexOf("/")+1), user_id);
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideLoading();
                        error.printStackTrace();
                        Toast.makeText(activity, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<>();
                return params;
            }
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                Log.e("pic upload","akm");
                long imagename = System.currentTimeMillis();
                String key="image";
                params.put(key, new DataPart(imagename + ".jpg", O.getByteFromBitmap(newBitmap)));
                return params;
            }
        };
        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue rQueue = Volley.newRequestQueue(activity);
        rQueue.add(volleyMultipartRequest);
    }
    private void getPicture(String user_id ){
        final JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("user_id", user_id);
        }catch (Exception e){
            e.printStackTrace();
        }
        final JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, GET_PIC_API, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //  Toast.makeText(SuperVisorFeedback.this, "success", Toast.LENGTH_SHORT).show();
                            Log.e("array", response.toString());
                            org.json.JSONArray array = response.getJSONArray("Getuserspic");
                            JSONObject obj = array.getJSONObject(0);
                            String pic_url=obj.getString("EMPM_picture_url");
                            Picasso.get().load(pic_url).into(iv_mypic);
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
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        requestQueue.add(objectRequest);
    }
    private void StopCapture() {
        try {
            MainActivity.mfs100.StopAutoCapture();
        } catch (Exception e) {
            //SetTextOnUIThread("Error");
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("IntentData", "" + data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_MYPIC ) {
            bitmapPic = O.reduceScale(fileUri, 1280).copy(Bitmap.Config.ARGB_8888, true);
            String cityName = "", stateName = "", countryName = "";
            Log.e("akm","for pic0");
            try {
                try {
                    double latitude = myLocation.getLocation().getLatitude();
                    double longitude = myLocation.getLocation().getLongitude();
                    Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    cityName = addresses.get(0).getAddressLine(0);
                    stateName = addresses.get(0).getAddressLine(1);
                    countryName = addresses.get(0).getAddressLine(2);
                    // cityname.setText(cityName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Canvas cs = new Canvas(bitmapPic);
                Paint tPaint = new Paint();
                tPaint.setTextSize(35);
                tPaint.setColor(Color.BLUE);
                tPaint.setStyle(Paint.Style.FILL);
                cs.drawText(cityName, 10, bitmapPic.getHeight() - 5, tPaint);
                try {
                    Log.e("akm","for pic2");
                    iv_mypic.setImageBitmap(bitmapPic);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    private void getSkilledTypes() {
        final JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("depot_code", "PMB3");
//            jsonObject.put("depot_code", MainActivity.station_code);
        }catch (Exception e){
            e.printStackTrace();
        }
        final JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, GET_SKILLED_TYPE, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.e("array", response.toString());
                            org.json.JSONArray array = response.getJSONArray("Getskilledtype");
                            skilled_type_list.clear();
                            skilled_type_list.add(0,"Select Skill Type");
                            skilled_type_id_list.clear();
                            skilled_type_id_list.add(0,"Select Skill Type");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                skilled_type_list.add(obj.getString("skilled_type"));
                                skilled_type_id_list.add(obj.getString("id"));

                            }
                            if (userDataModel != null) {
                                try {
                                    sp_skilled_type.setSelection(skilled_type_id_list.indexOf(userDataModel.skilled_type));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter_skilled_type.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        requestQueue.add(objectRequest);
    }
    private void getUserTypes() {
        final JSONObject jsonObject=new JSONObject();
        try {
//            jsonObject.put("depot_code", MainActivity.station_code);
            jsonObject.put("depot_code", "PMB3");
        }catch (Exception e){
            e.printStackTrace();
        }
        final JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, GET_USER_TYPE, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            Log.e("array", response.toString());
                            org.json.JSONArray array = response.getJSONArray("Getuserstype");
                            utype_list.clear();
                            utype_list.add(0,"Select User Type");
                            utype_id_list.clear();
                            utype_id_list.add(0,"Select User Type");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                utype_list.add(obj.getString("user_type_name"));
                                utype_id_list.add(obj.getString("user_type"));
                            }
                            if (userDataModel != null) {
                                try {
                                    sp_utype.setSelection(utype_id_list.indexOf(userDataModel.user_type));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter_utype.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(activity);
        requestQueue.add(objectRequest);
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
    public void clearData() {
        try {
            MainActivity.lastCapFingerData = null;
            O.clearImg(dialog_finger);
            dialog_fname.setText("");
            dialog_quality.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean onBackPressed() {
        if (v_fingerp.getVisibility() == View.VISIBLE) {
            v_fingerp.setVisibility(View.GONE);
            v_fields.setVisibility(View.VISIBLE);
            btNext.setText("Next");
            return false;
        } else {
            return true;
        }
    }

    public void setFG(String fname) {
        if (fname.equalsIgnoreCase(O.LPINKY)) {
            ivl_pinky.setForeground(getResources().getDrawable(R.drawable.fgl_marked));
            bl_pinky = true;
        } else if (fname.equalsIgnoreCase(O.LRING)) {
            ivl_ring.setForeground(getResources().getDrawable(R.drawable.fgl_marked));
            bl_ring = true;
        } else if (fname.equalsIgnoreCase(O.LMIDDLE)) {
            ivl_middle.setForeground(getResources().getDrawable(R.drawable.fgl_marked));
            bl_middle = true;
        } else if (fname.equalsIgnoreCase(O.LINDEX)) {
            ivl_index.setForeground(getResources().getDrawable(R.drawable.fgl_marked));
            bl_index = true;
        } else if (fname.equalsIgnoreCase(O.LTHUMB)) {
            ivl_thumb.setForeground(getResources().getDrawable(R.drawable.fgl_marked));
            bl_thumb = true;
        } else if (fname.equalsIgnoreCase(O.RPINKY)) {
            ivr_pinky.setForeground(getResources().getDrawable(R.drawable.fgr_marked));
            br_pinky = true;
        } else if (fname.equalsIgnoreCase(O.RRING)) {
            ivr_ring.setForeground(getResources().getDrawable(R.drawable.fgr_marked));
            br_ring = true;
        } else if (fname.equalsIgnoreCase(O.RMIDDLE)) {
            ivr_middle.setForeground(getResources().getDrawable(R.drawable.fgr_marked));
            br_middle = true;
        } else if (fname.equalsIgnoreCase(O.RINDEX)) {
            ivr_index.setForeground(getResources().getDrawable(R.drawable.fgr_marked));
            br_index = true;
        } else if (fname.equalsIgnoreCase(O.RTHUMB)) {
            ivr_thumb.setForeground(getResources().getDrawable(R.drawable.fgr_marked));
            br_thumb = true;
        }
    }
    private void initViews(View v) {

        v_fields = v.findViewById(R.id.v_field);
        v_fingerp = v.findViewById(R.id.v_finger);
        btNext = v.findViewById(R.id.tv_next);
        bt_back = v.findViewById(R.id.tv_back);
        bt_done = v.findViewById(R.id.tv_done);
        til_pwd = v.findViewById(R.id.til_password);
        et_name = v.findViewById(R.id.tit_name);
        et_phone = v.findViewById(R.id.tit_phone);
        et_employeeId = v.findViewById(R.id.tit_employeeId);
        et_password = v.findViewById(R.id.tit_password);
        et_dateOfJoining = v.findViewById(R.id.tit_dateOfJoining);
        sp_utype = v.findViewById(R.id.sp_utype);
        sp_skilled_type = v.findViewById(R.id.sp_skilled);
        rg_ltype = v.findViewById(R.id.rg_loginwith);
        rg_policev = v.findViewById(R.id.rg_police_verification);
        rg_idcard = v.findViewById(R.id.rg_idcard);
        ivl_pinky = v.findViewById(R.id.iv_lpinky);
        ivl_ring = v.findViewById(R.id.iv_lring);
        ivl_middle = v.findViewById(R.id.iv_lmiddle);
        ivl_index = v.findViewById(R.id.iv_lindex);
        ivl_thumb = v.findViewById(R.id.iv_lthumb);
        ivr_pinky = v.findViewById(R.id.iv_rpinky);
        ivr_ring = v.findViewById(R.id.iv_rring);
        ivr_middle = v.findViewById(R.id.iv_rmiddle);
        ivr_index = v.findViewById(R.id.iv_rindex);
        ivr_thumb = v.findViewById(R.id.iv_rthumb);
        iv_mypic = v.findViewById(R.id.iv_my_pic);
        tv_pic=v.findViewById(R.id.tv_pic);

    }
    private void checkPermission(int REQUEST_CODE) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            fileUri = O.cameraProcess(activity, REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }
}

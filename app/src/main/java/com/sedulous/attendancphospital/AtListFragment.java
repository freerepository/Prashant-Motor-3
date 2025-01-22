package com.sedulous.attendancphospital;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sedulous.attendancphospital.Models.AtDataModel;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static com.sedulous.attendancphospital.AttendanceFragment.ATT_API;
import static com.sedulous.attendancphospital.MainActivity.workTypeModel;

public class AtListFragment extends Fragment {

    Activity activity;
    TextView tv_search;
    ImageView iv_upload;
    RecyclerView rv;
    AtAdapter adapter;
    ArrayList<AtDataModel> at_list=new ArrayList<>();
    MyDataBase myDataBase;
    ProgressDialog mProgressDialog;
    String last_time="", last_uid="";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_atlist, container, false);
        activity = getActivity();

        tv_search=view.findViewById(R.id.tv_search);
        iv_upload=view.findViewById(R.id.iv_upload);
        rv=view.findViewById(R.id.rv);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(layoutManager);
        myDataBase=new MyDataBase(activity);
        adapter = new AtAdapter(activity, at_list );
        rv.setAdapter(adapter);
        rv.setHasFixedSize(false);
        rv.setNestedScrollingEnabled(true);
//        adapter.datalist=myDataBase.getAttendanceObj("","");
//        adapter.notifyDataSetChanged();
        openSearchDialog();
        tv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearchDialog();
            }
        });
        iv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<AtDataModel> ats=myDataBase.getPendingAttendanceObj();
                if(ats.size()>0)
                uploadAttendance(ats,0);
            }
        });

        return view;
    }
    public void uploadAttendance(final ArrayList<AtDataModel> ats, final int position) {
        final AtDataModel atObj=ats.get(position);
        final JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("RAPD_BioID",atObj.user_id);
            jsonObject.put("EMPM_Name", atObj.user_name);
            jsonObject.put("EMPM_type", atObj.user_type);
            jsonObject.put("RAPD_DeviceId",((MainActivity)getActivity()).getDeviceId());
            jsonObject.put("RAPD_Field1", atObj.io_staus);
            String tasktypeID="";
            for(int a=0; a<workTypeModel.mWorkTypeItems.size(); a++){
                if(workTypeModel.mWorkTypeItems.get(a).mWorkTypeName.equalsIgnoreCase(atObj.work_type)){
                    tasktypeID=workTypeModel.mWorkTypeItems.get(a).mWorkTypeId;
                    break;
                }
            }
            jsonObject.put("RAPD_Field2", tasktypeID);
            jsonObject.put("RAPD_Field3","0");
            jsonObject.put("RAPD_Field4",atObj.work_in);
            jsonObject.put("RAPD_Field5","");
            jsonObject.put("RAPD_PunchDateTime",atObj.created_time);
            jsonObject.put("RAPD_latitude",atObj.latitude);
            jsonObject.put("RAPD_longitude",atObj.longitude);
            jsonObject.put("address",atObj.address);
            jsonObject.put("depot_code",atObj.depot_code);

        }catch (Exception e){
            e.printStackTrace();
        }
        final String requestBody=jsonObject.toString();
        Log.e("reqbody",requestBody);
        showLoading("Upoading data...");

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST,
                ATT_API, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideLoading();
                Log.e("response",response);
                try {
                    JSONObject resObj=new JSONObject(response);
                    if(resObj.getInt("success")==1){
                        myDataBase.updateAttedanceStatus(atObj.id,O.YES);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
                if(position<ats.size()-1){
                    uploadAttendance(ats, position+1);
                }else {
                    adapter.datalist=myDataBase.getAttendanceObj(last_uid,last_time);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(activity, "" + response, Toast.LENGTH_LONG).show();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(com.android.volley.VolleyError error) {
                hideLoading();
                if(position<ats.size()-1){
                    uploadAttendance(ats, position+1);
                }else{
                    adapter.datalist=myDataBase.getAttendanceObj(last_uid,last_time);
                    adapter.notifyDataSetChanged();
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
    public class AtAdapter extends RecyclerView.Adapter<AtAdapter.ViewHolder> {

        private ArrayList<AtDataModel> datalist;
        private LayoutInflater mInflater;

        public AtAdapter(Context context, ArrayList<AtDataModel> data) {
            this.mInflater = LayoutInflater.from(context);
            this.datalist = data;
        }
        @Override
        public AtAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_at, parent, false);
            return new AtAdapter.ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(AtAdapter.ViewHolder h, final int position ) {
            h.setIsRecyclable(false);
            final AtDataModel atDataModel=datalist.get(position);
            h.tv1.setText(atDataModel.user_id);
            h.tv2.setText(atDataModel.user_name);
            h.tv3.setText(atDataModel.io_staus);
            h.tv4.setText(atDataModel.work_type+"  "+atDataModel.work_in);
            h.tv5.setText(atDataModel.created_time);
            if(atDataModel.upload_status.equalsIgnoreCase(O.YES))
                h.iv_true.setVisibility(View.VISIBLE);
            else{
                h.iv_true.setVisibility(View.INVISIBLE);
            }
            h.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                Fragment f=new AtDetailFragment();
                Bundle b=new Bundle();
                b.putSerializable(O.DATA, atDataModel);
                f.setArguments(b);
                ((MainActivity)getActivity()).loadFragmentForBack(f);

                }
            });
        }
        @Override
        public int getItemCount() {
            return datalist.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder{
            TextView tv1, tv2, tv3, tv4, tv5;
            ImageView iv_true;
            ViewHolder(View itemView) {
                super(itemView);
                tv1=itemView.findViewById(R.id.tv1);
                tv2=itemView.findViewById(R.id.tv2);
                tv3=itemView.findViewById(R.id.tv3);
                tv4=itemView.findViewById(R.id.tv4);
                tv5=itemView.findViewById(R.id.tv5);
                iv_true=itemView.findViewById(R.id.iv6);

            }
        }
    }
    public void openSearchDialog(){
        final Dialog dialog = new Dialog(activity, R.style.Dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_atsearch);
        TextView tv_skip=dialog.findViewById(R.id.tv_skip);
        final EditText et_search=dialog.findViewById(R.id.et_search);
        final Spinner sp_year=dialog.findViewById(R.id.sp_year);
        final Spinner sp_month=dialog.findViewById(R.id.sp_month);
        final Spinner sp_date=dialog.findViewById(R.id.sp_date);
        sp_month.setEnabled(false);
        sp_date.setEnabled(false);
        sp_month.setForeground(getResources().getDrawable(R.drawable.corner_stroke_disable));
        sp_date.setForeground(getResources().getDrawable(R.drawable.corner_stroke_disable));
        final ArrayList<String> list_year=new ArrayList<>();
        final ArrayList<String> list_month=new ArrayList<>();
        final ArrayList<String> list_date=new ArrayList<>();
        list_year.add(0,"Year");
        list_month.add(0,"Month");
        list_date.add(0,"Date");
        for(int a=2019; a<=2022; a++){
            String s=String.valueOf(a);
            list_year.add(s);
        }
        for(int a=1; a<=12; a++){
            String s=String.valueOf(a);
            if(s.length()==1) s="0"+s;
            list_month.add(s);
        }
        for(int a=1; a<=31; a++){
            String s=String.valueOf(a);
            if(s.length()==1) s="0"+s;
            list_date.add(s);
        }
        ArrayAdapter adapter_year = new ArrayAdapter<String>(activity, R.layout.text_v, list_year);
        adapter_year.setDropDownViewResource(R.layout.text_v);
        sp_year.setAdapter(adapter_year);
        sp_year.setSelection(0, true);
        ArrayAdapter adapter_month = new ArrayAdapter<String>(activity, R.layout.text_v, list_month);
        adapter_month.setDropDownViewResource(R.layout.text_v);
        sp_month.setAdapter(adapter_month);
        sp_month.setSelection(0, true);
        ArrayAdapter adapter_date = new ArrayAdapter<String>(activity, R.layout.text_v, list_date);
        adapter_date.setDropDownViewResource(R.layout.text_v);
        sp_date.setAdapter(adapter_date);
        sp_date.setSelection(0, true);
        sp_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    sp_month.setEnabled(false);
                    sp_month.setForeground(getResources().getDrawable(R.drawable.corner_stroke_disable));
                    sp_date.setEnabled(false);
                    sp_date.setForeground(getResources().getDrawable(R.drawable.corner_stroke_disable));
                }
                else{
                    sp_month.setEnabled(true);
                    sp_month.setForeground(null);
                    if(sp_month.getSelectedItemPosition()>0) {
                        sp_date.setEnabled(true);
                        sp_date.setForeground(null);
                    }
                }
            }@Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        sp_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0){
                    sp_date.setEnabled(false);
                    sp_date.setForeground(getResources().getDrawable(R.drawable.corner_stroke_disable));
                }
                else{
                    sp_date.setEnabled(true);
                    sp_date.setForeground(null);
                }
            }@Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        tv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                adapter.datalist=myDataBase.getAttendanceObj(last_uid,last_time);
                adapter.notifyDataSetChanged();
            }
        });
        TextView tv_ok=dialog.findViewById(R.id.tv_ok);
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user_id="";
                if(!TextUtils.isEmpty(et_search.getText()))
                    user_id=et_search.getText().toString();
                String time="";
                if(sp_year.getSelectedItemPosition()!=0){
                    time=list_year.get(sp_year.getSelectedItemPosition());
                    if(sp_month.getSelectedItemPosition()!=0){
                        time=time+"_"+list_month.get(sp_month.getSelectedItemPosition());
                        if(sp_date.getSelectedItemPosition()!=0){
                            time=time+"_"+list_date.get(sp_date.getSelectedItemPosition());
                        }
                    }
                }
                last_uid=user_id; last_time=time;
                adapter.datalist=myDataBase.getAttendanceObj(last_uid,last_time);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.show();
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

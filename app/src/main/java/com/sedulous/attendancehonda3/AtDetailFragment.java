package com.sedulous.attendancehonda3;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sedulous.attendancehonda3.Models.AtDataModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import attendancehonda.R;

public class AtDetailFragment extends Fragment {

    RecyclerView rv;
    Adapter adapter;
    ArrayList<HashMap<String, String>> list=new ArrayList<>();
    Activity activity;
    AtDataModel atDataModel;
    MyDataBase myDataBase;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_atdetail, container, false);
        activity = getActivity();
        myDataBase = new MyDataBase(activity);
        atDataModel=(AtDataModel) getArguments().getSerializable(O.DATA);

        Field[] fields = AtDataModel.class.getDeclaredFields();
        for (Field f:fields) {
            HashMap<String, String> hashMap=new HashMap<>();
            try {
                hashMap.put(f.getName(), f.get(atDataModel).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            list.add(hashMap);
        }

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

}

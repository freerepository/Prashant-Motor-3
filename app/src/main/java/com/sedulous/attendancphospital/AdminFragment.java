package com.sedulous.attendancphospital;

import android.app.Activity;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class AdminFragment extends Fragment {

    Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_admin, container, false);
        activity=getActivity();
        view.findViewById(R.id.ll_adduser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(O.getPreference(activity,O.ACTIVATION_KEY))) {
                    Fragment f=new RegisterFragment();
                    Bundle b=new Bundle();
                    b.putString(O.TASK, O.ADD);
                    f.setArguments(b);
                    ((MainActivity)getActivity()).loadFragmentForBack(f);
                }else{
                    Toast.makeText(activity,"First activate device",Toast.LENGTH_SHORT).show();
                }
            }
        });
        view.findViewById(R.id.ll_users).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(O.getPreference(activity,O.ACTIVATION_KEY))) {
                    Fragment f=new UsersFragment();
                    Bundle b=new Bundle();
                    b.putString(O.FROM, O.ADMIN);
                    f.setArguments(b);
                    ((MainActivity)getActivity()).loadFragmentForBack(f);
                }else{
                    Toast.makeText(activity,"First activate device",Toast.LENGTH_SHORT).show();
                }
            }
        });
        view.findViewById(R.id.ll_Attendance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment f=new AtListFragment();
                Bundle b=new Bundle();
                b.putString(O.FROM, O.ADMIN);
                f.setArguments(b);
                ((MainActivity)getActivity()).loadFragmentForBack(f);
            }
        });
        view.findViewById(R.id.ll_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).loadFragmentForBack(new SettingFragment());
            }
        });
        view.findViewById(R.id.ll_maintance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).loadFragmentForBack(new MaintainFragment());
            }
        });

        return view;
    }

}

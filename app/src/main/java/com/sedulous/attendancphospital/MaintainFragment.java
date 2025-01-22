package com.sedulous.attendancphospital;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class MaintainFragment extends Fragment {

    Activity activity;
    MyDataBase mDB;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_maintain, container, false);
        activity=getActivity();
        mDB=new MyDataBase(activity);

        view.findViewById(R.id.tv_deleteat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                builder1.setMessage("All attandance data will be deleted.\nAre you sure?");
                builder1.setCancelable(false);
                builder1.setPositiveButton(
                        "YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mDB.deleteAttandanceRecords();
                                Toast.makeText(activity, "All attandance deleted successfully",
                                        Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        });
                builder1.setNegativeButton(
                        "CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });

        return view;
    }

}

package com.sedulous.attendancehonda3.Models;

import java.io.Serializable;

public class AtDataModel implements Serializable {
    public String id, user_id, user_name, user_type, depot_code, work_type, work_in, io_staus,
            latitude, longitude, address, upload_status, machine_id, created_time;
    public AtDataModel(String id, String user_id, String user_name, String user_type, String depot_code,
                       String work_type, String work_in, String io_staus, String latitude, String longitude,
                       String address, String upload_status, String machine_id, String created_time){
        this.id=id; this.user_id=user_id; this.user_name=user_name; this.user_type=user_type;
        this.depot_code=depot_code; this.work_type=work_type; this.work_in=work_in; this.io_staus=io_staus;
        this.latitude=latitude; this.longitude=longitude; this.address=address; this.machine_id=machine_id;
        this.upload_status=upload_status; this.created_time=created_time;
    }
}

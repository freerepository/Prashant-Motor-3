package com.sedulous.attendancehonda3.Models;

import java.io.Serializable;

public class FPdataModel implements Serializable {

    public String id, user_id, depot_code, machine_id, upload_status, created_time, fname;
    public byte[] fbytes;
    public FPdataModel(String id, String user_id, String depot_code, String machine_id, String upload_status, String created_time, String fname, byte[] fbytes){
        this.id=id; this.user_id=user_id; this.depot_code=depot_code; this.machine_id=machine_id; this.upload_status=upload_status;
        this.created_time=created_time; this.fname=fname; this.fbytes=fbytes;
    }
}

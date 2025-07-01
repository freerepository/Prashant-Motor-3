package com.sedulous.attendancehonda3.Models;

import java.io.Serializable;

public class UserDataModel implements Serializable {

    public String id,
            user_id, station_code,user_name, user_type, skilled_type, login_method, password, phone, dataOfJoining,
            police_verification, id_card, upload_status, created_time;
    public UserDataModel(String id, String user_id, String station_code, String user_name, String user_type, String skilled_type, String login_method,
                         String password, String phone, String dataOfJoining, String police_verification,
                         String id_card, String upload_status, String created_time){
        this.id=id; this.user_id=user_id; this.station_code = station_code; this.user_name=user_name; this.user_type=user_type;
        this.skilled_type=skilled_type; this.login_method=login_method; this.password=password; this.phone=phone; this.dataOfJoining=dataOfJoining;
        this.police_verification=police_verification; this.id_card=id_card;
        this.upload_status=upload_status; this.created_time=created_time;
    }
}

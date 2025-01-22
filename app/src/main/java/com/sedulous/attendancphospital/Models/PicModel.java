package com.sedulous.attendancphospital.Models;

import java.io.Serializable;

public class PicModel implements Serializable {

    String id, pic_id, user_id, path,  upload_status, created_time;
    public PicModel(String id, String pic_id, String user_id, String path, String upload_status, String created_time){
        this.id=id; this.user_id=user_id; this.pic_id=pic_id; this.path=path;
        this.upload_status=upload_status; this.created_time=created_time;
    }
}

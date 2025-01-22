package com.sedulous.attendancphospital.Models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;

public class WorkTypeModel implements Serializable {

    @SerializedName("success")
    public int mStatus;
    @SerializedName("message")
    public String mMessage;
    @SerializedName("task_type")
    public ArrayList<WorkTypeItem> mWorkTypeItems;
    public static class WorkTypeItem implements Serializable{

        @SerializedName("id")
        public String mWorkTypeId;
        @SerializedName("task_type")
        public String mWorkTypeName;
        @SerializedName("task_code")
        public String mWorkTypeCode;
        @SerializedName("mode")
        public String mWorkInType;

    }
}

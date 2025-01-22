package com.sedulous.attendancphospital.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class WorkInModel implements Serializable {

    @SerializedName("success")
    public int mStatus;
    @SerializedName("message")
    public String mMessage;
    @SerializedName("workin")
    public ArrayList<WorkInItem> mWorkInItems;
    public static class WorkInItem implements Serializable{

        @SerializedName("id")
        public String mWorkInId;
        @SerializedName("name")
        public String mWorkInName;

    }

}

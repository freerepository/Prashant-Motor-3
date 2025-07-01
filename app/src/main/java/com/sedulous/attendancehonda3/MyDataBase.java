package com.sedulous.attendancehonda3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.sedulous.attendancehonda3.Models.AtDataModel;
import com.sedulous.attendancehonda3.Models.FPdataModel;
import com.sedulous.attendancehonda3.Models.UserDataModel;
import java.util.ArrayList;
import java.util.HashMap;

public class MyDataBase {
    private static final String DATABASE_NAME = "dball";    // Database Name
    private static final int DATABASE_Version = 1;    // Database Version
    String tag="MyDataBase", TABLE_FINGER="table_finger", TABLE_USER="table_user", TABLE_ATTENDANCE ="table_Attendance", TABLE_PIC="table_pic";
    private String CREATE_TABLE_FINGER = String.format(" Create Table IF NOT EXISTS %s (%s integer PRIMARY KEY AUTOINCREMENT,  " +
                    " %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT NOT NULL, %s BLOB )",
            TABLE_FINGER, O.ID, O.USER_ID, O.STATION_CODE, O.MACHINE_ID, O.UPLOAD_STATUS,O.CREATED_TIME, O.FINGER_NAME,O.FINGER_BYTES);
    private String CREATE_TABLE_USER = String.format(" Create Table IF NOT EXISTS %s (%s integer PRIMARY KEY AUTOINCREMENT," +
                    " %s TEXT NOT NULL UNIQUE, %s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT DEFAULT '',%s TEXT DEFAULT '',%s TEXT DEFAULT ''," +
                    " %s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT NOT NULL, %s TEXT NOT NULL )",
            TABLE_USER, O.ID, O.USER_ID, O.STATION_CODE, O.USER_NAME, O.USER_TYPE,O.SKILLED_TYPE, O.LOGIN_METHOD, O.PASSWORD, O.PHONE, O.EMPLOYEEid, O.DATAOFJOINING,
            O.POLICE_VERIFICATION, O.ID_CARD, O.UPLOAD_STATUS, O.CREATED_TIME);
    private String CREATE_TABLE_ATTENDANCE = String.format(" Create Table IF NOT EXISTS %s (%s integer PRIMARY KEY AUTOINCREMENT," +
                    " %s TEXT NOT NULL, %s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT NOT NULL,%s TEXT NOT NULL," +
                    " %s TEXT DEFAULT '', %s TEXT DEFAULT '', %s TEXT DEFAULT '', %s TEXT NOT NULL, %s TEXT NOT NULL, %s TEXT NOT NULL )",
            TABLE_ATTENDANCE, O.ID, O.USER_ID, O.USER_NAME, O.USER_TYPE, O.STATION_CODE, O.WORK_TYPE, O.WORKIN,
            O.IO_STATUS, O.LAT, O.LONG, O.ADDRESS, O.UPLOAD_STATUS, O.MACHINE_ID, O.CREATED_TIME);

    SQLiteDatabase db;
    Context context;

    public MyDataBase(Context context) {
        this.context = context;
        try {
            db = new MyDbHelper(context).getWritableDatabase();
        } catch (SQLiteCantOpenDatabaseException s) {
            s.printStackTrace();
        }
    }

    public int countAttendance(String user_id, String MMyy){
        int n=0;
        n = db.rawQuery("SELECT id FROM " + TABLE_ATTENDANCE + " WHERE "+O.USER_ID +" = ? AND "+O.CREATED_TIME +" like ? ", new String[]{user_id, "%"+MMyy+"%"}).getCount();
        return  n;
    }
    public void deleteUserRecords(){
        db.execSQL("delete from "+ TABLE_USER);
    }
    public void deleteFingerPrintRecords(){
        db.execSQL("delete from "+ TABLE_FINGER);
    }
    public void deleteAttandanceRecords(){
        db.execSQL("delete from "+ TABLE_ATTENDANCE);
    }

    public long insertToFinger(String user_id, String depot_code, String machine_id, String upload_status,String finger_name, byte[] bytes) {
        long n = 0;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(O.USER_ID, user_id);
            contentValues.put(O.STATION_CODE, depot_code);
            contentValues.put(O.MACHINE_ID, machine_id);
            contentValues.put(O.UPLOAD_STATUS, upload_status);
            contentValues.put(O.CREATED_TIME, O.getDateTime());
            contentValues.put(O.FINGER_NAME, finger_name);
            contentValues.put(O.FINGER_BYTES, bytes);
            n = db.insert(TABLE_FINGER, null, contentValues);

        } catch (Exception e) {
            Log.e("DB", " problem inserting to "+TABLE_FINGER + e);
        }
        return n;
    }
    public ArrayList<HashMap<String, Object>> getAllFingers() {
        ArrayList<HashMap<String, Object>> datalist = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_FINGER, new String[]{});
            if (cursor != null && cursor.moveToLast()) {
                do {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put(O.ID, String.valueOf(cursor.getInt(cursor.getColumnIndex(O.ID))));
                    data.put(O.USER_ID, cursor.getString(cursor.getColumnIndex(O.USER_ID)));
                    data.put(O.STATION_CODE, cursor.getString(cursor.getColumnIndex(O.STATION_CODE)));
                    data.put(O.MACHINE_ID, cursor.getString(cursor.getColumnIndex(O.MACHINE_ID)));
                    data.put(O.UPLOAD_STATUS, cursor.getString(cursor.getColumnIndex(O.UPLOAD_STATUS)));
                    data.put(O.CREATED_TIME, cursor.getString(cursor.getColumnIndex(O.CREATED_TIME)));
                    data.put(O.FINGER_NAME, cursor.getString(cursor.getColumnIndex(O.FINGER_NAME)));
                    data.put(O.FINGER_BYTES, cursor.getBlob(cursor.getColumnIndex(O.FINGER_BYTES)));
                    datalist.add(data);
                }
                while (cursor.moveToPrevious());
            }
            if(cursor!=null && !cursor.isClosed()) cursor.close();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return datalist;
    }
    public ArrayList<FPdataModel> getAllFingersObj() {
        ArrayList<FPdataModel> datalist = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_FINGER, new String[]{});
            if (cursor != null && cursor.moveToLast()) {
                do {
                    FPdataModel fPdataModel=new FPdataModel(
                    String.valueOf(cursor.getInt(cursor.getColumnIndex(O.ID))),
                    cursor.getString(cursor.getColumnIndex(O.USER_ID)),
                    cursor.getString(cursor.getColumnIndex(O.STATION_CODE)),
                    cursor.getString(cursor.getColumnIndex(O.MACHINE_ID)),
                    cursor.getString(cursor.getColumnIndex(O.UPLOAD_STATUS)),
                    cursor.getString(cursor.getColumnIndex(O.CREATED_TIME)),
                    cursor.getString(cursor.getColumnIndex(O.FINGER_NAME)),
                    cursor.getBlob(cursor.getColumnIndex(O.FINGER_BYTES)));
                    datalist.add(fPdataModel);
                }
                while (cursor.moveToPrevious());
            }
            if(cursor!=null && !cursor.isClosed()) cursor.close();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return datalist;
    }
    public ArrayList<FPdataModel> getFingersOfUser(String user_id) {
        ArrayList<FPdataModel> datalist = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_FINGER + " WHERE " + O.USER_ID + " = ? ",
                    new String[]{ user_id } );
            if (cursor != null && cursor.moveToLast()) {
                do {
                    FPdataModel fPdataModel=new FPdataModel(
                    String.valueOf(cursor.getInt(cursor.getColumnIndex(O.ID))),
                    cursor.getString(cursor.getColumnIndex(O.USER_ID)),
                    cursor.getString(cursor.getColumnIndex(O.STATION_CODE)),
                    cursor.getString(cursor.getColumnIndex(O.MACHINE_ID)),
                    cursor.getString(cursor.getColumnIndex(O.UPLOAD_STATUS)),
                    cursor.getString(cursor.getColumnIndex(O.CREATED_TIME)),
                    cursor.getString(cursor.getColumnIndex(O.FINGER_NAME)),
                    cursor.getBlob(cursor.getColumnIndex(O.FINGER_BYTES)));
                    datalist.add(fPdataModel);
                }
                while (cursor.moveToPrevious());
            }
            if(cursor!=null && !cursor.isClosed()) cursor.close();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return datalist;
    }
    public boolean isFingerExist(String user_id, String fingername) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_FINGER + " WHERE " + O.USER_ID + " = ? AND "+O.FINGER_NAME + " = ? ",
                    new String[]{ user_id, fingername } );
            if (cursor != null && cursor.getCount()>0) {
               return true;
            }else {
                return false;
            }

        } catch (Exception e) {
            Log.e(tag, "" + e);
            return false;
        }
    }
    public int deleteFinger(String user_id) {
        try {
            return db.delete(TABLE_FINGER, O.USER_ID+ " = ?", new String[]{user_id});
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return 0;
    }

    public long insertToUser(String user_id, String unit_id, String name, String utype, String skilled_type, String login_method,
                             String password, String phone, String dataOfJoining, String police_verification,
                             String id_card, String upload_status) {
        long n = 0;
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER +" WHERE "+O.USER_ID+ " = ? ", new String[]{user_id});
            if (cursor.getCount() > 0) {
                n = -1;
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(O.USER_ID, user_id);
                contentValues.put(O.STATION_CODE, unit_id);
                contentValues.put(O.USER_NAME, name);
                contentValues.put(O.USER_TYPE, utype);
                contentValues.put(O.SKILLED_TYPE, skilled_type);
                contentValues.put(O.LOGIN_METHOD, login_method);
                contentValues.put(O.PASSWORD, password);
                contentValues.put(O.PHONE, phone);
                contentValues.put(O.DATAOFJOINING, dataOfJoining);
                contentValues.put(O.POLICE_VERIFICATION, police_verification);
                contentValues.put(O.ID_CARD, id_card);
                contentValues.put(O.UPLOAD_STATUS, upload_status);
                contentValues.put(O.CREATED_TIME, O.getDateTime());
                n = db.insert(TABLE_USER, null, contentValues);
            }
            if(!cursor.isClosed()) cursor.close();
        } catch (Exception e) {
            Toast.makeText(context, " problem inserting to "+TABLE_USER + e, Toast.LENGTH_SHORT).show();
        }
        return n;
    }
    public long updateUser(String user_id, String unit_id, String name, String utype, String skilled_type, String login_method,
                           String password, String phone, String dataOfJoining , String police_verification,
                           String id_card, String upload_status) {
        long n = 0;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(O.STATION_CODE, unit_id);
            contentValues.put(O.USER_NAME, name);
            contentValues.put(O.USER_TYPE, utype);
            contentValues.put(O.SKILLED_TYPE, skilled_type);
            contentValues.put(O.LOGIN_METHOD, login_method);
            contentValues.put(O.PASSWORD, password);
            contentValues.put(O.PHONE, phone);
            contentValues.put(O.DATAOFJOINING, dataOfJoining);
            contentValues.put(O.POLICE_VERIFICATION, police_verification);
            contentValues.put(O.ID_CARD, id_card);
            contentValues.put(O.UPLOAD_STATUS, upload_status);
            n = db.update(TABLE_USER, contentValues, O.USER_ID+" = ?", new String[]{user_id});
        } catch (Exception e) {
            Log.e("DB","problem inserting profile " + e);
        }
        return n;
    }

    public UserDataModel getUserData(String user_id) {
        UserDataModel userDataModel=null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE "+O.USER_ID+" = ?", new String[]{ user_id });
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                userDataModel=new UserDataModel(
                String.valueOf(cursor.getInt(cursor.getColumnIndex(O.ID))),
                cursor.getString(cursor.getColumnIndex(O.USER_ID)),
                cursor.getString(cursor.getColumnIndex(O.STATION_CODE)),
                cursor.getString(cursor.getColumnIndex(O.USER_NAME)),
                cursor.getString(cursor.getColumnIndex(O.USER_TYPE)),
                cursor.getString(cursor.getColumnIndex(O.SKILLED_TYPE)),
                cursor.getString(cursor.getColumnIndex(O.LOGIN_METHOD)),
                cursor.getString(cursor.getColumnIndex(O.PASSWORD)),
                cursor.getString(cursor.getColumnIndex(O.PHONE)),
                cursor.getString(cursor.getColumnIndex(O.DATAOFJOINING)),
                cursor.getString(cursor.getColumnIndex(O.POLICE_VERIFICATION)),
                cursor.getString(cursor.getColumnIndex(O.ID_CARD)),
                cursor.getString(cursor.getColumnIndex(O.UPLOAD_STATUS)),
                cursor.getString(cursor.getColumnIndex(O.CREATED_TIME)));
            }
            if(cursor!=null && !cursor.isClosed()) cursor.close();
        } catch (Exception e) {
            Log.e(tag, "getUserData " + e);
        }
        return userDataModel;
    }
    public UserDataModel getUser(String user_id, String password) {
        UserDataModel userDataModel=null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USER +
                    " WHERE "+O.USER_ID + " = ? AND " + O.PASSWORD + " = ? ", new String[]{ user_id , password});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                userDataModel=new UserDataModel(
                        String.valueOf(cursor.getInt(cursor.getColumnIndex(O.ID))),
                        cursor.getString(cursor.getColumnIndex(O.USER_ID)),
                        cursor.getString(cursor.getColumnIndex(O.STATION_CODE)),
                        cursor.getString(cursor.getColumnIndex(O.USER_NAME)),
                        cursor.getString(cursor.getColumnIndex(O.USER_TYPE)),
                        cursor.getString(cursor.getColumnIndex(O.SKILLED_TYPE)),
                        cursor.getString(cursor.getColumnIndex(O.LOGIN_METHOD)),
                        cursor.getString(cursor.getColumnIndex(O.PASSWORD)),
                        cursor.getString(cursor.getColumnIndex(O.PHONE)),
                        cursor.getString(cursor.getColumnIndex(O.DATAOFJOINING)),
                        cursor.getString(cursor.getColumnIndex(O.POLICE_VERIFICATION)),
                        cursor.getString(cursor.getColumnIndex(O.ID_CARD)),
                        cursor.getString(cursor.getColumnIndex(O.UPLOAD_STATUS)),
                        cursor.getString(cursor.getColumnIndex(O.CREATED_TIME)));
            }
            if(cursor!=null && !cursor.isClosed()) cursor.close();
        } catch (Exception e) {
            Log.e(tag, "getUserData " + e);
        }
        return userDataModel;
    }
    public int deleteUser(String user_id) {
        try {
            return db.delete(TABLE_USER, O.USER_ID+ " = ?", new String[]{user_id});
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return 0;
    }


    public ArrayList<UserDataModel> getAllUsersObj() {
        ArrayList<UserDataModel> datalist = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USER, new String[]{});
            if (cursor != null && cursor.moveToLast()) {
                do {
                    UserDataModel userDataModel=new UserDataModel(
                            String.valueOf(cursor.getInt(cursor.getColumnIndex(O.ID))),
                            cursor.getString(cursor.getColumnIndex(O.USER_ID)),
                            cursor.getString(cursor.getColumnIndex(O.STATION_CODE)),
                            cursor.getString(cursor.getColumnIndex(O.USER_NAME)),
                            cursor.getString(cursor.getColumnIndex(O.USER_TYPE)),
                            cursor.getString(cursor.getColumnIndex(O.SKILLED_TYPE)),
                            cursor.getString(cursor.getColumnIndex(O.LOGIN_METHOD)),
                            cursor.getString(cursor.getColumnIndex(O.PASSWORD)),
                            cursor.getString(cursor.getColumnIndex(O.PHONE)),
                            cursor.getString(cursor.getColumnIndex(O.DATAOFJOINING)),
                            cursor.getString(cursor.getColumnIndex(O.POLICE_VERIFICATION)),
                            cursor.getString(cursor.getColumnIndex(O.ID_CARD)),
                            cursor.getString(cursor.getColumnIndex(O.UPLOAD_STATUS)),
                            cursor.getString(cursor.getColumnIndex(O.CREATED_TIME)));
                    datalist.add(userDataModel);
                }
                while (cursor.moveToPrevious());
            }
            if(cursor!=null && !cursor.isClosed()) cursor.close();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return datalist;
    }
    public long insertToAttendance(String user_id, String user_name, String utype, String depot_code,
               String wtype,String workin, String io_status, String latitude, String longitude,
               String address, String upload_status,String machine_id) {
        long n = 0;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(O.USER_ID, user_id);
            contentValues.put(O.USER_NAME, user_name);
            contentValues.put(O.USER_TYPE, utype);
            contentValues.put(O.STATION_CODE, depot_code);
            contentValues.put(O.WORK_TYPE, wtype);
            contentValues.put(O.WORKIN, workin);
            contentValues.put(O.IO_STATUS, io_status);
            contentValues.put(O.LAT, latitude);
            contentValues.put(O.LONG, longitude);
            contentValues.put(O.ADDRESS, address);
            contentValues.put(O.UPLOAD_STATUS, upload_status);
            contentValues.put(O.MACHINE_ID, machine_id);
            contentValues.put(O.CREATED_TIME, O.getDateTime());
            n = db.insert(TABLE_ATTENDANCE, null, contentValues);
        } catch (Exception e) {
            Toast.makeText(context, " problem inserting to "+ TABLE_ATTENDANCE + e, Toast.LENGTH_SHORT).show();
        }
        return n;
    }
    public long updateAttedanceStatus(String id, String upload_status) {
        long n = 0;
        try {
            Log.e("akm","at"+"id "+id+" us "+upload_status);
            Toast.makeText(context,"id "+id+" us "+upload_status,Toast.LENGTH_SHORT).show();
            ContentValues contentValues = new ContentValues();
            contentValues.put(O.UPLOAD_STATUS, upload_status);
            n = db.update(TABLE_ATTENDANCE, contentValues, O.ID+" = ?", new String[]{id});
        } catch (Exception e) {
            Log.e("DB","problem inserting profile " + e);
        }
        return n;
    }
//    public HashMap<String, String> getAttendanceOfUser(String user_id, String time_yyyymmdd) {
//        HashMap<String, String> data = new HashMap<>();
//        Cursor cursor = null;
//        try {
//            cursor = db.rawQuery("SELECT * FROM " + TABLE_ATTENDANCE +
//                    " WHERE user_id = ? AND created_time like ?", new String[]{ user_id, time_yyyymmdd +"%" });
//            if (cursor != null && cursor.getCount() > 0) {
//                cursor.moveToFirst();
//                data.put(O.USER_NAME, cursor.getString(cursor.getColumnIndex(O.USER_NAME)));
//                data.put(O.CREATED_TIME, cursor.getString(cursor.getColumnIndex(O.CREATED_TIME)));
//                if(!cursor.isClosed()) cursor.close();
//            }
//        } catch (Exception e) {
//            Log.e(tag, "getAttendanceData " + e);
//        }
//        return data;
//    }

    public ArrayList<AtDataModel> getAttendanceObj(String user_id, String time_yyyymmdd) {
        ArrayList<AtDataModel> datalist = new ArrayList<>();
        Cursor cursor = null;
        try {
            if(TextUtils.isEmpty(user_id) && TextUtils.isEmpty(time_yyyymmdd)){
                cursor = db.rawQuery("SELECT * FROM " + TABLE_ATTENDANCE, new String[]{});
            } else if(TextUtils.isEmpty(user_id) && !TextUtils.isEmpty(time_yyyymmdd)){
                cursor = db.rawQuery("SELECT * FROM " + TABLE_ATTENDANCE +
                        " WHERE created_time LIKE ?", new String[]{ time_yyyymmdd + "%"});
            } else if(!TextUtils.isEmpty(user_id) && TextUtils.isEmpty(time_yyyymmdd)){
                cursor = db.rawQuery("SELECT * FROM " + TABLE_ATTENDANCE +
                        " WHERE user_id = ?", new String[]{user_id});
            } else{
                cursor = db.rawQuery("SELECT * FROM " + TABLE_ATTENDANCE +
                        " WHERE user_id = ? AND created_time like ?", new String[]{user_id, time_yyyymmdd + "%"});
            }
            if (cursor != null && cursor.moveToLast()) {
                do {
                    AtDataModel data=new AtDataModel(
                    String.valueOf(cursor.getInt(cursor.getColumnIndex(O.ID))),
                    cursor.getString(cursor.getColumnIndex(O.USER_ID)),
                    cursor.getString(cursor.getColumnIndex(O.USER_NAME)),
                    cursor.getString(cursor.getColumnIndex(O.USER_TYPE)),
                    cursor.getString(cursor.getColumnIndex(O.STATION_CODE)),
                    cursor.getString(cursor.getColumnIndex(O.WORK_TYPE)),
                    cursor.getString(cursor.getColumnIndex(O.WORKIN)),
                    cursor.getString(cursor.getColumnIndex(O.IO_STATUS)),
                    cursor.getString(cursor.getColumnIndex(O.LAT)),
                    cursor.getString(cursor.getColumnIndex(O.LONG)),
                    cursor.getString(cursor.getColumnIndex(O.ADDRESS)),
                    cursor.getString(cursor.getColumnIndex(O.UPLOAD_STATUS)),
                    cursor.getString(cursor.getColumnIndex(O.MACHINE_ID)),
                    cursor.getString(cursor.getColumnIndex(O.CREATED_TIME)));
                    datalist.add(data);
                }
                while (cursor.moveToPrevious());
            }
            if(cursor!=null && !cursor.isClosed()) cursor.close();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return datalist;
    }
    public ArrayList<AtDataModel> getPendingAttendanceObj() {
        ArrayList<AtDataModel> datalist = new ArrayList<>();
        Cursor cursor = null;
        try {

            cursor = db.rawQuery("SELECT * FROM " + TABLE_ATTENDANCE+
                    " WHERE "+O.UPLOAD_STATUS +"= ?", new String[]{O.NO});

            if (cursor != null && cursor.moveToLast()) {
                do {
                    AtDataModel data=new AtDataModel(
                            String.valueOf(cursor.getInt(cursor.getColumnIndex(O.ID))),
                            cursor.getString(cursor.getColumnIndex(O.USER_ID)),
                            cursor.getString(cursor.getColumnIndex(O.USER_NAME)),
                            cursor.getString(cursor.getColumnIndex(O.USER_TYPE)),
                            cursor.getString(cursor.getColumnIndex(O.STATION_CODE)),
                            cursor.getString(cursor.getColumnIndex(O.WORK_TYPE)),
                            cursor.getString(cursor.getColumnIndex(O.WORKIN)),
                            cursor.getString(cursor.getColumnIndex(O.IO_STATUS)),
                            cursor.getString(cursor.getColumnIndex(O.LAT)),
                            cursor.getString(cursor.getColumnIndex(O.LONG)),
                            cursor.getString(cursor.getColumnIndex(O.ADDRESS)),
                            cursor.getString(cursor.getColumnIndex(O.UPLOAD_STATUS)),
                            cursor.getString(cursor.getColumnIndex(O.MACHINE_ID)),
                            cursor.getString(cursor.getColumnIndex(O.CREATED_TIME)));
                    datalist.add(data);
                }
                while (cursor.moveToPrevious());
            }
            if(cursor!=null && !cursor.isClosed()) cursor.close();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return datalist;
    }
    public AtDataModel getLastAttendance(String user_id, String shift) {
        AtDataModel data = null;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_ATTENDANCE +
                    " WHERE "+O.USER_ID+" = ? AND "+O.WORKIN +" = ? ORDER BY "+O.CREATED_TIME + " DESC LIMIT 1", new String[]{user_id, shift});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                    data=new AtDataModel(
                            String.valueOf(cursor.getInt(cursor.getColumnIndex(O.ID))),
                            cursor.getString(cursor.getColumnIndex(O.USER_ID)),
                            cursor.getString(cursor.getColumnIndex(O.USER_NAME)),
                            cursor.getString(cursor.getColumnIndex(O.USER_TYPE)),
                            cursor.getString(cursor.getColumnIndex(O.STATION_CODE)),
                            cursor.getString(cursor.getColumnIndex(O.WORK_TYPE)),
                            cursor.getString(cursor.getColumnIndex(O.WORKIN)),
                            cursor.getString(cursor.getColumnIndex(O.IO_STATUS)),
                            cursor.getString(cursor.getColumnIndex(O.LAT)),
                            cursor.getString(cursor.getColumnIndex(O.LONG)),
                            cursor.getString(cursor.getColumnIndex(O.ADDRESS)),
                            cursor.getString(cursor.getColumnIndex(O.UPLOAD_STATUS)),
                            cursor.getString(cursor.getColumnIndex(O.MACHINE_ID)),
                            cursor.getString(cursor.getColumnIndex(O.CREATED_TIME)));
            }
            if(cursor!=null && !cursor.isClosed()) cursor.close();
        } catch (Exception e) {
            Log.e(tag, "" + e);
        }
        return data;
    }


    /**
     * SQLiteOpenHelper child class to create database , table
     */
    private class MyDbHelper extends SQLiteOpenHelper {
        private Context context;

        public MyDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
            this.context = context;
        }
        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(CREATE_TABLE_FINGER);
            db.execSQL(CREATE_TABLE_USER);
            db.execSQL(CREATE_TABLE_ATTENDANCE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}

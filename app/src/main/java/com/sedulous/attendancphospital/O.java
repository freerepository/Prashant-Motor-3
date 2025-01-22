package com.sedulous.attendancphospital;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class O {

    public static  String DATE_FORMATE="yyyy-MM-dd HH:mm:ss", IN="IN", OUT="OUT", ID="id", USER_ID="user_id", USER_NAME="user_name",
            USER_TYPE="user_type", PHONE="phone",EMPLOYEEid="employee_id",DATAOFJOINING="data_of_Joining", EMAIL="email", ADDRESS="address",
            FINGER_BYTES="finger_bytes", MACHINE_ID="machine_id", IO_STATUS="io_status",
            CREATED_TIME="created_time", LAT="latitude", LONG="longitude", WORK_TYPE="work_type",
            APP_WORK_TYPE="app_work_type",
            WORKIN ="workin", LOGIN_METHOD="login_method", PASSWORD="password", FP="FP", PWD="PWD",
            BOTH="BOTH", FINGER_NAME="finger_name", UPLOAD_STATUS="upload_status",
            LTHUMB="LEFT THUMB", LINDEX="LEFT INDEX", LMIDDLE="LEFT MIDDLE",LRING="LEFT RING", LPINKY="LEFT PINKY",
            RTHUMB="RIGHT THUMB", RINDEX="RIGHT INDEX", RMIDDLE="RIGHT MIDDLE", RRING="RIGHT RING", RPINKY="RIGHT PINKY",
            STATION_CODE ="depot_code",POLICE_VERIFICATION="police_verification", ID_CARD="id_card",
            PIC_ID ="pic_id", USER_PIC="user_pic",PATH="path",SKILLED_TYPE="skilled_type",
            ADMIN="admin", USER="user", FROM="from",TASK="task", ADD="add", EDIT="edit", DATA="data",
            QUALITY="quality", MATCH="match", TIMEOUT="timeout", MINWORKHOUR="minworkhour",
            MINWORKMINUTE="minworkminute", MINEXPHOUR="minexphour", MINEXPMINUTE="minexpminute",ATTENDANCE_BY="attendance_by",
            FOLDER_CAMIMG="Camera", FOLDER_TEMP="Temp", YES="YES", NO="NO",
            DEVICE_ID="deviceid", ACTIVATION_KEY="activation_key", SHIFTRAIN="work";

    public static void savePreference(Context context, String keys, String values){
        SharedPreferences pref = context.getSharedPreferences("akmrrmsnr", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(keys, values);
        editor.apply();
    }
    public static String getPreference(Context context, String name){
        SharedPreferences pref = context.getSharedPreferences("akmrrmsnr", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String value= pref.getString(name, "");
        editor.apply();
        return  value;
    }
    public static void clearPref(Context c){
        SharedPreferences settings = c.getSharedPreferences("akmrrmsnr", Context.MODE_PRIVATE);
        settings.edit().clear().commit();
    }

    public static void savePreferenceInt(Context context, String keys, int values){
        SharedPreferences pref = context.getSharedPreferences("akmrrmsnr", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(keys, values);
        editor.apply();
    }

    public static int getPreferenceInt(Context context, String name, int default_value){
        SharedPreferences pref = context.getSharedPreferences("akmrrmsnr", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        int value= pref.getInt(name, default_value);
        editor.apply();
        return  value;
    }
    //get current date time
    public static String getDateTime(){
       Date date = new Date();
       SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMATE);
       return formatter.format(date);
    }
    public static TimeDif getTimeDef(long dtime){
        long second_factor=1000;
        long minute_factor=60*second_factor;
        long hour_factor=60*minute_factor;
        long day_factor=24*hour_factor;
        int days= (int) (dtime/day_factor);
        dtime=dtime%day_factor;
        int hours= (int) (dtime/hour_factor);
        dtime=dtime%hour_factor;
        int minutes= (int) (dtime/minute_factor);
        dtime=dtime%minute_factor;
        int seconds= (int) (dtime/second_factor);
        dtime=dtime%second_factor;
        int milis=(int)dtime;
        return new TimeDif(days, hours, minutes, seconds);
    }
    public static long getMiliFrom(int hour, int minute, int sec)
    {
        return hour*60l*60l*1000l+minute*60l*1000l+sec*1000l;
    }
    public static class TimeDif{
        int days=0, hours=0, minutes=0, seconds=0;
        public TimeDif(int days, int hours, int minutes, int seconds ){
            this.days=days; this.hours=hours; this.minutes=minutes; this.seconds=seconds;
        }
    }

    public static long getDateDiffMiliS(SimpleDateFormat format, String newDate, String oldDate) {
        try {
            return TimeUnit.MILLISECONDS.convert(format.parse(newDate).getTime() - format.parse(oldDate).getTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public static long getLongmsDate(SimpleDateFormat format, String date) {
        try {
            return TimeUnit.MILLISECONDS.convert(format.parse(date).getTime(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

//close keyboard
    public static  void closeKeyboard(Activity activity, View view){
        try {
        InputMethodManager mImm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        mImm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        if (activity.getCurrentFocus() != null)
            mImm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }catch (Exception e){
        e.printStackTrace();
    }}

    public static void deleteRecursive(File fileOrDirectory) {
      try {
          if (fileOrDirectory.isDirectory())
              for (File child : fileOrDirectory.listFiles())
                  deleteRecursive(child);

          fileOrDirectory.delete();
      }catch (Exception e){
          e.printStackTrace();
      }
    }

    public static boolean checkNetworkOld(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }
    public static boolean checkNetwork(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    }  else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)){
                        return true;
                    }
                }
            }
            else {
                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }
        Log.i("update_statut","Network is available : FALSE ");
        return false;
    }
    public static void eResponse(Context context, VolleyError error){
        Log.e("error", ""+error.getMessage());
        if (error instanceof NetworkError) {
            Toast.makeText(context, "Internet connection not available, Try again", Toast.LENGTH_SHORT).show();
        } else if (error instanceof ServerError) {
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show();
        } else if (error instanceof AuthFailureError) {
            Toast.makeText(context, "Authentication failed, Try again", Toast.LENGTH_SHORT).show();
        } else if (error instanceof ParseError) {
            Toast.makeText(context, "Try again", Toast.LENGTH_SHORT).show();
        } else if (error instanceof TimeoutError) {
            Toast.makeText(context, "Internet connection poor, Try again", Toast.LENGTH_SHORT).show();
        }
    }
    public static String getDeviceInfo() {
        return String.format("Brand : %s\nModel : %s\nVersionName : %s\nAndroidOs : %s\nAppVersionNo. : %s",
                Build.BRAND, Build.MODEL, Build.VERSION.SDK_INT, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME
        );
    }

//    public static void selectTime(Context context, final TextView textView){
//        final int hr=0, min=0;
//        TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
//            @Override
//            public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
//                Log.e("Time", "hour "+hourOfDay+"  minutes "+minutes);
//                String time = (hourOfDay < 10 ? 0 + String.valueOf(hourOfDay) : String.valueOf(hourOfDay)) + ":" + (minutes < 10 ? 0 + String.valueOf(minutes) : String.valueOf(minutes)) + ":" + "00";
//                textView.setText(time.substring(0,5));
//            }
//        };
//        TimePickerDialog timePickerDialog=new TimePickerDialog(context, R.style.CalenderTheme, timePickerListener, hr, min, false);
//        //timePickerDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        timePickerDialog.show();
//    }
//    public static void selectDate(Context context, final TextView textView) {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        DatePickerDialog dpd = new MyDatePickerDialog(context,
//                new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker arg0, int year, int month,
//                                          int day) {
//                        month++;
//                        String date = year + "-" + (month < 10 ? 0 + String.valueOf(month) : String.valueOf(month))+"-"+ (day < 10 ? 0 + String.valueOf(day) : String.valueOf(day));
//                        textView.setText(date);
//                        Log.e("date:", date);
//                    }
//                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH));
//        dpd.show();
//    }
    public static void clearImg(ImageView iv){
        iv.setImageBitmap(null);
        iv.setImageDrawable(null);
        //iv.setBackgroundResource(0);
        //iv.setForeground(null);
    }
    public static String getEncId(String id){
        String s="9"+id;
        int n = Integer.parseInt(s);
        String result="";
        while (n>0){
            result=decTo36(n%36)+result;
            n = n/36;
        }
        return result;
    }
    public static String getDecId(String decid){
        int n = 0;
        int result=0;
        while (decid.length()>0){
            int nch = decFrom36(decid.charAt(decid.length()-1));
            result = result+nch*(int)Math.pow(36, n);
            n++;
            decid=decid.substring(0, decid.length()-1);
        }
        return String.valueOf(result).substring(1);
    }

    public static String decTo36(int n){
        if(n<36) {
            String s = "abcdefghijklmnopqrstuvwxyz";
            if (n < 10) return String.valueOf(n);
            else {
                return String.valueOf(s.charAt(n-10));
            }
        }else{
            return null;
        }
    }
    public static int decFrom36(char chexa){
        if(Character.isDigit(chexa)) {
            return Integer.parseInt(String.valueOf(chexa));
        } else if(Character.isLetter(chexa)){
            String s = "abcdefghijklmnopqrstuvwxyz";
            return s.indexOf(chexa)+10;
        } else {
            return 0;
        }
    }
    public static boolean isVersionUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return true;
        } else {
           return false;
        }
    }
    public static Uri getImageUriFromPath(Context context, String path){
        Uri uri=null;
        try {
            if(isVersionUp()) {
                uri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), path, "pamphlet", "Step Up"));
            } else{
                uri = Uri.parse("file://" + path);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return uri;
    }
    public static String getPathFromUri(Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst() ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close();
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }
    public static Uri getUriFromPath(Context context, String path){
        Uri uri=null;
        try {
            if(isVersionUp()) {
                uri = Uri.fromFile(new File(path));
            } else{
                uri = Uri.parse("file://" + path);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return uri;
    }
    public static boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    public static BitmapFactory.Options getPixel(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options;
    }
    public static int getSize(String path){
        try{
            File file = new File(path);
            long length = file.length();
            length = length/1024;
            return (int)length;
        }catch(Exception e){
            System.out.println("File not found : " + e.getMessage() + e);
        }
        return 0;
    }

    public static class Resolution{
      public float width=0;
      public float height =0;
      public Resolution(float width, float height){
          this.width=width;
          this.height=height;
      }
    }

    public static boolean isValid(String urlString) {
        try {
            if(urlString.contains("/")) {
                if (nthIndexOf(urlString, '/', 3) > 0)
                    urlString = urlString.substring(0, nthIndexOf(urlString, '/', 3));
                Log.e("url invalied", "" + urlString);
            }
            //new URL(urlString).toURI();
            if( URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches())
            return true;
            else return false;
        } catch (Exception e) {
            Log.e("url invalied", ""+e);
            return false;
        }
    }

    public static int nthIndexOf(String s, char c, int n) {
        int i = -1;
        while (n-- > 0) {
            i = s.indexOf(c, i + 1);
            if (i == -1)
                break;
        }
        return i;
    }
    public static Bitmap getResizeBitmap(Bitmap bitmap, int newWidth, int newHeight){
        return  Bitmap.createScaledBitmap( bitmap, newWidth, newHeight, false);
    }

    public static byte[] getBytes(String path){
        File file = new File(path);
        byte[] b = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(b);
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        }
        catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        }
        return b;
    }
//    public static  byte[] getByteVid(Context context, Uri uri) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        try {
//            InputStream iStream =   context.getContentResolver().openInputStream(uri);
//            int bufferSize = 2048;
//            byte[] buffer = new byte[bufferSize];
//
//            // we need to know how may bytes were read to write them to the byteBuffer
//            int len = 0;
//            if (iStream != null) {
//                while ((len = iStream.read(buffer)) != -1) {
//                    byteArrayOutputStream.write(buffer, 0, len);
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //  bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//        return byteArrayOutputStream.toByteArray();
//    }
    public static String getPathOld(Context context, String type, Uri uri ) {
        String result = null;
        String[] proj = { type };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close();
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }
    public static String getPath(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static String savefile(Context c, String sub_folder, Bitmap bitmap, int quality) {
        String extension = ".jpg";
        Bitmap.CompressFormat compressFormat=Bitmap.CompressFormat.JPEG;
        if(quality<0) quality=0;
        if(quality>=100){
            quality=100;
            extension = ".png";
            compressFormat=Bitmap.CompressFormat.PNG;
        }

        File f=new File(c.getExternalFilesDir(null).getAbsolutePath(), sub_folder);
        if(!f.exists()) f.mkdir();
        String destinationFilename = f.getAbsolutePath()+File.separator+System.currentTimeMillis()+extension;
        FileOutputStream out=null;
        try {
            out = new FileOutputStream(destinationFilename);
            bitmap.compress(compressFormat, quality, out); // bmp is your Bitmap instance
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return destinationFilename;
    }
    public static String cameraProcess(Activity c, int request_code){
        String myfolder= ((Context)c).getExternalFilesDir(null).getAbsolutePath()+File.separator+O.FOLDER_TEMP;
        File f=new File(myfolder);
        if(!f.exists()){
            if(!f.mkdir()){
                Toast.makeText(c, myfolder+" can't be created.", Toast.LENGTH_SHORT).show();
            }}
        String mImageName = "PIC" + new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date()) + ".jpg";
        File tempfile = new File(f, mImageName);

        Uri outputTempFileUri = FileProvider.getUriForFile(c.getApplicationContext(), c.getApplicationContext().getPackageName() + ".provider", tempfile);
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputTempFileUri);
        c.startActivityForResult(intent, request_code);
        return tempfile.getAbsolutePath();
    }
    public static byte[] getByteFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    public static Bitmap reduceScale(String path, int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        Bitmap srcBitmap=null;
        InputStream inputStream=null;
        try{
            inputStream = (InputStream) (new FileInputStream(path));
            int h=options.outHeight;
            int w=options.outWidth;
            int multiplex=1;
            while (h>maxHeight){
                h/=2;
                multiplex*=2;
            }
            options.inSampleSize = multiplex;
            options.inJustDecodeBounds = false;
            srcBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return srcBitmap;
    }

    public static void showInfo(Context context, String text){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        TextView textView=new TextView(context);
        ViewGroup.LayoutParams layoutParams=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        textView.setText(text);
        textView.setPadding(16,16,16,16);
        dialog.setContentView(textView);
        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }
    public static void writeToText(Context context, String data, String filename) {
        try {
            File file = new File(context.getExternalFilesDir(null), filename+".txt");
            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(data.getBytes());
            } finally {
                stream.close();
            }
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    public static String getVideoPathFromURI(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    private static String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    public class DecimalDigitsInputFilter implements InputFilter {
        private int mDigitsBeforeZero;
        private int mDigitsAfterZero;
        private Pattern mPattern;

        private static final int DIGITS_BEFORE_ZERO_DEFAULT = 100;
        private static final int DIGITS_AFTER_ZERO_DEFAULT = 100;

        public DecimalDigitsInputFilter(Integer digitsBeforeZero, Integer digitsAfterZero) {
            this.mDigitsBeforeZero = (digitsBeforeZero != null ? digitsBeforeZero : DIGITS_BEFORE_ZERO_DEFAULT);
            this.mDigitsAfterZero = (digitsAfterZero != null ? digitsAfterZero : DIGITS_AFTER_ZERO_DEFAULT);
            mPattern = Pattern.compile("-?[0-9]{0," + (mDigitsBeforeZero) + "}+((\\.[0-9]{0," + (mDigitsAfterZero)
                    + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String replacement = source.subSequence(start, end).toString();
            String newVal = dest.subSequence(0, dstart).toString() + replacement
                    + dest.subSequence(dend, dest.length()).toString();
            Matcher matcher = mPattern.matcher(newVal);
            if (matcher.matches())
                return null;

            if (TextUtils.isEmpty(source))
                return dest.subSequence(dstart, dend);
            else
                return "";
        }
    }
    public static float dpToPx(Context context, float dp) {
        if (context == null) {
            return -1;
        }
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float pxToDp(Context context, float px) {
        if (context == null) {
            return -1;
        }
        return px / context.getResources().getDisplayMetrics().density;
    }
    public static void showDialog(Context c, String message){
        new AlertDialog.Builder(c)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}

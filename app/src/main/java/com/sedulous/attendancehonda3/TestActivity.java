package com.sedulous.attendancehonda3;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TestActivity {
    public static void main(String[] args){

        Date date = new Date();
        int day=3, hour=4, minute=53, second=23;
        date.setTime(date.getTime()-day*24*60*60*1000-hour*60*60*1000-minute*60*1000-second*1000);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd hh:mm:ss");
        String t_old=formatter.format(date);
        String t_now=O.getDateTime();
        System.out.println("t_old  "+t_old+"      mili  "+getLongmsDate(formatter, t_old));
        System.out.println("t_now  "+t_now+"      mili  "+getLongmsDate(formatter, t_now));
        long dif_ms=getDateDiff(formatter, t_old,t_now);
        TimeDif timeDif=getTimeDef(dif_ms);
        System.out.println("time_difference_millis " +dif_ms+
                "\ndays "+timeDif.days+
                "\nhours  "+timeDif.hours+
                "\nminutes "+timeDif.minutes+
                "\nseconds "+timeDif.seconds);
        System.out.println("max "+Long.MAX_VALUE+"     "+1000l*24*60*60*1000*1000);

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
    public TimeDif getDate(){
        TimeDif timeDif=null;

        return timeDif;
    }

    public static class TimeDif{
        int days=0, hours=0, minutes=0, seconds=0;
        public TimeDif(int days, int hours, int minutes, int seconds ){
            this.days=days; this.hours=hours; this.minutes=minutes; this.seconds=seconds;
        }
    }

    public static long getDateDiff(SimpleDateFormat format, String oldDate, String newDate) {
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
}

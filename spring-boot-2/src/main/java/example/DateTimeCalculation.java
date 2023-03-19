package example;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateTimeCalculation {
    public String calculateHours(String start_date, String end_date){
        // SimpleDateFormat converts the string format to date object
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);
            //Calculate time difference in milliseconds
            long difference_In_Time = d2.getTime() - d1.getTime();

            long difference_In_Minutes = TimeUnit.MILLISECONDS.toMinutes(difference_In_Time) % 60;

            long difference_In_Hours = TimeUnit.MILLISECONDS.toHours(difference_In_Time) % 24;

            long difference_In_Days = TimeUnit.MILLISECONDS.toDays(difference_In_Time) % 365;

            System.out.print("Difference" + " between two dates is: ");
            System.out.println(difference_In_Hours + " hours, " + difference_In_Minutes + " minutes, ");
            double result = difference_In_Days*24.0 + difference_In_Hours*1.0 + difference_In_Minutes*1.0/60.0;
            DecimalFormat df = new DecimalFormat("#.##");

            return String.valueOf(df.format(result));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Cannot calculate hours");
    }

    public String differenceInDate(String start_date, String end_date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {

            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);

            long difference_In_Time = d2.getTime() - d1.getTime();

            long difference_In_Days = TimeUnit.MILLISECONDS.toDays(difference_In_Time) % 365;

            System.out.print("Difference" + " between two dates is: ");
            System.out.println(difference_In_Days + " day(s)");

            return String.valueOf(difference_In_Days);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Cannot calculate difference in date");
    }
    public static String addOneDayCalendar(String date) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(date));
        c.add(Calendar.DATE, 1);
        return sdf.format(c.getTime());
    }

}

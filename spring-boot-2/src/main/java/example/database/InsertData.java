package example.database;

import com.slack.api.model.User;
import example.DateTimeCalculation;

import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Date;

public class InsertData {
    String url = "jdbc:mysql://localhost:3306/epibuildstaff?useTimezone=true&serverTimezone=UTC";
    String user = "root";
    String password = "root";

    Connection connection = null;
    Statement statement = null;

    public String InsertClockIn(String date, String time, String userId, String UserFullName, String whatFor, String company, String summary) {
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();

            //Retrieve user's previous record, expecting a clock-out record
            String GetRecordByIdQuery = "SELECT * FROM staff WHERE staffId = ? ORDER BY id DESC LIMIT 1;";
            PreparedStatement GetClockInStatement = connection.prepareStatement(GetRecordByIdQuery);
            GetClockInStatement.setString(1, userId);
            ResultSet resultSet=GetClockInStatement.executeQuery();

            if(resultSet.next()) {
                //Handle if someone tried to clock in twice
                String InOrOut = resultSet.getString("InOrOut");
                String ClockOutDateTime = resultSet.getString("DateTime");
                if (InOrOut.equals("Clock In")) {
                    return "Failed to clock in, please clock out before you clock in again.";
                }
                //Avoid causing intersection between last clock out time and current clock in time
                DateTimeCalculation dtc = new DateTimeCalculation();
                Double timePeriod = Double.valueOf(dtc.calculateHours(ClockOutDateTime, date +" "+time));
                if(timePeriod<0.00){
                    return "Failed to clock in, please choose a time that is after you last clock out.";
                }
            }
            //MySQL query
            String query = "INSERT INTO staff"
                    +"(Datetime, staffId, name, InorOut, whatFor, summary, company, hours)"
                    + " values (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStmt.setString (1, date +" "+time);
            preparedStmt.setString (2, userId);
            preparedStmt.setString (3, UserFullName);
            preparedStmt.setString (4, "Clock In");
            preparedStmt.setString (5, whatFor);
            preparedStmt.setString (6, summary);
            preparedStmt.setString (7, company);
            preparedStmt.setString (8, null);

            preparedStmt.execute();
            connection.close();

        } catch (Exception e) {
            System.err.println("Got an exception from database!");
            System.err.println(e.getMessage());
        }
        return "Good";
    }
    public String InsertClockOut(String date, String time,String userId, String UserFullName, String summary){
        String message="Good";
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();

            //Retrieve user's previous record, expecting a clock-in record
            String GetRecordByIdQuery = "SELECT * FROM staff WHERE staffId = ? ORDER BY id DESC LIMIT 1;";
            PreparedStatement GetClockInStatement = connection.prepareStatement(GetRecordByIdQuery);
            GetClockInStatement.setString(1, userId);
            ResultSet resultSet=GetClockInStatement.executeQuery();

            List<String> list = new ArrayList<>();
            if(resultSet.next()) {
                String InOrOut = resultSet.getString("InOrOut");
                //Handle if someone tried to clock out twice
                if (InOrOut.equals("Clock Out")) {
                    return "Failed to clock out. I didn't find a clock-in record matches your clock-out.";
                }
                String whatFor = resultSet.getString("whatFor");
                String company = resultSet.getString("company");
                String ClockInDateTime = resultSet.getString("DateTime");
                //Store data in the list, so it can be accessed outside the if statement
                list.add(whatFor); //list index: 0
                list.add(company); //list index: 1
                list.add(ClockInDateTime);//list index: 2
            }
            String ClockOutDateTime = date +" "+time;
            DateTimeCalculation dtc = new DateTimeCalculation();
            String hours = dtc.calculateHours(list.get(2), ClockOutDateTime);

            //Handle if someone entered the wrong time that might be prior to when they clock in
            if(Double.valueOf(hours)<=0.00){
                return "Failed to clock out. You need to choose a time that is after you clocked in.";
            }

            String query = "INSERT INTO staff"
                    +"(Datetime, staffId, name, InorOut, whatFor, summary, company, hours)"
                    + " values (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStmt.setString (1, ClockOutDateTime); //Datetime
            preparedStmt.setString (2, userId);           //staffId
            preparedStmt.setString (3, UserFullName);     //name
            preparedStmt.setString (4, "Clock Out");   //InorOut
            preparedStmt.setString (5, list.get(0));      //whatFor
            preparedStmt.setString (6, summary);          //summary
            preparedStmt.setString (7, list.get(1));      //company
            preparedStmt.setString (8, hours);            //hours

            preparedStmt.execute();
            connection.close();

        } catch (Exception e) {
            System.err.println("Got an exception from database!");
            System.err.println(e.getMessage());
        }
        return "Good";
    }

}

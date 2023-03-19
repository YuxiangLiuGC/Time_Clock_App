package example.database;

import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import example.DateTimeCalculation;
import example.sheet.SheetService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RetrieveData {
    String url = "jdbc:mysql://localhost:3306/epibuildstaff?useTimezone=true&serverTimezone=UTC";
    String user = "root";
    String password = "root";

    Connection connection = null;
    Statement statement = null;

    String sheetId ="1mOJ5zz11XJzPCm-pvwQtSKTZv0R14HObwAM7kSg85AE";
    // Retrieve data from database and update on the Google sheet
    public List<List<Object>> RetrieveTodayAndUpdate(){

        List<List<Object>> list = new ArrayList<>();
        int row = 0;
        try{
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            LocalDate date = LocalDate.now();

            //Retrieve the second last record and check its date to see if we need to delete yesterday records from sheet
            //Note that "id" is the entries' id in MySQL not "userId"
            String GetSecondLastQuery = "SELECT * FROM staff ORDER BY id DESC LIMIT 1,1;";
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(GetSecondLastQuery);
            if(rs.next()){
                String YesterdayDate= rs.getString("Datetime");
                if(!YesterdayDate.startsWith(String.valueOf(date))){
                    SheetService sheetService = new SheetService();
                    sheetService.deletePreviousEntries(sheetId,"TODAY");
                }
            }
            String GetTodayQuery = "SELECT * FROM staff WHERE DateTime LIKE ? ;";
            PreparedStatement GetClockInStatement = connection.prepareStatement(GetTodayQuery);
            GetClockInStatement.setString(1,String.valueOf(date)+"%");
            ResultSet resultSet=GetClockInStatement.executeQuery();

            list.add(new ArrayList<>());
            //set the column name that will be presented on Google sheet
            Collections.addAll(list.get(0),"Datetime","staffId","name","InOrOut","whatFor","summary","company","hours");
            row++;

            while(resultSet.next()) {
                list.add(new ArrayList<>());
                String Datetime= resultSet.getString("Datetime");
                String staffId= resultSet.getString("staffId");
                String name= resultSet.getString("name");
                String InOrOut = resultSet.getString("InOrOut");
                String whatFor= resultSet.getString("whatFor");
                String summary= resultSet.getString("summary");
                String company= resultSet.getString("company");
                String hours= resultSet.getString("hours");
                Collections.addAll(list.get(list.size()-1),Datetime,staffId,name,InOrOut,whatFor,summary,company,hours);
                row++;
            }
            System.out.println("List here!");
            System.out.println(list);

            SheetService update = new SheetService();

            String range = "TODAY!A1:H"+ row;
            String inputOption="RAW";

            BatchUpdateValuesResponse response = update.batchUpdateValues(sheetId, range, inputOption, list);
            System.out.println(response);

        }catch(Exception e){
            System.err.println("Got an exception from RetrieveToday()!");
            System.err.println(e.getMessage());
        }
        return list;
    }


    public List<List<Object>> RetrieveHoursAndUpdate(String start_date, String end_date){

        Map<String, List<Object>> resultMap = new HashMap<>();
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();

            DateTimeCalculation dtc = new DateTimeCalculation();
            int total = Integer.valueOf(dtc.differenceInDate(start_date, end_date));
            int i=0;

            List<String> currentList = new ArrayList<>();
            currentList.add(start_date);

            List<List<Object>> titleList = new ArrayList<>();
            List<Object> timePeriod = new ArrayList<>();
            timePeriod.add("From: "+start_date);
            timePeriod.add("To: "+end_date);
            titleList.add(timePeriod);

            List<Object> fieldNames = new ArrayList<>();
            fieldNames.add("StaffId");
            fieldNames.add("Name");
            fieldNames.add("Hours");
            titleList.add(fieldNames);

            //Find every day's hours for each staff
            while(i<=total) {
                String getHoursQuery = "SELECT * FROM staff WHERE InOrOut='Clock Out' AND DateTime LIKE ? ;";
                PreparedStatement GetHoursStatement = connection.prepareStatement(getHoursQuery);
                GetHoursStatement.setString(1, currentList.get(0) +"%");
                ResultSet rs=GetHoursStatement.executeQuery();

                while(rs.next()){
                    List<Object> temp = new ArrayList<>();
                    String staffId = rs.getString("staffId");
                    String name = rs.getString("name");
                    String hours = rs.getString("hours");

                    if(!resultMap.containsKey(staffId)){
                        temp.add(staffId);
                        temp.add(name);
                        temp.add(hours);// index: 2
                        resultMap.put(staffId,temp);
                    }else{
                        Double previousHours = Double.valueOf((String) resultMap.get(staffId).get(2));
                        DecimalFormat df = new DecimalFormat("#.##");

                        Double sum =  Double.valueOf(hours)+Double.valueOf(previousHours);
                        resultMap.get(staffId).set(2, df.format(sum));
                    }
                }
                //Store next day into currentList to avoid reassigning string
                String nextDay = dtc.addOneDayCalendar(currentList.get(0));
                currentList.set(0,nextDay);
                i++;
            }

            List<List<Object>> OutputList = new ArrayList<>();
            if(resultMap.size()==0){
                OutputList.add(new ArrayList<>());
                OutputList.get(2).add("No data found in this time period");
            }else{
                for(String key: resultMap.keySet()){
                    OutputList.add(resultMap.get(key));
                }
            }
            List<List<Object>> mergeList = new ArrayList<>();
            mergeList.addAll(titleList);
            mergeList.addAll(OutputList);

            String row = String.valueOf(mergeList.size());

            SheetService update = new SheetService();
            update.deletePreviousEntries(sheetId,"HOURS");

            String range = "HOURS!A1:C"+ row;
            String inputOption="RAW";
            // Update google sheet
            BatchUpdateValuesResponse response = update.batchUpdateValues(sheetId, range, inputOption, mergeList);
            System.out.println(response);

            return mergeList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public List<List<Object>> RetrieveRecordsAndUpdate(String start_date, String end_date) {
        Map<String, List<List<Object>>> resultMap = new HashMap<>();
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();

            DateTimeCalculation dtc = new DateTimeCalculation();
            int total = Integer.valueOf(dtc.differenceInDate(start_date, end_date));
            int i=0;

            List<String> currentList = new ArrayList<>();
            currentList.add(start_date);

            List<List<Object>> titleList = new ArrayList<>();
            List<Object> timePeriod = new ArrayList<>();
            timePeriod.add("From: "+start_date);
            timePeriod.add("To: "+end_date);
            resultMap.put("FromTo", new ArrayList<>());
            titleList.add(timePeriod);

            List<Object> fieldNames = new ArrayList<>();
            Collections.addAll(fieldNames,"id","Datetime","staffId","name","InOrOut","whatFor","summary","company","hours");
            resultMap.put("Title", new ArrayList<>());
            titleList.add(fieldNames);

            while(i<=total) {
                String getHoursQuery = "SELECT * FROM staff WHERE DateTime LIKE ? ORDER BY id ASC;";
                PreparedStatement GetHoursStatement = connection.prepareStatement(getHoursQuery);
                GetHoursStatement.setString(1, currentList.get(0) +"%");
                ResultSet rs=GetHoursStatement.executeQuery();

                while(rs.next()){
                    List<Object> temp = new ArrayList<>();
                    String id = rs.getString("id");
                    String Datetime= rs.getString("Datetime");
                    String staffId= rs.getString("staffId");
                    String name= rs.getString("name");
                    String InOrOut = rs.getString("InOrOut");
                    String whatFor= rs.getString("whatFor");
                    String summary= rs.getString("summary");
                    String company= rs.getString("company");
                    String hours= rs.getString("hours");
                    Collections.addAll(temp, id,Datetime,staffId,name,InOrOut,whatFor,summary,company,hours);

                    if(!resultMap.containsKey(staffId)){
                        resultMap.put(staffId, new ArrayList<>());
                    }
                    resultMap.get(staffId).add(temp);

                }
                //Add one day and pass into query for next loop
                String nextDay = dtc.addOneDayCalendar(currentList.get(0));
                currentList.set(0,nextDay);
                i++;
            }

            List<List<Object>> OutputList = new ArrayList<>();
            if(resultMap.size()==0){
                OutputList.add(new ArrayList<>());
                OutputList.get(2).add("No data found in this time period");
            }else{
                for(String key: resultMap.keySet()){
                    for(List<Object> list:resultMap.get(key))
                        OutputList.add(list);
                }
            }
            List<List<Object>> mergeList = new ArrayList<>();
            mergeList.addAll(titleList);
            mergeList.addAll(OutputList);

            String row = String.valueOf(mergeList.size());

            SheetService update = new SheetService();
            update.deletePreviousEntries(sheetId,"RECORDS");

            String range = "RECORDS!A1:I"+ row;
            String inputOption="RAW";
            // Update google sheet
            BatchUpdateValuesResponse response = update.batchUpdateValues(sheetId, range, inputOption, mergeList);
            System.out.println(response);

            return mergeList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String,String> RetrieveRecordById(String userId){
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();

            String GetLastEntryByIdQuery = "SELECT * FROM staff WHERE staffId = ? ORDER BY id DESC LIMIT 1;";
            PreparedStatement GetLastEntryStatement = connection.prepareStatement(GetLastEntryByIdQuery);
            GetLastEntryStatement.setString(1, userId);
            ResultSet rs = GetLastEntryStatement.executeQuery();

            if(rs.next()){
                Map<String, String> map = new HashMap<>();
                String Datetime = rs.getString("Datetime");
                String name = rs.getString("name");
                String InOrOut = rs.getString("InOrOut");
                String whatFor = rs.getString("whatFor");
                String summary = rs.getString("summary");
                String company = rs.getString("company");

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime current = LocalDateTime.now();
                String now = dtf.format(current);
                DateTimeCalculation dtc = new DateTimeCalculation();
                Double difference = Double.valueOf(dtc.calculateHours(Datetime,now));

                if(difference>=24){
                    System.out.println("You want to find the record that is "+difference+" ago");
                    map.put("No", "No record found for past 24 hours");
                    return map;
                }

                String result = "InOrOut:  "+InOrOut+"\n"
                                +"DateTime:  "+Datetime+"\n"
                                +"Name:  "+name+"\n"
                                +"WhatFor:  "+whatFor+"\n"
                                +"Company:  "+company+"\n"
                                +"Summary:  "+summary;
                map.put(InOrOut, result);
                return map;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

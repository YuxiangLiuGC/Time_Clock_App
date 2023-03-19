package example.database;

import java.sql.*;
import java.util.*;

public class UpdateData {
    String url = "jdbc:mysql://localhost:3306/epibuildstaff?useTimezone=true&serverTimezone=UTC";
    String user = "root";
    String password = "root";
    Connection connection = null;
    Statement statement = null;

    public String updateDataFromSheet(List<List<Object>> list){
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();

            String GetEntryQuery = "SELECT * FROM staff WHERE id = ?";
            PreparedStatement GetEntryStatement = connection.prepareStatement(GetEntryQuery);
            String UpdateEntryQuery = "UPDATE staff SET Datetime=? , InOrOut=?"+
                                      " ,whatFor=? , summary=? , company=? , hours=? WHERE id = ?";
            PreparedStatement UpdateEntryStatement = connection.prepareStatement(UpdateEntryQuery);
            int count = 0;

            for(List<Object> entry: list){
                if(!isNumeric(String.valueOf(entry.get(0)))){
                    continue;
                }
                //Clock-in entry has length of 8. Clock-out entry has length of 9.
                if(entry.size()>9){
                    return "Failed. Invalid value appears nearby the entries.";
                }
                if(entry.size()==9 && entry.get(4).equals("Clock In")){
                    return "Failed. Clock-in entry cannot have hours value.";
                }
                GetEntryStatement.setString(1,String.valueOf(entry.get(0)));
                ResultSet resultSet=GetEntryStatement.executeQuery();
                if(resultSet.next()) {
                    List<Object> datebaseList = new ArrayList<>();
                    String id= resultSet.getString("id");
                    String Datetime= resultSet.getString("Datetime");
                    String staffId= resultSet.getString("staffId");
                    String name= resultSet.getString("name");
                    String InOrOut = resultSet.getString("InOrOut");
                    String whatFor= resultSet.getString("whatFor");
                    String summary= resultSet.getString("summary");
                    String company= resultSet.getString("company");
                    String hours= resultSet.getString("hours");
                    Collections.addAll(datebaseList,id,Datetime,staffId,name,InOrOut,whatFor,summary,company,hours);

                    if(!sameOrNot(datebaseList,entry)){
                        UpdateEntryStatement.setString(1,String.valueOf(entry.get(1)));//Datetime
                        UpdateEntryStatement.setString(2,String.valueOf(entry.get(4)));//InOrOut
                        UpdateEntryStatement.setString(3,String.valueOf(entry.get(5)));//whatFor
                        UpdateEntryStatement.setString(4,String.valueOf(entry.get(6)));//summary
                        UpdateEntryStatement.setString(5,String.valueOf(entry.get(7)));//company
                        if(entry.get(4).equals("Clock Out")){
                            UpdateEntryStatement.setString(6,String.valueOf(entry.get(8)));//hours
                        }else{
                            UpdateEntryStatement.setString(6,null);
                        }
                        UpdateEntryStatement.setString(7,String.valueOf(entry.get(0)));//id
                        UpdateEntryStatement.executeUpdate();
                        count++;
                        System.out.println("datebaseList here!!!");
                        System.out.println(datebaseList);
                    }
                }
            }
            if(count==0){
                return "No changes made in database";
            }else{
                return count+" row(s) has been updated in database";
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean sameOrNot(List<Object> list1,List<Object> list2){
        int shorter=0;
        if(list1.size()<list2.size()){
            shorter = list1.size();
        }else{
            shorter = list2.size();
        }
        for(int i=0; i<shorter; i++){
            if(!list1.get(i).equals(list2.get(i))){
                return false;
            }
        }
        return true;
    }
    public boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}

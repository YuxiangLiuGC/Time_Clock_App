package example.database;

import org.springframework.data.jpa.repository.Modifying;

import java.sql.*;
import java.util.Map;

public class DeleteData {

    String url = "jdbc:mysql://localhost:3306/epibuildstaff?useTimezone=true&serverTimezone=UTC";
    String user = "root";
    String password = "root";

    Connection connection = null;
    Statement statement = null;

    public String deleteLastRecord(String userId){
        try {
            connection = DriverManager.getConnection(url, user, password);
            statement = connection.createStatement();
            //Retrieve record again to get whether it is clock in or clock out
            RetrieveData rd = new RetrieveData();
            Map<String,String> map = rd.RetrieveRecordById(userId);

            String GetRecordByIdQuery = "DELETE FROM staff WHERE staffId = ? ORDER BY id DESC LIMIT 1;";
            PreparedStatement GetClockInStatement = connection.prepareStatement(GetRecordByIdQuery);
            GetClockInStatement.setString(1, userId);
            GetClockInStatement.executeUpdate();

            RetrieveData retrieveData = new RetrieveData();
            retrieveData.RetrieveTodayAndUpdate();

            for (String InOrOut : map.keySet()) {
                return "Your previous "+InOrOut+ " record has been deleted!";
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "Failed to deleted";
    }
}

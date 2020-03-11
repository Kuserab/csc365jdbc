import java.sql.*;

public class InnDatabase
{
   public static void main (String[] args)
   {
      InnDatabase db = new InnDatabase();
      db.run();
   }

   private void run()
   {
      try {
         Connection conn =  DriverManager.getConnection(
                 System.getenv("APP_JDBC_URL"),
                 System.getenv("APP_JDBC_USER"),
                 System.getenv("APP_JDBC_PW"));
         System.out.println("Connection established");
         Statement stmt = conn.createStatement();
         System.out.println("Statement formed");
         runSqlQueries(stmt);
         conn.close();
      } catch (SQLException e) {
         System.out.println("Unable to complete sql request: " + e.getMessage());
         System.exit(1);
      }
   }

   // Sample query: 'select * from Rooms
   private void runSqlQueries(Statement stmt)
   {
      try {
         ResultSet rs = stmt.executeQuery("select * from Rooms");
         while (rs.next()) {
            String id = rs.getString("RoomId");
            String name = rs.getString ("RoomName");
            int beds = rs.getInt("Beds");
            String bedtype = rs.getString("BedType");
            String maxoccupancy = rs.getString("MaxOccupancy");
            float baseprice = rs.getFloat("BasePrice");
            String decor = rs.getString("Decor");

            System.out.format("%s, %s, %d, %s, %s, %f, %s\n",
                    id, name, beds, bedtype, maxoccupancy, baseprice, decor);
         }
      } catch (SQLException e) {
         System.out.println("Unable to complete sql request: " + e.getMessage());
         System.exit(1);
      }
   }
}

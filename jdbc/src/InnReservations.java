import java.sql.*;

public class InnReservations
{
   public static void main (String[] args)
   {
      InnReservations db = new InnReservations();
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
         runSqlQueries(conn);
         conn.close();
      } catch (SQLException e) {
         System.out.println("Unable to complete sql request: " + e.getMessage());
         System.exit(1);
      }
   }

   private void runSqlQueries(Connection conn)
   {
      sampleQuery(conn);
      // run RoomsAndDates FR1

      // run Reservations FR2

      // run ReservationChange FR3

      // run ReservationCancellation FR4

      // run ReservationDetails FR5

      // run Revenue FR6
   }

   private void sampleQuery(Connection conn)
   {
      try {
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("select * from lab7_rooms");
         while (rs.next()) {
            String code = rs.getString("RoomCode");
            String name = rs.getString ("RoomName");
            int beds = rs.getInt("Beds");
            String bedtype = rs.getString("BedType");
            String maxoccupancy = rs.getString("MaxOcc");
            float baseprice = rs.getFloat("BasePrice");
            String decor = rs.getString("Decor");

            System.out.format("%s, %s, %d, %s, %s, %f, %s\n",
                    code, name, beds, bedtype, maxoccupancy, baseprice, decor);
         }
      } catch (SQLException e) {
         System.out.println("Unable to complete sql request: " + e.getMessage());
         System.exit(1);
      }
   }
}

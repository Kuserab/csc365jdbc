import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

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
      fr5(conn);
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

   private void fr5(Connection conn) {
      try {
         System.out.println("== Welcome to FR5 ==");
         Scanner scanner = new Scanner(System.in);
         System.out.print("First name: ");
         String firstName = scanner.nextLine();
         System.out.print("Last name: ");
         String lastName  = scanner.nextLine();
         System.out.print("Checkin: ");
         String checkInString = scanner.nextLine();
         System.out.print("Checkout: ");
         String checkOutString = scanner.nextLine();
         System.out.print("Room Code: ");
         String room = scanner.nextLine();
         System.out.print("Reservation Code: ");
         String resCode = scanner.nextLine();

         firstName = ((firstName.trim().length() <= 0) ? "%" : "%" + firstName + "%");
         lastName = ((lastName.trim().length() <= 0) ? "%" : "%" + lastName + "%");
         if(checkInString.trim().length() <= 0) {
            checkInString = "%";
         }
         if(checkOutString.trim().length() <= 0) {
            checkOutString = "%";
         }
         room = ((room.trim().length() <= 0) ? "%" : "%" + room + "%");
         resCode = ((resCode.trim().length() <= 0) ? "%" : "%" + resCode + "%");

         PreparedStatement pstmt = conn.prepareStatement(
                 "select * from egarc113.lab7_reservations " +
                         "inner join egarc113.lab7_rooms on Room like RoomCode " +
                         "where FirstName like ? and LastName like ? " +
                         "and CheckIn like ? and CheckOut like ? " +
                         "and Room like ? and CODE like ?;"
         );
         pstmt.setString(1, firstName);
         pstmt.setString(2, lastName);
         if(!checkInString.equals("%")) {
            LocalDate checkIn = LocalDate.parse(checkInString);
            pstmt.setDate(3, java.sql.Date.valueOf(checkIn));
         }
         else {
            pstmt.setString(3, checkInString);
         }
         if(!checkOutString.equals("%")) {
            LocalDate checkOut = LocalDate.parse(checkOutString);
            pstmt.setDate(4, java.sql.Date.valueOf(checkOut));
         }
         else {
            pstmt.setString(4, checkOutString);
         }
         pstmt.setString(5, room);
         pstmt.setString(6, resCode);

         ResultSet rs = pstmt.executeQuery();
         while (rs.next()) {
            String code = rs.getString("CODE");
            String roomabbr = rs.getString ("Room");
            String roomname = rs.getString("RoomName");
            String checkin = rs.getString("CheckIn");
            String checkout = rs.getString("CheckOut");
            double rate = rs.getDouble("Rate");
            String lname = rs.getString("LastName");
            String fname = rs.getString("FirstName");
            int adults = rs.getInt("Adults");
            int kids = rs.getInt("Kids");
            System.out.format("%s, %s, %s, %s, %s, %s, %s, %f, %d, %d\n",
                    code, roomabbr, roomname, lname, fname, checkin, checkout, rate, adults, kids);
         }
      } catch (SQLException e) {
         System.out.println("Unable to complete sql request: " + e.getMessage());
         System.exit(1);
      }
   }
}

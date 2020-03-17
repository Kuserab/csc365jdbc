import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.ArrayList;
import java.time.temporal.ChronoUnit;
import java.time.DayOfWeek;


public class AltRooms
{
   /* tries to find other rooms based on similarity */
   public static void possiblities(Connection conn, ArrayList<Room> options, LocalDate checkIn, LocalDate checkOut, String room, String bed, int occ)
   {
   
      System.out.println("No available rooms that fit your exact preference.");
      System.out.println("Checking other similar rooms... ");
      checkRoomTypes(conn, options, checkIn, checkOut, occ);
   }
   
   /* ignores requested room and checks other rooms based on dates of stay */
   private static void checkRoomTypes(Connection conn, ArrayList<Room> options, LocalDate checkIn, LocalDate checkOut, int occ)
   {
      try {
         PreparedStatement pstmt = conn.prepareStatement
         (
            "WITH notAvail AS (select * from egarc113.lab7_reservations "
                  + "inner join egarc113.lab7_rooms on Room like RoomCode "
                  + "AND ((? BETWEEN CheckIn and Checkout) OR (? BETWEEN CheckIn and CheckOut))) "
                  
            + "SELECT * from egarc113.lab7_rooms WHERE (RoomCode, bedType) not in (SELECT RoomCode, bedType FROM notAvail)"
            + "AND maxOcc>=(?);"   
         );
         int i = 1;
         pstmt.setDate(i++, java.sql.Date.valueOf(checkIn));
         pstmt.setDate(i++, java.sql.Date.valueOf(checkOut));
         pstmt.setInt(i++, occ);

         ResultSet rs = pstmt.executeQuery();
         i = options.size();
         while (rs.next() && options.size()<=5) {
            Room roomRow = new Room(rs.getString("RoomCode"),
               rs.getString("RoomName"),
               rs.getInt("Beds"),
               rs.getString("bedType"),
               rs.getInt("maxOcc"),
               rs.getDouble("basePrice"),
               rs.getString("decor"));
            options.add(roomRow);
            System.out.format("%d. %s\n",i++, roomRow.getRoomCode());
         }
      }catch (SQLException e) {
         System.out.println("Unable to complete sql request: " + e.getMessage());
         System.exit(1);
      }
   }
}
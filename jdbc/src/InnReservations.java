import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.ArrayList;

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
      String input = "";
      Scanner scanner = new Scanner(System.in);
      while (!input.startsWith("q"))
      {
         System.out.print ("Enter a query to run [FR<n> | q]: ");
         input = scanner.nextLine();
         switch (input.toUpperCase()) {
            case "FR1":
               fr1(conn);
               break;
            case "FR2":
               fr2(conn);
               break;
            case "FR3":
               fr3(conn);
               break;
            case "FR4":
               fr4(conn);
               break;
            case "FR5":
               fr5(conn);
               break;
            case "FR6":
               fr6(conn);
               break;
         }
      }
      scanner.close();
      System.out.println("Goodbye");
   }

   private void fr1(Connection conn) {
      System.out.println("== Welcome to FR1 ==");
   }

   private String checkStringLen(Scanner scanner, String printMsg)
   {
      String res = scanner.nextLine();
   
      while (res.length()<=0)
      {
         System.out.println("Input is required. Please try again.");
         System.out.print(printMsg);
         res=scanner.nextLine();
      }
   
      return res;
   }
   private void fr2(Connection conn) {
      try {
         System.out.println("== Welcome to FR2 ==");   
         Scanner scanner = new Scanner(System.in);
         System.out.print("First name: ");
         String firstName = checkStringLen(scanner,"First name: ");
         System.out.print("Last name: ");
         String lastName  = checkStringLen(scanner,"Last name: ");
         System.out.print("Room Code: ");
         String room = scanner.nextLine();
         System.out.print("Bed type: ");
         String bed = scanner.nextLine();
         System.out.print("Checkin: ");
         String checkInString = checkStringLen(scanner,"Checkin: ");
         System.out.print("Checkout: ");
         String checkOutString = checkStringLen(scanner,"Checkout: ");
         System.out.print("Number of children: ");
         int numKids = scanner.nextInt();
         System.out.print("Number of adults: ");
         int numAdults = scanner.nextInt();

         /* Trim user input */
         firstName = firstName.trim();
         lastName = lastName.trim();
         bed = ((bed.trim().length() <= 0 || bed.toUpperCase().equals("ANY")) ? "%" : bed);
         room = ((room.trim().length() <= 0 || room.toUpperCase().equals("ANY")) ? "%" : room);
         String bedPref = ((bed.equals("%")) ? "" : " AND bedType = ?"); 
         String roomPref = ((room.equals("%")) ? "" : " AND RoomCode = ? ");
         
         /* Queries for rooms that are available according to customer's input */
         PreparedStatement pstmt = conn.prepareStatement
         (
            "WITH notAvail AS (select * from egarc113.lab7_reservations "
                    + "inner join egarc113.lab7_rooms on Room like RoomCode "
                    + "AND ((? BETWEEN CheckIn  and Checkout) OR (? BETWEEN CheckIn and CheckOut))) "
                    
            + "SELECT * from egarc113.lab7_rooms WHERE (RoomCode, bedType) not in (SELECT RoomCode, bedType FROM notAvail)"
            + bedPref + roomPref 
            + "AND maxOcc>=(? + ?);"   
         );
   
         int i = 1;
         LocalDate checkIn = LocalDate.parse(checkInString);
         pstmt.setDate(i++, java.sql.Date.valueOf(checkIn));
         LocalDate checkOut = LocalDate.parse(checkOutString);
         pstmt.setDate(i++, java.sql.Date.valueOf(checkOut));

         if (!bed.equals("%"))
            pstmt.setString(i++, bed);
         if (!room.equals("%"))
            pstmt.setString(i++, room);
         pstmt.setInt(i++, numAdults);
         pstmt.setInt(i++, numKids);

         ResultSet rs = pstmt.executeQuery();
         System.out.println("\nAvailable Rooms: ");
         ArrayList<Room> options = new ArrayList<Room>();
         options.add(new Room("empty","empty",0,"empty",0,0,"empty"));
         i = 0;
         while (rs.next()) {
            Room roomRow = new Room(rs.getString("RoomCode"),
               rs.getString("RoomName"),
               rs.getInt("Beds"),
               rs.getString("bedType"),
               rs.getInt("maxOcc"),
               rs.getDouble("basePrice"),
               rs.getString("decor"));
            options.add(roomRow);
            System.out.format("%d. %s\n",++i,roomRow.getRoomCode());
         }
         System.out.print("Select a room number (or select 0 to return: ");
         int option = scanner.nextInt();
         while (option>=options.size())
         {
            System.out.println("Invalid option. Please try again.");
            System.out.print("Select a room number (or select 0 to return): ");
            option=scanner.nextInt();
         }
         
         if(option==0)
            return;
         else
         {
            System.out.println("Confirmation of reservation request: ");
            System.out.format("First name: %s\nLast name: %s\n" 
               + "Room code: %s\nRoom name: %s\nBed type: %s\n"
               + "Number of adults: %d\nNumber of children: %d\n"
               , firstName,lastName
               , options.get(i).getRoomCode()
               , options.get(i).getRoomName()
               , options.get(i).getBedType()
               , numAdults, numKids);
         }
      }
   
      catch (SQLException e) 
      {
         System.out.println("Unable to complete sql request: " + e.getMessage());
         System.exit(1);
      }
   }

   private void fr3(Connection conn) {
      System.out.println("== Welcome to FR3 ==");
   }

   private void fr4(Connection conn) {
      System.out.println("== Welcome to FR4 ==");
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

   private void fr6(Connection conn) {
      System.out.println("-- Welcome to FR6 --");
   }
}

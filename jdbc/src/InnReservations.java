import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.ArrayList;
import java.time.temporal.ChronoUnit;
import java.time.DayOfWeek;

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

   private int checkInt(Scanner scanner)
   {
      int result = 0;
      try {
         result = Integer.parseInt(scanner.nextLine());
      } catch (NumberFormatException e) {
         e.printStackTrace();
      }
      return result;

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
         int numKids = checkInt(scanner);
         System.out.print("Number of adults: ");
         int numAdults = checkInt(scanner);

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

         /* shows available rooms after query */
         ResultSet rs = pstmt.executeQuery();
         System.out.println("\nChecking available rooms... ");
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

         if (options.size() < 2)
            AltRooms.possiblities(conn, options, checkIn, checkOut, room, bed
            , (numKids+numAdults));

         if (options.size() < 2){
            System.out.println("No other room found due to exceeding max occupancy");
            return;
         }

         /* give user option to choose */
         System.out.print("Select a room number (or select 0 to return): ");
         int option = checkInt(scanner);
         while (option>=options.size())
         {
            System.out.println("Invalid option. Please try again.");
            System.out.print("Select a room number (or select 0 to return): ");
            option = checkInt(scanner);
         }
         
         if(option==0)
            return; /* user decided not to choose from rooms, returns to main menu */
         else
         {
            /* finds the number of weekdays and weekends between dates of stay */
            long days = ChronoUnit.DAYS.between(checkIn,checkOut);
            LocalDate temp = checkIn;
            int weekdays = 0;
            int weekends = 0;
            for(int j =0; j<=days; j++)
            {
               DayOfWeek day = temp.getDayOfWeek();
               if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY)
                  weekends++;
               else
                  weekdays++;
               temp = temp.plusDays(1);
            }

            /* calculate cost of stay */
            double weekdayRate = weekdays * options.get(option).getBasePrice();
            double weekendRate = weekends * 1.1 * options.get(option).getBasePrice();
            double tax = 0.18 * (weekdayRate + weekendRate) ;
            double total = weekdayRate + weekendRate + tax;

            /* shows confirmation info  */
            System.out.println("\nConfirmation of reservation request: ");
            System.out.format("First name: %s\nLast name: %s\n" 
               + "Room code: %s\nRoom name: %s\nBed type: %s\n"
               + "Number of adults: %d\nNumber of children: %d\n"
               + "Calculated cost of stay:\n"
               + "  Weekday rate: %.2f\n"
               + "  Weekend rate: %.2f\n"
               + "  Tax:          %.2f\n"
               + "  TOTAL:        %.2f\n"
               , firstName,lastName
               , options.get(option).getRoomCode()
               , options.get(option).getRoomName()
               , options.get(option).getBedType()
               , numAdults, numKids
               , weekdayRate, weekendRate, tax, total);
            
            System.out.print("Confirm reservation? (YES or NO) : ");
            String confirm = scanner.nextLine();
            
            /* if confirmed, create entry in table */
            if (confirm.toUpperCase().equals("YES"))
            {
               /* finds max reservation code, increment */
               PreparedStatement pstmtMax = conn.prepareStatement (
                  "SELECT MAX(CODE) as max from egarc113.lab7_reservations;"
               );

               ResultSet rs2 = pstmtMax.executeQuery();
               int maxResCode=0;
               while (rs2.next()) {
                  maxResCode = rs2.getInt("max") + 1;
               }

               /* insert entry into reservations table */
               String ins = "INSERT INTO egarc113.lab7_reservations" 
               + " (CODE, Room, CheckIn, Checkout, Rate, LastName, FirstName, Adults, Kids)"
               + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";
              
               i=1;
               try (PreparedStatement pstmtIns = conn.prepareStatement (ins)) {
                  pstmtIns.setInt(i++,maxResCode);
                  pstmtIns.setString(i++, options.get(option).getRoomCode());
                  pstmtIns.setDate(i++, java.sql.Date.valueOf(checkIn));
                  pstmtIns.setDate(i++, java.sql.Date.valueOf(checkOut));
                  pstmtIns.setDouble(i++, total);
                  pstmtIns.setString(i++, lastName);
                  pstmtIns.setString(i++, firstName);
                  pstmtIns.setInt(i++, numAdults);
                  pstmtIns.setInt(i++, numKids);
                  int rowCount = pstmtIns.executeUpdate();
                  System.out.println("Reservation reserved!\nReturning to the main menu..");
               }
               catch (SQLException e) {
                  System.out.println("Unable to complete sql insert: " + e.getMessage());
                  System.exit(1);
               }
            }
            else if(confirm.toUpperCase().equals("NO")) {
               System.out.println("Reservation request cancelled. Returning to the main menu..");
            }
            else {
               System.out.println("Not a valid input. Reservation request cancelled."
                  + " Returning to the main menu..");
            }
         }
      }
      catch (SQLException e) {
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

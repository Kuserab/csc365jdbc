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

   private void runSqlQueries(Connection conn) throws SQLException
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

   private void fr1(Connection conn) throws SQLException 
   {
      System.out.println("== Welcome to FR1 ==");
      
      PreparedStatement pstmt = conn.prepareStatement
      (
         "with daysInRange as (\n"
         + "select room, CheckIn, CheckOut\n"
         + "from egarc113.lab7_reservations resv\n"
         + "where CheckIn between DATE_SUB(CURDATE(), INTERVAL 180 DAY) and CURDATE()\n"
         + "), numDays as (\n"
         + "select room, DATEDIFF(CheckOut, CheckIn) daysOcc\n"
         + "from daysInRange\n"
         + "), daysOccByRoom as (\n"
         + "select room, SUM(daysOcc) daysOcc\n"
         + "from numDays\n"
         + "group by room\n"
         + "), nextDay as (\n"
         + "select room, DATE_ADD(MAX(CheckOut), INTERVAL 1 DAY) available\n"
         + "from egarc113.lab7_reservations resv\n"
         + "group by room\n"
         + "), stayBeforeToday as (\n"
         + "select room, CheckOut, DATEDIFF(CheckOut, CheckIn) daysOcc\n"
         + "from daysInRange\n"
         + "where CheckOut < CURDATE()\n"
         + "), mostRecStay as (\n"
         + "select room, MAX(CheckOut) mostRecCheckOut\n"
         + "from stayBeforeToday\n"
         + "group by room\n"
         + "), mostRecVisit as (\n"
         + "select room, CheckOut, daysOcc\n"
         + "from stayBeforeToday\n"
         + "where (room, CheckOut) in (select * from mostRecStay)\n"
         + ")\n"
         + "select RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor, \n"
         + "ROUND((occ.daysOcc / 180), 2) popularity, available,\n"
         + "mRV.daysOcc numDaysMostRecStay, CheckOut mostRecCheckOut\n"
         + "from egarc113.lab7_rooms r\n"
         + "join daysOccByRoom occ on RoomCode = occ.room\n"
         + "join nextDay nD on RoomCode = nD.room\n"
         + "join mostRecVisit mRV on RoomCode = mRV.room\n"
         + "order by popularity desc\n"
      );

      ResultSet rs = pstmt.executeQuery();
      String roomCode;
      String roomName;
      int beds;
      String bedType;
      int maxOcc;
      int basePrice;
      String decor;
      float popularity;
      Date available;
      int numDaysMostRecStay;
      Date mostRecCheckOut;

      System.out.println("RoomCode  RoomName                  Beds  bedType  maxOcc  basePrice  decor        popularity  available    numDaysMostRecStay  mostRecCheckOut");
      while (rs.next())
      {
         try {
            roomCode = rs.getString("RoomCode");
            roomName = rs.getString("RoomName");
            beds = rs.getInt("Beds");
            bedType = rs.getString("bedType");
            maxOcc = rs.getInt("maxOcc");
            basePrice = rs.getInt("basePrice");
            decor = rs.getString("decor");
            popularity = rs.getFloat("popularity");
            available = rs.getDate("available");
            numDaysMostRecStay = rs.getInt("numDaysMostRecStay");
            mostRecCheckOut = rs.getDate("mostRecCheckOut");
            System.out.format("%s       %-25s   %d   %-6s   %-5d   %-8d   %-12s   %-7.2f   %s   %-17d   %s\n",
                              roomCode, roomName, beds, bedType, maxOcc, basePrice, decor, popularity, available.toString(), numDaysMostRecStay, mostRecCheckOut.toString());
         } catch (SQLException e) {
            System.out.println("Unable to display popularity table");
            throw e;
         }
      }
      
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
      Scanner scanner = new Scanner(System.in);
      System.out.print("Enter a reservation code: ");

      try {
         int code = Integer.parseInt(scanner.nextLine().trim());
         PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT * from egarc113.lab7_reservations WHERE CODE = ?"
         );
         pstmt.setInt(1, code);
         ResultSet rs = pstmt.executeQuery();
         String input;

         if (!rs.isBeforeFirst()) {
            System.out.println("No reservation with given code found");
            pstmt.close();
            return;
         }
         Reservation res = new Reservation(rs);
         System.out.println("Enter updated information or indicate 'No change'");
         System.out.print("First name: "); // CONRAD SELBIG, 1 adult 0 children
         if (!(input = scanner.nextLine()).equalsIgnoreCase("no change")) {
            res.setFirstname(input);
         }
         System.out.print("Last name: ");
         if (!(input = scanner.nextLine()).equalsIgnoreCase("no change")) {
            res.setLastname(input);
         }
         System.out.print("Checkin: ");
         if (!(input = scanner.nextLine()).equalsIgnoreCase( "no change")) {
            res.setCheckin(Date.valueOf(input));
         }
         System.out.print("Checkout: ");
         if (!(input = scanner.nextLine()).equalsIgnoreCase("no change")){
            res.setCheckout(Date.valueOf(input));
         }
         System.out.print("# of children: ");
         if (! (input = scanner.nextLine()).equalsIgnoreCase("no change")) {
            res.setKids(Integer.parseInt(input));
         }
         System.out.print("# of adults: ");
         if (! (input = scanner.nextLine()).equalsIgnoreCase("no change")) {
            res.setAdults(Integer.parseInt(input));
         }

         if (res.isValidTimeSlot(conn)) {
            pstmt = conn.prepareStatement(
                    "UPDATE lab7_reservations " +
                            "SET FirstName = ?, LastName = ?, CheckIn = ?, CheckOut = ?, Kids = ?, Adults = ? " +
                            "WHERE Code = ?"
            );
            pstmt.setString(1, res.getFirstname());
            pstmt.setString(2, res.getLastname());
            pstmt.setDate(3, res.getCheckin());
            pstmt.setDate(4, res.getCheckout());
            pstmt.setInt(5, res.getKids());
            pstmt.setInt(6, res.getAdults());
            pstmt.setInt(7, res.getCode());
            pstmt.execute();
            pstmt.close();
            System.out.println("Reservation details have been updated!");
            System.out.println(res);
         }
         else {
            pstmt.close();
            System.out.println("Could not complete request: new reservation time conflicts with another");
         }

      } catch (SQLException e) {
         System.out.println("Unable to complete sql request: " + e.getMessage());
         System.exit(1);
      } catch (NumberFormatException e) {
         System.out.println("Input must be numerical");
      } catch (IllegalArgumentException e) {
         System.out.println("Invalid date");
      }
   }

   private void fr4(Connection conn) {
      System.out.println("== Welcome to FR4 ==");
      Scanner scanner = new Scanner(System.in);
      System.out.print("Enter a reservation code: ");

      try {
         int code = Integer.parseInt(scanner.nextLine().trim());
         PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT * from lab7_reservations WHERE CODE = ?"
         );
         pstmt.setInt(1, code);
         ResultSet rs = pstmt.executeQuery();

         if (!rs.isBeforeFirst()) {
            System.out.println("No reservation with given code found");
            pstmt.close();
            return;
         }
         Reservation res = new Reservation(rs);
         System.out.format ("Enter 'confirm' to Confirm cancellation of reservation %d under %s %s\n",
                 res.getCode(), res.getFirstname(), res.getLastname());
         if (scanner.nextLine().trim().equalsIgnoreCase("confirm")) {
            pstmt = conn.prepareStatement(
                    "DELETE FROM lab7_reservations WHERE CODE = ?"
            );
            pstmt.setInt(1, code);
            pstmt.execute();
            System.out.println("Reservation has been deleted!");
         }
         pstmt.close();
      }
      catch (SQLException e) {
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

   private void fr6(Connection conn) {
           System.out.println("-- Welcome to FR6 --");

      Scanner scanner = new Scanner(System.in);
      System.out.print("What year do you want to find the revenue of? Enter: ");

      try {
         int inYear = Integer.parseInt(scanner.nextLine().trim());
         pstmt = conn.prepareStatement(
                    "select RoomName, " +
"ROUND(ifnull(SUM(January),0),0) AS January," +
"ROUND(ifnull(SUM(February),0),0) AS February, " +
"ROUND(ifnull(SUM(March),0),0) AS March, " +
"ROUND(ifnull(SUM(April),0),0) AS April, " +
"ROUND(ifnull(SUM(May),0),0) AS May, " +
"ROUND(ifnull(SUM(June),0),0) AS June, " +
"ROUND(ifnull(SUM(July),0),0) AS July, " +
"ROUND(ifnull(SUM(August),0),0) AS August, " +
"ROUND(ifnull(SUM(September),0),0) AS September, " +
"ROUND(ifnull(SUM(October),0),0) AS October, " +
"ROUND(ifnull(SUM(November),0),0) AS November, " +
"ROUND(ifnull(SUM(December),0),0) AS December," +
"ROUND(SUM(ifnull(January, 0.00) + ifnull(February,0.00)+ ifnull(March,0.00) +" +
"ifnull(April,0.00) + ifnull(May ,0.00)+ ifnull(June,0.00) + " +
"ifnull(July,0.00) + ifnull(August,0.00)" +
"+ ifnull(September,0.00) + ifnull(October,0.00) + " +
"ifnull(November,0.00) + ifnull(December,0.00)),0) AS Totals FROM" +
"(" +
"select RoomName," +
"CASE" +
"    WHEN 1 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-01-01'" +
"        AND CheckOut <= '" + inYear + "-02-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-01-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-01-01'" +
"        AND CheckOut > '" + inYear + "-02-01'" +
"        THEN (DATEDIFF('" + inYear + "-02-01', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-01-01'" +
"        AND CheckOut > '" + inYear + "-02-01'" +
"        THEN (DATEDIFF('" + inYear + "-02-01', '" + inYear + "-01-01')*Rate)" +
"    END" +
"    " +
"END AS January," +
"CASE" +
"    WHEN 2 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-02-01'" +
"        AND CheckOut <= '" + inYear + "-03-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-02-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-02-01'" +
"        AND CheckOut > '" + inYear + "-03-01'" +
"        THEN (DATEDIFF('" + inYear + "-03-01', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-02-01'" +
"        AND CheckOut > '" + inYear + "-03-01'" +
"        THEN (DATEDIFF('" + inYear + "-03-01', '" + inYear + "-02-01')*Rate)" +
"    END  " +
"END AS February," +

"CASE" +
"    WHEN 3 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-03-01'" +
"        AND CheckOut <= '" + inYear + "-04-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-03-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-03-01'" +
"        AND CheckOut > '" + inYear + "-04-01'" +
"        THEN (DATEDIFF('" + inYear + "-04-01', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-03-01'" +
"        AND CheckOut > '" + inYear + "-04-01'" +
"        THEN (DATEDIFF('" + inYear + "-04-01', '" + inYear + "-03-01')*Rate)" +
"    END" +
"    " +
"END AS March," +
"CASE" +
"    WHEN 4 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-04-01'" +
"        AND CheckOut <= '" + inYear + "-05-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-04-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-04-01'" +
"        AND CheckOut > '" + inYear + "-05-01'" +
"        THEN (DATEDIFF('" + inYear + "-05-01', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-04-01'" +
"        AND CheckOut > '" + inYear + "-05-01'" +
"        THEN (DATEDIFF('" + inYear + "-05-01', '" + inYear + "-04-01')*Rate)" +
"    END" +
"    " +
"END AS April," +
"CASE" +
"    WHEN 5 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-05-01'" +
"        AND CheckOut <=  '" + inYear + "-06-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-05-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-05-01'" +
"        AND CheckOut >  '" + inYear + "-06-01'" +
"        THEN (DATEDIFF( '" + inYear + "-06-01', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-05-01'" +
"        AND CheckOut >  '" + inYear + "-06-01'" +
"        THEN (DATEDIFF( '" + inYear + "-06-01', '" + inYear + "-05-01')*Rate)" +
"    END" +
"    " +
"END AS May," +
"CASE" +
"    WHEN 6 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-06-01'" +
"        AND CheckOut <= '" + inYear + "-07-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-06-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-06-01'" +
"        AND CheckOut > '" + inYear + "-07-01'" +
"        THEN (DATEDIFF('" + inYear + "-06-30', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-06-01'" +
"        AND CheckOut > '" + inYear + "-07-01'" +
"        THEN (DATEDIFF('" + inYear + "-07-01', '" + inYear + "-06-01')*Rate)" +
"    END" +
"    " +
"END AS June," +
"CASE" +
"    WHEN 7 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-07-01'" +
"        AND CheckOut <= '" + inYear + "-08-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-07-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-07-01'" +
"        AND CheckOut > '" + inYear + "-07-31'" +
"        THEN (DATEDIFF('" + inYear + "-08-01', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-07-01'" +
"        AND CheckOut > '" + inYear + "-08-01'" +
"        THEN (DATEDIFF('" + inYear + "-08-01', '" + inYear + "-07-01')*Rate)" +
"    END" +
"    " +
"END AS July," +
"CASE" +
"    WHEN 8 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-08-01'" +
"        AND CheckOut <= '" + inYear + "-09-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-08-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-08-01'" +
"        AND CheckOut > '" + inYear + "-09-01'" +
"        THEN (DATEDIFF('" + inYear + "-08-31', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-08-01'" +
"        AND CheckOut > '" + inYear + "-09-01'" +
"        THEN (DATEDIFF('" + inYear + "-09-01', '" + inYear + "-08-01')*Rate)" +
"    END" +
"    " +
"END AS August," +
"CASE" +
"    WHEN 9 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-09-01'" +
"        AND CheckOut <= '" + inYear + "-10-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-09-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-09-01'" +
"        AND CheckOut > '" + inYear + "-10-01'" +
"        THEN (DATEDIFF('" + inYear + "-10-01', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-09-01'" +
"        AND CheckOut > '" + inYear + "-10-01'" +
"        THEN (DATEDIFF('" + inYear + "-10-01', '" + inYear + "-09-01')*Rate)" +
"    END" +
"    " +
"END AS September," +
"CASE" +
"    WHEN 10 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-10-01'" +
"        AND CheckOut <= '" + inYear + "-11-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-10-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-10-01'" +
"        AND CheckOut > '" + inYear + "-11-01'" +
"        THEN (DATEDIFF('" + inYear + "-11-01', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-10-01'" +
"        AND CheckOut > '" + inYear + "-11-01'" +
"        THEN (DATEDIFF('" + inYear + "-11-01', '" + inYear + "-10-01')*Rate)" +
"    END" +
"    " +
"END AS October," +
"CASE" +
"    WHEN 11 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-11-01'" +
"        AND CheckOut <= '" + inYear + "-12-01'" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-11-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-11-01'" +
"        AND CheckOut > '" + inYear + "-12-01'" +
"        THEN (DATEDIFF('" + inYear + "-12-01', CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-11-01'" +
"        AND CheckOut > '" + inYear + "-12-01'" +
"        THEN (DATEDIFF('" + inYear + "-12-01', '" + inYear + "-11-01')*Rate)" +
"    END" +
"    " +
"END AS November," +
"CASE" +
"    WHEN 12 BETWEEN MONTH(CheckIn) AND MONTH(CheckOut) THEN" +
"    CASE" +
"        WHEN MONTH(CheckIn) = MONTH(CheckOut) " +
"        THEN (DATEDIFF(CheckOut, CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-12-01'" +
"        AND CheckOut <= DATE_ADD('" + inYear + "-12-31', INTERVAL 1 DAY)" +
"        THEN (DATEDIFF(CheckOut, '" + inYear + "-12-01')*Rate)" +
"        " +
"        WHEN CheckIn >= '" + inYear + "-12-01'" +
"        AND CheckOut >  DATE_ADD('" + inYear + "-12-31', INTERVAL 1 DAY)" +
"        THEN (DATEDIFF( DATE_ADD('" + inYear + "-12-31', INTERVAL 1 DAY), CheckIn)*Rate)" +
"        " +
"        WHEN CheckIn < '" + inYear + "-12-01'" +
"        AND CheckOut >  DATE_ADD('" + inYear + "-12-31', INTERVAL 1 DAY)" +
"        THEN (DATEDIFF( DATE_ADD('" + inYear + "-12-31', INTERVAL 1 DAY), '" + inYear + "-12-01')*Rate)" +
"    END" +
"    " +
"END AS December" +
"from egarc113.lab7_reservations" +
"JOIN egarc113.lab7_rooms ON " +
"Room = RoomCode WHERE " + inYear + " BETWEEN Year(CheckIn) AND Year(CheckOut)" +
") t" +
"GROUP BY RoomName"
            );
            pstmt.execute();
         }

          ResultSet rs = pstmt.executeQuery();
         while (rs.next()) {
            String roomName = rs.getString("RoomName");
            int jan = rs.getInt("January");
            int feb = rs.getInt("February");
            int mar = rs.getInt("March");
            int apr = rs.getInt("April");
            int may = rs.getInt("May");
            int jun = rs.getInt("June");
            int jul = rs.getInt("July");
            int aug = rs.getInt("August");
            int sep = rs.getInt("September");
            int oct = rs.getInt("October");
            int nov = rs.getInt("November");
            int dec = rs.getInt("December");
            int tot = rs.getInt("Totals");

            System.out.format("%s, %d, %d, %d, %d, %d, %d, %d, %d, %d , %d, %d, %d %d\n",
                   roomName, jan, feb, mar, apr, may, jun, jul, aug, sep, oct, nov, dec, tot);
         }
         pstmt.close();
      }
      catch (SQLException e) {
         System.out.println("Unable to complete sql request: " + e.getMessage());
         System.exit(1);
      }
   
}

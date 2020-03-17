import java.sql.*;

public class Reservation
{
   private int code;
   private String room;
   private Date checkin;
   private Date checkout;
   private String firstname;
   private String lastname;
   private float rate;
   private int adults;
   private int kids;

   public Reservation(ResultSet rs) throws SQLException
   {
      if (rs.next()) {
         try {
            this.code = rs.getInt("CODE");
            this.room = rs.getString("Room");
            this.checkin = rs.getDate("CheckIn");
            this.checkout = rs.getDate("CheckOut");
            this.firstname = rs.getString("FirstName");
            this.lastname = rs.getString("LastName");
            this.rate = rs.getFloat("Rate");
            this.adults = rs.getInt("Adults");
            this.kids = rs.getInt("Kids");
         } catch (SQLException e) {
            System.out.println("Unable to build Reservation from Result set");
            throw e;
         }
      }
   }

   public int getCode() {
      return code;
   }

   public String getRoom() {
      return room;
   }

   public Date getCheckin() {
      return checkin;
   }

   public void setCheckin(Date checkin) {
      this.checkin = checkin;
   }

   public Date getCheckout() {
      return checkout;
   }

   public void setCheckout(Date checkout) {
      this.checkout = checkout;
   }

   public String getFirstname() {
      return firstname;
   }

   public void setFirstname(String firstname) {
      this.firstname = firstname;
   }

   public String getLastname() {
      return lastname;
   }

   public void setLastname(String lastname) {
      this.lastname = lastname;
   }

   public float getRate() {
      return rate;
   }

   public int getAdults() {
      return adults;
   }

   public void setAdults(int adults) {
      this.adults = adults;
   }

   public int getKids() {
      return kids;
   }

   public void setKids(int kids) {
      this.kids = kids;
   }

   public boolean isValidTimeSlot(Connection conn) {
      try {
         PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM lab7_reservations " +
                         "WHERE CODE != ? AND Room = ? " +
                         "AND Checkout > ? AND CheckIn < ?"
         );
         stmt.setInt(1, code);
         stmt.setString(2, room);
         stmt.setDate(3, checkin);
         stmt.setDate(4, checkout);
         ResultSet rs = stmt.executeQuery();
         return !(rs.isBeforeFirst());
      } catch (SQLException e) {
         System.out.println("Check valid time slot failed: " + e.getMessage());
         return false;
      }
   }

   public String toString() {
      return String.format("Reservation details:\nRoom = %s\nCheckIn = %s\nCheckOut = %s\n" +
              "FirstName = %s\nLastName = %s\nAdults = %d\nKids = %d",
              room, checkin.toString(), checkout.toString(), firstname, lastname, adults, kids);
   }
}

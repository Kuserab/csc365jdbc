import java.util.*;
import java.io.*;

public class Room{

      private String roomCode;
      private String roomName;
      private int beds;
      private String bedType;
      private int maxOcc;
      private double basePrice;
      private String decor; 

   public Room(String roomCode, String roomName, int beds, String bedType,
      int maxOcc, double basePrice, String decor)
   {
      this.roomCode = roomCode;
      this.roomName = roomName;
      this.beds = beds;
      this.bedType = bedType;
      this.maxOcc = maxOcc;
      this.basePrice = basePrice;
      this.decor = decor;
   }

   public String getRoomCode() {
      return roomCode;
   }
   public void setRoomCode(String roomCode) {
      this.roomCode = roomCode;
   }

   public String getRoomName() {
      return roomName;
   }
   public void setRoomName(String roomName) {
      this.roomName = roomName;
   }

   public int getBeds() {
      return beds;
   }
   public void setBeds(int beds) {
      this.beds = beds;
   }

   public String getBedType(){
      return bedType;
   }
   public void setBedType(String bedType) {
      this.bedType = bedType;
   }

   public int getMaxOcc(){
      return maxOcc;
   }
   public void setMaxOcc(int maxOcc) {
      this.maxOcc = maxOcc;
   }

   public double getBasePrice() {
      return basePrice;
   }
   public void setBasePrice(double basePrice) {
      this.basePrice = basePrice;
   }

   public String getDecor(){
      return decor;
   }
   public void setDecor(String decor) {
      this.decor = decor;
   }
}
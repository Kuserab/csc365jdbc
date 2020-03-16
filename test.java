import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.ArrayList;
import java.time.temporal.ChronoUnit;
public class test
{
 	public static void main(String[] args)
	{
		String date1 = "2019-05-03";
		LocalDate checkIn = LocalDate.parse(date1);
		String date2 = "2019-06-05";
		LocalDate checkOut = LocalDate.parse(date2);
		long days = ChronoUnit.DAYS.between(date1,date2);
		System.out.println(checkIn);
	}
}

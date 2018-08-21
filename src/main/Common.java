package main;

import database.SqliteDb;

public class Common {
	
	public static SqliteDb database=new SqliteDb("D:\\share\\sourceReader_mate9.db");
	public static int ThreadSize=16;
	
//	public static String DebugDir="C:\\Users\\zxl\\Desktop\\Test";
	public static String DebugDir="E:\\work_space\\Test\\src\\main";
	public static String OutputDir ="C:\\output";
	
	
	public static String[] illegalSignature = new String[] { };


	public static String[] validSignature = new String[] {
	 }; 

}

package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class LogUtil {
	
	private static void log(String msg){
		System.out.println(msg);
	}
	
	public static void debugVariableCount(String msg){
		File logFile=new java.io.File("debugVariableCount.txt");
		try {
			if(!logFile.exists())
				logFile.createNewFile();
			BufferedWriter writer=new BufferedWriter(new FileWriter(logFile,true));
			writer.write(msg+"\n");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			error("File debugVariableCount : ", msg);
		}
	}
	
	
	public static void error(String tag,String msg){
		System.out.println(tag+" : "+TimeMeasurement.currentTime()+"\n");
		System.out.println(msg+"\n\n");
		File logFile=new java.io.File("error.txt");
		try {
			if(!logFile.exists())
				logFile.createNewFile();
			BufferedWriter writer=new BufferedWriter(new FileWriter(logFile,true));
			writer.write(tag+" : "+TimeMeasurement.currentTime()+"\n");
			System.out.println(tag+" : "+TimeMeasurement.currentTime()+"\n");
			writer.write(msg+"\n\n");
			System.out.println(msg+"\n\n");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			error("File Error1 : "+tag, msg);
		}
	}
	
	public static void debug(String tag,String msg){
		log(tag+" DEBUG : "+msg);
	}
	
	
	public static  synchronized void exception(String tag,String msg,Exception e){
		log(tag+" EXCEPTION : "+msg);
		File logFile=new java.io.File("exception.txt");
		try {
			if(!logFile.exists())
				logFile.createNewFile();
			BufferedWriter writer=new BufferedWriter(new FileWriter(logFile,true));
			writer.write(tag+" : "+TimeMeasurement.currentTime()+"\n");
			writer.write(msg+"\n");
			for(StackTraceElement element : e.getStackTrace())
				writer.write(element+"\n");
			writer.write("\n");
			writer.flush();
			writer.close();
		} catch (Exception e2) {
			error("File Error4 : "+tag, msg);
		}
	}
	
	public static synchronized void info(String tag,String msg){
			File logFile=new java.io.File("info.txt");
			try {
				if(!logFile.exists())
					logFile.createNewFile();
				BufferedWriter writer=new BufferedWriter(new FileWriter(logFile,true));
				writer.write(tag+" : "+TimeMeasurement.currentTime()+"\n");
				writer.write(msg+"\n\n");
				writer.flush();
				writer.close();
			} catch (Exception e) {
				error("File Error1 : "+tag, msg);
			}
	}
	public static synchronized void info2(String tag,String msg){
		File logFile=new java.io.File("info2.txt");
		try {
			if(!logFile.exists())
				logFile.createNewFile();
			BufferedWriter writer=new BufferedWriter(new FileWriter(logFile,true));
			writer.write(tag+" : "+TimeMeasurement.currentTime()+"\n");
			writer.write(msg+"\n\n");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			error("File Error2 : "+tag, msg);
		}
}
	public static  void info4Thread(String name,String msg){
		File logFile=new java.io.File("logFor"+name);
		try {
			if(!logFile.exists())
				logFile.createNewFile();
			BufferedWriter writer=new BufferedWriter(new FileWriter(logFile,true));
			writer.write(TimeMeasurement.currentTime()+"\n");
			writer.write(msg+"\n\n");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			error("File Error3 : ", msg);
		}
}

}

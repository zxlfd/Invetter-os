package module.variablename;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.activation.FileDataSource;

import org.omg.CORBA.PRIVATE_MEMBER;

import com.google.common.collect.Maps;

import database.SqliteDb;
import util.LogUtil;
import main.Common;
import module.util;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.coffi.field_info;
import main.Memory;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;

public class InputVariableName {
	private static String TAG = "InputVariableName";
	private static List<String> sourceClassNames = module.parcel.SearchParcelableClass.tmpUseStrings;
	private static Map<String, Integer> variableOccurenceTimes = new HashMap<>();

	static SqliteDb curDB=new SqliteDb("invetter.db");
	public static void run() {
		LogUtil.error(TAG, " start run InputVariableName !!!!");
//		if (initFromDataProvider() == 0) {
		if ( sourceClassNames.size() == 0) {
			LogUtil.error(TAG, "this class must be run when ParcelableClass Table has data!");
			return;
		}
		LogUtil.error(TAG, "names list size InputVariableName : " + sourceClassNames.size());
		for (String sourceClassName : sourceClassNames) {
			searchSourceClassNameForFieldName(sourceClassName);
		}
		LogUtil.error(TAG, "occurence time map size : " + variableOccurenceTimes.size());
		for (Map.Entry<String, Integer> entry : variableOccurenceTimes.entrySet()) {
			LogUtil.debugVariableCount(entry.getKey() + " " + entry.getValue().toString());
			System.out.println(entry.getKey() + " , " +entry.getValue().toString());

		}

	}

	private static int searchSourceClassNameForFieldName(String sourceClassName) {
		SootClass sourceClass = Memory.classNameMapSootClass.get(sourceClassName);
		if (sourceClass == null) {
			LogUtil.error(TAG, "Why you feed me a impossible to find source class name???" + sourceClassName);
			return -1;
		}
		SootMethod writeToParcelMethod = null;
		try {
		writeToParcelMethod = sourceClass.getMethodByName("writeToParcel");
		}catch(Exception e) {
			e.printStackTrace();
		}

		if (writeToParcelMethod == null) {
			LogUtil.error(TAG, "Can't find write to parcel method : " + sourceClassName
					+ " but ofcourse the getMethodByName is so stupid. Following is total methods.");
			for (SootMethod method : sourceClass.getMethods()) {
				LogUtil.error(TAG, method.getSignature());
			}
			return -1;
		}

		LogUtil.debug(TAG, "find writetoParcel : " + sourceClass.getName());
		try {
		Body body = writeToParcelMethod.retrieveActiveBody();
		if (body == null) {
			LogUtil.error(TAG, "body is null" + writeToParcelMethod.getSignature());
			return -1;
		}
		for (Unit unit : body.getUnits()) {
			Stmt stmt = (Stmt) unit;
			if (stmt.containsFieldRef()) {
				String fieldSignature = stmt.getFieldRef().getField().getSignature();
				String name = stmt.getFieldRef().getField().getName();
				if (variableOccurenceTimes.containsKey(name)) {
					variableOccurenceTimes.put(name, variableOccurenceTimes.get(name) + 1);
				} else {
					variableOccurenceTimes.put(name, 1);
				}

			}

		}
		}catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		return 0;

	}

	private static int initFromDataProvider() {

		String TAG = "ParcelableClass";
		String tableName = TAG;
		int count = 0;
		System.out.println("before select");
		ResultSet resultSet = Common.database.select("SELECT * FROM " + tableName + ";");
//		ResultSet resultSet = curDB.select("SELECT * FROM " + tableName + ";");
	
		if(resultSet == null) {
			System.out.println("empty");
		}
		try {
			if (resultSet.next()) {
				System.out.println("has next");
			}
			else {
				System.out.println("no next");
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("before query");
		try {
			while (resultSet != null && resultSet.next()) {
				String className = resultSet.getString("CLASSNAME");
				sourceClassNames.add(className);
				System.out.println("res " + count++);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		LogUtil.error(TAG, "source class name size" + sourceClassNames.size());
		System.out.println("size" + sourceClassNames.size());
		return sourceClassNames.size();
	}

	public static void main(String[] args) {
		int res = initFromDataProvider();
		System.out.println("res = " + res);
		
	}

}

package cfg;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import dataflow.ClassAnalyzer;
import main.Common;
import main.Memory;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import util.StringUtil;

public class SearchSwappedClass {

	public void run(boolean needInsert) {
		if (needInsert)
			initByInsertIntoTable();
		else
			initBySelectFromTable();
	}

	private void initByInsertIntoTable() {
		String tableName = "SwappedMethod";
			Common.database.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " ("
					+ "ID				INTEGER  PRIMARY KEY AUTOINCREMENT," + "SOURCE_SIGNATURE		TEXT,"
					+ "TARGET_SIGNATURE     TEXT" + ");");

		for (SootClass sootClass : Scene.v().getClasses()) {
			for (SootMethod sootMethod : sootClass.getMethods()) {
				if (!ClassAnalyzer.isValidMethod(sootMethod))
					continue;

				if (sootMethod.isConcrete()) {
					if (isGetOrWrap(sootMethod.getSignature())) {

						String actual = null;
						UnitGraph unitGraph = new ExceptionalUnitGraph(sootMethod.retrieveActiveBody());
						for (Unit unit : unitGraph.getBody().getUnits()) {
							if (((Stmt) unit).containsInvokeExpr()) {
								SootMethod callee = ((Stmt) unit).getInvokeExpr().getMethod(); // if a unit booth has
																								// InvokeExpr and
																								// FieldRef----?
								actual = callee.getSignature();
							}
							if (((Stmt) unit).containsFieldRef()) {
								SootField field = ((Stmt) unit).getFieldRef().getField();
								actual = field.getSignature();
							}
						}
						if (actual == null) {// the same with aforementioned------?
							for (Unit unit : unitGraph.getBody().getUnits()) {
								if ((((Stmt) unit)).containsInvokeExpr()) {
									SootMethod callee = ((Stmt) unit).getInvokeExpr().getMethod();
									actual = callee.getSignature();
								}
							}
						}

						String escape = sootMethod.getSignature().replace("\'", "");// -----?

						Memory.swappedMethodSigntureMapActualMethodSignature.put(sootMethod.getSignature(), actual);

						String value = StringUtil.sqlString(escape) + "," + StringUtil.sqlString(actual);
							Common.database.executeUpdate("INSERT INTO " + tableName
									+ " (SOURCE_SIGNATURE,TARGET_SIGNATURE)" + "VALUES (" + value + ");");
						
					}
				}
			}

		}
	}

	private void initBySelectFromTable() {
		String tableName = "SwappedMethod";
		try {
			ResultSet resultSet = Common.database.select("SELECT * FROM " + tableName + ";");
			while (resultSet != null && resultSet.next()) {
				String source = resultSet.getString("SOURCE_SIGNATURE");
				String sink = resultSet.getString("TARGET_SIGNATURE");

				Memory.swappedMethodSigntureMapActualMethodSignature.put(source, sink);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static boolean isGetOrWrap(String string) {
		if (string.contains("-get") || string.contains("-wrap") || string.contains("-set")) {
			return true;
		}
		return false;
	}
}

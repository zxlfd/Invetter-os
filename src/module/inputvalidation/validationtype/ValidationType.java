package module.inputvalidation.validationtype;

import soot.Unit;

public abstract class ValidationType {
	
	public Unit unit;
	public Unit originalUnitInEntryMethod;
	public int argsNumOfInput;
	public int originalArgsNum;
	public String signatureOfCurrentMethod;

}

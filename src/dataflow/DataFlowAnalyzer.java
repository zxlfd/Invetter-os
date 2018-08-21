package dataflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JRetStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JStaticInvokeExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import util.LogUtil;


public class DataFlowAnalyzer {

	public final static String TAG = "DataFlow.DataFlowAnalyzer";

	// undone, debugging
	public static ValueBox introProcedural_findDataDependentValueboxBetweenUnits(SootMethod method, Unit sourceUnit,
			ValueBox sourceVB, Unit sinkUnit) {
		if (!method.isConcrete())
			return null;
		if (!ClassAnalyzer.isUnitUsedInMethod(method, sourceUnit))
			return null;
		if (sourceVB == null)
			return null;

		UnitGraph unitGraph = new ExceptionalUnitGraph(method.getActiveBody());

		ValueBox def = null;
		if (sourceUnit.getDefBoxes().contains(sourceVB)) {
			def = sourceVB;
		} else if (sourceUnit.getUseBoxes().contains(sourceVB)) {
			if (canTaintFromUseboxToDefbox(method, sourceUnit, sourceVB))
				def = sourceUnit.getDefBoxes().get(0);
			else if (canTaintFromUseboxToThisbox(method, sourceUnit, sourceVB))
				def = null;
		} else {
			def = sourceVB;
		}

		ValueBox result = null;
		for (Unit next : unitGraph.getSuccsOf(sourceUnit)) {
			if (next.equals(sinkUnit)) {
				if (ClassAnalyzer.isValueUsedInUnit(next, def.getValue())) {
					result = ClassAnalyzer.findValueboxByValue(next, def.getValue());
				}
			} else if (ClassAnalyzer.isValueDefinedInUnit(next, def.getValue())) {
				result = introProcedural_findDataDependentValueboxBetweenUnits(method, next, next.getDefBoxes().get(0),
						sinkUnit);
			} else if (ClassAnalyzer.isValueUsedInUnit(next, def.getValue())) {
				ValueBox result1 = introProcedural_findDataDependentValueboxBetweenUnits(method, next,
						ClassAnalyzer.findValueboxByValue(next, def.getValue()), sinkUnit);
				result = result1 != null ? result1
						: introProcedural_findDataDependentValueboxBetweenUnits(method, next, def, sinkUnit);
			} else {
				result = introProcedural_findDataDependentValueboxBetweenUnits(method, next, def, sinkUnit);
			}
		}
		return result;
	}

	public static HashMap<Unit, ValueBox> introProcedural_findDirectUseUnitsAndValueBoxes(Body body, Unit sourceUnit,
			ValueBox sourceBox) {
		HashMap<Unit, ValueBox> result = new HashMap<>();
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		List<Unit> processedUnit = new ArrayList<>();
		List<Unit> waitForProcessedUnit = new ArrayList<>();
		waitForProcessedUnit.add(sourceUnit);
		while (!waitForProcessedUnit.isEmpty()) {
			Unit unit = waitForProcessedUnit.get(0);
			processedUnit.add(unit);
			waitForProcessedUnit.remove(unit);
			for (Unit succor : unitGraph.getSuccsOf(unit)) {
				if (!processedUnit.contains(succor) && ClassAnalyzer.isValueUsedInUnit(succor, sourceBox.getValue())) {
					result.put(succor, ClassAnalyzer.findValueboxByValue(succor, sourceBox.getValue()));
				}
				if (!processedUnit.contains(succor)
						&& !ClassAnalyzer.isValueDefinedInUnit(succor, sourceBox.getValue()))
					waitForProcessedUnit.add(succor);
			}
		}
		return result;
	}

	//HashMap<Unit, ValueBox> allUse = DataFlowAnalyzer.introProcedure_findAllUseUnitsAndValueBoxes(
	//method.retrieveActiveBody(), unit, unit.getDefBoxes().get(0));
	
	public static HashMap<Unit, ValueBox> intraProcedural_findAllUseUnitsAndValueBoxes(Body body, Unit sourceUnit,
			ValueBox sourceBox) {
		HashMap<Unit, ValueBox> result = new HashMap<>();
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		Set<Event> processedUnit = new HashSet<>();
		EventQueue waitForProcessedUnit = new EventQueue();
		waitForProcessedUnit.add(new Event(sourceUnit, sourceBox));
		while (!waitForProcessedUnit.isEmpty()) {
			if(Thread.interrupted())
				break;
			Event event = waitForProcessedUnit.poll();
			Unit unit = event.unit;
			ValueBox sourceValueBox = event.valueBox;
			processedUnit.add(event);
			for (Unit succor : unitGraph.getSuccsOf(unit)) {
				if (!ClassAnalyzer.isValueDefinedInUnit(succor, sourceValueBox.getValue())) {
					Event event1 = new Event(succor, sourceValueBox);
					if (!processedUnit.contains(event1))
						waitForProcessedUnit.add(event1);
				}
				if (ClassAnalyzer.isValueUsedInUnit(succor, sourceValueBox.getValue())
						&& !ClassAnalyzer.isValueDefinedInUnit(succor, sourceValueBox.getValue())) {
					result.put(succor, ClassAnalyzer.findValueboxByValue(succor, sourceValueBox.getValue()));//被taint的succorUnit和tainted value
					//记录左值被taint的Value，以及对应的succor的unit
					if (canTaintFromUseboxToDefbox(body.getMethod(), succor, // ----succor的unite的DefBoxes不为空，且所在method是有效的
							ClassAnalyzer.findValueboxByValue(succor, sourceValueBox.getValue()))) { //----?
						Event event2 = new Event(succor, succor.getDefBoxes().get(0));
						if (!processedUnit.contains(event2)) {
							waitForProcessedUnit.add(event2);
						}
					} 
					//记录右值被taint的Value，以及对应的succor的unit
				    // ----succor的unite的UseBoxes不为空，且所在method是有效的
					//--(((Stmt) unit).containsInvokeExpr() && !(((Stmt) unit).getInvokeExpr() instanceof JStaticInvokeExpr))
					else if (canTaintFromUseboxToThisbox(body.getMethod(), succor, 
							ClassAnalyzer.findValueboxByValue(succor, sourceValueBox.getValue()))) { 
						Event event2 = new Event(succor, ((Stmt) succor).getInvokeExpr().getUseBoxes()
								.get(    ((Stmt) succor).getInvokeExpr().getUseBoxes().size() - 1)          );
						if (!processedUnit.contains(event2)) {
							waitForProcessedUnit.add(event2);
						}
					}
				}
			}
		}
		return result;
	}
	public static HashMap<Unit, ValueBox> interProcedural_findAllUseUnitsAndValueBoxes(Body body, Unit sourceUnit,
			ValueBox sourceBox) {
		HashMap<Unit, ValueBox> result = new HashMap<>();
		
		HashMap<Unit, ValueBox> current=intraProcedural_findAllUseUnitsAndValueBoxes(body, sourceUnit, sourceBox);
		result.putAll(current);
		for(Unit unit:current.keySet()){
			if(((Stmt)unit).containsInvokeExpr()){
				ValueBox valueBox=current.get(unit);
				SootMethod next=((Stmt)unit).getInvokeExpr().getMethod();
				if(ClassAnalyzer.isValidMethod(next) && next.isConcrete()){
					Body nextBody=next.retrieveActiveBody();
					int param =((Stmt)unit).getInvokeExpr().getArgs().indexOf(valueBox.getValue());
					Unit nextUnit=null;
					ValueBox nextValueBox=null;
					for(Unit tmp:body.getUnits()){
						if(tmp.toString().contains("@param"+param)){
							nextUnit=tmp;
							nextValueBox=tmp.getDefBoxes().get(0);
							break;
						}
					}
					if(nextUnit!=null && nextValueBox!=null)
						result.putAll(interProcedural_findAllUseUnitsAndValueBoxes(nextBody, nextUnit, nextValueBox));
				}
			}
		}
		return result;
	}

	public static HashMap<Unit, ValueBox> introProcedural_findDirectDefUnitsAndValueBoxes(Body body, Unit sourceUnit,
			ValueBox sourceBox) {
		HashMap<Unit, ValueBox> result = new HashMap<>();
		UnitGraph unitGraph = new ExceptionalUnitGraph(body);
		Set<Unit> processedUnit=new HashSet<>();
		Event current=new Event(sourceUnit, sourceBox);
		EventQueue queue=new EventQueue();
		queue.add(current);
		while(!queue.isEmpty()){
			Event header=queue.poll();
			Unit unit=header.unit;
			ValueBox valueBox=header.valueBox;
			processedUnit.add(unit);
			for(Unit pred:unitGraph.getPredsOf(unit)){
				if(processedUnit.contains(pred))
					continue;
				if(pred.getDefBoxes().isEmpty()
						|| !ClassAnalyzer.isValueDefinedInUnit(pred, valueBox.getValue())){
					queue.add(new Event(pred, valueBox));
				}else{
					result.put(pred, pred.getDefBoxes().get(0));
				}
			}
		}
		return result;
	}

	public static HashMap<Unit, ValueBox> introProcedural_findAllDefUnitsAndValueBoxes(Body body, Unit sourceUnit,
			ValueBox sourceBox) {
		HashMap<Unit, ValueBox> result = new HashMap<>();
		Set<Unit> processedUnits=new HashSet<>();
		EventQueue waitForProcessedUnit = new EventQueue();
		waitForProcessedUnit.add(new Event(sourceUnit, sourceBox));
		while (!waitForProcessedUnit.isEmpty()) {
			Event current=waitForProcessedUnit.poll();
			HashMap<Unit, ValueBox> currentResult=introProcedural_findDirectDefUnitsAndValueBoxes(body,current.unit,current.valueBox);
			result.putAll(currentResult);
			processedUnits.add(current.unit);
			for(Unit next:currentResult.keySet()){
				if(processedUnits.contains(next))
					continue;
				waitForProcessedUnit.add(new Event(next, currentResult.get(next)));
			}
		}
		
		return result;
	}


	public static boolean canTaintFromUseboxToDefbox(SootMethod method, Unit unit, ValueBox use) {
		if (unit.getDefBoxes().size() == 0 || !ClassAnalyzer.isValidMethod(method))
			return false;

		return true;
	}

	public static boolean canTaintFromUseboxToThisbox(SootMethod method, Unit unit, ValueBox use) {
		if (unit.getUseBoxes().size() == 0 || !ClassAnalyzer.isValidMethod(method))
			return false;
		if (((Stmt) unit).containsInvokeExpr() && !(((Stmt) unit).getInvokeExpr() instanceof JStaticInvokeExpr))
			return true;
		return false;
	}
	
	
	public static boolean isCalleeCanBeReturnedByCaller(SootMethod caller,SootMethod callee){
		if(!caller.isConcrete()) return false;
		if(!caller.getSignature().split(" ")[1].equals(callee.getSignature().split(" ")[1]))
			return false;
		boolean voidCallee=callee.getSignature().contains(" void ");
		Body body=caller.retrieveActiveBody();
		if(body == null) {
			LogUtil.error("DataFlowAnalyzer", "isCalleeCanBeReturnedByCaller caller body = null : "+ "caller "+caller.getSignature() );
			return false;
		}
		List<Unit> invokeCallee=new ArrayList<>();
		for(Unit unit : body.getUnits()){
			if(voidCallee && ((Stmt)unit).containsInvokeExpr()){
				if(((Stmt)unit).getInvokeExpr().getMethod().getSignature().equals(callee.getSignature()))
					return true;
			}
			if(unit instanceof JAssignStmt && ((JAssignStmt) unit).containsInvokeExpr()){
				if(((Stmt)unit).getInvokeExpr().getMethod().getSignature().equals(callee.getSignature())){
					invokeCallee.add(unit);
				}
			}
		}
		for(Unit sourceUnit : invokeCallee){
			HashMap<Unit, ValueBox> allUseUnitAndValueBox=DataFlowAnalyzer.intraProcedural_findAllUseUnitsAndValueBoxes(body, sourceUnit, sourceUnit.getDefBoxes().get(0));
			for(Unit sinkUnit : allUseUnitAndValueBox.keySet())
				if(sinkUnit instanceof JRetStmt || sinkUnit instanceof JReturnStmt)
					return true;
		}
		return false;
	}
}






class Event {
	public Unit unit;
	public ValueBox valueBox;

	public Event(Unit unit, ValueBox vBox) {
		this.unit = unit;
		this.valueBox = vBox;
	}

	public boolean equals(Event event) {
		return this.unit == event.unit && this.valueBox == event.valueBox;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Event)) {
			return false;
		}
		Event event = (Event) obj;
		return equals(event);
	}

	@Override
	public int hashCode() {
		return Objects.hash(unit, valueBox);
	}
}

class EventQueue {
	private Queue<Event> eventQueue = new LinkedList<>();

	public boolean isEmpty() {
		return eventQueue.isEmpty();
	}

	public void add(Event event) {
		eventQueue.add(event);
	}

	public Event poll() {
		return eventQueue.poll();
	}

	public boolean contains(Event event) {
		return eventQueue.contains(event);
	}
	
	public Queue<Event> getEventQueue() {
		return eventQueue;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof EventQueue)){
			return false;
		}
		EventQueue eventQueue = (EventQueue) obj;
		return this.eventQueue.equals(eventQueue.getEventQueue());
	}
	
	@Override
	public int hashCode() {
		return eventQueue.hashCode();
	}
}

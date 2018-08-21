package module.inputvalidation.concurrent;

import java.util.ArrayList;
import java.util.List;

import module.inputvalidation.validationtype.ValidationType;

public class MethodAndValidations {
	
	public String signature;
	public List<ValidationType> validations=new ArrayList<>();
	
	
	
	public MethodAndValidations(String signature,List<ValidationType> validationTypes){
		this.signature=signature;
		this.validations=validationTypes;
	}

}

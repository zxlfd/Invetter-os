package module.inputvalidation;

import static org.junit.Assert.*;


import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import database.SqliteDb;
import module.inputvalidation.*;

import module.inputvalidation.validationtype.ValidationType;
public class SearchInputValidationTest {

	SqliteDb SqliteDb = new SqliteDb("invetter");
	SearchInputValidation validation = new SearchInputValidation();
	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void test() {
		validation.createTableInDatabase();
		List<ValidationType>validations = new ArrayList<>();
//		validations.append();
		validation.insertIntoTableOfInputValidationInMethod(validations);
		
	}

}

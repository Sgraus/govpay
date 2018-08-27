package it.govpay.core.utils.validator;

import org.openspcoop2.generic_project.exception.ValidationException;

public class EnumValidator {

	private String fieldName;
	private Enum<?> fieldValue;

	protected EnumValidator(String fieldName, Enum<?> fieldValue) { 
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}

	public EnumValidator notNull() throws ValidationException {
		if(fieldValue == null) {
			throw new ValidationException("Il campo " + fieldName + " non deve essere vuoto.");
		}
		return this;
	}
	
	public EnumValidator isNull() throws ValidationException {
		if(fieldValue != null) {
			throw new ValidationException("Il campo " + fieldName + " deve essere vuoto.");
		}
		return this;
	}
}
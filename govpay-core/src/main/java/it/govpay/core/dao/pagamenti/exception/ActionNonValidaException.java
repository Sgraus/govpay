package it.govpay.core.dao.pagamenti.exception;

import it.govpay.core.dao.commons.exception.RedirectException;

public class ActionNonValidaException extends RedirectException{

	public ActionNonValidaException(String location) {
		super(location);
	}
	
	public ActionNonValidaException(String location, String message) {
		super(location,message);
	}
	
	public ActionNonValidaException(String location,Throwable t) {
		super(location, t);
	}
	
	public ActionNonValidaException(String location, String message ,Throwable t) {
		super(location,message,t);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}

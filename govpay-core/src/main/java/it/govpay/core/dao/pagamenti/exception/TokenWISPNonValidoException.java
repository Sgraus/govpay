package it.govpay.core.dao.pagamenti.exception;

import it.govpay.core.dao.commons.exception.RedirectException;

public class TokenWISPNonValidoException extends RedirectException{

	public TokenWISPNonValidoException(String location) {
		super(location);
	}
	
	public TokenWISPNonValidoException(String location, String message) {
		super(location,message);
	}
	
	public TokenWISPNonValidoException(String location,Throwable t) {
		super(location, t);
	}
	
	public TokenWISPNonValidoException(String location, String message ,Throwable t) {
		super(location,message,t);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}

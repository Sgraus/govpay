/**
 * 
 */
package it.govpay.core.dao.pagamenti.exception;

import it.govpay.core.dao.commons.exception.NonTrovataException;

/**
 * @author Bussu Giovanni (bussu@link.it)
 * @author  $Author: bussu $
 * @version $ Rev: 12563 $, $Date: 01 feb 2018 $
 * 
 */
public class RiscossioneNonTrovataException extends NonTrovataException {

	private static final long serialVersionUID = 1L;

	public RiscossioneNonTrovataException(String msg) {
		super(msg);
	}
	public RiscossioneNonTrovataException(String message, Throwable t) {
		super(message, t);
	}


}

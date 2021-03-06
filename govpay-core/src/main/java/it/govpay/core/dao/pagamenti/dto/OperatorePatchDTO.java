/**
 * 
 */
package it.govpay.core.dao.pagamenti.dto;

import it.govpay.model.IAutorizzato;

/**
 * @author Bussu Giovanni (bussu@link.it)
 * @author  $Author: bussu $
 * @version $ Rev: 12563 $, $Date: 28 giu 2018 $
 * 
 */
public class OperatorePatchDTO extends AbstractPatchDTO {

	private String idOperatore;
	
	public OperatorePatchDTO(IAutorizzato user) {
		super(user);
	}

	public String getIdOperatore() {
		return this.idOperatore;
	}

	public void setIdOperatore(String idOperatore) {
		this.idOperatore = idOperatore;
	}

	
}

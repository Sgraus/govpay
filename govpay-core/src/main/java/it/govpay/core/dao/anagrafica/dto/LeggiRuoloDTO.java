/**
 * 
 */
package it.govpay.core.dao.anagrafica.dto;

import it.govpay.model.IAutorizzato;

/**
 * @author Bussu Giovanni (bussu@link.it)
 * @author  $Author: bussu $
 * @version $ Rev: 12563 $, $Date: 01 feb 2018 $
 * 
 */
public class LeggiRuoloDTO extends BasicCreateRequestDTO {


	public LeggiRuoloDTO(IAutorizzato user) {
		super(user);
	}

	public String getRuolo() {
		return this.ruolo;
	}

	public void setRuolo(String ruolo) {
		this.ruolo = ruolo;
	}

	private String ruolo;
	
}

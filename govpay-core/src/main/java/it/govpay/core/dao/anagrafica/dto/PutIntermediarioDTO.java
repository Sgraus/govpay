package it.govpay.core.dao.anagrafica.dto;

import it.govpay.model.Intermediario;
import it.govpay.model.IAutorizzato;

public class PutIntermediarioDTO extends BasicCreateRequestDTO  {
	
	private Intermediario intermediario;
	private String idIntermediario;
	
	public PutIntermediarioDTO(IAutorizzato user) {
		super(user);
		// TODO Auto-generated constructor stub
	}

	public Intermediario getIntermediario() {
		return intermediario;
	}

	public void setIntermediario(Intermediario intermediario) {
		this.intermediario = intermediario;
	}

	public String getIdIntermediario() {
		return idIntermediario;
	}

	public void setIdIntermediario(String idIntermediario) {
		this.idIntermediario = idIntermediario;
	}

}
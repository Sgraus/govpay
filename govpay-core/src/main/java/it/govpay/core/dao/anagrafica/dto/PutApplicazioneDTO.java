package it.govpay.core.dao.anagrafica.dto;

import it.govpay.bd.model.Applicazione;
import it.govpay.model.IAutorizzato;

public class PutApplicazioneDTO extends BasicCreateRequestDTO  {
	
	private Applicazione applicazione;
	private String idApplicazione;
	
	public PutApplicazioneDTO(IAutorizzato user) {
		super(user);
	}

	public Applicazione getApplicazione() {
		return applicazione;
	}

	public void setApplicazione(Applicazione applicazione) {
		this.applicazione = applicazione;
	}

	public String getIdApplicazione() {
		return idApplicazione;
	}

	public void setIdApplicazione(String idApplicazione) {
		this.idApplicazione = idApplicazione;
	}

}
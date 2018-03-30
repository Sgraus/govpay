package it.govpay.core.dao.anagrafica.dto;

import it.govpay.bd.model.Tributo;
import it.govpay.model.IAutorizzato;

public class PutEntrataDominioDTO extends BasicCreateRequestDTO  {
	
	
	private Tributo tributo;
	private String idDominio;
	private String idTributo;
	private String ibanAccredito;
	private String ibanAppoggio;

	
	public PutEntrataDominioDTO(IAutorizzato user) {
		super(user);
	}

	public Tributo getTributo() {
		return tributo;
	}

	public void setTributo(Tributo tributo) {
		this.tributo = tributo;
	}

	public String getIdTributo() {
		return idTributo;
	}

	public void setIdTributo(String idTributo) {
		this.idTributo = idTributo;
	}

	public String getIdDominio() {
		return idDominio;
	}

	public void setIdDominio(String idDominio) {
		this.idDominio = idDominio;
	}

	public String getIbanAccredito() {
		return ibanAccredito;
	}

	public void setIbanAccredito(String ibanAccredito) {
		this.ibanAccredito = ibanAccredito;
	}

	public String getIbanAppoggio() {
		return ibanAppoggio;
	}

	public void setIbanAppoggio(String ibanAppoggio) {
		this.ibanAppoggio = ibanAppoggio;
	}


}
package it.govpay.core.dao.anagrafica.dto;

import it.govpay.model.IAutorizzato;

public class GetAvvisoDTO extends BasicRequestDTO {
	
	public enum FormatoAvviso  {PDF, JSON};
	
	private String codDominio;
	private String iuv;
	private FormatoAvviso formato;
	
	public GetAvvisoDTO(IAutorizzato user, String codDominio, String iuv) {
		super(user);
		this.setCodDominio(codDominio);
		this.setIuv(iuv);
	}

	public String getIuv() {
		return this.iuv;
	}

	public void setIuv(String iuv) {
		this.iuv = iuv;
	}

	public String getCodDominio() {
		return this.codDominio;
	}

	public void setCodDominio(String codDominio) {
		this.codDominio = codDominio;
	}

	public FormatoAvviso getFormato() {
		return formato;
	}

	public void setFormato(FormatoAvviso formato) {
		this.formato = formato;
	}


}

package it.govpay.core.dao.eventi.dto;

import org.openspcoop2.generic_project.expression.SortOrder;

import it.govpay.core.dao.anagrafica.dto.BasicFindRequestDTO;
import it.govpay.model.IAutorizzato;
import it.govpay.orm.Evento;

public class ListaEventiDTO extends BasicFindRequestDTO{

	public ListaEventiDTO(IAutorizzato user) {
		super(user);
		this.addSort(Evento.model().DATA_1, SortOrder.ASC);
	}

	private String idDominio;
	private String iuv;
	private String idA2A;
	private String idPendenza;
	
	public String getIdDominio() {
		return this.idDominio;
	}
	public void setIdDominio(String idDominio) {
		this.idDominio = idDominio;
	}
	public String getIuv() {
		return this.iuv;
	}
	public void setIuv(String iuv) {
		this.iuv = iuv;
	}
	public String getIdA2A() {
		return this.idA2A;
	}
	public void setIdA2A(String idA2A) {
		this.idA2A = idA2A;
	}
	public String getIdPendenza() {
		return this.idPendenza;
	}
	public void setIdPendenza(String idPendenza) {
		this.idPendenza = idPendenza;
	}
}

/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2017 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govpay.bd.model;

import java.util.List;

import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.pagamento.PagamentiBD;
import it.govpay.bd.pagamento.filters.PagamentoFilter;
import it.govpay.model.Applicazione;

public class Incasso extends it.govpay.model.Incasso {

	private static final long serialVersionUID = 1L;
	// Business
	private List<Pagamento> pagamenti;
	private Applicazione applicazione;
	private Dominio dominio;


	public List<Pagamento> getPagamenti(BasicBD bd) throws ServiceException {
		if(pagamenti == null && getId() != null){
			PagamentiBD pagamentiBD = new PagamentiBD(bd);
			PagamentoFilter filter = pagamentiBD.newFilter();
			filter.setIdIncasso(getId());
			pagamenti = pagamentiBD.findAll(filter);
		}
		return pagamenti;
	}
	
	public Applicazione getApplicazione(BasicBD bd) throws ServiceException {
		if(applicazione == null) {
			applicazione = AnagraficaManager.getApplicazione(bd, getIdApplicazione());
		} 
		return applicazione;
	}
	
	public void setApplicazione(long idApplicazione, BasicBD bd) throws ServiceException {
		applicazione = AnagraficaManager.getApplicazione(bd, idApplicazione);
		this.setIdApplicazione(applicazione.getId());
	}
	
	public Dominio getDominio(BasicBD bd) throws ServiceException {
		if(dominio == null) {
			try{
				dominio = AnagraficaManager.getDominio(bd, this.getCodDominio());
			}catch (NotFoundException e) {
				dominio = null;
			}
		} 
		return dominio;
	}
}


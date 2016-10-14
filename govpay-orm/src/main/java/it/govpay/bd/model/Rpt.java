/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2016 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import it.govpay.bd.pagamento.VersamentiBD;
import it.govpay.model.Intermediario;

public class Rpt extends it.govpay.model.Rpt{
	
	private static final long serialVersionUID = 1L;
	 
	// Business
	
	private Versamento versamento;
	private Stazione stazione;
	private Intermediario intermediario;
	private Canale canale;
	private Psp psp;
	private List<Pagamento> pagamenti;
	
	
	public Versamento getVersamento(BasicBD bd) throws ServiceException {
		if(this.versamento == null) {
			VersamentiBD versamentiBD = new VersamentiBD(bd);
			this.versamento = versamentiBD.getVersamento(getIdVersamento());
		}
		return this.versamento;
	}
	
	public void setVersamento(Versamento versamento) {
		this.versamento = versamento;
	}
	
	public Stazione getStazione(BasicBD bd) throws ServiceException {
		if(this.stazione == null) {
			try {
				this.stazione = AnagraficaManager.getStazione(bd, getCodStazione());
			} catch (NotFoundException e) {
				throw new ServiceException(e);
			}
		}
		return this.stazione;
	}
	
	public void setStazione(Stazione stazione) {
		this.stazione = stazione;
	}
	
	public Intermediario getIntermediario(BasicBD bd) throws ServiceException {
		if(this.intermediario == null) {
			this.intermediario = AnagraficaManager.getIntermediario(bd, getStazione(bd).getIdIntermediario());
		}
		return this.intermediario;
	}
	public void setIntermediario(Intermediario intermediario) {
		this.intermediario = intermediario;
	}
	
	public Canale getCanale(BasicBD bd) throws ServiceException {
		if(canale == null)
			canale = AnagraficaManager.getCanale(bd, getIdCanale());
		return canale;
	}
	public void setCanale(Canale canale) {
		this.canale = canale;
	}
	
	public Psp getPsp(BasicBD bd) throws ServiceException {
		if(psp == null) 
			psp = AnagraficaManager.getPsp(bd, getCanale(bd).getIdPsp());
		return psp;
	}
	public void setPsp(Psp psp) {
		this.psp = psp;
	}
	
	public List<Pagamento> getPagamenti(BasicBD bd) throws ServiceException {
		if(pagamenti == null) {
			PagamentiBD pagamentiBD = new PagamentiBD(bd);
			pagamenti = pagamentiBD.getPagamenti(getId());
		}
		return pagamenti;
	}
	
	public Pagamento getPagamento(String iur, BasicBD bd) throws ServiceException, NotFoundException {
		List<Pagamento> pagamenti = getPagamenti(bd);
		for(Pagamento pagamento : pagamenti) {
			if(pagamento.getIur().equals(iur))
				return pagamento;
		}
		throw new NotFoundException();
	}
	
	public void setPagamenti(List<Pagamento> pagamenti) {
		this.pagamenti = pagamenti;
	}

}

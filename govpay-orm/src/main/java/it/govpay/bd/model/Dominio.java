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

import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.model.Anagrafica;
import it.govpay.model.Applicazione;

public class Dominio extends it.govpay.model.Dominio {
	private static final long serialVersionUID = 1L;
	
	// Business
	
	private transient Anagrafica anagrafica;
	private transient Stazione stazione;
	private transient Applicazione applicazioneDefault;
	
	public Stazione getStazione(BasicBD bd) throws ServiceException {
		if(stazione == null) {
			stazione = AnagraficaManager.getStazione(bd, this.getIdStazione());
		} 
		return stazione;
	}

	public Anagrafica getAnagrafica(BasicBD bd) throws ServiceException, NotFoundException {
		if(anagrafica == null) {
			anagrafica = AnagraficaManager.getUnitaOperativa(bd, this.getId(), EC).getAnagrafica();
		}
		return anagrafica;
	}

	public void setAnagrafica(Anagrafica anagrafica) {
		this.anagrafica = anagrafica;
	}

	public Applicazione getApplicazioneDefault(BasicBD bd) throws ServiceException {
		if(applicazioneDefault == null && this.getIdApplicazioneDefault() != null) {
			applicazioneDefault = AnagraficaManager.getApplicazione(bd, this.getIdApplicazioneDefault());
		} 
		return applicazioneDefault;
	}

	public void setApplicazioneDefault(Applicazione applicazioneDefault) {
		this.applicazioneDefault = applicazioneDefault;
		this.setIdApplicazioneDefault(applicazioneDefault.getId());
	}


}


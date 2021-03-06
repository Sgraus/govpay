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

package it.govpay.core.business.model;

import java.util.List;

import it.govpay.bd.model.Canale;
import it.govpay.bd.model.Utenza;
import it.govpay.bd.model.Applicazione;

public class AvviaTransazioneDTO {

	private Utenza utente;
	private List<Object> versamentoOrVersamentoRef;
	private it.govpay.servizi.commons.Anagrafica versante;
	private String ibanAddebito;
	private String autenticazione;
	private String urlRitorno;
	private Boolean aggiornaSeEsisteB;
	private Canale canale;
	
	public List<Object> getVersamentoOrVersamentoRef() {
		return this.versamentoOrVersamentoRef;
	}
	public void setVersamentoOrVersamentoRef(List<Object> versamentoOrVersamentoRef) {
		this.versamentoOrVersamentoRef = versamentoOrVersamentoRef;
	}
	public it.govpay.servizi.commons.Anagrafica getVersante() {
		return this.versante;
	}
	public void setVersante(it.govpay.servizi.commons.Anagrafica versante) {
		this.versante = versante;
	}
	public String getIbanAddebito() {
		return this.ibanAddebito;
	}
	public void setIbanAddebito(String ibanAddebito) {
		this.ibanAddebito = ibanAddebito;
	}
	public String getAutenticazione() {
		return this.autenticazione;
	}
	public void setAutenticazione(String autenticazione) {
		this.autenticazione = autenticazione;
	}
	public String getUrlRitorno() {
		return this.urlRitorno;
	}
	public void setUrlRitorno(String urlRitorno) {
		this.urlRitorno = urlRitorno;
	}
	public Boolean getAggiornaSeEsisteB() {
		return this.aggiornaSeEsisteB;
	}
	public void setAggiornaSeEsisteB(Boolean aggiornaSeEsisteB) {
		this.aggiornaSeEsisteB = aggiornaSeEsisteB;
	}
	public Canale getCanale() {
		return this.canale;
	}
	public void setCanale(Canale canale) {
		this.canale = canale;
	}
	public Utenza getUtente() {
		return utente;
	}
	public void setUtente(Utenza utente) {
		this.utente = utente;
	}

}

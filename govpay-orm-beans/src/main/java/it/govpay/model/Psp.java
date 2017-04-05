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
package it.govpay.model;

import java.util.ArrayList;
import java.util.List;

public class Psp extends BasicModel {	
	
	private static final long serialVersionUID = 1L;
	private Long id; 	
	private String codPsp;
	private String codFlusso;
	private String ragioneSociale;
	private String urlInfo;
	private boolean bolloGestito;
	private boolean stornoGestito;
	private boolean abilitato;
	private List<Canale> canali = new ArrayList<Canale>();
	
	public Psp() {
		canali = new ArrayList<Canale>();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodPsp() {
		return codPsp;
	}

	public void setCodPsp(String codPsp) {
		this.codPsp = codPsp;
	}

	public String getRagioneSociale() {
		return ragioneSociale;
	}

	public void setRagioneSociale(String ragioneSociale) {
		this.ragioneSociale = ragioneSociale;
	}

	public String getUrlInfo() {
		return urlInfo;
	}

	public void setUrlInfo(String urlInfo) {
		this.urlInfo = urlInfo;
	}

	public boolean isBolloGestito() {
		return bolloGestito;
	}

	public void setBolloGestito(boolean bolloGestito) {
		this.bolloGestito = bolloGestito;
	}

	public boolean isStornoGestito() {
		return stornoGestito;
	}

	public void setStornoGestito(boolean stornoGestito) {
		this.stornoGestito = stornoGestito;
	}

	public boolean isAbilitato() {
		return abilitato;
	}

	public void setAbilitato(boolean abilitato) {
		this.abilitato = abilitato;
	}
	
	public List<Canale> getCanali() {
		return canali;
	}

	public void setCanali(List<Canale> canali) {
		this.canali = canali;
	}
	
	public String getCodFlusso() {
		return codFlusso;
	}

	public void setCodFlusso(String codFlusso) {
		this.codFlusso = codFlusso;
	}

}

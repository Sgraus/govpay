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
package it.govpay.web.rs.dars.model.input.dinamic;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.web.rs.dars.model.RawParamValue;
import it.govpay.web.rs.dars.model.Voce;
import it.govpay.web.rs.dars.model.input.FieldType;
import it.govpay.web.rs.dars.model.input.RefreshableParamField;

public abstract class SelectList<T> extends RefreshableParamField<T> {
	
	public SelectList(String id, String label, URI refreshUri, List<RawParamValue> paramValues, Object ... objects) {
		super(id, label, refreshUri, paramValues, FieldType.SELECT_LIST);
		//init(paramValues, objects);
	}
	
	public SelectList(String id, String label, URI refreshUri, List<RawParamValue> paramValues, FieldType paramType, Object ... objects) {
		super(id, label, refreshUri, paramValues, paramType);
		//init(paramValues, objects);
	}

	@Override
	public void init(List<RawParamValue> paramValues, Object... objects) {
		super.init(paramValues, objects); 
		try {
			this.values = this.getValues(paramValues, objects);
		} catch (ServiceException e) {
			this.values = new ArrayList<Voce<T>>();
			this.values.add(new Voce<T>("!! ERRORE !!", null));
		}
	}
	
	@Override
	public void aggiornaParametro(List<RawParamValue> values, Object... objects) {
		super.aggiornaParametro(values, objects);
		try {
			this.values = this.getValues(values, objects);
		} catch (ServiceException e) {
			this.values = new ArrayList<Voce<T>>();
			this.values.add(new Voce<T>("!! ERRORE !!", null));
		}
	}
	
	@JsonIgnore
	protected abstract List<Voce<T>> getValues(List<RawParamValue> paramValues, Object ... objects) throws ServiceException;
	
	private List<Voce<T>> values; 
	
	public List<Voce<T>> getValues() {
		return this.values;
	}

}

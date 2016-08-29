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
package it.govpay.web.rs.dars.anagrafica.intermediari.input;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import it.govpay.bd.BasicBD;
import it.govpay.bd.model.Connettore;
import it.govpay.web.rs.dars.anagrafica.connettori.ConnettoreHandler;
import it.govpay.web.rs.dars.model.RawParamValue;
import it.govpay.web.rs.dars.model.input.dinamic.InputSecret;
import it.govpay.web.utils.Utils;

public class Password extends InputSecret {
	
	private String tipoAutenticazioneId= null;
	private String nomeServizio = null;
	private String nomeConnettore = null;
	private String idOwnerId = null;

	public Password(String nomeConnettore,String nomeServizio,String id, String label, int minLength, int maxLength, URI refreshUri,List<RawParamValue> values, Object... objects) {
		super(id, label, minLength, maxLength, refreshUri, values);
		this.nomeServizio = nomeServizio;
		this.nomeConnettore = nomeConnettore;
		this.tipoAutenticazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + this.nomeConnettore + ".tipoAutenticazione.id");
		this.idOwnerId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".id.id");
	}

	@Override
	protected String getDefaultValue(List<RawParamValue> values, Object... objects) {
		String tipoAutenticazioneValue = Utils.getValue(values, this.tipoAutenticazioneId);
		String idOwner = Utils.getValue(values, this.idOwnerId);
		Connettore c = null;
		
		if(StringUtils.isNotEmpty(tipoAutenticazioneValue) && !tipoAutenticazioneValue.equals(ConnettoreHandler.TIPO_AUTENTICAZIONE_VALUE_HTTP_BASIC)){
			return "";
		}
		
		if(StringUtils.isEmpty(idOwner)){
			return "";
		}
		
		BasicBD bd = (BasicBD) objects[0];
		c= Utils.getConnettore(idOwner, this.nomeConnettore,bd);
		
		if(c!= null){
			return c.getHttpPassw();
		}
		
		return "";
	}

	@Override
	protected boolean isRequired(List<RawParamValue> values, Object... objects) {
		String tipoAutenticazioneValue = Utils.getValue(values, this.tipoAutenticazioneId);
		
		if(StringUtils.isNotEmpty(tipoAutenticazioneValue) && tipoAutenticazioneValue.equals(ConnettoreHandler.TIPO_AUTENTICAZIONE_VALUE_HTTP_BASIC)){
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean isHidden(List<RawParamValue> values, Object... objects) {
		String tipoAutenticazioneValue = Utils.getValue(values, this.tipoAutenticazioneId);
		
		if(StringUtils.isNotEmpty(tipoAutenticazioneValue) && tipoAutenticazioneValue.equals(ConnettoreHandler.TIPO_AUTENTICAZIONE_VALUE_HTTP_BASIC)){
			return false;
		}
		
		return true;
	}

	@Override
	protected boolean isEditable(List<RawParamValue> values, Object... objects) {
		String tipoAutenticazioneValue = Utils.getValue(values, this.tipoAutenticazioneId);
		
		if(StringUtils.isNotEmpty(tipoAutenticazioneValue) && tipoAutenticazioneValue.equals(ConnettoreHandler.TIPO_AUTENTICAZIONE_VALUE_HTTP_BASIC)){
			return true;
		}
		
		return false;
	}

	
}

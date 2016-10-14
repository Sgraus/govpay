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
package it.govpay.web.rs.dars.anagrafica.portali.input;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.SortOrder;

import it.govpay.bd.BasicBD;
import it.govpay.bd.FilterSortWrapper;
import it.govpay.bd.anagrafica.PortaliBD;
import it.govpay.bd.anagrafica.TipiTributoBD;
import it.govpay.bd.anagrafica.filters.TipoTributoFilter;
import it.govpay.model.Acl;
import it.govpay.model.Portale;
import it.govpay.model.TipoTributo;
import it.govpay.model.Acl.Servizio;
import it.govpay.model.Acl.Tipo;
import it.govpay.web.rs.dars.model.RawParamValue;
import it.govpay.web.rs.dars.model.Voce;
import it.govpay.web.rs.dars.model.input.dinamic.MultiSelectList;
import it.govpay.web.utils.Utils;

public class TipiTributoPO extends MultiSelectList<Long, List<Long>>{

	private String pagamentiOnlineId= null;
	private String portaleId = null;
	private String nomeServizio = null;
	private String trustedId = null;
	private Servizio servizio = Servizio.PAGAMENTI_ATTESA;
	private Tipo tipo = Tipo.TRIBUTO;

	public TipiTributoPO(String nomeServizio,String id, String label, URI refreshUri, List<RawParamValue> paramValues,
			Object... objects) {
		super(id, label, refreshUri, paramValues, objects);
		this.nomeServizio = nomeServizio;
		this.pagamentiOnlineId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".pagamentiOnline.id");
		this.portaleId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".id.id");
		this.trustedId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".trusted.id");
	}

	@Override
	protected List<Voce<Long>> getValues(List<RawParamValue> paramValues, Object... objects) throws ServiceException {
		String pagamentiOnlineValue = Utils.getValue(paramValues, this.pagamentiOnlineId);
		String trustedValue = Utils.getValue(paramValues, this.trustedId);
		List<Voce<Long>> lst = new ArrayList<Voce<Long>>();

		
		if(StringUtils.isNotEmpty(pagamentiOnlineValue) && pagamentiOnlineValue.equalsIgnoreCase("false")){
			return lst;
		}
		
		if(StringUtils.isNotEmpty(trustedValue) && trustedValue.equalsIgnoreCase("true")){
			return lst;
		}

		try {
			BasicBD bd = (BasicBD) objects[0];
			TipiTributoBD tributiBD = new TipiTributoBD(bd);

			TipoTributoFilter filter = tributiBD.newFilter();
			FilterSortWrapper fsw = new FilterSortWrapper();
			fsw.setField(it.govpay.orm.TipoTributo.model().COD_TRIBUTO);
			fsw.setSortOrder(SortOrder.ASC);
			filter.getFilterSortList().add(fsw);

			List<TipoTributo> findAll = tributiBD.findAll(filter);

			lst.add(Utils.getVoceTutti());
			for(TipoTributo tributo : findAll) {
				StringBuilder sb = new StringBuilder();

				sb.append( tributo.getDescrizione());
				sb.append(" (").append(tributo.getCodTributo()).append(")");
				
				lst.add(new Voce<Long>(sb.toString(), tributo.getId())); 
			}
		} catch (Exception e) {
			throw new ServiceException(e);
		}

		return lst;
	}

	@Override
	protected List<Long> getDefaultValue(List<RawParamValue> values, Object... objects) {
		String pagamentiOnlineValue = Utils.getValue(values, this.pagamentiOnlineId);
		String idPortale = Utils.getValue(values, this.portaleId);
		String trustedValue = Utils.getValue(values, this.trustedId);
		List<Long> lst = new ArrayList<Long>();

		if(StringUtils.isNotEmpty(pagamentiOnlineValue) && pagamentiOnlineValue.equalsIgnoreCase("false")){
			return lst;
		}
		
		if(StringUtils.isNotEmpty(trustedValue) && trustedValue.equalsIgnoreCase("true")){
			return lst;
		}
		
		if(StringUtils.isEmpty(idPortale)){
			return lst;
		}

		try {
			BasicBD bd = (BasicBD) objects[0];
			PortaliBD portaliBD = new PortaliBD(bd);
			Portale portale = portaliBD.getPortale(Long.parseLong(idPortale));
			List<Acl> acls = portale.getAcls();
			
			for (Acl acl : acls) {
				Tipo tipo = acl.getTipo();
				if(acl.getServizio().equals(this.servizio) && tipo.equals(this.tipo)){
					if(acl.getIdTributo() == null){
						lst.clear();
						lst.add(-1L);
						break;
					}else{
						lst.add(acl.getIdTributo());
					}
				}
			}
		} catch (Exception e) {
		}

		return lst;
	}
	@Override
	protected boolean isRequired(List<RawParamValue> values, Object... objects) {
		String pagamentiOnlineValue = Utils.getValue(values, this.pagamentiOnlineId);
		String trustedValue = Utils.getValue(values, this.trustedId);

		if(StringUtils.isNotEmpty(pagamentiOnlineValue) && pagamentiOnlineValue.equalsIgnoreCase("false")){
			return false;
		}
		
		if(StringUtils.isNotEmpty(trustedValue) && trustedValue.equalsIgnoreCase("true")){
			return false;
		}

		return true;
	}
	@Override
	protected boolean isHidden(List<RawParamValue> values, Object... objects) {
		String pagamentiOnlineValue = Utils.getValue(values, this.pagamentiOnlineId);
		String trustedValue = Utils.getValue(values, this.trustedId);
		if(StringUtils.isNotEmpty(pagamentiOnlineValue) && pagamentiOnlineValue.equalsIgnoreCase("false")){
			return true;
		}
		
		if(StringUtils.isNotEmpty(trustedValue) && trustedValue.equalsIgnoreCase("true")){
			return true;
		}

		return false;
	}
	@Override
	protected boolean isEditable(List<RawParamValue> values, Object... objects) {
		String pagamentiOnlineValue = Utils.getValue(values, this.pagamentiOnlineId);
		String trustedValue = Utils.getValue(values, this.trustedId);
		if(StringUtils.isNotEmpty(pagamentiOnlineValue) && pagamentiOnlineValue.equalsIgnoreCase("false")){
			return false;
		}
		
		if(StringUtils.isNotEmpty(trustedValue) && trustedValue.equalsIgnoreCase("true")){
			return false;
		}
		
		return true;
	}
}

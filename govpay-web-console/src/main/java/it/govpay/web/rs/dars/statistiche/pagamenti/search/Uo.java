package it.govpay.web.rs.dars.statistiche.pagamenti.search;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.SortOrder;

import it.govpay.bd.BasicBD;
import it.govpay.bd.FilterSortWrapper;
import it.govpay.bd.anagrafica.UnitaOperativeBD;
import it.govpay.bd.anagrafica.filters.UnitaOperativaFilter;
import it.govpay.bd.model.UnitaOperativa;
import it.govpay.web.rs.dars.model.RawParamValue;
import it.govpay.web.rs.dars.model.Voce;
import it.govpay.web.rs.dars.model.input.dinamic.SelectList;
import it.govpay.web.utils.Utils;

public class Uo extends SelectList<Long>{
	
	private String idDominioId= null;
	private String nomeServizio = null;

	public Uo(String nomeServizio,String id, String label, URI refreshUri, List<RawParamValue> paramValues,
			Object... objects) {
		super(id, label, refreshUri, paramValues, objects);
		this.nomeServizio = nomeServizio;
		Locale locale = objects[1] != null ? (Locale) objects[1] : null;
		this.idDominioId = Utils.getInstance(locale).getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");
	}

	@Override
	protected List<Voce<Long>> getValues(List<RawParamValue> paramValues, Object... objects) throws ServiceException {
		String idDominioValue = Utils.getValue(paramValues, this.idDominioId);
		List<Voce<Long>> lst = new ArrayList<Voce<Long>>();
		Locale locale = objects[1] != null ? (Locale) objects[1] : null;
		lst.add(new Voce<Long>(Utils.getInstance(locale).getMessageFromResourceBundle("commons.label.qualsiasi"), -1L));
		
		if(StringUtils.isEmpty(idDominioValue)){
			return lst;
		}
		
		try {
			BasicBD bd = (BasicBD) objects[0];
			UnitaOperativeBD uoBD = new UnitaOperativeBD(bd);
			UnitaOperativaFilter uoFilter = uoBD.newFilter();
			FilterSortWrapper fsw = new FilterSortWrapper();
			fsw.setField(it.govpay.orm.Uo.model().COD_UO);
			fsw.setSortOrder(SortOrder.ASC);
			uoFilter.getFilterSortList().add(fsw);
			uoFilter.setExcludeEC(true);
			uoFilter.setDominioFilter(Long.parseLong(idDominioValue));
			List<UnitaOperativa> findAll = uoBD.findAll(uoFilter); 
			
			
			if(findAll != null && findAll.size() > 0){
				for (UnitaOperativa uo : findAll) {
					lst.add(new Voce<Long>(uo.getCodUo(), uo.getId()));  
				}
			}
			
		} catch (Exception e) {
			throw new ServiceException(e);
		}

		return lst;
	}

	@Override
	protected Long getDefaultValue(List<RawParamValue> values, Object... objects) {
		String idDominioValue = Utils.getValue(values, this.idDominioId);
		
		if(StringUtils.isEmpty(idDominioValue)){
			return -1L;
		}
		return -1L;
	}

	@Override
	protected boolean isRequired(List<RawParamValue> values, Object... objects) {
		return false;
	}

	@Override
	protected boolean isHidden(List<RawParamValue> values, Object... objects) {
		String idDominioValue = Utils.getValue(values, this.idDominioId);
		
		if(StringUtils.isEmpty(idDominioValue)){
			return true	;
		}
		
		try {
			BasicBD bd = (BasicBD) objects[0];
			UnitaOperativeBD uoBD = new UnitaOperativeBD(bd);
			UnitaOperativaFilter uoFilter = uoBD.newFilter();
			FilterSortWrapper fsw = new FilterSortWrapper();
			fsw.setField(it.govpay.orm.Uo.model().COD_UO);
			fsw.setSortOrder(SortOrder.ASC);
			uoFilter.getFilterSortList().add(fsw);
			uoFilter.setExcludeEC(true);
			uoFilter.setDominioFilter(Long.parseLong(idDominioValue));
			long count = uoBD.count(uoFilter);  
			
			if (count > 0){
				return false;
			}
			
		} catch (Exception e) { }
		return true;
	}

	@Override
	protected boolean isEditable(List<RawParamValue> values, Object... objects) {
		String idDominioValue = Utils.getValue(values, this.idDominioId);
		if(StringUtils.isNotEmpty(idDominioValue)){
			return true;
		}
		return false;
	}

}

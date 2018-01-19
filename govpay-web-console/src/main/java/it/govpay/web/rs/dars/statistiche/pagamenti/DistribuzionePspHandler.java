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
package it.govpay.web.rs.dars.statistiche.pagamenti;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.SortOrder;

import it.govpay.bd.BasicBD;
import it.govpay.bd.FilterSortWrapper;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.anagrafica.DominiBD;
import it.govpay.bd.anagrafica.TipiTributoBD;
import it.govpay.bd.anagrafica.filters.DominioFilter;
import it.govpay.bd.anagrafica.filters.TipoTributoFilter;
import it.govpay.bd.model.Dominio;
import it.govpay.bd.reportistica.statistiche.PagamentiBD;
import it.govpay.bd.reportistica.statistiche.filters.PagamentiFilter;
import it.govpay.model.TipoTributo;
import it.govpay.model.reportistica.statistiche.DistribuzionePsp;
import it.govpay.model.reportistica.statistiche.TipoIntervallo;
import it.govpay.web.rs.dars.anagrafica.domini.Domini;
import it.govpay.web.rs.dars.anagrafica.domini.DominiHandler;
import it.govpay.web.rs.dars.base.BaseDarsService;
import it.govpay.web.rs.dars.base.StatisticaDarsHandler;
import it.govpay.web.rs.dars.exception.ConsoleException;
import it.govpay.web.rs.dars.exception.ExportException;
import it.govpay.web.rs.dars.handler.IStatisticaDarsHandler;
import it.govpay.web.rs.dars.model.InfoForm;
import it.govpay.web.rs.dars.model.InfoForm.Sezione;
import it.govpay.web.rs.dars.model.RawParamValue;
import it.govpay.web.rs.dars.model.Voce;
import it.govpay.web.rs.dars.model.input.ParamField;
import it.govpay.web.rs.dars.model.input.RefreshableParamField;
import it.govpay.web.rs.dars.model.input.base.InputDate;
import it.govpay.web.rs.dars.model.input.base.InputNumber;
import it.govpay.web.rs.dars.model.input.base.SelectList;
import it.govpay.web.rs.dars.model.statistiche.Grafico;
import it.govpay.web.rs.dars.model.statistiche.Grafico.TipoGrafico;
import it.govpay.web.rs.dars.model.statistiche.PaginaGrafico;
import it.govpay.web.rs.dars.model.statistiche.Serie;
import it.govpay.web.rs.dars.statistiche.pagamenti.search.Uo;
import it.govpay.web.utils.Utils;

public class DistribuzionePspHandler extends StatisticaDarsHandler<DistribuzionePsp> implements IStatisticaDarsHandler<DistribuzionePsp>{

	public static final String ANAGRAFICA_DEBITORE = "anagrafica";
	private SimpleDateFormat sdf = null;
	private SimpleDateFormat sdfGiorno =null;
	private SimpleDateFormat sdfMese = null;   

	public DistribuzionePspHandler(Logger log, BaseDarsService darsService) { 
		super(log, darsService);
		this.sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", this.getLanguage());  
		this.sdfGiorno = new SimpleDateFormat("dd/MM/yyyy", this.getLanguage());
		this.sdfMese = new SimpleDateFormat("MMMMM yyyy", this.getLanguage());  
	}


	@Override
	public PaginaGrafico getGrafico(UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException {
		String methodName = "getGrafico " + this.titoloServizio;
		try{	
			// Operazione consentita solo agli utenti che hanno almeno un ruolo consentito per la funzionalita'
			this.darsService.checkDirittiServizio(bd, this.funzionalita);

			this.log.info("Esecuzione " + methodName + " in corso..."); 

			PagamentiBD pagamentiBD = new PagamentiBD(bd);

			PagamentiFilter filter = pagamentiBD.newFilter();
			this.popolaFiltroRicerca(uriInfo, pagamentiBD, filter);

//			Calendar calendar = Calendar.getInstance();
//			calendar.setTime(filter.getData());
			SimpleDateFormat _sdf = null;
			switch (filter.getTipoIntervallo()) {
			case MENSILE:
//				calendar.add(Calendar.MONTH, - (filter.getLimit() -1 ));
				_sdf = this.sdfMese;
				break;
			case GIORNALIERO:
//				calendar.add(Calendar.DATE, - (filter.getLimit() -1 ));
				_sdf= this.sdfGiorno;
				break;
			case ORARIO:
//				calendar.add(Calendar.HOUR, - (filter.getLimit() -1 ));
				_sdf = this.sdf;
				break;
			}
//			Date start = calendar.getTime();
			String sottoTitolo = Utils.getInstance(this.getLanguage()).getMessageWithParamsFromResourceBundle(this.nomeServizio + ".label.sottotitolo."+ filter.getTipoIntervallo(),_sdf.format(filter.getData()));



			// visualizza la ricerca solo se i risultati sono > del limit
			InfoForm infoRicerca = this.getInfoRicerca(uriInfo, bd);
			Map<String, ParamField<?>> infoGrafico = this.getInfoGrafico(uriInfo, bd); 
			
			// valorizzo i valori da restitire al client
			infoGrafico = this.valorizzaInfoGrafico(uriInfo, bd, filter, infoGrafico);

			List<DistribuzionePsp> distribuzioneEsiti = pagamentiBD.getDistribuzionePsp(filter);

			this.log.info("Esecuzione " + methodName + " completata.");

			PaginaGrafico paginaGrafico = new PaginaGrafico(this.titoloServizio, this.getInfoEsportazione(uriInfo,bd), infoRicerca,infoGrafico); 

			Grafico grafico = new Grafico(TipoGrafico.pie);

			grafico.setTitolo(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".label.titolo"));
			grafico.setSottotitolo(sottoTitolo); 
			grafico.setColoriAutomatici(true);

			if (distribuzioneEsiti != null && distribuzioneEsiti.size() > 0) {
				Serie<Long> serie1 = new Serie<Long>();
				for (DistribuzionePsp elemento : distribuzioneEsiti) {
					String dataPsp = elemento.getPsp();
					long pagamenti = elemento.getNumero();

					grafico.getCategorie().add(Utils.getInstance(this.getLanguage()).getMessageWithParamsFromResourceBundle(this.nomeServizio + ".psp.label", dataPsp));
					//					grafico.getValoriX().add(dataElemento);
					serie1.getDati().add(pagamenti);
					serie1.getTooltip().add(Utils.getInstance(this.getLanguage()).getMessageWithParamsFromResourceBundle(this.nomeServizio + ".psp.tooltip", dataPsp, pagamenti));
				}

				grafico.getSerie().add(serie1);
			}

			paginaGrafico.setGrafico(grafico );

			return paginaGrafico;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	private boolean popolaFiltroRicerca(UriInfo uriInfo, BasicBD bd, PagamentiFilter filter) throws ConsoleException, Exception {
		Set<Long> setDomini = this.darsService.getIdDominiAbilitatiLetturaServizio(bd, this.funzionalita);
		boolean eseguiRicerca = !setDomini.isEmpty(); // isAdmin;

		String idDominioId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");
		String idDominio = this.getParameter(uriInfo, idDominioId, String.class);

		if(StringUtils.isNotEmpty(idDominio)){

			long idDom = -1l;
			try{
				idDom = Long.parseLong(idDominio);
			}catch(Exception e){ idDom = -1l;	}
			if(idDom > 0){
				filter.setCodDominio(AnagraficaManager.getDominio(bd, idDom).getCodDominio());
				//				filter.setIdDomini(idDomini);
			}
		}
		
		String idUoId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idUo.id");
		String idUo = this.getParameter(uriInfo, idUoId, String.class);
		if(StringUtils.isNotEmpty(idUo)){
			long idUoL = -1l;
			try{
				idUoL = Long.parseLong(idUo);
			}catch(Exception e){ idUoL = -1l;	}
			if(idUoL > 0){
				filter.setCodUo(AnagraficaManager.getUnitaOperativa(bd, idUoL).getCodUo());
			}
		}
		
		String idTributoId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idTributo.id");
		String idTributo = this.getParameter(uriInfo, idTributoId, String.class);
		if(StringUtils.isNotEmpty(idTributo)){
			long idTributoL = -1l;
			try{
				idTributoL = Long.parseLong(idTributo);
			}catch(Exception e){ idTributoL = -1l;	}
			if(idTributoL > 0){
				filter.setCodTributo(AnagraficaManager.getTributo(bd, idTributoL).getCodTributo());
			}
		}
		
		filter = (PagamentiFilter) popoloFiltroStatistiche(uriInfo, bd, filter);
		

		return eseguiRicerca ;
	}

//	private boolean popolaFiltroRicerca(List<RawParamValue> rawValues, BasicBD bd, VersamentoFilter filter) throws ConsoleException, Exception {
//		//		Set<Long> setDomini = this.darsService.getIdDominiAbilitatiLetturaServizio(bd, this.funzionalita);
//		List<Long> idPsps = new ArrayList<Long>();
//		List<Long> idDomini = new ArrayList<Long>();
//		boolean eseguiRicerca = true;  
//
//		String idPspId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idPsp.id");
//		String idPsp = Utils.getValue(rawValues, idPspId);
//		if(StringUtils.isNotEmpty(idPsp)){
//			long idPspL = -1l;
//			try{
//				idPspL = Long.parseLong(idPsp);
//			}catch(Exception e){ idPspL = -1l;	}
//			if(idPspL > 0){
//				idPsps.add(idPspL);
//				//	filter.setIdPsp(idPsps);
//			}
//		}
//
//		String idDominioId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");
//		String idDominio = Utils.getValue(rawValues, idDominioId);
//		if(StringUtils.isNotEmpty(idDominio)){
//			long idDom = -1l;
//			try{
//				idDom = Long.parseLong(idDominio);
//			}catch(Exception e){ idDom = -1l;	}
//			if(idDom > 0){
//				idDomini.add(idDom);
//			}
//		}
//
//		return eseguiRicerca  ;
//	}

	@SuppressWarnings("unchecked")
	@Override
	public InfoForm getInfoRicerca(UriInfo uriInfo, BasicBD bd, boolean visualizzaRicerca, Map<String,String> parameters) throws ConsoleException {
		URI ricerca = this.getUriRicerca(uriInfo, bd);
		InfoForm infoRicerca = new InfoForm(ricerca);

		if(visualizzaRicerca) {
			String idDominioId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");
			String idTributoId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idTributo.id");
			String idUoId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idUo.id");

			if(this.infoRicercaMap == null){
				this.initInfoRicerca(uriInfo, bd);
			}

			Sezione sezioneRoot = infoRicerca.getSezioneRoot();

			try{
				// Operazione consentita solo agli utenti che hanno almeno un ruolo consentito per la funzionalita'
				this.darsService.checkDirittiServizio(bd, this.funzionalita);

				// idDominio
				List<Voce<Long>> domini = new ArrayList<Voce<Long>>();

				DominiBD dominiBD = new DominiBD(bd);
				DominioFilter filter;
				try {
					filter = dominiBD.newFilter();
					domini.add(new Voce<Long>(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("commons.label.qualsiasi"), -1L));
					FilterSortWrapper fsw = new FilterSortWrapper();
					fsw.setField(it.govpay.orm.Dominio.model().COD_DOMINIO);
					fsw.setSortOrder(SortOrder.ASC);
					filter.getFilterSortList().add(fsw);
					List<Dominio> findAll = dominiBD.findAll(filter );

					Domini dominiDars = new Domini();
					DominiHandler dominiHandler = (DominiHandler) dominiDars.getDarsHandler();

					if(findAll != null && findAll.size() > 0){
						for (Dominio dominio : findAll) {
							domini.add(new Voce<Long>(dominiHandler.getTitolo(dominio,bd), dominio.getId()));  
						}
					}
				} catch (ServiceException e) {
					throw new ConsoleException(e);
				}
				SelectList<Long> idDominio = (SelectList<Long>) this.infoRicercaMap.get(idDominioId);
				idDominio.setDefaultValue(-1L);
				idDominio.setValues(domini); 
				sezioneRoot.addField(idDominio);

			}catch(Exception e){
				throw new ConsoleException(e);
			}
			
			// uo
			List<RawParamValue> idDominioDependencyValues = new ArrayList<RawParamValue>();
			idDominioDependencyValues.add(new RawParamValue(idDominioId, null));
			Uo uo = (Uo) this.infoRicercaMap.get(idUoId);
			uo.init(idDominioDependencyValues, bd,this.getLanguage());
			sezioneRoot.addField(uo);

			// tributi
			try{
				// psp
				List<Voce<Long>> tributi  = new ArrayList<Voce<Long>>();

				TipiTributoBD tipiTributoBD = new TipiTributoBD(bd);
				TipoTributoFilter filter;
				try {
					filter = tipiTributoBD.newFilter();
					tributi.add(new Voce<Long>(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("commons.label.qualsiasi"), -1L));
					FilterSortWrapper fsw = new FilterSortWrapper();
					fsw.setField(it.govpay.orm.TipoTributo.model().DESCRIZIONE);
					fsw.setSortOrder(SortOrder.ASC);
					filter.getFilterSortList().add(fsw);
					List<TipoTributo> findAll = tipiTributoBD.findAll(filter);

					if(findAll != null && findAll.size() > 0){
						for (TipoTributo _tipoTributo : findAll) {
							tributi.add(new Voce<Long>(_tipoTributo.getDescrizione(), _tipoTributo.getId()));  
						}
					}
				} catch (ServiceException e) {
					throw new ConsoleException(e);
				}
				SelectList<Long> idTributo = (SelectList<Long>) this.infoRicercaMap.get(idTributoId);
				idTributo.setDefaultValue(-1L);
				idTributo.setValues(tributi); 
				sezioneRoot.addField(idTributo);

			}catch(Exception e){
				throw new ConsoleException(e);
			}
		}
		return infoRicerca;
	}

	private void initInfoRicerca(UriInfo uriInfo, BasicBD bd) throws ConsoleException{
		if(this.infoRicercaMap == null){
			this.infoRicercaMap = new HashMap<String, ParamField<?>>();

			String idDominioId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");
			String idTributoId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idTributo.id");
			String idUoId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idUo.id");
			
			List<Voce<Long>> domini = new ArrayList<Voce<Long>>();
			// idDominio
			String idDominioLabel = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idDominio.label");
			SelectList<Long> idDominio = new SelectList<Long>(idDominioId, idDominioLabel, null, false, false, true, domini);
			this.infoRicercaMap.put(idDominioId, idDominio);
			
			List<Voce<Long>> tributi = new ArrayList<Voce<Long>>();
			// idTributo
			String idTributoLabel = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idTributo.label");
			SelectList<Long> idTributo = new SelectList<Long>(idTributoId, idTributoLabel, null, false, false, true, tributi);
			this.infoRicercaMap.put(idTributoId, idTributo);

			// uo
			String idUoLabel = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle(this.nomeServizio + ".idUo.label");
			List<RawParamValue> idDominioDependencyValues = new ArrayList<RawParamValue>();
			idDominioDependencyValues.add(new RawParamValue(idDominioId, null));
			URI uoRefreshUri = this.getUriSearchField(uriInfo, bd, idUoId); 
			Uo uo = new Uo(this.nomeServizio, idUoId, idUoLabel, uoRefreshUri, idDominioDependencyValues, bd,this.getLanguage());
			uo.addDependencyField(idDominio);
			uo.init(idDominioDependencyValues, bd,this.getLanguage());
			
			this.infoRicercaMap.put(idUoId, uo);
			
			this.initInfoGrafico(uriInfo,bd);
		}
	}
	
	protected void initInfoGrafico(UriInfo uriInfo, BasicBD bd) throws ConsoleException{
		if(this.infoRicercaMap == null)
			this.infoRicercaMap = new HashMap<String, ParamField<?>>();

		String dataId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.data.id");
		String colonneId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.colonne.id");
		String tipoIntervalloId = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo.id");

		InputNumber colonne = new InputNumber(colonneId, null, null, true, true, false, 1, 20);
		this.infoRicercaMap.put(colonneId, colonne);

		InputDate data = new InputDate(dataId, null, new Date(), false, false, true, null, null);
		this.infoRicercaMap.put(dataId, data);

		List<Voce<String>> tipiIntervallo = new ArrayList<Voce<String>>(); //tipoIntervallo.ORARIO.label
//		tipiIntervallo.add(new Voce<String>(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.ORARIO.name()+".label"),
//				Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.ORARIO.name()+".value")));
		tipiIntervallo.add(new Voce<String>(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.GIORNALIERO.name()+".label"),
				Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.GIORNALIERO.name()+".value")));
		tipiIntervallo.add(new Voce<String>(Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.MENSILE.name()+".label"),
				Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.MENSILE.name()+".value")));
		String tipoIntervalloLabel = Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo.label");
		SelectList<String> tipoIntervallo = new SelectList<String>(tipoIntervalloId, tipoIntervalloLabel, 
				Utils.getInstance(this.getLanguage()).getMessageFromResourceBundle("statistiche.tipoIntervallo."+TipoIntervallo.GIORNALIERO.name()+".value"), false, false, true, tipiIntervallo );
		this.infoRicercaMap.put(tipoIntervalloId, tipoIntervallo);
	}

	@Override
	public Object getSearchField(UriInfo uriInfo, List<RawParamValue> values, String fieldId, BasicBD bd)	throws WebApplicationException, ConsoleException { 	
		this.log.debug("Richiesto search field ["+fieldId+"]");
		try{
			// Operazione consentita solo ai ruoli con diritto di scrittura
			this.darsService.checkDirittiServizioScrittura(bd, this.funzionalita); 

			if(this.infoRicercaMap == null){
				this.initInfoRicerca(uriInfo, bd);
			}

			if(this.infoRicercaMap.containsKey(fieldId)){
				RefreshableParamField<?> paramField = (RefreshableParamField<?>) this.infoRicercaMap.get(fieldId);

				paramField.aggiornaParametro(values,bd,this.getLanguage());

				return paramField;

			}

			this.log.debug("Field ["+fieldId+"] non presente.");

		}catch(Exception e){
			throw new ConsoleException(e);
		}
		return null; 
	}

	@Override
	public Object getExportField(UriInfo uriInfo, List<RawParamValue> values, String fieldId, BasicBD bd) throws WebApplicationException, ConsoleException { return null; }

	@Override
	public String getTitolo(DistribuzionePsp entry,BasicBD bd) {
		StringBuilder sb = new StringBuilder();

		sb.append(Utils.getInstance(this.getLanguage()).getMessageWithParamsFromResourceBundle(this.nomeServizio + ".label.titolo"));

		return sb.toString();
	}

	@Override
	public String getSottotitolo(DistribuzionePsp entry,BasicBD bd) {
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	} 

	@Override
	public Map<String, Voce<String>> getVoci(DistribuzionePsp entry, BasicBD bd) throws ConsoleException {
		Map<String, Voce<String>> voci = new HashMap<String, Voce<String>>();
		return voci; 
	}

	@Override
	public InfoForm getInfoEsportazione(UriInfo uriInfo, BasicBD bd, Map<String, String> parameters) throws ConsoleException { 
		InfoForm infoEsportazione = null;
		//		try{
		//			if(this.darsService.isServizioAbilitatoLettura(bd, this.funzionalita)){
		//				URI esportazione = this.getUriEsportazione(uriInfo, bd);
		//				infoEsportazione = new InfoForm(esportazione);
		//			}
		//		}catch(ServiceException e){
		//			throw new ConsoleException(e);
		//		}
		return infoEsportazione;
	}

	@Override
	public InfoForm getInfoEsportazioneDettaglio(UriInfo uriInfo, BasicBD bd, DistribuzionePsp entry)	throws ConsoleException {	
		InfoForm infoEsportazione = null;
		return infoEsportazione;		
	}

	@Override
	public String esporta(List<Long> idsToExport, List<RawParamValue> rawValues, UriInfo uriInfo, BasicBD bd, ZipOutputStream zout)
			throws WebApplicationException, ConsoleException,ExportException {
		return null;
	}

	@Override
	public String esporta(Long idToExport, List<RawParamValue> rawValues, UriInfo uriInfo, BasicBD bd, ZipOutputStream zout) throws WebApplicationException, ConsoleException,ExportException { return null; }

}

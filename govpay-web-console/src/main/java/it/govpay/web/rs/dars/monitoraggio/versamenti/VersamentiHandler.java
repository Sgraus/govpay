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
package it.govpay.web.rs.dars.monitoraggio.versamenti;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.SortOrder;
import org.openspcoop2.utils.csv.Printer;

import it.gov.digitpa.schemas._2011.pagamenti.CtRicevutaTelematica;
import it.gov.digitpa.schemas._2011.pagamenti.revoche.CtEsitoRevoca;
import it.govpay.bd.BasicBD;
import it.govpay.bd.FilterSortWrapper;
import it.govpay.bd.anagrafica.AclBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.anagrafica.DominiBD;
import it.govpay.bd.anagrafica.filters.DominioFilter;
import it.govpay.bd.model.Dominio;
import it.govpay.bd.model.Rpt;
import it.govpay.bd.model.Rr;
import it.govpay.bd.model.SingoloVersamento;
import it.govpay.bd.model.UnitaOperativa;
import it.govpay.bd.model.Versamento;
import it.govpay.bd.pagamento.EventiBD;
import it.govpay.bd.pagamento.RptBD;
import it.govpay.bd.pagamento.RrBD;
import it.govpay.bd.pagamento.VersamentiBD;
import it.govpay.bd.pagamento.filters.EventiFilter;
import it.govpay.bd.pagamento.filters.RptFilter;
import it.govpay.bd.pagamento.filters.RrFilter;
import it.govpay.bd.pagamento.filters.VersamentoFilter;
import it.govpay.bd.reportistica.EstrattiContoBD;
import it.govpay.bd.reportistica.filters.EstrattoContoFilter;
import it.govpay.core.utils.CSVUtils;
import it.govpay.core.utils.JaxbUtils;
import it.govpay.core.utils.RtUtils;
import it.govpay.model.Acl;
import it.govpay.model.Acl.Tipo;
import it.govpay.model.Anagrafica;
import it.govpay.model.Applicazione;
import it.govpay.model.EstrattoConto;
import it.govpay.model.Evento;
import it.govpay.model.Operatore;
import it.govpay.model.Operatore.ProfiloOperatore;
import it.govpay.model.Versamento.StatoVersamento;
import it.govpay.model.comparator.EstrattoContoComparator;
import it.govpay.stampe.pdf.er.ErPdf;
import it.govpay.stampe.pdf.rt.RtPdf;
import it.govpay.web.rs.BaseRsService;
import it.govpay.web.rs.dars.BaseDarsHandler;
import it.govpay.web.rs.dars.BaseDarsService;
import it.govpay.web.rs.dars.IDarsHandler;
import it.govpay.web.rs.dars.anagrafica.anagrafica.AnagraficaHandler;
import it.govpay.web.rs.dars.anagrafica.domini.Domini;
import it.govpay.web.rs.dars.anagrafica.domini.DominiHandler;
import it.govpay.web.rs.dars.exception.ConsoleException;
import it.govpay.web.rs.dars.exception.DuplicatedEntryException;
import it.govpay.web.rs.dars.exception.ValidationException;
import it.govpay.web.rs.dars.model.Dettaglio;
import it.govpay.web.rs.dars.model.Elemento;
import it.govpay.web.rs.dars.model.Elenco;
import it.govpay.web.rs.dars.model.InfoForm;
import it.govpay.web.rs.dars.model.InfoForm.Sezione;
import it.govpay.web.rs.dars.model.RawParamValue;
import it.govpay.web.rs.dars.model.Voce;
import it.govpay.web.rs.dars.model.input.ParamField;
import it.govpay.web.rs.dars.model.input.base.InputText;
import it.govpay.web.rs.dars.model.input.base.SelectList;
import it.govpay.web.rs.dars.monitoraggio.eventi.Eventi;
import it.govpay.web.rs.dars.monitoraggio.eventi.EventiHandler;
import it.govpay.web.utils.ConsoleProperties;
import it.govpay.web.utils.Utils;

public class VersamentiHandler extends BaseDarsHandler<Versamento> implements IDarsHandler<Versamento>{

	public static final String ANAGRAFICA_DEBITORE = "anagrafica";
	private static Map<String, ParamField<?>> infoRicercaMap = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");  

	public VersamentiHandler(Logger log, BaseDarsService darsService) { 
		super(log, darsService);
	}

	@Override
	public Elenco getElenco(UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException {
		String methodName = "getElenco " + this.titoloServizio;
		try{	
			// Operazione consentita agli utenti registrati
			Operatore operatore = this.darsService.getOperatoreByPrincipal(bd); 
			ProfiloOperatore profilo = operatore.getProfilo();
			boolean isAdmin = profilo.equals(ProfiloOperatore.ADMIN);


			Integer offset = this.getOffset(uriInfo);
			Integer limit = this.getLimit(uriInfo);
			URI esportazione = this.getUriEsportazione(uriInfo, bd); 
			URI cancellazione = null;

			this.log.info("Esecuzione " + methodName + " in corso..."); 

			VersamentiBD versamentiBD = new VersamentiBD(bd);
			AclBD aclBD = new AclBD(bd);
			List<Acl> aclOperatore = aclBD.getAclOperatore(operatore.getId());
			List<Long> idDomini = new ArrayList<Long>();
			VersamentoFilter filter = versamentiBD.newFilter();
			filter.setOffset(offset);
			filter.setLimit(limit);
			FilterSortWrapper fsw = new FilterSortWrapper();
			fsw.setField(it.govpay.orm.Versamento.model().DATA_ORA_ULTIMO_AGGIORNAMENTO);
			fsw.setSortOrder(SortOrder.DESC);
			filter.getFilterSortList().add(fsw);

			String cfDebitoreId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".cfDebitore.id");
			String cfDebitore = this.getParameter(uriInfo, cfDebitoreId, String.class);
			if(StringUtils.isNotEmpty(cfDebitore))
				filter.setCodUnivocoDebitore(cfDebitore); 

			String codVersamentoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codVersamento.id");
			String codVersamento = this.getParameter(uriInfo, codVersamentoId, String.class);
			if(StringUtils.isNotEmpty(codVersamento))
				filter.setCodVersamento(codVersamento);


			String idDominioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");
			String idDominio = this.getParameter(uriInfo, idDominioId, String.class);
			if(StringUtils.isNotEmpty(idDominio)){
				long idDom = -1l;
				try{
					idDom = Long.parseLong(idDominio);
				}catch(Exception e){ idDom = -1l;	}
				if(idDom > 0){
					idDomini.add(idDom);
					filter.setIdDomini(idDomini);
				}
			}

			String iuvId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".iuv.id");
			String iuv = this.getParameter(uriInfo, iuvId, String.class);
			if(StringUtils.isNotEmpty(iuv)){
				RptBD rptBD = new RptBD(bd);
				RptFilter newFilter = rptBD.newFilter();
				newFilter.setIuv(iuv);
				List<Rpt> findAll = rptBD.findAll(newFilter);
				List<Long> idVersamentoL = new ArrayList<Long>();
				for (Rpt rpt : findAll) {
					idVersamentoL.add(rpt.getIdVersamento());
				}

				filter.setIdVersamento(idVersamentoL);  
			}
			
			String statoVersamentoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento.id");
			String statoVersamento = this.getParameter(uriInfo, statoVersamentoId, String.class);
			
			if(StringUtils.isNotEmpty(statoVersamento)){
				filter.setStatoVersamento(StatoVersamento.valueOf(statoVersamento));
			}

			boolean eseguiRicerca = true; // isAdmin;
			// SE l'operatore non e' admin vede solo i versamenti associati ai domini definiti nelle ACL
			if(!isAdmin && idDomini.isEmpty()){
				boolean vediTuttiDomini = false;

				for(Acl acl: aclOperatore) {
					if(Tipo.DOMINIO.equals(acl.getTipo())) {
						if(acl.getIdDominio() == null) {
							vediTuttiDomini = true;
							break;
						} else {
							idDomini.add(acl.getIdDominio());
						}
					}
				}
				if(!vediTuttiDomini) {
					if(idDomini.isEmpty()) {
						eseguiRicerca = false;
					} else {
						filter.setIdDomini(idDomini);
					}
				}
			}

			long count = eseguiRicerca ? versamentiBD.count(filter) : 0;

			// visualizza la ricerca solo se i risultati sono > del limit
			boolean visualizzaRicerca = this.visualizzaRicerca(count, limit);
			InfoForm infoRicerca = visualizzaRicerca ? this.getInfoRicerca(uriInfo, bd) : null;

			Elenco elenco = new Elenco(this.titoloServizio, infoRicerca,
					this.getInfoCreazione(uriInfo, bd),
					count, esportazione, cancellazione); 

			UriBuilder uriDettaglioBuilder = BaseRsService.checkDarsURI(uriInfo).path(this.pathServizio).path("{id}");

			List<Versamento> findAll = eseguiRicerca ? versamentiBD.findAll(filter) : new ArrayList<Versamento>(); 

			if(findAll != null && findAll.size() > 0){
				for (Versamento entry : findAll) {
					elenco.getElenco().add(this.getElemento(entry, entry.getId(), uriDettaglioBuilder,bd));
				}
			}

			this.log.info("Esecuzione " + methodName + " completata.");

			return elenco;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public InfoForm getInfoRicerca(UriInfo uriInfo, BasicBD bd) throws ConsoleException {
		URI ricerca = this.getUriRicerca(uriInfo, bd);
		InfoForm infoRicerca = new InfoForm(ricerca);

		String cfDebitoreId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".cfDebitore.id");
		String codVersamentoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codVersamento.id");
		String idDominioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");
		String iuvId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".iuv.id");
		String statoVersamentoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento.id");

		if(infoRicercaMap == null){
			this.initInfoRicerca(uriInfo, bd);
		}

		Sezione sezioneRoot = infoRicerca.getSezioneRoot();
		SelectList<String> statoVersamento = (SelectList<String>) infoRicercaMap.get(statoVersamentoId);
		statoVersamento.setDefaultValue("");
		sezioneRoot.addField(statoVersamento);

		InputText cfDebitore = (InputText) infoRicercaMap.get(cfDebitoreId);
		cfDebitore.setDefaultValue(null);
		sezioneRoot.addField(cfDebitore);

		InputText codVersamento = (InputText) infoRicercaMap.get(codVersamentoId);
		codVersamento.setDefaultValue(null);
		sezioneRoot.addField(codVersamento);

		InputText iuv = (InputText) infoRicercaMap.get(iuvId);
		iuv.setDefaultValue(null);
		sezioneRoot.addField(iuv);

		try{

			Operatore operatore = this.darsService.getOperatoreByPrincipal(bd); 
			ProfiloOperatore profilo = operatore.getProfilo();
			boolean isAdmin = profilo.equals(ProfiloOperatore.ADMIN);

			// idDominio
			List<Voce<Long>> domini = new ArrayList<Voce<Long>>();

			DominiBD dominiBD = new DominiBD(bd);
			DominioFilter filter;
			try {
				filter = dominiBD.newFilter();
				boolean eseguiRicerca = true;
				if(isAdmin){

				} else {
					AclBD aclBD = new AclBD(bd);
					List<Acl> aclOperatore = aclBD.getAclOperatore(operatore.getId());

					boolean vediTuttiDomini = false;
					List<Long> idDomini = new ArrayList<Long>();
					for(Acl acl: aclOperatore) {
						if(Tipo.DOMINIO.equals(acl.getTipo())) {
							if(acl.getIdDominio() == null) {
								vediTuttiDomini = true;
								break;
							} else {
								idDomini.add(acl.getIdDominio());
							}
						}
					}
					if(!vediTuttiDomini) {
						if(idDomini.isEmpty()) {
							eseguiRicerca = false;
						} else {
							filter.setIdDomini(idDomini);
						}
					}
				}



				if(eseguiRicerca) {
					domini.add(new Voce<Long>(Utils.getInstance().getMessageFromResourceBundle("commons.label.qualsiasi"), -1L));
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
				}else {
					domini.add(new Voce<Long>(Utils.getInstance().getMessageFromResourceBundle("commons.label.qualsiasi"), -1L));
				}
			} catch (ServiceException e) {
				throw new ConsoleException(e);
			}
			SelectList<Long> idDominio = (SelectList<Long>) infoRicercaMap.get(idDominioId);
			idDominio.setDefaultValue(-1L);
			idDominio.setValues(domini); 
			sezioneRoot.addField(idDominio);

		}catch(Exception e){
			throw new ConsoleException(e);
		}

		return infoRicerca;
	}

	private void initInfoRicerca(UriInfo uriInfo, BasicBD bd) throws ConsoleException{
		if(infoRicercaMap == null){
			infoRicercaMap = new HashMap<String, ParamField<?>>();

			String cfDebitoreId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".cfDebitore.id");
			String codVersamentoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codVersamento.id");
			String idDominioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");
			String iuvId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".iuv.id");
			String statoVersamentoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento.id");

			// statoVersamento
			List<Voce<String>> stati = new ArrayList<Voce<String>>();
			stati.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle("commons.label.qualsiasi"), ""));
			stati.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento."+StatoVersamento.ESEGUITO), StatoVersamento.ESEGUITO.toString()));
			stati.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento."+StatoVersamento.NON_ESEGUITO), StatoVersamento.NON_ESEGUITO.toString()));
			stati.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento."+StatoVersamento.PARZIALMENTE_ESEGUITO), StatoVersamento.PARZIALMENTE_ESEGUITO.toString()));
			stati.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento."+StatoVersamento.ESEGUITO_SENZA_RPT), StatoVersamento.ESEGUITO_SENZA_RPT.toString()));
			stati.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento."+StatoVersamento.ANOMALO), StatoVersamento.ANOMALO.toString()));
			stati.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento."+StatoVersamento.ANNULLATO), StatoVersamento.ANNULLATO.toString()));
			
			String statoVersamentoLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento.label");
			SelectList<String> statoVersamento = new SelectList<String>(statoVersamentoId, statoVersamentoLabel, null, false, false, true, stati);
			infoRicercaMap.put(statoVersamentoId, statoVersamento);
			
			// cfDebitore
			String cfDebitoreLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".cfDebitore.label");
			InputText cfDebitore = new InputText(cfDebitoreId, cfDebitoreLabel, null, false, false, true, 1, 35);
			infoRicercaMap.put(cfDebitoreId, cfDebitore);

			// Id Versamento
			String codVersamentoLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codVersamento.label");
			InputText codVersamento = new InputText(codVersamentoId, codVersamentoLabel, null, false, false, true, 1, 35);
			infoRicercaMap.put(codVersamentoId, codVersamento);

			// iuv
			String iuvLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".iuv.label");
			InputText iuv = new InputText(iuvId, iuvLabel, null, false, false, true, 1, 35);
			infoRicercaMap.put(iuvId, iuv);			

			List<Voce<Long>> domini = new ArrayList<Voce<Long>>();
			// idDominio
			String idDominioLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idDominio.label");
			SelectList<Long> idDominio = new SelectList<Long>(idDominioId, idDominioLabel, null, false, false, true, domini);
			infoRicercaMap.put(idDominioId, idDominio);

		}
	}

	@Override
	public Object getField(UriInfo uriInfo, List<RawParamValue> values, String fieldId, BasicBD bd) throws WebApplicationException, ConsoleException {
		return null;
	}

	@Override
	public Dettaglio getDettaglio(long id, UriInfo uriInfo, BasicBD bd)
			throws WebApplicationException, ConsoleException {
		String methodName = "dettaglio " + this.titoloServizio + "."+ id;

		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			// Operazione consentita agli utenti registrati
			Operatore operatore = this.darsService.getOperatoreByPrincipal(bd); 
			ProfiloOperatore profilo = operatore.getProfilo();
			boolean isAdmin = profilo.equals(ProfiloOperatore.ADMIN);

			boolean eseguiRicerca = true; //isAdmin;
			// SE l'operatore non e' admin vede solo i versamenti associati alle sue UO ed applicazioni
			// controllo se l'operatore ha fatto una richiesta di visualizzazione di un versamento che puo' vedere
			if(!isAdmin){
				//				eseguiRicerca = !Utils.isEmpty(operatore.getIdApplicazioni()) || !Utils.isEmpty(operatore.getIdEnti());
				VersamentiBD versamentiBD = new VersamentiBD(bd);
				VersamentoFilter filter = versamentiBD.newFilter();
				//				filter.setIdApplicazioni(operatore.getIdApplicazioni());
				//				filter.setIdUo(operatore.getIdEnti()); 

				FilterSortWrapper fsw = new FilterSortWrapper();
				fsw.setField(it.govpay.orm.Versamento.model().DATA_CREAZIONE);
				fsw.setSortOrder(SortOrder.DESC);
				filter.getFilterSortList().add(fsw);

				long count = eseguiRicerca ? versamentiBD.count(filter) : 0;
				List<Long> idVersamentoL = new ArrayList<Long>();
				idVersamentoL.add(id);
				filter.setIdVersamento(idVersamentoL);

				eseguiRicerca = eseguiRicerca && count > 0;
			}

			// recupero oggetto
			VersamentiBD versamentiBD = new VersamentiBD(bd);
			Versamento versamento = eseguiRicerca ? versamentiBD.getVersamento(id) : null;

			InfoForm infoModifica = null;
			URI cancellazione = null;
			URI esportazione = this.getUriEsportazioneDettaglio(uriInfo, versamentiBD, id);

			String titolo = versamento != null ? this.getTitolo(versamento,bd) : "";
			Dettaglio dettaglio = new Dettaglio(titolo, esportazione, cancellazione, infoModifica);

			it.govpay.web.rs.dars.model.Sezione root = dettaglio.getSezioneRoot();

			if(versamento != null){

				if(StringUtils.isNotEmpty(versamento.getCodVersamentoEnte())) 
					root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codVersamentoEnte.label"), versamento.getCodVersamentoEnte());
				// Uo
				UnitaOperativa uo = versamento.getUo(bd);
				if(uo != null){
					Dominio dominio = uo.getDominio(bd);
					Domini dominiDars = new Domini();
					UriBuilder uriDettaglioDominioBuilder = BaseRsService.checkDarsURI(uriInfo).path(dominiDars.getPathServizio()).path("{id}");
					Elemento elemento = ((DominiHandler)dominiDars.getDarsHandler()).getElemento(dominio, dominio.getId(), uriDettaglioDominioBuilder, bd);
					root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idDominio.label"), elemento.getTitolo(),uriDettaglioDominioBuilder.build(dominio.getId())); 
				}

				// Applicazione
				Applicazione applicazione = versamento.getApplicazione(bd);
				if(applicazione != null)
					root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".applicazione.label"), applicazione.getCodApplicazione());  
				if(versamento.getStatoVersamento() != null)
					root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento.label"),
							Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento."+versamento.getStatoVersamento()));
				if(StringUtils.isNotEmpty(versamento.getDescrizioneStato())) 
					root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".descrizioneStato.label"), versamento.getDescrizioneStato());
				if(versamento.getImportoTotale() != null)
					root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".importoTotale.label"), versamento.getImportoTotale().toString()+ "€");
				if(versamento.getDataCreazione() != null)
					root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dataCreazione.label"), this.sdf.format(versamento.getDataCreazione()));
				if(versamento.getDataScadenza() != null)
					root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dataScadenza.label"), this.sdf.format(versamento.getDataScadenza()));
				if(versamento.getDataUltimoAggiornamento() != null)
					root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dataUltimoAggiornamento.label"), this.sdf.format(versamento.getDataUltimoAggiornamento()));
				root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".aggiornabile.label"), Utils.getSiNoAsLabel(versamento.isAggiornabile()));
				if(versamento.getCausaleVersamento() != null)
					root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".causaleVersamento.label"), versamento.getCausaleVersamento().toString());

				// Sezione Anagrafica Debitore

				Anagrafica anagrafica = versamento.getAnagraficaDebitore(); 
				it.govpay.web.rs.dars.model.Sezione sezioneAnagrafica = dettaglio.addSezione(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + ANAGRAFICA_DEBITORE + ".titolo"));
				AnagraficaHandler anagraficaHandler = new AnagraficaHandler(ANAGRAFICA_DEBITORE,this.nomeServizio,this.pathServizio);
				anagraficaHandler.fillSezioneAnagraficaUO(sezioneAnagrafica, anagrafica);

				// Singoli Versamenti
				String etichettaSingoliVersamenti = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.singoliVersamenti.titolo");
				it.govpay.web.rs.dars.model.Sezione sezioneSingoliVersamenti = dettaglio.addSezione(etichettaSingoliVersamenti);

				List<SingoloVersamento> singoliVersamenti = versamento.getSingoliVersamenti(bd);
				if(!Utils.isEmpty(singoliVersamenti)){
					SingoliVersamenti svDars = new SingoliVersamenti();
					SingoliVersamentiHandler svDarsHandler = (SingoliVersamentiHandler) svDars.getDarsHandler();
					UriBuilder uriDettaglioSVBuilder = BaseRsService.checkDarsURI(uriInfo).path(svDars.getPathServizio()).path("{id}");

					if(singoliVersamenti != null && singoliVersamenti.size() > 0){
						for (SingoloVersamento entry : singoliVersamenti) {
							Elemento elemento = svDarsHandler.getElemento(entry, entry.getId(), uriDettaglioSVBuilder,bd);
							sezioneSingoliVersamenti.addVoce(elemento.getTitolo(), elemento.getSottotitolo());
						}
					}
				}

				Pagamenti pagamentiDars = new Pagamenti();
				String etichettaPagamenti = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.pagamenti.titolo");
				String versamentoId = Utils.getInstance().getMessageFromResourceBundle(pagamentiDars.getNomeServizio() + ".idVersamento.id");
				UriBuilder uriBuilderPagamenti = BaseRsService.checkDarsURI(uriInfo).path(pagamentiDars.getPathServizio()).queryParam(versamentoId, versamento.getId());

				dettaglio.addElementoCorrelato(etichettaPagamenti, uriBuilderPagamenti.build()); 

				Transazioni transazioniDars = new Transazioni();
				String etichettaTransazioni = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.transazioni.titolo");
				versamentoId = Utils.getInstance().getMessageFromResourceBundle(transazioniDars.getNomeServizio()+ ".idVersamento.id");
				UriBuilder uriBuilder = BaseRsService.checkDarsURI(uriInfo).path(transazioniDars.getPathServizio()).queryParam(versamentoId, versamento.getId());

				dettaglio.addElementoCorrelato(etichettaTransazioni, uriBuilder.build());
			}

			this.log.info("Esecuzione " + methodName + " completata.");

			return dettaglio;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	@Override
	public String getTitolo(Versamento entry,BasicBD bd) {
		StringBuilder sb = new StringBuilder();

		String codVersamentoEnte = entry.getCodVersamentoEnte();

		StatoVersamento statoVersamento = entry.getStatoVersamento();

		String dominioLabel =  null;

		try{
			// Uo
			UnitaOperativa uo = entry.getUo(bd);
			if(uo != null){
				Dominio dominio = uo.getDominio(bd);
				Domini dominiDars = new Domini();
				Elemento elemento = ((DominiHandler)dominiDars.getDarsHandler()).getElemento(dominio, dominio.getId(), null, bd);
				dominioLabel = elemento.getTitolo(); 
			}
		}catch(Exception e){log.error(e);}

		switch (statoVersamento) {
		case NON_ESEGUITO:
			sb.append(Utils.getInstance().getMessageWithParamsFromResourceBundle(this.nomeServizio + ".label.titolo.nonEseguito", codVersamentoEnte,dominioLabel));
			break;
		case ANNULLATO:
		case ANOMALO:
		case ESEGUITO_SENZA_RPT:
		case PARZIALMENTE_ESEGUITO:
		case ESEGUITO:
		default:
			sb.append(
					Utils.getInstance().getMessageWithParamsFromResourceBundle(this.nomeServizio + ".label.titolo", codVersamentoEnte,dominioLabel,
							Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento."+statoVersamento.name())));
			break;
		}

		//		sb.append("Versamento ").append(codVersamentoEnte).append(" di ").append(importoTotale).append("€");

		return sb.toString();
	}

	@Override
	public String getSottotitolo(Versamento entry,BasicBD bd) {
		StringBuilder sb = new StringBuilder();
		Date dataUltimoAggiornamento = entry.getDataUltimoAggiornamento();


		StatoVersamento statoVersamento = entry.getStatoVersamento();
		Date dataScadenza = entry.getDataScadenza();

		switch (statoVersamento) {
		case NON_ESEGUITO:
			if(dataScadenza != null)
				sb.append(Utils.getInstance().getMessageWithParamsFromResourceBundle(this.nomeServizio + ".label.sottotitolo.nonEseguito",this.sdf.format(dataScadenza)));
			else 
				sb.append(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".label.sottotitolo.nonEseguito.noScadenza"));
			break;
		case ANOMALO:
			sb.append("");
			break;
		case ANNULLATO:
		case ESEGUITO_SENZA_RPT:
		case PARZIALMENTE_ESEGUITO:
		case ESEGUITO:
		default:
			sb.append(
					Utils.getInstance().getMessageWithParamsFromResourceBundle(this.nomeServizio + ".label.sottotitolo",
							Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".statoVersamento."+statoVersamento.name()),this.sdf.format(dataUltimoAggiornamento) ));
			break;
		}

		return sb.toString();
	} 

	@Override
	public List<String> getValori(Versamento entry, BasicBD bd) throws ConsoleException {
		return null;
	}

	@Override
	public String esporta(List<Long> idsToExport, UriInfo uriInfo, BasicBD bd, ZipOutputStream zout)
			throws WebApplicationException, ConsoleException {
		StringBuffer sb = new StringBuffer();
		if(idsToExport != null && idsToExport.size() > 0)
			for (Long long1 : idsToExport) {

				if(sb.length() > 0)
					sb.append(", ");

				sb.append(long1);
			}

		Printer printer  = null;
		String methodName = "esporta " + this.titoloServizio + "[" + sb.toString() + "]";
		int numeroZipEntries = 0;
		String pathLoghi = ConsoleProperties.getInstance().getPathEstrattoContoPdfLoghi();

		if(idsToExport.size() == 1)
			return this.esporta(idsToExport.get(0), uriInfo, bd, zout); 

		String fileName = "Export.zip";
		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			Operatore operatore = this.darsService.getOperatoreByPrincipal(bd); 
			ProfiloOperatore profilo = operatore.getProfilo();
			boolean isAdmin = profilo.equals(ProfiloOperatore.ADMIN);
			boolean eseguiRicerca = true;

			VersamentiBD versamentiBD = new VersamentiBD(bd);
			RptBD rptBD = new RptBD(bd);
			EventiBD eventiBd = new EventiBD(bd);
			Eventi eventiDars = new Eventi();
			it.govpay.core.business.EstrattoConto estrattoContoBD = new it.govpay.core.business.EstrattoConto(bd);
			EventiHandler eventiDarsHandler = (EventiHandler) eventiDars.getDarsHandler(); 

			Map<String, List<Long>> mappaInputEstrattoConto = new HashMap<String, List<Long>>();
			Map<String, Dominio> mappaInputDomini = new HashMap<String, Dominio>();

			VersamentoFilter filter = versamentiBD.newFilter();
			List<Long> ids = new ArrayList<Long>();
			ids = idsToExport;

			if(!isAdmin){

				AclBD aclBD = new AclBD(bd);
				List<Acl> aclOperatore = aclBD.getAclOperatore(operatore.getId());

				boolean vediTuttiDomini = false;
				List<Long> idDomini = new ArrayList<Long>();
				for(Acl acl: aclOperatore) {
					if(Tipo.DOMINIO.equals(acl.getTipo())) {
						if(acl.getIdDominio() == null) {
							vediTuttiDomini = true;
							break;
						} else {
							idDomini.add(acl.getIdDominio());
						}
					}
				}
				if(!vediTuttiDomini) {
					if(idDomini.isEmpty()) {
						eseguiRicerca = false;
					} else {
						filter.setIdDomini(idDomini);
					}
				}

				// l'operatore puo' vedere i domini associati, controllo se c'e' un versamento con Id nei domini concessi.
				if(eseguiRicerca){
					filter.setIdVersamento(ids);
					eseguiRicerca = eseguiRicerca && versamentiBD.count(filter) > 0;
				}
			}

			if(eseguiRicerca){
				for (Long idVersamento : idsToExport) {
					Versamento versamento = versamentiBD.getVersamento(idVersamento);

					// Prelevo il dominio
					UnitaOperativa uo  = AnagraficaManager.getUnitaOperativa(bd, versamento.getIdUo());
					Dominio dominio  = AnagraficaManager.getDominio(bd, uo.getIdDominio());

					String dirDominio = dominio.getCodDominio();

					// Aggrego i versamenti per dominio per generare gli estratti conto
					List<Long> idVersamentiDominio = null;
					if(mappaInputEstrattoConto.containsKey(dominio.getCodDominio()))
						idVersamentiDominio = mappaInputEstrattoConto.get(dominio.getCodDominio());
					else{
						idVersamentiDominio = new ArrayList<Long>();
						mappaInputEstrattoConto.put(dominio.getCodDominio(), idVersamentiDominio);
						mappaInputDomini.put(dominio.getCodDominio(), dominio);
					}
					idVersamentiDominio.add(idVersamento);

					String dirVersamento = dirDominio + "/" + versamento.getCodVersamentoEnte();

					RptFilter rptFilter = rptBD.newFilter();
					FilterSortWrapper rptFsw = new FilterSortWrapper();
					rptFsw.setField(it.govpay.orm.RPT.model().DATA_MSG_RICHIESTA);
					rptFsw.setSortOrder(SortOrder.DESC);
					rptFilter.getFilterSortList().add(rptFsw);
					rptFilter.setIdVersamento(idVersamento); 

					RrBD rrBD = new RrBD(bd);
					FilterSortWrapper rrFsw = new FilterSortWrapper();
					rrFsw.setField(it.govpay.orm.RR.model().DATA_MSG_REVOCA);
					rrFsw.setSortOrder(SortOrder.DESC);

					List<Rpt> listaRpt = rptBD.findAll(rptFilter);
					if(listaRpt != null && listaRpt.size() >0 )
						for (Rpt rpt : listaRpt) {
							numeroZipEntries ++;

							String iuv = rpt.getIuv();
							String ccp = rpt.getCcp();

							String iuvCcpDir = dirVersamento + "/" + iuv;

							// non appendo il ccp nel caso sia uguale ad 'n/a' altrimenti crea un nuovo livello di directory;
							if(!StringUtils.equalsIgnoreCase(ccp, "n/a"))
								iuvCcpDir = iuvCcpDir  + "_" + ccp;

							String rptEntryName = iuvCcpDir + "/rpt_" + rpt.getCodMsgRichiesta() + ".xml"; 


							ZipEntry rptXml = new ZipEntry(rptEntryName);
							zout.putNextEntry(rptXml);
							zout.write(rpt.getXmlRpt());
							zout.closeEntry();

							if(rpt.getXmlRt() != null){
								numeroZipEntries ++;
								String rtEntryName = iuvCcpDir + "/rt_" + rpt.getCodMsgRichiesta() + ".xml";
								ZipEntry rtXml = new ZipEntry(rtEntryName);
								zout.putNextEntry(rtXml);
								zout.write(rpt.getXmlRt());
								zout.closeEntry();

								// RT in formato pdf
								String tipoFirma = rpt.getFirmaRichiesta().getCodifica();
								byte[] rtByteValidato = RtUtils.validaFirma(tipoFirma, rpt.getXmlRt(), dominio.getCodDominio());
								CtRicevutaTelematica rt = JaxbUtils.toRT(rtByteValidato);
								String causale = versamento.getCausaleVersamento().getSimple();
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								RtPdf.getPdfRicevutaPagamento(pathLoghi, rt, causale,baos,log);

								String rtPdfEntryName = iuvCcpDir + "/ricevuta_pagamento.pdf";
								numeroZipEntries ++;
								ZipEntry rtPdf = new ZipEntry(rtPdfEntryName);
								zout.putNextEntry(rtPdf);
								zout.write(baos.toByteArray());
								zout.closeEntry();
							}

							// Eventi
							String entryEventiCSV =  iuvCcpDir + "/eventi.csv";

							EventiFilter eventiFilter = eventiBd.newFilter();
							eventiFilter.setCodDominio(dominio.getCodDominio());
							eventiFilter.setIuv(iuv);
							eventiFilter.setCcp(ccp);
							FilterSortWrapper fsw = new FilterSortWrapper();
							fsw.setField(it.govpay.orm.Evento.model().DATA_1);
							fsw.setSortOrder(SortOrder.ASC);
							eventiFilter.getFilterSortList().add(fsw);

							List<Evento> findAllEventi = eventiBd.findAll(eventiFilter);
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							eventiDarsHandler.scriviCSVEventi(baos, findAllEventi);

							ZipEntry eventiCSV = new ZipEntry(entryEventiCSV);
							zout.putNextEntry(eventiCSV);
							zout.write(baos.toByteArray());
							zout.closeEntry();

							RrFilter rrFilter = rrBD.newFilter();
							rrFilter.getFilterSortList().add(rrFsw);
							rrFilter.setIdRpt(rpt.getId()); 
							List<Rr> findAll = rrBD.findAll(rrFilter);
							if(findAll != null && findAll.size() > 0){
								for (Rr rr : findAll) {
									numeroZipEntries ++;
									String rrEntryName = iuvCcpDir + "/rr_" + rr.getCodMsgRevoca() + ".xml"; 

									ZipEntry rrXml = new ZipEntry(rrEntryName);
									zout.putNextEntry(rrXml);
									zout.write(rr.getXmlRr());
									zout.closeEntry();

									if(rr.getXmlEr() != null){
										numeroZipEntries ++;
										String erEntryName = iuvCcpDir + "/er_" + rr.getCodMsgRevoca() + ".xml"; 
										ZipEntry rtXml = new ZipEntry(erEntryName);
										zout.putNextEntry(rtXml);
										zout.write(rr.getXmlEr());
										zout.closeEntry();

										// ER in formato pdf
										CtEsitoRevoca er = JaxbUtils.toER(rr.getXmlEr());
										String causale = versamento.getCausaleVersamento().getSimple();
										baos = new ByteArrayOutputStream();
										Dominio dominio3 = AnagraficaManager.getDominio(bd, er.getDominio().getIdentificativoDominio());
										ErPdf.getPdfEsitoRevoca(pathLoghi, er, dominio3, dominio3.getAnagrafica(bd), causale,baos,log);

										String erPdfEntryName = iuvCcpDir + "/esito_revoca.pdf";
										numeroZipEntries ++;
										ZipEntry erPdf = new ZipEntry(erPdfEntryName);
										zout.putNextEntry(erPdf);
										zout.write(baos.toByteArray());
										zout.closeEntry();
									}
								}
							}
						}
				}

				List<it.govpay.core.business.model.EstrattoConto> listInputEstrattoConto = new ArrayList<it.govpay.core.business.model.EstrattoConto>();
				for (String codDominio : mappaInputEstrattoConto.keySet()) {
					it.govpay.core.business.model.EstrattoConto input =  it.govpay.core.business.model.EstrattoConto.creaEstrattoContoVersamentiPDF(mappaInputDomini.get(codDominio), mappaInputEstrattoConto.get(codDominio)); 
					listInputEstrattoConto.add(input);
				}


				List<it.govpay.core.business.model.EstrattoConto> listOutputEstattoConto = estrattoContoBD.getEstrattoContoVersamenti(listInputEstrattoConto,pathLoghi);

				for (it.govpay.core.business.model.EstrattoConto estrattoContoOutput : listOutputEstattoConto) {
					Map<String, ByteArrayOutputStream> estrattoContoVersamenti = estrattoContoOutput.getOutput(); 
					for (String nomeEntry : estrattoContoVersamenti.keySet()) {
						numeroZipEntries ++;
						ByteArrayOutputStream baos = estrattoContoVersamenti.get(nomeEntry);
						ZipEntry estrattoContoEntry = new ZipEntry(estrattoContoOutput.getDominio().getCodDominio() + "/" + nomeEntry);
						zout.putNextEntry(estrattoContoEntry);
						zout.write(baos.toByteArray());
						zout.closeEntry();
					}


				}

				// Estratto Conto in formato CSV
				EstrattiContoBD estrattiContoBD = new EstrattiContoBD(bd);
				EstrattoContoFilter ecFilter = estrattiContoBD.newFilter();
				ecFilter.setIdVersamento(idsToExport); 
				List<EstrattoConto> findAll =  estrattiContoBD.estrattoContoFromIdVersamenti(ecFilter);

				if(findAll != null && findAll.size() > 0){
					//ordinamento record
					Collections.sort(findAll, new EstrattoContoComparator());
					numeroZipEntries ++;
					ByteArrayOutputStream baos  = new ByteArrayOutputStream();
					try{
						ZipEntry pagamentoCsv = new ZipEntry("estrattoConto.csv");
						zout.putNextEntry(pagamentoCsv);
						printer = new Printer(this.getFormat() , baos);
						printer.printRecord(CSVUtils.getEstrattoContoCsvHeader());
						for (EstrattoConto pagamento : findAll) {
							printer.printRecord(CSVUtils.getEstrattoContoAsCsvRow(pagamento,this.sdf));
						}
					}finally {
						try{
							if(printer!=null){
								printer.close();
							}
						}catch (Exception e) {
							throw new Exception("Errore durante la chiusura dello stream ",e);
						}
					}
					zout.write(baos.toByteArray());
					zout.closeEntry();
				}
			}

			// se non ho inserito nessuna entry
			if(numeroZipEntries == 0){
				String noEntriesTxt = "/README";
				ZipEntry entryTxt = new ZipEntry(noEntriesTxt);
				zout.putNextEntry(entryTxt);
				zout.write("Non sono state trovate informazioni sui versamenti selezionati.".getBytes());
				zout.closeEntry();
			}


			zout.flush();
			zout.close();

			this.log.info("Esecuzione " + methodName + " completata.");

			return fileName;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	@Override
	public String esporta(Long idToExport, UriInfo uriInfo, BasicBD bd, ZipOutputStream zout)
			throws WebApplicationException, ConsoleException {
		String methodName = "esporta " + this.titoloServizio + "[" + idToExport + "]";  
		Printer printer  = null;

		try{
			int numeroZipEntries = 0;
			this.log.info("Esecuzione " + methodName + " in corso...");
			Operatore operatore = this.darsService.getOperatoreByPrincipal(bd); 
			ProfiloOperatore profilo = operatore.getProfilo();
			boolean isAdmin = profilo.equals(ProfiloOperatore.ADMIN);

			boolean eseguiRicerca = true;
			VersamentiBD versamentiBD = new VersamentiBD(bd);
			RptBD rptBD = new RptBD(bd);
			EventiBD eventiBd = new EventiBD(bd);
			EstrattiContoBD estrattiContoBD = new EstrattiContoBD(bd);
			it.govpay.core.business.EstrattoConto estrattoContoBD = new it.govpay.core.business.EstrattoConto(bd);
			VersamentoFilter filter = versamentiBD.newFilter();


			List<Long> ids = new ArrayList<Long>();
			ids.add(idToExport);

			if(!isAdmin){

				AclBD aclBD = new AclBD(bd);
				List<Acl> aclOperatore = aclBD.getAclOperatore(operatore.getId());

				boolean vediTuttiDomini = false;
				List<Long> idDomini = new ArrayList<Long>();
				for(Acl acl: aclOperatore) {
					if(Tipo.DOMINIO.equals(acl.getTipo())) {
						if(acl.getIdDominio() == null) {
							vediTuttiDomini = true;
							break;
						} else {
							idDomini.add(acl.getIdDominio());
						}
					}
				}
				if(!vediTuttiDomini) {
					if(idDomini.isEmpty()) {
						eseguiRicerca = false;
					} else {
						filter.setIdDomini(idDomini);
					}
				}

				// l'operatore puo' vedere i domini associati, controllo se c'e' un versamento con Id nei domini concessi.
				if(eseguiRicerca){
					filter.setIdVersamento(ids);
					eseguiRicerca = eseguiRicerca && versamentiBD.count(filter) > 0;
				}
			}
			Eventi eventiDars = new Eventi();
			EventiHandler eventiDarsHandler = (EventiHandler) eventiDars.getDarsHandler(); 
			Versamento versamento = eseguiRicerca ? versamentiBD.getVersamento(idToExport) : null;
			String fileName = "Export.zip";  

			if(versamento != null){
				// Prelevo il dominio

				UnitaOperativa uo  = AnagraficaManager.getUnitaOperativa(bd, versamento.getIdUo());
				Dominio dominio  = AnagraficaManager.getDominio(bd, uo.getIdDominio());

				String dirDominio = dominio.getCodDominio();

				// Estratto conto per iban e codiceversamento.
				List<Long> idVersamentiDominio = new ArrayList<Long>();
				idVersamentiDominio.add(idToExport);
				it.govpay.core.business.model.EstrattoConto input =  it.govpay.core.business.model.EstrattoConto.creaEstrattoContoVersamentiPDF(dominio, idVersamentiDominio);
				List<it.govpay.core.business.model.EstrattoConto> listInputEstrattoConto = new ArrayList<it.govpay.core.business.model.EstrattoConto>();
				listInputEstrattoConto.add(input);
				String pathLoghi = ConsoleProperties.getInstance().getPathEstrattoContoPdfLoghi();
				List<it.govpay.core.business.model.EstrattoConto> listOutputEstattoConto = estrattoContoBD.getEstrattoContoVersamenti(listInputEstrattoConto,pathLoghi);

				for (it.govpay.core.business.model.EstrattoConto estrattoContoOutput : listOutputEstattoConto) {
					Map<String, ByteArrayOutputStream> estrattoContoVersamenti = estrattoContoOutput.getOutput(); 
					for (String nomeEntry : estrattoContoVersamenti.keySet()) {
						numeroZipEntries ++;
						ByteArrayOutputStream baos = estrattoContoVersamenti.get(nomeEntry);
						ZipEntry estrattoContoEntry = new ZipEntry(estrattoContoOutput.getDominio().getCodDominio() + "/" + nomeEntry);
						zout.putNextEntry(estrattoContoEntry);
						zout.write(baos.toByteArray());
						zout.closeEntry();
					}
				}

				String dirVersamento = dirDominio + "/" + versamento.getCodVersamentoEnte();

				RptFilter rptFilter = rptBD.newFilter();
				FilterSortWrapper rptFsw = new FilterSortWrapper();
				rptFsw.setField(it.govpay.orm.RPT.model().DATA_MSG_RICHIESTA);
				rptFsw.setSortOrder(SortOrder.DESC);
				rptFilter.getFilterSortList().add(rptFsw);
				rptFilter.setIdVersamento(idToExport); 

				RrBD rrBD = new RrBD(bd);
				FilterSortWrapper rrFsw = new FilterSortWrapper();
				rrFsw.setField(it.govpay.orm.RR.model().DATA_MSG_REVOCA);
				rrFsw.setSortOrder(SortOrder.DESC);

				List<Rpt> listaRpt = rptBD.findAll(rptFilter);
				if(listaRpt != null && listaRpt.size() >0 )
					for (Rpt rpt : listaRpt) {
						numeroZipEntries ++;
						String iuv = rpt.getIuv();
						String ccp = rpt.getCcp();
						String iuvCcpDir = dirVersamento + "/" + iuv;

						// non appendo il ccp nel caso sia uguale ad 'n/a' altrimenti crea un nuovo livello di directory;
						if(!StringUtils.equalsIgnoreCase(ccp, "n/a"))
							iuvCcpDir = iuvCcpDir  + "_" + ccp;

						String rptEntryName = iuvCcpDir + "/rpt_" + rpt.getCodMsgRichiesta() + ".xml"; 

						ZipEntry rptXml = new ZipEntry( rptEntryName);
						zout.putNextEntry(rptXml);
						zout.write(rpt.getXmlRpt());
						zout.closeEntry();

						if(rpt.getXmlRt() != null){
							numeroZipEntries ++;
							String rtEntryName = iuvCcpDir + "/rt_" + rpt.getCodMsgRichiesta() + ".xml";
							ZipEntry rtXml = new ZipEntry(rtEntryName);
							zout.putNextEntry(rtXml);
							zout.write(rpt.getXmlRt());
							zout.closeEntry();

							// RT in formato pdf
							String tipoFirma = rpt.getFirmaRichiesta().getCodifica();
							byte[] rtByteValidato = RtUtils.validaFirma(tipoFirma, rpt.getXmlRt(), dominio.getCodDominio());
							CtRicevutaTelematica rt = JaxbUtils.toRT(rtByteValidato);
							String causale = versamento.getCausaleVersamento().getSimple();
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							RtPdf.getPdfRicevutaPagamento(pathLoghi, rt, causale,baos,log);

							String rtPdfEntryName = iuvCcpDir + "/ricevuta_pagamento.pdf";
							numeroZipEntries ++;
							ZipEntry rtPdf = new ZipEntry(rtPdfEntryName);
							zout.putNextEntry(rtPdf);
							zout.write(baos.toByteArray());
							zout.closeEntry();
						}

						// Eventi
						String entryEventiCSV =  iuvCcpDir + "/eventi.csv";

						EventiFilter eventiFilter = eventiBd.newFilter();
						eventiFilter.setCodDominio(dominio.getCodDominio());
						eventiFilter.setIuv(iuv);
						eventiFilter.setCcp(ccp);
						FilterSortWrapper fsw = new FilterSortWrapper();
						fsw.setField(it.govpay.orm.Evento.model().DATA_1);
						fsw.setSortOrder(SortOrder.ASC);
						eventiFilter.getFilterSortList().add(fsw);

						List<Evento> findAllEventi = eventiBd.findAll(eventiFilter);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						eventiDarsHandler.scriviCSVEventi(baos, findAllEventi);

						ZipEntry eventiCSV = new ZipEntry(entryEventiCSV);
						zout.putNextEntry(eventiCSV);
						zout.write(baos.toByteArray());
						zout.closeEntry();


						RrFilter rrFilter = rrBD.newFilter();
						rrFilter.getFilterSortList().add(rrFsw);
						rrFilter.setIdRpt(rpt.getId()); 
						List<Rr> findAll = rrBD.findAll(rrFilter);
						if(findAll != null && findAll.size() > 0){
							for (Rr rr : findAll) {
								numeroZipEntries ++;
								String rrEntryName = iuvCcpDir + "/rr_" + rr.getCodMsgRevoca() + ".xml"; 

								ZipEntry rrXml = new ZipEntry(rrEntryName);
								zout.putNextEntry(rrXml);
								zout.write(rr.getXmlRr());
								zout.closeEntry();

								if(rr.getXmlEr() != null){
									numeroZipEntries ++;
									String erEntryName = iuvCcpDir + "/er_" + rr.getCodMsgRevoca() + ".xml"; 
									ZipEntry rtXml = new ZipEntry(erEntryName);
									zout.putNextEntry(rtXml);
									zout.write(rr.getXmlEr());
									zout.closeEntry();

									// ER in formato pdf
									CtEsitoRevoca er = JaxbUtils.toER(rr.getXmlEr());
									String causale = versamento.getCausaleVersamento().getSimple();
									baos = new ByteArrayOutputStream();
									Dominio dominio3 = AnagraficaManager.getDominio(bd, er.getDominio().getIdentificativoDominio());
									ErPdf.getPdfEsitoRevoca(pathLoghi, er, dominio3, dominio3.getAnagrafica(bd), causale,baos,log);

									String erPdfEntryName = iuvCcpDir + "/esito_revoca.pdf";
									numeroZipEntries ++;
									ZipEntry erPdf = new ZipEntry(erPdfEntryName);
									zout.putNextEntry(erPdf);
									zout.write(baos.toByteArray());
									zout.closeEntry();
								}
							}
						}
					}

				//Estratto conto in formato CSV
				EstrattoContoFilter ecFilter = estrattiContoBD.newFilter(true); 
				ecFilter.setIdVersamento(ids);
				List<EstrattoConto> findAll =  estrattiContoBD.estrattoContoFromIdVersamenti(ecFilter);

				if(findAll != null && findAll.size() > 0){
					//ordinamento record
					Collections.sort(findAll, new EstrattoContoComparator());
					numeroZipEntries ++;
					ByteArrayOutputStream baos  = new ByteArrayOutputStream();
					try{
						ZipEntry pagamentoCsv = new ZipEntry("estrattoConto.csv");
						zout.putNextEntry(pagamentoCsv);
						printer = new Printer(this.getFormat() , baos);
						printer.printRecord(CSVUtils.getEstrattoContoCsvHeader());
						for (EstrattoConto pagamento : findAll) {
							printer.printRecord(CSVUtils.getEstrattoContoAsCsvRow(pagamento,this.sdf));
						}
					}finally {
						try{
							if(printer!=null){
								printer.close();
							}
						}catch (Exception e) {
							throw new Exception("Errore durante la chiusura dello stream ",e);
						}
					}
					zout.write(baos.toByteArray());
					zout.closeEntry();
				}
			}

			// se non ho inserito nessuna entry
			if(numeroZipEntries == 0){
				String noEntriesTxt = "/README";
				ZipEntry entryTxt = new ZipEntry(noEntriesTxt);
				zout.putNextEntry(entryTxt);
				zout.write("Non sono state trovate informazioni sui versamenti selezionati.".getBytes());
				zout.closeEntry();
			}

			zout.flush();
			zout.close();

			this.log.info("Esecuzione " + methodName + " completata.");

			return fileName;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	/* Creazione/Update non consentiti**/

	@Override
	public InfoForm getInfoCreazione(UriInfo uriInfo, BasicBD bd) throws ConsoleException { return null; }

	@Override
	public InfoForm getInfoModifica(UriInfo uriInfo, BasicBD bd, Versamento entry) throws ConsoleException { return null; }

	@Override
	public void delete(List<Long> idsToDelete, UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException {	}

	@Override
	public Versamento creaEntry(InputStream is, UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException { return null; }

	@Override
	public Dettaglio insert(InputStream is, UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException, ValidationException, DuplicatedEntryException { return null; }

	@Override
	public void checkEntry(Versamento entry, Versamento oldEntry) throws ValidationException { }

	@Override
	public Dettaglio update(InputStream is, UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException, ValidationException { return null; }

	@Override
	public Object uplaod(MultipartFormDataInput input, UriInfo uriInfo, BasicBD bd)	throws WebApplicationException, ConsoleException, ValidationException { return null;}
}

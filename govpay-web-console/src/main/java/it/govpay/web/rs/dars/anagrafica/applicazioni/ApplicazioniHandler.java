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
package it.govpay.web.rs.dars.anagrafica.applicazioni;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.expression.SortOrder;

import it.govpay.bd.BasicBD;
import it.govpay.bd.FilterSortWrapper;
import it.govpay.bd.anagrafica.ApplicazioniBD;
import it.govpay.bd.anagrafica.DominiBD;
import it.govpay.bd.anagrafica.TipiTributoBD;
import it.govpay.bd.anagrafica.filters.ApplicazioneFilter;
import it.govpay.bd.anagrafica.filters.DominioFilter;
import it.govpay.bd.anagrafica.filters.TipoTributoFilter;
import it.govpay.bd.model.Acl.Servizio;
import it.govpay.bd.model.Acl.Tipo;
import it.govpay.bd.model.Connettore.EnumSslType;
import it.govpay.bd.model.Acl;
import it.govpay.bd.model.Applicazione;
import it.govpay.bd.model.Connettore;
import it.govpay.bd.model.Dominio;
import it.govpay.bd.model.TipoTributo;
import it.govpay.bd.model.Versionabile.Versione;
import it.govpay.bd.model.Rpt.FirmaRichiesta;
import it.govpay.web.rs.BaseRsService;
import it.govpay.web.rs.dars.BaseDarsHandler;
import it.govpay.web.rs.dars.BaseDarsService;
import it.govpay.web.rs.dars.IDarsHandler;
import it.govpay.web.rs.dars.anagrafica.applicazioni.input.DominiRendicontazione;
import it.govpay.web.rs.dars.anagrafica.applicazioni.input.DominiVersamenti;
import it.govpay.web.rs.dars.anagrafica.applicazioni.input.TipiTributoVersamenti;
import it.govpay.web.rs.dars.anagrafica.applicazioni.input.Trusted;
import it.govpay.web.rs.dars.anagrafica.connettori.ConnettoreHandler;
import it.govpay.web.rs.dars.anagrafica.domini.DominiHandler;
import it.govpay.web.rs.dars.anagrafica.tributi.TipiTributoHandler;
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
import it.govpay.web.rs.dars.model.VoceRiferimento;
import it.govpay.web.rs.dars.model.input.ParamField;
import it.govpay.web.rs.dars.model.input.RefreshableParamField;
import it.govpay.web.rs.dars.model.input.base.CheckButton;
import it.govpay.web.rs.dars.model.input.base.InputNumber;
import it.govpay.web.rs.dars.model.input.base.InputText;
import it.govpay.web.rs.dars.model.input.base.SelectList;
import it.govpay.web.utils.Utils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class ApplicazioniHandler extends BaseDarsHandler<Applicazione> implements IDarsHandler<Applicazione>{

	public static final String CONNETTORE_VERIFICA = ConnettoreHandler.CONNETTORE_VERIFICA;
	public static final String CONNETTORE_NOTIFICA = ConnettoreHandler.CONNETTORE_NOTIFICA; 
	private static Map<String, ParamField<?>> infoCreazioneMap = null;
	private static Map<String, ParamField<?>> infoRicercaMap = null;

	public ApplicazioniHandler(Logger log, BaseDarsService darsService) {
		super(log,darsService);
	}

	@Override
	public Elenco getElenco(UriInfo uriInfo,BasicBD bd) throws WebApplicationException,ConsoleException {
		String methodName = "getElenco " + this.titoloServizio;
		try{	
			// Operazione consentita solo all'amministratore
			this.darsService.checkOperatoreAdmin(bd);

			Integer offset = this.getOffset(uriInfo);
			Integer limit = this.getLimit(uriInfo);
			URI esportazione = null;
			URI cancellazione = null;
			boolean visualizzaRicerca = true;

			this.log.info("Esecuzione " + methodName + " in corso..."); 

			ApplicazioniBD applicazioniBD = new ApplicazioniBD(bd);
			ApplicazioneFilter filter = applicazioniBD.newFilter();
			filter.setOffset(offset);
			filter.setLimit(limit);
			FilterSortWrapper fsw = new FilterSortWrapper();
			fsw.setField(it.govpay.orm.Applicazione.model().COD_APPLICAZIONE);
			fsw.setSortOrder(SortOrder.ASC);
			filter.getFilterSortList().add(fsw);

			String codApplicazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.id");
			String codApplicazione = this.getParameter(uriInfo, codApplicazioneId, String.class	);
			if(StringUtils.isNotEmpty(codApplicazione)){
				filter.setCodApplicazione(codApplicazione); 
			}

			long count = applicazioniBD.count(filter);

			// visualizza la ricerca solo se i risultati sono > del limit
			visualizzaRicerca = visualizzaRicerca && this.visualizzaRicerca(count, limit);

			InfoForm infoRicerca = visualizzaRicerca ? this.getInfoRicerca(uriInfo, bd) : null;

			Elenco elenco = new Elenco(this.titoloServizio, infoRicerca,
					this.getInfoCreazione(uriInfo, bd),
					count, esportazione, cancellazione); 

			UriBuilder uriDettaglioBuilder = BaseRsService.checkDarsURI(uriInfo).path(this.pathServizio).path("{id}");

			List<Applicazione> findAll = applicazioniBD.findAll(filter);

			if(findAll != null && findAll.size() > 0){
				for (Applicazione entry : findAll) {
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

	@Override
	public InfoForm getInfoRicerca(UriInfo uriInfo, BasicBD bd) throws ConsoleException {
		URI ricerca = this.getUriRicerca(uriInfo, bd);
		InfoForm infoRicerca = new InfoForm(ricerca);

		String codApplicazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.id");

		if(infoRicercaMap == null){
			this.initInfoRicerca(uriInfo, bd);

		}
		Sezione sezioneRoot = infoRicerca.getSezioneRoot();

		InputText codApplicazione= (InputText) infoRicercaMap.get(codApplicazioneId);
		codApplicazione.setDefaultValue(null);
		sezioneRoot.addField(codApplicazione);

		return infoRicerca;
	}

	private void initInfoRicerca(UriInfo uriInfo, BasicBD bd) throws ConsoleException{
		if(infoRicercaMap == null){
			infoRicercaMap = new HashMap<String, ParamField<?>>();

			String codApplicazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.id");
			String codApplicazioneLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.label");
			InputText codApplicazione = new InputText(codApplicazioneId, codApplicazioneLabel, null, false, false, true, 1, 255);
			infoRicercaMap.put(codApplicazioneId, codApplicazione);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public InfoForm getInfoCreazione(UriInfo uriInfo, BasicBD bd) throws ConsoleException {
		URI creazione = this.getUriCreazione(uriInfo, bd);
		InfoForm infoCreazione = new InfoForm(creazione,Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".creazione.titolo"));

		String codApplicazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.id");
		String principalId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".principal.id");
		String abilitatoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.id");
		String applicazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".id.id");
		String firmaRichiestaId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.id");
		String versioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".versione.id");

		String versamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".versamenti.id");
		String rendicontazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".rendicontazione.id");
		String dominiVersamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dominiVersamenti.id");
		String tipiTributoVersamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".tipiTributoVersamenti.id");
		String dominiRendicontazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dominiRendicontazione.id");
		String trustedId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".trusted.id");

		ConnettoreHandler connettoreVerificaHandler = new ConnettoreHandler(CONNETTORE_VERIFICA,this.nomeServizio,this.pathServizio);
		List<ParamField<?>> infoCreazioneConnettoreVerifica = connettoreVerificaHandler.getInfoCreazione(uriInfo, bd,true);

		ConnettoreHandler connettoreNotificaHandler = new ConnettoreHandler(CONNETTORE_NOTIFICA,this.nomeServizio,this.pathServizio);
		List<ParamField<?>> infoCreazioneConnettoreNotifica = connettoreNotificaHandler.getInfoCreazione(uriInfo, bd,true);

		if(infoCreazioneMap == null){
			this.initInfoCreazione(uriInfo, bd);
		}

		Sezione sezioneRoot = infoCreazione.getSezioneRoot();
		InputNumber idInterm = (InputNumber) infoCreazioneMap.get(applicazioneId);
		idInterm.setDefaultValue(null);
		sezioneRoot.addField(idInterm);

		InputText codApplicazione = (InputText) infoCreazioneMap.get(codApplicazioneId);
		codApplicazione.setDefaultValue(null);
		codApplicazione.setEditable(true); 
		sezioneRoot.addField(codApplicazione);

		InputText principal = (InputText) infoCreazioneMap.get(principalId);
		principal.setDefaultValue(null);
		sezioneRoot.addField(principal);

		SelectList<String> firmaRichiesta = (SelectList<String>) infoCreazioneMap.get(firmaRichiestaId);
		firmaRichiesta.setDefaultValue(FirmaRichiesta.NESSUNA.getCodifica());
		sezioneRoot.addField(firmaRichiesta);

		// versione
		SelectList<String> versione = (SelectList<String>) infoCreazioneMap.get(versioneId);
		versione.setDefaultValue(Versione.getUltimaVersione().getLabel());
		sezioneRoot.addField(versione);

		CheckButton abilitato = (CheckButton) infoCreazioneMap.get(abilitatoId);
		abilitato.setDefaultValue(true); 
		sezioneRoot.addField(abilitato);

		String etichettaVersamenti = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.versamenti.titolo");
		Sezione sezioneVersamenti = infoCreazione.addSezione(etichettaVersamenti);

		CheckButton versamenti = (CheckButton) infoCreazioneMap.get(versamentiId);
		versamenti.setDefaultValue(false); 
		sezioneVersamenti.addField(versamenti);

		List<RawParamValue> versamentiValues = new ArrayList<RawParamValue>();
		versamentiValues.add(new RawParamValue(applicazioneId, null));
		versamentiValues.add(new RawParamValue(versamentiId, "false"));

		Trusted trusted = (Trusted) infoCreazioneMap.get(trustedId);
		trusted.init(versamentiValues, bd); 
		sezioneVersamenti.addField(trusted);

		List<RawParamValue> versamentiTrustedValues = new ArrayList<RawParamValue>();
		versamentiTrustedValues.addAll(versamentiValues);
		versamentiTrustedValues.add(new RawParamValue(trustedId, "false"));

		TipiTributoVersamenti tipiTributoVersamenti = (TipiTributoVersamenti) infoCreazioneMap.get(tipiTributoVersamentiId);
		tipiTributoVersamenti.init(versamentiTrustedValues, bd); 
		sezioneVersamenti.addField(tipiTributoVersamenti);

		DominiVersamenti dominiVersamenti = (DominiVersamenti) infoCreazioneMap.get(dominiVersamentiId);
		dominiVersamenti.init(versamentiValues, bd); 
		sezioneVersamenti.addField(dominiVersamenti); 

		String etichettaRendicontazione = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.rendicontazione.titolo");
		Sezione sezioneRendicontazione = infoCreazione.addSezione(etichettaRendicontazione);

		CheckButton rendicontazione = (CheckButton) infoCreazioneMap.get(rendicontazioneId);
		rendicontazione.setDefaultValue(false); 
		sezioneRendicontazione.addField(rendicontazione);


		List<RawParamValue> rendicontazioneValues = new ArrayList<RawParamValue>();
		rendicontazioneValues.add(new RawParamValue(applicazioneId, null));
		rendicontazioneValues.add(new RawParamValue(rendicontazioneId, "false"));

		DominiRendicontazione dominiRendicontazione = (DominiRendicontazione) infoCreazioneMap.get(dominiRendicontazioneId);
		dominiRendicontazione.init(rendicontazioneValues, bd); 
		sezioneRendicontazione.addField(dominiRendicontazione); 


		Sezione sezioneConnettoreVerifica = infoCreazione.addSezione(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + CONNETTORE_VERIFICA + ".titolo"));

		for (ParamField<?> par : infoCreazioneConnettoreVerifica) { 
			sezioneConnettoreVerifica.addField(par); 	
		}

		Sezione sezioneConnettoreNotifica = infoCreazione.addSezione(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + CONNETTORE_NOTIFICA + ".titolo"));

		for (ParamField<?> par : infoCreazioneConnettoreNotifica) { 
			sezioneConnettoreNotifica.addField(par); 	
		}


		return infoCreazione;
	}

	private void initInfoCreazione(UriInfo uriInfo, BasicBD bd) throws ConsoleException{
		if(infoCreazioneMap == null){
			infoCreazioneMap = new HashMap<String, ParamField<?>>();

			String codApplicazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.id");
			String principalId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".principal.id");
			String abilitatoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.id");
			String applicazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".id.id");
			String firmaRichiestaId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.id");
			String versioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".versione.id");

			String versamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".versamenti.id");
			String rendicontazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".rendicontazione.id");
			String dominiVersamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dominiVersamenti.id");
			String tipiTributoVersamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".tipiTributoVersamenti.id");
			String dominiRendicontazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dominiRendicontazione.id");
			String trustedId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".trusted.id");

			// id 
			InputNumber id = new InputNumber(applicazioneId, null, null, true, true, false, 1, 20);
			infoCreazioneMap.put(applicazioneId, id);

			// versione
			SelectList<String> versione = this.getSelectListVersione(versioneId);
			infoCreazioneMap.put(versioneId, versione);

			// codApplicazione
			String codApplicazioneLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.label");
			InputText codApplicazione = new InputText(codApplicazioneId, codApplicazioneLabel, null, true, false, true, 1, 35);
			codApplicazione.setSuggestion(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.suggestion"));
			codApplicazione.setValidation(null, Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.errorMessage"));
			infoCreazioneMap.put(codApplicazioneId, codApplicazione);

			// principal
			String principalLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".principal.label");
			InputText principal = new InputText(principalId, principalLabel, null, true, false, true, 1, 255);
			principal.setValidation(null, Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".principal.errorMessage"));
			infoCreazioneMap.put(principalId, principal);

			// abilitato
			String abilitatoLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.label");
			CheckButton abiliato = new CheckButton(abilitatoId, abilitatoLabel, true, false, false, true);
			infoCreazioneMap.put(abilitatoId, abiliato);

			String firmaRichiestaLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.label");
			List<Voce<String>> valoriFirma = new ArrayList<Voce<String>>(); 
			valoriFirma.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.nessuna"), FirmaRichiesta.NESSUNA.getCodifica()));
			valoriFirma.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.ca_des"), FirmaRichiesta.CA_DES.getCodifica()));
			valoriFirma.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.xa_des"), FirmaRichiesta.XA_DES.getCodifica()));
			//valoriFirma.add(new Voce<String>(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.avanzata"), FirmaRichiesta.AVANZATA.getCodifica()));
			SelectList<String> firmaRichiesta = new SelectList<String>(firmaRichiestaId, firmaRichiestaLabel, null, true, false, true, valoriFirma);
			infoCreazioneMap.put(firmaRichiestaId, firmaRichiesta);

			//seziona rendicontazione
			// abilitato
			String rendicontazioneLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".rendicontazione.label");
			CheckButton rendicontazione = new CheckButton(rendicontazioneId, rendicontazioneLabel, true, false, false, true);
			infoCreazioneMap.put(rendicontazioneId, rendicontazione);

			List<RawParamValue> rendicontazioneValues = new ArrayList<RawParamValue>();
			rendicontazioneValues.add(new RawParamValue(applicazioneId, null));
			rendicontazioneValues.add(new RawParamValue(rendicontazioneId, "false"));

			String dominiRendicontazioneLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dominiRendicontazione.label");
			URI dominiRendicontazioneRefreshUri = this.getUriField(uriInfo, bd, dominiRendicontazioneId); 
			DominiRendicontazione dominiRendicontazione = new DominiRendicontazione(this.nomeServizio, dominiRendicontazioneId, dominiRendicontazioneLabel, dominiRendicontazioneRefreshUri , rendicontazioneValues, bd);
			dominiRendicontazione.addDependencyField(rendicontazione);
			dominiRendicontazione.init(rendicontazioneValues, bd); 
			infoCreazioneMap.put(dominiRendicontazioneId, dominiRendicontazione);

			//seziona versamenti
			// abilitato
			String versamentiLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".versamenti.label");
			CheckButton versamenti = new CheckButton(versamentiId, versamentiLabel, true, false, false, true);
			infoCreazioneMap.put(versamentiId, versamenti);

			List<RawParamValue> versamentiValues = new ArrayList<RawParamValue>();
			versamentiValues.add(new RawParamValue(applicazioneId, null));
			versamentiValues.add(new RawParamValue(versamentiId, "false"));

			// trusted
			String trustedLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".trusted.label");
			URI trustedRefreshUri = this.getUriField(uriInfo, bd, trustedId); 
			Trusted trusted = new Trusted(this.nomeServizio,trustedId, trustedLabel, trustedRefreshUri, versamentiValues);
			trusted.addDependencyField(versamenti);
			trusted.init(versamentiValues, bd);
			infoCreazioneMap.put(trustedId, trusted);

			List<RawParamValue> versamentiTrustedValues = new ArrayList<RawParamValue>();
			versamentiTrustedValues.addAll(versamentiValues);
			versamentiTrustedValues.add(new RawParamValue(trustedId, "false"));

			String tipiTributoVersamentiLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".tipiTributoVersamenti.label");
			URI tipiTributoVersamentiRefreshUri = this.getUriField(uriInfo, bd, tipiTributoVersamentiId); 
			TipiTributoVersamenti tipiTributoVersamenti = new TipiTributoVersamenti(this.nomeServizio, tipiTributoVersamentiId, tipiTributoVersamentiLabel, tipiTributoVersamentiRefreshUri , versamentiTrustedValues, bd);
			tipiTributoVersamenti.addDependencyField(versamenti);
			tipiTributoVersamenti.addDependencyField(trusted);
			tipiTributoVersamenti.init(versamentiTrustedValues, bd); 
			infoCreazioneMap.put(tipiTributoVersamentiId, tipiTributoVersamenti);

			String dominiVersamentiLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dominiVersamenti.label");
			URI dominiVersamentiRefreshUri = this.getUriField(uriInfo, bd, dominiVersamentiId); 
			DominiVersamenti dominiVersamenti = new DominiVersamenti(this.nomeServizio, dominiVersamentiId, dominiVersamentiLabel, dominiVersamentiRefreshUri , versamentiValues, bd);
			dominiVersamenti.addDependencyField(versamenti);
			dominiVersamenti.addDependencyField(trusted);
			dominiVersamenti.init(versamentiValues, bd); 
			infoCreazioneMap.put(dominiVersamentiId, dominiVersamenti);

			ConnettoreHandler connettoreVerificaHandler = new ConnettoreHandler(CONNETTORE_VERIFICA,this.nomeServizio,this.pathServizio);
			List<ParamField<?>> infoCreazioneConnettoreVerifica = connettoreVerificaHandler.getInfoCreazione(uriInfo, bd,true);

			ConnettoreHandler connettoreNotificaHandler = new ConnettoreHandler(CONNETTORE_NOTIFICA,this.nomeServizio,this.pathServizio);
			List<ParamField<?>> infoCreazioneConnettoreNotifica = connettoreNotificaHandler.getInfoCreazione(uriInfo, bd,true);

			for (ParamField<?> par : infoCreazioneConnettoreVerifica) { 
				infoCreazioneMap.put(par.getId(),par); 	
			}

			for (ParamField<?> par : infoCreazioneConnettoreNotifica) { 
				infoCreazioneMap.put(par.getId(),par); 	
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public InfoForm getInfoModifica(UriInfo uriInfo, BasicBD bd, Applicazione entry) throws ConsoleException {
		URI modifica = this.getUriModifica(uriInfo, bd);
		InfoForm infoModifica = new InfoForm(modifica,Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".modifica.titolo"));

		String codApplicazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.id");
		String principalId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".principal.id");
		String abilitatoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.id");
		String applicazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".id.id");
		String firmaRichiestaId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.id");
		String versioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".versione.id");

		String versamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".versamenti.id");
		String rendicontazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".rendicontazione.id");
		String dominiVersamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dominiVersamenti.id");
		String tipiTributoVersamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".tipiTributoVersamenti.id");
		String dominiRendicontazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dominiRendicontazione.id");
		String trustedId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".trusted.id");

		ConnettoreHandler connettoreVerificaHandler = new ConnettoreHandler(CONNETTORE_VERIFICA,this.nomeServizio,this.pathServizio);
		List<ParamField<?>> infoModificaConnettoreVerifica = connettoreVerificaHandler.getInfoModifica(uriInfo, bd, entry.getConnettoreVerifica(),entry.getId(),true);

		ConnettoreHandler connettoreNotificaHandler = new ConnettoreHandler(CONNETTORE_NOTIFICA,this.nomeServizio,this.pathServizio);
		List<ParamField<?>> infoModificaConnettoreNotifica = connettoreNotificaHandler.getInfoModifica(uriInfo, bd, entry.getConnettoreNotifica(),entry.getId(),true);

		if(infoCreazioneMap == null){
			this.initInfoCreazione(uriInfo, bd);
		}

		Sezione sezioneRoot = infoModifica.getSezioneRoot();
		InputNumber idInterm = (InputNumber) infoCreazioneMap.get(applicazioneId);
		idInterm.setDefaultValue(entry.getId());
		sezioneRoot.addField(idInterm);

		InputText codApplicazione = (InputText) infoCreazioneMap.get(codApplicazioneId);
		codApplicazione.setDefaultValue(entry.getCodApplicazione());
		codApplicazione.setEditable(false); 
		sezioneRoot.addField(codApplicazione);

		InputText principal = (InputText) infoCreazioneMap.get(principalId);
		principal.setDefaultValue(entry.getPrincipal());
		sezioneRoot.addField(principal);

		FirmaRichiesta firmaRichiestaValue = entry.getFirmaRichiesta() != null ? entry.getFirmaRichiesta() : FirmaRichiesta.NESSUNA;
		SelectList<String> firmaRichiesta = (SelectList<String>) infoCreazioneMap.get(firmaRichiestaId);
		firmaRichiesta.setDefaultValue(firmaRichiestaValue.getCodifica());
		sezioneRoot.addField(firmaRichiesta);

		// versione
		SelectList<String> versione = (SelectList<String>) infoCreazioneMap.get(versioneId);
		versione.setDefaultValue(entry.getVersione().getLabel());
		sezioneRoot.addField(versione);

		CheckButton abilitato = (CheckButton) infoCreazioneMap.get(abilitatoId);
		abilitato.setDefaultValue(entry.isAbilitato()); 
		sezioneRoot.addField(abilitato);

		String etichettaVersamenti = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.versamenti.titolo");
		Sezione sezioneVersamenti = infoModifica.addSezione(etichettaVersamenti);

		List<Long> idsAclDominiVersamenti = Utils.getIdsFromAcls(entry.getAcls(), Tipo.DOMINIO, Servizio.VERSAMENTI);
		List<Long> idsAclTributiVersamenti = Utils.getIdsFromAcls(entry.getAcls(), Tipo.TRIBUTO, Servizio.VERSAMENTI);
		boolean visualizzaVersamenti = idsAclDominiVersamenti.size() > 0 || idsAclTributiVersamenti.size() > 0 || entry.isTrusted(); 

		CheckButton versamenti = (CheckButton) infoCreazioneMap.get(versamentiId);
		versamenti.setDefaultValue(visualizzaVersamenti); 
		sezioneVersamenti.addField(versamenti);

		List<RawParamValue> versamentiValues = new ArrayList<RawParamValue>();
		versamentiValues.add(new RawParamValue(applicazioneId, entry.getId() + ""));
		versamentiValues.add(new RawParamValue(versamentiId, (visualizzaVersamenti? "true" : "false")));

		Trusted trusted = (Trusted) infoCreazioneMap.get(trustedId);
		trusted.init(versamentiValues, bd); 
		sezioneVersamenti.addField(trusted);

		List<RawParamValue> versamentiTrustedValues = new ArrayList<RawParamValue>();
		versamentiTrustedValues.addAll(versamentiValues);
		versamentiTrustedValues.add(new RawParamValue(trustedId, (entry.isTrusted() ? "true" : "false")));

		TipiTributoVersamenti tipiTributoVersamenti = (TipiTributoVersamenti) infoCreazioneMap.get(tipiTributoVersamentiId);
		tipiTributoVersamenti.init(versamentiTrustedValues, bd); 
		sezioneVersamenti.addField(tipiTributoVersamenti);

		DominiVersamenti dominiVersamenti = (DominiVersamenti) infoCreazioneMap.get(dominiVersamentiId);
		dominiVersamenti.init(versamentiValues, bd); 
		sezioneVersamenti.addField(dominiVersamenti); 

		String etichettaRendicontazione = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.rendicontazione.titolo");
		Sezione sezioneRendicontazione = infoModifica.addSezione(etichettaRendicontazione);

		List<Long> idsAclDominiRendicontazione = Utils.getIdsFromAcls(entry.getAcls(), Tipo.DOMINIO, Servizio.RENDICONTAZIONE);
		boolean visualizzaRendicontazione = idsAclDominiRendicontazione.size() > 0 ;

		CheckButton rendicontazione = (CheckButton) infoCreazioneMap.get(rendicontazioneId);
		rendicontazione.setDefaultValue(visualizzaRendicontazione); 
		sezioneRendicontazione.addField(rendicontazione);

		List<RawParamValue> rendicontazioneValues = new ArrayList<RawParamValue>();
		rendicontazioneValues.add(new RawParamValue(applicazioneId, entry.getId() + ""));
		rendicontazioneValues.add(new RawParamValue(rendicontazioneId,  (visualizzaRendicontazione? "true" : "false")));

		DominiRendicontazione dominiRendicontazione = (DominiRendicontazione) infoCreazioneMap.get(dominiRendicontazioneId);
		dominiRendicontazione.init(rendicontazioneValues, bd); 
		sezioneRendicontazione.addField(dominiRendicontazione);


		Sezione sezioneConnettoreVerifica = infoModifica.addSezione(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + CONNETTORE_VERIFICA + ".titolo"));

		for (ParamField<?> par : infoModificaConnettoreVerifica) { 
			sezioneConnettoreVerifica.addField(par); 	
		}

		Sezione sezioneConnettoreNotifica = infoModifica.addSezione(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + CONNETTORE_NOTIFICA + ".titolo"));

		for (ParamField<?> par : infoModificaConnettoreNotifica) { 
			sezioneConnettoreNotifica.addField(par); 	
		}

		return infoModifica;
	}

	@Override
	public Object getField(UriInfo uriInfo,List<RawParamValue>values, String fieldId,BasicBD bd) throws WebApplicationException,ConsoleException {
		this.log.debug("Richiesto field ["+fieldId+"]");
		try{
			// Operazione consentita solo all'amministratore
			this.darsService.checkOperatoreAdmin(bd);

			if(infoCreazioneMap == null){
				this.initInfoCreazione(uriInfo, bd);
			}

			if(infoCreazioneMap.containsKey(fieldId)){
				RefreshableParamField<?> paramField = (RefreshableParamField<?>) infoCreazioneMap.get(fieldId);

				paramField.aggiornaParametro(values,bd);

				return paramField;

			}

			this.log.debug("Field ["+fieldId+"] non presente.");

		}catch(Exception e){
			throw new ConsoleException(e);
		}
		return null;
	}

	@Override
	public Dettaglio getDettaglio(long id, UriInfo uriInfo, BasicBD bd) throws WebApplicationException,ConsoleException {
		String methodName = "dettaglio " + this.titoloServizio + "."+ id;

		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			// Operazione consentita solo all'amministratore
			this.darsService.checkOperatoreAdmin(bd);

			// recupero oggetto
			ApplicazioniBD applicazioniBD = new ApplicazioniBD(bd);
			Applicazione applicazione = applicazioniBD.getApplicazione(id);

			InfoForm infoModifica = this.getInfoModifica(uriInfo, bd,applicazione);
			URI cancellazione = null;
			URI esportazione = null;

			Dettaglio dettaglio = new Dettaglio(this.getTitolo(applicazione,bd), esportazione, cancellazione, infoModifica);

			it.govpay.web.rs.dars.model.Sezione root = dettaglio.getSezioneRoot(); 

			// dati dell'intermediario
			root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codApplicazione.label"), applicazione.getCodApplicazione());
			root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".principal.label"), applicazione.getPrincipal());

			FirmaRichiesta firmaRichiestaValue = applicazione.getFirmaRichiesta() != null ? applicazione.getFirmaRichiesta() : FirmaRichiesta.NESSUNA;
			String firmaRichiestaAsString = null;

			switch (firmaRichiestaValue) {
			case AVANZATA:
				firmaRichiestaAsString = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.avanzata");
				break;
			case CA_DES: 
				firmaRichiestaAsString = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.ca_des");
				break;
			case XA_DES :
				firmaRichiestaAsString = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.xa_des");
				break;
			case NESSUNA: 
			default:
				firmaRichiestaAsString = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.nessuna");
				break;
			}

			root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.label"), firmaRichiestaAsString);
			root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".versione.label"), applicazione.getVersione().getLabel(), true);
			root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.label"), Utils.getSiNoAsLabel(applicazione.isAbilitato()));

			List<Acl> acls = applicazione.getAcls();

			String etichettaTipiTributo = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.tipiTributo.titolo");
			String etichettaDomini = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.domini.titolo");

			String etichettaVersamenti = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.versamenti.titolo");
			it.govpay.web.rs.dars.model.Sezione sezioneVersamenti = dettaglio.addSezione(etichettaVersamenti);

			List<Long> idTributi = Utils.getIdsFromAcls(acls, Tipo.TRIBUTO , Servizio.VERSAMENTI);
			List<Voce<String>> listaVociTributi = new ArrayList<Voce<String>>();
			String valore = null;
			if(!Utils.isEmpty(idTributi)){
				if(!idTributi.contains(-1L)){
					TipiTributoBD tipiTributoBD = new TipiTributoBD(bd);
					TipoTributoFilter filter = tipiTributoBD.newFilter();
					FilterSortWrapper fsw = new FilterSortWrapper();
					fsw.setField(it.govpay.orm.TipoTributo.model().COD_TRIBUTO);
					fsw.setSortOrder(SortOrder.ASC);
					filter.getFilterSortList().add(fsw);
					filter.setListaIdTributi(idTributi);
					List<TipoTributo> findAll =  tipiTributoBD.findAll(filter);

					it.govpay.web.rs.dars.anagrafica.tributi.TipiTributo tipiTributoDars = new it.govpay.web.rs.dars.anagrafica.tributi.TipiTributo();
					TipiTributoHandler tipiTributoDarsHandler = (TipiTributoHandler) tipiTributoDars.getDarsHandler();
					UriBuilder uriDettaglioUoBuilder = BaseRsService.checkDarsURI(uriInfo).path(tipiTributoDars.getPathServizio()).path("{id}");

					if(findAll != null && findAll.size() > 0){
						for (TipoTributo entry : findAll) {
							Elemento elemento = tipiTributoDarsHandler.getElemento(entry, entry.getId(), uriDettaglioUoBuilder,bd);
							listaVociTributi.add(new VoceRiferimento<String>(elemento.getTitolo(), elemento.getSottotitolo(), elemento.getUri()));
						}
					}
				}else{
					valore = Utils.getInstance().getMessageFromResourceBundle("commons.label.tutti");
				}
			} else {
				valore = Utils.getInstance().getMessageFromResourceBundle("commons.label.nessuno");
			}

			sezioneVersamenti.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".trusted.label"), Utils.getSiNoAsLabel(applicazione.isTrusted()));

			if(Utils.isEmpty(listaVociTributi)){
				if(!applicazione.isTrusted())
					sezioneVersamenti.addVoce(etichettaTipiTributo, valore); 
			} else {
				sezioneVersamenti.addVoce(etichettaTipiTributo, null); 
				for (Voce<String> voce : listaVociTributi) {
					sezioneVersamenti.addVoce(voce);
				}
			}

			List<Long> idDomini = Utils.getIdsFromAcls(acls, Tipo.DOMINIO, Servizio.VERSAMENTI);
			List<Voce<String>> listaVociDomini = new ArrayList<Voce<String>>();
			valore = null;
			if(!Utils.isEmpty(idDomini)){
				if(!idDomini.contains(-1L)){
					DominiBD dominiBD = new DominiBD(bd);
					DominioFilter filter = dominiBD.newFilter();
					FilterSortWrapper fsw = new FilterSortWrapper();
					fsw.setField(it.govpay.orm.Dominio.model().COD_DOMINIO);
					fsw.setSortOrder(SortOrder.ASC);
					filter.getFilterSortList().add(fsw);
					filter.setIdDomini(idDomini);
					List<Dominio> findAll =  dominiBD.findAll(filter);

					it.govpay.web.rs.dars.anagrafica.domini.Domini dominiDars = new it.govpay.web.rs.dars.anagrafica.domini.Domini();
					DominiHandler dominiDarsHandler = (DominiHandler) dominiDars.getDarsHandler();
					UriBuilder uriDettaglioDominiBuilder = BaseRsService.checkDarsURI(uriInfo).path(dominiDars.getPathServizio()).path("{id}");

					if(findAll != null && findAll.size() > 0){
						for (Dominio entry : findAll) {
							Elemento elemento = dominiDarsHandler.getElemento(entry, entry.getId(), uriDettaglioDominiBuilder,bd);
							listaVociDomini.add(new VoceRiferimento<String>(elemento.getTitolo(), elemento.getSottotitolo(), elemento.getUri()));
						}
					}
				}else{
					valore = Utils.getInstance().getMessageFromResourceBundle("commons.label.tutti");
				}
			} else {
				valore = Utils.getInstance().getMessageFromResourceBundle("commons.label.nessuno");
			}

			if(Utils.isEmpty(listaVociDomini)){
				sezioneVersamenti.addVoce(etichettaDomini, valore); 
			} else {
				sezioneVersamenti.addVoce(etichettaDomini, null); 
				for (Voce<String> voce : listaVociDomini) {
					sezioneVersamenti.addVoce(voce);
				}
			}

			String etichettaRendicontazione = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".elementoCorrelato.rendicontazione.titolo");
			it.govpay.web.rs.dars.model.Sezione sezioneRendicontazione = dettaglio.addSezione(etichettaRendicontazione);

			idDomini = Utils.getIdsFromAcls(acls, Tipo.DOMINIO, Servizio.RENDICONTAZIONE);
			listaVociDomini = new ArrayList<Voce<String>>();
			valore = null;
			if(!Utils.isEmpty(idDomini)){
				if(!idDomini.contains(-1L)){
					DominiBD dominiBD = new DominiBD(bd);
					DominioFilter filter = dominiBD.newFilter();
					FilterSortWrapper fsw = new FilterSortWrapper();
					fsw.setField(it.govpay.orm.Dominio.model().COD_DOMINIO);
					fsw.setSortOrder(SortOrder.ASC);
					filter.getFilterSortList().add(fsw);
					filter.setIdDomini(idDomini);
					List<Dominio> findAll =  dominiBD.findAll(filter);

					it.govpay.web.rs.dars.anagrafica.domini.Domini dominiDars = new it.govpay.web.rs.dars.anagrafica.domini.Domini();
					DominiHandler dominiDarsHandler = (DominiHandler) dominiDars.getDarsHandler();
					UriBuilder uriDettaglioDominiBuilder = BaseRsService.checkDarsURI(uriInfo).path(dominiDars.getPathServizio()).path("{id}");

					if(findAll != null && findAll.size() > 0){
						for (Dominio entry : findAll) {
							Elemento elemento = dominiDarsHandler.getElemento(entry, entry.getId(), uriDettaglioDominiBuilder,bd);
							listaVociDomini.add(new VoceRiferimento<String>(elemento.getTitolo(), elemento.getSottotitolo(), elemento.getUri()));
						}
					}
				}else{
					valore = Utils.getInstance().getMessageFromResourceBundle("commons.label.tutti");
				}
			} else {
				valore = Utils.getInstance().getMessageFromResourceBundle("commons.label.nessuno");
			}

			if(Utils.isEmpty(listaVociDomini)){
				sezioneRendicontazione.addVoce(etichettaDomini, valore); 
			} else {
				sezioneRendicontazione.addVoce(etichettaDomini, null); 
				for (Voce<String> voce : listaVociDomini) {
					sezioneRendicontazione.addVoce(voce);
				}
			}


			// sezione connettore
			Connettore connettoreVerifica = applicazione.getConnettoreVerifica();
			it.govpay.web.rs.dars.model.Sezione sezioneConnettoreVerifica = dettaglio.addSezione(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + CONNETTORE_VERIFICA + ".titolo"));
			ConnettoreHandler connettoreVerificaHandler = new ConnettoreHandler(CONNETTORE_VERIFICA,this.nomeServizio,this.pathServizio);
			connettoreVerificaHandler.fillSezione(sezioneConnettoreVerifica, connettoreVerifica,true);

			// sezione connettore
			Connettore connettoreNotifica = applicazione.getConnettoreNotifica();
			it.govpay.web.rs.dars.model.Sezione sezioneConnettoreNotifica = dettaglio.addSezione(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + CONNETTORE_NOTIFICA + ".titolo"));
			ConnettoreHandler connettoreNotificaHandler = new ConnettoreHandler(CONNETTORE_NOTIFICA,this.nomeServizio,this.pathServizio);
			connettoreNotificaHandler.fillSezione(sezioneConnettoreNotifica, connettoreNotifica,true);

			this.log.info("Esecuzione " + methodName + " completata.");

			return dettaglio;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	@Override
	public Dettaglio insert(InputStream is, UriInfo uriInfo, BasicBD bd)
			throws WebApplicationException, ConsoleException,ValidationException,DuplicatedEntryException {
		String methodName = "Insert " + this.titoloServizio;

		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			// Operazione consentita solo all'amministratore
			this.darsService.checkOperatoreAdmin(bd);

			Applicazione entry = this.creaEntry(is, uriInfo, bd);

			this.checkEntry(entry, null);

			ApplicazioniBD applicazioniBD = new ApplicazioniBD(bd);

			try{
				applicazioniBD.getApplicazione(entry.getCodApplicazione());
				String msg = Utils.getInstance().getMessageWithParamsFromResourceBundle(this.nomeServizio + ".oggettoEsistente", entry.getCodApplicazione());
				throw new DuplicatedEntryException(msg);
			}catch(NotFoundException e){}

			applicazioniBD.insertApplicazione(entry); 

			this.log.info("Esecuzione " + methodName + " completata.");

			return this.getDettaglio(entry.getId(),uriInfo,bd);
		}catch(DuplicatedEntryException e){
			throw e;
		}catch(ValidationException e){
			throw e;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Applicazione creaEntry(InputStream is, UriInfo uriInfo, BasicBD bd)
			throws WebApplicationException, ConsoleException {
		String methodName = "creaEntry " + this.titoloServizio;
		Applicazione entry = null;
		String rendicontazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".rendicontazione.id");
		String versamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".versamenti.id");
		String dominiRendicontazioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dominiRendicontazione.id");
		String dominiVersamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".dominiVersamenti.id");
		String tipiTributoVersamentiId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".tipiTributoVersamenti.id");
		String versioneId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".versione.id");
		
		String tipoSslIdNot = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + CONNETTORE_NOTIFICA + ".tipoSsl.id");
		String tipoSslIdVer = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + CONNETTORE_VERIFICA + ".tipoSsl.id");
		
		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			// Operazione consentita solo all'amministratore
			this.darsService.checkOperatoreAdmin(bd);

			JsonConfig jsonConfig = new JsonConfig();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Utils.copy(is, baos);

			baos.flush();
			baos.close();

			Map<String,Class<?>> classMap = new HashMap<String, Class<?>>();
			classMap.put(dominiRendicontazioneId, Long.class); 
			classMap.put(dominiVersamentiId, Long.class); 
			classMap.put(tipiTributoVersamentiId, Long.class); 
			jsonConfig.setClassMap(classMap);

			JSONObject jsonObjectApplicazione = JSONObject.fromObject( baos.toString() );  

			List<Acl> lstAclDominiRendicontazione = new ArrayList<Acl>();

			if(jsonObjectApplicazione.getBoolean(rendicontazioneId)){
				JSONArray jsonDomini = jsonObjectApplicazione.getJSONArray(dominiRendicontazioneId);

				for (int i = 0; i < jsonDomini.size(); i++) {
					long idDominio = jsonDomini.getLong(i);

					Acl acl = new Acl();
					acl.setTipo(Tipo.DOMINIO);
					acl.setServizio(Servizio.RENDICONTAZIONE);
					if(idDominio > 0){
						acl.setIdDominio(idDominio);
						lstAclDominiRendicontazione.add(acl);
					}else {
						lstAclDominiRendicontazione.clear();
						lstAclDominiRendicontazione.add(acl);
						break;
					}
				}
			}
			// rimuovo gli oggetti della parte rendicontazione
			jsonObjectApplicazione.remove(rendicontazioneId);
			jsonObjectApplicazione.remove(dominiRendicontazioneId);


			List<Acl> lstAclTributiVersamenti = new ArrayList<Acl>();
			List<Acl> lstAclDominiVersamenti = new ArrayList<Acl>();

			if(jsonObjectApplicazione.getBoolean(versamentiId)){
				JSONArray jsonTributi = jsonObjectApplicazione.getJSONArray(tipiTributoVersamentiId);


				for (int i = 0; i < jsonTributi.size(); i++) {
					long idTributo = jsonTributi.getLong(i);

					Acl acl = new Acl();
					acl.setTipo(Tipo.TRIBUTO);
					acl.setServizio(Servizio.VERSAMENTI);
					if(idTributo > 0){
						acl.setIdTributo(idTributo);
						lstAclTributiVersamenti.add(acl);
					}else {
						lstAclTributiVersamenti.clear();
						lstAclTributiVersamenti.add(acl);
						break;
					}
				}
				JSONArray jsonDomini = jsonObjectApplicazione.getJSONArray(dominiVersamentiId);

				for (int i = 0; i < jsonDomini.size(); i++) {
					long idDominio = jsonDomini.getLong(i);

					Acl acl = new Acl();
					acl.setTipo(Tipo.DOMINIO);
					acl.setServizio(Servizio.VERSAMENTI);
					if(idDominio > 0){
						acl.setIdDominio(idDominio);
						lstAclDominiVersamenti.add(acl);
					}else {
						lstAclDominiVersamenti.clear();
						lstAclDominiVersamenti.add(acl);
						break;
					}
				}
			}
			// rimuovo gli oggetti della parte versamenti
			jsonObjectApplicazione.remove(versamentiId);
			jsonObjectApplicazione.remove(tipiTributoVersamentiId);
			jsonObjectApplicazione.remove(dominiVersamentiId);

			Versione versione = this.getVersioneSelezionata(jsonObjectApplicazione, versioneId, true); 
			
			
			jsonConfig.setRootClass(Applicazione.class);
			entry = (Applicazione) JSONObject.toBean( jsonObjectApplicazione, jsonConfig );

			entry.setVersione(versione); 
			
			entry.setAcls(lstAclDominiRendicontazione);
			entry.getAcls().addAll(lstAclTributiVersamenti);
			entry.getAcls().addAll(lstAclDominiVersamenti);

			String firmaRichiestaId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".firmaRichiesta.id");
			String codFirma = jsonObjectApplicazione.getString(firmaRichiestaId);
			if(codFirma != null){
				FirmaRichiesta enum1 = FirmaRichiesta.toEnum(codFirma);
				entry.setFirmaRichiesta(enum1); 
			}
			
			String tipoSslNot = jsonObjectApplicazione.containsKey(tipoSslIdNot) ? jsonObjectApplicazione.getString(tipoSslIdNot) : null;
			if(tipoSslNot != null)
				jsonObjectApplicazione.remove(tipoSslIdNot);
			
			String tipoSslVer = jsonObjectApplicazione.containsKey(tipoSslIdVer) ? jsonObjectApplicazione.getString(tipoSslIdVer) : null;
			if(tipoSslVer != null)
				jsonObjectApplicazione.remove(tipoSslIdVer);


			String cvPrefix = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + CONNETTORE_VERIFICA + ".idPrefix");
			JSONObject jsonObjectCV = new JSONObject();
			String cnPrefix = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + "." + CONNETTORE_NOTIFICA + ".idPrefix");
			JSONObject jsonObjectCN = new JSONObject();

			Set<String> keySet = jsonObjectApplicazione.keySet();
			for (String key : keySet) {
				if(key.startsWith(cvPrefix)){
					jsonObjectCV.put(key.substring(key.indexOf(cvPrefix)+cvPrefix.length()), jsonObjectApplicazione.get(key)); 
				}
				if(key.startsWith(cnPrefix)){
					jsonObjectCN.put(key.substring(key.indexOf(cnPrefix)+cnPrefix.length()), jsonObjectApplicazione.get(key));
				}
			}

			jsonConfig.setRootClass(Connettore.class);
			Connettore cv = (Connettore) JSONObject.toBean( jsonObjectCV, jsonConfig );
			
			if(StringUtils.isNotEmpty(tipoSslVer)){
				cv.setTipoSsl(EnumSslType.valueOf(tipoSslVer)); 
			}
			
			entry.setConnettoreVerifica(cv);

			Connettore cn = (Connettore) JSONObject.toBean( jsonObjectCN, jsonConfig );
			
			if(StringUtils.isNotEmpty(tipoSslNot)){
				cn.setTipoSsl(EnumSslType.valueOf(tipoSslNot)); 
			}
			
			entry.setConnettoreNotifica(cn);

			this.log.info("Esecuzione " + methodName + " completata.");
			return entry;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	@Override
	public void checkEntry(Applicazione entry, Applicazione oldEntry) throws ValidationException {
		if(entry == null || StringUtils.isEmpty(entry.getCodApplicazione())) {
			throw new ValidationException("il campo Cod Applicazione deve essere valorizzato.");
		}

		if(entry.getPrincipal() == null || entry.getPrincipal().isEmpty()) throw new ValidationException("Il campo Principal deve essere valorizzato.");

		Connettore connettoreNotifica = entry.getConnettoreNotifica();
		ConnettoreHandler connettoreNotificaHandler = new ConnettoreHandler(CONNETTORE_NOTIFICA, this.titoloServizio, this.pathServizio);
		connettoreNotificaHandler.valida(connettoreNotifica,true);

		Connettore connettoreVerifica = entry.getConnettoreVerifica();
		ConnettoreHandler connettoreVerificaHandler = new ConnettoreHandler(CONNETTORE_VERIFICA, this.titoloServizio, this.pathServizio);
		connettoreVerificaHandler.valida(connettoreVerifica,true);

		if(oldEntry != null) { //caso update
			if(!oldEntry.getCodApplicazione().equals(entry.getCodApplicazione()))
				throw new ValidationException("Cod Applicazione non deve cambiare in update. Atteso ["+oldEntry.getCodApplicazione()+"] trovato ["+entry.getCodApplicazione()+"]");
		}
	}

	@Override
	public Dettaglio update(InputStream is, UriInfo uriInfo, BasicBD bd)
			throws WebApplicationException, ConsoleException, ValidationException {
		String methodName = "Update " + this.titoloServizio;

		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			// Operazione consentita solo all'amministratore
			this.darsService.checkOperatoreAdmin(bd);

			Applicazione entry = this.creaEntry(is, uriInfo, bd);

			ApplicazioniBD applicazioniBD = new ApplicazioniBD(bd);
			Applicazione oldEntry = applicazioniBD.getApplicazione(entry.getCodApplicazione());

			this.checkEntry(entry, oldEntry);

			applicazioniBD.updateApplicazione(entry); 

			this.log.info("Esecuzione " + methodName + " completata.");
			return this.getDettaglio(entry.getId(),uriInfo,bd);
		}catch(ValidationException e){
			throw e;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}

	@Override
	public void delete(List<Long> idsToDelete, UriInfo uriInfo, BasicBD bd) throws ConsoleException {
		// operazione non prevista
	}

	@Override
	public String getTitolo(Applicazione entry, BasicBD bd) throws ConsoleException {
		StringBuilder sb = new StringBuilder();

		sb.append(entry.getCodApplicazione());
		return sb.toString();
	}

	@Override
	public String getSottotitolo(Applicazione entry, BasicBD bd)  throws ConsoleException{
		StringBuilder sb = new StringBuilder();

		sb.append(Utils.getAbilitatoAsLabel(entry.isAbilitato()));

		return sb.toString();
	}

	@Override
	public String esporta(List<Long> idsToExport, UriInfo uriInfo, BasicBD bd, ZipOutputStream zout)
			throws WebApplicationException, ConsoleException {
		return null;
	}

	@Override
	public String esporta(Long idToExport, UriInfo uriInfo, BasicBD bd, ZipOutputStream zout)	throws WebApplicationException, ConsoleException {
		return null;
	}

}

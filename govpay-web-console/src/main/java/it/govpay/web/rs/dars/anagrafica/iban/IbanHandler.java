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
package it.govpay.web.rs.dars.anagrafica.iban;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.expression.SortOrder;

import it.govpay.bd.BasicBD;
import it.govpay.bd.FilterSortWrapper;
import it.govpay.bd.anagrafica.DominiBD;
import it.govpay.bd.anagrafica.IbanAccreditoBD;
import it.govpay.bd.anagrafica.filters.IbanAccreditoFilter;
import it.govpay.model.Dominio;
import it.govpay.model.IbanAccredito;
import it.govpay.web.rs.BaseRsService;
import it.govpay.web.rs.dars.BaseDarsHandler;
import it.govpay.web.rs.dars.BaseDarsService;
import it.govpay.web.rs.dars.IDarsHandler;
import it.govpay.web.rs.dars.anagrafica.iban.input.IdNegozio;
import it.govpay.web.rs.dars.anagrafica.iban.input.IdSellerBank;
import it.govpay.web.rs.dars.exception.ConsoleException;
import it.govpay.web.rs.dars.exception.DuplicatedEntryException;
import it.govpay.web.rs.dars.exception.ValidationException;
import it.govpay.web.rs.dars.model.Dettaglio;
import it.govpay.web.rs.dars.model.Elenco;
import it.govpay.web.rs.dars.model.InfoForm;
import it.govpay.web.rs.dars.model.InfoForm.Sezione;
import it.govpay.web.rs.dars.model.RawParamValue;
import it.govpay.web.rs.dars.model.input.ParamField;
import it.govpay.web.rs.dars.model.input.RefreshableParamField;
import it.govpay.web.rs.dars.model.input.base.CheckButton;
import it.govpay.web.rs.dars.model.input.base.InputNumber;
import it.govpay.web.rs.dars.model.input.base.InputText;
import it.govpay.web.utils.Utils;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class IbanHandler extends BaseDarsHandler<IbanAccredito> implements IDarsHandler<IbanAccredito> {

	public static final String patternIBAN = "[a-zA-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}"; 
	public static final String patternBIC = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}";
	private static Map<String, ParamField<?>> infoCreazioneMap = null;
	private Long idDominio = null;

	public IbanHandler(Logger log, BaseDarsService darsService) {
		super(log,darsService);
	}

	@Override
	public Elenco getElenco(UriInfo uriInfo, BasicBD bd) throws WebApplicationException,ConsoleException {
		String methodName = "getElenco " + this.titoloServizio;
		try{
			this.log.info("Esecuzione " + methodName + " in corso..."); 
			// Operazione consentita solo all'amministratore
			this.darsService.checkOperatoreAdmin(bd);

			String idDominioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio+ ".idDominio.id");
			this.idDominio = this.getParameter(uriInfo, idDominioId, Long.class);
			URI esportazione = null;
			URI cancellazione = null;

			Integer offset = this.getOffset(uriInfo);
			Integer limit = this.getLimit(uriInfo);
			IbanAccreditoBD ibanAccreditoBD = new IbanAccreditoBD(bd);
			IbanAccreditoFilter filter = ibanAccreditoBD.newFilter();
			filter.setIdDominio(this.idDominio);
			FilterSortWrapper fsw = new FilterSortWrapper();
			fsw.setField(it.govpay.orm.IbanAccredito.model().COD_IBAN);
			fsw.setSortOrder(SortOrder.ASC);
			filter.getFilterSortList().add(fsw);
			filter.setOffset(offset);
			filter.setLimit(limit);


			long count = ibanAccreditoBD.count(filter);

			Elenco elenco = new Elenco(this.titoloServizio, this.getInfoRicerca(uriInfo, bd),
					this.getInfoCreazione(uriInfo, bd),
					count, esportazione, cancellazione); 

			UriBuilder uriDettaglioBuilder = BaseRsService.checkDarsURI(uriInfo).path(this.pathServizio).path("{id}");

			List<IbanAccredito> findAll = ibanAccreditoBD.findAll(filter);

			if(findAll != null && findAll.size() > 0){
				for (IbanAccredito entry : findAll) {
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
		return null;
	}

	@Override
	public InfoForm getInfoCreazione(UriInfo uriInfo, BasicBD bd) throws ConsoleException {
		URI creazione = this.getUriCreazione(uriInfo, bd);
		InfoForm infoCreazione = new InfoForm(creazione,Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".creazione.titolo"));

		String ibanAccreditoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".id.id");
		String codIbanId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIban.id");
		String codIbanAppoggioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIbanAppoggio.id");
		String codBicAccreditoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAccredito.id");
		String codBicAppoggioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAppoggio.id");
		String idNegozioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idNegozio.id");
		String idSellerBankId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idSellerBank.id");
		String abilitatoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.id");
		String attivatoObepId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".attivatoObep.id");
		String postaleId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".postale.id");
		String idDominioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");

		if(infoCreazioneMap == null){
			this.initInfoCreazione(uriInfo, bd);
		}

		Sezione sezioneRoot = infoCreazione.getSezioneRoot();
		InputNumber idIbanAccredito = (InputNumber) infoCreazioneMap.get(ibanAccreditoId);
		idIbanAccredito.setDefaultValue(null);
		sezioneRoot.addField(idIbanAccredito);

		InputNumber idDominio = (InputNumber) infoCreazioneMap.get(idDominioId);
		idDominio.setDefaultValue(this.idDominio);
		sezioneRoot.addField(idDominio);

		InputText codIban = (InputText) infoCreazioneMap.get(codIbanId);
		codIban.setDefaultValue(null);
		codIban.setEditable(true);     
		sezioneRoot.addField(codIban);

		InputText codIbanAppoggio = (InputText) infoCreazioneMap.get(codIbanAppoggioId);
		codIbanAppoggio.setDefaultValue(null);
		sezioneRoot.addField(codIbanAppoggio);

		InputText codBicAccredito = (InputText) infoCreazioneMap.get(codBicAccreditoId);
		codBicAccredito.setDefaultValue(null);
		sezioneRoot.addField(codBicAccredito);

		InputText codBicAppoggio = (InputText) infoCreazioneMap.get(codBicAppoggioId);
		codBicAppoggio.setDefaultValue(null);
		sezioneRoot.addField(codBicAppoggio);

		IdNegozio idNegozio = (IdNegozio) infoCreazioneMap.get(idNegozioId);
		idNegozio.setDefaultValue(null);
		sezioneRoot.addField(idNegozio);

		IdSellerBank idSellerBank = (IdSellerBank) infoCreazioneMap.get(idSellerBankId);
		idSellerBank.setDefaultValue(null);
		sezioneRoot.addField(idSellerBank);

		CheckButton abilitato = (CheckButton) infoCreazioneMap.get(abilitatoId);
		abilitato.setDefaultValue(true); 
		sezioneRoot.addField(abilitato);

		CheckButton attivatoObep = (CheckButton) infoCreazioneMap.get(attivatoObepId);
		attivatoObep.setDefaultValue(false); 
		sezioneRoot.addField(attivatoObep);

		CheckButton postale = (CheckButton) infoCreazioneMap.get(postaleId);
		postale.setDefaultValue(false); 
		sezioneRoot.addField(postale);

		return infoCreazione;
	}

	private void initInfoCreazione(UriInfo uriInfo, BasicBD bd) throws ConsoleException{
		if(infoCreazioneMap == null){
			infoCreazioneMap = new HashMap<String, ParamField<?>>();

			String ibanAccreditoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".id.id");
			String codIbanId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIban.id");
			String codIbanAppoggioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIbanAppoggio.id");
			String codBicAccreditoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAccredito.id");
			String codBicAppoggioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAppoggio.id");
			String idNegozioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idNegozio.id");
			String idSellerBankId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idSellerBank.id");
			String abilitatoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.id");
			String attivatoObepId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".attivatoObep.id");
			String postaleId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".postale.id");
			String idDominioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");

			// id 
			InputNumber id = new InputNumber(ibanAccreditoId, null, null, true, true, false, 1, 20);
			infoCreazioneMap.put(ibanAccreditoId, id);

			// idDominio
			InputNumber idDominio = new InputNumber(idDominioId, null, null, true, true, false, 1, 255);
			infoCreazioneMap.put(idDominioId, idDominio);

			// codIban
			String codIbanLabel =  Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIban.label");
			InputText codIban = new InputText(codIbanId, codIbanLabel, null, true, false, true, 5, 34);
			codIban.setValidation(patternIBAN, Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIban.errorMessage"));
			codIban.setSuggestion(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIban.suggestion"));
			infoCreazioneMap.put(codIbanId, codIban);

			// codIbanAppoggio
			String codIbanAppoggioLabel =  Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIbanAppoggio.label");
			InputText codIbanAppoggio = new InputText(codIbanAppoggioId, codIbanAppoggioLabel, null, false, false, true, 5, 34);
			codIbanAppoggio.setValidation(patternIBAN, Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIbanAppoggio.errorMessage"));
			codIbanAppoggio.setAvanzata(true);
			//codIbanAppoggio.setSuggestion(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIbanAppoggio.suggestion"));
			infoCreazioneMap.put(codIbanAppoggioId, codIbanAppoggio);

			// codBicAccredito
			String codBicAccreditoLabel =  Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAccredito.label");
			InputText codBicAccredito = new InputText(codBicAccreditoId, codBicAccreditoLabel, null, false, false, true, 8, 11);
			codBicAccredito.setValidation(patternBIC, Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAccredito.errorMessage"));
			//codBicAccredito.setSuggestion(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAccredito.suggestion"));
			infoCreazioneMap.put(codBicAccreditoId, codBicAccredito);

			// codBicAppoggio
			String codBicAppoggioLabel =  Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAppoggio.label");
			InputText codBicAppoggio = new InputText(codBicAppoggioId, codBicAppoggioLabel, null, false, false, true, 8, 11);
			codBicAppoggio.setAvanzata(true); 
			codBicAppoggio.setValidation(patternBIC, Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAppoggio.errorMessage"));
			//codBicAppoggio.setSuggestion(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAppoggio.suggestion"));
			infoCreazioneMap.put(codBicAppoggioId, codBicAppoggio);

			// attivatoObep
			String attivatoObepLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".attivatoObep.label");
			CheckButton attivatoObep = new CheckButton(attivatoObepId, attivatoObepLabel, null, false, false, true);
			infoCreazioneMap.put(attivatoObepId, attivatoObep);

			List<RawParamValue> attivatoObepValues = new ArrayList<RawParamValue>();
			attivatoObepValues.add(new RawParamValue(attivatoObepId, "false")); 
			// idNegozio
			String idNegozioLabel =  Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idNegozio.label");
			URI idNegozioRefreshUri = this.getUriField(uriInfo, bd, idNegozioId); 

			IdNegozio idNegozio = new IdNegozio(this.nomeServizio, idNegozioId, idNegozioLabel, 1, 255, idNegozioRefreshUri , attivatoObepValues); 
			idNegozio.addDependencyField(attivatoObep);
			idNegozio.init(attivatoObepValues);
			infoCreazioneMap.put(idNegozioId,idNegozio);

			// idSellerBank
			String idSellerBankLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idSellerBank.label");
			URI idSellerBankRefreshUri = this.getUriField(uriInfo, bd, idSellerBankId); 
			IdSellerBank idSellerBank = new IdSellerBank(this.nomeServizio, idSellerBankId, idSellerBankLabel, 1, 255, idSellerBankRefreshUri , attivatoObepValues);
			idSellerBank.addDependencyField(attivatoObep);
			idSellerBank.init(attivatoObepValues);
			infoCreazioneMap.put(idSellerBankId, idSellerBank);

			// abilitato
			String abilitatoLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.label");
			CheckButton abiliato = new CheckButton(abilitatoId, abilitatoLabel, null, false, false, true);
			infoCreazioneMap.put(abilitatoId, abiliato);

			// postale
			String postaleLabel = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".postale.label");
			CheckButton postale = new CheckButton(postaleId, postaleLabel, null, false, false, true);
			infoCreazioneMap.put(postaleId, postale);


		}
	}

	@Override
	public InfoForm getInfoModifica(UriInfo uriInfo, BasicBD bd, IbanAccredito entry) throws ConsoleException {
		URI modifica = this.getUriModifica(uriInfo, bd);
		InfoForm infoModifica = new InfoForm(modifica,Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".modifica.titolo"));

		String ibanAccreditoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".id.id");
		String codIbanId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIban.id");
		String codIbanAppoggioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIbanAppoggio.id");
		String codBicAccreditoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAccredito.id");
		String codBicAppoggioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAppoggio.id");
		String idNegozioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idNegozio.id");
		String idSellerBankId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idSellerBank.id");
		String abilitatoId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.id");
		String attivatoObepId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".attivatoObep.id");
		String postaleId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".postale.id");
		String idDominioId = Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idDominio.id");

		if(infoCreazioneMap == null){
			this.initInfoCreazione(uriInfo, bd);
		}

		Sezione sezioneRoot = infoModifica.getSezioneRoot();
		InputNumber idIbanAccredito = (InputNumber) infoCreazioneMap.get(ibanAccreditoId);
		idIbanAccredito.setDefaultValue(entry.getId());
		sezioneRoot.addField(idIbanAccredito);

		InputNumber idDominio = (InputNumber) infoCreazioneMap.get(idDominioId);
		idDominio.setDefaultValue(entry.getIdDominio()); 
		sezioneRoot.addField(idDominio);

		InputText codIban = (InputText) infoCreazioneMap.get(codIbanId);
		codIban.setDefaultValue(entry.getCodIban());
		codIban.setEditable(false);     
		sezioneRoot.addField(codIban);

		InputText codIbanAppoggio = (InputText) infoCreazioneMap.get(codIbanAppoggioId);
		codIbanAppoggio.setDefaultValue(entry.getCodIbanAppoggio());
		sezioneRoot.addField(codIbanAppoggio);

		InputText codBicAccredito = (InputText) infoCreazioneMap.get(codBicAccreditoId);
		codBicAccredito.setDefaultValue(entry.getCodBicAccredito());
		sezioneRoot.addField(codBicAccredito);

		InputText codBicAppoggio = (InputText) infoCreazioneMap.get(codBicAppoggioId);
		codBicAppoggio.setDefaultValue(entry.getCodBicAppoggio());
		sezioneRoot.addField(codBicAppoggio);

		IdNegozio idNegozio = (IdNegozio) infoCreazioneMap.get(idNegozioId);
		idNegozio.setDefaultValue(entry.getIdNegozio());
		sezioneRoot.addField(idNegozio);

		IdSellerBank idSellerBank = (IdSellerBank) infoCreazioneMap.get(idSellerBankId);
		idSellerBank.setDefaultValue(entry.getIdSellerBank());
		sezioneRoot.addField(idSellerBank);

		CheckButton abilitato = (CheckButton) infoCreazioneMap.get(abilitatoId);
		abilitato.setDefaultValue(entry.isAbilitato()); 
		sezioneRoot.addField(abilitato);

		CheckButton attivatoObep = (CheckButton) infoCreazioneMap.get(attivatoObepId);
		attivatoObep.setDefaultValue(entry.isAttivatoObep()); 
		sezioneRoot.addField(attivatoObep);

		CheckButton postale = (CheckButton) infoCreazioneMap.get(postaleId);
		postale.setDefaultValue(entry.isPostale()); 
		sezioneRoot.addField(postale);
		return infoModifica;
	}

	@Override
	public Object getField(UriInfo uriInfo,List<RawParamValue>values, String fieldId,BasicBD bd) throws ConsoleException {
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
			IbanAccreditoBD ibanAccreditoBD = new IbanAccreditoBD(bd);
			IbanAccredito ibanAccredito = ibanAccreditoBD.getIbanAccredito(id);

			DominiBD dominiBD = new DominiBD(bd);
			Dominio dominio = dominiBD.getDominio(ibanAccredito.getIdDominio());

			InfoForm infoModifica = this.getInfoModifica(uriInfo, bd,ibanAccredito);
			URI cancellazione = null;
			URI esportazione = null;

			Dettaglio dettaglio = new Dettaglio(this.getTitolo(ibanAccredito,bd), esportazione, cancellazione, infoModifica);

			it.govpay.web.rs.dars.model.Sezione root = dettaglio.getSezioneRoot(); 

			// dati dele dettaglio
			root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIban.label"), ibanAccredito.getCodIban());
			root.addVoce(Utils.getInstance().getMessageFromResourceBundle("domini.codDominio.label"), dominio.getCodDominio());
			if(StringUtils.isNotEmpty(ibanAccredito.getCodIbanAppoggio()))
				root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codIbanAppoggio.label"), ibanAccredito.getCodIbanAppoggio(),true);
			if(StringUtils.isNotEmpty(ibanAccredito.getCodBicAccredito()))
				root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAccredito.label"), ibanAccredito.getCodBicAccredito(),true);
			if(StringUtils.isNotEmpty(ibanAccredito.getCodBicAppoggio()))
				root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".codBicAppoggio.label"), ibanAccredito.getCodBicAppoggio(),true);
			if(StringUtils.isNotEmpty(ibanAccredito.getIdNegozio()))
				root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idNegozio.label"), ibanAccredito.getIdNegozio(),true);
			if(StringUtils.isNotEmpty(ibanAccredito.getIdSellerBank()))
				root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".idSellerBank.label"), ibanAccredito.getIdSellerBank(),true);
			root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.label"), Utils.getSiNoAsLabel(ibanAccredito.isAbilitato()));
			root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".attivatoObep.label"), Utils.getSiNoAsLabel(ibanAccredito.isAttivatoObep()));
			root.addVoce(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".postale.label"), Utils.getSiNoAsLabel(ibanAccredito.isPostale()));


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
			throws WebApplicationException, ConsoleException ,ValidationException,DuplicatedEntryException {
		String methodName = "Insert " + this.titoloServizio;

		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			// Operazione consentita solo all'amministratore
			this.darsService.checkOperatoreAdmin(bd);

			IbanAccredito entry = this.creaEntry(is, uriInfo, bd);

			this.checkEntry(entry, null);

			IbanAccreditoBD ibanAccreditoBD = new IbanAccreditoBD(bd);
			try{
				ibanAccreditoBD.getIbanAccredito(entry.getIdDominio(),entry.getCodIban());
				String msg = Utils.getInstance().getMessageWithParamsFromResourceBundle(this.nomeServizio + ".oggettoEsistente", entry.getCodIban());
				throw new DuplicatedEntryException(msg);
			}catch(NotFoundException e){}

			ibanAccreditoBD.insertIbanAccredito(entry); 

			this.log.info("Esecuzione " + methodName + " completata.");

			return this.getDettaglio(entry.getId(), uriInfo, bd);
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

	@Override
	public IbanAccredito creaEntry(InputStream is, UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException {
		String methodName = "creaEntry " + this.titoloServizio;
		IbanAccredito entry = null;
		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			// Operazione consentita solo all'amministratore
			this.darsService.checkOperatoreAdmin(bd);

			JsonConfig jsonConfig = new JsonConfig();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Utils.copy(is, baos);

			baos.flush();
			baos.close();

			JSONObject jsonObjectIBAN = JSONObject.fromObject( baos.toString() );  
			jsonConfig.setRootClass(IbanAccredito.class);
			entry = (IbanAccredito) JSONObject.toBean( jsonObjectIBAN, jsonConfig );

			this.log.info("Esecuzione " + methodName + " completata.");
			return entry;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw new ConsoleException(e);
		}
	}
	@Override
	public void checkEntry(IbanAccredito entry, IbanAccredito oldEntry) throws ValidationException {
		if(entry.getCodIban() == null)  throw new ValidationException("Codice Iban Accredito nullo");

		if(entry.getCodIban().length() < 5 || entry.getCodIban().length() > 34)
			throw new ValidationException("La lunghezza dell'Iban Accredito deve essere compresa tra 5 e 34 caratteri, trovati " + entry.getCodIban().length() + ".");

		Pattern ibanPattern= Pattern.compile(patternIBAN);
		Matcher matcher = ibanPattern.matcher(entry.getCodIban());

		if(!matcher.matches())
			throw new ValidationException("Il formato dell'Iban Accredito e' errato."); 

		if(StringUtils.isNotEmpty(entry.getCodIbanAppoggio())){
			if(entry.getCodIbanAppoggio().length() < 5 || entry.getCodIbanAppoggio().length() > 34)
				throw new ValidationException("La lunghezza dell'Iban Appoggio deve essere compresa tra 5 e 34 caratteri, trovati " + entry.getCodIbanAppoggio().length() + ".");

			matcher = ibanPattern.matcher(entry.getCodIbanAppoggio());
			if(!matcher.matches())
				throw new ValidationException("Il formato dell'Iban Appoggio e' errato.");
		}
		
		// validazione dei bic
		Pattern bicPattern= Pattern.compile(patternBIC); //[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}
		
		if(StringUtils.isNotEmpty(entry.getCodBicAccredito())){
			if(entry.getCodBicAccredito().length() < 8 || entry.getCodBicAccredito().length() > 11)
				throw new ValidationException("La lunghezza dell'Bic Accredito deve essere compresa tra 8 e 11 caratteri, trovati " + entry.getCodBicAccredito().length() + ".");

			Matcher bicMtcher = bicPattern.matcher(entry.getCodBicAccredito());
			if(!bicMtcher.matches())
				throw new ValidationException("Il formato del Bic Accredito e' errato.");
		}
		
		if(StringUtils.isNotEmpty(entry.getCodBicAppoggio())){
			if(entry.getCodBicAppoggio().length() < 8 || entry.getCodBicAppoggio().length() > 11)
				throw new ValidationException("La lunghezza dell'Bic Appoggio deve essere compresa tra 8 e 11 caratteri, trovati " + entry.getCodBicAppoggio().length() + ".");

			Matcher bicMtcher = bicPattern.matcher(entry.getCodBicAppoggio());
			if(!bicMtcher.matches())
				throw new ValidationException("Il formato del Bic Appoggio e' errato.");
		}

		// update
		if(oldEntry != null){
			if(!entry.getCodIban().equals(oldEntry.getCodIban())) throw new ValidationException("Non e' consentito modificare l'Iban Accredito");
		}

	}

	@Override
	public Dettaglio update(InputStream is, UriInfo uriInfo, BasicBD bd) throws WebApplicationException, ConsoleException, ValidationException {
		String methodName = "Update " + this.titoloServizio;

		try{
			this.log.info("Esecuzione " + methodName + " in corso...");
			// Operazione consentita solo all'amministratore
			this.darsService.checkOperatoreAdmin(bd);

			IbanAccredito entry = this.creaEntry(is, uriInfo, bd);

			IbanAccreditoBD ibanAccreditoBD = new IbanAccreditoBD(bd);
			IbanAccredito oldEntry = ibanAccreditoBD.getIbanAccredito(entry.getIdDominio(),entry.getCodIban());

			this.checkEntry(entry, oldEntry); 

			ibanAccreditoBD.updateIbanAccredito(entry); 

			this.log.info("Esecuzione " + methodName + " completata.");
			return this.getDettaglio(entry.getId(), uriInfo, bd);
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
	}


	@Override
	public String getTitolo(IbanAccredito entry, BasicBD bd) {
		return entry.getCodIban();
	}

	@Override
	public String getSottotitolo(IbanAccredito entry, BasicBD bd) {
		StringBuilder sb = new StringBuilder();

		sb.append(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".abilitato.label")).append(": ").append(Utils.getSiNoAsLabel(entry.isAbilitato()));
		sb.append(", ").append(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".attivatoObep.label")).append(": ").append(Utils.getSiNoAsLabel(entry.isAttivatoObep()));
		sb.append(", ").append(Utils.getInstance().getMessageFromResourceBundle(this.nomeServizio + ".postale.label")).append(": ").append(Utils.getSiNoAsLabel(entry.isPostale()));

		return Utils.getAbilitatoAsLabel(entry.isAbilitato()); 
	}
	
	@Override
	public List<String> getValori(IbanAccredito entry, BasicBD bd) throws ConsoleException {
		return null;
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

	@Override
	public Object uplaod(MultipartFormDataInput input, UriInfo uriInfo, BasicBD bd)	throws WebApplicationException, ConsoleException, ValidationException { return null;}
}

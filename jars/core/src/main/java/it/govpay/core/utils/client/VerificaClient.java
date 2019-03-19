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
package it.govpay.core.utils.client;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.UtilsException;
import org.openspcoop2.utils.json.ValidationException;
import org.openspcoop2.utils.logger.beans.Property;
import org.openspcoop2.utils.service.context.IContext;
import org.slf4j.Logger;

import it.govpay.bd.BasicBD;
import it.govpay.bd.model.Applicazione;
import it.govpay.bd.model.Versamento;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.exceptions.VersamentoAnnullatoException;
import it.govpay.core.exceptions.VersamentoDuplicatoException;
import it.govpay.core.exceptions.VersamentoScadutoException;
import it.govpay.core.exceptions.VersamentoSconosciutoException;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.core.utils.client.v1.VerificaConverter;
import it.govpay.core.utils.rawutils.ConverterUtils;
import it.govpay.core.utils.validator.PendenzaVerificataValidator;
import it.govpay.ec.v1.beans.PendenzaVerificata;
import it.govpay.ec.v1.beans.StatoPendenzaVerificata;
import it.govpay.model.Versionabile.Versione;

public class VerificaClient extends BasicClient {

	private static final String ERROR_MESSAGE_ERRORE_NELLA_DESERIALIZZAZIONE_DEL_MESSAGGIO_DI_RISPOSTA_0 = "Errore nella deserializzazione del messaggio di risposta ({0})";
	private static final String AZIONE_SOAP_PA_VERIFICA_VERSAMENTO = "paVerificaVersamento";
	private static final String LOG_KEY_VERIFICA_VERIFICA_KO = "verifica.verificaKo";
	private static Logger log = LoggerWrapperFactory.getLogger(VerificaClient.class);
	private Versione versione;
	private String codApplicazione;


	public VerificaClient(Applicazione applicazione) throws ClientException {
		super(applicazione, TipoConnettore.VERIFICA);
		this.versione = applicazione.getConnettoreVerifica().getVersione();
		this.codApplicazione = applicazione.getCodApplicazione();
	}

	/**
	 * GESTIONE INTERNA DELLA CONNESSIONE AL DB.
	 * Fornirla aperta con tutto gia' committato.
	 * Viene restituita aperta.
	 * @throws UtilsException 
	 * @throws ValidationException 
	 */
	public Versamento invoke(String codVersamentoEnte, String bundlekey, String codUnivocoDebitore, String codDominio, String iuv, BasicBD bd) throws ClientException, ServiceException, VersamentoAnnullatoException, VersamentoDuplicatoException, VersamentoScadutoException, VersamentoSconosciutoException, GovPayException, UtilsException, ValidationException {

		String codVersamentoEnteD = codVersamentoEnte != null ? codVersamentoEnte : "-";
		String bundlekeyD = bundlekey != null ? bundlekey : "-";
		String debitoreD = codUnivocoDebitore != null ? codUnivocoDebitore : "-";
		String codDominioD = codDominio != null ? codDominio : "-";
		String iuvD = iuv != null ? iuv : "-";

		log.debug("Richiedo la verifica per il versamento [Applicazione:" + this.codApplicazione + " Versamento:" + codVersamentoEnteD + " BundleKey:" + bundlekeyD + " Debitore:" + codUnivocoDebitore + " Dominio:" + codDominioD + " Iuv:" + iuvD + "] in versione (" + this.versione.toString() + ") alla URL ("+this.url+")");

		IContext ctx = GpThreadLocal.get();
		GpContext appContext = (GpContext) ctx.getApplicationContext();

		try {
			this.operationID = appContext.setupPaClient(this.codApplicazione, AZIONE_SOAP_PA_VERIFICA_VERSAMENTO, this.url.toExternalForm(), this.versione);
			ctx.getApplicationLogger().log("verifica.verifica", this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD);

			//Chiudo la connessione al DB prima della comunicazione HTTP
			bd.closeConnection();

			List<Property> headerProperties = new ArrayList<>();
			headerProperties.add(new Property("Accept", "application/json"));
			String jsonResponse = "";

			String path = null;

			if(iuv == null) {
				path = "/pendenze/" + this.codApplicazione + "/" + codVersamentoEnte;
			} else {
				path = "/avvisi/" + codDominio + "/" + iuv;
			}

			PendenzaVerificata pendenzaVerificata = null;
			try {
				try {
					jsonResponse = new String(this.getJson(path, headerProperties));
					pendenzaVerificata = ConverterUtils.parse(jsonResponse, PendenzaVerificata.class); 
				}catch(ClientException e) {
					String logErrorMessage = MessageFormat.format(ERROR_MESSAGE_ERRORE_NELLA_DESERIALIZZAZIONE_DEL_MESSAGGIO_DI_RISPOSTA_0,	e.getMessage());
					ctx.getApplicationLogger().log(LOG_KEY_VERIFICA_VERIFICA_KO, this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD, logErrorMessage);
					throw e;
				} catch (ValidationException e) {
					ctx.getApplicationLogger().log(LOG_KEY_VERIFICA_VERIFICA_KO, this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD, "[SINTASSI] " + e.getMessage());
					throw e;
				} catch(ServiceException e) {
					String logErrorMessage = MessageFormat.format(ERROR_MESSAGE_ERRORE_NELLA_DESERIALIZZAZIONE_DEL_MESSAGGIO_DI_RISPOSTA_0,	e.getMessage());
					ctx.getApplicationLogger().log(LOG_KEY_VERIFICA_VERIFICA_KO, this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD, logErrorMessage);
					throw new ClientException(e);
				}
			} finally {
				bd.setupConnection(GpThreadLocal.get().getTransactionId());
			}
			
			StatoPendenzaVerificata stato = pendenzaVerificata.getStato();
			
			if(stato == null)
				throw new ServiceException("Stato pendenza non gestito: null");
			
			switch (stato) {
			case NON_ESEGUITA: // CASO OK su
				ctx.getApplicationLogger().log("verifica.avvio", this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD);
				new PendenzaVerificataValidator(pendenzaVerificata).validate();
				try {
					return it.govpay.core.business.VersamentoUtils.toVersamentoModel(VerificaConverter.getVersamentoFromPendenzaVerificata(pendenzaVerificata),bd);
				} catch (GovPayException e) {
					ctx.getApplicationLogger().log(LOG_KEY_VERIFICA_VERIFICA_KO, this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD, "[" + e.getCodEsito() + "] " + e.getMessage());
					throw e;
				} catch (ValidationException e) {
					ctx.getApplicationLogger().log(LOG_KEY_VERIFICA_VERIFICA_KO, this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD, "[SINTASSI] " + e.getMessage());
					throw e;
				}
			case ANNULLATA:
				ctx.getApplicationLogger().log("verifica.verificaAnnullato", this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD);
				throw new VersamentoAnnullatoException(pendenzaVerificata.getDescrizioneStato());
			case DUPLICATA:
				ctx.getApplicationLogger().log("verifica.verificaDuplicato", this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD);
				throw new VersamentoDuplicatoException(pendenzaVerificata.getDescrizioneStato());
			case SCADUTA:
				ctx.getApplicationLogger().log("verifica.verificaScaduto", this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD);
				if(StringUtils.isNotEmpty(pendenzaVerificata.getDescrizioneStato()))
					throw new VersamentoScadutoException(pendenzaVerificata.getDescrizioneStato());
				else 
					throw new VersamentoScadutoException(pendenzaVerificata.getDataScadenza() != null ? pendenzaVerificata.getDataScadenza() : null);
			case SCONOSCIUTA:
				ctx.getApplicationLogger().log("verifica.verificaSconosciuto", this.codApplicazione, codVersamentoEnteD, bundlekeyD, debitoreD, codDominioD, iuvD);
				throw new VersamentoSconosciutoException();
			default:
				throw new ValidationException("Stato pendenza non gestito: " + stato.name());
			}
		} catch (ServiceException e) {
			ctx.getApplicationLogger().log(LOG_KEY_VERIFICA_VERIFICA_KO, this.codApplicazione, codVersamentoEnteD, codDominioD, iuvD, e.getMessage());
			throw e;
		}  
	}

	public class SendEsitoResponse {

		private int responseCode;
		private String detail;
		public int getResponseCode() {
			return this.responseCode;
		}
		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}
		public String getDetail() {
			return this.detail;
		}
		public void setDetail(String detail) {
			this.detail = detail;
		}
	}
	
	@Override
	public String getOperationId() {
		return this.operationID;
	}
}
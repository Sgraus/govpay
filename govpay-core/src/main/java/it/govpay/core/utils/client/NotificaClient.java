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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.logger.beans.Property;
import org.openspcoop2.utils.resources.Charset;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import it.gov.digitpa.schemas._2011.pagamenti.CtRicevutaTelematica;
import it.govpay.bd.BasicBD;
import it.govpay.bd.model.Applicazione;
import it.govpay.bd.model.Notifica;
import it.govpay.bd.model.Pagamento;
import it.govpay.bd.model.Rpt;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.exceptions.NdpException;
import it.govpay.core.rs.v1.beans.base.Riscossione;
import it.govpay.core.utils.Gp21Utils;
import it.govpay.core.utils.Gp25Utils;
import it.govpay.core.utils.JaxbUtils;
import it.govpay.core.utils.UriBuilderUtils;
import it.govpay.model.Connettore.Tipo;
import it.govpay.model.Rr;
import it.govpay.model.Versionabile.Versione;
import it.govpay.servizi.commons.StatoRevoca;
import it.govpay.servizi.pa.ObjectFactory;
import it.govpay.servizi.pa.PaNotificaStorno;
import it.govpay.servizi.pa.PaNotificaStorno.RichiestaStorno;
import it.govpay.servizi.pa.PaNotificaTransazione;

public class NotificaClient extends BasicClient {

	private static Logger log = LoggerWrapperFactory.getLogger(NotificaClient.class);
	private Tipo tipo;
	private Versione versione;
	private static ObjectFactory objectFactory;

	public NotificaClient(Applicazione applicazione) throws ClientException {
		super(applicazione, TipoConnettore.NOTIFICA);
		this.versione = applicazione.getConnettoreNotifica().getVersione();
		this.tipo = applicazione.getConnettoreNotifica().getTipo();

		if(objectFactory == null || log == null ){
			objectFactory = new ObjectFactory();
		}
	}

	/**
	 * Business utilizzati da precaricare:
	 * notifica.getApplicazione
	 * notifica.getRpt.getVersamento
	 * notifica.getRpt.getCanale
	 * notifica.getRpt.getPsp
	 * notifica.getRpt.getPagamenti
	 * 
	 * @param notifica
	 * @return
	 * @throws ServiceException 
	 * @throws GovPayException 
	 * @throws ClientException
	 * @throws SAXException 
	 * @throws JAXBException 
	 * @throws NdpException 
	 */
	public void invoke(Notifica notifica, BasicBD bd) throws ClientException, ServiceException, GovPayException, JAXBException, SAXException, NdpException {

		log.debug("Spedisco la notifica di " + notifica.getTipo() + ((notifica.getIdRr() == null) ? " PAGAMENTO" : " STORNO") + " della transazione (" + notifica.getRpt(null).getCodDominio() + ")(" + notifica.getRpt(null).getIuv() + ")(" + notifica.getRpt(null).getCcp() + ") in versione (" + this.versione.toString() + ") alla URL ("+this.url+")");

		switch (this.tipo) {
		case SOAP:
			if(notifica.getIdRr() == null) {
				Rpt rpt = notifica.getRpt(null);
				PaNotificaTransazione paNotificaTransazione = new PaNotificaTransazione();
				paNotificaTransazione.setCodApplicazione(notifica.getApplicazione(bd).getCodApplicazione());
				paNotificaTransazione.setCodVersamentoEnte(rpt.getVersamento(bd).getCodVersamentoEnte());
				paNotificaTransazione.setTransazione(Gp25Utils.toTransazione(rpt, bd));
				paNotificaTransazione.setCodSessionePortale(rpt.getCodSessionePortale());

				QName qname = new QName("http://www.govpay.it/servizi/pa/", "paNotificaTransazione");
				this.sendSoap("paNotificaTransazione", new JAXBElement<>(qname, PaNotificaTransazione.class, paNotificaTransazione), null, false);
				return;
			} else {
				Rr rr = notifica.getRr(bd);
				Rpt rpt = notifica.getRr(bd).getRpt(bd);
				PaNotificaStorno paNotificaStorno = new PaNotificaStorno();
				paNotificaStorno.setCodApplicazione(notifica.getApplicazione(bd).getCodApplicazione());
				paNotificaStorno.setCodVersamentoEnte(rpt.getVersamento(bd).getCodVersamentoEnte());
				RichiestaStorno richiestaStorno = new RichiestaStorno();
				richiestaStorno.setCcp(rr.getCcp());
				richiestaStorno.setCodDominio(rr.getCodDominio());
				richiestaStorno.setCodRichiesta(rr.getCodMsgRevoca());
				richiestaStorno.setDataRichiesta(rr.getDataMsgRevoca());
				richiestaStorno.setDescrizioneStato(rr.getDescrizioneStato());
				richiestaStorno.setEr(rr.getXmlEr());
				if(rr.getImportoTotaleRevocato() != null)
					richiestaStorno.setImportoStornato(rr.getImportoTotaleRevocato());
				richiestaStorno.setIuv(rr.getIuv());
				richiestaStorno.setRr(rr.getXmlRr());
				richiestaStorno.setStato(StatoRevoca.valueOf(rr.getStato().toString()));
				paNotificaStorno.setRichiestaStorno(richiestaStorno);
				QName qname = new QName("http://www.govpay.it/servizi/pa/", "paNotificaStorno");
				this.sendSoap("paNotificaStorno", new JAXBElement<>(qname, PaNotificaStorno.class, paNotificaStorno), null, false);
				return;
			}
		case REST:
			List<Property> headerProperties = new ArrayList<>();
			headerProperties.add(new Property("Accept", "application/json"));
			String jsonBody = "";
			String path = "";

			if(notifica.getIdRr() == null) {
				Rpt rpt = notifica.getRpt(null);
				path = "/pagamenti/" + rpt.getCodDominio() + "/"+ rpt.getIuv();

				boolean amp = false;
				if(rpt.getCodSessione() != null) {
					amp = true;
					path += "?idSession=" + encode(rpt.getCodSessione());
				}

				if(rpt.getCodSessionePortale() != null) {
					if(amp) {
						path += "?idSessionePortale=" + encode(rpt.getCodSessionePortale());
						amp = true;
					} else {
						path += "&idSessionePortale=" + encode(rpt.getCodSessionePortale());
					}

				}

				if(rpt.getCodCarrello() != null) {
					if(amp) {
						path += "?idCarrello=" + encode(rpt.getCodCarrello());
						amp = true;
					} else {
						path += "&idCarrello=" + encode(rpt.getCodCarrello());
					}
				}

				it.govpay.core.rs.v1.beans.client.Notifica notificaRsModel = new it.govpay.core.rs.v1.beans.client.Notifica();
				notificaRsModel.setIdA2A(notifica.getApplicazione(bd).getCodApplicazione());
				notificaRsModel.setIdPendenza(rpt.getVersamento(bd).getCodVersamentoEnte());
				// rpt
				notificaRsModel.setRpt(JaxbUtils.toRPT(rpt.getXmlRpt())); 
				// rt
				if(rpt.getXmlRt() != null) {
					CtRicevutaTelematica rt = JaxbUtils.toRT(rpt.getXmlRt());
					notificaRsModel.setRt(rt);
				}
				// elenco pagamenti
				if(rpt.getPagamenti(bd) != null && rpt.getPagamenti(bd).size() > 0) {
					List<Riscossione> riscossioni = new ArrayList<>();
					int indice = 1;
					String urlPendenza = UriBuilderUtils.getPendenzaByIdA2AIdPendenza(notifica.getApplicazione(bd).getCodApplicazione(), rpt.getVersamento(bd).getCodVersamentoEnte());
					String urlRpt = UriBuilderUtils.getRppByDominioIuvCcp(rpt.getCodDominio(), rpt.getIuv(), rpt.getCcp());
					for(Pagamento pagamento : rpt.getPagamenti(bd)) {
						riscossioni.add(Gp21Utils.toRiscossione(pagamento, bd, indice, urlPendenza, urlRpt));
						indice ++;
					}
					notificaRsModel.setRiscossioni(riscossioni);
				}

				jsonBody = notificaRsModel.toJSON(null);

			} else {
				throw new ServiceException("Notifica Storno REST non implementata!");
			}
			this.sendJson(path, jsonBody, headerProperties);
			break;
		}

	}

	private String encode(String value) {
		try {
			return URLEncoder.encode(value, Charset.UTF_8.getValue());
		} catch (UnsupportedEncodingException e) {
			return "";
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
}

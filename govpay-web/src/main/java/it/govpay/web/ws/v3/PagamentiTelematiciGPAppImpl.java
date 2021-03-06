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
package it.govpay.web.ws.v3;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.logger.beans.Property;
import org.openspcoop2.utils.logger.beans.proxy.Actor;
import org.slf4j.Logger;
import org.slf4j.MDC;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.model.Applicazione;
import it.govpay.bd.model.Fr;
import it.govpay.bd.model.Rendicontazione;
import it.govpay.bd.model.Rpt;
import it.govpay.bd.model.Utenza;
import it.govpay.bd.model.Versamento;
import it.govpay.bd.pagamento.FrBD;
import it.govpay.core.business.model.CaricaIuvDTO;
import it.govpay.core.business.model.CaricaIuvDTOResponse;
import it.govpay.core.business.model.GeneraIuvDTO;
import it.govpay.core.business.model.GeneraIuvDTOResponse;
import it.govpay.core.business.model.Iuv;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.rs.v1.costanti.EsitoOperazione;
import it.govpay.core.utils.CredentialUtils;
import it.govpay.core.utils.Gp23Utils;
import it.govpay.core.utils.Gp25Utils;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.core.utils.IuvUtils;
import it.govpay.core.utils.VersamentoUtils;
import it.govpay.servizi.commons.MetaInfo;
import it.govpay.servizi.commons.StatoVersamento;
import it.govpay.servizi.v2_3.commons.GpResponse;
import it.govpay.servizi.v2_3.commons.Mittente;
import it.govpay.servizi.v2_3.gpapp.GpAnnullaVersamento;
import it.govpay.servizi.v2_3.gpapp.GpCaricaIuv;
import it.govpay.servizi.v2_3.gpapp.GpCaricaIuvResponse;
import it.govpay.servizi.v2_3.gpapp.GpCaricaVersamento;
import it.govpay.servizi.v2_3.gpapp.GpCaricaVersamentoResponse;
import it.govpay.servizi.v2_3.gpapp.GpChiediFlussoRendicontazione;
import it.govpay.servizi.v2_3.gpapp.GpChiediFlussoRendicontazioneResponse;
import it.govpay.servizi.v2_3.gpapp.GpChiediListaFlussiRendicontazione;
import it.govpay.servizi.v2_3.gpapp.GpChiediListaFlussiRendicontazioneResponse;
import it.govpay.servizi.v2_3.gpapp.GpChiediStatoVersamento;
import it.govpay.servizi.v2_3.gpapp.GpChiediStatoVersamentoResponse;
import it.govpay.servizi.v2_3.gpapp.GpGeneraIuv;
import it.govpay.servizi.v2_3.gpapp.GpGeneraIuvResponse;
import it.govpay.servizi.v2_3.gpapp.GpNotificaPagamento;
import it.govpay.servizi.v2_5.PagamentiTelematiciGPApp;
import it.govpay.web.ws.Utils;

@WebService(serviceName = "PagamentiTelematiciGPAppService",
endpointInterface = "it.govpay.servizi.v2_5.PagamentiTelematiciGPApp",
targetNamespace = "http://www.govpay.it/servizi/v2_5",
portName = "GPAppPort",
wsdlLocation="/wsdl/GpApp_2.5.wsdl",
name="PagamentiTelematiciGPAppService")

@HandlerChain(file="../../../../../handler-chains/handler-chain-gpws.xml")

@org.apache.cxf.annotations.SchemaValidation
public class PagamentiTelematiciGPAppImpl implements PagamentiTelematiciGPApp {

	@Resource
	WebServiceContext wsCtxt;
	
	private static Logger log = LoggerWrapperFactory.getLogger(PagamentiTelematiciGPPrtImpl.class);

	@Override
	public GpGeneraIuvResponse gpGeneraIuv(GpGeneraIuv bodyrichiesta, MetaInfo metaInfo) {
		log.info("Richiesta operazione gpGeneraIuv di " + bodyrichiesta.getIuvRichiesto().size() + " Iuv per (" + bodyrichiesta.getCodApplicazione() + ")");
		GpGeneraIuvResponse response = new GpGeneraIuvResponse();
		GpContext ctx = GpThreadLocal.get();
		Utils.loadMetaInfo(ctx, metaInfo);
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			Applicazione applicazioneAutenticata = this.getApplicazioneAutenticata(bd);
			ctx.log("ws.ricevutaRichiesta");
			this.verificaApplicazione(applicazioneAutenticata, bodyrichiesta.getCodApplicazione());
			it.govpay.core.business.Iuv iuvBusiness = new it.govpay.core.business.Iuv(bd);
			GeneraIuvDTO dto = new GeneraIuvDTO();
			dto.setApplicazioneAutenticata(applicazioneAutenticata);
			dto.setCodApplicazione(bodyrichiesta.getCodApplicazione());
			dto.setCodDominio(bodyrichiesta.getCodDominio());
			dto.getIuvRichiesto().addAll(Gp23Utils.toIuvRichiesto(bodyrichiesta.getIuvRichiesto()));
			GeneraIuvDTOResponse dtoResponse = iuvBusiness.generaIUV(dto);
			response.getIuvGenerato().addAll(Gp23Utils.toIuvGenerato(dtoResponse.getIuvGenerato()));
			response.setCodEsito(EsitoOperazione.OK.toString());
			response.setDescrizioneEsito("Operazione completata con successo");
			response.setMittente(Mittente.GOV_PAY);
			ctx.log("ws.ricevutaRichiestaOk");
		} catch (GovPayException gpe) {
			response = (GpGeneraIuvResponse) gpe.getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} catch (Exception e) {
			response = (GpGeneraIuvResponse) new GovPayException(e).getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(MDC.get("op"));
		return response;
	}
	
	@Override
	public GpCaricaIuvResponse gpCaricaIuv(GpCaricaIuv bodyrichiesta) {
		log.info("Richiesta operazione gpCaricaIuv di " + bodyrichiesta.getIuvGenerato().size() + " Iuv per (" + bodyrichiesta.getCodApplicazione() + ")");
		GpCaricaIuvResponse response = new GpCaricaIuvResponse();
		GpContext ctx = GpThreadLocal.get();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			Applicazione applicazioneAutenticata = this.getApplicazioneAutenticata(bd);
			ctx.log("ws.ricevutaRichiesta");
			this.verificaApplicazione(applicazioneAutenticata, bodyrichiesta.getCodApplicazione());
			it.govpay.core.business.Iuv iuvBusiness = new it.govpay.core.business.Iuv(bd);
			CaricaIuvDTO dto = new CaricaIuvDTO();
			dto.setApplicazioneAutenticata(applicazioneAutenticata);
			dto.setCodApplicazione(bodyrichiesta.getCodApplicazione());
			dto.setCodDominio(bodyrichiesta.getCodDominio());
			dto.getIuvDaCaricare().addAll(Gp23Utils.toIuvDaCaricare(bodyrichiesta.getIuvGenerato()));
			CaricaIuvDTOResponse dtoResponse = iuvBusiness.caricaIUV(dto);
			response.getIuvCaricato().addAll(Gp23Utils.toIuvCaricato(dtoResponse.getIuvCaricato()));			
			response.setCodEsito(EsitoOperazione.OK.toString());
			response.setDescrizioneEsito("Operazione completata con successo");
			response.setMittente(Mittente.GOV_PAY);
			ctx.log("ws.ricevutaRichiestaOk");
		} catch (GovPayException gpe) {
			response = (GpCaricaIuvResponse) gpe.getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} catch (Exception e) {
			response = (GpCaricaIuvResponse) new GovPayException(e).getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(MDC.get("op"));
		return response;
	}

	@Override
	public GpCaricaVersamentoResponse gpCaricaVersamento(GpCaricaVersamento bodyrichiesta, MetaInfo metaInfo) {
		log.info("Richiesta operazione gpCaricaVersamento per il versamento (" + bodyrichiesta.getVersamento().getCodVersamentoEnte() + ") dell'applicazione (" +  bodyrichiesta.getVersamento().getCodApplicazione()+") con generazione IUV (" + bodyrichiesta.isGeneraIuv() + ")");
		GpCaricaVersamentoResponse response = new GpCaricaVersamentoResponse();
		GpContext ctx = GpThreadLocal.get();
		Utils.loadMetaInfo(ctx, metaInfo);
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			Applicazione applicazioneAutenticata = this.getApplicazioneAutenticata(bd);
			ctx.log("ws.ricevutaRichiesta");
			
			ctx.getContext().getRequest().addGenericProperty(new Property("codApplicazione", bodyrichiesta.getVersamento().getCodApplicazione()));
			ctx.getContext().getRequest().addGenericProperty(new Property("codVersamentoEnte", bodyrichiesta.getVersamento().getCodVersamentoEnte()));
			ctx.setCorrelationId(bodyrichiesta.getVersamento().getCodApplicazione() + bodyrichiesta.getVersamento().getCodVersamentoEnte());
			ctx.log("versamento.carica");
			
			this.verificaApplicazione(applicazioneAutenticata, bodyrichiesta.getVersamento().getCodApplicazione());
			it.govpay.core.business.Versamento versamentoBusiness = new it.govpay.core.business.Versamento(bd);
			it.govpay.servizi.commons.Versamento versamento = bodyrichiesta.getVersamento();
			it.govpay.bd.model.Versamento versamentoModel = VersamentoUtils.toVersamentoModel(versamento, bd);
			boolean aggiornaSeEsiste = true;
			if(bodyrichiesta.isAggiornaSeEsiste() != null) {
				aggiornaSeEsiste = bodyrichiesta.isAggiornaSeEsiste();
			}
			it.govpay.model.Iuv iuv = versamentoBusiness.caricaVersamento(applicazioneAutenticata, versamentoModel, bodyrichiesta.isGeneraIuv(), aggiornaSeEsiste);

			if(iuv != null) {
				Iuv iuvGenerato = IuvUtils.toIuv(versamentoModel.getApplicazione(bd), versamentoModel.getUo(bd).getDominio(bd), iuv, versamento.getImportoTotale());
				response.setIuvGenerato(Gp23Utils.toIuvGenerato(iuvGenerato));
				ctx.getContext().getResponse().addGenericProperty(new Property("codDominio", iuvGenerato.getCodDominio()));
				ctx.getContext().getResponse().addGenericProperty(new Property("iuv", iuvGenerato.getIuv()));
				ctx.log("versamento.caricaOkIuv");
			} else {
				ctx.log("versamento.caricaOk");
			}
			response.setCodEsito(EsitoOperazione.OK.toString());
			response.setDescrizioneEsito("Operazione completata con successo");
			response.setMittente(Mittente.GOV_PAY);
			ctx.log("ws.ricevutaRichiestaOk");
		} catch (GovPayException gpe) {
			response = (GpCaricaVersamentoResponse) gpe.getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} catch (Exception e) {
			response = (GpCaricaVersamentoResponse) new GovPayException(e).getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(MDC.get("op"));
		return response;
	}

	@Override
	public GpResponse gpAnnullaVersamento(GpAnnullaVersamento bodyrichiesta) {
		log.info("Richiesta operazione gpChiediAnnullaVersamento per il versamento (" + bodyrichiesta.getCodVersamentoEnte() + ") dell'applicazione (" +  bodyrichiesta.getCodApplicazione()+")");
		GpResponse response = new GpResponse();
		GpContext ctx = GpThreadLocal.get();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			Applicazione applicazioneAutenticata = this.getApplicazioneAutenticata(bd);
			ctx.log("ws.ricevutaRichiesta");
			
			ctx.getContext().getRequest().addGenericProperty(new Property("codApplicazione", bodyrichiesta.getCodApplicazione()));
			ctx.getContext().getRequest().addGenericProperty(new Property("codVersamentoEnte", bodyrichiesta.getCodVersamentoEnte()));
			ctx.setCorrelationId(bodyrichiesta.getCodApplicazione() + bodyrichiesta.getCodVersamentoEnte());
			ctx.log("versamento.annulla");
			
			this.verificaApplicazione(applicazioneAutenticata, bodyrichiesta.getCodApplicazione());
			it.govpay.core.business.Versamento versamentoBusiness = new it.govpay.core.business.Versamento(bd);
			versamentoBusiness.annullaVersamento(applicazioneAutenticata, bodyrichiesta.getCodApplicazione(), bodyrichiesta.getCodVersamentoEnte());
			ctx.log("versamento.annullaOk");
			
			response.setCodEsito(EsitoOperazione.OK.toString());
			response.setDescrizioneEsito("Operazione completata con successo");
			response.setMittente(Mittente.GOV_PAY);
			ctx.log("ws.ricevutaRichiestaOk");
		} catch (GovPayException gpe) {
			response = gpe.getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} catch (Exception e) {
			response = new GovPayException(e).getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(MDC.get("op"));
		return response;
	}
	
	@Override
	public GpResponse gpNotificaPagamento(GpNotificaPagamento bodyrichiesta) {
		log.info("Richiesta operazione gpNotificaPagamento per il versamento (" + bodyrichiesta.getCodVersamentoEnte() + ") dell'applicazione (" +  bodyrichiesta.getCodApplicazione()+")");
		GpResponse response = new GpResponse();
		GpContext ctx = GpThreadLocal.get();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			Applicazione applicazioneAutenticata = this.getApplicazioneAutenticata(bd);
			ctx.log("ws.ricevutaRichiesta");
			this.verificaApplicazione(applicazioneAutenticata, bodyrichiesta.getCodApplicazione());
			it.govpay.core.business.Versamento versamentoBusiness = new it.govpay.core.business.Versamento(bd);
			versamentoBusiness.notificaPagamento(applicazioneAutenticata, bodyrichiesta.getCodApplicazione(), bodyrichiesta.getCodVersamentoEnte());
			response.setCodEsito(EsitoOperazione.OK.toString());
			response.setDescrizioneEsito("Operazione completata con successo");
			response.setMittente(Mittente.GOV_PAY);
			ctx.log("ws.ricevutaRichiestaOk");
		} catch (GovPayException gpe) {
			response = gpe.getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} catch (Exception e) {
			response = new GovPayException(e).getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(MDC.get("op"));
		return response;
	}

	@Override
	public GpChiediStatoVersamentoResponse gpChiediStatoVersamento(GpChiediStatoVersamento bodyrichiesta) {
		log.info("Richiesta operazione gpChiediStatoVersamento per il versamento (" + bodyrichiesta.getCodVersamentoEnte() + ") dell'applicazione (" +  bodyrichiesta.getCodApplicazione()+")");
		GpChiediStatoVersamentoResponse response = new GpChiediStatoVersamentoResponse();
		GpContext ctx = GpThreadLocal.get();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			Applicazione applicazioneAutenticata = this.getApplicazioneAutenticata(bd);
			ctx.log("ws.ricevutaRichiesta");
			this.verificaApplicazione(applicazioneAutenticata, bodyrichiesta.getCodApplicazione());
			it.govpay.core.business.Versamento versamentoBusiness = new it.govpay.core.business.Versamento(bd);
			Versamento versamento = versamentoBusiness.chiediVersamento(bodyrichiesta.getCodApplicazione(), bodyrichiesta.getCodVersamentoEnte(), null, null, null, null);
			response.setCodApplicazione(versamento.getApplicazione(bd).getCodApplicazione());
			response.setCodEsito(EsitoOperazione.OK.toString());
			response.setDescrizioneEsito("Operazione completata con successo");
			response.setMittente(Mittente.GOV_PAY);
			response.setCodVersamentoEnte(versamento.getCodVersamentoEnte());
			response.setStato(StatoVersamento.valueOf(versamento.getStatoVersamento().toString()));
			List<Rpt> rpts = versamento.getRpt(bd);
			for(Rpt rpt : rpts) {
				response.getTransazione().add(Gp25Utils.toTransazione(rpt, bd));
			}
			ctx.log("ws.ricevutaRichiestaOk");
		} catch (GovPayException gpe) {
			response = (GpChiediStatoVersamentoResponse) gpe.getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} catch (Exception e) {
			response = (GpChiediStatoVersamentoResponse) new GovPayException(e).getWsResponse(response, "ws.ricevutaRichiestaKo", log);
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(MDC.get("op"));
		return response;
	}

	@Override
	public GpChiediListaFlussiRendicontazioneResponse gpChiediListaFlussiRendicontazione(GpChiediListaFlussiRendicontazione bodyrichiesta) {
		log.info("Richiesta operazione gpChiediListaFlussiRendicontazione");
		GpChiediListaFlussiRendicontazioneResponse response = new GpChiediListaFlussiRendicontazioneResponse();
		GpContext ctx = GpThreadLocal.get();
		BasicBD bd = null;
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			Applicazione applicazione = this.getApplicazioneAutenticata(bd);
			this.verificaApplicazione(applicazione, bodyrichiesta.getCodApplicazione());
			ctx.log("ws.ricevutaRichiesta");
			it.govpay.core.business.Rendicontazioni rendicontazioneBusiness = new it.govpay.core.business.Rendicontazioni(bd);
			
			Date da = null, a=null;
			
			if(bodyrichiesta.getDataInizio() != null) {
				Calendar inizio = Calendar.getInstance();
				inizio.setTime(bodyrichiesta.getDataInizio());
				inizio.set(Calendar.HOUR_OF_DAY, 0);
				inizio.set(Calendar.MINUTE, 0);
				inizio.set(Calendar.SECOND, 0);
				inizio.set(Calendar.MILLISECOND, 0);
				da = inizio.getTime();
			}
			
			if(bodyrichiesta.getDataFine() != null) {
				Calendar fine = Calendar.getInstance();
				fine.setTime(bodyrichiesta.getDataFine());
				fine.set(Calendar.HOUR_OF_DAY, 23);
				fine.set(Calendar.MINUTE, 59);
				fine.set(Calendar.SECOND, 59);
				fine.set(Calendar.MILLISECOND, 999);
				a = fine.getTime();
			}
			
			List<Fr> rendicontazioni = rendicontazioneBusiness.chiediListaRendicontazioni(applicazione, bodyrichiesta.getCodDominio(), bodyrichiesta.getCodApplicazione(), da, a);
			for(Fr frModel : rendicontazioni) {
				response.getFlussoRendicontazione().add(Gp23Utils.toFr(frModel, bd));
			}
			response.setCodEsito(EsitoOperazione.OK.toString());
			response.setDescrizioneEsito("Operazione completata con successo");
			response.setMittente(Mittente.GOV_PAY);
			ctx.log("ws.ricevutaRichiestaOk");
		} catch (GovPayException gpe) {
			response = (GpChiediListaFlussiRendicontazioneResponse) gpe.getWsResponse(response, "gprnd.ricevutaRichiestaKo", log);
		} catch (Exception e) {
			response = (GpChiediListaFlussiRendicontazioneResponse) new GovPayException(e).getWsResponse(response, "gprnd.ricevutaRichiestaKo", log);
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(MDC.get("op"));
		return response;
	}

	
	@Override
	public GpChiediFlussoRendicontazioneResponse gpChiediFlussoRendicontazione(GpChiediFlussoRendicontazione bodyrichiesta) {
		
		log.info("Richiesta operazione gpChiediFlussoRendicontazione");
		GpChiediFlussoRendicontazioneResponse response = new GpChiediFlussoRendicontazioneResponse();
		GpContext ctx = GpThreadLocal.get();
		BasicBD bd = null;
		
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			Applicazione applicazione = this.getApplicazioneAutenticata(bd);
			ctx.log("gprnd.ricevutaRichiesta");

			//Autorizzazione alla richiesta: controllo che il dominio sia tra quelli abilitati per l'applicazione
			Fr frModel = new FrBD(bd).getFr(bodyrichiesta.getCodFlusso());
			
			this.verificaApplicazione(applicazione, bodyrichiesta.getCodApplicazione());
			
			
			List<Rendicontazione> rends = frModel.getRendicontazioni(bd);
			for(Rendicontazione rend : rends) {
				if(rend.getPagamento(bd) == null) {
					try {
						it.govpay.bd.model.Versamento versamento = new it.govpay.core.business.Versamento(bd).chiediVersamento(null, null, null, null,	frModel.getDominio(bd).getCodDominio(), rend.getIuv());
						rend.setVersamento(versamento);
					}catch (Exception e) {
						continue;
					}
				}
			}
			
			if(bodyrichiesta.getCodApplicazione() != null) {
				Long idApplicazione = AnagraficaManager.getApplicazione(bd, bodyrichiesta.getCodApplicazione()).getId();
				List<Rendicontazione> rendsFiltrato = new ArrayList<>();
				
				for(Rendicontazione rend : rends) {
					if(rend.getVersamento(bd) ==  null || rend.getVersamento(bd).getIdApplicazione() != idApplicazione.longValue()) {
						continue;
					}
					rendsFiltrato.add(rend);
				}
				
				rends = rendsFiltrato;
			}
			
			
			response.setFlussoRendicontazione(Gp23Utils.toFr(frModel, rends, bd));
			response.setCodEsito(EsitoOperazione.OK.toString());
			response.setDescrizioneEsito("Operazione completata con successo");
			response.setMittente(Mittente.GOV_PAY);
			ctx.log("gprnd.ricevutaRichiestaOk");
		} catch (GovPayException gpe) {
			response = (GpChiediFlussoRendicontazioneResponse) gpe.getWsResponse(response, "gprnd.ricevutaRichiestaKo", log);
		} catch (NotFoundException gpe) {
			response.setCodEsito(EsitoOperazione.OK.toString());
			response.setDescrizioneEsito("Operazione completata con successo");
			response.setMittente(Mittente.GOV_PAY);
		} catch (Exception e) {
			response = (GpChiediFlussoRendicontazioneResponse) new GovPayException(e).getWsResponse(response, "gprnd.ricevutaRichiestaKo", log);
		} finally {
			if(ctx != null) {
				ctx.setResult(response);
				ctx.log();
			}
			if(bd != null) bd.closeConnection();
		}
		response.setCodOperazione(MDC.get("op"));
		return response;
	}

	private Applicazione getApplicazioneAutenticata(BasicBD bd) throws GovPayException, ServiceException {
		Applicazione app = null;
		try {
			HttpServletRequest request = (HttpServletRequest) this.wsCtxt.getMessageContext().get(MessageContext.SERVLET_REQUEST);  
			Utenza user = CredentialUtils.getUser(request, log);
			if(user == null) {
				throw new GovPayException(EsitoOperazione.AUT_000);
			}
			app = CredentialUtils.getApplicazione(bd,user);
		} catch (NotFoundException e) {
			throw new GovPayException(EsitoOperazione.AUT_001, this.wsCtxt.getUserPrincipal().getName());
		}
		
		if(app != null) {
			Actor from = new Actor();
			from.setName(app.getCodApplicazione());
			from.setType(GpContext.TIPO_SOGGETTO_APP);
			GpThreadLocal.get().getTransaction().setFrom(from);
			GpThreadLocal.get().getTransaction().getClient().setName(app.getCodApplicazione());
		}
		return app;
	}
	
	private void verificaApplicazione(Applicazione applicazioneAutenticata, String codApplicazione) throws GovPayException {
		if(!applicazioneAutenticata.getCodApplicazione().equals(codApplicazione))
			throw new GovPayException(EsitoOperazione.APP_002, applicazioneAutenticata.getCodApplicazione(), codApplicazione);
	}

}

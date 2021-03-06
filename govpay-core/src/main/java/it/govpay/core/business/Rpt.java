package it.govpay.core.business;

import java.util.ArrayList;
import java.util.List;

import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.logger.beans.Property;
import org.slf4j.Logger;

import it.gov.digitpa.schemas._2011.ws.paa.FaultBean;
import it.gov.digitpa.schemas._2011.ws.paa.NodoChiediStatoRPTRisposta;
import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.model.Applicazione;
import it.govpay.bd.model.Canale;
import it.govpay.bd.model.Notifica;
import it.govpay.bd.model.PagamentoPortale;
import it.govpay.bd.model.SingoloVersamento;
import it.govpay.bd.model.Stazione;
import it.govpay.bd.model.Utenza;
import it.govpay.bd.model.UtenzaApplicazione;
import it.govpay.bd.model.UtenzaCittadino;
import it.govpay.bd.model.Versamento;
import it.govpay.bd.pagamento.IuvBD;
import it.govpay.bd.pagamento.NotificheBD;
import it.govpay.bd.pagamento.RptBD;
import it.govpay.core.business.model.Risposta;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.exceptions.VersamentoAnnullatoException;
import it.govpay.core.exceptions.VersamentoDuplicatoException;
import it.govpay.core.exceptions.VersamentoScadutoException;
import it.govpay.core.exceptions.VersamentoSconosciutoException;
import it.govpay.core.rs.v1.costanti.EsitoOperazione;
import it.govpay.core.utils.AclEngine;
import it.govpay.core.utils.DateUtils;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.core.utils.IuvUtils;
import it.govpay.core.utils.RptBuilder;
import it.govpay.core.utils.RptUtils;
import it.govpay.core.utils.SimpleDateFormatUtils;
import it.govpay.core.utils.UrlUtils;
import it.govpay.core.utils.VersamentoUtils;
import it.govpay.core.utils.client.BasicClient.ClientException;
import it.govpay.core.utils.client.NodoClient.Azione;
import it.govpay.core.utils.thread.InviaNotificaThread;
import it.govpay.core.utils.thread.ThreadExecutorManager;
import it.govpay.model.Acl.Diritti;
import it.govpay.model.Acl.Servizio;
import it.govpay.model.Anagrafica;
import it.govpay.model.Intermediario;
import it.govpay.model.Iuv.TipoIUV;
import it.govpay.model.Notifica.TipoNotifica;
import it.govpay.model.Rpt.StatoRpt;
import it.govpay.model.Versamento.StatoVersamento;

public class Rpt extends BasicBD{

	private static Logger log = LoggerWrapperFactory.getLogger(Rpt.class);

	public Rpt(BasicBD basicBD) {
		super(basicBD);
	}

	public List<it.govpay.bd.model.Rpt> avviaTransazione(List<Versamento> versamenti, Utenza utenzaChiamante, Canale canale, String ibanAddebito, Anagrafica versante, String autenticazione, String redirect, boolean aggiornaSeEsiste) throws GovPayException {
		return this.avviaTransazione(versamenti, utenzaChiamante, canale, ibanAddebito, versante, autenticazione, redirect, aggiornaSeEsiste, null);
	}

	public List<it.govpay.bd.model.Rpt> avviaTransazione(List<Versamento> versamenti, Utenza utenza, Canale canale, String ibanAddebito, Anagrafica versante, String autenticazione, String redirect, boolean aggiornaSeEsiste, PagamentoPortale pagamentoPortale) throws GovPayException {
		GpContext ctx = GpThreadLocal.get();
		try {
			ctx.getPagamentoCtx().setCarrello(true);
			String codCarrello = RptUtils.buildUUID35();
			if(pagamentoPortale != null) codCarrello= pagamentoPortale.getIdSessione();
			ctx.getPagamentoCtx().setCodCarrello(codCarrello);
			ctx.getContext().getRequest().addGenericProperty(new Property("codCarrello", codCarrello));
			ctx.setCorrelationId(codCarrello);
			ctx.log("pagamento.avviaTransazioneCarrelloWISP20");

			Stazione stazione = null;

			for(Versamento versamentoModel : versamenti) {

				ctx.log("rpt.validazioneSemantica", versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte());

				for(SingoloVersamento sv : versamentoModel.getSingoliVersamenti(this)) {

					String codTributo = sv.getTributo(this) != null ? sv.getTributo(this).getCodTributo() : null;

					log.debug("Verifica autorizzazione " + utenza.getTipoUtenza() + " [" + utenza.getIdentificativo() + "] al caricamento tributo [" + codTributo + "] per dominio [" + versamentoModel.getUo(this).getDominio(this).getCodDominio() + "]...");

					List<Diritti> diritti = new ArrayList<>(); 
					diritti.add(Diritti.ESECUZIONE);

					if(!AclEngine.isAuthorized(utenza, Servizio.PAGAMENTI_E_PENDENZE, versamentoModel.getUo(this).getDominio(this).getCodDominio(), codTributo,diritti)) {
						log.warn("Non autorizzato applicazione " + utenza.getTipoUtenza() + " [" + utenza.getIdentificativo() + "] al caricamento tributo [" + codTributo + "] per dominio [" + versamentoModel.getUo(this).getDominio(this).getCodDominio() + "] ");
						throw new GovPayException(EsitoOperazione.APP_003, utenza.getIdentificativo(), versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte());
					}

					log.debug("Autorizzato " + utenza.getTipoUtenza() + " [" + utenza.getIdentificativo() + "] al caricamento tributo [" + codTributo + "] per dominio [" + versamentoModel.getUo(this).getDominio(this).getCodDominio() + "]");

				}

				log.debug("Verifica autorizzazione pagamento del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "]...");
				if(!versamentoModel.getStatoVersamento().equals(StatoVersamento.NON_ESEGUITO)) {
					log.debug("Non autorizzato pagamento del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "]: pagamento in stato diverso da " + StatoVersamento.NON_ESEGUITO);
					throw new GovPayException(EsitoOperazione.PAG_006, versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte(), versamentoModel.getStatoVersamento().toString());
				} else {
					log.debug("Autorizzato pagamento del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "]: pagamento in stato " + StatoVersamento.NON_ESEGUITO);
				}

				log.debug("Verifica scadenza del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "]...");
				if(versamentoModel.getDataScadenza() != null && DateUtils.isDataScaduta(versamentoModel.getDataScadenza())) {
					log.warn("Scadenza del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "] decorsa.");
					throw new GovPayException(EsitoOperazione.PAG_007, versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte(), SimpleDateFormatUtils.newSimpleDateFormatSoloData().format(versamentoModel.getDataScadenza()));
				} else { // versamento non scaduto, controllo data validita'
					log.debug("Verifica validita' del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "]...");
					if(versamentoModel.getDataValidita() != null && DateUtils.isDataScaduta(versamentoModel.getDataValidita())) {

						if(versamentoModel.getId() == null) {
							// Versamento fornito scaduto. Ritorno errore.
							throw new GovPayException(EsitoOperazione.PAG_012, versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte(), SimpleDateFormatUtils.newSimpleDateFormatSoloData().format(versamentoModel.getDataValidita()));
						} else {
							// Versammento in archivio scaduto. Ne chiedo un aggiornamento.
							log.info("Validita del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "] decorsa. Avvio richiesta di aggiornamento all'applicazione.");
							try {
								versamentoModel = VersamentoUtils.aggiornaVersamento(versamentoModel, this);
								log.info("Versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "] aggiornato tramite servizio di verifica.");
							} catch (VersamentoAnnullatoException e){
								log.warn("Aggiornamento del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "] fallito: versamento annullato");
								throw new GovPayException(EsitoOperazione.VER_013, versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte());
							} catch (VersamentoScadutoException e) {
								log.warn("Aggiornamento del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "] fallito: versamento scaduto");
								throw new GovPayException(EsitoOperazione.VER_010, versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte());
							} catch (VersamentoDuplicatoException e) {
								log.warn("Aggiornamento del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "] fallito: versamento duplicato");
								throw new GovPayException(EsitoOperazione.VER_012, versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte());
							} catch (VersamentoSconosciutoException e) {
								log.warn("Aggiornamento del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "] fallito: versamento sconosciuto");
								throw new GovPayException(EsitoOperazione.VER_011, versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte());
							} catch (ClientException e) {
								log.warn("Aggiornamento del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "] fallito: errore di interazione con il servizio di verifica.");
								throw new GovPayException(EsitoOperazione.VER_014, versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte(), e.getMessage());
							}
						}
					} else { 
						// versamento valido passo
					}
				}

				log.debug("Scadenza del versamento [" + versamentoModel.getCodVersamentoEnte() + "] applicazione [" + versamentoModel.getApplicazione(this).getCodApplicazione() + "] verificata.");

				if(stazione == null) {
					stazione = versamentoModel.getUo(this).getDominio(this).getStazione();
				} else {
					if(stazione.getId().compareTo(versamentoModel.getUo(this).getDominio(this).getStazione().getId()) != 0) {
						throw new GovPayException(EsitoOperazione.PAG_000);
					}
				}

				ctx.log("rpt.validazioneSemanticaOk", versamentoModel.getApplicazione(this).getCodApplicazione(), versamentoModel.getCodVersamentoEnte());
			}

			Intermediario intermediario = AnagraficaManager.getIntermediario(this, stazione.getIdIntermediario());

			Iuv iuvBusiness = new Iuv(this);
			IuvBD iuvBD = new IuvBD(this);
			RptBD rptBD = new RptBD(this);
			it.govpay.core.business.Versamento versamentiBusiness = new it.govpay.core.business.Versamento(this);
			this.setAutoCommit(false);
			List<it.govpay.bd.model.Rpt> rpts = new ArrayList<>();
			for(Versamento versamento : versamenti) {
				// Aggiorno tutti i versamenti che mi sono stati passati

				if(versamento.getId() == null) {
					versamentiBusiness.caricaVersamento(versamento, false, aggiornaSeEsiste);
				}
				it.govpay.model.Iuv iuv = null;
				String ccp = null;

				// Verifico se ha uno IUV suggerito ed in caso lo assegno
				if(versamento.getIuvProposto() != null) {
					log.debug("IUV Proposto: " + versamento.getIuvProposto());
					TipoIUV tipoIuv = iuvBusiness.getTipoIUV(versamento.getIuvProposto());
					iuvBusiness.checkIUV(versamento.getUo(this).getDominio(this), versamento.getIuvProposto(), tipoIuv);
					iuv = iuvBusiness.caricaIUV(versamento.getApplicazione(this), versamento.getUo(this).getDominio(this), versamento.getIuvProposto(), tipoIuv, versamento.getCodVersamentoEnte());
					if(tipoIuv.equals(TipoIUV.NUMERICO))
						ccp = IuvUtils.buildCCP();
					else 
						ccp = it.govpay.model.Rpt.CCP_NA;
					ctx.log("iuv.assegnazioneIUVCustom", versamento.getApplicazione(this).getCodApplicazione(), versamento.getCodVersamentoEnte(), versamento.getUo(this).getDominio(this).getCodDominio(), versamento.getIuvProposto(), ccp);
				} else {
					// Verifico se ha gia' uno IUV numerico assegnato. In tal caso lo riuso. 
					try {
						log.debug("Cerco iuv gia' assegnato....");
						iuv = iuvBD.getIuv(versamento.getIdApplicazione(), versamento.getCodVersamentoEnte(), TipoIUV.NUMERICO);
						log.debug(".. iuv gia' assegnato: " + iuv.getIuv());
						ccp = IuvUtils.buildCCP();
						ctx.log("iuv.assegnazioneIUVRiuso", versamento.getApplicazione(this).getCodApplicazione(), versamento.getCodVersamentoEnte(), versamento.getUo(this).getDominio(this).getCodDominio(), iuv.getIuv(), ccp);
					} catch (NotFoundException e) {
						log.debug("Iuv non assegnato. Generazione...");
						// Non c'e' iuv assegnato. Glielo genero io.
						iuv = iuvBusiness.generaIUV(versamento.getApplicazione(this), versamento.getUo(this).getDominio(this), versamento.getCodVersamentoEnte(), it.govpay.model.Iuv.TipoIUV.ISO11694);
						if(iuvBusiness.getTipoIUV(iuv.getIuv()).equals(TipoIUV.ISO11694)) {
							ccp = it.govpay.model.Rpt.CCP_NA;
						} else {
							ccp = IuvUtils.buildCCP();
						}
						ctx.log("iuv.assegnazioneIUVGenerato", versamento.getApplicazione(this).getCodApplicazione(), versamento.getCodVersamentoEnte(), versamento.getUo(this).getDominio(this).getCodDominio(), iuv.getIuv(), ccp);
					}
				}

				if(ctx.getPagamentoCtx().getCodCarrello() != null) {
					ctx.setCorrelationId(ctx.getPagamentoCtx().getCodCarrello());
				} else {
					ctx.setCorrelationId(versamento.getUo(this).getDominio(this).getCodDominio() + iuv.getIuv() + ccp);
				}
				it.govpay.bd.model.Rpt rpt = new RptBuilder().buildRpt(ctx.getPagamentoCtx().getCodCarrello(), versamento, canale, iuv.getIuv(), ccp, versante, autenticazione, ibanAddebito, redirect, this);
				rpt.setCodSessionePortale(ctx.getPagamentoCtx().getCodSessionePortale());

				if(pagamentoPortale!= null)
					rpt.setIdPagamentoPortale(pagamentoPortale.getId());

				rptBD.insertRpt(rpt);
				rpts.add(rpt);
				ctx.log("rpt.creazioneRpt", versamento.getUo(this).getDominio(this).getCodDominio(), iuv.getIuv(), ccp, rpt.getCodMsgRichiesta());
				log.info("Inserita Rpt per il versamento ("+versamento.getCodVersamentoEnte()+") dell'applicazione (" + versamento.getApplicazione(this).getCodApplicazione() + ") con dominio (" + rpt.getCodDominio() + ") iuv (" + rpt.getIuv() + ") ccp (" + rpt.getCcp() + ")");
			} 

			this.commit();
			this.closeConnection();

			// Spedisco le RPT al Nodo
			// Se ho una GovPayException, non ho sicuramente spedito nulla.
			// Se ho una ClientException non so come sia andata la consegna.

			String idTransaction = null;
			try {

				idTransaction = ctx.openTransaction();
				Risposta risposta = null;
				ctx.setupNodoClient(stazione.getCodStazione(), null, Azione.nodoInviaCarrelloRPT);
				ctx.getContext().getRequest().addGenericProperty(new Property("codCarrello", ctx.getPagamentoCtx().getCodCarrello()));
				ctx.log("rpt.invioCarrelloRpt");
				risposta = RptUtils.inviaCarrelloRPT(intermediario, stazione, rpts, this);
				this.setupConnection(GpThreadLocal.get().getTransactionId());
				if(risposta.getEsito() == null || !risposta.getEsito().equals("OK")) {
					// RPT rifiutata dal Nodo
					// Aggiorno lo stato e ritorno l'errore
					try {

						for(FaultBean fb : risposta.getListaErroriRPT()) {
							it.govpay.bd.model.Rpt rpt = rpts.get(fb.getSerial() - 1);
							String descrizione = null; 
							if(fb != null) {
								descrizione = "[" + fb.getFaultCode() + "] " + fb.getFaultString();
								descrizione = fb.getDescription() != null ? descrizione + ": " + fb.getDescription() : descrizione;
							}
							rpt.setStato(StatoRpt.RPT_RIFIUTATA_NODO);
							rpt.setDescrizioneStato(descrizione);
							rptBD.updateRpt(rpt.getId(), StatoRpt.RPT_RIFIUTATA_NODO, descrizione, null, null);
						}

					} catch (Exception e) {
						// Se uno o piu' aggiornamenti vanno male, non importa. 
						// si risolvera' poi nella verifica pendenti
					} 
					// Potrebbero rimanere escluse delle RPT dall'aggiornamento:
					for(it.govpay.bd.model.Rpt rpt : rpts) {
						if(!rpt.getStato().equals(StatoRpt.RPT_RIFIUTATA_NODO)) {
							try {
								String descrizione = "Richiesta di pagamento rifiutata per errori rilevati in altre RPT del carrello";
								rptBD.updateRpt(rpt.getId(), StatoRpt.RPT_RIFIUTATA_NODO, descrizione, null, null);
							} catch (NotFoundException e) {
								// Se uno o piu' aggiornamenti vanno male, non importa. 
								// si risolvera' poi nella verifica pendenti
							}
						}
					}
					ctx.log("rpt.invioKo", risposta.getLog());
					log.info("RPT rifiutata dal Nodo dei Pagamenti: " + risposta.getLog());
					throw new GovPayException(risposta.getFaultBean());
				} else {
					log.info("Rpt accettata dal Nodo dei Pagamenti");
					// RPT accettata dal Nodo
					// Aggiorno lo stato e ritorno
					if(risposta.getUrl() != null) {
						log.debug("Redirect URL: " + risposta.getUrl());
						ctx.log("rpt.invioOk");
					} else {
						log.debug("Nessuna URL di redirect");
						ctx.log("rpt.invioOkNoRedirect");
					}
					return this.updateStatoRpt(rpts, StatoRpt.RPT_ACCETTATA_NODO, risposta.getUrl(), pagamentoPortale, null);
				}
			} catch (ClientException e) {
				// ClientException: tento una chiedi stato RPT per recuperare lo stato effettivo.
				//   - RPT non esistente: rendo un errore NDP per RPT non inviata
				//   - RPT esistente: faccio come OK
				//   - Errore nella richiesta: rendo un errore NDP per stato sconosciuto
				ctx.log("rpt.invioFail", e.getMessage());
				log.warn("Errore nella spedizione dell'Rpt: " + e);
				NodoChiediStatoRPTRisposta risposta = null;
				String idTransaction2 = null;
				log.info("Attivazione della procedura di recupero del processo di pagamento.");
				try {
					idTransaction2 = ctx.openTransaction();
					ctx.setupNodoClient(stazione.getCodStazione(), rpts.get(0).getCodDominio(), Azione.nodoChiediStatoRPT);
					risposta = RptUtils.chiediStatoRPT(intermediario, stazione, rpts.get(0), this);
				} catch (ClientException ee) {
					ctx.log("rpt.invioRecoveryStatoRPTFail", ee.getMessage());
					log.warn("Errore nella richiesta di stato RPT: " + ee.getMessage() + ". Recupero stato fallito.");
					this.updateStatoRpt(rpts, StatoRpt.RPT_ERRORE_INVIO_A_NODO, "Impossibile comunicare con il Nodo dei Pagamenti SPC: " + e.getMessage(), pagamentoPortale, null);
					throw new GovPayException(EsitoOperazione.NDP_000, e, "Errore nella consegna della richiesta di pagamento al Nodo dei Pagamenti");
				} finally {
					ctx.closeTransaction(idTransaction2);
				}
				if(risposta.getEsito() == null) {
					// anche la chiedi stato e' fallita
					ctx.log("rpt.invioRecoveryStatoRPTKo", risposta.getFault().getFaultCode(), risposta.getFault().getFaultString(), risposta.getFault().getDescription());
					log.warn("Recupero sessione fallito. Errore nella richiesta di stato RPT: " + risposta.getFault().getFaultCode() + " " + risposta.getFault().getFaultString());
					throw new GovPayException(EsitoOperazione.NDP_000, e, "Errore nella consegna della richiesta di pagamento al Nodo dei Pagamenti");
				} else {
					StatoRpt statoRpt = StatoRpt.toEnum(risposta.getEsito().getStato());
					log.info("Acquisito stato RPT dal nodo: " + risposta.getEsito().getStato());


					if(statoRpt.equals(StatoRpt.RT_ACCETTATA_PA) || statoRpt.equals(StatoRpt.RT_RIFIUTATA_PA)) {
						ctx.log("rpt.invioRecoveryStatoRPTcompletato");
						log.info("Processo di pagamento gia' completato.");
						// Ho gia' trattato anche la RT. Non faccio nulla.
						throw new GovPayException(EsitoOperazione.NDP_000, e, "Richiesta di pagamento gia' gestita dal Nodo dei Pagamenti");
					}


					// Ho lo stato aggiornato. Aggiorno il db
					if(risposta.getEsito().getUrl() != null) {
						log.info("Processo di pagamento recuperato. Url di redirect: " + risposta.getEsito().getUrl());
						ctx.getContext().getResponse().addGenericProperty(new Property("redirectUrl", risposta.getEsito().getUrl()));
						ctx.log("rpt.invioRecoveryStatoRPTOk");
					} else {
						log.info("Processo di pagamento recuperato. Nessuna URL di redirect.");
						ctx.log("rpt.invioRecoveryStatoRPTOk");
					}
					return this.updateStatoRpt(rpts, statoRpt, risposta.getEsito().getUrl(), pagamentoPortale, e);
				}
			} finally {
				ctx.closeTransaction(idTransaction);
			}
		} catch (ServiceException e) {
			this.rollback();
			throw new GovPayException(e);
		} catch (GovPayException e){
			this.rollback();
			throw e;
		} 
	}

	private List<it.govpay.bd.model.Rpt> updateStatoRpt(List<it.govpay.bd.model.Rpt> rpts, StatoRpt statoRpt, String url, PagamentoPortale pagamentoPortale, Exception e) throws ServiceException, GovPayException { 
		GpContext ctx = GpThreadLocal.get();
		this.setupConnection(ctx.getTransactionId());
		String sessionId = null;
		NotificheBD notificheBD = new NotificheBD(this);
		try {
			if(url != null)
				sessionId = UrlUtils.getCodSessione(url);
		} catch (Exception ee) {
			log.warn("Impossibile acquisire l'idSessione dalla URL di redirect al PSP: " + url, ee);
		}

		for(it.govpay.bd.model.Rpt rpt : rpts) {
			Notifica notifica = new Notifica(rpt, TipoNotifica.ATTIVAZIONE, this);
			rpt.setPspRedirectURL(url);
			rpt.setStato(statoRpt);
			rpt.setCodSessione(sessionId);
			try {
				RptBD rptBD = new RptBD(this);
				rptBD.updateRpt(rpt.getId(), statoRpt, null, sessionId, url);
				notificheBD.insertNotifica(notifica);
				ThreadExecutorManager.getClientPoolExecutor().execute(new InviaNotificaThread(notifica, this));
			} catch (Exception ee) {
				// Se uno o piu' aggiornamenti vanno male, non importa. 
				// si risolvera' poi nella verifica pendenti
			} 
		}

		switch (statoRpt) {
		case RPT_RIFIUTATA_NODO:
		case RPT_ERRORE_INVIO_A_NODO:
		case RPT_ERRORE_INVIO_A_PSP:
		case RPT_RIFIUTATA_PSP:
			// Casi di rifiuto. Rendo l'errore
			throw new GovPayException(EsitoOperazione.NDP_000, e);
		default:

			String codSessione = rpts.get(0).getCodSessione();

			if(codSessione != null) {
				ctx.getContext().getResponse().addGenericProperty(new Property("codPspSession", codSessione));
			}

			if(codSessione != null) {
				ctx.log("pagamento.invioCarrelloRpt");
			} else {
				ctx.log("pagamento.invioCarrelloRptNoRedirect");
			}

			log.info("RPT inviata correttamente al nodo");
			return rpts;
		}
	}

	public it.govpay.bd.model.Rpt chiediTransazione(Applicazione applicazioneAutenticata, String codDominio, String iuv, String ccp) throws GovPayException, ServiceException {
		if(!applicazioneAutenticata.getUtenza().isAbilitato())
			throw new GovPayException(EsitoOperazione.APP_001, applicazioneAutenticata.getCodApplicazione());

		RptBD rptBD = new RptBD(this);
		try {
			it.govpay.bd.model.Rpt rpt = rptBD.getRpt(codDominio, iuv, ccp);
			if(!applicazioneAutenticata.getId().equals(rpt.getIdApplicazione())) {
				throw new GovPayException(EsitoOperazione.APP_004); 
			}
			return rpt;
		} catch (NotFoundException e) {
			throw new GovPayException(EsitoOperazione.PAG_008);
		}
	}
}

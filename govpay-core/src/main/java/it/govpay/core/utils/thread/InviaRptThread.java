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
package it.govpay.core.utils.thread;


import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.logger.beans.Property;
import org.slf4j.Logger;
import org.slf4j.MDC;

import it.gov.digitpa.schemas._2011.ws.paa.FaultBean;
import it.govpay.bd.BasicBD;
import it.govpay.bd.model.Notifica;
import it.govpay.bd.model.Rpt;
import it.govpay.bd.pagamento.NotificheBD;
import it.govpay.bd.pagamento.RptBD;
import it.govpay.core.business.model.Risposta;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.core.utils.RptUtils;
import it.govpay.core.utils.client.BasicClient.ClientException;
import it.govpay.core.utils.client.NodoClient.Azione;
import it.govpay.model.Notifica.TipoNotifica;
import it.govpay.model.Rpt.StatoRpt;

public class InviaRptThread implements Runnable {
	
	private Rpt rpt;
	private static Logger log = LoggerWrapperFactory.getLogger(InviaRptThread.class);
	
	public InviaRptThread(Rpt rpt, BasicBD bd) throws ServiceException {
		this.rpt = rpt;
		this.rpt.getIntermediario(bd);
		this.rpt.getStazione(bd);
	}
	
	@Override
	public void run() {
		BasicBD bd = null;
		GpContext ctx = null;
		
		try {
			ctx = new GpContext(this.rpt.getIdTransazioneRpt());
			GpThreadLocal.set(ctx);
			MDC.put("cmd", "InviaRptThread");
			MDC.put("op", ctx.getTransactionId());
			
			ctx.setupNodoClient(this.rpt.getStazione(bd).getCodStazione(), this.rpt.getCodDominio(), Azione.nodoInviaCarrelloRPT);
			
			log.info("Spedizione RPT al Nodo [CodMsgRichiesta: " + this.rpt.getCodMsgRichiesta() + "]");
			
			ctx.getContext().getRequest().addGenericProperty(new Property("codDominio", this.rpt.getCodDominio()));
			ctx.getContext().getRequest().addGenericProperty(new Property("iuv", this.rpt.getIuv()));
			ctx.getContext().getRequest().addGenericProperty(new Property("ccp", this.rpt.getCcp()));
			
			ctx.log("pagamento.invioRptAttivata");
				
			Risposta risposta = RptUtils.inviaRPT(rpt, bd);

			if(bd == null) {
				bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			}
			
			RptBD rptBD = new RptBD(bd);
			
			// Prima di procedere allo'aggiornamento dello stato verifico che nel frattempo non sia arrivato una RT
			this.rpt = rptBD.getRpt(this.rpt.getId());
			if(this.rpt.getStato().equals(StatoRpt.RT_ACCETTATA_PA)) {
				// E' arrivata l'RT nel frattempo. Non aggiornare.
				log.info("RPT inviata, ma nel frattempo e' arrivata l'RT. Non aggiorno lo stato");
				ctx.log("pagamento.invioRptAttivataRTricevuta");
				return;
			}
				
			
			if(!risposta.getEsito().equals("OK") && !risposta.getFaultBean().getFaultCode().equals("PPT_RPT_DUPLICATA")) {
				// RPT rifiutata dal Nodo
				// Loggo l'errore ma lascio lo stato invariato. 
				// v3.1: Perche' non cambiare lo stato a fronte di un rifiuto? Lo aggiorno e evito la rispedizione.
				// Redo: Perche' e' difficile capire se e' un errore temporaneo o meno. Essendo un'attivazione di RPT, non devo smettere di riprovare.
				FaultBean fb = risposta.getFaultBean();
				String descrizione = null; 
				if(fb != null)
					descrizione = fb.getFaultCode() + ": " + fb.getFaultString();
				rptBD.updateRpt(this.rpt.getId(), null, descrizione, null, null);
				log.warn("RPT rifiutata dal nodo con fault " + descrizione);
				ctx.log("pagamento.invioRptAttivataKo", fb.getFaultCode(), fb.getFaultString(), fb.getDescription() != null ? fb.getDescription() : "[-- Nessuna descrizione --]");
			} else {
				// RPT accettata dal Nodo
				// Invio la notifica e aggiorno lo stato
				Notifica notifica = new Notifica(this.rpt, TipoNotifica.ATTIVAZIONE, bd);
				NotificheBD notificheBD = new NotificheBD(bd);
				
				
				bd.setAutoCommit(false);
				rptBD.updateRpt(this.rpt.getId(), StatoRpt.RPT_ACCETTATA_NODO, null, null, null);
				notificheBD.insertNotifica(notifica);
				bd.commit();
				
				ThreadExecutorManager.getClientPoolExecutor().execute(new InviaNotificaThread(notifica, bd));
				log.info("RPT inviata correttamente al nodo");
				ctx.log("pagamento.invioRptAttivataOk");
			}
		} catch (ClientException e) {
			log.error("Errore nella spedizione della RPT", e);
			ctx.log("pagamento.invioRptAttivataFail", e.getMessage());
		} catch (Exception e) {
			log.error("Errore nella spedizione della RPT", e);
			ctx.log("pagamento.invioRptAttivataFail", e.getMessage());
			if(bd != null) bd.rollback();
		} finally {
			if(ctx != null) ctx.log();
			if(bd != null) bd.closeConnection();
		}
	}
}

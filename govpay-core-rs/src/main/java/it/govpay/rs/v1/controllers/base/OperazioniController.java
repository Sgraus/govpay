package it.govpay.rs.v1.controllers.base;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

import it.govpay.core.dao.operazioni.OperazioniDAO;
import it.govpay.core.dao.operazioni.dto.LeggiOperazioneDTO;
import it.govpay.core.dao.operazioni.dto.LeggiOperazioneDTOResponse;
import it.govpay.core.dao.operazioni.dto.ListaOperazioniDTO;
import it.govpay.core.dao.operazioni.dto.ListaOperazioniDTOResponse;
import it.govpay.core.rs.v1.beans.base.ListaOperazioni;
import it.govpay.core.utils.GovpayConfig;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.model.IAutorizzato;
import it.govpay.rs.v1.beans.converter.OperazioniConverter;

public class OperazioniController extends it.govpay.rs.BaseController {

    public OperazioniController(String nomeServizio,Logger log) {
		super(nomeServizio,log, GovpayConfig.GOVPAY_BACKOFFICE_OPEN_API_FILE_NAME);
    }
    
    public Response operazioniGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer pagina, Integer risultatiPerPagina, String ordinamento, String campi) {
    	String methodName = "operazioniGET";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 
		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
//			String principal = this.getPrincipal();
			
			// Parametri - > DTO Input
			
			ListaOperazioniDTO listaOperazioniDTO = new ListaOperazioniDTO(user);
			listaOperazioniDTO.setPagina(pagina);
			listaOperazioniDTO.setLimit(risultatiPerPagina);
			
			if(ordinamento != null)
				listaOperazioniDTO.setOrderBy(ordinamento);
			// INIT DAO
			
			OperazioniDAO operazioniDAO = new OperazioniDAO(); 
			
			// CHIAMATA AL DAO
			
			ListaOperazioniDTOResponse listaOperazioniDTOResponse = operazioniDAO.listaOperazioni(listaOperazioniDTO); 
			
			// CONVERT TO JSON DELLA RISPOSTA
			
			List<it.govpay.core.rs.v1.beans.base.OperazioneIndex> results = new ArrayList<>();
			for(LeggiOperazioneDTOResponse pagamentoPortale: listaOperazioniDTOResponse.getResults()) {
				results.add(OperazioniConverter.toRsModelIndex(pagamentoPortale));
			}
			
			ListaOperazioni response = new ListaOperazioni(results, this.getServicePath(uriInfo),
					listaOperazioniDTOResponse.getTotalResults(), pagina, risultatiPerPagina);
			
			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(campi), 200);
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(campi)),transactionId).build();
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }

    public Response operazioniIdGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , String id) {
    	String methodName = "operazioniIdGET";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 
		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			LeggiOperazioneDTO leggiOperazioneDTO = new LeggiOperazioneDTO(user);
			leggiOperazioneDTO.setIdOperazione(id);
			
			OperazioniDAO operazioniDAO = new OperazioniDAO(); 
			
			LeggiOperazioneDTOResponse leggiOperazioneDTOResponse = operazioniDAO.eseguiOperazione(leggiOperazioneDTO);
			
			it.govpay.core.rs.v1.beans.base.Operazione response = OperazioniConverter.toRsModel(leggiOperazioneDTOResponse);
			
			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(null), 200);
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(null)),transactionId).build();
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }

    public Response operazioniStatoIdGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , String id) {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity( "Not implemented" ).build();
    }

}

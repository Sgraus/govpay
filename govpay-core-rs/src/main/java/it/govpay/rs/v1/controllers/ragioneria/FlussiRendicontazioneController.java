package it.govpay.rs.v1.controllers.ragioneria;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

import it.govpay.core.dao.pagamenti.RendicontazioniDAO;
import it.govpay.core.dao.pagamenti.dto.LeggiRendicontazioneDTO;
import it.govpay.core.dao.pagamenti.dto.LeggiRendicontazioneDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaRendicontazioniDTO;
import it.govpay.core.dao.pagamenti.dto.ListaRendicontazioniDTOResponse;
import it.govpay.core.rs.v1.beans.ragioneria.FlussoRendicontazione;
import it.govpay.core.rs.v1.beans.ragioneria.FlussoRendicontazioneIndex;
import it.govpay.core.rs.v1.beans.ragioneria.ListaFlussiRendicontazione;
import it.govpay.core.utils.GovpayConfig;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.core.utils.SimpleDateFormatUtils;
import it.govpay.model.IAutorizzato;
import it.govpay.rs.BaseController;
import it.govpay.rs.v1.beans.ragioneria.converter.FlussiRendicontazioneConverter;



public class FlussiRendicontazioneController extends BaseController {

     public FlussiRendicontazioneController(String nomeServizio,Logger log) {
		super(nomeServizio,log, GovpayConfig.GOVPAY_BACKOFFICE_OPEN_API_FILE_NAME);
     }



    public Response flussiRendicontazioneIdFlussoGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , String idFlusso) {
    	String methodName = "flussiRendicontazioneIdFlussoGET";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 
		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			String accept = null;
			if(httpHeaders.getRequestHeaders().containsKey("Accept")) {
				accept = httpHeaders.getRequestHeaders().get("Accept").get(0).toLowerCase();
			}
			
			// Parametri - > DTO Input
			
			LeggiRendicontazioneDTO leggiRendicontazioneDTO = new LeggiRendicontazioneDTO(user, idFlusso);
			
			// INIT DAO
			
			RendicontazioniDAO rendicontazioniDAO = new RendicontazioniDAO();
			
			// CHIAMATA AL DAO
			
			LeggiRendicontazioneDTOResponse leggiRendicontazioneDTOResponse = rendicontazioniDAO.leggiRendicontazione(leggiRendicontazioneDTO);
					
			
			// CONVERT TO JSON DELLA RISPOSTA
			if(accept.toLowerCase().contains(MediaType.APPLICATION_XML)) {
				byte[] response = leggiRendicontazioneDTOResponse.getFr().getXml();
				this.logResponse(uriInfo, httpHeaders, methodName, response, 200);
				this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
				return this.handleResponseOk(Response.status(Status.OK).entity(new String(response)).type(MediaType.APPLICATION_XML),transactionId).build();
			} else {
				FlussoRendicontazione response = FlussiRendicontazioneConverter.toRsModel(leggiRendicontazioneDTOResponse.getFr()); 
				this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(null), 200);
				this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
				return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(null)).type(MediaType.APPLICATION_JSON),transactionId).build();
			}
			
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }



    public Response flussiRendicontazioneGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders , Integer pagina, Integer risultatiPerPagina, String idDominio, String ordinamento, String dataDa, String dataA) {
    	String methodName = "flussiRendicontazioneGET";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 
		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			// Parametri - > DTO Input
			
			ListaRendicontazioniDTO findRendicontazioniDTO = new ListaRendicontazioniDTO(user);
			findRendicontazioniDTO.setIdDominio(idDominio);
			findRendicontazioniDTO.setPagina(pagina);
			findRendicontazioniDTO.setLimit(risultatiPerPagina);
			findRendicontazioniDTO.setOrderBy(ordinamento);
			if(dataDa != null)
				findRendicontazioniDTO.setDataDa(SimpleDateFormatUtils.newSimpleDateFormatSoloData().parse(dataDa)); 
			if(dataA != null)
				findRendicontazioniDTO.setDataA(SimpleDateFormatUtils.newSimpleDateFormatSoloData().parse(dataA));
			
			// INIT DAO
			
			RendicontazioniDAO rendicontazioniDAO = new RendicontazioniDAO();
			
			// CHIAMATA AL DAO
			
			ListaRendicontazioniDTOResponse findRendicontazioniDTOResponse = rendicontazioniDAO.listaRendicontazioni(findRendicontazioniDTO);
			
			// CONVERT TO JSON DELLA RISPOSTA
			
			List<FlussoRendicontazioneIndex> collect = new ArrayList<>();
			
			for(LeggiRendicontazioneDTOResponse res: findRendicontazioniDTOResponse.getResults()) {
				collect.add(FlussiRendicontazioneConverter.toRsIndexModel(res.getFr()));
			}
			
			ListaFlussiRendicontazione response = new ListaFlussiRendicontazione(collect, 
					uriInfo.getRequestUri(), findRendicontazioniDTOResponse.getTotalResults(), pagina, risultatiPerPagina);
			
			this.logResponse(uriInfo, httpHeaders, methodName, response.toJSON(null), 200);
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(response.toJSON(null)),transactionId).build();
			
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }


}



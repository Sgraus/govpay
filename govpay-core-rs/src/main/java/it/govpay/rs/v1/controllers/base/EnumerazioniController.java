package it.govpay.rs.v1.controllers.base;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.openspcoop2.utils.UtilsException;
import org.openspcoop2.utils.json.JSONUtils;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.node.ArrayNode;

import it.govpay.core.rs.v1.beans.base.ServizioEnum;
import it.govpay.core.rs.v1.beans.base.VersioneApiEnum;
import it.govpay.core.utils.GovpayConfig;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.model.IAutorizzato;
import it.govpay.rs.BaseController;




public class EnumerazioniController extends BaseController {

     public EnumerazioniController(String nomeServizio,Logger log) {
		super(nomeServizio,log, GovpayConfig.GOVPAY_BACKOFFICE_OPEN_API_FILE_NAME);
     }



    public Response enumerazioniServiziACLGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders ) {
		String methodName = "enumerazioniServiziACLGET";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 
		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			List<String> results = new ArrayList<>();
			
			for(ServizioEnum serv: ServizioEnum.values()) {
				results.add(serv.toString());
			}

			this.logResponse(uriInfo, httpHeaders, methodName, this.toJsonArray(results), 200);
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(this.toJsonArray(results)),transactionId).build();
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }



    public Response enumerazioniVersioneConnettoreGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders ) {
		String methodName = "enumerazioniVersioneConnettoreGET";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 
		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);

			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();

			List<String> results = new ArrayList<>();
			
			for(VersioneApiEnum serv: VersioneApiEnum.values()) {
				results.add(serv.toString());
			}

			this.logResponse(uriInfo, httpHeaders, methodName, this.toJsonArray(results), 200);
			
			
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(this.toJsonArray(results)),transactionId).build();
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
    }



	/**
	 * @param results
	 * @return
	 * @throws UtilsException 
	 */
	private String toJsonArray(List<String> results) throws UtilsException {
		ArrayNode newArrayNode = JSONUtils.getInstance().newArrayNode();
		for(String str: results) {
			newArrayNode.add(str);
		}
		
		return JSONUtils.getInstance().toString(newArrayNode);
	}


}



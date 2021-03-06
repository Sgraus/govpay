package it.govpay.rs.v1.controllers.base;



import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;

import it.govpay.core.rs.v1.beans.base.InfoGovPay;
import it.govpay.core.utils.GovpayConfig;
import it.govpay.core.utils.GpContext;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.model.IAutorizzato; 



public class InfoController extends it.govpay.rs.BaseController {
	
	private String govpayBuildNumber = null;
	private String govpayVersione = null;
	
	public InfoController(String nomeServizio,Logger log) {
		super(nomeServizio,log, GovpayConfig.GOVPAY_BACKOFFICE_OPEN_API_FILE_NAME);
	}

	public Response infoGET(IAutorizzato user, UriInfo uriInfo, HttpHeaders httpHeaders ) {
		String methodName = "infoGET";  
		GpContext ctx = null;
		String transactionId = null;
		ByteArrayOutputStream baos= null;
		this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_IN_CORSO, methodName)); 
		try{
			baos = new ByteArrayOutputStream();
			this.logRequest(uriInfo, httpHeaders, methodName, baos);
			
			ctx =  GpThreadLocal.get();
			transactionId = ctx.getTransactionId();
			
			InfoGovPay info = new InfoGovPay();
			info.setAmbiente(GovpayConfig.getInstance().getAmbienteDeploy());
			info.setAppName(GovpayConfig.getInstance().getAppName());
			info.setBuild(this.govpayBuildNumber);
			info.setVersione(this.govpayVersione);
			
			this.logResponse(uriInfo, httpHeaders, methodName, info.toJSON(null), 200);
			this.log.info(MessageFormat.format(it.govpay.rs.BaseController.LOG_MSG_ESECUZIONE_METODO_COMPLETATA, methodName)); 
			return this.handleResponseOk(Response.status(Status.OK).entity(info.toJSON(null)),transactionId).build();
			
		}catch (Exception e) {
			return this.handleException(uriInfo, httpHeaders, methodName, e, transactionId);
		} finally {
			if(ctx != null) ctx.log();
		}
	}

	public String getGovpayBuildNumber() {
		return govpayBuildNumber;
	}

	public void setGovpayBuildNumber(String govpayBuildNumber) {
		this.govpayBuildNumber = govpayBuildNumber;
	}

	public String getGovpayVersione() {
		return govpayVersione;
	}

	public void setGovpayVersione(String govpayVersione) {
		this.govpayVersione = govpayVersione;
	}
	
}



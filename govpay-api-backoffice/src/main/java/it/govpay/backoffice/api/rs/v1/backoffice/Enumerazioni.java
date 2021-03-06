package it.govpay.backoffice.api.rs.v1.backoffice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.rs.v1.BaseRsServiceV1;
import it.govpay.rs.v1.controllers.base.EnumerazioniController;


@Path("/enumerazioni")

public class Enumerazioni extends BaseRsServiceV1{


	private EnumerazioniController controller = null;

	public Enumerazioni() throws ServiceException {
		super("enumerazioni");
		this.controller = new EnumerazioniController(this.nomeServizio,this.log);
	}



    @GET
    @Path("/serviziACL")
    
    @Produces({ "application/json" })
    public Response enumerazioniServiziACLGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.enumerazioniServiziACLGET(this.getUser(), uriInfo, httpHeaders);
    }

    @GET
    @Path("/versioneConnettore")
    
    @Produces({ "application/json" })
    public Response enumerazioniVersioneConnettoreGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.enumerazioniVersioneConnettoreGET(this.getUser(), uriInfo, httpHeaders);
    }

}



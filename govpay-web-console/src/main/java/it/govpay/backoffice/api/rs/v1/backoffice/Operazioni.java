package it.govpay.backoffice.api.rs.v1.backoffice;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.core.rs.v1.costanti.Costanti;
import it.govpay.rs.v1.BaseRsServiceV1;
import it.govpay.rs.v1.controllers.base.OperazioniController;


@Path("/operazioni")

public class Operazioni extends BaseRsServiceV1{


	private OperazioniController controller = null;

	public Operazioni() throws ServiceException {
		super("operazioni");
		this.controller = new OperazioniController(this.nomeServizio,this.log);
	}



    @GET
    @Path("/")
    
    @Produces({ "application/json" })
    public Response operazioniGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @QueryParam(value=Costanti.PARAMETRO_PAGINA) @DefaultValue(value="1") Integer pagina, @QueryParam(value=Costanti.PARAMETRO_RISULTATI_PER_PAGINA) @DefaultValue(value="25") Integer risultatiPerPagina){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.operazioniGET(this.getUser(), uriInfo, httpHeaders, pagina, risultatiPerPagina, null, null);
    }

    @GET
    @Path("/stato/{id}")
    
    @Produces({ "application/json" })
    public Response operazioniStatoIdGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("id") String id){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.operazioniStatoIdGET(this.getUser(), uriInfo, httpHeaders,  id);
    }

    @GET
    @Path("/{idOperazione}")
    
    @Produces({ "application/json" })
    public Response operazioniIdGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("idOperazione") String idOperazione){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.operazioniIdGET(this.getUser(), uriInfo, httpHeaders,  idOperazione);
    }

}



package it.govpay.backoffice.api.rs.v1.backoffice;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import it.govpay.rs.v1.BaseRsServiceV1;
import it.govpay.rs.v1.controllers.base.AclController;


@Path("/acl")

public class Acl extends BaseRsServiceV1{


	private AclController controller = null;

	public Acl() {
		super("acl");
		this.controller = new AclController(this.nomeServizio,this.log);
	}




    @GET
    @Path("/")
    
    @Produces({ "application/json" })
    public Response aclGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @QueryParam("pagina") Integer pagina, @QueryParam("risultatiPerPagina") Integer risultatiPerPagina, @QueryParam("abilitato") Boolean abilitato, @QueryParam("ruolo") Boolean ruolo, @QueryParam("principal") Boolean principal, @QueryParam("servizio") Boolean servizio){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.aclGET(this.getUser(), uriInfo, httpHeaders, pagina, risultatiPerPagina, abilitato, ruolo, principal, servizio);
    }



    @DELETE
    @Path("/{id}")
    
    @Produces({ "application/json" })
    public Response aclIdDELETE(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("id") String id){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.aclIdDELETE(this.getUser(), uriInfo, httpHeaders,  id);
    }



    @GET
    @Path("/{id}")
    
    @Produces({ "application/json" })
    public Response aclIdGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("id") String id){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.aclIdGET(this.getUser(), uriInfo, httpHeaders,  id);
    }



    @POST
    @Path("/")
    @Consumes({ "application/json" })
    
    public Response aclPOST(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("id") String id, java.io.InputStream is){
        this.controller.setRequestResponse(this.request, this.response);
        return this.controller.aclPOST(this.getUser(), uriInfo, httpHeaders,  id, is);
    }

    
}


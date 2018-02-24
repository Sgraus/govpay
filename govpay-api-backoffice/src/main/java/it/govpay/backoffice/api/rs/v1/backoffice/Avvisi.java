package it.govpay.backoffice.api.rs.v1.backoffice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import it.govpay.rs.v1.BaseRsServiceV1;
import it.govpay.rs.v1.controllers.base.AvvisiController;


@Path("/avvisi")

public class Avvisi extends BaseRsServiceV1{


	private AvvisiController controller = null;

	public Avvisi() {
		super("avvisi");
		this.controller = new AvvisiController(this.nomeServizio,this.log);
	}




    @GET
    @Path("/{idDominio}/{iuv}")
    
    @Produces({ "application/json", "application/pdf" })
    public Response avvisiIdDominioIuvGET(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("idDominio") String idDominio, @PathParam("iuv") String iuv){
        this.controller.setRequestResponse(this.request, this.response);
	return this.controller.avvisiIdDominioIuvGET(this.getPrincipal(), this.getListaRuoli(), uriInfo, httpHeaders,  idDominio,  iuv);
    }


/*
    @GET
    @Path("/{idDominio}/{iuv}")
    
    @Produces({ "application/json", "application/pdf" })
    public Response avvisiIdDominioIuvGET_1(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("idDominio") String idDominio, @PathParam("iuv") String iuv){
        this.controller.setRequestResponse(this.request, this.response);
	return this.controller.avvisiIdDominioIuvGET_1(this.getPrincipal(), this.getListaRuoli(), uriInfo, httpHeaders,  idDominio,  iuv);
    }
*/


    @POST
    @Path("/{idDominio}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response avvisiIdDominioPOST(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("idDominio") String idDominio, java.io.InputStream is){
        this.controller.setRequestResponse(this.request, this.response);
	return this.controller.avvisiIdDominioPOST(this.getPrincipal(), this.getListaRuoli(), uriInfo, httpHeaders,  idDominio, is);
    }


/*
    @POST
    @Path("/{idDominio}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    public Response avvisiIdDominioPOST_2(@Context UriInfo uriInfo, @Context HttpHeaders httpHeaders, @PathParam("idDominio") String idDominio, java.io.InputStream is){
        this.controller.setRequestResponse(this.request, this.response);
	return this.controller.avvisiIdDominioPOST_2(this.getPrincipal(), this.getListaRuoli(), uriInfo, httpHeaders,  idDominio, is);
    }
*/

}
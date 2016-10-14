/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2016 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
package it.govpay.web.rs;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.pagamento.RptBD;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.model.Rpt;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/pub")
public class GestioneRedirectGw {

	@GET
	@Path("/backUrl")
	public Response backUrl(
			@QueryParam(value = "idDominio") String codDominio, 
			@QueryParam(value = "idSession") String codSessione,
			@QueryParam(value = "esito") @DefaultValue("ERROR") String esito) {
		BasicBD bd = null;
		Rpt rpt = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			RptBD rptBD = new RptBD(bd);
			rpt = rptBD.getRptByCodSessione(codDominio, codSessione);

			if(rpt.getCallbackURL() != null) {
				UriBuilder ub = UriBuilder.fromUri(rpt.getCallbackURL());
				if(codDominio != null) ub.queryParam("idDominio", codDominio);
				ub.queryParam("idSession", codSessione);
				ub.queryParam("esito", esito);
				return Response.seeOther(ub.build()).build();
			} else {
				UriBuilder ub = UriBuilder.fromUri(AnagraficaManager.getPortale(bd, rpt.getIdPortale()).getDefaultCallbackURL());
				if(codDominio != null) ub.queryParam("idDominio", codDominio);
				ub.queryParam("idSession", codSessione);
				ub.queryParam("esito", esito);
				return Response.seeOther(ub.build()).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.NOT_FOUND).build();
		} finally {
			if(bd!= null) bd.closeConnection();
		}
	}
}


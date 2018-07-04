package it.govpay.core.rs.v1.beans.pendenze;

import java.net.URI;
import java.util.List;

import it.govpay.core.rs.v1.beans.Lista;

public class ListaPendenze extends Lista<PendenzaIndex> {
	
	public ListaPendenze(List<PendenzaIndex> pagamentiPortale, URI requestUri, long count, long pagina, long limit) {
		super(pagamentiPortale, requestUri, count, pagina, limit);
	}
	
}

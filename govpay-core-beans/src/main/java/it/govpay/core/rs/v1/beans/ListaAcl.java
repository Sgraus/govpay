package it.govpay.core.rs.v1.beans;

import java.net.URI;
import java.util.List;


public class ListaAcl extends Lista<ACL> {
	
	public ListaAcl() {
	}
	public ListaAcl(List<ACL> acl, URI requestUri, long count, long pagina, long limit) {
		super(acl, requestUri, count, pagina, limit);
	}
	
}
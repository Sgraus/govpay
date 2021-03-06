package it.govpay.core.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openspcoop2.generic_project.exception.ServiceException;
import org.slf4j.Logger;

import it.govpay.core.dao.anagrafica.AclDAO;
import it.govpay.core.dao.anagrafica.dto.ListaAclDTOResponse;
import it.govpay.model.Acl;

public class AclCache {

	private Logger log = null;
	private static AclCache instance = null;
	
	private Map<String, List<Acl>> mapAclsRuoli = null;
	
	private Map<String, List<Acl>> mapAclsPrincipal = null;
	
	public AclCache(Logger log) {
		this.log = log;
		this.mapAclsRuoli = new HashMap<>();
		this.mapAclsPrincipal = new HashMap<>();

		
		this.inizializzaCacheAclsRuoli(); 
		
		this.inizializzaCacheAclsPrincipal(); 
	}

	public void inizializzaCacheAclsRuoli() {
		AclDAO aclDAO = new AclDAO();
		
		try {
			this.log.info("Caricamento dei Ruoli registrati nel sistema in corso... "); 
			ListaAclDTOResponse leggiAclRuoloRegistrateSistema = aclDAO.leggiAclRuoloRegistrateSistema();
			
			this.log.info("Caricamento dei Ruoli registrati nel sistema trovate ["+leggiAclRuoloRegistrateSistema.getTotalResults()+"] Acl.");
			List<Acl> results = leggiAclRuoloRegistrateSistema.getResults();
			if (results != null && results.size() >0) {
				for (Acl acl : results) {
					List<Acl> listaAclI = null;
					
					String ruolo = acl.getRuolo();
					if(this.mapAclsRuoli.containsKey(ruolo)) {
						listaAclI = this.mapAclsRuoli.remove(ruolo);
					} else {
						listaAclI = new ArrayList<>();
					}
				
					listaAclI.add(acl);
					this.mapAclsRuoli.put(ruolo,listaAclI);
				}
			}
			
			this.log.info("Caricamento dei Ruoli registrati nel sistema completato"); 
		} catch (ServiceException e) {
			this.log.error("Caricamento dei Ruoli registrati nel sistema completato con errori: " + e.getMessage(),e); 
		}
	}
	
	public void inizializzaCacheAclsPrincipal() {
		AclDAO aclDAO = new AclDAO();
		
		try {
			this.log.info("Caricamento dei Principal registrati nel sistema in corso... "); 
			ListaAclDTOResponse leggiAclRuoloRegistrateSistema = aclDAO.leggiAclPrincipalRegistrateSistema();
			
			this.log.info("Caricamento dei Principal registrati nel sistema trovate ["+leggiAclRuoloRegistrateSistema.getTotalResults()+"] Acl.");
			List<Acl> results = leggiAclRuoloRegistrateSistema.getResults();
			if (results != null && results.size() >0) {
				for (Acl acl : results) {
					List<Acl> listaAclI = null;
					
					String ruolo = acl.getRuolo();
					if(this.mapAclsPrincipal.containsKey(ruolo)) {
						listaAclI = this.mapAclsPrincipal.remove(ruolo);
					} else {
						listaAclI = new ArrayList<>();
					}
				
					listaAclI.add(acl);
					this.mapAclsPrincipal.put(ruolo,listaAclI);
				}
			}
			
			this.log.info("Caricamento dei Principal registrati nel sistema completato"); 
		} catch (ServiceException e) {
			this.log.error("Caricamento dei Principal registrati nel sistema completato con errori: " + e.getMessage(),e); 
		}
	}
	
	public static synchronized void newInstance(Logger log) {
		if(AclCache.instance == null)
			AclCache.instance = new AclCache(log);
	}
	
	public static AclCache getInstance() {
		return AclCache.instance;
	}
	
	public List<String> getListaRuoli(){
		return new ArrayList<>();
	}
	
	public Set<String> getChiaviRuoli(){
		return this.mapAclsRuoli.keySet();
	}
	
	public String getRuolo(String key){
		if(this.mapAclsRuoli.containsKey(key)) {
			return key;
		}
		
		return null;
	}
	
	public List<Acl> getAclsRuolo(String key){
		return this.mapAclsRuoli.get(key);
	}
	
	public void reloadRuolo(String ruolo) {
		AclDAO aclDAO = new AclDAO();
		
		try {
			this.log.info("Aggiornamento della entry in cache per il Ruolo ["+ruolo+"] in corso... "); 
			ListaAclDTOResponse leggiAclRuoloRegistrateSistema = aclDAO.leggiAclRuoloRegistratoSistema(ruolo);
			
			// reset della entry
			this.mapAclsRuoli.remove(ruolo);
			this.mapAclsRuoli.put(ruolo,new ArrayList<>());
			
			this.log.info("Aggiornamento della entry in cache per il Ruolo ["+ruolo+"]  trovate ["+leggiAclRuoloRegistrateSistema.getTotalResults()+"] Acl.");
			List<Acl> results = leggiAclRuoloRegistrateSistema.getResults();
			if (results != null && results.size() >0) {
				for (Acl acl : results) {
					List<Acl> listaAclI = null;
					
					String ruoloTmp = acl.getRuolo();
					if(this.mapAclsRuoli.containsKey(ruoloTmp)) {
						listaAclI = this.mapAclsRuoli.remove(ruoloTmp);
					} else {
						listaAclI = new ArrayList<>();
					}
				
					listaAclI.add(acl);
					this.mapAclsRuoli.put(ruoloTmp,listaAclI);
				}
			}
			
			this.log.info("Aggiornamento della entry in cache per il Ruolo ["+ruolo+"] completato"); 
		} catch (ServiceException e) {
			this.log.error("Aggiornamento della entry in cache per il Ruolo ["+ruolo+"] completato con errori: " + e.getMessage(),e); 
		}
	}
}

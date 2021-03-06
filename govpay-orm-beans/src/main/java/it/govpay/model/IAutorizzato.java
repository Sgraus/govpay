package it.govpay.model;

import java.util.List;

import org.openspcoop2.generic_project.exception.ServiceException;

public interface IAutorizzato {
	
	public void setCheckSubject(boolean checkSubject);
	public boolean isCheckSubject();
	
	public void setPrincipal(String principal);
	public String getPrincipal();
	
	public void setPrincipalOriginale(String subject);
	public String getPrincipalOriginale();
	
	public void setRuoli(List<String> ruoli);
	public List<String> getRuoli();
	
	public List<String> getIdDominio();
//	public void setIdDominio(List<String> idDominio) ;
	public List<String> getIdTributo();
//	public void setIdTributo(List<String> idTributo);
	
	public List<Acl> getAcls(); 
//	public void setAcls(List<Acl> acls);

	public void merge(IAutorizzato src) throws ServiceException; 
}

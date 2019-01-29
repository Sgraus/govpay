package it.govpay.backoffice.v1.beans.converter;

import java.util.ArrayList;
import java.util.List;

import org.openspcoop2.generic_project.exception.ServiceException;
import org.springframework.security.core.Authentication;

import it.govpay.backoffice.v1.beans.AclPost;
import it.govpay.backoffice.v1.beans.Applicazione;
import it.govpay.backoffice.v1.beans.ApplicazionePost;
import it.govpay.backoffice.v1.beans.CodificaAvvisi;
import it.govpay.backoffice.v1.beans.DominioIndex;
import it.govpay.backoffice.v1.beans.TipoEntrata;
import it.govpay.backoffice.v1.controllers.ApplicazioniController;
import it.govpay.bd.model.Dominio;
import it.govpay.bd.model.Tributo;
import it.govpay.bd.model.UtenzaApplicazione;
import it.govpay.core.dao.anagrafica.dto.PutApplicazioneDTO;
import it.govpay.core.exceptions.NotAuthorizedException;
import it.govpay.model.Acl;
import it.govpay.model.Rpt.FirmaRichiesta;

public class ApplicazioniConverter {
	
	public static PutApplicazioneDTO getPutApplicazioneDTO(ApplicazionePost applicazionePost, String idA2A, Authentication user) throws ServiceException,NotAuthorizedException {
		PutApplicazioneDTO applicazioneDTO = new PutApplicazioneDTO(user);
		it.govpay.bd.model.Applicazione applicazione = new it.govpay.bd.model.Applicazione();
		it.govpay.bd.model.Utenza utenza = new it.govpay.bd.model.Utenza();
		utenza.setAbilitato(applicazionePost.isAbilitato());
		utenza.setPrincipal(applicazionePost.getPrincipal());
		utenza.setPrincipalOriginale(applicazionePost.getPrincipal()); 
		applicazione.setUtenza(new UtenzaApplicazione(utenza, idA2A));
		applicazioneDTO.setIdUtenza(applicazionePost.getPrincipal());
		
		boolean appTrusted = false;
		boolean appAuthEntrateAll = false;
		boolean appAuthDominiAll = false;
		
		if(applicazionePost.getEntrate() != null) {
			List<String> idTributi = new ArrayList<>();
						
			for (String id : applicazionePost.getEntrate()) {
				if(id.equals(ApplicazioniController.AUTODETERMINAZIONE_TRIBUTI_VALUE)) {
					appTrusted = true;
				} else if(id.equals(ApplicazioniController.AUTORIZZA_TRIBUTI_STAR)) {
					appAuthEntrateAll = true;
				} else{
					idTributi.add(id);
				}
			}
			
			if(appAuthEntrateAll || appTrusted)
				applicazioneDTO.setIdTributi(new ArrayList<>());				
			else
				applicazioneDTO.setIdTributi(idTributi);
		}
		
		applicazione.setTrusted(appTrusted);
		applicazione.getUtenza().setAutorizzazioneTributiStar(appAuthEntrateAll);
		
		if(applicazionePost.getDomini() != null) {
			List<String> idDomini = new ArrayList<>();
			
			for (String id : applicazionePost.getDomini()) {
				if(id.equals(ApplicazioniController.AUTORIZZA_DOMINI_STAR)) {
					appAuthDominiAll = true;
					idDomini.clear();
					break;
				}
				idDomini.add(id);
			}
			
			applicazioneDTO.setIdDomini(idDomini);
		}
		applicazione.getUtenza().setAutorizzazioneDominiStar(appAuthDominiAll);
		
		CodificaAvvisi codificaAvvisi = new CodificaAvvisi();
		codificaAvvisi.setCodificaIuv(applicazione.getCodApplicazioneIuv());
		codificaAvvisi.setRegExpIuv(applicazione.getRegExp());
		codificaAvvisi.setGenerazioneIuvInterna(applicazione.isAutoIuv());
		
		applicazione.setCodApplicazioneIuv(applicazionePost.getCodificaAvvisi().getCodificaIuv());
		applicazione.setRegExp(applicazionePost.getCodificaAvvisi().getRegExpIuv());
		applicazione.setAutoIuv(applicazionePost.getCodificaAvvisi().isGenerazioneIuvInterna());
		applicazione.setCodApplicazione(idA2A);
		applicazione.setFirmaRichiesta(FirmaRichiesta.NESSUNA);
		
		if(applicazionePost.getServizioNotifica() != null)
			applicazione.setConnettoreNotifica(ConnettoriConverter.getConnettore(applicazionePost.getServizioNotifica()));
		if(applicazionePost.getServizioVerifica() != null)
			applicazione.setConnettoreVerifica(ConnettoriConverter.getConnettore(applicazionePost.getServizioVerifica()));
		
		applicazioneDTO.setApplicazione(applicazione);
		applicazioneDTO.setIdApplicazione(idA2A);
		
		if(applicazionePost.getAcl()!=null) {
			List<Acl> aclPrincipal = new ArrayList<Acl>();
			for(AclPost aclPost: applicazionePost.getAcl()) {
				Acl acl = AclConverter.getAcl(aclPost, user);
				acl.setPrincipal(applicazionePost.getPrincipal());
				aclPrincipal.add(acl);
			}
			applicazione.getUtenza().setAclPrincipal(aclPrincipal);
		}
		
		return applicazioneDTO;		
	}

	public static Applicazione toRsModel(it.govpay.bd.model.Applicazione applicazione) throws ServiceException {
		Applicazione rsModel = new Applicazione();
		rsModel.setAbilitato(applicazione.getUtenza().isAbilitato());
		
		CodificaAvvisi codificaAvvisi = new CodificaAvvisi();
		codificaAvvisi.setCodificaIuv(applicazione.getCodApplicazioneIuv());
		codificaAvvisi.setRegExpIuv(applicazione.getRegExp());
		codificaAvvisi.setGenerazioneIuvInterna(applicazione.isAutoIuv());
		rsModel.setCodificaAvvisi(codificaAvvisi);
		
		rsModel.setIdA2A(applicazione.getCodApplicazione());
		rsModel.setPrincipal(applicazione.getUtenza().getPrincipalOriginale());
		
		if(applicazione.getConnettoreNotifica()!=null)
			rsModel.setServizioNotifica(ConnettoriConverter.toRsModel(applicazione.getConnettoreNotifica()));
		
		if(applicazione.getConnettoreVerifica()!=null)
			rsModel.setServizioVerifica(ConnettoriConverter.toRsModel(applicazione.getConnettoreVerifica()));
		
		
		List<DominioIndex> idDomini = new ArrayList<>();
		if(applicazione.getUtenza().isAutorizzazioneDominiStar()) {
			DominioIndex tuttiDomini = new DominioIndex();
			tuttiDomini.setIdDominio(ApplicazioniController.AUTORIZZA_DOMINI_STAR);
			tuttiDomini.setRagioneSociale(ApplicazioniController.AUTORIZZA_DOMINI_STAR_LABEL);
			idDomini.add(tuttiDomini);
		} else if(applicazione.getUtenza().getDomini(null) != null) {
			for (Dominio dominio : applicazione.getUtenza().getDomini(null)) {
				idDomini.add(DominiConverter.toRsModelIndex(dominio));
			}
		}
		
		rsModel.setDomini(idDomini);

		List<TipoEntrata> idTributi = new ArrayList<>();
		List<Tributo> tributi = applicazione.getUtenza().getTributi(null);
		if(tributi == null)
			tributi = new ArrayList<>();
		
		if(applicazione.isTrusted()) {
			TipoEntrata tEI = new TipoEntrata();
			tEI.setIdEntrata(ApplicazioniController.AUTODETERMINAZIONE_TRIBUTI_VALUE);
			tEI.setDescrizione(ApplicazioniController.AUTODETERMINAZIONE_TRIBUTI_LABEL);
			idTributi.add(tEI);
		}
		
		if(applicazione.getUtenza().isAutorizzazioneTributiStar()) {
			TipoEntrata tEI = new TipoEntrata();
			tEI.setIdEntrata(ApplicazioniController.AUTORIZZA_TRIBUTI_STAR);
			tEI.setDescrizione(ApplicazioniController.AUTORIZZA_TRIBUTI_STAR_LABEL);
			idTributi.add(tEI);
		} 
		
		if(!applicazione.isTrusted() && !applicazione.getUtenza().isAutorizzazioneTributiStar()) {
			for (Tributo tributo : tributi) {
				TipoEntrata tEI = new TipoEntrata();
				tEI.setIdEntrata(tributo.getCodTributo());
				tEI.setDescrizione(tributo.getDescrizione());
				idTributi.add(tEI);
			}
		}
		
		if(applicazione.getUtenza().getAcls()!=null) {
			List<AclPost> aclList = new ArrayList<>();
			
			for(Acl acl: applicazione.getUtenza().getAcls()) {
				aclList.add(AclConverter.toRsModel(acl));
			}
			
			rsModel.setAcl(aclList);
		}
		
		rsModel.setEntrate(idTributi);
		
		return rsModel;
	}
}

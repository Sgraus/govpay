package it.govpay.rs.v1.beans.pagamenti.converter;

import java.util.ArrayList;
import java.util.List;

import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.bd.model.Tributo;
import it.govpay.core.dao.anagrafica.dto.PutDominioDTO;
import it.govpay.core.dao.anagrafica.dto.PutIbanAccreditoDTO;
import it.govpay.core.dao.anagrafica.dto.PutUnitaOperativaDTO;
import it.govpay.core.rs.v1.beans.pagamenti.ContiAccredito;
import it.govpay.core.rs.v1.beans.pagamenti.ContiAccreditoPost;
import it.govpay.core.rs.v1.beans.pagamenti.Dominio;
import it.govpay.core.rs.v1.beans.pagamenti.DominioIndex;
import it.govpay.core.rs.v1.beans.pagamenti.DominioPost;
import it.govpay.core.rs.v1.beans.pagamenti.Entrata;
import it.govpay.core.rs.v1.beans.pagamenti.TipoContabilita;
import it.govpay.core.rs.v1.beans.pagamenti.UnitaOperativa;
import it.govpay.core.rs.v1.beans.pagamenti.UnitaOperativaPost;
import it.govpay.core.utils.UriBuilderUtils;
import it.govpay.model.Anagrafica;
import it.govpay.model.IAutorizzato;

public class DominiConverter {
	
	public static PutIbanAccreditoDTO getPutIbanAccreditoDTO(ContiAccreditoPost ibanAccreditoPost, String idDominio, String idIbanAccredito, IAutorizzato user) {
		PutIbanAccreditoDTO ibanAccreditoDTO = new PutIbanAccreditoDTO(user);
		
		it.govpay.bd.model.IbanAccredito iban = new it.govpay.bd.model.IbanAccredito();
		
		iban.setAbilitato(ibanAccreditoPost.isAbilitato());
		iban.setAttivatoObep(ibanAccreditoPost.isMybank());
		iban.setCodBic(ibanAccreditoPost.getBic());
		iban.setCodIban(idIbanAccredito);
		iban.setPostale(ibanAccreditoPost.isPostale());
		
		ibanAccreditoDTO.setIban(iban);
		ibanAccreditoDTO.setIdDominio(idDominio);
		ibanAccreditoDTO.setIbanAccredito(idIbanAccredito);
				
		return ibanAccreditoDTO;		
	}
	
	public static PutUnitaOperativaDTO getPutUnitaOperativaDTO(UnitaOperativaPost uoPost, String idDominio, String idUo, IAutorizzato user) {
		PutUnitaOperativaDTO uoDTO = new PutUnitaOperativaDTO(user);
		
		it.govpay.bd.model.UnitaOperativa uo = new it.govpay.bd.model.UnitaOperativa();
		uo.setAbilitato(uoPost.isAbilitato());
		Anagrafica anagrafica = new Anagrafica();
		anagrafica.setCap(uoPost.getCap());
		anagrafica.setCivico(uoPost.getCivico());
		anagrafica.setCodUnivoco(idUo);
		anagrafica.setEmail(uoPost.getEmail());
		anagrafica.setPec(uoPost.getPec());
		anagrafica.setFax(uoPost.getFax());
		anagrafica.setIndirizzo(uoPost.getIndirizzo());
		anagrafica.setLocalita(uoPost.getLocalita());
		anagrafica.setNazione(uoPost.getNazione());
		anagrafica.setProvincia(uoPost.getProvincia());
		anagrafica.setRagioneSociale(uoPost.getRagioneSociale());
		anagrafica.setTelefono(uoPost.getTel());
		anagrafica.setUrlSitoWeb(uoPost.getWeb());
		anagrafica.setArea(uoPost.getArea());
		
		uo.setAnagrafica(anagrafica);
		uo.setCodUo(idUo);
		
		uoDTO.setUo(uo );
		uoDTO.setIdDominio(idDominio);
		uoDTO.setIdUo(idUo);
				
		return uoDTO;		
	}

	public static PutDominioDTO getPutDominioDTO(DominioPost dominioPost, String idDominio, IAutorizzato user) {
		PutDominioDTO dominioDTO = new PutDominioDTO(user);
		
		it.govpay.bd.model.Dominio dominio = new it.govpay.bd.model.Dominio();
		dominio.setAbilitato(dominioPost.isAbilitato());
		Anagrafica anagrafica = new Anagrafica();
		anagrafica.setCap(dominioPost.getCap());
		anagrafica.setCivico(dominioPost.getCivico());
		anagrafica.setCodUnivoco(idDominio);
		anagrafica.setEmail(dominioPost.getEmail());
		anagrafica.setPec(dominioPost.getPec());
		anagrafica.setFax(dominioPost.getFax());
		anagrafica.setIndirizzo(dominioPost.getIndirizzo());
		anagrafica.setLocalita(dominioPost.getLocalita());
		anagrafica.setNazione(dominioPost.getNazione());
		anagrafica.setProvincia(dominioPost.getProvincia());
		anagrafica.setRagioneSociale(dominioPost.getRagioneSociale());
		anagrafica.setTelefono(dominioPost.getTel());
		anagrafica.setUrlSitoWeb(dominioPost.getWeb());
		
		dominio.setAnagrafica(anagrafica );
		if(dominioPost.getAuxDigit() != null)
			dominio.setAuxDigit(Integer.parseInt(dominioPost.getAuxDigit()));
		dominio.setCbill(dominioPost.getCbill());
		dominio.setCodDominio(idDominio);
		dominio.setGln(dominioPost.getGln());
		dominio.setIdApplicazioneDefault(null);
		
		dominio.setIuvPrefix(dominioPost.getIuvPrefix());
		if(dominioPost.getLogo() != null)
			dominio.setLogo(dominioPost.getLogo().getBytes());
		dominio.setNdpData(null);
		dominio.setNdpDescrizione(null);
		dominio.setNdpOperazione(null);
		dominio.setNdpStato(null);
		dominio.setRagioneSociale(dominioPost.getRagioneSociale());
		if(dominioPost.getSegregationCode() != null)
			dominio.setSegregationCode(Integer.parseInt(dominioPost.getSegregationCode()));
		
		
		dominioDTO.setDominio(dominio);
		dominioDTO.setIdDominio(idDominio);
		dominioDTO.setCodStazione(dominioPost.getStazione());
		return dominioDTO;		
	}
	
	
	
	public static DominioIndex toRsModelIndex(it.govpay.bd.model.Dominio dominio) throws ServiceException {
		DominioIndex rsModel = new DominioIndex();
		rsModel.setWeb(dominio.getAnagrafica().getUrlSitoWeb());
		rsModel.setIdDominio(dominio.getCodDominio()); 
		rsModel.setRagioneSociale(dominio.getRagioneSociale());
		rsModel.setIndirizzo(dominio.getAnagrafica().getIndirizzo());
		rsModel.setCivico(dominio.getAnagrafica().getCivico());
		rsModel.setCap(dominio.getAnagrafica().getCap());
		rsModel.setLocalita(dominio.getAnagrafica().getLocalita());
		rsModel.setProvincia(dominio.getAnagrafica().getProvincia());
		rsModel.setNazione(dominio.getAnagrafica().getNazione());
		rsModel.setEmail(dominio.getAnagrafica().getEmail());
		rsModel.setPec(dominio.getAnagrafica().getPec());
		rsModel.setTel(dominio.getAnagrafica().getTelefono());
		rsModel.setFax(dominio.getAnagrafica().getFax());
		rsModel.setGln(dominio.getGln());
		rsModel.setAuxDigit("" + dominio.getAuxDigit());
		if(dominio.getSegregationCode() != null)
			rsModel.setSegregationCode("" + dominio.getSegregationCode());
		if(dominio.getLogo() != null) {
			rsModel.setLogo(UriBuilderUtils.getLogoDominio(dominio.getCodDominio()));
		}
		rsModel.setIuvPrefix(dominio.getIuvPrefix());
		rsModel.setStazione(dominio.getStazione().getCodStazione());
		rsModel.setContiAccredito(UriBuilderUtils.getContiAccreditoByDominio(dominio.getCodDominio()));
		rsModel.setUnitaOperative(UriBuilderUtils.getListUoByDominio(dominio.getCodDominio()));
		rsModel.setEntrate(UriBuilderUtils.getEntrateByDominio(dominio.getCodDominio()));
		rsModel.setAbilitato(dominio.isAbilitato());
		
		return rsModel;
	}
	
	public static Dominio toRsModel(it.govpay.bd.model.Dominio dominio, List<it.govpay.bd.model.UnitaOperativa> uoLst, List<it.govpay.bd.model.Tributo> tributoLst, List<it.govpay.bd.model.IbanAccredito> ibanAccreditoLst) throws ServiceException {
		Dominio rsModel = new Dominio();
		rsModel.setWeb(dominio.getAnagrafica().getUrlSitoWeb());
		rsModel.setIdDominio(dominio.getCodDominio()); 
		rsModel.setRagioneSociale(dominio.getRagioneSociale());
		rsModel.setIndirizzo(dominio.getAnagrafica().getIndirizzo());
		rsModel.setCivico(dominio.getAnagrafica().getCivico());
		rsModel.setCap(dominio.getAnagrafica().getCap());
		rsModel.setLocalita(dominio.getAnagrafica().getLocalita());
		rsModel.setProvincia(dominio.getAnagrafica().getProvincia());
		rsModel.setNazione(dominio.getAnagrafica().getNazione());
		rsModel.setEmail(dominio.getAnagrafica().getEmail());
		rsModel.setPec(dominio.getAnagrafica().getPec());
		rsModel.setTel(dominio.getAnagrafica().getTelefono());
		rsModel.setFax(dominio.getAnagrafica().getFax());
		rsModel.setGln(dominio.getGln());
		rsModel.setAuxDigit("" + dominio.getAuxDigit());
		if(dominio.getSegregationCode() != null)
			rsModel.setSegregationCode("" + dominio.getSegregationCode());
		if(dominio.getLogo() != null) {
			rsModel.setLogo(UriBuilderUtils.getLogoDominio(dominio.getCodDominio()));
		}
		rsModel.setIuvPrefix(dominio.getIuvPrefix());
		rsModel.setStazione(dominio.getStazione().getCodStazione());
		
		if(uoLst != null) {
			List<UnitaOperativa> unitaOperative = new ArrayList<>();
			
			for(it.govpay.bd.model.UnitaOperativa uo: uoLst) {
				unitaOperative.add(toUnitaOperativaRsModel(uo));
			}
			rsModel.setUnitaOperative(unitaOperative);
		}

		if(ibanAccreditoLst != null) {
			List<ContiAccredito> contiAccredito = new ArrayList<>();
			
			for(it.govpay.bd.model.IbanAccredito iban: ibanAccreditoLst) {
				contiAccredito.add(toIbanRsModel(iban));
			}
			rsModel.setContiAccredito(contiAccredito);
		}

		if(tributoLst != null) {
			List<Entrata> entrate = new ArrayList<>();
			
			for(Tributo tributo: tributoLst) {
				entrate.add(toEntrataRsModel(tributo, tributo.getIbanAccredito()));
			}
			rsModel.setEntrate(entrate);
		}
		rsModel.setAbilitato(dominio.isAbilitato());
		
		return rsModel;
	}
	
	public static ContiAccredito toIbanRsModel(it.govpay.bd.model.IbanAccredito iban) throws ServiceException {
		ContiAccredito rsModel = new ContiAccredito();
		rsModel.abilitato(iban.isAbilitato())
		.bic(iban.getCodBic())
		.iban(iban.getCodIban())
		.mybank(iban.isAttivatoObep())
		.postale(iban.isPostale());
		
		return rsModel;
	}
	
	
	public static UnitaOperativa toUnitaOperativaRsModel(it.govpay.bd.model.UnitaOperativa uo) throws IllegalArgumentException, ServiceException {
		UnitaOperativa rsModel = new UnitaOperativa();
		
		rsModel.setCap(uo.getAnagrafica().getRagioneSociale());
		rsModel.setCivico(uo.getAnagrafica().getCivico());
		rsModel.setIdUnita(uo.getAnagrafica().getCodUnivoco());
		rsModel.setIndirizzo(uo.getAnagrafica().getIndirizzo());
		rsModel.setLocalita(uo.getAnagrafica().getLocalita());
		rsModel.setRagioneSociale(uo.getAnagrafica().getRagioneSociale());
		rsModel.setWeb(uo.getAnagrafica().getUrlSitoWeb());
		rsModel.setProvincia(uo.getAnagrafica().getProvincia());
		rsModel.setNazione(uo.getAnagrafica().getNazione());
		rsModel.setEmail(uo.getAnagrafica().getEmail());
		rsModel.setPec(uo.getAnagrafica().getPec());
		rsModel.setTel(uo.getAnagrafica().getTelefono());
		rsModel.setFax(uo.getAnagrafica().getFax());
		rsModel.setArea(uo.getAnagrafica().getArea());
		
		return rsModel;
	}
	
	public static Entrata toEntrataRsModel(it.govpay.bd.model.Tributo tributo, it.govpay.model.IbanAccredito ibanAccredito) throws ServiceException {
		Entrata rsModel = new Entrata();
		rsModel.codiceContabilita(tributo.getCodContabilita())
		.abilitato(tributo.isAbilitato())
		.idEntrata(tributo.getCodTributo())
		.tipoEntrata(EntrateConverter.toTipoEntrataRsModel(tributo));
		
		if(tributo.getTipoContabilita() != null) {
			switch (tributo.getTipoContabilita()) {
			case ALTRO:
				rsModel.tipoContabilita(TipoContabilita.ALTRO);
				break;
			case CAPITOLO:
				rsModel.tipoContabilita(TipoContabilita.CAPITOLO);
				break;
			case SIOPE:
				rsModel.tipoContabilita(TipoContabilita.SIOPE);
				break;
			case SPECIALE:
				rsModel.tipoContabilita(TipoContabilita.SPECIALE);
				break;
			}
		}
		
		if(ibanAccredito != null)
			rsModel.contoAccredito(ibanAccredito.getCodIban());

		if(tributo.getIbanAccredito()!=null)
			rsModel.contoAppoggio(tributo.getIbanAccredito().getCodIban());

		if(tributo.getCodTributoIuv()!=null)
			rsModel.codificaIUV(tributo.getCodTributoIuv());
		
		return rsModel;
	}
}

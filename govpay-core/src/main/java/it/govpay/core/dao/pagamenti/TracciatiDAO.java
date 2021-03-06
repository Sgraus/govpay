/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2018 Link.it srl (http://www.link.it).
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
package it.govpay.core.dao.pagamenti;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.SortOrder;
import org.openspcoop2.utils.serialization.IOException;
import org.openspcoop2.utils.serialization.ISerializer;
import org.openspcoop2.utils.serialization.SerializationConfig;
import org.openspcoop2.utils.serialization.SerializationFactory;
import org.openspcoop2.utils.serialization.SerializationFactory.SERIALIZATION_TYPE;

import it.govpay.bd.BasicBD;
import it.govpay.bd.FilterSortWrapper;
import it.govpay.bd.model.Operatore;
import it.govpay.bd.model.Operazione;
import it.govpay.bd.model.Tracciato;
import it.govpay.bd.model.Utenza;
import it.govpay.bd.pagamento.OperazioniBD;
import it.govpay.bd.pagamento.TracciatiBD;
import it.govpay.bd.pagamento.filters.OperazioneFilter;
import it.govpay.bd.pagamento.filters.TracciatoFilter;
import it.govpay.core.beans.tracciati.Pendenza;
import it.govpay.core.business.Tracciati;
import it.govpay.core.dao.commons.BaseDAO;
import it.govpay.core.dao.pagamenti.dto.LeggiTracciatoDTO;
import it.govpay.core.dao.pagamenti.dto.ListaOperazioniTracciatoDTO;
import it.govpay.core.dao.pagamenti.dto.ListaOperazioniTracciatoDTOResponse;
import it.govpay.core.dao.pagamenti.dto.ListaTracciatiDTO;
import it.govpay.core.dao.pagamenti.dto.ListaTracciatiDTOResponse;
import it.govpay.core.dao.pagamenti.dto.PostTracciatoDTO;
import it.govpay.core.dao.pagamenti.dto.PostTracciatoDTOResponse;
import it.govpay.core.dao.pagamenti.exception.TracciatoNonTrovatoException;
import it.govpay.core.exceptions.GovPayException;
import it.govpay.core.exceptions.NotAuthenticatedException;
import it.govpay.core.exceptions.NotAuthorizedException;
import it.govpay.core.utils.AclEngine;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.core.utils.SimpleDateFormatUtils;
import it.govpay.model.Acl.Diritti;
import it.govpay.model.Acl.Servizio;
import it.govpay.model.Tracciato.STATO_ELABORAZIONE;
import it.govpay.model.Tracciato.TIPO_TRACCIATO;
import it.govpay.orm.constants.StatoTracciatoType;

public class TracciatiDAO extends BaseDAO{

	public TracciatiDAO() {
	}

	public Tracciato leggiTracciato(LeggiTracciatoDTO leggiTracciatoDTO) throws ServiceException,TracciatoNonTrovatoException, NotAuthorizedException, NotAuthenticatedException{

		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			List<String> listaDominiFiltro;
			this.autorizzaRichiesta(leggiTracciatoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA, bd);

			// Autorizzazione sui domini
			listaDominiFiltro = AclEngine.getDominiAutorizzati((Utenza) leggiTracciatoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA);
			if(listaDominiFiltro == null) {
				throw new NotAuthorizedException("L'utenza autenticata ["+leggiTracciatoDTO.getUser().getPrincipal()+"] non e' autorizzata ai servizi " + Servizio.PAGAMENTI_E_PENDENZE + " per alcun dominio");
			}

			TracciatiBD tracciatoBD = new TracciatiBD(bd);
			Tracciato tracciato = tracciatoBD.getTracciato(leggiTracciatoDTO.getId());
			tracciato.getOperatore(bd);
			return tracciato;

		} catch (NotFoundException e) {
			throw new TracciatoNonTrovatoException(e.getMessage(), e);
		} finally {
			if(bd != null)
				bd.closeConnection();
		}
	}

	public byte[] leggiRichiestaTracciato(LeggiTracciatoDTO leggiTracciatoDTO) throws ServiceException,TracciatoNonTrovatoException, NotAuthorizedException, NotAuthenticatedException{

		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			List<String> listaDominiFiltro;
			this.autorizzaRichiesta(leggiTracciatoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA, bd);

			// Autorizzazione sui domini
			listaDominiFiltro = AclEngine.getDominiAutorizzati((Utenza) leggiTracciatoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA);
			if(listaDominiFiltro == null) {
				throw new NotAuthorizedException("L'utenza autenticata ["+leggiTracciatoDTO.getUser().getPrincipal()+"] non e' autorizzata ai servizi " + Servizio.PAGAMENTI_E_PENDENZE + " per alcun dominio");
			}

			TracciatiBD tracciatoBD = new TracciatiBD(bd);

			Tracciato tracciato = tracciatoBD.getTracciato(leggiTracciatoDTO.getId());
			tracciato.getOperatore(bd);
			byte[] rawRichiesta = tracciato.getRawRichiesta();
			if(rawRichiesta == null)
				throw new NotFoundException("File di richiesta non salvato");
			return rawRichiesta;

		} catch (NotFoundException e) {
			throw new TracciatoNonTrovatoException(e.getMessage(), e);
		} finally {
			if(bd != null)
				bd.closeConnection();
		}
	}

	public byte[] leggiEsitoTracciato(LeggiTracciatoDTO leggiTracciatoDTO) throws ServiceException,TracciatoNonTrovatoException, NotAuthorizedException, NotAuthenticatedException{

		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			List<String> listaDominiFiltro;
			this.autorizzaRichiesta(leggiTracciatoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA, bd);

			// Autorizzazione sui domini
			listaDominiFiltro = AclEngine.getDominiAutorizzati((Utenza) leggiTracciatoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA);
			if(listaDominiFiltro == null) {
				throw new NotAuthorizedException("L'utenza autenticata ["+leggiTracciatoDTO.getUser().getPrincipal()+"] non e' autorizzata ai servizi " + Servizio.PAGAMENTI_E_PENDENZE + " per alcun dominio");
			}

			TracciatiBD tracciatoBD = new TracciatiBD(bd);

			Tracciato tracciato = tracciatoBD.getTracciato(leggiTracciatoDTO.getId());
			tracciato.getOperatore(bd);
			byte[] rawEsito = tracciato.getRawEsito();
			if(rawEsito == null)
				throw new NotFoundException("File di esito non salvato");
			return rawEsito;


		} catch (NotFoundException e) {
			throw new TracciatoNonTrovatoException(e.getMessage(), e);
		} finally {
			if(bd != null)
				bd.closeConnection();
		}
	}
	
	public ListaTracciatiDTOResponse listaTracciati(ListaTracciatiDTO listaTracciatiDTO) throws ServiceException, NotAuthorizedException, NotAuthenticatedException{
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());

			return this.listaTracciati(listaTracciatiDTO, bd);
		} finally {
			if(bd != null)
				bd.closeConnection();
		}
	}

	public ListaTracciatiDTOResponse listaTracciati(ListaTracciatiDTO listaTracciatiDTO, BasicBD bd) throws NotAuthenticatedException, NotAuthorizedException, ServiceException {

		List<String> listaDominiFiltro;
		this.autorizzaRichiesta(listaTracciatiDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA, listaTracciatiDTO.getIdDominio(), null, bd);

		// Autorizzazione sui domini
		listaDominiFiltro = AclEngine.getDominiAutorizzati((Utenza) listaTracciatiDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA);
		if(listaDominiFiltro == null) {
			throw new NotAuthorizedException("L'utenza autenticata ["+listaTracciatiDTO.getUser().getPrincipal()+"] non e' autorizzata ai servizi " + Servizio.PAGAMENTI_E_PENDENZE + " per alcun dominio");
		}

		TracciatiBD tracciatoBD = new TracciatiBD(bd);
		TracciatoFilter filter = tracciatoBD.newFilter();

		if(listaTracciatiDTO.getIdDominio() != null)
				listaDominiFiltro.add(listaTracciatiDTO.getIdDominio());
		
		filter.setDomini(listaDominiFiltro);
		filter.setTipo(listaTracciatiDTO.getTipoTracciato());
		filter.setOffset(listaTracciatiDTO.getOffset());
		filter.setLimit(listaTracciatiDTO.getLimit());
		filter.setOperatore(listaTracciatiDTO.getOperatore());
		filter.setStato(listaTracciatiDTO.getStatoTracciato()); 
		filter.setDettaglioStato(listaTracciatiDTO.getDettaglioStato()); 
		
		List<FilterSortWrapper> filterSortList = new ArrayList<>();
		FilterSortWrapper fsw = new FilterSortWrapper();
		fsw.setSortOrder(SortOrder.DESC);
		fsw.setField(it.govpay.orm.Tracciato.model().DATA_CARICAMENTO);
		filterSortList.add(fsw );
		filter.setFilterSortList(filterSortList );
		
		

		long count = tracciatoBD.count(filter);

		List<Tracciato> resList = new ArrayList<>();
		if(count > 0) {
			List<Tracciato> resListTmp = new ArrayList<>();
			
			resListTmp = tracciatoBD.findAll(filter);
			
			if(!resListTmp.isEmpty()) {
				for (Tracciato tracciato : resListTmp) {
					tracciato.getOperatore(bd);
					resList.add(tracciato);
				}
			}
		} 

		return new ListaTracciatiDTOResponse(count, resList);
	}
	
	public PostTracciatoDTOResponse create(PostTracciatoDTO postTracciatoDTO) throws NotAuthenticatedException, NotAuthorizedException, GovPayException {
		
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
			this.autorizzaRichiesta(postTracciatoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.SCRITTURA, postTracciatoDTO.getIdDominio(), null, bd);
			
			SerializationConfig config = new SerializationConfig();
			config.setDf(SimpleDateFormatUtils.newSimpleDateFormatDataOreMinuti());
			config.setIgnoreNullValues(true);
			ISerializer serializer = SerializationFactory.getSerializer(SERIALIZATION_TYPE.JSON_JACKSON, config);
	
			TracciatiBD tracciatoBD = new TracciatiBD(bd);
			
			Operatore operatoreFromUser = this.getOperatoreFromUser(postTracciatoDTO.getUser(),bd);
			it.govpay.core.beans.tracciati.Pendenza beanDati = new Pendenza();
			beanDati.setStepElaborazione(StatoTracciatoType.NUOVO.getValue());
			
			Tracciato tracciato = new Tracciato();
			tracciato.setCodDominio(postTracciatoDTO.getIdDominio());
			tracciato.setBeanDati(serializer.getObject(beanDati));
			tracciato.setDataCaricamento(new Date());
			tracciato.setFileNameRichiesta(postTracciatoDTO.getNomeFile());
			tracciato.setRawRichiesta(postTracciatoDTO.getContenuto());
		
			tracciato.setIdOperatore(operatoreFromUser.getId());
			tracciato.setTipo(TIPO_TRACCIATO.PENDENZA);
			tracciato.setStato(STATO_ELABORAZIONE.ELABORAZIONE);
			tracciato.setIdOperatore(operatoreFromUser.getId()); 
			
			tracciatoBD.insertTracciato(tracciato);
			
			// avvio elaborazione tracciato
			it.govpay.core.business.Operazioni.setEseguiElaborazioneTracciati();
			return new PostTracciatoDTOResponse();

			
		} catch (ServiceException | IOException e) {
			throw new GovPayException(e);
		} catch (NotFoundException e) {
			throw AclEngine.toNotAuthorizedException(postTracciatoDTO.getUser());
		} finally {
			if(bd != null)
				bd.closeConnection();
		}

	}
	
	public ListaOperazioniTracciatoDTOResponse listaOperazioniTracciatoPendenza(ListaOperazioniTracciatoDTO listaOperazioniTracciatoDTO) throws ServiceException, NotAuthorizedException, NotAuthenticatedException{
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());

			return this.listaOperazioniTracciatoPendenza(listaOperazioniTracciatoDTO, bd);
		} finally {
			if(bd != null)
				bd.closeConnection();
		}
	}
	
	public ListaOperazioniTracciatoDTOResponse listaOperazioniTracciatoPendenza(ListaOperazioniTracciatoDTO listaOperazioniTracciatoDTO, BasicBD bd) throws NotAuthenticatedException, NotAuthorizedException, ServiceException {

		List<String> listaDominiFiltro;
		this.autorizzaRichiesta(listaOperazioniTracciatoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA, bd);

		// Autorizzazione sui domini
		listaDominiFiltro = AclEngine.getDominiAutorizzati((Utenza) listaOperazioniTracciatoDTO.getUser(), Servizio.PAGAMENTI_E_PENDENZE, Diritti.LETTURA);
		if(listaDominiFiltro == null) {
			throw new NotAuthorizedException("L'utenza autenticata ["+listaOperazioniTracciatoDTO.getUser().getPrincipal()+"] non e' autorizzata ai servizi " + Servizio.PAGAMENTI_E_PENDENZE + " per alcun dominio");
		}

		OperazioniBD operazioniBD = new OperazioniBD(bd);
		OperazioneFilter filter = operazioniBD.newFilter();

		filter.setIdTracciato(listaOperazioniTracciatoDTO.getIdTracciato());
		filter.setOffset(listaOperazioniTracciatoDTO.getOffset());
		filter.setLimit(listaOperazioniTracciatoDTO.getLimit());

		long count = operazioniBD.count(filter);

		List<Operazione> resList = new ArrayList<>();
		if(count > 0) {
			List<Operazione> resListTmp = operazioniBD.findAll(filter);
			
			Tracciati tracciatiBD = new Tracciati(bd);
			for (Operazione operazione : resListTmp) {
				resList.add(tracciatiBD.fillOperazione(operazione).getOperazione());
			}
		} 
		
		return new ListaOperazioniTracciatoDTOResponse(count, resList);
	}
	
	
	
}

/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2017 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
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
package it.govpay.core.dao.anagrafica;


import java.util.ArrayList;
import java.util.List;

import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.utils.json.ValidationException;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.anagrafica.OperatoriBD;
import it.govpay.bd.anagrafica.filters.OperatoreFilter;
import it.govpay.bd.model.Operatore;
import it.govpay.bd.model.Utenza;
import it.govpay.core.dao.anagrafica.dto.DeleteOperatoreDTO;
import it.govpay.core.dao.anagrafica.dto.FindOperatoriDTO;
import it.govpay.core.dao.anagrafica.dto.FindOperatoriDTOResponse;
import it.govpay.core.dao.anagrafica.dto.LeggiOperatoreDTO;
import it.govpay.core.dao.anagrafica.dto.LeggiOperatoreDTOResponse;
import it.govpay.core.dao.anagrafica.dto.LeggiProfiloDTOResponse;
import it.govpay.core.dao.anagrafica.dto.PutOperatoreDTO;
import it.govpay.core.dao.anagrafica.dto.PutOperatoreDTOResponse;
import it.govpay.core.dao.anagrafica.exception.DominioNonTrovatoException;
import it.govpay.core.dao.anagrafica.exception.OperatoreNonTrovatoException;
import it.govpay.core.dao.anagrafica.exception.TipoTributoNonTrovatoException;
import it.govpay.core.dao.anagrafica.utils.UtenzaPatchUtils;
import it.govpay.core.dao.commons.BaseDAO;
import it.govpay.core.dao.pagamenti.dto.OperatorePatchDTO;
import it.govpay.core.dao.pagamenti.exception.PagamentoPortaleNonTrovatoException;
import it.govpay.core.exceptions.NotAuthenticatedException;
import it.govpay.core.exceptions.NotAuthorizedException;
import it.govpay.core.rs.v1.beans.base.PatchOp;
import it.govpay.core.utils.GpThreadLocal;
import it.govpay.model.Acl.Diritti;
import it.govpay.model.Acl.Servizio;
import it.govpay.model.IAutorizzato;


public class UtentiDAO extends BaseDAO{
	
	public UtentiDAO() {
		super();
	}

	public UtentiDAO(boolean useCacheData) {
		super(useCacheData);
	}

	public enum TipoUtenza {
		PORTALE, OPERATORE, APPLICAZIONE;
	}

	public void populateUser(IAutorizzato user) throws NotAuthenticatedException, ServiceException, NotAuthorizedException {
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId(), useCacheData);
			this.populateUser(user, bd);
		} finally {
			if(bd != null)
				bd.closeConnection();
		}
	}

	public LeggiProfiloDTOResponse getProfilo(IAutorizzato user) throws NotAuthenticatedException, ServiceException, NotAuthorizedException {
		BasicBD bd = null;
		LeggiProfiloDTOResponse response = new LeggiProfiloDTOResponse();
		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId(), useCacheData);
			response.setNome(this.populateUser(user, bd));
			
			response.setUtente(user);
			
			response.setDomini(((Utenza)user).getDomini(bd));
			response.setTributi(((Utenza)user).getTributi(bd));
			
		} finally {
			if(bd != null)
				bd.closeConnection();
		}
		
		return response;
	}

	public LeggiOperatoreDTOResponse getOperatore(LeggiOperatoreDTO leggiOperatore) throws NotAuthenticatedException, ServiceException, OperatoreNonTrovatoException, NotAuthorizedException {
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId(), useCacheData);
			this.autorizzaRichiesta(leggiOperatore.getUser(), Servizio.ANAGRAFICA_RUOLI, Diritti.LETTURA,bd);
			OperatoriBD applicazioniBD = new OperatoriBD(bd);
			
			Operatore operatore = applicazioniBD.getOperatore(leggiOperatore.getPrincipal());
			LeggiOperatoreDTOResponse response = new LeggiOperatoreDTOResponse();
			response.setOperatore(operatore);
			return response;
		} catch (org.openspcoop2.generic_project.exception.NotFoundException e3) {
			throw new OperatoreNonTrovatoException("Operatore " + leggiOperatore.getPrincipal() + " non censito in Anagrafica");
		} finally {
			if(bd != null)
				bd.closeConnection();
		}
	}

	public FindOperatoriDTOResponse findOperatori(FindOperatoriDTO listaOperatoriDTO) throws NotAuthorizedException, ServiceException, NotAuthenticatedException {
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId(), useCacheData);
			this.autorizzaRichiesta(listaOperatoriDTO.getUser(), Servizio.ANAGRAFICA_RUOLI, Diritti.LETTURA,bd);

			OperatoriBD applicazioniBD = new OperatoriBD(bd);
			OperatoreFilter filter = null;
			if(listaOperatoriDTO.isSimpleSearch()) {
				filter = applicazioniBD.newFilter(true);
				filter.setSimpleSearchString(listaOperatoriDTO.getSimpleSearch());
			} else {
				filter = applicazioniBD.newFilter(false);

			}

			filter.setSearchAbilitato(listaOperatoriDTO.getAbilitato());
			filter.setOffset(listaOperatoriDTO.getOffset());
			filter.setLimit(listaOperatoriDTO.getLimit());
			filter.getFilterSortList().addAll(listaOperatoriDTO.getFieldSortList());

			return new FindOperatoriDTOResponse(applicazioniBD.count(filter), applicazioniBD.findAll(filter));

		} finally {
			if(bd != null)
				bd.closeConnection();
		}
	}

	public PutOperatoreDTOResponse createOrUpdate(PutOperatoreDTO putOperatoreDTO) throws ServiceException, OperatoreNonTrovatoException,TipoTributoNonTrovatoException, DominioNonTrovatoException, NotAuthorizedException, NotAuthenticatedException {
		PutOperatoreDTOResponse operatoreDTOResponse = new PutOperatoreDTOResponse();
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId(), useCacheData);
			this.autorizzaRichiesta(putOperatoreDTO.getUser(), Servizio.ANAGRAFICA_RUOLI, Diritti.SCRITTURA,bd);
			OperatoriBD operatoriBD = new OperatoriBD(bd);
			OperatoreFilter filter = operatoriBD.newFilter(false);
			filter.setPrincipal(putOperatoreDTO.getPrincipal());
			filter.setSearchModeEquals(true); // ricerca esatta del principal che sto inserendo

			// flag creazione o update
			boolean isCreate = operatoriBD.count(filter) == 0;
			operatoreDTOResponse.setCreated(isCreate);

			if(putOperatoreDTO.getIdDomini() != null) {
				List<Long> idDomini = new ArrayList<>();
				for (String codDominio : putOperatoreDTO.getIdDomini()) {
					try {
						idDomini.add(AnagraficaManager.getDominio(bd, codDominio).getId());
					} catch (org.openspcoop2.generic_project.exception.NotFoundException e) {
						throw new DominioNonTrovatoException(e.getMessage(), e);
					}
				}

				putOperatoreDTO.getOperatore().getUtenza().setIdDomini(idDomini );
			}

			if(putOperatoreDTO.getIdTributi() != null) {
				List<Long> idTributi = new ArrayList<>();
				for (String codTributo : putOperatoreDTO.getIdTributi()) {
					try {
						idTributi.add(AnagraficaManager.getTipoTributo(bd, codTributo).getId());
					} catch (org.openspcoop2.generic_project.exception.NotFoundException e) {
						throw new TipoTributoNonTrovatoException(e.getMessage(), e);
					}
				}

				putOperatoreDTO.getOperatore().getUtenza().setIdTributi(idTributi);
			}


			if(isCreate) {
				operatoriBD.insertOperatore(putOperatoreDTO.getOperatore());
			} else {
				putOperatoreDTO.getOperatore().setIdUtenza(AnagraficaManager.getUtenza(bd, putOperatoreDTO.getOperatore().getUtenza().getPrincipal()).getId());
				operatoriBD.updateOperatore(putOperatoreDTO.getOperatore());
			}
		} catch (org.openspcoop2.generic_project.exception.NotFoundException e) {
			throw new OperatoreNonTrovatoException(e.getMessage(), e);
		} finally {
			if(bd != null)
				bd.closeConnection();
		}
		return operatoreDTOResponse;
	}

	/**
	 * @param deleteOperatoreDTO
	 * @throws NotAuthenticatedException 
	 */
	public void deleteOperatore(DeleteOperatoreDTO deleteOperatoreDTO) throws NotAuthorizedException, OperatoreNonTrovatoException, ServiceException, NotAuthenticatedException {
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId(), useCacheData);
			this.autorizzaRichiesta(deleteOperatoreDTO.getUser(), Servizio.ANAGRAFICA_RUOLI, Diritti.SCRITTURA,bd);
			new OperatoriBD(bd).deleteOperatore(deleteOperatoreDTO.getPrincipal());
		} catch (NotFoundException e) {
			throw new OperatoreNonTrovatoException(e.getMessage());
		} finally {
			if(bd != null)
				bd.closeConnection();
		}
	}
	

	public LeggiOperatoreDTOResponse patch(OperatorePatchDTO patchDTO) throws ServiceException,PagamentoPortaleNonTrovatoException, NotAuthorizedException, NotAuthenticatedException, ValidationException{
		BasicBD bd = null;

		try {
			bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId(), useCacheData);
			
			this.autorizzaRichiesta(patchDTO.getUser(), Servizio.ANAGRAFICA_RUOLI, Diritti.SCRITTURA,bd);

			OperatoriBD operatoriBD = new OperatoriBD(bd);
			Operatore operatore = operatoriBD.getOperatore(patchDTO.getIdOperatore());
			LeggiOperatoreDTOResponse leggiOperatoreDTOResponse = new LeggiOperatoreDTOResponse();
			for(PatchOp op: patchDTO.getOp()) {
				UtenzaPatchUtils.patchUtenza(op, operatore.getUtenza(), bd);
			}
			
			//operatoriBD.updateOperatore(operatore);
			
			AnagraficaManager.removeFromCache(operatore);
			AnagraficaManager.removeFromCache(operatore.getUtenza()); 
			
			operatore = operatoriBD.getOperatore(patchDTO.getIdOperatore());
			leggiOperatoreDTOResponse.setOperatore(operatore);
			
			return leggiOperatoreDTOResponse;
		}catch(NotFoundException e) {
			throw new PagamentoPortaleNonTrovatoException("Non esiste un operatore associato al principal ["+patchDTO.getIdOperatore()+"]");
		}finally {
			if(bd != null)
				bd.closeConnection();
		}
		
	}


}




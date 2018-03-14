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

import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.AnagraficaManager;
import it.govpay.bd.anagrafica.TipiTributoBD;
import it.govpay.bd.anagrafica.filters.TipoTributoFilter;
import it.govpay.core.dao.anagrafica.dto.FindEntrateDTO;
import it.govpay.core.dao.anagrafica.dto.FindEntrateDTOResponse;
import it.govpay.core.dao.anagrafica.dto.GetEntrataDTO;
import it.govpay.core.dao.anagrafica.dto.GetEntrataDTOResponse;
import it.govpay.core.dao.anagrafica.dto.PutEntrataDTO;
import it.govpay.core.dao.anagrafica.dto.PutEntrataDTOResponse;
import it.govpay.core.dao.anagrafica.exception.TipoTributoNonTrovatoException;
import it.govpay.core.exceptions.NotAuthorizedException;
import it.govpay.core.utils.GpThreadLocal;

public class EntrateDAO {

	public PutEntrataDTOResponse createOrUpdateEntrata(PutEntrataDTO putTipoTributoDTO) throws ServiceException,TipoTributoNonTrovatoException{
		PutEntrataDTOResponse intermediarioDTOResponse = new PutEntrataDTOResponse();
		BasicBD bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
		try {
			TipiTributoBD intermediariBD = new TipiTributoBD(bd);
			TipoTributoFilter filter = intermediariBD.newFilter(false);
			filter.setCodTributo(putTipoTributoDTO.getCodTributo());
			
			// flag creazione o update
			boolean isCreate = intermediariBD.count(filter) == 0;
			intermediarioDTOResponse.setCreated(isCreate);
			if(isCreate) {
				intermediariBD.insertTipoTributo(putTipoTributoDTO.getTipoTributo());
			} else {
				intermediariBD.updateTipoTributo(putTipoTributoDTO.getTipoTributo());
			}
		} catch (org.openspcoop2.generic_project.exception.NotFoundException e) {
			throw new TipoTributoNonTrovatoException(e.getMessage());
		} finally {
			bd.closeConnection();
		}
		return intermediarioDTOResponse;
	}

	public FindEntrateDTOResponse findEntrate(FindEntrateDTO findEntrateDTO) throws NotAuthorizedException, ServiceException {
		BasicBD bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
		try {

			TipiTributoBD stazioneBD = new TipiTributoBD(bd);
			TipoTributoFilter filter = null;
			if(findEntrateDTO.isSimpleSearch()) {
				filter = stazioneBD.newFilter(true);
				filter.setSimpleSearchString(findEntrateDTO.getSimpleSearch());
			} else {
				filter = stazioneBD.newFilter(false);
				filter.setSearchAbilitato(findEntrateDTO.getAbilitato());
			}

			filter.setOffset(findEntrateDTO.getOffset());
			filter.setLimit(findEntrateDTO.getLimit());
			filter.getFilterSortList().addAll(findEntrateDTO.getFieldSortList());

			return new FindEntrateDTOResponse(stazioneBD.count(filter), stazioneBD.findAll(filter));
		} finally {
			bd.closeConnection();
		}
	}

	public GetEntrataDTOResponse getEntrata(GetEntrataDTO getEntrataDTO) throws NotAuthorizedException, TipoTributoNonTrovatoException, ServiceException {
		BasicBD bd = BasicBD.newInstance(GpThreadLocal.get().getTransactionId());
		GetEntrataDTOResponse response = null;
		try {
			response = new GetEntrataDTOResponse(AnagraficaManager.getTipoTributo(bd, getEntrataDTO.getCodTipoTributo()));
		} catch (org.openspcoop2.generic_project.exception.NotFoundException e) {
			throw new TipoTributoNonTrovatoException("Entrata " + getEntrataDTO.getCodTipoTributo() + " non censita in Anagrafica");
		} finally {
			bd.closeConnection();
		}
		return response;
	}

}
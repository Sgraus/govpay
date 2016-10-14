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
package it.govpay.bd.anagrafica;

import it.govpay.bd.BasicBD;
import it.govpay.bd.anagrafica.filters.StazioneFilter;
import it.govpay.bd.model.converter.StazioneConverter;
import it.govpay.bd.model.Stazione;
import it.govpay.orm.IdStazione;
import it.govpay.orm.dao.jdbc.JDBCStazioneServiceSearch;
import it.govpay.orm.dao.jdbc.converter.StazioneFieldConverter;

import java.util.List;

import org.openspcoop2.generic_project.beans.CustomField;
import org.openspcoop2.generic_project.exception.ExpressionException;
import org.openspcoop2.generic_project.exception.ExpressionNotImplementedException;
import org.openspcoop2.generic_project.exception.MultipleResultException;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IPaginatedExpression;
import org.openspcoop2.utils.UtilsException;

public class StazioniBD extends BasicBD {
	
	public StazioniBD(BasicBD basicBD) {
		super(basicBD);
		
	}

	public void insertStazione(Stazione stazione) throws ServiceException {
		try {

			it.govpay.orm.Stazione vo = StazioneConverter.toVO(stazione);

			this.getStazioneService().create(vo);
			stazione.setId(vo.getId());			
			
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}
	}
	
	public void updateStazione(Stazione stazione) throws ServiceException, NotFoundException {

		try {
			it.govpay.orm.Stazione vo = StazioneConverter.toVO(stazione);
			IdStazione idStazione = this.getStazioneService().convertToId(vo);
			
			if(!this.getStazioneService().exists(idStazione)) {
				throw new NotFoundException("IdStazione con id ["+idStazione.toJson()+"] non trovata");
			}
			
			this.getStazioneService().update(idStazione, vo);
			stazione.setId(vo.getId());
			AnagraficaManager.removeFromCache(stazione);
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (MultipleResultException e) {
			throw new ServiceException(e);
		} catch (UtilsException e) {
			throw new ServiceException(e);
		}
	}

	public Stazione getStazione(Long idStazione) throws ServiceException, NotFoundException, MultipleResultException {
		
		if(idStazione== null) {
			throw new ServiceException("Parameter 'id' cannot be NULL");
		}
		long id = idStazione.longValue();
		try {
			it.govpay.orm.Stazione stazioneVO = ((JDBCStazioneServiceSearch)this.getStazioneService()).get(id);
			Stazione stazione = StazioneConverter.toDTO(stazioneVO);
			return stazione;
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}

	}

	public Stazione getStazione(String codStazione) throws ServiceException, NotFoundException, MultipleResultException {
		try {
			IdStazione idStazione = new IdStazione();
			idStazione.setCodStazione(codStazione);

			it.govpay.orm.Stazione stazioneVO = this.getStazioneService().get(idStazione);
			Stazione stazione = StazioneConverter.toDTO(stazioneVO);
			return stazione;
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}

	}
	
	public StazioneFilter newFilter() throws ServiceException {
		return new StazioneFilter(this.getStazioneService());
	}

	public long count(StazioneFilter filter) throws ServiceException {
		try {
			return this.getStazioneService().count(filter.toExpression()).longValue();
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}
	}

	public List<Stazione> findAll(StazioneFilter filter) throws ServiceException {
		try {
			return StazioneConverter.toDTOList(this.getStazioneService().findAll(filter.toPaginatedExpression()));
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}
	}

	// Ritorna tutte le stazioni afferenti all'intermediario indicato
	public List<Stazione> getStazioni(long idIntermediario) throws ServiceException {
		try {
			StazioneFieldConverter fieldConverter = new StazioneFieldConverter(this.getJdbcProperties().getDatabaseType());

			IPaginatedExpression exp = this.getStazioneService().newPaginatedExpression();
			exp.equals(new CustomField("id_intermediario", Long.class, "id_intermediario", fieldConverter.toTable(it.govpay.orm.Stazione.model())), idIntermediario);
			return StazioneConverter.toDTOList(this.getStazioneService().findAll(exp));
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionNotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionException e) {
			throw new ServiceException(e);
		}
	}

	public List<Stazione> getStazioni() throws ServiceException {
		return this.findAll(this.newFilter());
	}

}

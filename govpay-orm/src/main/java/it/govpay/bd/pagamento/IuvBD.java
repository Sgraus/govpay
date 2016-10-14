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
package it.govpay.bd.pagamento;

import it.govpay.bd.BasicBD;
import it.govpay.model.Applicazione;
import it.govpay.model.Dominio;
import it.govpay.model.Iuv;
import it.govpay.model.Iuv.TipoIUV;
import it.govpay.bd.model.converter.IuvConverter;
import it.govpay.bd.pagamento.util.IuvUtils;
import it.govpay.orm.IUV;
import it.govpay.orm.dao.jdbc.JDBCIUVService;
import it.govpay.orm.dao.jdbc.converter.IUVFieldConverter;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openspcoop2.generic_project.beans.CustomField;
import org.openspcoop2.generic_project.exception.ExpressionException;
import org.openspcoop2.generic_project.exception.ExpressionNotImplementedException;
import org.openspcoop2.generic_project.exception.MultipleResultException;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IExpression;
import org.openspcoop2.generic_project.expression.IPaginatedExpression;
import org.openspcoop2.utils.UtilsException;
import org.openspcoop2.utils.id.serial.IDSerialGeneratorType;
import org.openspcoop2.utils.id.serial.InfoStatistics;

public class IuvBD extends BasicBD {
	
	public enum	Algoritmo {
		BASE, PAP
	}

	private static Logger log = Logger.getLogger(IuvBD.class);

	public IuvBD(BasicBD basicBD) {
		super(basicBD);
	}
	
	public Iuv generaIuv(Applicazione applicazione, Dominio dominio, String codVersamentoEnte, int auxDigit, int applicationCode, TipoIUV type) throws ServiceException {
		return generaIuv(applicazione, dominio, codVersamentoEnte, auxDigit, applicationCode, type, Algoritmo.BASE, true);
	}


	public Iuv generaIuv(Applicazione applicazione, Dominio dominio, String codVersamentoEnte, int auxDigit, int applicationCode, TipoIUV type, Algoritmo alg, boolean persistence) throws ServiceException {
		long prg = 0;
		String iuv = null;
		switch (alg) {
		case BASE:
			prg = getNextPrgIuv(dominio.getCodDominio(), type);
			
			switch (type) {
			case ISO11694:
				String reference = String.format("%015d", prg);
				String check = IuvUtils.getCheckDigit(reference);
				iuv = "RF" + check + reference;
				break;
			case NUMERICO:
				iuv = IuvUtils.buildIuvNumerico(prg, auxDigit, applicationCode);
				break;
			}
			
			break;
			
		case PAP:
			String gd = IuvUtils.retriveActualJulianDate();
			prg = getNextPrgIuv(dominio.getCodDominio(), type, gd);
			
			switch (type) {
			case ISO11694:
				String reference = IuvUtils.buildReference(gd, Long.toString(prg));
				String check = IuvUtils.getCheckDigit(reference);
				iuv = "RF" + check + reference;
				break;
			case NUMERICO:
				iuv = IuvUtils.buildIuvNumerico(prg, auxDigit, applicationCode);
				break;
			}
		}
		
		Iuv iuvDTO = new Iuv();
		iuvDTO.setIdDominio(dominio.getId());
		iuvDTO.setPrg(prg);
		iuvDTO.setIuv(iuv);
		iuvDTO.setDataGenerazione(new Date());
		iuvDTO.setIdApplicazione(applicazione.getId());
		iuvDTO.setTipo(type);
		iuvDTO.setCodVersamentoEnte(codVersamentoEnte);
		iuvDTO.setApplicationCode(applicationCode);
		if(persistence)
			return insertIuv(iuvDTO);
		else
			return iuvDTO;
	}

	public Iuv insertIuv(Iuv iuv) throws ServiceException{
		IUV iuvVO = IuvConverter.toVO(iuv);
		try {
			this.getIuvService().create(iuvVO);
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		}
		iuv.setId(iuvVO.getId());
		return iuv;
	}


	/**
	 * Crea un nuovo IUV a meno dell'iuv stesso.
	 * Il prg deve essere un progressivo all'interno del DominioEnte fornito
	 * 
	 * @param codDominio
	 * @param idApplicazione
	 * @return prg
	 * @throws ServiceException
	 */
	
	private long getNextPrgIuv(String codDominio, TipoIUV type) throws ServiceException {
		return getNextPrgIuv(codDominio, type, "");
	}
	private long getNextPrgIuv(String codDominio, TipoIUV type, String gd) throws ServiceException {
		InfoStatistics infoStat = null;
		BasicBD bd = null;
		try {
			infoStat = new InfoStatistics();
			org.openspcoop2.utils.id.serial.IDSerialGenerator serialGenerator = new org.openspcoop2.utils.id.serial.IDSerialGenerator(infoStat);
			org.openspcoop2.utils.id.serial.IDSerialGeneratorParameter params = new org.openspcoop2.utils.id.serial.IDSerialGeneratorParameter("GovPay");
			params.setTipo(IDSerialGeneratorType.NUMERIC);
			params.setWrap(false);
			params.setInformazioneAssociataAlProgressivo(codDominio + gd + type.toString()); // il progressivo sarà relativo a questa informazione

			java.sql.Connection con = null; 

			// Se sono in transazione aperta, utilizzo una connessione diversa perche' l'utility di generazione non supporta le transazioni.
			if(!isAutoCommit()) {
				bd = BasicBD.newInstance(this.getIdTransaction());
				con = bd.getConnection();
			} else {
				con = getConnection();
			}

			return serialGenerator.buildIDAsNumber(params, con, this.getJdbcProperties().getDatabase(), log);
		} catch (UtilsException e) {
			log.error("Numero di errori 'access serializable': "+infoStat.getErrorSerializableAccess());
			for (int i=0; i<infoStat.getExceptionOccurs().size(); i++) {
				Throwable t = infoStat.getExceptionOccurs().get(i);
				log.error("Errore-"+(i+1)+" (occurs:"+infoStat.getNumber(t)+"): "+t.getMessage());
			}
			throw new ServiceException(e);
		} finally {
			if(bd != null) bd.closeConnection();
		}
	}

	/**
	 * Recupera lo IUV con la chiave logi generato
	 * @param iuv
	 * @return
	 * @throws ServiceException
	 */
	public Iuv getIuv(long idDominio, String iuv) throws ServiceException, NotFoundException {
		try {
			IExpression exp = this.getIuvService().newExpression();
			exp.equals(it.govpay.orm.IUV.model().IUV, iuv);
			IUVFieldConverter converter = new IUVFieldConverter(this.getJdbcProperties().getDatabase());
			CustomField idDominioField = new CustomField("id_dominio", Long.class, "id_dominio", converter.toTable(IUV.model()));

			exp.equals(idDominioField, idDominio);
			it.govpay.orm.IUV iuvVO = this.getIuvService().find(exp);

			Iuv iuvDTO = IuvConverter.toDTO(iuvVO);

			return iuvDTO;
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionNotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionException e) {
			throw new ServiceException(e);
		} catch (MultipleResultException e) {
			throw new ServiceException(e);
		}
	}

	public Iuv getIuv(long idApplicazione, String codVersamentoEnte, TipoIUV tipo) throws ServiceException, NotFoundException {
		try {
			IPaginatedExpression exp = this.getIuvService().newPaginatedExpression();
			exp.equals(it.govpay.orm.IUV.model().COD_VERSAMENTO_ENTE, codVersamentoEnte);
			exp.equals(it.govpay.orm.IUV.model().TIPO_IUV, tipo.getCodifica());
			IUVFieldConverter converter = new IUVFieldConverter(this.getJdbcProperties().getDatabase());
			CustomField idDominioField = new CustomField("id_applicazione", Long.class, "id_applicazione", converter.toTable(IUV.model()));
			exp.equals(idDominioField, idApplicazione);
			List<it.govpay.orm.IUV> iuvVO = this.getIuvService().findAll(exp);
			List<Iuv> iuvs = IuvConverter.toDTOList(iuvVO);
			if(iuvs.size() > 0)
				return Collections.max(iuvs, new IuvComparator());
			else 
				throw new NotFoundException();
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionNotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionException e) {
			throw new ServiceException(e);
		} 
	}

	/**
	 * Recupera lo IUV con la chiave logi generato
	 * @param iuv
	 * @return
	 * @throws ServiceException
	 */
	public Iuv getIuv(long idIuv) throws ServiceException {
		try {
			it.govpay.orm.IUV iuvVO = ((JDBCIUVService)this.getIuvService()).get(idIuv);
			Iuv iuvDTO = IuvConverter.toDTO(iuvVO);

			return iuvDTO;
		}  catch (NotFoundException e) {
			throw new ServiceException(e);
		}  catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (MultipleResultException e) {
			throw new ServiceException(e);
		}
	}

	public class IuvComparator implements Comparator<Iuv> {
		@Override
		public int compare(Iuv o1, Iuv o2) {
			return o1.getDataGenerazione().compareTo(o2.getDataGenerazione());
		}
	}
}

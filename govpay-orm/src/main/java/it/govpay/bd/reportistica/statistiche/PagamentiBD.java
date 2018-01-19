package it.govpay.bd.reportistica.statistiche;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;

import it.govpay.bd.BasicBD;
import it.govpay.bd.nativequeries.NativeQueries;
import it.govpay.bd.reportistica.statistiche.filters.PagamentiFilter;
import it.govpay.model.reportistica.statistiche.AndamentoTemporale;
import it.govpay.model.reportistica.statistiche.DistribuzionePsp;
import it.govpay.orm.dao.jdbc.JDBCServiceManager;

public class PagamentiBD extends BasicBD {

	public PagamentiBD(BasicBD basicBD) {
		super(basicBD);
	}
	
	public PagamentiFilter newFilter() throws ServiceException {
		return new PagamentiFilter(this.getPagamentoService());
	}
	
	public List<AndamentoTemporale> getAndamentoTemporale(PagamentiFilter filtro) throws ServiceException {
		try {
			List<Class<?>> lstReturnType = new ArrayList<Class<?>>();

			lstReturnType.add(Date.class);
			lstReturnType.add(Long.class);

			String nativeQueryString = NativeQueries.getInstance().getStatistichePagamentiAndamentoTemporaleQuery(filtro.getTipoIntervallo(), filtro.getData(), filtro.getLimit(), filtro);
			Logger.getLogger(JDBCServiceManager.class).debug(nativeQueryString);

			Object[] array = NativeQueries.getInstance().getStatistichePagamentiAndamentoTemporaleValues(filtro.getTipoIntervallo(), filtro.getData(), filtro.getLimit(), filtro);
			Logger.getLogger(JDBCServiceManager.class).debug("Params: ");
			for(Object obj: array) {
				Logger.getLogger(JDBCServiceManager.class).debug(obj);
			}

			List<List<Object>> lstRecords = this.getRptService().nativeQuery(nativeQueryString, lstReturnType, array);

			List<AndamentoTemporale> distribuzioni = new ArrayList<AndamentoTemporale>();

			for(List<Object> record: lstRecords) {
				distribuzioni.add(new AndamentoTemporale((Date) record.get(0), (Long) record.get(1)));
			}
			return distribuzioni;
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (NotFoundException e) {
			return new ArrayList<AndamentoTemporale>();
		}
	}
	
	
	public List<DistribuzionePsp> getDistribuzionePsp(PagamentiFilter filtro) throws ServiceException {
		try {
			List<Class<?>> lstReturnType = new ArrayList<Class<?>>();

			lstReturnType.add(String.class);
			lstReturnType.add(Long.class);

			String nativeQueryString = NativeQueries.getInstance().getStatistichePagamentiPerPspQuery(filtro.getTipoIntervallo(), filtro.getData(), filtro);
			Logger.getLogger(JDBCServiceManager.class).debug(nativeQueryString);

			Object[] array = NativeQueries.getInstance().getStatistichePagamentiPerPspValues(filtro.getTipoIntervallo(), filtro.getData(), filtro);
			Logger.getLogger(JDBCServiceManager.class).debug("Params: ");
			for(Object obj: array) {
				Logger.getLogger(JDBCServiceManager.class).debug(obj);
			}

			List<List<Object>> lstRecords = this.getRptService().nativeQuery(nativeQueryString, lstReturnType, array);

			List<DistribuzionePsp> distribuzioniTmp = new ArrayList<DistribuzionePsp>();
			List<DistribuzionePsp> distribuzioniFinal = new ArrayList<DistribuzionePsp>();

			long totale = 0;
			for(List<Object> record: lstRecords) {
				distribuzioniTmp.add(new DistribuzionePsp((String) record.get(0), (Long) record.get(1)));
				totale += (Long) record.get(1);
			}
			
			// Calcolo le percentuali
			double percentualeOccupata = 0;
			long totaleOccupato = 0;
			for(int i=0; i<distribuzioniTmp.size(); i++) {
				DistribuzionePsp distribuzione = distribuzioniTmp.get(i);
				
				double percentuale = ((double) distribuzione.getNumero()) / totale;
				if(percentuale >= filtro.getSoglia()) {
					distribuzione.setPercentuale(percentuale);
					distribuzioniFinal.add(distribuzione);
					percentualeOccupata += percentuale;
					totaleOccupato += distribuzione.getNumero();
				} else {
					DistribuzionePsp distribuzioneAltro = new DistribuzionePsp("Altri", totale - totaleOccupato);
					distribuzioneAltro.setPercentuale(1 - percentualeOccupata);
					distribuzioniFinal.add(distribuzioneAltro);
					break;
				}
			}
			
			return distribuzioniFinal;
		} catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (NotFoundException e) {
			return new ArrayList<DistribuzionePsp>();
		}
	}
}

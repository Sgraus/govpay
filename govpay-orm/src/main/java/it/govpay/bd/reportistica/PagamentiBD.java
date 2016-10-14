package it.govpay.bd.reportistica;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openspcoop2.generic_project.beans.ConstantField;
import org.openspcoop2.generic_project.beans.CustomField;
import org.openspcoop2.generic_project.beans.Function;
import org.openspcoop2.generic_project.beans.FunctionField;
import org.openspcoop2.generic_project.beans.NonNegativeNumber;
import org.openspcoop2.generic_project.beans.Union;
import org.openspcoop2.generic_project.beans.UnionExpression;
import org.openspcoop2.generic_project.exception.ExpressionException;
import org.openspcoop2.generic_project.exception.ExpressionNotImplementedException;
import org.openspcoop2.generic_project.exception.NotFoundException;
import org.openspcoop2.generic_project.exception.NotImplementedException;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IExpression;
import org.openspcoop2.generic_project.expression.IPaginatedExpression;
import org.openspcoop2.generic_project.expression.SortOrder;

import it.govpay.bd.BasicBD;
import it.govpay.bd.ConnectionManager;
import it.govpay.bd.GovpayConfig;
import it.govpay.bd.model.reportistica.converter.PagamentoConverter;
import it.govpay.bd.reportistica.filters.PagamentoFilter;
import it.govpay.model.Versamento;
import it.govpay.model.SingoloVersamento.StatoSingoloVersamento;
import it.govpay.model.reportistica.Pagamento;
import it.govpay.orm.dao.IPagamentoService;
import it.govpay.orm.dao.jdbc.converter.PagamentoFieldConverter;

public class PagamentiBD extends BasicBD{

	public static final String ALIAS_DATA_PAGAMENTO = "dataPagamento";
	public static final String ALIAS_COD_VERSAMENTO_ENTE = "codVersamentoEnte";
	public static final String ALIAS_CODICE_FISCALE_DEBITORE = "codiceFiscaleDebitore";
	public static final String ALIAS_CAUSALE = "causale";
	public static final String ALIAS_STATO_VERSAMENTO = "statoVersamento";
	public static final String ALIAS_IMPORTO_DOVUTO = "importoDovuto";
	public static final String ALIAS_IMPORTO_PAGATO = "importoPagato";
	public static final String ALIAS_IUV = "iuv";
	public static final String ALIAS_COD_SINGOLO_VERSAMENTO_ENTE = "codSingoloVersamentoEnte";
	public static final String ALIAS_ID = "id";

	public static final Long DEFAULT_VALUE_LONG = 0L;
	public static final Date DEFAULT_VALUE_DATE = new Date();
	public static final Double DEFAULT_VALUE_DOUBLE = 0D;
	public static final String DEFAULT_VALUE_STRING = "gbyFake";
	
	private static final int LIMIT = 50;

	public PagamentiBD(BasicBD basicBD) {
		super(basicBD);
	}

	public PagamentoFilter newFilter() throws ServiceException {
		return new PagamentoFilter(this.getPagamentoService());
	}

	public List<Pagamento> findAll(PagamentoFilter filter) throws ServiceException {
		try {
			PagamentoFieldConverter pagamentoFieldConverter = new PagamentoFieldConverter(ConnectionManager.getJDBCServiceManagerProperties().getDatabase()); 

			IPagamentoService pagamentoService = this.getPagamentoService();
			// applico i filtri tranne ordinamento e paginazione
			IPaginatedExpression pagExpr = pagamentoService.toPaginatedExpression(filter.toExpression());

			// forzo la join con singoliversamenti
			pagExpr.isNotNull(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.COD_SINGOLO_VERSAMENTO_ENTE); 

			CustomField idVersamentoField = new CustomField(PagamentiBD.ALIAS_ID, Long.class, PagamentiBD.ALIAS_ID,
					pagamentoFieldConverter.toTable(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO));
			pagExpr.addGroupBy(idVersamentoField);
			// ordinamento parziale
			pagExpr.addOrder(idVersamentoField, SortOrder.DESC);

			UnionExpression unionExpr = new UnionExpression(pagExpr);
			unionExpr.addSelectField(idVersamentoField, PagamentiBD.ALIAS_ID);

			IExpression fakeExpr = pagamentoService.newPaginatedExpression();
			UnionExpression unionExprFake = new UnionExpression(fakeExpr);
			unionExprFake.addSelectField(new ConstantField(PagamentiBD.ALIAS_ID, DEFAULT_VALUE_LONG, Long.class), PagamentiBD.ALIAS_ID);

			Union union = new Union();
			union.setUnionAll(true);
			union.addField(PagamentiBD.ALIAS_ID);
			union.addGroupBy(PagamentiBD.ALIAS_ID);

			union.addOrderBy(PagamentiBD.ALIAS_DATA_PAGAMENTO,SortOrder.DESC);
			union.addField(PagamentiBD.ALIAS_DATA_PAGAMENTO, Function.MAX, PagamentiBD.ALIAS_DATA_PAGAMENTO);
			union.addField(PagamentiBD.ALIAS_COD_VERSAMENTO_ENTE, Function.MAX, PagamentiBD.ALIAS_COD_VERSAMENTO_ENTE);
			union.addField(PagamentiBD.ALIAS_CODICE_FISCALE_DEBITORE, Function.MAX, PagamentiBD.ALIAS_CODICE_FISCALE_DEBITORE);
			union.addField(PagamentiBD.ALIAS_CAUSALE, Function.MAX, PagamentiBD.ALIAS_CAUSALE);
			union.addField(PagamentiBD.ALIAS_STATO_VERSAMENTO, Function.MAX, PagamentiBD.ALIAS_STATO_VERSAMENTO);
			union.addField(PagamentiBD.ALIAS_IMPORTO_DOVUTO, Function.MAX, PagamentiBD.ALIAS_IMPORTO_DOVUTO);
			union.addField(PagamentiBD.ALIAS_IMPORTO_PAGATO, Function.MAX, PagamentiBD.ALIAS_IMPORTO_PAGATO);
			union.addField(PagamentiBD.ALIAS_IUV, Function.MAX, PagamentiBD.ALIAS_IUV);
			//			union.addField(PagamentiBD.ALIAS_COD_SINGOLO_VERSAMENTO_ENTE, Function.MAX, PagamentiBD.ALIAS_COD_SINGOLO_VERSAMENTO_ENTE);

			// Funzioni di aggragazione

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().DATA_PAGAMENTO, Function.MAX, PagamentiBD.ALIAS_DATA_PAGAMENTO));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_DATA_PAGAMENTO, PagamentiBD.DEFAULT_VALUE_DATE, it.govpay.orm.Pagamento.model().DATA_PAGAMENTO.getFieldType()),
					Function.MAX, PagamentiBD.ALIAS_DATA_PAGAMENTO));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.COD_VERSAMENTO_ENTE, Function.MAX, PagamentiBD.ALIAS_COD_VERSAMENTO_ENTE));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_COD_VERSAMENTO_ENTE, PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.COD_VERSAMENTO_ENTE.getFieldType()),
					Function.MAX, PagamentiBD.ALIAS_COD_VERSAMENTO_ENTE));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.DEBITORE_IDENTIFICATIVO, Function.MAX, PagamentiBD.ALIAS_CODICE_FISCALE_DEBITORE));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_CODICE_FISCALE_DEBITORE, PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.DEBITORE_IDENTIFICATIVO.getFieldType()),
					Function.MAX, PagamentiBD.ALIAS_CODICE_FISCALE_DEBITORE));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.CAUSALE_VERSAMENTO, Function.MAX, PagamentiBD.ALIAS_CAUSALE));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_CAUSALE, PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.CAUSALE_VERSAMENTO.getFieldType()),
					Function.MAX, PagamentiBD.ALIAS_CAUSALE));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.STATO_VERSAMENTO, Function.MAX, PagamentiBD.ALIAS_STATO_VERSAMENTO));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_STATO_VERSAMENTO, PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.STATO_VERSAMENTO.getFieldType()), 
					Function.MAX, PagamentiBD.ALIAS_STATO_VERSAMENTO));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.IMPORTO_TOTALE, Function.MAX, PagamentiBD.ALIAS_IMPORTO_DOVUTO));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_IMPORTO_DOVUTO, PagamentiBD.DEFAULT_VALUE_DOUBLE, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.IMPORTO_TOTALE.getFieldType()), 
					Function.MAX, PagamentiBD.ALIAS_IMPORTO_DOVUTO));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().IMPORTO_PAGATO, Function.SUM, PagamentiBD.ALIAS_IMPORTO_PAGATO));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_IMPORTO_PAGATO, PagamentiBD.DEFAULT_VALUE_DOUBLE, it.govpay.orm.Pagamento.model().IMPORTO_PAGATO.getFieldType()),
					Function.MAX, PagamentiBD.ALIAS_IMPORTO_PAGATO));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_RPT.IUV, Function.MAX, PagamentiBD.ALIAS_IUV));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_IUV,  PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_RPT.IUV.getFieldType()), 
					Function.MAX, PagamentiBD.ALIAS_IUV));

			//			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.COD_SINGOLO_VERSAMENTO_ENTE, Function.MAX, PagamentiBD.ALIAS_COD_SINGOLO_VERSAMENTO_ENTE));
			//			unionExprFake.addSelectFunctionField(new FunctionField(
			//					new ConstantField( PagamentiBD.ALIAS_COD_SINGOLO_VERSAMENTO_ENTE, PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.COD_SINGOLO_VERSAMENTO_ENTE.getFieldType()),
			//						Function.MAX, PagamentiBD.ALIAS_COD_SINGOLO_VERSAMENTO_ENTE));

			if(filter.getOffset() != null)
				union.setOffset(filter.getOffset());
			if(filter.getLimit()!= null)
				union.setLimit(filter.getLimit());

			List<Pagamento> lst = new ArrayList<Pagamento>();

			List<Map<String, Object>> list = this.getPagamentoService().union(union, unionExpr, unionExprFake);

			if(list!= null && list.size() > 0){
				for (Map<String, Object> map : list) {
					// elimino la entry falsa
					if(!PagamentiBD.DEFAULT_VALUE_STRING.equals(map.get(PagamentiBD.ALIAS_STATO_VERSAMENTO))){
						lst.add(PagamentoConverter.toReportisticaDTO(map));
					}
				}
			}

			return lst;
		} catch (NotFoundException e) {
			return new ArrayList<Pagamento>();
		}catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionException e) {
			throw new ServiceException(e);
		} catch (ExpressionNotImplementedException e) {
			throw new ServiceException(e);
		}
	}
	public long count(PagamentoFilter filter) throws ServiceException {
		try {
			PagamentoFieldConverter pagamentoFieldConverter = new PagamentoFieldConverter(ConnectionManager.getJDBCServiceManagerProperties().getDatabase()); 

			IPagamentoService pagamentoService = this.getPagamentoService();
			// applico i filtri tranne ordinamento e paginazione
			IPaginatedExpression pagExpr = pagamentoService.toPaginatedExpression(filter.toExpression());

			// forzo la join con singoliversamenti
			pagExpr.isNotNull(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.COD_SINGOLO_VERSAMENTO_ENTE); 

			CustomField idVersamentoField = new CustomField(PagamentiBD.ALIAS_ID, Long.class, PagamentiBD.ALIAS_ID,
					pagamentoFieldConverter.toTable(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO));
			pagExpr.addGroupBy(idVersamentoField);
			// ordinamento parziale
			pagExpr.addOrder(idVersamentoField, SortOrder.DESC);

			UnionExpression unionExpr = new UnionExpression(pagExpr);
			unionExpr.addSelectField(idVersamentoField, PagamentiBD.ALIAS_ID);

			IExpression fakeExpr = pagamentoService.newPaginatedExpression();
			UnionExpression unionExprFake = new UnionExpression(fakeExpr);
			unionExprFake.addSelectField(new ConstantField(PagamentiBD.ALIAS_ID, DEFAULT_VALUE_LONG, Long.class), PagamentiBD.ALIAS_ID);

			Union union = new Union();
			union.setUnionAll(true);
			union.addField(PagamentiBD.ALIAS_ID);

			// Funzioni di aggragazione

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().DATA_PAGAMENTO, Function.MAX, PagamentiBD.ALIAS_DATA_PAGAMENTO));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_DATA_PAGAMENTO, PagamentiBD.DEFAULT_VALUE_DATE, it.govpay.orm.Pagamento.model().DATA_PAGAMENTO.getFieldType()),
					Function.MAX, PagamentiBD.ALIAS_DATA_PAGAMENTO));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.COD_VERSAMENTO_ENTE, Function.MAX, PagamentiBD.ALIAS_COD_VERSAMENTO_ENTE));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_COD_VERSAMENTO_ENTE, PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.COD_VERSAMENTO_ENTE.getFieldType()),
					Function.MAX, PagamentiBD.ALIAS_COD_VERSAMENTO_ENTE));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.DEBITORE_IDENTIFICATIVO, Function.MAX, PagamentiBD.ALIAS_CODICE_FISCALE_DEBITORE));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_CODICE_FISCALE_DEBITORE, PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.DEBITORE_IDENTIFICATIVO.getFieldType()),
					Function.MAX, PagamentiBD.ALIAS_CODICE_FISCALE_DEBITORE));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.CAUSALE_VERSAMENTO, Function.MAX, PagamentiBD.ALIAS_CAUSALE));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_CAUSALE, PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.CAUSALE_VERSAMENTO.getFieldType()),
					Function.MAX, PagamentiBD.ALIAS_CAUSALE));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.STATO_VERSAMENTO, Function.MAX, PagamentiBD.ALIAS_STATO_VERSAMENTO));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_STATO_VERSAMENTO, PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.STATO_VERSAMENTO.getFieldType()), 
					Function.MAX, PagamentiBD.ALIAS_STATO_VERSAMENTO));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.IMPORTO_TOTALE, Function.MAX, PagamentiBD.ALIAS_IMPORTO_DOVUTO));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_IMPORTO_DOVUTO, PagamentiBD.DEFAULT_VALUE_DOUBLE, it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.ID_VERSAMENTO.IMPORTO_TOTALE.getFieldType()), 
					Function.MAX, PagamentiBD.ALIAS_IMPORTO_DOVUTO));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().IMPORTO_PAGATO, Function.SUM, PagamentiBD.ALIAS_IMPORTO_PAGATO));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_IMPORTO_PAGATO, PagamentiBD.DEFAULT_VALUE_DOUBLE, it.govpay.orm.Pagamento.model().IMPORTO_PAGATO.getFieldType()),
					Function.MAX, PagamentiBD.ALIAS_IMPORTO_PAGATO));

			unionExpr.addSelectFunctionField(new FunctionField(it.govpay.orm.Pagamento.model().ID_RPT.IUV, Function.MAX, PagamentiBD.ALIAS_IUV));
			unionExprFake.addSelectFunctionField(new FunctionField(
					new ConstantField( PagamentiBD.ALIAS_IUV,  PagamentiBD.DEFAULT_VALUE_STRING, it.govpay.orm.Pagamento.model().ID_RPT.IUV.getFieldType()), 
					Function.MAX, PagamentiBD.ALIAS_IUV));


			NonNegativeNumber count  = this.getPagamentoService().unionCount(union, unionExpr, unionExprFake);
			return count != null ? count.longValue() - 1: 0;
		} catch (NotFoundException e) {
			return 0;
		}catch (NotImplementedException e) {
			throw new ServiceException(e);
		} catch (ExpressionException e) {
			throw new ServiceException(e);
		} catch (ExpressionNotImplementedException e) {
			throw new ServiceException(e);
		}
	}
	
	public  List<Pagamento>  csvReportisticaPagamenti(List<Long> idVersamenti, Integer offset, Integer limit)throws ServiceException {
		List<Pagamento> listaPagamenti = new ArrayList<Pagamento>();
		IPagamentoService pagamentoService = this.getPagamentoService();

		List<Class<?>> listaFields = new ArrayList<Class<?>>();
		listaFields.add(it.govpay.orm.Pagamento.model().DATA_PAGAMENTO.getFieldType());
		listaFields.add(it.govpay.orm.Pagamento.model().IMPORTO_PAGATO.getFieldType());
		listaFields.add(it.govpay.orm.Pagamento.model().IUR.getFieldType());
		listaFields.add(it.govpay.orm.Pagamento.model().ID_RPT.IUV.getFieldType());
		listaFields.add(it.govpay.orm.Pagamento.model().CODFLUSSO_RENDICONTAZIONE.getFieldType());
		listaFields.add(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.COD_SINGOLO_VERSAMENTO_ENTE.getFieldType());
		listaFields.add(it.govpay.orm.Pagamento.model().ID_SINGOLO_VERSAMENTO.NOTE.getFieldType());
		listaFields.add(it.govpay.orm.FR.model().COD_BIC_RIVERSAMENTO.getFieldType());
		listaFields.add(it.govpay.orm.FR.model().IUR.getFieldType());
		listaFields.add(it.govpay.orm.Pagamento.model().IBAN_ACCREDITO.getFieldType());
		listaFields.add(Long.class);
		listaFields.add(it.govpay.orm.Versamento.model().COD_VERSAMENTO_ENTE.getFieldType());
		listaFields.add(it.govpay.orm.Versamento.model().STATO_VERSAMENTO.getFieldType());
		listaFields.add(it.govpay.orm.Versamento.model().CAUSALE_VERSAMENTO.getFieldType());
		listaFields.add(it.govpay.orm.Versamento.model().DEBITORE_IDENTIFICATIVO.getFieldType());
		listaFields.add(it.govpay.orm.SingoloVersamento.model().IMPORTO_SINGOLO_VERSAMENTO.getFieldType());
		listaFields.add(it.govpay.orm.SingoloVersamento.model().STATO_SINGOLO_VERSAMENTO.getFieldType());

		List<List<Object>> select = new ArrayList<List<Object>>();
		try {
			List<Object> listaParam = new ArrayList<Object>();
			listaParam.addAll(idVersamenti);
			listaParam.add(offset);
			
			if(GovpayConfig.getInstance().getDatabaseType().equals("oracle")) {
				listaParam.add(offset+limit);
			} else {
				listaParam.add(limit);
			}
			
			String nativeQuery = GovpayConfig.getInstance().getNativeQuery(it.govpay.bd.pagamento.PagamentiBD.NOME_QUERY_CSV_VERSAMENTI);
			
			StringBuilder sb = new StringBuilder();
			
			for (int i=0; i < idVersamenti.size() ; i++) {
				if(sb.length() > 0)
					sb.append(",");
				
				sb.append("?");
			}
			
			nativeQuery = nativeQuery.replace(it.govpay.bd.pagamento.PagamentiBD.PLACE_HOLDER_QUERY_ESTRATTI_CONTO_PER_VERSAMENTI, sb.toString());
			
			select = pagamentoService.nativeQuery(nativeQuery, listaFields, listaParam.toArray());
			if(select != null && select.size() > 0){
				for (List<Object> list : select) {
					Pagamento pagamento = new Pagamento();
					pagamento.setDataPagamento((Date) list.get(0));
					pagamento.setImportoPagato(new BigDecimal((Double) list.get(1)));
					pagamento.setIur((String) list.get(2));
					pagamento.setIuv((String) list.get(3));
					pagamento.setCodFlussoRendicontazione((String) list.get(4));
					pagamento.setCodSingoloVersamentoEnte((String) list.get(5));
					pagamento.setNote((String) list.get(6));
					pagamento.setCodBicRiversamento((String) list.get(7));
					pagamento.setIdRegolamento((String) list.get(8));
					pagamento.setIbanAccredito((String) list.get(9));
					pagamento.setId((Long) list.get(10));
					pagamento.setCodVersamentoEnte((String) list.get(11));
					pagamento.setStatoSingoloVersamento(StatoSingoloVersamento.valueOf((String) list.get(12)));
					pagamento.setCausale(Versamento.decode((String) list.get(13)).getSimple());
					pagamento.setDebitoreIdentificativo((String) list.get(14));
					pagamento.setImportoDovuto(new BigDecimal((Double) list.get(15)));
					pagamento.setStatoSingoloVersamento(StatoSingoloVersamento.valueOf((String) list.get(16)));
					
					listaPagamenti.add(pagamento); 
				}
			}
		} catch (NotFoundException e) {
		} catch (Exception e) {
			throw new ServiceException(e);
		}
		return listaPagamenti;
	}

	public List<Pagamento> creaCsvReportisticaPagamenti(PagamentoFilter filter) throws ServiceException {
		try {
			int offset = 0;
			List<Pagamento> lstRet = new ArrayList<Pagamento>();
			
			List<Pagamento> lst = this.csvReportisticaPagamenti(filter.getIdPagamento(), offset, LIMIT);
			
			
			while(lst != null && !lst.isEmpty()) {
				lstRet.addAll(lst);
				
				offset += lst.size();
				lst = this.csvReportisticaPagamenti(filter.getIdPagamento(), offset, LIMIT);
			}
	 
			return lstRet;
		} catch (ServiceException e) {
			throw e;
		}catch (Exception e) {
			throw new ServiceException(e);
		}
	}


	public Pagamento getPagamento(long id) throws ServiceException {
		return null;
	}

}

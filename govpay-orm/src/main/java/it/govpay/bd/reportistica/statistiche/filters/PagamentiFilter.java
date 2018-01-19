package it.govpay.bd.reportistica.statistiche.filters;

import org.apache.commons.lang.NotImplementedException;
import org.openspcoop2.generic_project.dao.IExpressionConstructor;
import org.openspcoop2.generic_project.exception.ServiceException;
import org.openspcoop2.generic_project.expression.IExpression;

public class PagamentiFilter extends StatisticaFilter {

	public PagamentiFilter(IExpressionConstructor expressionConstructor) {
		super(expressionConstructor);
	}
	
	private String codPsp;
	private String codUo;
	private String codTributo;
	
	public String getCodPsp() {
		return codPsp;
	}
	public void setCodPsp(String codPsp) {
		this.codPsp = codPsp;
	}
	public String getCodUo() {
		return codUo;
	}
	public void setCodUo(String codUo) {
		this.codUo = codUo;
	}
	public String getCodTributo() {
		return codTributo;
	}
	public void setCodTributo(String codTributo) {
		this.codTributo = codTributo;
	}
	@Override
	public IExpression _toExpression() throws ServiceException {
		throw new ServiceException(new NotImplementedException());
	}
}

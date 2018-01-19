package it.govpay.model.reportistica.statistiche;

import java.io.Serializable;
import java.util.Date;

public class AndamentoTemporale implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L; 
	private Date data;
	private long numero;
	
	public AndamentoTemporale(Date data, Long numero) {
		this.data = data;
		this.numero = numero;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public long getNumero() {
		return numero;
	}

	public void setNumero(long numero) {
		this.numero = numero;
	}

}

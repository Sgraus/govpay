package it.govpay.core.business.model;

import it.govpay.model.avvisi.AvvisoPagamento;

public class LeggiAvvisoDTOResponse {

	private AvvisoPagamento avviso;

	public AvvisoPagamento getAvviso() {
		return this.avviso;
	}

	public void setAvviso(AvvisoPagamento avviso) {
		this.avviso = avviso;
	}
}

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
package it.govpay.orm.model;

import it.govpay.orm.FR;

import org.openspcoop2.generic_project.beans.AbstractModel;
import org.openspcoop2.generic_project.beans.IField;
import org.openspcoop2.generic_project.beans.Field;
import org.openspcoop2.generic_project.beans.ComplexField;


/**     
 * Model FR 
 *
 * @author Giovanni Bussu (bussu@link.it)
 * @author Lorenzo Nardi (nardi@link.it)
 * @author $Author$
 * @version $Rev$, $Date$
 */
public class FRModel extends AbstractModel<FR> {

	public FRModel(){
	
		super();
	
		this.COD_PSP = new Field("codPsp",java.lang.String.class,"FR",FR.class);
		this.COD_DOMINIO = new Field("codDominio",java.lang.String.class,"FR",FR.class);
		this.COD_FLUSSO = new Field("codFlusso",java.lang.String.class,"FR",FR.class);
		this.STATO = new Field("stato",java.lang.String.class,"FR",FR.class);
		this.DESCRIZIONE_STATO = new Field("descrizioneStato",java.lang.String.class,"FR",FR.class);
		this.IUR = new Field("iur",java.lang.String.class,"FR",FR.class);
		this.DATA_ORA_FLUSSO = new Field("dataOraFlusso",java.util.Date.class,"FR",FR.class);
		this.DATA_REGOLAMENTO = new Field("dataRegolamento",java.util.Date.class,"FR",FR.class);
		this.DATA_ACQUISIZIONE = new Field("dataAcquisizione",java.util.Date.class,"FR",FR.class);
		this.NUMERO_PAGAMENTI = new Field("numeroPagamenti",long.class,"FR",FR.class);
		this.IMPORTO_TOTALE_PAGAMENTI = new Field("importoTotalePagamenti",java.lang.Double.class,"FR",FR.class);
		this.COD_BIC_RIVERSAMENTO = new Field("codBicRiversamento",java.lang.String.class,"FR",FR.class);
		this.XML = new Field("xml",byte[].class,"FR",FR.class);
		this.ID_PAGAMENTO = new it.govpay.orm.model.IdPagamentoModel(new Field("idPagamento",it.govpay.orm.IdPagamento.class,"FR",FR.class));
	
	}
	
	public FRModel(IField father){
	
		super(father);
	
		this.COD_PSP = new ComplexField(father,"codPsp",java.lang.String.class,"FR",FR.class);
		this.COD_DOMINIO = new ComplexField(father,"codDominio",java.lang.String.class,"FR",FR.class);
		this.COD_FLUSSO = new ComplexField(father,"codFlusso",java.lang.String.class,"FR",FR.class);
		this.STATO = new ComplexField(father,"stato",java.lang.String.class,"FR",FR.class);
		this.DESCRIZIONE_STATO = new ComplexField(father,"descrizioneStato",java.lang.String.class,"FR",FR.class);
		this.IUR = new ComplexField(father,"iur",java.lang.String.class,"FR",FR.class);
		this.DATA_ORA_FLUSSO = new ComplexField(father,"dataOraFlusso",java.util.Date.class,"FR",FR.class);
		this.DATA_REGOLAMENTO = new ComplexField(father,"dataRegolamento",java.util.Date.class,"FR",FR.class);
		this.DATA_ACQUISIZIONE = new ComplexField(father,"dataAcquisizione",java.util.Date.class,"FR",FR.class);
		this.NUMERO_PAGAMENTI = new ComplexField(father,"numeroPagamenti",long.class,"FR",FR.class);
		this.IMPORTO_TOTALE_PAGAMENTI = new ComplexField(father,"importoTotalePagamenti",java.lang.Double.class,"FR",FR.class);
		this.COD_BIC_RIVERSAMENTO = new ComplexField(father,"codBicRiversamento",java.lang.String.class,"FR",FR.class);
		this.XML = new ComplexField(father,"xml",byte[].class,"FR",FR.class);
		this.ID_PAGAMENTO = new it.govpay.orm.model.IdPagamentoModel(new ComplexField(father,"idPagamento",it.govpay.orm.IdPagamento.class,"FR",FR.class));
	
	}
	
	

	public IField COD_PSP = null;
	 
	public IField COD_DOMINIO = null;
	 
	public IField COD_FLUSSO = null;
	 
	public IField STATO = null;
	 
	public IField DESCRIZIONE_STATO = null;
	 
	public IField IUR = null;
	 
	public IField DATA_ORA_FLUSSO = null;
	 
	public IField DATA_REGOLAMENTO = null;
	 
	public IField DATA_ACQUISIZIONE = null;
	 
	public IField NUMERO_PAGAMENTI = null;
	 
	public IField IMPORTO_TOTALE_PAGAMENTI = null;
	 
	public IField COD_BIC_RIVERSAMENTO = null;
	 
	public IField XML = null;
	 
	public it.govpay.orm.model.IdPagamentoModel ID_PAGAMENTO = null;
	 

	@Override
	public Class<FR> getModeledClass(){
		return FR.class;
	}
	
	@Override
	public String toString(){
		if(this.getModeledClass()!=null){
			return this.getModeledClass().getName();
		}else{
			return "N.D.";
		}
	}

}
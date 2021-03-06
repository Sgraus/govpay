/**
 * 
 */
package it.govpay.rs.v1.beans.converter;

import it.govpay.core.rs.v1.beans.base.Nota;
import it.govpay.core.rs.v1.beans.base.TipoNota;

/**
 * @author Bussu Giovanni (bussu@link.it)
 * @author  $Author: bussu $
 * @version $ Rev: 12563 $, $Date: 28 giu 2018 $
 * 
 */
public class NoteConverter {

	/**
	 * @param nota
	 * @return
	 */
	public static Nota toRsModel(it.govpay.bd.model.Nota nota) {
		Nota rsModel = new Nota();
		rsModel.setAutore(nota.getAutore());
		rsModel.setData(nota.getData());
		rsModel.setTesto(nota.getTesto());
		rsModel.setPrincipal(nota.getPrincipal());
		rsModel.setOggetto(nota.getOggetto());
		rsModel.setTipo(TipoNota.fromValue(nota.getTipo().toString()));
		return rsModel;
	}

}

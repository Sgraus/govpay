
package it.govpay.core.rs.v1.beans;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class Soggetto extends it.govpay.core.rs.v1.beans.base.Soggetto {

	private static JsonConfig jsonConfig = new JsonConfig();

	static {
		jsonConfig.setRootClass(Soggetto.class);
	}
	public Soggetto() {}
	
	@Override
	public String getJsonIdFilter() {
		return "soggettoVersante";
	}
	
	public static Soggetto parse(String json) {
		JSONObject jsonObject = JSONObject.fromObject( json );  
		return (Soggetto) JSONObject.toBean( jsonObject, jsonConfig );
	}

}

package it.govpay.web.rs.utils;

import it.govpay.bd.BasicBD;
import it.govpay.web.rs.Caricatore;
import it.govpay.web.rs.model.EstrattoContoRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.Logger;

public class PagamentoUtils {
	
	public static List<String> datePatterns = null;
	
	public static SimpleDateFormat sdf = new SimpleDateFormat(DateFormatUtils.ISO_DATE_FORMAT.getPattern());
	
	static {
		
		datePatterns = new ArrayList<String>();
		datePatterns.add(DateFormatUtils.ISO_DATE_FORMAT.getPattern());
		datePatterns.add(DateFormatUtils.ISO_DATETIME_FORMAT.getPattern());
		datePatterns.add(DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.getPattern());
		datePatterns.add(DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
	}

	public static EstrattoContoRequest readEstrattoContoRequestFromRequest(Caricatore c, Logger log,InputStream is, UriInfo uriInfo, HttpHeaders httpHeaders,String methodName) throws WebApplicationException,Exception{
		String nomeMetodo = "readEstrattoContoRequestFromRequest";
		EstrattoContoRequest entry = null;
		ByteArrayOutputStream baos = null;
		try{
			log.info("Esecuzione " + nomeMetodo + " in corso...");
			
			JsonConfig jsonConfig = new JsonConfig();
			baos = c.logRequest(uriInfo, httpHeaders, methodName,is);
		
			JSONObject jsonObject = JSONObject.fromObject( baos.toString() );  
			jsonConfig.setRootClass(EstrattoContoRequest.class);
			entry = (EstrattoContoRequest) JSONObject.toBean( jsonObject, jsonConfig );
			
			String []datPat = datePatterns.toArray(new String[datePatterns.size()]);
			if(jsonObject.getString("dataInizio") != null)
				entry.setDataInizio(DateUtils.parseDate(jsonObject.getString("dataInizio"), datPat));
			
			if(jsonObject.getString("dataFine") != null)
				entry.setDataFine(DateUtils.parseDate(jsonObject.getString("dataFine"), datPat));
			
			log.debug("Lettura del versamento dall'inputStream completata.");
			
			log.info("Esecuzione " + nomeMetodo + " completata.");
			return entry;
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw e;
		} finally {
			if(baos != null)
				baos.close();
		}
	}
	
	public static void readGetRequest(Caricatore c, Logger log,UriInfo uriInfo, HttpHeaders httpHeaders,String methodName) throws WebApplicationException,Exception{
		String nomeMetodo = "readGetRequest: " +  methodName;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try{
			log.info("Esecuzione " + nomeMetodo + " in corso...");
		
			
			c.logRequest(uriInfo, httpHeaders, methodName,baos);

			log.info("Esecuzione " + nomeMetodo + " completata.");
		}catch(WebApplicationException e){
			throw e;
		}catch(Exception e){
			throw e;
		}finally {
			if(baos != null)
				baos.close();
		}
	}
	
	public static ByteArrayOutputStream writeListaEstrattoContoResponse(Caricatore c, Logger log, List<String> response, UriInfo uriInfo, HttpHeaders httpHeaders,BasicBD bd,String methodName) throws Exception{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String nomeMetodo = "writeListaEstrattoContoResponse";

		try{
			log.info("Esecuzione " + nomeMetodo + " in corso...");

			JsonConfig jsonConfig = new JsonConfig();
//			jsonConfig.setRootClass(String.class);
//			jsonConfig.setCollectionType(List.class);
			JSONArray jsonArray = JSONArray.fromObject( response , jsonConfig);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());

			IOUtils.copy(bais, baos);

			baos.flush();
			baos.close();

			c.logResponse(uriInfo, httpHeaders, methodName, baos);
			
			log.info("Esecuzione " + nomeMetodo + " completata.");
			return baos;

		}catch(Exception e){
			throw e;
		} finally {
			if(baos != null)
				baos.close();
		}
	}
	
	public static ByteArrayOutputStream writeScaricaEstrattoContoResponse(Caricatore c, Logger log, InputStream response, UriInfo uriInfo, HttpHeaders httpHeaders,BasicBD bd,String methodName) throws Exception{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String nomeMetodo = "writeScaricaEstrattoContoResponse";

		try{
			log.info("Esecuzione " + nomeMetodo + " in corso...");
			IOUtils.copy(response, baos);
			
			baos.flush();

			c.logResponse(uriInfo, httpHeaders, methodName, baos);
			
			log.info("Esecuzione " + nomeMetodo + " completata.");
			return baos;

		}catch(Exception e){
			throw e;
		} finally {
			if(response != null)
				response.close();
			
			if(baos != null)
				baos.close();
		}
	}

	public static ByteArrayOutputStream writeEstrattoContoResponse(
			Caricatore c, Logger log,
			List<it.govpay.bd.model.EstrattoConto> response, UriInfo uriInfo,
			HttpHeaders httpHeaders, BasicBD bd, String methodName) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String nomeMetodo = "writeListaEstrattoContoResponse";

		try{
			log.info("Esecuzione " + nomeMetodo + " in corso...");

			JsonConfig jsonConfig = new JsonConfig();
			JSONArray jsonArray = JSONArray.fromObject( response , jsonConfig);
			
			ByteArrayInputStream bais = new ByteArrayInputStream(jsonArray.toString().getBytes());

			IOUtils.copy(bais, baos);

			baos.flush();
			baos.close();

			c.logResponse(uriInfo, httpHeaders, methodName, new ByteArrayOutputStream());
			
			log.info("Esecuzione " + nomeMetodo + " completata.");
			return baos;

		}catch(Exception e){
			throw e;
		} finally {
			if(baos != null)
				baos.close();
		}
	}

}

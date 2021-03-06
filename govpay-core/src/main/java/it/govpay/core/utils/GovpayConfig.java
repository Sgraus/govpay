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
package it.govpay.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.openspcoop2.utils.LoggerWrapperFactory;
import org.openspcoop2.utils.TipiDatabase;
import org.openspcoop2.utils.logger.constants.Severity;
import org.openspcoop2.utils.logger.log4j.Log4JLoggerWithProxyContext;
import org.slf4j.Logger;

import it.govpay.core.business.IConservazione;
import it.govpay.core.utils.client.handler.IntegrationOutHandler;

public class GovpayConfig {

	public enum VersioneAvviso {
		v001, v002;
	}

	public static final String PROPERTIES_FILE = "/govpay.properties";
	public static final String GOVPAY_BACKOFFICE_OPEN_API_FILE = "/govpay-api-backoffice.json";
	public static final String GOVPAY_BACKOFFICE_OPEN_API_FILE_NAME = "back_office";
	public static final String GOVPAY_PAGAMENTI_OPEN_API_FILE = "/govpay-api-pagamento.yaml";
	public static final String GOVPAY_PAGAMENTI_OPEN_API_FILE_NAME = "pagamenti";
	public static final String MSG_DIAGNOSTICI_PROPERTIES_FILE = "/msgDiagnostici.properties";
	public static final String LOG4J2_XML_FILE = "/log4j2.xml";

	private static GovpayConfig instance;

	public static GovpayConfig getInstance() {
		return instance;
	}

	public static GovpayConfig newInstance(InputStream is) throws Exception {
		instance = new GovpayConfig(is);
		return instance;
	}

	private List<String> outHandlers;

	private URI log4j2Config;
	private URL urlPddVerifica;
	private String resourceDir;
	private VersioneAvviso versioneAvviso;
	private int dimensionePool;
	private String ksLocation, ksPassword, ksAlias;
	private String mLogClass, mLogDS;
	private Severity mLogLevel;
	private TipiDatabase mLogDBType;
	private boolean mLogOnLog4j, mLogOnDB, mLogSql, pddAuthEnable;
	private boolean batchOn;
	private Integer clusterId;
	private long timeoutBatch;

	private boolean batchAvvisiPagamento;
	private boolean batchAvvisaturaDigitale;
	private boolean batchCaricamentoTracciati;
	private boolean timeoutPendenti;
	private Integer timeoutPendentiMins;
	
	private Properties[] props;
	private IConservazione conservazionePlugin;
	
	private String urlGovpayWC = null;
	private String urlWISP = null;
	
	private boolean validazioneAPIRest;
	private boolean dumpAPIRestGETResponse;
	
	private String appName;
	private String ambienteDeploy;

	public GovpayConfig(InputStream is) throws Exception {
		// Default values:
		this.versioneAvviso = VersioneAvviso.v002;
		this.dimensionePool = 10;
		this.log4j2Config = null;
		this.urlPddVerifica = null;
		this.ksAlias = null;
		this.ksLocation = null;
		this.ksPassword = null;
		this.mLogClass = Log4JLoggerWithProxyContext.class.getName();
		this.mLogLevel = Severity.INFO;
		this.mLogOnDB = false;
		this.mLogOnLog4j = true;
		this.mLogSql = false;
		this.mLogDBType = null;
		this.mLogDS = null;
		this.batchOn=true;
		this.pddAuthEnable = true;
		this.validazioneAPIRest = false;
		this.dumpAPIRestGETResponse = false;
		this.batchAvvisaturaDigitale = false;
		this.batchCaricamentoTracciati = false;
		this.timeoutPendenti = false;
		this.timeoutPendentiMins = null;
		
		this.appName = null;
		this.ambienteDeploy = null;

		try {

			// Recupero il property all'interno dell'EAR
			this.props = new Properties[2];
			Properties props1 = new Properties();
			props1.load(is);
			this.props[1] = props1;

			// Recupero la configurazione della working dir
			// Se e' configurata, la uso come prioritaria

			try {
				this.resourceDir = getProperty("it.govpay.resource.path", props1, false, true, null);

				if(this.resourceDir != null) {
					File resourceDirFile = new File(this.resourceDir);
					if(!resourceDirFile.isDirectory())
						throw new Exception("Il path indicato nella property \"it.govpay.resource.path\" (" + this.resourceDir + ") non esiste o non e' un folder.");

					File log4j2ConfigFile = new File(this.resourceDir + File.separatorChar + "log4j2.xml");

					if(log4j2ConfigFile.exists()) {
						this.log4j2Config = log4j2ConfigFile.toURI();
					}
				}
			} catch (Exception e) {
				LoggerWrapperFactory.getLogger("boot").warn("Errore di inizializzazione: " + e.getMessage() + ". Property ignorata.");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public void readProperties() throws Exception {
		Logger log = LoggerWrapperFactory.getLogger("boot");
		try {
			Properties props0 = null;
			this.props[0] = props0;

			File gpConfigFile = new File(this.resourceDir + File.separatorChar + "govpay.properties");
			if(gpConfigFile.exists()) {
				props0 = new Properties();
				props0.load(new FileInputStream(gpConfigFile));
				log.info("Individuata configurazione prioritaria: " + gpConfigFile.getAbsolutePath());
				this.props[0] = props0;
			}

			try {
				String versioneAvvisoProperty = getProperty("it.govpay.avviso.versione", this.props, false, log);
				if(versioneAvvisoProperty != null && !versioneAvvisoProperty.trim().isEmpty()) {
					try {
						this.versioneAvviso = VersioneAvviso.valueOf(versioneAvvisoProperty.trim());
					} catch (Exception e) {
						throw new Exception("Valore della property \"it.govpay.avviso.versione\" non consetito [" + versioneAvvisoProperty + "]. Valori ammessi: " + Arrays.toString(VersioneAvviso.values()));
					}
				}
			} catch (Exception e) {
				log.warn("Errore di inizializzazione: " + e.getMessage() + ". Assunto valore di default: " + VersioneAvviso.v002);
				this.versioneAvviso = VersioneAvviso.v002;
			}

			try {
				String dimensionePoolProperty = getProperty("it.govpay.thread.pool", this.props, false, log);
				if(dimensionePoolProperty != null && !dimensionePoolProperty.trim().isEmpty()) {
					try {
						this.dimensionePool = Integer.parseInt(dimensionePoolProperty.trim());
					} catch (Exception e) {
						throw new Exception("Valore della property \"it.govpay.thread.pool\" non e' un numero intero");
					}
				}
			} catch (Exception e) {
				log.warn("Errore di inizializzazione: " + e.getMessage() + ". Assunto valore di default: " + 10);
				this.dimensionePool = 10;
			}

			String urlPddVerificaProperty = getProperty("it.govpay.check.urlVerificaPDD", this.props, false, log);

			if(urlPddVerificaProperty != null) {
				try {
					this.urlPddVerifica = new URL(urlPddVerificaProperty.trim());
				} catch (Exception e) {
					log.warn("Valore ["+urlPddVerificaProperty.trim()+"] non consentito per la property \"it.govpay.check.urlVerificaPDD\": " +e.getMessage());
				}
			}

			String mLogClassString = getProperty("it.govpay.mlog.class", this.props, false, log);
			if(mLogClassString != null && !mLogClassString.isEmpty()) 
				this.mLogClass = mLogClassString;

			String mLogOnLog4jString = getProperty("it.govpay.mlog.log4j", this.props, false, log);
			if(mLogOnLog4jString != null && !Boolean.valueOf(mLogOnLog4jString))
				this.mLogOnLog4j = false;


			String mLogOnLevelString = getProperty("it.govpay.mlog.level", this.props, false, log);
			try {
				this.mLogLevel = Severity.valueOf(mLogOnLevelString);
			} catch (Exception e) {
				log.warn("Valore ["+mLogOnLevelString+"] non consentito per la property \"it.govpay.mlog.level\". Assunto valore di default \"INFO\".");
			}

			String mLogOnDBString = getProperty("it.govpay.mlog.db", this.props, false, log);
			if(mLogOnDBString != null && Boolean.valueOf(mLogOnDBString))
				this.mLogOnDB = true;

			if(this.mLogOnDB) {
				String mLogDBTypeString = getProperty("it.govpay.mlog.db.type", this.props, true, log);
				try {
					this.mLogDBType = TipiDatabase.valueOf(mLogDBTypeString);
				} catch (IllegalArgumentException e) {
					throw new Exception("Valore ["+mLogDBTypeString.trim()+"] non consentito per la property \"it.govpay.mlog.db.type\": " +e.getMessage());
				}

				this.mLogDS = getProperty("it.govpay.mlog.db.ds", this.props, true, log);

				String mLogSqlString = getProperty("it.govpay.mlog.showSql", this.props, false, log);
				if(mLogSqlString != null)
					this.mLogOnLog4j = Boolean.valueOf(mLogSqlString);
			}

			String pddAuthEnableString = getProperty("it.govpay.pdd.auth", this.props, false, log);
			if(pddAuthEnableString != null && pddAuthEnableString.equalsIgnoreCase("false"))
				this.pddAuthEnable = false;

			String listaHandlers = getProperty("it.govpay.integration.client.out", this.props, false, log);

			this.outHandlers = new ArrayList<>();

			if(listaHandlers != null && !listaHandlers.isEmpty()) {
				String[] splitHandlers = listaHandlers.split(",");
				for(String handler: splitHandlers) {
					String handlerClass = getProperty("it.govpay.integration.client.out."+handler, this.props, true, log);
					Class<?> c = null;
					try {
						c = this.getClass().getClassLoader().loadClass(handlerClass);
					} catch (ClassNotFoundException e) {
						throw new Exception("La classe ["+handlerClass+"] specificata per l'handler ["+handler+"] non e' presente nel classpath");
					}
					Object instance = c.newInstance();
					if(!(instance instanceof IntegrationOutHandler)) {
						throw new Exception("La classe ["+handlerClass+"] specificata per l'handler ["+handler+"] deve implementare l'interfaccia " + IntegrationOutHandler.class.getName());
					}
					this.outHandlers.add(handlerClass);
				}
			}

			String batchOnString = getProperty("it.govpay.batchOn", this.props, false, log);
			if(batchOnString != null && batchOnString.equalsIgnoreCase("false"))
				this.batchOn = false;

			String clusterIdString = getProperty("it.govpay.clusterId", this.props, false, log);
			if(clusterIdString != null) {
				try{
					this.clusterId = Integer.parseInt(clusterIdString);
				} catch(NumberFormatException nfe) {
					log.warn("La proprieta \"it.govpay.clusterId\" deve essere valorizzata con un numero. Proprieta ignorata");
				}
			}

			String timeoutBatchString = getProperty("it.govpay.timeoutBatch", this.props, false, log);
			try{
				this.timeoutBatch = Integer.parseInt(timeoutBatchString) * 1000;
			} catch(Throwable t) {
				log.info("Proprieta \"it.govpay.timeoutBatch\" impostata com valore di default (5 minuti)");
				this.timeoutBatch = 5 * 60 * 1000;
			}
			
			String conservazionePluginString = getProperty("it.govpay.plugin.conservazione", this.props, false, log);
			
			if(conservazionePluginString != null && !conservazionePluginString.isEmpty()) {
				Class<?> c = null;
				try {
					c = this.getClass().getClassLoader().loadClass(conservazionePluginString);
				} catch (ClassNotFoundException e) {
					throw new Exception("La classe ["+conservazionePluginString+"] specificata per plugin di conservazione non e' presente nel classpath");
				}
				Object instance = c.newInstance();
				if(!(instance instanceof IConservazione)) {
					throw new Exception("La classe ["+conservazionePluginString+"] specificata per plugin di conservazione deve implementare l'interfaccia " + IConservazione.class.getName());
				}
				this.conservazionePlugin = (IConservazione) instance;
			}
			
			this.urlGovpayWC = getProperty("it.govpay.wc.url", this.props, false, log);
			this.urlWISP = getProperty("it.govpay.wisp.url", this.props, false, log);
			

			String batchAvvisiPagamentoStampaAvvisiString = getProperty("it.govpay.batch.avvisiPagamento.stampaAvvisiPagamento", this.props, false, log);
			if(batchAvvisiPagamentoStampaAvvisiString != null && Boolean.valueOf(batchAvvisiPagamentoStampaAvvisiString))
				this.batchAvvisiPagamento = true;
			
			String batchAvvisaturaDigitaleString = getProperty("it.govpay.batch.avvisaturaDigitale.enabled", this.props, false, log);
			if(batchAvvisaturaDigitaleString != null && Boolean.valueOf(batchAvvisaturaDigitaleString))
				this.batchAvvisaturaDigitale = true;
			
			String batchCaricamentoTracciatiString = getProperty("it.govpay.batch.caricamentoTracciati.enabled", this.props, false, log);
			if(batchCaricamentoTracciatiString != null && Boolean.valueOf(batchCaricamentoTracciatiString))
				this.batchCaricamentoTracciati = true;
			
			String validazioneAPIRestString = getProperty("it.govpay.rs.validazione.enabled", this.props, false, log);
			if(validazioneAPIRestString != null && Boolean.valueOf(validazioneAPIRestString))
				this.validazioneAPIRest = true;
			
			String dumpAPIRestGETResponseMessageString = getProperty("it.govpay.rs.dumpGetResponse.enabled", this.props, false, log);
			if(dumpAPIRestGETResponseMessageString != null && Boolean.valueOf(dumpAPIRestGETResponseMessageString))
				this.dumpAPIRestGETResponse = true;
			
			String timeoutPendentiString = getProperty("it.govpay.modello3.timeoutPagamento", props, false, log);
			if(timeoutPendentiString != null && !timeoutPendentiString.equalsIgnoreCase("false")) {
				try{
					this.timeoutPendentiMins = Integer.parseInt(timeoutPendentiString);
					this.timeoutPendenti = true;
				} catch(NumberFormatException nfe) {
					log.warn("La proprieta \"it.govpay.modello3.timeoutPagamento\" deve essere valorizzata a `false` o con un numero. Utilizzato valore di default `false`");
				}
			}
			
			this.appName = getProperty("it.govpay.backoffice.gui.appName", this.props, false, log);
			this.ambienteDeploy = getProperty("it.govpay.backoffice.gui.ambienteDeploy", this.props, false, log);
			
		} catch (Exception e) {
			log.error("Errore di inizializzazione: " + e.getMessage());
			throw e;
		}
	}

	public URL getUrlPddVerifica() {
		return this.urlPddVerifica;
	}

	private static String getProperty(String name, Properties props, boolean required, boolean fromInternalConfig, Logger log) throws Exception {
		String value = System.getProperty(name);

		if(value != null && value.trim().isEmpty()) {
			value = null;
		}
		String logString = "";
		if(fromInternalConfig) logString = "da file interno ";
		else logString = "da file esterno ";

		if(value == null) {
			if(props != null) {
				value = props.getProperty(name);
				if(value != null && value.trim().isEmpty()) {
					value = null;
				}
			}
			if(value == null) {
				if(required) 
					throw new Exception("Proprieta ["+name+"] non trovata");
				else return null;
			} else {
				if(log != null) log.info("Letta proprieta di configurazione " + logString + name + ": " + value);
			}
		} else {
			if(log != null) log.info("Letta proprieta di sistema " + name + ": " + value);
		}

		return value.trim();
	}

	private static String getProperty(String name, Properties[] props, boolean required, Logger log) throws Exception {
		String value = null;
		for(int i=0; i<props.length; i++) {
			try { value = getProperty(name, props[i], required, i==1, log); } catch (Exception e) { }
			if(value != null && !value.trim().isEmpty()) {
				return value;
			}
		}

		if(log!= null) log.info("Proprieta " + name + " non trovata");

		if(required) 
			throw new Exception("Proprieta ["+name+"] non trovata");
		else 
			return null;
	}

	public VersioneAvviso getVersioneAvviso() {
		return this.versioneAvviso;
	}

	public URI getLog4j2Config() {
		return this.log4j2Config;
	}

	public int getDimensionePool() {
		return this.dimensionePool;
	}

	public String getKsLocation() {
		return this.ksLocation;
	}

	public String getKsPassword() {
		return this.ksPassword;
	}

	public String getKsAlias() {
		return this.ksAlias;
	}

	public String getmLogClass() {
		return this.mLogClass;
	}

	public String getmLogDS() {
		return this.mLogDS;
	}

	public Severity getmLogLevel() {
		return this.mLogLevel;
	}

	public TipiDatabase getmLogDBType() {
		return this.mLogDBType;
	}

	public boolean ismLogOnLog4j() {
		return this.mLogOnLog4j;
	}

	public boolean ismLogOnDB() {
		return this.mLogOnDB;
	}

	public boolean ismLogSql() {
		return this.mLogSql;
	}

	public boolean isPddAuthEnable() {
		return this.pddAuthEnable;
	}

	public List<String> getOutHandlers() {
		return this.outHandlers;
	}

	public String getResourceDir() {
		return this.resourceDir;
	}

	public boolean isBatchOn() {
		return this.batchOn;
	}

	public Integer getClusterId(){
		return this.clusterId;
	}

	public long getTimeoutBatch(){
		return this.timeoutBatch;
	}
	
	public IConservazione getConservazionPlugin(){
		return this.conservazionePlugin;
	}

	public String getUrlGovpayWC() {
		return this.urlGovpayWC;
	}

	public String getUrlWISP() {
		return this.urlWISP;
	}
	
	public boolean isBatchAvvisiPagamento() {
		return this.batchAvvisiPagamento;
	}
	
	public boolean isValidazioneAPIRestAbilitata() {
		return this.validazioneAPIRest;
	}

	public boolean isDumpAPIRestGETResponse() {
		return this.dumpAPIRestGETResponse;
	}

	public Integer getCacheLogo() {
		return 2 * 60 * 60;
	}

	public boolean isBatchAvvisaturaDigitale() {
		return batchAvvisaturaDigitale;
	}

	public boolean isBatchCaricamentoTracciati() {
		return batchCaricamentoTracciati;
	}
	
	public boolean isTimeoutPendenti() {
		return timeoutPendenti;
	}

	public Integer getTimeoutPendentiMins() {
		return timeoutPendentiMins;
	}

	public String getAppName() {
		return appName;
	}

	public String getAmbienteDeploy() {
		return ambienteDeploy;
	}

}

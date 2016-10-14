/*
 * GovPay - Porta di Accesso al Nodo dei Pagamenti SPC 
 * http://www.gov4j.it/govpay
 * 
 * Copyright (c) 2014-2016 Link.it srl (http://www.link.it).
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import it.govpay.core.utils.client.handler.IntegrationOutHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openspcoop2.utils.TipiDatabase;
import org.openspcoop2.utils.logger.constants.Severity;
import org.openspcoop2.utils.logger.log4j.Log4JLoggerWithProxyContext;

public class GovpayConfig {

	public enum VersioneAvviso {
		v001, v002;
	}

	private static final String PROPERTIES_FILE = "/govpay.properties";

	private static GovpayConfig instance;

	public static GovpayConfig getInstance() {
		return instance;
	}

	public static GovpayConfig newInstance() throws Exception {
		instance = new GovpayConfig();
		return instance;
	}

	private List<String> outHandlers;

	private URI log4j2Config;
	private URL urlPddVerifica;
	private String logoDir;
	private String resourceDir;
	private VersioneAvviso versioneAvviso;
	private int dimensionePool;
	private String ksLocation, ksPassword, ksAlias;
	private String mLogClass, mLogDS;
	private Severity mLogLevel;
	private TipiDatabase mLogDBType;
	private boolean mLogOnLog4j, mLogOnDB, mLogSql, pddAuthEnable;

	private boolean batchEstrattoConto;
	private int numeroMesiEstrattoConto, giornoEsecuzioneEstrattoConto;
	private String guestPapCode;
	private String pathEstrattoConto, pathEstrattoContoPdf,pathEstrattoContoPdfLoghi;
	private boolean batchEstrattoContoPdf;
	private Properties[] props;

	public GovpayConfig() throws Exception {
		// Default values:
		this.logoDir = null;
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
		this.batchEstrattoConto = false;
		this.batchEstrattoContoPdf = false;
		this.pddAuthEnable = true;

		try {

			// Recupero il property all'interno dell'EAR
			InputStream is = GovpayConfig.class.getResourceAsStream(PROPERTIES_FILE);

			props = new Properties[2];
			Properties props1 = new Properties();
			props1.load(is);
			props[1] = props1;

			// Recupero la configurazione della working dir
			// Se e' configurata, la uso come prioritaria

			try {
				this.resourceDir = getProperty("it.govpay.resource.path", props1, false, null);

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
				LogManager.getLogger("boot").warn("Errore di inizializzazione: " + e.getMessage() + ". Property ignorata.");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public void readProperties() throws Exception {
		Logger log = LogManager.getLogger("boot");
		try {
			Properties props0 = null;
			props[0] = props0;

			File gpConfigFile = new File(this.resourceDir + File.separatorChar + "govpay.properties");
			if(gpConfigFile.exists()) {
				props0 = new Properties();
				props0.load(new FileInputStream(gpConfigFile));
				log.info("Individuata configurazione prioritaria: " + gpConfigFile.getAbsolutePath());
				props[0] = props0;
			}

			try {
				this.logoDir = getProperty("it.govpay.psp.logo.path", props, false, log);
				if(this.logoDir != null) {
					File logoDirFile = new File(logoDir);
					if(!logoDirFile.isDirectory())
						throw new Exception("Il path indicato nella property \"it.govpay.psp.logo.path\" (" + logoDir + ") non esiste o non e' un folder.");
					File logoDirFile80 = new File(logoDir + "/80x40");
					if(!logoDirFile80.isDirectory())
						throw new Exception("Il folder indicato nella property \"it.govpay.psp.logo.path\" (" + logoDir + ") non contiene il subfolder (80x40).");
					File logoDirFile160 = new File(logoDir + "/160x80");
					if(!logoDirFile160.isDirectory())
						throw new Exception("Il folder indicato nella property \"it.govpay.psp.logo.path\" (" + logoDir + ") non contiene il subfolder (160x80).");
				}
			} catch (Exception e) {
				log.warn("Errore di inizializzazione: " + e.getMessage() + ". Property ignorata.");
				this.logoDir = null;
			}

			try {
				String versioneAvvisoProperty = getProperty("it.govpay.avviso.versione", props, false, log);
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
				String dimensionePoolProperty = getProperty("it.govpay.thread.pool", props, false, log);
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

			String urlPddVerificaProperty = getProperty("it.govpay.check.urlVerificaPDD", props, false, log);

			if(urlPddVerificaProperty != null) {
				try {
					this.urlPddVerifica = new URL(urlPddVerificaProperty.trim());
				} catch (Exception e) {
					log.warn("Valore ["+urlPddVerificaProperty.trim()+"] non consentito per la property \"it.govpay.check.urlVerificaPDD\": " +e.getMessage());
				}
			}

			String mLogClassString = getProperty("it.govpay.mlog.class", props, false, log);
			if(mLogClassString != null && !mLogClassString.isEmpty()) 
				this.mLogClass = mLogClassString;

			String mLogOnLog4jString = getProperty("it.govpay.mlog.log4j", props, false, log);
			if(mLogOnLog4jString != null && !Boolean.valueOf(mLogOnLog4jString))
				this.mLogOnLog4j = false;

			try {
				this.mLogLevel = Severity.valueOf(getProperty("it.govpay.mlog.level", props, false, log));
			} catch (Exception e) {
				log.warn("Valore ["+getProperty("it.govpay.mlog.level", props, false, log)+"] non consentito per la property \"it.govpay.mlog.level\". Assunto valore di default \"INFO\".");
			}

			String mLogOnDBString = getProperty("it.govpay.mlog.db", props, false, log);
			if(mLogOnDBString != null && Boolean.valueOf(mLogOnDBString))
				this.mLogOnDB = true;

			if(this.mLogOnDB) {
				String mLogDBTypeString = getProperty("it.govpay.mlog.db.type", props, true, log);
				try {
					this.mLogDBType = TipiDatabase.valueOf(mLogDBTypeString);
				} catch (IllegalArgumentException e) {
					throw new Exception("Valore ["+mLogDBTypeString.trim()+"] non consentito per la property \"it.govpay.mlog.db.type\": " +e.getMessage());
				}

				this.mLogDS = getProperty("it.govpay.mlog.db.ds", props, true, log);

				String mLogSqlString = getProperty("it.govpay.mlog.showSql", props, false, log);
				if(mLogSqlString != null)
					this.mLogOnLog4j = Boolean.valueOf(mLogSqlString);
			}

			String batchEstrattoContoString = getProperty("it.govpay.batch.estrattoConto", props, false, log);
			if(batchEstrattoContoString != null && Boolean.valueOf(batchEstrattoContoString))
				this.batchEstrattoConto = true;

			if(this.batchEstrattoConto) {
				String numeroMesiEstrattoContoProperty = getProperty("it.govpay.batch.estrattoConto.numeroMesi", props, true, log);
				if(numeroMesiEstrattoContoProperty != null)
					try {
						this.numeroMesiEstrattoConto = Integer.parseInt(numeroMesiEstrattoContoProperty);
					} catch (IllegalArgumentException e) {
						throw new Exception("Valore ["+numeroMesiEstrattoContoProperty.trim()+"] non consentito per la property \"it.govpay.batch.estrattoConto.numeroMesi\": " +e.getMessage());
					}

				String giornoEsecuzioneEstrattoContoProperty = getProperty("it.govpay.batch.estrattoConto.giornoEsecuzione", props, true, log);
				if(giornoEsecuzioneEstrattoContoProperty != null)
					try {
						this.giornoEsecuzioneEstrattoConto = Integer.parseInt(giornoEsecuzioneEstrattoContoProperty);
					} catch (IllegalArgumentException e) {
						throw new Exception("Valore ["+giornoEsecuzioneEstrattoContoProperty.trim()+"] non consentito per la property \"it.govpay.batch.estrattoConto.giornoEsecuzione\": " +e.getMessage());
					}

				this.pathEstrattoConto = getProperty("it.govpay.batch.estrattoConto.pathEsportazione", props, true, log);
				if(this.pathEstrattoConto != null) {
					File logoDirFile = new File(this.pathEstrattoConto);
					if(!logoDirFile.isDirectory())
						throw new Exception("Il path indicato nella property \"it.govpay.batch.estrattoConto.pathEsportazione\" (" + pathEstrattoConto + ") non esiste o non e' un folder.");

				}
			}


			String batchEstrattoContoPdfString = getProperty("it.govpay.batch.estrattoConto.pdf", props, false, log);
			if(batchEstrattoContoPdfString != null && Boolean.valueOf(batchEstrattoContoPdfString))
				this.batchEstrattoContoPdf = true;

			if(this.batchEstrattoContoPdf){
				this.pathEstrattoContoPdf = getProperty("it.govpay.batch.estrattoConto.pdf.pathEsportazione", props, true, log);
				if(this.pathEstrattoContoPdf != null) {
					File logoDirFile = new File(this.pathEstrattoContoPdf);
					if(!logoDirFile.isDirectory())
						throw new Exception("Il path indicato nella property \"it.govpay.batch.estrattoConto.pdf.pathEsportazione\" (" + pathEstrattoContoPdf + ") non esiste o non e' un folder.");

				}

				this.pathEstrattoContoPdfLoghi = getProperty("it.govpay.batch.estrattoConto.pdf.pathLoghi", props, true, log);
				if(this.pathEstrattoContoPdfLoghi != null) {
					File loghiDirFile = new File(this.pathEstrattoContoPdfLoghi);
					if(!loghiDirFile.isDirectory())
						throw new Exception("Il path indicato nella property \"it.govpay.batch.estrattoConto.pdf.pathLoghi\" (" + this.pathEstrattoContoPdfLoghi + ") non esiste o non e' un folder.");
				}

			}

			String pddAuthEnableString = getProperty("it.govpay.pdd.auth", props, false, log);
			if(pddAuthEnableString != null && pddAuthEnableString.equalsIgnoreCase("false"))
				this.pddAuthEnable = false;

			String listaHandlers = getProperty("it.govpay.integration.client.out", props, false, log);

			this.outHandlers = new ArrayList<String>();

			if(listaHandlers != null && !listaHandlers.isEmpty()) {
				String[] splitHandlers = listaHandlers.split(",");
				for(String handler: splitHandlers) {
					String handlerClass = getProperty("it.govpay.integration.client.out."+handler, props, true, log);
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
			
			this.guestPapCode = getProperty("it.govpay.pap.guestPapCode", props, false, log);

		} catch (Exception e) {
			log.error("Errore di inizializzazione: " + e.getMessage());
			throw e;
		}
	}

	public URL getUrlPddVerifica() {
		return urlPddVerifica;
	}

	private static String getProperty(String name, Properties props, boolean required, Logger log) throws Exception {
		String value = System.getProperty(name);

		if(value != null && value.trim().isEmpty()) {
			value = null;
		}

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
				if(log != null) log.info("Letta proprieta di configurazione " + name + ": " + value);
			}
		} else {
			if(log != null) log.info("Letta proprieta di sistema " + name + ": " + value);
		}

		return value.trim();
	}

	private static String getProperty(String name, Properties[] props, boolean required, Logger log) throws Exception {
		String value = null;
		for(Properties p : props) {
			try { value = getProperty(name, p, required, log); } catch (Exception e) { }
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

	public String getLogoDir() {
		return logoDir;
	}

	public VersioneAvviso getVersioneAvviso() {
		return versioneAvviso;
	}

	public URI getLog4j2Config() {
		return log4j2Config;
	}

	public int getDimensionePool() {
		return dimensionePool;
	}

	public String getKsLocation() {
		return ksLocation;
	}

	public String getKsPassword() {
		return ksPassword;
	}

	public String getKsAlias() {
		return ksAlias;
	}

	public String getmLogClass() {
		return mLogClass;
	}

	public String getmLogDS() {
		return mLogDS;
	}

	public Severity getmLogLevel() {
		return mLogLevel;
	}

	public TipiDatabase getmLogDBType() {
		return mLogDBType;
	}

	public boolean ismLogOnLog4j() {
		return mLogOnLog4j;
	}

	public boolean ismLogOnDB() {
		return mLogOnDB;
	}

	public boolean ismLogSql() {
		return mLogSql;
	}

	public boolean isBatchEstrattoConto() {
		return batchEstrattoConto;
	}

	public int getNumeroMesiEstrattoConto() {
		return numeroMesiEstrattoConto;
	}

	public int getGiornoEsecuzioneEstrattoConto() {
		return giornoEsecuzioneEstrattoConto;
	}

	public String getPathEstrattoConto() {
		return pathEstrattoConto;
	}

	public boolean isPddAuthEnable() {
		return pddAuthEnable;
	}

	public List<String> getOutHandlers() {
		return outHandlers;
	}

	public String getResourceDir() {
		return resourceDir;
	}
	
	public String getGuestPapCode() {
		return guestPapCode;
	}

	public String getPathEstrattoContoPdf() {
		return pathEstrattoContoPdf;
	}

	public String getPathEstrattoContoPdfLoghi() {
		return pathEstrattoContoPdfLoghi;
	}

	public boolean isBatchEstrattoContoPdf() {
		return batchEstrattoContoPdf;
	}
}

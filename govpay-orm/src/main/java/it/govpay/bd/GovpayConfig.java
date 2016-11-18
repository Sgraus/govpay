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
package it.govpay.bd;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.govpay.bd.pagamento.util.CustomIuv;

public class GovpayConfig {

	private static GovpayConfig instance;

	public static GovpayConfig getInstance() {
		return instance;
	}

	public static GovpayConfig newInstance(String propertyFileName) throws Exception {
		instance = new GovpayConfig(propertyFileName);
		return instance;
	}

	private String databaseType;
	private boolean databaseShowSql;
	private String dataSourceJNDIName;
	private String dataSourceAppName;
	private Properties[] props;
	private String resourceDir;
	private CustomIuv defaultCustomIuvGenerator;

	public GovpayConfig(String propertyFileName) throws Exception {

		Logger log = LogManager.getLogger("boot");

		// Recupero il property all'interno dell'EAR
		InputStream is = GovpayConfig.class.getResourceAsStream(propertyFileName);

		props = new Properties[2];
		Properties props1 = new Properties();
		props1.load(is);
		props[1] = props1;

		// Recupero la configurazione della working dir
		// Se e' configurata, la uso come prioritaria

		try {
			this.resourceDir = getProperty("it.govpay.resource.path", props1, false, true);

			if(this.resourceDir != null) {
				File resourceDirFile = new File(this.resourceDir);
				if(!resourceDirFile.isDirectory())
					throw new Exception("Il path indicato nella property \"it.govpay.resource.path\" (" + this.resourceDir + ") non esiste o non e' un folder.");
			}
		} catch (Exception e) {
			LogManager.getLogger("boot").warn("Errore di inizializzazione: " + e.getMessage() + ". Property ignorata.");
		}

		Properties props0 = null;
		props[0] = props0;

		File gpConfigFile = new File(this.resourceDir + propertyFileName);
		if(gpConfigFile.exists()) {
			props0 = new Properties();
			props0.load(new FileInputStream(gpConfigFile));
			log.info("Individuata configurazione prioritaria: " + gpConfigFile.getAbsolutePath());
			props[0] = props0;
		}

		this.databaseType = getProperty("it.govpay.orm.databaseType", props, true);
		String databaseShowSqlString = getProperty("it.govpay.orm.showSql", props, true);
		this.databaseShowSql = Boolean.parseBoolean(databaseShowSqlString);
		this.dataSourceJNDIName = getProperty("it.govpay.orm.dataSourceJNDIName", props, true);
		this.dataSourceAppName = getProperty("it.govpay.orm.dataSourceAppName", props, true);

		String defaultCustomIuvGeneratorClass = getProperty("it.govpay.defaultCustomIuvGenerator.class", props, false);
		if(defaultCustomIuvGeneratorClass != null && !defaultCustomIuvGeneratorClass.isEmpty()) {
			Class<?> c = null;
			try {
				c = this.getClass().getClassLoader().loadClass(defaultCustomIuvGeneratorClass);
			} catch (ClassNotFoundException e) {
				throw new Exception("La classe ["+defaultCustomIuvGeneratorClass+"] specificata per la gestione di IUV non e' presente nel classpath");
			}
			Object instance = c.newInstance();
			if(!(instance instanceof CustomIuv)) {
				throw new Exception("La classe ["+defaultCustomIuvGeneratorClass+"] specificata per la gestione di IUV deve estendere la classe " + CustomIuv.class.getName());
			}
			this.defaultCustomIuvGenerator = (CustomIuv) instance;
		} else {
			this.defaultCustomIuvGenerator = new CustomIuv();
		}

	}

	private String getProperty(String name, Properties props, boolean required, boolean fromInternalConfig) throws Exception {
		Logger log = LogManager.getLogger("boot");

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
				log.info("Letta proprieta di configurazione " + logString + name + ": " + value);
			}
		} else {
			log.info("Letta proprieta di configurazione " + name + ": " + value);
		}

		return value.trim();
	}

	private String getProperty(String name, Properties[] props, boolean required) throws Exception {
		Logger log = LogManager.getLogger("boot");

		String value = null;
		for(int i=0; i<props.length; i++) {
			try { value = getProperty(name, props[i], required, i==1); } catch (Exception e) { }
			if(value != null && !value.trim().isEmpty()) {
				return value;
			}
		}

		if(log != null) log.info("Proprieta " + name + " non trovata");

		if(required) 
			throw new Exception("Proprieta ["+name+"] non trovata");
		else 
			return null;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public boolean isDatabaseShowSql() {
		return databaseShowSql;
	}

	public String getDataSourceJNDIName() {
		return dataSourceJNDIName;
	}

	public String getDataSourceAppName() {
		return dataSourceAppName;
	}

	public CustomIuv getDefaultCustomIuvGenerator() {
		return defaultCustomIuvGenerator;
	} 

}

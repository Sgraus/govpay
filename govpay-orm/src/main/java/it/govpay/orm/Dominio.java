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
package it.govpay.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;


/** <p>Java class for Dominio complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Dominio">
 * 		&lt;sequence>
 * 			&lt;element name="idStazione" type="{http://www.govpay.it/orm}id-stazione" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="codDominio" type="{http://www.govpay.it/orm}string" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="gln" type="{http://www.govpay.it/orm}string" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="abilitato" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="ragioneSociale" type="{http://www.govpay.it/orm}string" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="xmlContiAccredito" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="xmlTabellaControparti" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="riusoIUV" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="customIUV" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="idApplicazioneDefault" type="{http://www.govpay.it/orm}id-applicazione" minOccurs="0" maxOccurs="1"/>
 * 			&lt;element name="auxDigit" type="{http://www.govpay.it/orm}int" minOccurs="1" maxOccurs="1" default="0"/>
 * 			&lt;element name="iuvPrefix" type="{http://www.govpay.it/orm}string" minOccurs="0" maxOccurs="1"/>
 * 			&lt;element name="iuvPrefixStrict" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="1" maxOccurs="1" default="false"/>
 * 		&lt;/sequence>
 * &lt;/complexType>
 * </pre>
 * 
 * @version $Rev$, $Date$
 * 
 * @author Giovanni Bussu (bussu@link.it)
 * @author Lorenzo Nardi (nardi@link.it)
 * @author $Author$
 * */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Dominio", 
  propOrder = {
  	"idStazione",
  	"codDominio",
  	"gln",
  	"abilitato",
  	"ragioneSociale",
  	"xmlContiAccredito",
  	"xmlTabellaControparti",
  	"riusoIUV",
  	"customIUV",
  	"idApplicazioneDefault",
  	"_decimalWrapper_auxDigit",
  	"iuvPrefix",
  	"iuvPrefixStrict"
  }
)

@XmlRootElement(name = "Dominio")

public class Dominio extends org.openspcoop2.utils.beans.BaseBean implements Serializable , Cloneable {
  public Dominio() {
  }

  public Long getId() {
    if(this.id!=null)
		return this.id;
	else
		return new Long(-1);
  }

  public void setId(Long id) {
    if(id!=null)
		this.id=id;
	else
		this.id=new Long(-1);
  }

  public IdStazione getIdStazione() {
    return this.idStazione;
  }

  public void setIdStazione(IdStazione idStazione) {
    this.idStazione = idStazione;
  }

  public java.lang.String getCodDominio() {
    return this.codDominio;
  }

  public void setCodDominio(java.lang.String codDominio) {
    this.codDominio = codDominio;
  }

  public java.lang.String getGln() {
    return this.gln;
  }

  public void setGln(java.lang.String gln) {
    this.gln = gln;
  }

  public boolean isAbilitato() {
    return this.abilitato;
  }

  public boolean getAbilitato() {
    return this.abilitato;
  }

  public void setAbilitato(boolean abilitato) {
    this.abilitato = abilitato;
  }

  public java.lang.String getRagioneSociale() {
    return this.ragioneSociale;
  }

  public void setRagioneSociale(java.lang.String ragioneSociale) {
    this.ragioneSociale = ragioneSociale;
  }

  public byte[] getXmlContiAccredito() {
    return this.xmlContiAccredito;
  }

  public void setXmlContiAccredito(byte[] xmlContiAccredito) {
    this.xmlContiAccredito = xmlContiAccredito;
  }

  public byte[] getXmlTabellaControparti() {
    return this.xmlTabellaControparti;
  }

  public void setXmlTabellaControparti(byte[] xmlTabellaControparti) {
    this.xmlTabellaControparti = xmlTabellaControparti;
  }

  public boolean isRiusoIUV() {
    return this.riusoIUV;
  }

  public boolean getRiusoIUV() {
    return this.riusoIUV;
  }

  public void setRiusoIUV(boolean riusoIUV) {
    this.riusoIUV = riusoIUV;
  }

  public boolean isCustomIUV() {
    return this.customIUV;
  }

  public boolean getCustomIUV() {
    return this.customIUV;
  }

  public void setCustomIUV(boolean customIUV) {
    this.customIUV = customIUV;
  }

  public IdApplicazione getIdApplicazioneDefault() {
    return this.idApplicazioneDefault;
  }

  public void setIdApplicazioneDefault(IdApplicazione idApplicazioneDefault) {
    this.idApplicazioneDefault = idApplicazioneDefault;
  }

  public int getAuxDigit() {
    return (java.lang.Integer) this._decimalWrapper_auxDigit.getObject(java.lang.Integer.class);
  }

  public void setAuxDigit(int auxDigit) {
    this._decimalWrapper_auxDigit = new org.openspcoop2.utils.jaxb.DecimalWrapper(1,1,auxDigit);
  }

  public java.lang.String getIuvPrefix() {
    return this.iuvPrefix;
  }

  public void setIuvPrefix(java.lang.String iuvPrefix) {
    this.iuvPrefix = iuvPrefix;
  }

  public boolean isIuvPrefixStrict() {
    return this.iuvPrefixStrict;
  }

  public boolean getIuvPrefixStrict() {
    return this.iuvPrefixStrict;
  }

  public void setIuvPrefixStrict(boolean iuvPrefixStrict) {
    this.iuvPrefixStrict = iuvPrefixStrict;
  }

  private static final long serialVersionUID = 1L;

  @XmlTransient
  private Long id;

  private static it.govpay.orm.model.DominioModel modelStaticInstance = null;
  private static synchronized void initModelStaticInstance(){
	  if(it.govpay.orm.Dominio.modelStaticInstance==null){
  			it.govpay.orm.Dominio.modelStaticInstance = new it.govpay.orm.model.DominioModel();
	  }
  }
  public static it.govpay.orm.model.DominioModel model(){
	  if(it.govpay.orm.Dominio.modelStaticInstance==null){
	  		initModelStaticInstance();
	  }
	  return it.govpay.orm.Dominio.modelStaticInstance;
  }


  @XmlElement(name="idStazione",required=true,nillable=false)
  protected IdStazione idStazione;

  @javax.xml.bind.annotation.XmlSchemaType(name="string")
  @XmlElement(name="codDominio",required=true,nillable=false)
  protected java.lang.String codDominio;

  @javax.xml.bind.annotation.XmlSchemaType(name="string")
  @XmlElement(name="gln",required=true,nillable=false)
  protected java.lang.String gln;

  @javax.xml.bind.annotation.XmlSchemaType(name="boolean")
  @XmlElement(name="abilitato",required=true,nillable=false)
  protected boolean abilitato;

  @javax.xml.bind.annotation.XmlSchemaType(name="string")
  @XmlElement(name="ragioneSociale",required=true,nillable=false)
  protected java.lang.String ragioneSociale;

  @javax.xml.bind.annotation.XmlSchemaType(name="base64Binary")
  @XmlElement(name="xmlContiAccredito",required=true,nillable=false)
  protected byte[] xmlContiAccredito;

  @javax.xml.bind.annotation.XmlSchemaType(name="base64Binary")
  @XmlElement(name="xmlTabellaControparti",required=true,nillable=false)
  protected byte[] xmlTabellaControparti;

  @javax.xml.bind.annotation.XmlSchemaType(name="boolean")
  @XmlElement(name="riusoIUV",required=true,nillable=false)
  protected boolean riusoIUV;

  @javax.xml.bind.annotation.XmlSchemaType(name="boolean")
  @XmlElement(name="customIUV",required=true,nillable=false)
  protected boolean customIUV;

  @XmlElement(name="idApplicazioneDefault",required=false,nillable=false)
  protected IdApplicazione idApplicazioneDefault;

  @javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter(org.openspcoop2.utils.jaxb.Decimal2String.class)
  @javax.xml.bind.annotation.XmlSchemaType(name="int")
  @XmlElement(name="auxDigit",required=true,nillable=false,defaultValue="0")
  org.openspcoop2.utils.jaxb.DecimalWrapper _decimalWrapper_auxDigit = new org.openspcoop2.utils.jaxb.DecimalWrapper(1,1,  0);

  @XmlTransient
  protected int auxDigit;

  @javax.xml.bind.annotation.XmlSchemaType(name="string")
  @XmlElement(name="iuvPrefix",required=false,nillable=false)
  protected java.lang.String iuvPrefix;

  @javax.xml.bind.annotation.XmlSchemaType(name="boolean")
  @XmlElement(name="iuvPrefixStrict",required=true,nillable=false,defaultValue="false")
  protected boolean iuvPrefixStrict = false;

}

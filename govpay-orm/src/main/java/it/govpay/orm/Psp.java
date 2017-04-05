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
package it.govpay.orm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;


/** <p>Java class for Psp complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Psp">
 * 		&lt;sequence>
 * 			&lt;element name="codPsp" type="{http://www.govpay.it/orm}string" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="ragioneSociale" type="{http://www.govpay.it/orm}string" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="urlInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0" maxOccurs="1"/>
 * 			&lt;element name="abilitato" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="storno" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="1" maxOccurs="1"/>
 * 			&lt;element name="marcaBollo" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="1" maxOccurs="1"/>
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
@XmlType(name = "Psp", 
  propOrder = {
  	"codPsp",
  	"ragioneSociale",
  	"urlInfo",
  	"abilitato",
  	"storno",
  	"marcaBollo"
  }
)

@XmlRootElement(name = "Psp")

public class Psp extends org.openspcoop2.utils.beans.BaseBean implements Serializable , Cloneable {
  public Psp() {
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

  public java.lang.String getCodPsp() {
    return this.codPsp;
  }

  public void setCodPsp(java.lang.String codPsp) {
    this.codPsp = codPsp;
  }

  public java.lang.String getRagioneSociale() {
    return this.ragioneSociale;
  }

  public void setRagioneSociale(java.lang.String ragioneSociale) {
    this.ragioneSociale = ragioneSociale;
  }

  public java.lang.String getUrlInfo() {
    return this.urlInfo;
  }

  public void setUrlInfo(java.lang.String urlInfo) {
    this.urlInfo = urlInfo;
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

  public boolean isStorno() {
    return this.storno;
  }

  public boolean getStorno() {
    return this.storno;
  }

  public void setStorno(boolean storno) {
    this.storno = storno;
  }

  public boolean isMarcaBollo() {
    return this.marcaBollo;
  }

  public boolean getMarcaBollo() {
    return this.marcaBollo;
  }

  public void setMarcaBollo(boolean marcaBollo) {
    this.marcaBollo = marcaBollo;
  }

  private static final long serialVersionUID = 1L;

  @XmlTransient
  private Long id;

  private static it.govpay.orm.model.PspModel modelStaticInstance = null;
  private static synchronized void initModelStaticInstance(){
	  if(it.govpay.orm.Psp.modelStaticInstance==null){
  			it.govpay.orm.Psp.modelStaticInstance = new it.govpay.orm.model.PspModel();
	  }
  }
  public static it.govpay.orm.model.PspModel model(){
	  if(it.govpay.orm.Psp.modelStaticInstance==null){
	  		initModelStaticInstance();
	  }
	  return it.govpay.orm.Psp.modelStaticInstance;
  }


  @javax.xml.bind.annotation.XmlSchemaType(name="string")
  @XmlElement(name="codPsp",required=true,nillable=false)
  protected java.lang.String codPsp;

  @javax.xml.bind.annotation.XmlSchemaType(name="string")
  @XmlElement(name="ragioneSociale",required=true,nillable=false)
  protected java.lang.String ragioneSociale;

  @javax.xml.bind.annotation.XmlSchemaType(name="string")
  @XmlElement(name="urlInfo",required=false,nillable=false)
  protected java.lang.String urlInfo;

  @javax.xml.bind.annotation.XmlSchemaType(name="boolean")
  @XmlElement(name="abilitato",required=true,nillable=false)
  protected boolean abilitato;

  @javax.xml.bind.annotation.XmlSchemaType(name="boolean")
  @XmlElement(name="storno",required=true,nillable=false)
  protected boolean storno;

  @javax.xml.bind.annotation.XmlSchemaType(name="boolean")
  @XmlElement(name="marcaBollo",required=true,nillable=false)
  protected boolean marcaBollo;

}

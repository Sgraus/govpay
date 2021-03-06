//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.04 at 02:49:47 PM CEST 
//


package it.gov.digitpa.schemas._2011.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ctInformativaPSP complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctInformativaPSP">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="identificativoFlusso" type="{}stText35"/>
 *         &lt;element name="identificativoPSP" type="{}stText35"/>
 *         &lt;element name="ragioneSociale" type="{}stText70"/>
 *         &lt;element name="informativaMaster" type="{}ctInformativaMaster"/>
 *         &lt;element name="listaInformativaDetail" type="{}ctListaInformativaDetail"/>
 *         &lt;element name="listaServiziNonDiPagamento" type="{}ctListaServiziNonDiPagamento" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctInformativaPSP", propOrder = {
    "identificativoFlusso",
    "identificativoPSP",
    "ragioneSociale",
    "informativaMaster",
    "listaInformativaDetail",
    "listaServiziNonDiPagamento"
})
@XmlRootElement(name = "informativaPSP")
public class InformativaPSP {

    @XmlElement(required = true)
    protected String identificativoFlusso;
    @XmlElement(required = true)
    protected String identificativoPSP;
    @XmlElement(required = true)
    protected String ragioneSociale;
    @XmlElement(required = true)
    protected CtInformativaMaster informativaMaster;
    @XmlElement(required = true)
    protected CtListaInformativaDetail listaInformativaDetail;
    protected CtListaServiziNonDiPagamento listaServiziNonDiPagamento;

    /**
     * Gets the value of the identificativoFlusso property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoFlusso() {
        return this.identificativoFlusso;
    }

    /**
     * Sets the value of the identificativoFlusso property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoFlusso(String value) {
        this.identificativoFlusso = value;
    }

    /**
     * Gets the value of the identificativoPSP property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdentificativoPSP() {
        return this.identificativoPSP;
    }

    /**
     * Sets the value of the identificativoPSP property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdentificativoPSP(String value) {
        this.identificativoPSP = value;
    }

    /**
     * Gets the value of the ragioneSociale property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRagioneSociale() {
        return this.ragioneSociale;
    }

    /**
     * Sets the value of the ragioneSociale property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRagioneSociale(String value) {
        this.ragioneSociale = value;
    }

    /**
     * Gets the value of the informativaMaster property.
     * 
     * @return
     *     possible object is
     *     {@link CtInformativaMaster }
     *     
     */
    public CtInformativaMaster getInformativaMaster() {
        return this.informativaMaster;
    }

    /**
     * Sets the value of the informativaMaster property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtInformativaMaster }
     *     
     */
    public void setInformativaMaster(CtInformativaMaster value) {
        this.informativaMaster = value;
    }

    /**
     * Gets the value of the listaInformativaDetail property.
     * 
     * @return
     *     possible object is
     *     {@link CtListaInformativaDetail }
     *     
     */
    public CtListaInformativaDetail getListaInformativaDetail() {
        return this.listaInformativaDetail;
    }

    /**
     * Sets the value of the listaInformativaDetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtListaInformativaDetail }
     *     
     */
    public void setListaInformativaDetail(CtListaInformativaDetail value) {
        this.listaInformativaDetail = value;
    }

    /**
     * Gets the value of the listaServiziNonDiPagamento property.
     * 
     * @return
     *     possible object is
     *     {@link CtListaServiziNonDiPagamento }
     *     
     */
    public CtListaServiziNonDiPagamento getListaServiziNonDiPagamento() {
        return this.listaServiziNonDiPagamento;
    }

    /**
     * Sets the value of the listaServiziNonDiPagamento property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtListaServiziNonDiPagamento }
     *     
     */
    public void setListaServiziNonDiPagamento(CtListaServiziNonDiPagamento value) {
        this.listaServiziNonDiPagamento = value;
    }

}

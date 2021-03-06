//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.04 at 02:49:47 PM CEST 
//


package it.gov.spcoop.avvisopagamentopa.informazioniversamentoqr;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import it.govpay.core.utils.adapter.DecimalAdapter;


/**
 * <p>Java class for ctInformazioniVersamento complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ctInformazioniVersamento">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="codiceIdentificativoEnte" type="{http://AvvisoPagamentoPA.spcoop.gov.it/InformazioniVersamentoQR}stCodiceIdentificativoEnte"/>
 *         &lt;element name="numeroAvviso" type="{http://AvvisoPagamentoPA.spcoop.gov.it/InformazioniVersamentoQR}ctNumeroAvviso"/>
 *         &lt;element name="importoVersamento" type="{http://AvvisoPagamentoPA.spcoop.gov.it/InformazioniVersamentoQR}stImportoVersamento"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctInformazioniVersamento", propOrder = {
    "codiceIdentificativoEnte",
    "numeroAvviso",
    "importoVersamento"
})
@XmlRootElement(name = "informazioniVersamento")
public class InformazioniVersamento {

    @XmlElement(required = true)
    protected String codiceIdentificativoEnte;
    @XmlElement(required = true)
    protected CtNumeroAvviso numeroAvviso;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(DecimalAdapter.class)
    protected BigDecimal importoVersamento;

    /**
     * Gets the value of the codiceIdentificativoEnte property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCodiceIdentificativoEnte() {
        return this.codiceIdentificativoEnte;
    }

    /**
     * Sets the value of the codiceIdentificativoEnte property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCodiceIdentificativoEnte(String value) {
        this.codiceIdentificativoEnte = value;
    }

    /**
     * Gets the value of the numeroAvviso property.
     * 
     * @return
     *     possible object is
     *     {@link CtNumeroAvviso }
     *     
     */
    public CtNumeroAvviso getNumeroAvviso() {
        return this.numeroAvviso;
    }

    /**
     * Sets the value of the numeroAvviso property.
     * 
     * @param value
     *     allowed object is
     *     {@link CtNumeroAvviso }
     *     
     */
    public void setNumeroAvviso(CtNumeroAvviso value) {
        this.numeroAvviso = value;
    }

    /**
     * Gets the value of the importoVersamento property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BigDecimal getImportoVersamento() {
        return this.importoVersamento;
    }

    /**
     * Sets the value of the importoVersamento property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImportoVersamento(BigDecimal value) {
        this.importoVersamento = value;
    }

}

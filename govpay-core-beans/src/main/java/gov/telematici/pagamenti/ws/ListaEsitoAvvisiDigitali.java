//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2018.07.10 alle 10:18:53 AM CEST 
//


package gov.telematici.pagamenti.ws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per listaEsitoAvvisiDigitali complex type.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * 
 * <pre>
 * &lt;complexType name="listaEsitoAvvisiDigitali">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="versioneOggetto" type="{http://ws.pagamenti.telematici.gov/}stText16"/>
 *         &lt;element name="identificativoFlusso" type="{http://ws.pagamenti.telematici.gov/}stIdentificativoFlusso"/>
 *         &lt;element name="esitoAvvisoDigitale" type="{http://ws.pagamenti.telematici.gov/}ctEsitoAvvisoDigitale" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listaEsitoAvvisiDigitali", propOrder = {
    "versioneOggetto",
    "identificativoFlusso",
    "esitoAvvisoDigitale"
})
public class ListaEsitoAvvisiDigitali {

    @XmlElement(required = true)
    protected String versioneOggetto;
    @XmlElement(required = true)
    protected String identificativoFlusso;
    @XmlElement(required = true)
    protected List<CtEsitoAvvisoDigitale> esitoAvvisoDigitale;

    /**
     * Recupera il valore della proprietà versioneOggetto.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersioneOggetto() {
        return this.versioneOggetto;
    }

    /**
     * Imposta il valore della proprietà versioneOggetto.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersioneOggetto(String value) {
        this.versioneOggetto = value;
    }

    /**
     * Recupera il valore della proprietà identificativoFlusso.
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
     * Imposta il valore della proprietà identificativoFlusso.
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
     * Gets the value of the esitoAvvisoDigitale property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the esitoAvvisoDigitale property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEsitoAvvisoDigitale().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CtEsitoAvvisoDigitale }
     * 
     * 
     */
    public List<CtEsitoAvvisoDigitale> getEsitoAvvisoDigitale() {
        if (this.esitoAvvisoDigitale == null) {
            this.esitoAvvisoDigitale = new ArrayList<>();
        }
        return this.esitoAvvisoDigitale;
    }

}

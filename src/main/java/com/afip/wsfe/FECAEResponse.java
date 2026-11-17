
package com.afip.wsfe;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para FECAEResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="FECAEResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="FeCabResp" type="{http://ar.gov.afip.dif.FEV1/}FECAECabResponse" minOccurs="0"/&gt;
 *         &lt;element name="FeDetResp" type="{http://ar.gov.afip.dif.FEV1/}ArrayOfFECAEDetResponse" minOccurs="0"/&gt;
 *         &lt;element name="Events" type="{http://ar.gov.afip.dif.FEV1/}ArrayOfEvt" minOccurs="0"/&gt;
 *         &lt;element name="Errors" type="{http://ar.gov.afip.dif.FEV1/}ArrayOfErr" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FECAEResponse", propOrder = {
    "feCabResp",
    "feDetResp",
    "events",
    "errors"
})
public class FECAEResponse {

    @XmlElement(name = "FeCabResp")
    protected FECAECabResponse feCabResp;
    @XmlElement(name = "FeDetResp")
    protected ArrayOfFECAEDetResponse feDetResp;
    @XmlElement(name = "Events")
    protected ArrayOfEvt events;
    @XmlElement(name = "Errors")
    protected ArrayOfErr errors;

    /**
     * Obtiene el valor de la propiedad feCabResp.
     * 
     * @return
     *     possible object is
     *     {@link FECAECabResponse }
     *     
     */
    public FECAECabResponse getFeCabResp() {
        return feCabResp;
    }

    /**
     * Define el valor de la propiedad feCabResp.
     * 
     * @param value
     *     allowed object is
     *     {@link FECAECabResponse }
     *     
     */
    public void setFeCabResp(FECAECabResponse value) {
        this.feCabResp = value;
    }

    /**
     * Obtiene el valor de la propiedad feDetResp.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfFECAEDetResponse }
     *     
     */
    public ArrayOfFECAEDetResponse getFeDetResp() {
        return feDetResp;
    }

    /**
     * Define el valor de la propiedad feDetResp.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfFECAEDetResponse }
     *     
     */
    public void setFeDetResp(ArrayOfFECAEDetResponse value) {
        this.feDetResp = value;
    }

    /**
     * Obtiene el valor de la propiedad events.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEvt }
     *     
     */
    public ArrayOfEvt getEvents() {
        return events;
    }

    /**
     * Define el valor de la propiedad events.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEvt }
     *     
     */
    public void setEvents(ArrayOfEvt value) {
        this.events = value;
    }

    /**
     * Obtiene el valor de la propiedad errors.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfErr }
     *     
     */
    public ArrayOfErr getErrors() {
        return errors;
    }

    /**
     * Define el valor de la propiedad errors.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfErr }
     *     
     */
    public void setErrors(ArrayOfErr value) {
        this.errors = value;
    }

}

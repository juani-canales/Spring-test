
package com.afip.wsfe;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para anonymous complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="FECompUltimoAutorizadoResult" type="{http://ar.gov.afip.dif.FEV1/}FERecuperaLastCbteResponse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "feCompUltimoAutorizadoResult"
})
@XmlRootElement(name = "FECompUltimoAutorizadoResponse")
public class FECompUltimoAutorizadoResponse {

    @XmlElement(name = "FECompUltimoAutorizadoResult")
    protected FERecuperaLastCbteResponse feCompUltimoAutorizadoResult;

    /**
     * Obtiene el valor de la propiedad feCompUltimoAutorizadoResult.
     * 
     * @return
     *     possible object is
     *     {@link FERecuperaLastCbteResponse }
     *     
     */
    public FERecuperaLastCbteResponse getFECompUltimoAutorizadoResult() {
        return feCompUltimoAutorizadoResult;
    }

    /**
     * Define el valor de la propiedad feCompUltimoAutorizadoResult.
     * 
     * @param value
     *     allowed object is
     *     {@link FERecuperaLastCbteResponse }
     *     
     */
    public void setFECompUltimoAutorizadoResult(FERecuperaLastCbteResponse value) {
        this.feCompUltimoAutorizadoResult = value;
    }

}

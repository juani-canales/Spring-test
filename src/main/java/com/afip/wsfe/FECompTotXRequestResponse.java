
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
 *         &lt;element name="FECompTotXRequestResult" type="{http://ar.gov.afip.dif.FEV1/}FERegXReqResponse" minOccurs="0"/&gt;
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
    "feCompTotXRequestResult"
})
@XmlRootElement(name = "FECompTotXRequestResponse")
public class FECompTotXRequestResponse {

    @XmlElement(name = "FECompTotXRequestResult")
    protected FERegXReqResponse feCompTotXRequestResult;

    /**
     * Obtiene el valor de la propiedad feCompTotXRequestResult.
     * 
     * @return
     *     possible object is
     *     {@link FERegXReqResponse }
     *     
     */
    public FERegXReqResponse getFECompTotXRequestResult() {
        return feCompTotXRequestResult;
    }

    /**
     * Define el valor de la propiedad feCompTotXRequestResult.
     * 
     * @param value
     *     allowed object is
     *     {@link FERegXReqResponse }
     *     
     */
    public void setFECompTotXRequestResult(FERegXReqResponse value) {
        this.feCompTotXRequestResult = value;
    }

}

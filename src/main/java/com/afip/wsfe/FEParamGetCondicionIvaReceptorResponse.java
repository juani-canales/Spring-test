
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
 *         &lt;element name="FEParamGetCondicionIvaReceptorResult" type="{http://ar.gov.afip.dif.FEV1/}CondicionIvaReceptorResponse" minOccurs="0"/&gt;
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
    "feParamGetCondicionIvaReceptorResult"
})
@XmlRootElement(name = "FEParamGetCondicionIvaReceptorResponse")
public class FEParamGetCondicionIvaReceptorResponse {

    @XmlElement(name = "FEParamGetCondicionIvaReceptorResult")
    protected CondicionIvaReceptorResponse feParamGetCondicionIvaReceptorResult;

    /**
     * Obtiene el valor de la propiedad feParamGetCondicionIvaReceptorResult.
     * 
     * @return
     *     possible object is
     *     {@link CondicionIvaReceptorResponse }
     *     
     */
    public CondicionIvaReceptorResponse getFEParamGetCondicionIvaReceptorResult() {
        return feParamGetCondicionIvaReceptorResult;
    }

    /**
     * Define el valor de la propiedad feParamGetCondicionIvaReceptorResult.
     * 
     * @param value
     *     allowed object is
     *     {@link CondicionIvaReceptorResponse }
     *     
     */
    public void setFEParamGetCondicionIvaReceptorResult(CondicionIvaReceptorResponse value) {
        this.feParamGetCondicionIvaReceptorResult = value;
    }

}


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
 *         &lt;element name="FEParamGetActividadesResult" type="{http://ar.gov.afip.dif.FEV1/}FEActividadesResponse" minOccurs="0"/&gt;
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
    "feParamGetActividadesResult"
})
@XmlRootElement(name = "FEParamGetActividadesResponse")
public class FEParamGetActividadesResponse {

    @XmlElement(name = "FEParamGetActividadesResult")
    protected FEActividadesResponse feParamGetActividadesResult;

    /**
     * Obtiene el valor de la propiedad feParamGetActividadesResult.
     * 
     * @return
     *     possible object is
     *     {@link FEActividadesResponse }
     *     
     */
    public FEActividadesResponse getFEParamGetActividadesResult() {
        return feParamGetActividadesResult;
    }

    /**
     * Define el valor de la propiedad feParamGetActividadesResult.
     * 
     * @param value
     *     allowed object is
     *     {@link FEActividadesResponse }
     *     
     */
    public void setFEParamGetActividadesResult(FEActividadesResponse value) {
        this.feParamGetActividadesResult = value;
    }

}

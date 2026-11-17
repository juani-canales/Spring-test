
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
 *         &lt;element name="FECAEASinMovimientoInformarResult" type="{http://ar.gov.afip.dif.FEV1/}FECAEASinMovResponse" minOccurs="0"/&gt;
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
    "fecaeaSinMovimientoInformarResult"
})
@XmlRootElement(name = "FECAEASinMovimientoInformarResponse")
public class FECAEASinMovimientoInformarResponse {

    @XmlElement(name = "FECAEASinMovimientoInformarResult")
    protected FECAEASinMovResponse fecaeaSinMovimientoInformarResult;

    /**
     * Obtiene el valor de la propiedad fecaeaSinMovimientoInformarResult.
     * 
     * @return
     *     possible object is
     *     {@link FECAEASinMovResponse }
     *     
     */
    public FECAEASinMovResponse getFECAEASinMovimientoInformarResult() {
        return fecaeaSinMovimientoInformarResult;
    }

    /**
     * Define el valor de la propiedad fecaeaSinMovimientoInformarResult.
     * 
     * @param value
     *     allowed object is
     *     {@link FECAEASinMovResponse }
     *     
     */
    public void setFECAEASinMovimientoInformarResult(FECAEASinMovResponse value) {
        this.fecaeaSinMovimientoInformarResult = value;
    }

}

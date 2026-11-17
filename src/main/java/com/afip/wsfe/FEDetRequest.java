
package com.afip.wsfe;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para FEDetRequest complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="FEDetRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Concepto" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="DocTipo" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="DocNro" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="CbteDesde" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="CbteHasta" type="{http://www.w3.org/2001/XMLSchema}long"/&gt;
 *         &lt;element name="CbteFch" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ImpTotal" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="ImpTotConc" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="ImpNeto" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="ImpOpEx" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="ImpTrib" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="ImpIVA" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="FchServDesde" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="FchServHasta" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="FchVtoPago" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="MonId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="MonCotiz" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="CanMisMonExt" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CondicionIVAReceptorId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="CbtesAsoc" type="{http://ar.gov.afip.dif.FEV1/}ArrayOfCbteAsoc" minOccurs="0"/&gt;
 *         &lt;element name="Tributos" type="{http://ar.gov.afip.dif.FEV1/}ArrayOfTributo" minOccurs="0"/&gt;
 *         &lt;element name="Iva" type="{http://ar.gov.afip.dif.FEV1/}ArrayOfAlicIva" minOccurs="0"/&gt;
 *         &lt;element name="Opcionales" type="{http://ar.gov.afip.dif.FEV1/}ArrayOfOpcional" minOccurs="0"/&gt;
 *         &lt;element name="Compradores" type="{http://ar.gov.afip.dif.FEV1/}ArrayOfComprador" minOccurs="0"/&gt;
 *         &lt;element name="PeriodoAsoc" type="{http://ar.gov.afip.dif.FEV1/}Periodo" minOccurs="0"/&gt;
 *         &lt;element name="Actividades" type="{http://ar.gov.afip.dif.FEV1/}ArrayOfActividad" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FEDetRequest", propOrder = {
    "concepto",
    "docTipo",
    "docNro",
    "cbteDesde",
    "cbteHasta",
    "cbteFch",
    "impTotal",
    "impTotConc",
    "impNeto",
    "impOpEx",
    "impTrib",
    "impIVA",
    "fchServDesde",
    "fchServHasta",
    "fchVtoPago",
    "monId",
    "monCotiz",
    "canMisMonExt",
    "condicionIVAReceptorId",
    "cbtesAsoc",
    "tributos",
    "iva",
    "opcionales",
    "compradores",
    "periodoAsoc",
    "actividades"
})
@XmlSeeAlso({
    FECAEDetRequest.class,
    FECAEADetRequest.class
})
public class FEDetRequest {

    @XmlElement(name = "Concepto")
    protected int concepto;
    @XmlElement(name = "DocTipo")
    protected int docTipo;
    @XmlElement(name = "DocNro")
    protected long docNro;
    @XmlElement(name = "CbteDesde")
    protected long cbteDesde;
    @XmlElement(name = "CbteHasta")
    protected long cbteHasta;
    @XmlElement(name = "CbteFch")
    protected String cbteFch;
    @XmlElement(name = "ImpTotal")
    protected double impTotal;
    @XmlElement(name = "ImpTotConc")
    protected double impTotConc;
    @XmlElement(name = "ImpNeto")
    protected double impNeto;
    @XmlElement(name = "ImpOpEx")
    protected double impOpEx;
    @XmlElement(name = "ImpTrib")
    protected double impTrib;
    @XmlElement(name = "ImpIVA")
    protected double impIVA;
    @XmlElement(name = "FchServDesde")
    protected String fchServDesde;
    @XmlElement(name = "FchServHasta")
    protected String fchServHasta;
    @XmlElement(name = "FchVtoPago")
    protected String fchVtoPago;
    @XmlElement(name = "MonId")
    protected String monId;
    @XmlElement(name = "MonCotiz")
    protected Double monCotiz;
    @XmlElement(name = "CanMisMonExt")
    protected String canMisMonExt;
    @XmlElement(name = "CondicionIVAReceptorId")
    protected Integer condicionIVAReceptorId;
    @XmlElement(name = "CbtesAsoc")
    protected ArrayOfCbteAsoc cbtesAsoc;
    @XmlElement(name = "Tributos")
    protected ArrayOfTributo tributos;
    @XmlElement(name = "Iva")
    protected ArrayOfAlicIva iva;
    @XmlElement(name = "Opcionales")
    protected ArrayOfOpcional opcionales;
    @XmlElement(name = "Compradores")
    protected ArrayOfComprador compradores;
    @XmlElement(name = "PeriodoAsoc")
    protected Periodo periodoAsoc;
    @XmlElement(name = "Actividades")
    protected ArrayOfActividad actividades;

    /**
     * Obtiene el valor de la propiedad concepto.
     * 
     */
    public int getConcepto() {
        return concepto;
    }

    /**
     * Define el valor de la propiedad concepto.
     * 
     */
    public void setConcepto(int value) {
        this.concepto = value;
    }

    /**
     * Obtiene el valor de la propiedad docTipo.
     * 
     */
    public int getDocTipo() {
        return docTipo;
    }

    /**
     * Define el valor de la propiedad docTipo.
     * 
     */
    public void setDocTipo(int value) {
        this.docTipo = value;
    }

    /**
     * Obtiene el valor de la propiedad docNro.
     * 
     */
    public long getDocNro() {
        return docNro;
    }

    /**
     * Define el valor de la propiedad docNro.
     * 
     */
    public void setDocNro(long value) {
        this.docNro = value;
    }

    /**
     * Obtiene el valor de la propiedad cbteDesde.
     * 
     */
    public long getCbteDesde() {
        return cbteDesde;
    }

    /**
     * Define el valor de la propiedad cbteDesde.
     * 
     */
    public void setCbteDesde(long value) {
        this.cbteDesde = value;
    }

    /**
     * Obtiene el valor de la propiedad cbteHasta.
     * 
     */
    public long getCbteHasta() {
        return cbteHasta;
    }

    /**
     * Define el valor de la propiedad cbteHasta.
     * 
     */
    public void setCbteHasta(long value) {
        this.cbteHasta = value;
    }

    /**
     * Obtiene el valor de la propiedad cbteFch.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCbteFch() {
        return cbteFch;
    }

    /**
     * Define el valor de la propiedad cbteFch.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCbteFch(String value) {
        this.cbteFch = value;
    }

    /**
     * Obtiene el valor de la propiedad impTotal.
     * 
     */
    public double getImpTotal() {
        return impTotal;
    }

    /**
     * Define el valor de la propiedad impTotal.
     * 
     */
    public void setImpTotal(double value) {
        this.impTotal = value;
    }

    /**
     * Obtiene el valor de la propiedad impTotConc.
     * 
     */
    public double getImpTotConc() {
        return impTotConc;
    }

    /**
     * Define el valor de la propiedad impTotConc.
     * 
     */
    public void setImpTotConc(double value) {
        this.impTotConc = value;
    }

    /**
     * Obtiene el valor de la propiedad impNeto.
     * 
     */
    public double getImpNeto() {
        return impNeto;
    }

    /**
     * Define el valor de la propiedad impNeto.
     * 
     */
    public void setImpNeto(double value) {
        this.impNeto = value;
    }

    /**
     * Obtiene el valor de la propiedad impOpEx.
     * 
     */
    public double getImpOpEx() {
        return impOpEx;
    }

    /**
     * Define el valor de la propiedad impOpEx.
     * 
     */
    public void setImpOpEx(double value) {
        this.impOpEx = value;
    }

    /**
     * Obtiene el valor de la propiedad impTrib.
     * 
     */
    public double getImpTrib() {
        return impTrib;
    }

    /**
     * Define el valor de la propiedad impTrib.
     * 
     */
    public void setImpTrib(double value) {
        this.impTrib = value;
    }

    /**
     * Obtiene el valor de la propiedad impIVA.
     * 
     */
    public double getImpIVA() {
        return impIVA;
    }

    /**
     * Define el valor de la propiedad impIVA.
     * 
     */
    public void setImpIVA(double value) {
        this.impIVA = value;
    }

    /**
     * Obtiene el valor de la propiedad fchServDesde.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFchServDesde() {
        return fchServDesde;
    }

    /**
     * Define el valor de la propiedad fchServDesde.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFchServDesde(String value) {
        this.fchServDesde = value;
    }

    /**
     * Obtiene el valor de la propiedad fchServHasta.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFchServHasta() {
        return fchServHasta;
    }

    /**
     * Define el valor de la propiedad fchServHasta.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFchServHasta(String value) {
        this.fchServHasta = value;
    }

    /**
     * Obtiene el valor de la propiedad fchVtoPago.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFchVtoPago() {
        return fchVtoPago;
    }

    /**
     * Define el valor de la propiedad fchVtoPago.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFchVtoPago(String value) {
        this.fchVtoPago = value;
    }

    /**
     * Obtiene el valor de la propiedad monId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMonId() {
        return monId;
    }

    /**
     * Define el valor de la propiedad monId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMonId(String value) {
        this.monId = value;
    }

    /**
     * Obtiene el valor de la propiedad monCotiz.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMonCotiz() {
        return monCotiz;
    }

    /**
     * Define el valor de la propiedad monCotiz.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMonCotiz(Double value) {
        this.monCotiz = value;
    }

    /**
     * Obtiene el valor de la propiedad canMisMonExt.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCanMisMonExt() {
        return canMisMonExt;
    }

    /**
     * Define el valor de la propiedad canMisMonExt.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCanMisMonExt(String value) {
        this.canMisMonExt = value;
    }

    /**
     * Obtiene el valor de la propiedad condicionIVAReceptorId.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getCondicionIVAReceptorId() {
        return condicionIVAReceptorId;
    }

    /**
     * Define el valor de la propiedad condicionIVAReceptorId.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setCondicionIVAReceptorId(Integer value) {
        this.condicionIVAReceptorId = value;
    }

    /**
     * Obtiene el valor de la propiedad cbtesAsoc.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfCbteAsoc }
     *     
     */
    public ArrayOfCbteAsoc getCbtesAsoc() {
        return cbtesAsoc;
    }

    /**
     * Define el valor de la propiedad cbtesAsoc.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfCbteAsoc }
     *     
     */
    public void setCbtesAsoc(ArrayOfCbteAsoc value) {
        this.cbtesAsoc = value;
    }

    /**
     * Obtiene el valor de la propiedad tributos.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfTributo }
     *     
     */
    public ArrayOfTributo getTributos() {
        return tributos;
    }

    /**
     * Define el valor de la propiedad tributos.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfTributo }
     *     
     */
    public void setTributos(ArrayOfTributo value) {
        this.tributos = value;
    }

    /**
     * Obtiene el valor de la propiedad iva.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfAlicIva }
     *     
     */
    public ArrayOfAlicIva getIva() {
        return iva;
    }

    /**
     * Define el valor de la propiedad iva.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfAlicIva }
     *     
     */
    public void setIva(ArrayOfAlicIva value) {
        this.iva = value;
    }

    /**
     * Obtiene el valor de la propiedad opcionales.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfOpcional }
     *     
     */
    public ArrayOfOpcional getOpcionales() {
        return opcionales;
    }

    /**
     * Define el valor de la propiedad opcionales.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfOpcional }
     *     
     */
    public void setOpcionales(ArrayOfOpcional value) {
        this.opcionales = value;
    }

    /**
     * Obtiene el valor de la propiedad compradores.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfComprador }
     *     
     */
    public ArrayOfComprador getCompradores() {
        return compradores;
    }

    /**
     * Define el valor de la propiedad compradores.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfComprador }
     *     
     */
    public void setCompradores(ArrayOfComprador value) {
        this.compradores = value;
    }

    /**
     * Obtiene el valor de la propiedad periodoAsoc.
     * 
     * @return
     *     possible object is
     *     {@link Periodo }
     *     
     */
    public Periodo getPeriodoAsoc() {
        return periodoAsoc;
    }

    /**
     * Define el valor de la propiedad periodoAsoc.
     * 
     * @param value
     *     allowed object is
     *     {@link Periodo }
     *     
     */
    public void setPeriodoAsoc(Periodo value) {
        this.periodoAsoc = value;
    }

    /**
     * Obtiene el valor de la propiedad actividades.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfActividad }
     *     
     */
    public ArrayOfActividad getActividades() {
        return actividades;
    }

    /**
     * Define el valor de la propiedad actividades.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfActividad }
     *     
     */
    public void setActividades(ArrayOfActividad value) {
        this.actividades = value;
    }

}

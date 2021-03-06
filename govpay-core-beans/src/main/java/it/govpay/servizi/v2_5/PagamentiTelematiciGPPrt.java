package it.govpay.servizi.v2_5;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 3.1.5
 * 2018-07-04T14:49:50.409+02:00
 * Generated source version: 3.1.5
 * 
 */
@WebService(targetNamespace = "http://www.govpay.it/servizi/v2_5", name = "PagamentiTelematiciGPPrt")
@XmlSeeAlso({it.govpay.servizi.commons.ObjectFactory.class, it.govpay.servizi.v2_3.commons.ObjectFactory.class, it.govpay.servizi.v2_5.gpprt.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface PagamentiTelematiciGPPrt {

    @WebMethod(action = "gpChiediListaVersamenti")
    @WebResult(name = "gpChiediListaVersamentiResponse", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/", partName = "bodyrisposta")
    public it.govpay.servizi.v2_5.gpprt.GpChiediListaVersamentiResponse gpChiediListaVersamenti(
        @WebParam(partName = "bodyrichiesta", name = "gpChiediListaVersamenti", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/")
        it.govpay.servizi.v2_5.gpprt.GpChiediListaVersamenti bodyrichiesta
    );

    @WebMethod(action = "gpChiediStatoRichiestaStorno")
    @WebResult(name = "gpChiediStatoRichiestaStornoResponse", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/", partName = "bodyrisposta")
    public it.govpay.servizi.v2_5.gpprt.GpChiediStatoRichiestaStornoResponse gpChiediStatoRichiestaStorno(
        @WebParam(partName = "bodyrichiesta", name = "gpChiediStatoRichiestaStorno", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/")
        it.govpay.servizi.v2_5.gpprt.GpChiediStatoRichiestaStorno bodyrichiesta
    );

    @WebMethod(action = "gpChiediListaPsp")
    @WebResult(name = "gpChiediListaPspResponse", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/", partName = "bodyrisposta")
    public it.govpay.servizi.v2_5.gpprt.GpChiediListaPspResponse gpChiediListaPsp(
        @WebParam(partName = "bodyrichiesta", name = "gpChiediListaPsp", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/")
        it.govpay.servizi.v2_5.gpprt.GpChiediListaPsp bodyrichiesta
    );

    @WebMethod(action = "gpChiediStatoTransazione")
    @WebResult(name = "gpChiediStatoTransazioneResponse", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/", partName = "bodyrisposta")
    public it.govpay.servizi.v2_5.gpprt.GpChiediStatoTransazioneResponse gpChiediStatoTransazione(
        @WebParam(partName = "bodyrichiesta", name = "gpChiediStatoTransazione", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/")
        it.govpay.servizi.v2_5.gpprt.GpChiediStatoTransazione bodyrichiesta
    );

    @WebMethod(action = "gpAvviaTransazionePagamento")
    @WebResult(name = "gpAvviaTransazionePagamentoResponse", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/", partName = "bodyrisposta")
    public it.govpay.servizi.v2_5.gpprt.GpAvviaTransazionePagamentoResponse gpAvviaTransazionePagamento(
        @WebParam(partName = "bodyrichiesta", name = "gpAvviaTransazionePagamento", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/")
        it.govpay.servizi.v2_5.gpprt.GpAvviaTransazionePagamento bodyrichiesta,
        @WebParam(partName = "metaInfo", name = "metaInfo", targetNamespace = "http://www.govpay.it/servizi/commons/", header = true)
        it.govpay.servizi.commons.MetaInfo metaInfo
    );

    @WebMethod(action = "gpAvviaRichiestaStorno")
    @WebResult(name = "gpAvviaRichiestaStornoResponse", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/", partName = "bodyrisposta")
    public it.govpay.servizi.v2_5.gpprt.GpAvviaRichiestaStornoResponse gpAvviaRichiestaStorno(
        @WebParam(partName = "bodyrichiesta", name = "gpAvviaRichiestaStorno", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/")
        it.govpay.servizi.v2_5.gpprt.GpAvviaRichiestaStorno bodyrichiesta
    );

    @WebMethod(action = "gpChiediStatoVersamento")
    @WebResult(name = "gpChiediStatoVersamentoResponse", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/", partName = "bodyrisposta")
    public it.govpay.servizi.v2_5.gpprt.GpChiediStatoVersamentoResponse gpChiediStatoVersamento(
        @WebParam(partName = "bodyrichiesta", name = "gpChiediStatoVersamento", targetNamespace = "http://www.govpay.it/servizi/v2_5/gpPrt/")
        it.govpay.servizi.v2_5.gpprt.GpChiediStatoVersamento bodyrichiesta
    );
}

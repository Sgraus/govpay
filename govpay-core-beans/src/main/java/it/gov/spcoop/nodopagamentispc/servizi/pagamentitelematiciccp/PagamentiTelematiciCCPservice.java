package it.gov.spcoop.nodopagamentispc.servizi.pagamentitelematiciccp;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 3.1.5
 * 2018-07-04T14:49:50.003+02:00
 * Generated source version: 3.1.5
 * 
 */
@WebServiceClient(name = "PagamentiTelematiciCCPservice", 
                  wsdlLocation = "classpath:wsdl/PaPerNodoPagamentoPsp.wsdl",
                  targetNamespace = "http://NodoPagamentiSPC.spcoop.gov.it/servizi/PagamentiTelematiciCCP") 
public class PagamentiTelematiciCCPservice extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://NodoPagamentiSPC.spcoop.gov.it/servizi/PagamentiTelematiciCCP", "PagamentiTelematiciCCPservice");
    public final static QName PPTPort = new QName("http://NodoPagamentiSPC.spcoop.gov.it/servizi/PagamentiTelematiciCCP", "PPTPort");
    static {
        URL url = PagamentiTelematiciCCPservice.class.getClassLoader().getResource("wsdl/PaPerNodoPagamentoPsp.wsdl");
        if (url == null) {
            java.util.logging.Logger.getLogger(PagamentiTelematiciCCPservice.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "classpath:wsdl/PaPerNodoPagamentoPsp.wsdl");
        }       
        WSDL_LOCATION = url;   
    }

    public PagamentiTelematiciCCPservice(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public PagamentiTelematiciCCPservice(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public PagamentiTelematiciCCPservice() {
        super(WSDL_LOCATION, SERVICE);
    }
    




    /**
     *
     * @return
     *     returns PagamentiTelematiciCCP
     */
    @WebEndpoint(name = "PPTPort")
    public PagamentiTelematiciCCP getPPTPort() {
        return super.getPort(PPTPort, PagamentiTelematiciCCP.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns PagamentiTelematiciCCP
     */
    @WebEndpoint(name = "PPTPort")
    public PagamentiTelematiciCCP getPPTPort(WebServiceFeature... features) {
        return super.getPort(PPTPort, PagamentiTelematiciCCP.class, features);
    }

}

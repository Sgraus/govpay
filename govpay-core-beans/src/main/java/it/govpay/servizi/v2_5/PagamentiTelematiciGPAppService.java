package it.govpay.servizi.v2_5;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 3.1.5
 * 2018-07-04T14:49:50.281+02:00
 * Generated source version: 3.1.5
 * 
 */
@WebServiceClient(name = "PagamentiTelematiciGPAppService", 
                  wsdlLocation = "classpath:wsdl/GpApp_2.5.wsdl",
                  targetNamespace = "http://www.govpay.it/servizi/v2_5") 
public class PagamentiTelematiciGPAppService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://www.govpay.it/servizi/v2_5", "PagamentiTelematiciGPAppService");
    public final static QName GPAppPort = new QName("http://www.govpay.it/servizi/v2_5", "GPAppPort");
    static {
        URL url = PagamentiTelematiciGPAppService.class.getClassLoader().getResource("wsdl/GpApp_2.5.wsdl");
        if (url == null) {
            java.util.logging.Logger.getLogger(PagamentiTelematiciGPAppService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "classpath:wsdl/GpApp_2.5.wsdl");
        }       
        WSDL_LOCATION = url;   
    }

    public PagamentiTelematiciGPAppService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public PagamentiTelematiciGPAppService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public PagamentiTelematiciGPAppService() {
        super(WSDL_LOCATION, SERVICE);
    }
    




    /**
     *
     * @return
     *     returns PagamentiTelematiciGPApp
     */
    @WebEndpoint(name = "GPAppPort")
    public PagamentiTelematiciGPApp getGPAppPort() {
        return super.getPort(GPAppPort, PagamentiTelematiciGPApp.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns PagamentiTelematiciGPApp
     */
    @WebEndpoint(name = "GPAppPort")
    public PagamentiTelematiciGPApp getGPAppPort(WebServiceFeature... features) {
        return super.getPort(GPAppPort, PagamentiTelematiciGPApp.class, features);
    }

}

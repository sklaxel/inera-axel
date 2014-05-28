package se.inera.axel.ssek.common;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class SSEKHeaderInterceptor extends AbstractSoapInterceptor {
    private final Set<QName> handledHeaders;
    private QName ssekHeaderQName;

    public SSEKHeaderInterceptor() {
        super(Phase.USER_PROTOCOL);
        ssekHeaderQName = SsekNamespaces.SSEK.getQName("SSEK");
        handledHeaders = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ssekHeaderQName)));
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        Header ssekHeader = message.getHeader(ssekHeaderQName);
    }

    @Override
    public Set<QName> getUnderstoodHeaders() {
        return handledHeaders;
    }
}

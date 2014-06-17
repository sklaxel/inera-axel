package se.inera.axel.ssek.common;

import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.ReadHeadersInterceptor;
import org.apache.cxf.databinding.DataReader;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.ServiceModelUtil;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.w3c.dom.Node;
import se.inera.axel.ssek.common.schema.ssek.SSEK;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@ManagedResource
public class SSEKHeaderInterceptor extends AbstractSoapInterceptor {
    private final Set<QName> handledHeaders;
    private QName ssekHeaderQName;

    private String expectedReceiver;

    public SSEKHeaderInterceptor() {
        super(Phase.PRE_INVOKE);
        addAfter(ReadHeadersInterceptor.class.getName());
        ssekHeaderQName = SsekNamespaces.SSEK.getQName("SSEK");
        handledHeaders = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ssekHeaderQName)));
    }

    @ManagedAttribute
    public void setExpectedReceiver(String expectedReceiver) {
        this.expectedReceiver = expectedReceiver;
    }

    @ManagedAttribute
    public String getExpectedReceiver() {
        return expectedReceiver;
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        SoapHeader ssekHeader = (SoapHeader)message.getHeader(ssekHeaderQName);
        if(ssekHeader != null) {
            Service service =
                    ServiceModelUtil.getService(message.getExchange());
            DataReader<Node> ssekDataReader =
                    service.getDataBinding().createReader(Node.class);
            SSEK ssek =
                (SSEK)ssekDataReader.read(ssekHeaderQName,
                        (Node)ssekHeader.getObject(), SSEK.class);

            SSEK.ReceiverId receiverId = ssek.getReceiverId();

            if (expectedReceiver != null && (receiverId == null || !expectedReceiver.equals(receiverId.getValue()))) {
                throw  new SoapFault(String.format("SSEK receiver id '%s' does not equal expected receiver '%s'",
                        receiverId.getValue(),
                        expectedReceiver),
                        SsekNamespaces.SSEK.getQName("ReceiverIdUnknown"));
            }
        }
    }

    @Override
    public Set<QName> getUnderstoodHeaders() {
        return handledHeaders;
    }
}

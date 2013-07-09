package se.inera.axel.webconsole;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.broker.product.ProductService;
import se.inera.axel.shs.xml.product.ShsProduct;

import javax.ws.rs.PathParam;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RestProductService implements ProductService {
    Logger log = LoggerFactory.getLogger(RestProductService.class);
    URL address;

    public URL getAddress() {
        return address;
    }

    public void setAddress(URL address) {
        this.address = address;
    }

    private ProductService createProxy() {
        return JAXRSClientFactory.create(getAddress().toString(), ProductService.class);
    }

    @Override
    public ShsProduct getProduct(@PathParam("productId") String productTypeId) {
        try {
            return createProxy().getProduct(productTypeId);
        } catch (Exception e) {
            log.error("Cannot load product: " + productTypeId, e);
            // TODO do not return null, but throw exception. Seee JIRA issue.
            return null;
        }
    }

    @Override
    public List<ShsProduct> findAll() {
        try {
            return createProxy().findAll();
        } catch (Exception e) {
            log.error("Cannot load product list", e);
            // TODO do not return empty list, but throw exception. Seee JIRA issue.
            return new ArrayList<ShsProduct>();
        }

    }
}

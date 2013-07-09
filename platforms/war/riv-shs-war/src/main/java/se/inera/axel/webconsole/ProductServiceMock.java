package se.inera.axel.webconsole;

import se.inera.axel.shs.broker.product.ProductService;
import se.inera.axel.shs.xml.product.Principal;
import se.inera.axel.shs.xml.product.ShsProduct;

import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;

public class ProductServiceMock implements ProductService {
    ShsProduct product;

    public ProductServiceMock() {
        product = new ShsProduct();
        product.setCommonName("Mock Product");
        product.setDescription("This is just a test product");
        Principal principal = new Principal();
        principal.setvalue("0000000000");
        product.setPrincipal(principal);
        product.setUuid("00000000-0000-0000-0000-000000000001");
        product.setVersion("1.2");
    }

    @Override
    public ShsProduct getProduct(@PathParam("productId") String productTypeId) {
        if (productTypeId != null && productTypeId.equals(product.getUuid()))
            return product;

        return null;
    }

    @Override
    public List<ShsProduct> findAll() {
        List<ShsProduct> list = new ArrayList<ShsProduct>();
        list.add(product);
        return list;

    }
}

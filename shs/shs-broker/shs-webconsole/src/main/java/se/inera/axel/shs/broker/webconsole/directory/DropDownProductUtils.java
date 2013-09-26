package se.inera.axel.shs.broker.webconsole.directory;

import se.inera.axel.shs.broker.directory.ProductType;
import se.inera.axel.shs.xml.product.ShsProduct;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DropDownProductUtils {
    /**
     * Utility class should not be instantiated
     */
    private  DropDownProductUtils() {

    }

    public static DropdownProduct createDropdownProduct(ProductType productType) {
        return new DropdownProduct(productType.getSerialNumber(), productType.getProductName(), productType.getLabeledUri());
    }

    public static DropdownProduct createDropdownProduct(ShsProduct shsProduct) {
        return new DropdownProduct(shsProduct.getUuid(), shsProduct
                .getCommonName(), shsProduct.getLabeledURI());
    }
}

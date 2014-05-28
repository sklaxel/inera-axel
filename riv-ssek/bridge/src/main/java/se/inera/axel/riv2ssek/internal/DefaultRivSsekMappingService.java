package se.inera.axel.riv2ssek.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.riv2ssek.RivSsekServiceMapping;
import se.inera.axel.riv2ssek.RivSsekServiceMappingRepository;

import javax.annotation.Resource;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class DefaultRivSsekMappingService implements RivSsekMappingService {
    private static final Logger log = LoggerFactory.getLogger(DefaultRivSsekMappingService.class);

    @Resource
    private RivSsekServiceMappingRepository repository;

    @Override
    public RivSsekServiceMapping lookupSsekService(String receiver, String serviceNamespace) {
        log.debug("lookupSsekService({}, {})", receiver, serviceNamespace);
        RivSsekServiceMapping rivSsekServiceMapping = repository.findByRivLogicalAddressAndRivServiceNamespace(receiver, serviceNamespace);
        if (rivSsekServiceMapping == null) {
            throw new RuntimeException(
                    String.format("Could not find a mapping for RIV service namespace %s to SSEK receiver %s",
                            serviceNamespace, receiver));
        }

        return rivSsekServiceMapping;
    }
}

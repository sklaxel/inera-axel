package se.inera.axel.riv2ssek.internal;

import se.inera.axel.riv2ssek.RivSsekServiceMapping;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public interface RivSsekMappingService {
    /**
     * Tries to find a mapping and sets the headers with the mapping information.
     *
     */
    public RivSsekServiceMapping lookupSsekService(String receiver, String serviceNamespace);
}

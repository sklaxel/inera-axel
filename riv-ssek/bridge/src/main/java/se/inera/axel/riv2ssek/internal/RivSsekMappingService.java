package se.inera.axel.riv2ssek.internal;

import se.inera.axel.riv2ssek.SsekServiceInfo;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public interface RivSsekMappingService {
    /**
     * Tries to find a mapping and sets the headers with the mapping information.
     *
     */
    public SsekServiceInfo lookupSsekService(String receiver, String serviceNamespace);
}

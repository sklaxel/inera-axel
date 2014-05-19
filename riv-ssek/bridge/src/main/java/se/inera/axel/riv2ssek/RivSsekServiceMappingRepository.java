package se.inera.axel.riv2ssek;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public interface RivSsekServiceMappingRepository extends PagingAndSortingRepository<SsekServiceInfo, String> {
    SsekServiceInfo findByReceiverAndRivServiceNamespace(String receiver, String serviceNamespace);
}

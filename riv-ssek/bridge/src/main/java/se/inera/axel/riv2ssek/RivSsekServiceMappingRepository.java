package se.inera.axel.riv2ssek;

import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public interface RivSsekServiceMappingRepository extends PagingAndSortingRepository<RivSsekServiceMapping, String> {
    RivSsekServiceMapping findByRivLogicalAddressAndRivServiceNamespace(String receiver, String serviceNamespace);
}

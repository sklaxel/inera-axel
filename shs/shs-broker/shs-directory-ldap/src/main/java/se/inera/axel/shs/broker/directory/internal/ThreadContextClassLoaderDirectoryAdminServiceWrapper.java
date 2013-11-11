/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.broker.directory.internal;

import se.inera.axel.shs.broker.directory.*;

import java.util.List;

/**
 * When the DirectoryAdminService is used in osgi from a bundle that does not
 * have the Spring Ldap packages imported the Sun LDAP classes cannot instantiate
 * the Spring LDAP classes since the Threads current class loader cannot find them.
 *
 * <p>This wrapper changes the TCCL to the classloader that loaded this class
 * so that the Spring LDAP classes can be found.</p>
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class ThreadContextClassLoaderDirectoryAdminServiceWrapper implements DirectoryAdminService {
    private DirectoryAdminService directoryAdminService;

    public ThreadContextClassLoaderDirectoryAdminServiceWrapper(DirectoryAdminService directoryAdminService) {
        this.directoryAdminService = directoryAdminService;
    }

    @Override
    public List<Organization> getOrganizations() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            return directoryAdminService.getOrganizations();
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public void saveProduct(Organization organization, ProductType product) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            directoryAdminService.saveProduct(organization, product);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public ProductType getProductType(String orgNumber, String productId) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return directoryAdminService.getProductType(orgNumber, productId);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public List<Agreement> findAgreements(String orgNumber, String productId) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return directoryAdminService.findAgreements(orgNumber, productId);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Deprecated
    @Override
    public Agreement getAgreement(String orgNumber, String productId, String transferType) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return directoryAdminService.getAgreement(orgNumber, productId, transferType);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public Organization getOrganization(String orgNumber) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return directoryAdminService.getOrganization(orgNumber);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public List<Agreement> getAgreements(Organization organization) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return directoryAdminService.getAgreements(organization);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public void removeAddress(Organization organization, Address Address) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            directoryAdminService.removeAddress(organization, Address);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public Agreement lookupAgreement(Organization organization, String serialNumber) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return directoryAdminService.lookupAgreement(organization, serialNumber);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public void deleteActor(Organization organization) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            directoryAdminService.deleteActor(organization);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public Address getAddress(String orgNumber, String productId) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return directoryAdminService.getAddress(orgNumber, productId);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public void saveActor(Organization organization) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            directoryAdminService.saveActor(organization);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public List<Address> getAddresses(Organization organization) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return directoryAdminService.getAddresses(organization);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public void removeProduct(Organization organization, ProductType product) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            directoryAdminService.removeProduct(organization, product);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public void removeAgreement(Organization organization, Agreement agreement) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            directoryAdminService.removeAgreement(organization, agreement);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public List<ProductType> getProductTypes(Organization organization) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            return directoryAdminService.getProductTypes(organization);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public void saveAgreement(Organization organization, Agreement agreement) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            directoryAdminService.saveAgreement(organization, agreement);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    @Override
    public void saveAddress(Organization organization, Address address) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            directoryAdminService.saveAddress(organization, address);

        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }
}

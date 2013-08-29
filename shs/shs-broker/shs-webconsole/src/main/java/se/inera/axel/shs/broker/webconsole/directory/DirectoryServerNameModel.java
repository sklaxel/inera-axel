package se.inera.axel.shs.broker.webconsole.directory;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import se.inera.axel.shs.broker.directory.DirectoryAdminServiceRegistry;
import se.inera.axel.shs.broker.webconsole.WicketApplication;

import java.util.List;

/**
* @author Jan Hallonstén, jan.hallonsten@r2m.se
*/
class DirectoryServerNameModel implements IModel<String> {
    private DirectoryAdminServiceRegistry directoryAdminServiceRegistry;

    public DirectoryServerNameModel(DirectoryAdminServiceRegistry directoryAdminServiceRegistry) {
        this.directoryAdminServiceRegistry = directoryAdminServiceRegistry;
    }

    @Override
    public String getObject() {

        String directoryServerName = Session.get().getMetaData(WicketApplication.DIRECTORY_SERVER_NAME_KEY);
        List<String> serverNames = directoryAdminServiceRegistry.getServerNames();
        if (!serverNames.contains(directoryServerName)) {
            directoryServerName = serverNames.get(0);
        }

        return directoryServerName;
    }

    @Override
    public void setObject(String object) {
        // TODO validation?
        Session.get().setMetaData(WicketApplication.DIRECTORY_SERVER_NAME_KEY, object);
    }

    @Override
    public void detach() {
    }
}

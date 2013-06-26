package se.inera.axel.shs.cmdline;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all shs-cmdline commands.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public abstract class ShsBaseCommand {
    @Parameter(names = {"--corrId"}, description = "Correlation Id (Defaults to TxId of a new message)")
    public String shsCorrId;

    @Parameter(names = {"-E", "--endRecipient"}, description = "End recipient")
    public String shsEndRecipient;

    @Parameter(names = {"-t", "--to"}, description = "To address (OrgNbr)")
    public String shsTo;

    @DynamicParameter(names = {"-m", "--meta"}, description = "Meta data. The meta parameter is allowed multiple times.")
    public Map<String, String> meta = new HashMap<String, String>();

    public static ApplicationContext createApplicationContext() {
        String contextFile = System.getProperty("spring-context-location", "classpath:shs-cmdline-context.xml");

        return new ClassPathXmlApplicationContext(contextFile);
    }
}

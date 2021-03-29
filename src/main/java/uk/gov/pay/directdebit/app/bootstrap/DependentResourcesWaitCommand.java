package uk.gov.pay.directdebit.app.bootstrap;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import uk.gov.pay.directdebit.app.config.DirectDebitConfig;
import uk.gov.service.payments.commons.utils.startup.ApplicationStartupDependentResourceChecker;
import uk.gov.service.payments.commons.utils.startup.DatabaseStartupResource;

public class DependentResourcesWaitCommand extends ConfiguredCommand<DirectDebitConfig> {

    public DependentResourcesWaitCommand() {
        super("waitOnDependencies", "Waits for dependent resources to become available");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
    }

    @Override
    protected void run(Bootstrap<DirectDebitConfig> bs, Namespace ns, DirectDebitConfig conf) {
        new ApplicationStartupDependentResourceChecker(new DatabaseStartupResource(conf.getDataSourceFactory()))
                .checkAndWaitForResource();
    }
}

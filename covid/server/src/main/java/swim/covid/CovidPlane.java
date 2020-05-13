package swim.covid;

import swim.api.SwimRoute;
import swim.api.agent.AgentRoute;
import swim.api.plane.AbstractPlane;
import swim.client.ClientRuntime;
import swim.kernel.Kernel;
import swim.server.ServerLoader;

import java.io.FileNotFoundException;

public class CovidPlane extends AbstractPlane {

    @SwimRoute("/covid/:id")
    private AgentRoute<UnitAgent> unitAgent;

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        final Kernel kernel = ServerLoader.loadServer();

        kernel.start();
        System.out.println("Running Covid plane...");
        kernel.run();

        final ClientRuntime client = new ClientRuntime();
        client.start();
        final DataSource ds = new DataSource(client, "warp://localhost:9001");
        ds.sendCommands();
    }

}

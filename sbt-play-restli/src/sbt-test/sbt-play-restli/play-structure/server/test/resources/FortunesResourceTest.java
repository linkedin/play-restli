package resources;

import com.example.fortune.Fortune;
import com.example.fortune.FortunesRequestBuilders;
import com.linkedin.common.callback.FutureCallback;
import com.linkedin.r2.RemoteInvocationException;
import com.linkedin.r2.transport.common.Client;
import com.linkedin.r2.transport.common.bridge.client.TransportClient;
import com.linkedin.r2.transport.common.bridge.client.TransportClientAdapter;
import com.linkedin.r2.transport.http.client.HttpClientFactory;
import com.linkedin.restli.client.GetRequest;
import com.linkedin.restli.client.Response;
import com.linkedin.restli.client.RestClient;
import java.util.Collections;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithServer;

import static org.junit.Assert.*;

public class FortunesResourceTest extends WithServer {
  @Override
  protected Application provideApplication() {
    return new GuiceApplicationBuilder().build();
  }

  @Test
  public void testFortunesResource() {
    final HttpClientFactory http = new HttpClientFactory.Builder().build();
    final TransportClient transportClient = http.getClient(Collections.<String, String>emptyMap());

    // create an abstraction layer over the actual client, which supports both REST and RPC
    final Client r2Client = new TransportClientAdapter(transportClient);

    // REST client wrapper that simplifies the interface
    final RestClient restClient = new RestClient(r2Client, "http://localhost:" + port + "/");

    FortunesRequestBuilders requestBuilders = new FortunesRequestBuilders();
    GetRequest<Fortune> request = requestBuilders.get().id(1L).build();

    Response<Fortune> response;
    try {
      response = restClient.sendRequest(request).getResponse();
    } catch (RemoteInvocationException ex) {
      throw new RuntimeException(ex);
    }

    assertFalse(response.hasError());
    assertEquals(200, response.getStatus());

    Fortune fortune = response.getEntity();

    assertEquals("Today is your lucky day.", fortune.getFortune());

    http.shutdown(new FutureCallback<>());
  }
}

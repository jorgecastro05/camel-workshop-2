import com.workshops.eips.MySpringBootApplication;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.apache.camel.test.spring.UseAdviceWith;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest(classes = MySpringBootApplication.class)
@MockEndpoints
@UseAdviceWith
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CamelTest {


    @Autowired
    private CamelContext context;
    @Autowired
    private ProducerTemplate template;

    private static final String inputAnimals = "src/test/resources/animals.json";
    private static final String outputZoo = "src/test/resources/zoo-CN.json";
    private static final String inputCars = "src/test/resources/cars_in.json";
    private static final String outputCars = "src/test/resources/cars_out.json";


    @Test
    public void testZoo() throws Exception {

        context.start();

        template.requestBodyAndHeader("file:data/inbox/animals",
                new File(inputAnimals), "CamelFileName", "animals.json");

        MockEndpoint mockEndpoint = context.getEndpoint("mock:file:data/outbox/animals", MockEndpoint.class);

        mockEndpoint.expectedMessageCount(129);
        mockEndpoint.whenAnyExchangeReceived(exchange -> {
            String filename = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
            if (filename.equals("zoo-CN.json")) {
                String fileContent = exchange.getIn().getBody(String.class);
                String fileExpected = context.getTypeConverter().convertTo(String.class, new File(outputZoo));
                assertEquals(fileContent, fileExpected);
            }
        });

        mockEndpoint.assertIsSatisfied(15000);

    }

    @Test
    public void testCars() throws Exception {

        context.start();

        template.requestBodyAndHeader("file:data/inbox/json",
                new File(inputCars), "CamelFileName", "cars.json");

        MockEndpoint mockEndpoint = context.getEndpoint("mock:file:data/outbox/cars", MockEndpoint.class);

        File target = new File(outputCars);
        String content = context.getTypeConverter().convertTo(String.class, target);

        mockEndpoint.expectedMinimumMessageCount(1);
        mockEndpoint.message(0).header("CamelFileName").isEqualTo("cars.json");
        mockEndpoint.expectedBodiesReceived(content);
        mockEndpoint.assertIsSatisfied();

    }


}

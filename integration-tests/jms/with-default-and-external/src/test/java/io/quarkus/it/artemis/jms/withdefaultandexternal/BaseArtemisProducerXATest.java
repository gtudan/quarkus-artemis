package io.quarkus.it.artemis.jms.withdefaultandexternal;

import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public abstract class BaseArtemisProducerXATest extends BaseArtemisProducerTest {
    @Test
    void testXADefault() throws Exception {
        testXA(createDefaultContext(), "/artemis/xa", "test-jms-default");
    }

    @Test
    void testXANamedOne() throws Exception {
        testXA(createNamedOneContext(), "/artemis/named-1/xa", "test-jms-named-1");
    }

    protected void testXA(JMSContext context, String endpoint, String queueName) throws JMSException {
        String body = createBody();
        Response response = RestAssured.with().body(body).post(endpoint);
        Assertions.assertEquals(javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode(), response.statusCode());

        try (JMSContext autoClosedContext = context) {
            JMSConsumer consumer = autoClosedContext.createConsumer(autoClosedContext.createQueue(queueName));
            Message message = consumer.receive(1000L);
            Assertions.assertEquals(body, message.getBody(String.class));
        }
    }

    @Test
    public void testRollbackDefault() {
        testRollback(createDefaultContext(), "/artemis/xa", "test-jms-default");
    }

    @Test
    public void testRollbackNamedOne() {
        testRollback(createNamedOneContext(), "/artemis/named-1/xa", "test-jms-named-1");
    }

    protected void testRollback(JMSContext context, String endpoint, String queueName) {
        Response response = RestAssured.with().body("fail").post(endpoint);
        Assertions.assertEquals(javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode(), response.statusCode());

        try (JMSContext autoClosedContext = context) {
            JMSConsumer consumer = autoClosedContext.createConsumer(autoClosedContext.createQueue(queueName));
            Message message = consumer.receive(1000L);
            Assertions.assertNull(message);
        }
    }
}
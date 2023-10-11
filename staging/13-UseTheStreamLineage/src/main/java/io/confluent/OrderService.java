package io.confluent;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class OrderService {
    private static final String SERVICE_NAME = "OrderService";
    private static final String ORDER_CREATED = "OrderCreated";
    private final Random rnd = new Random();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(final String[] args) {
        OrderService orderService = new OrderService();
        orderService.execute();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void execute() {
        try {
            Properties props = ProducerConfig.load(SERVICE_NAME, "java.config");
            props.setProperty("client.id", "OrderService");

            try (KafkaProducer<String, OrderCreated> producer = new KafkaProducer<>(props)) {

                while(true) {
                    final String orderId = "order-" + rnd.nextInt(100);
                    final String email = "email" + rnd.nextInt(100) + "@fake.com"; // TODO: Use this when constructing the OrderCreated below

                    List<Item> items = new ArrayList<>();
                    for(int i = 0; i<rnd.nextInt(5); i++) {
                        final String itemId = "item-" + rnd.nextInt(100);
                        final int quantity = rnd.nextInt(10);
                        items.add(new Item(itemId, quantity));
                    }

                    final OrderCreated order = new OrderCreated(orderId, email, items);
                    final ProducerRecord<String, OrderCreated> record = new ProducerRecord<>(ORDER_CREATED, orderId, order);

                    logger.info("Producing {}: OrderId = {}", ORDER_CREATED, orderId);

                    producer.send(record);
                    producer.flush();
                    Thread.sleep(1000L);
                }
            } catch (final SerializationException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
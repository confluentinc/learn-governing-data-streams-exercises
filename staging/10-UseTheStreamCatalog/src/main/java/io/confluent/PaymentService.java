package io.confluent;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;

public class PaymentService {
    private static final String SERVICE_NAME = "PaymentService";
    private static final String ORDER_CREATED = "OrderCreated";
    private static final String PAYMENT_SUCCEEDED = "PaymentSucceeded";
    private final Random rnd = new Random();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(final String[] args) {
        PaymentService paymentService = new PaymentService();
        paymentService.execute();
    }

    public void execute() {
        try {
            Properties consumerProps = ConsumerConfig.load(SERVICE_NAME, "java.config");
            Properties producerProps = ProducerConfig.load(SERVICE_NAME, "java.config");

            KafkaConsumer<String, OrderCreated> orderCreatedConsumer = new KafkaConsumer<>(consumerProps);
            orderCreatedConsumer.subscribe(Collections.singletonList(ORDER_CREATED));

            KafkaProducer<String, Object> paymentResultProducer = new KafkaProducer<>(producerProps);

            while (true) {
                ConsumerRecords<String, OrderCreated> records = orderCreatedConsumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, OrderCreated> orderRecord : records) {
                    OrderCreated order = orderRecord.value();

                    logger.info("Processing OrderCreated: OrderId = {}", order.getOrderId());

                    final String paymentId = "PaymentId" + rnd.nextInt(1000);

                    final PaymentSucceeded payment = new PaymentSucceeded(paymentId, order.getOrderId(), rnd.nextDouble(0, 1000));
                    final ProducerRecord<String, Object> paymentRecord = new ProducerRecord<>(PAYMENT_SUCCEEDED, paymentId, payment);
                    logger.info("Producing {}: PaymentId = {}, OrderId = {}", PAYMENT_SUCCEEDED, paymentId, order.getOrderId());
                    paymentResultProducer.send(paymentRecord);
                }

                paymentResultProducer.flush();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

}
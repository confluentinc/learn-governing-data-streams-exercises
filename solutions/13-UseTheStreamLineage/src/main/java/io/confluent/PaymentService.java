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
    private static final String PAYMENT_FAILED = "PaymentFailed";
    private final Random rnd = new Random();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(final String[] args) {
        PaymentService paymentService = new PaymentService();
        paymentService.execute();
    }

    public void execute() {
        try {
            Properties consumerProps = ConsumerConfig.load(SERVICE_NAME, "java.config");
            consumerProps.setProperty("client.id", "PaymentServiceConsumer");
            consumerProps.setProperty("group.id", "PaymentServiceConsumer");
            Properties paymentSucceededProducerProps = ProducerConfig.load(SERVICE_NAME, "java.config");
            paymentSucceededProducerProps.setProperty("client.id", "PaymentSucceededProducer");
            paymentSucceededProducerProps.setProperty("client.id", "PaymentSucceededProducer");
            Properties paymentFailedProducerProps = ProducerConfig.load(SERVICE_NAME, "java.config");
            paymentFailedProducerProps.setProperty("client.id", "PaymentFailedProducer");

            KafkaConsumer<String, OrderCreated> orderCreatedConsumer = new KafkaConsumer<>(consumerProps);
            orderCreatedConsumer.subscribe(Collections.singletonList(ORDER_CREATED));

            KafkaProducer<String, PaymentSucceeded> paymentSucceededProducer = new KafkaProducer<>(paymentSucceededProducerProps);
            KafkaProducer<String, PaymentFailed> paymentFailedProducer = new KafkaProducer<>(paymentFailedProducerProps);

            while (true) {
                ConsumerRecords<String, OrderCreated> records = orderCreatedConsumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, OrderCreated> orderRecord : records) {
                    OrderCreated order = orderRecord.value();

                    logger.info("Processing OrderCreated: OrderId = {}", order.getOrderId());

                    final String paymentId = "PaymentId" + rnd.nextInt(1000);

                    if(rnd.nextInt(100) >=10) {
                        final PaymentSucceeded payment = new PaymentSucceeded(paymentId, order.getOrderId(), rnd.nextDouble(0, 1000));
                        final ProducerRecord<String, PaymentSucceeded> paymentRecord = new ProducerRecord<>(PAYMENT_SUCCEEDED, paymentId, payment);
                        logger.info("Producing {}: PaymentId = {}, OrderId = {}", PAYMENT_SUCCEEDED, paymentId, order.getOrderId());
                        paymentSucceededProducer.send(paymentRecord);
                    } else {
                        final PaymentFailed payment = new PaymentFailed(paymentId, order.getOrderId(), rnd.nextDouble(0, 1000), "Insufficient Funds");
                        final ProducerRecord<String, PaymentFailed> paymentRecord = new ProducerRecord<>(PAYMENT_FAILED, paymentId, payment);
                        logger.info("Producing {}: PaymentId = {}, OrderId = {}", PAYMENT_FAILED, paymentId, order.getOrderId());
                        paymentFailedProducer.send(paymentRecord);
                    }
                }

                paymentSucceededProducer.flush();
                paymentFailedProducer.flush();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

}
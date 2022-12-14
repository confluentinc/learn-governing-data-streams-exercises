package io.confluent;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ShippingService {
    private static final String SERVICE_NAME = "ShippingService";
    private static final String PAYMENT_SUCCEEDED = "PaymentSucceeded";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(final String[] args) {
        ShippingService shippingService = new ShippingService();
        shippingService.execute();
    }

    public void execute() {
        try {
            Properties consumerProps = ConsumerConfig.load(SERVICE_NAME, "java.config");

            KafkaConsumer<String, PaymentSucceeded> consumer = new KafkaConsumer<>(consumerProps);
            consumer.subscribe(Collections.singletonList(PAYMENT_SUCCEEDED));

            while (true) {
                ConsumerRecords<String, PaymentSucceeded> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, PaymentSucceeded> paymentRecord : records) {
                    PaymentSucceeded payment = paymentRecord.value();

                    logger.info("Processing PaymentSucceeded: PaymentId = {}, OrderId = {}", payment.getPaymentId(), payment.getOrderId());
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }
}

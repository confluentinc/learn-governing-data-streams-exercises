package io.confluent;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConsumerConfig {
    public static Properties load(String clientId, String file) throws IOException {
        final Properties props = new Properties();

        try (final InputStream input = ConsumerConfig.class.getClassLoader().getResourceAsStream(file)) {
            props.load(input);
        } catch (IOException ex) {
            throw new IOException("Unable to load configuration from: "+file);
        }

        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, clientId);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        return props;
    }
}

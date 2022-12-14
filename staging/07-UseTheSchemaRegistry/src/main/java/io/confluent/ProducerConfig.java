package io.confluent;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProducerConfig {
    public static Properties load(String clientId, String file) throws IOException {
        final Properties props = new Properties();

        try (final InputStream input = ProducerConfig.class.getClassLoader().getResourceAsStream(file)) {
            props.load(input);
        } catch (IOException ex) {
            throw new IOException("Unable to load configuration from: "+file);
        }

        props.put(org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        return props;
    }
}

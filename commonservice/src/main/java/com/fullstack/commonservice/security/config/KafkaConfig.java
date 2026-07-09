package com.fullstack.commonservice.security.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    /**
     * Cấu hình "Nhà máy" tạo Producer.
     * Thiết lập các tham số cơ bản giúp ứng dụng biết gửi tin đến đâu và mã hóa dữ liệu như thế nào.
     *
     * @return ProducerFactory cấu hình cho Key và Value đều là dạng String
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // Cấu hình địa chỉ Kafka Cluster
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);

        // Mã hóa Key (Khóa) từ object Java (String) thành chuỗi Byte để gửi đi qua mạng
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);

        // Mã hóa Value (Nội dung tin nhắn) từ String thành chuỗi Byte
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Bean cung cấp công cụ cốt lõi để gửi tin nhắn (Produce message) lên Kafka Topic.
     * Các Service khác sẽ inject KafkaTemplate này để gọi hàm send().
     *
     * @return KafkaTemplate sử dụng producerFactory ở trên
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Cấu hình "Nhà máy" tạo Consumer.
     * Thiết lập cách thức lắng nghe, định danh nhóm và giải mã dữ liệu nhận về.
     *
     * @return ConsumerFactory cấu hình giải mã chuỗi Byte về lại String
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Cấu hình địa chỉ Kafka Cluster
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);

        // Cấu hình Group ID định danh nhóm Consumer để Kafka phân phối tin nhắn chuẩn xác
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                consumerGroupId);

        // Giải mã Key từ chuỗi Byte nhận được ngược lại thành String
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        // Giải mã Value (nội dung tin) từ chuỗi Byte ngược lại thành String
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Bean quản lý vòng đời và luồng (threads) cho các @KafkaListener.
     * Container Factory này giúp Spring tự động tạo các listener chạy ngầm để hứng tin nhắn mới.
     *
     * @return ConcurrentKafkaListenerContainerFactory cho phép xử lý đồng thời (concurrent)
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // Gắn nhà máy Consumer vào container
        factory.setConsumerFactory(consumerFactory());

        return factory;
    }
}


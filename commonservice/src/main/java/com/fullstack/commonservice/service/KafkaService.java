package com.fullstack.commonservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class KafkaService {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    public void sendMessage(String topic,String message){
        try {
            kafkaTemplate.send(topic,message).get();
            log.info("Message sent to topic: {}", topic);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while sending Kafka message to " + topic, exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Could not send Kafka message to " + topic, exception);
        }
    }
}

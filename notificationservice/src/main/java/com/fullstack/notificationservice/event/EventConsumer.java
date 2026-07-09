package com.fullstack.notificationservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fullstack.commonservice.notification.event.EmailVerificationRequestedEvent;
import com.fullstack.commonservice.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.RetriableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class EventConsumer {

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.frontend.login-url}")
    private String loginUrl;

    @KafkaListener(topics = "${app.kafka.topics.email-verification}", containerFactory = "kafkaListenerContainerFactory")
    public void sendVerificationEmail(String message) throws JsonProcessingException {
        EmailVerificationRequestedEvent event = objectMapper.readValue(message, EmailVerificationRequestedEvent.class);
        String html = """
                <div>
                    <h2>Xac thuc tai khoan</h2>
                    <p>Cam on ban da dang ky Watch Commerce.</p>
                    <p>Bam vao link ben duoi de xac thuc email:</p>
                    <p><a href="%s">Xac thuc email</a></p>
                    <p>Sau khi xac thuc, ban co the dang nhap tai <a href="%s">trang dang nhap</a>.</p>
                </div>
                """.formatted(event.verificationLink(), loginUrl);

        emailService.sendEmail(event.email(), "Xac thuc tai khoan Watch Commerce", html, true, null);
    }

    @RetryableTopic(
            attempts = "4", // 3 topic retry + 1 topic DLQ
            backoff = @Backoff(delay = 1000,multiplier = 2),
            autoCreateTopics = "true",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            include = {RetriableException.class,RuntimeException.class}
    )
    @KafkaListener(topics = "test",containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message){
        log.info("Received message: " +message);
        // processing message
        throw new RuntimeException("Errror  test");
    }

    @DltHandler
    void processDltMessage(@Payload String messsage){
        log.info("DLT receive message: "+messsage);
        // send mail if have error message in dead letter queue
    }

    @KafkaListener(topics = "testEmail",containerFactory = "kafkaListenerContainerFactory")
    public void testEmail(String message){
        log.info("Received message: " +message);

        String template = "<div>\n" +
                "    <h1>Welcome, %s!</h1>\n" +
                "    <p>Thank you for joining us. We're excited to have you on board.</p>\n" +
                "    <p>Your username is: <strong>%s</strong></p>\n" +
                "</div>";
        String filledTemplate = String.format(template,"Van Giang",message);

        emailService.sendEmail(message,"Thanks for buy my course",filledTemplate,true,null);
    }

    @KafkaListener(topics = "emailTemplate",containerFactory = "kafkaListenerContainerFactory")
    public void emailTemplate(String message){
        log.info("Received message: " +message);

        Map<String,Object> placeholders = new HashMap<>();
        placeholders.put("name","Lap trinh FullStack");

        emailService.sendEmailWithTemplate(message,"Welcome to Christmas","emailTemplate.ftl",placeholders,null);
    }
}

package com.eos.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailSenderConfig {

    @Bean(name = "mailSender1")
    @Primary // This will be used by default unless otherwise specified
    public JavaMailSender mailSender1(
            @Value("${custom-mail.mail1.host}") String host,
            @Value("${custom-mail.mail1.port}") int port,
            @Value("${custom-mail.mail1.username}") String username,
            @Value("${custom-mail.mail1.password}") String password
    ) {
        return createMailSender(host, port, username, password);
    }

    @Bean(name = "mailSender2")
    public JavaMailSender mailSender2(
            @Value("${custom-mail.mail2.host}") String host,
            @Value("${custom-mail.mail2.port}") int port,
            @Value("${custom-mail.mail2.username}") String username,
            @Value("${custom-mail.mail2.password}") String password
    ) {
        return createMailSender(host, port, username, password);
    }

    private JavaMailSender createMailSender(String host, int port, String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }
}

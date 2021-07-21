package com.home.project.stocks.service;

import com.home.project.stocks.model.processing.ProcessingResult;
import freemarker.template.TemplateException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Log4j2
public class NotifierService {

    private JavaMailSender sender;
    private FreeMarkerConfigurer freemarkerConfig;

    @Autowired
    public void setFreemarkerConfig(FreeMarkerConfigurer freemarkerConfig) {
        this.freemarkerConfig = freemarkerConfig;
    }

    @Autowired
    public void setSender(JavaMailSender sender) {
        this.sender = sender;
    }

    public void notifyAdmin(Set<ProcessingResult> processingResultSet) {
        var stocks = processingResultSet.stream().map(ProcessingResult::getTicker)
                    .collect(Collectors.toList());
        var mimeMessage = sender.createMimeMessage();
        try {
            var helper = new MimeMessageHelper(mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            var templateContent = FreeMarkerTemplateUtils
                    .processTemplateIntoString(
                            freemarkerConfig.getConfiguration().getTemplate("/email/welcome.ftlh"),
                            Map.of("stocks", stocks));
            helper.setTo("ruslanlagai@mail.ru");
            helper.setSubject("Stocks to check");
            helper.setText(templateContent);
            sender.send(mimeMessage);
        } catch (IOException e) {
            log.error("Unable to find email template. \n" + e.getMessage());
        } catch (TemplateException e) {
            log.error("Failed to parse template \n" + e.getMessage());
        } catch (MessagingException e) {
            log.error("Failed to send email . \n" + e.getMessage());
        }
    }
}

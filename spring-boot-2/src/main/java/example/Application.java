package example;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.google.gson.JsonElement;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootApplication
@ServletComponentScan
//@EntityScan("example.database")
@ComponentScan
//@EnableJpaRepositories(basePackages = "example.database")

public class Application {
    private static String webHookUrl = "";
    private static String slackChannel = "epibuildstaff";

    public static void main(String[] args) {
        ApplicationContext myapp = new AnnotationConfigApplicationContext(SlackApp.class);
        SpringApplication.run(Application.class, args);
        sendMessageToSlack("EpiBuildStaff Application is up now!");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println("Now is"+dtf.format(now));
    }

    public static void sendMessageToSlack(String message){
        try {
            StringBuilder msbuilder = new StringBuilder();
            msbuilder.append(message);

            Payload payload = Payload.builder().channel(slackChannel).text(msbuilder.toString()).build();

            WebhookResponse wbResp = Slack.getInstance().send(webHookUrl, payload);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

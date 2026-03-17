package ua.fpv;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;


@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        SecurityAutoConfiguration.class
})
public class FpvBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(FpvBotApplication.class, args);
    }

    @Bean
    public CommandLineRunner debugRunner(ApplicationContext ctx) {
        return args -> {
            System.out.println("--- ПЕРЕВІРКА БІНІВ ---");
            boolean botExists = ctx.containsBean("fpvReportTelegramBot");
            System.out.println("Бот знайдений у контексті: " + botExists);
            System.out.println("-----------------------");
        };
    }
}

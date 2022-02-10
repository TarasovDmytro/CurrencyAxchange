package ua.tarasov.currencyexchange;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CurrencyExchangeApplication {

    public static void main(String[] args) {
        Application.launch(FXApplication.class, args);
    }

}

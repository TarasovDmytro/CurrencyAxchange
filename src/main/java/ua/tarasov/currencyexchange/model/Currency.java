package ua.tarasov.currencyexchange.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Currency {
    private String currencyName;
    private String currencyFullName;
    private Double currencyRate;

    @Override
    public String toString() {
        return currencyName + " (" + currencyFullName + ")";
    }
}

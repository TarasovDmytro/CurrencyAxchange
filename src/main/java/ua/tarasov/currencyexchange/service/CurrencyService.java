package ua.tarasov.currencyexchange.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ua.tarasov.currencyexchange.model.Currency;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

@Service
@Data
public class CurrencyService {
    private final Gson jsonConverter = new Gson();
    private final HashMap<String, Currency> currencyHashMap = new HashMap<>();
    private final HashMap<String, Currency> myCurrencyHashMap = new HashMap<>();

    @Scheduled(fixedDelay = 60000)
    @PostConstruct
    public void setCurrencyHashMap() {
        try {
            String response = getContent("https://openexchangerates.org/api/currencies.json");
            String currencyNamesJsonMap = response.replace(":", "=");
            HashMap<String, String> currencyNamesMap = jsonConverter.fromJson(currencyNamesJsonMap,
                    new TypeToken<HashMap<String, String>>() {}.getType());
            HashMap<String, Double> currencyRate = getRateOfCurrency();
            currencyNamesMap.forEach((String key, String value) -> {
                Currency currency = new Currency();
                currency.setCurrencyName(key);
                currency.setCurrencyFullName(value);
                currency.setCurrencyRate(currencyRate.get(currency.getCurrencyName()));
                if (currency.getCurrencyRate() != null)
                    currencyHashMap.put(currency.getCurrencyName(), currency);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Double> getRateOfCurrency() {
        String jsonRates = null;
        try {
            String response = getContent("https://openexchangerates.org/api/latest.json?app_id=3c567128b4eb40cd9d58c34713eb3f85");
            JsonObject rates = jsonConverter.fromJson(response, JsonObject.class);
            String stringRates = String.valueOf(rates.get("rates"));
            jsonRates = stringRates.replace(":", "=");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonConverter.fromJson(jsonRates, new TypeToken<HashMap<String, Double>>() {
        }.getType());
    }

    private String getContent(String currentUrl) throws IOException {
        URL url = new URL(currentUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public void addToMyCurrenciesHashMap(String currencyName) {
        myCurrencyHashMap.put(currencyName, currencyHashMap.get(currencyName));
    }

    public Double convertCurrency(Currency baseCurrency, Currency targetCurrency, double amountOfCurrency) {
        double rate = targetCurrency.getCurrencyRate()/baseCurrency.getCurrencyRate();
        return amountOfCurrency * rate;
    }
}

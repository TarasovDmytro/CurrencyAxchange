package ua.tarasov.currencyexchange.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sun.javafx.collections.ObservableMapWrapper;
import javafx.collections.ObservableMap;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@Service
@Data
public class CurrencyService {
    private final Gson jsonConverter = new Gson();
    private final ObservableMap<String, Currency> currencyHashMap;
    private final ObservableMap<String, Currency> myCurrencyHashMap;

    public CurrencyService() {
        this.myCurrencyHashMap = new ObservableMapWrapper<>(new HashMap<>());
        this.currencyHashMap = new ObservableMapWrapper<>(new HashMap<>());
    }

    @Scheduled(fixedDelay = 60000)
    @PostConstruct
    public void setCurrencyHashMap() {
        try {
            String response = getContent("https://openexchangerates.org/api/currencies.json");
            String currencyNamesJsonMap = response.replace(":", "=");
            HashMap<String, String> currencyNamesMap = jsonConverter.fromJson(currencyNamesJsonMap,
                    new TypeToken<HashMap<String, String>>() {
                    }.getType());
            HashMap<String, Double> currencyRate = getRateOfCurrency();
            currencyNamesMap.forEach((String key, String value) -> {
                Currency currency = new Currency();
                currency.setCurrencyName(key);
                currency.setCurrencyFullName(value);
                currency.setCurrencyRate(currencyRate.get(currency.getCurrencyName()));
                if (currency.getCurrencyRate() != null)
                    currencyHashMap.put(currency.getCurrencyName(), currency);
            });
            if (!myCurrencyHashMap.isEmpty()) {
                myCurrencyHashMap.forEach((String key, Currency value) ->
                        myCurrencyHashMap.put(key, currencyHashMap.get(key)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Double> getRateOfCurrency() {
        String jsonRates = null;
        try {
            String response = getContent("https://openexchangerates.org/api/latest.json?app_id=3d0bc5fcd3bd4059b9dba733b23ee281");
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
        StringBuilder response = new StringBuilder();
        URL url = new URL(currentUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while (in.ready()) {
            response.append(in.readLine());
        }
        in.close();
        return response.toString();
    }

    public void addToMyCurrencies(Currency currency) {
        String myCurrencyName = currency.getCurrencyName();
        myCurrencyHashMap.put(myCurrencyName, currencyHashMap.get(myCurrencyName));
    }

    public Double convertCurrency(Currency baseCurrency, Currency targetCurrency, double amountOfCurrency) {
        double rate = targetCurrency.getCurrencyRate() / baseCurrency.getCurrencyRate();
        return amountOfCurrency * rate;
    }
}
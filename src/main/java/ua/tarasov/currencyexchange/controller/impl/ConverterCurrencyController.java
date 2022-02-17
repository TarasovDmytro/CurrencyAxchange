package ua.tarasov.currencyexchange.controller.impl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ua.tarasov.currencyexchange.controller.ViewController;
import ua.tarasov.currencyexchange.model.Currency;
import ua.tarasov.currencyexchange.service.CurrencyService;

@Component
@Log4j2
public class ConverterCurrencyController implements ViewController {
    @Value("classpath:/currency.fxml")
    private Resource currencyExchangeResource;
    private final CurrencyService service;
    private final ApplicationContext applicationContext;
    @FXML
    private ComboBox<Currency> chooseBaseCurrencyMenu;
    @FXML
    private ComboBox<Currency> chooseTargetCurrencyMenu;
    @FXML
    private TextField amountOfBaseCurrency;
    @FXML
    private TextField amountOfTargetCurrency;
    @FXML
    private TextArea errorTextArea;

    public ConverterCurrencyController(CurrencyService service, ApplicationContext applicationContext) {
        this.service = service;
        this.applicationContext = applicationContext;
    }

    @Override
    @FXML
    public void initialize() {
        chooseBaseCurrencyMenu.setItems(FXCollections.observableList(service.getMyCurrencyHashMap().values().stream().toList()).sorted());
        chooseTargetCurrencyMenu.setItems(FXCollections.observableList(service.getMyCurrencyHashMap().values().stream().toList()).sorted());
    }

    @FXML
    private void getCurrencyRateList(ActionEvent event) {
        getPage(event, currencyExchangeResource, applicationContext);
    }

    @FXML
    private void changeStateOfCurrencies() {
        Currency tempCurrency;
        tempCurrency = chooseBaseCurrencyMenu.getValue();
        chooseBaseCurrencyMenu.setValue(chooseTargetCurrencyMenu.getValue());
        chooseTargetCurrencyMenu.setValue(tempCurrency);
        String amountText = amountOfBaseCurrency.getText();
        if (isCorrectAmount(amountText)) getAmountOfTargetCurrency();
    }

    @FXML
    private void getAmountOfTargetCurrency() {
        double amountOfCurrency = 0;
        String amountText = amountOfBaseCurrency.getText();
        if (isCorrectAmount(amountText)) amountOfCurrency = Double.parseDouble(amountText);
        Currency baseCurrency = chooseBaseCurrencyMenu.getValue();
        Currency targetCurrency = chooseTargetCurrencyMenu.getValue();
        if (baseCurrency != null && targetCurrency != null) {
            errorTextArea.setText(errorTextArea.getText());
            amountOfTargetCurrency.setText(service.convertCurrency(baseCurrency, targetCurrency, amountOfCurrency).toString());
        } else
            errorTextArea.setText("ERROR:\nThe base currency or target currency is null\nPlease, choose the currencies\n\n" + errorTextArea.getText());
    }

    private Boolean isCorrectAmount(String amountText){
        try {
            if (!amountText.startsWith("-")) {
                Double.parseDouble(amountText);
                errorTextArea.setText("");
                return true;
            } else {
                errorTextArea.setText("ERROR:\nCurrency amount cannot be negative");
                return false;
            }
        } catch (Exception e) {
            log.error("The amount of currency has incorrect data: value = " + amountText);
            errorTextArea.setText("ERROR:\nThe amount of currency has incorrect data\nPlease, set the amount of currency");
        }
        return false;
    }
}
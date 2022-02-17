package ua.tarasov.currencyexchange.controller.impl;

import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import ua.tarasov.currencyexchange.controller.ViewController;
import ua.tarasov.currencyexchange.model.Currency;
import ua.tarasov.currencyexchange.service.CurrencyService;

@Controller
@Log4j2
public class CurrencyController implements ViewController {
    @Value("classpath:/currency-converter.fxml")
    private Resource currencyConverterResource;
    private final CurrencyService service;
    private final ApplicationContext applicationContext;
    @FXML
    private ComboBox<Currency> addCurrencyMenu;
    @FXML
    private TableColumn<Currency, String> columnCurrency;
    @FXML
    private TableColumn<Currency, String> columnCurrencyFullName;
    @FXML
    private TableColumn<Currency, Double> columnCurrencyRate;
    @FXML
    private TableView<Currency> currencyTable;

    public CurrencyController(CurrencyService service, ApplicationContext applicationContext) {
        this.service = service;
        this.applicationContext = applicationContext;
    }

    @FXML
    public void initialize() {
        service.getMyCurrencyHashMap().addListener((MapChangeListener<? super String, ? super Currency>) change -> {
            if (change.wasAdded() || change.wasRemoved()) {
                currencyTable.setItems(getMyCurrencies());
            }
        });
        service.getCurrencyHashMap().addListener((MapChangeListener<String, Currency>) change -> {
            if (change.wasAdded() || change.wasRemoved()) {
                addCurrencyMenu.setItems(getMyCurrencies());
            }
        });
        columnCurrency.setCellValueFactory(new PropertyValueFactory<>("currencyName"));
        columnCurrencyFullName.setCellValueFactory(new PropertyValueFactory<>("currencyFullName"));
        columnCurrencyRate.setCellValueFactory(new PropertyValueFactory<>("currencyRate"));
        currencyTable.setItems(getMyCurrencies());
        addCurrencyMenu.setItems(getCurrencies());
    }

    private ObservableList<Currency> getCurrencies() {
        return FXCollections.observableList(service.getCurrencyHashMap()
                        .values()
                        .stream()
                        .toList())
                .sorted();
    }

    private ObservableList<Currency> getMyCurrencies() {
        return FXCollections.observableList(service.getMyCurrencyHashMap()
                        .values()
                        .stream()
                        .toList())
                .sorted();
    }

    @FXML
    private void getCurrencyConverterPage(ActionEvent event) {
        getPage(event, currencyConverterResource, applicationContext);
    }

    @FXML
    private void addCurrencyToList() {
        Currency currency = addCurrencyMenu.getValue();
        service.addToMyCurrencies(currency);
        log.info(currency + "rate = " + currency.getCurrencyRate());
    }
}
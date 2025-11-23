package com.example.currencyparser.parser;

import com.example.currencyparser.entity.CurrencyRate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class CbrRatesParser implements RatesParser {

    private final WebClient webClient;

    // Формат параметра ?date_req=22/11/2025
    private static final DateTimeFormatter CBR_REQUEST_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Формат атрибута Date="22.11.2025"
    private static final DateTimeFormatter CBR_DATE_ATTR_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public CbrRatesParser(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Старый метод без параметров оставляем для совместимости.
     * Используется планировщиком и ручным сбором "на сегодня".
     */
    @Override
    public List<CurrencyRate> parseRates() {
        return parseRatesForDate(LocalDate.now());
    }

    /**
     * Новый метод: получает курсы ЦБ на конкретную дату.
     * Используется эндпоинтом /rates/{date}/{code} для онлайн-запросов.
     */
    public List<CurrencyRate> parseRatesForDate(LocalDate date) {
        String dateParam = date.format(CBR_REQUEST_FORMAT);
        String url = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=" + dateParam;

        String xml = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (xml == null || xml.isBlank()) {
            System.out.println("CBR response is empty");
            return List.of();
        }

        List<CurrencyRate> result = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            try {
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            } catch (Exception ignored) {}

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();

            String dateAttr = doc.getDocumentElement().getAttribute("Date");
            LocalDate rateDate;
            try {
                rateDate = LocalDate.parse(dateAttr, CBR_DATE_ATTR_FORMAT);
            } catch (Exception e) {
                rateDate = date;
            }

            NodeList nodes = doc.getElementsByTagName("Valute");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element valute = (Element) nodes.item(i);

                String charCode = valute.getElementsByTagName("CharCode").item(0).getTextContent().trim();
                String name = valute.getElementsByTagName("Name").item(0).getTextContent().trim();
                String nominalStr = valute.getElementsByTagName("Nominal").item(0).getTextContent().trim();
                String valueStr = valute.getElementsByTagName("Value").item(0).getTextContent().trim();

                valueStr = valueStr.replace(",", ".");
                BigDecimal value = new BigDecimal(valueStr);
                BigDecimal nominal = new BigDecimal(nominalStr);

                BigDecimal rate = value.divide(nominal, 6, RoundingMode.HALF_UP);

                result.add(new CurrencyRate(
                        charCode,
                        name,
                        rateDate,
                        rate,
                        null,
                        getSourceName()
                ));
            }

            result.add(new CurrencyRate(
                    "RUB",
                    "Российский рубль",
                    rateDate,
                    BigDecimal.ONE,
                    null,
                    getSourceName()
            ));

        } catch (Exception e) {
            System.out.println("Ошибка при разборе XML ЦБ РФ: " + e.getMessage());
            return List.of();
        }

        return result;
    }

    @Override
    public String getSourceName() {
        return "cbr.ru";
    }
}

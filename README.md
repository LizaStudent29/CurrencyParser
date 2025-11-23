# Currency Parser

Учебный проект по дисциплине «Теория и практика многопоточности».

Проект демонстрирует:

- Spring Boot MVC (REST-сервис)
- многопоточную обработку данных через `ExecutorService`
- планировщик `@Scheduled`
- встроенную БД H2 + JPA
- потокобезопасные структуры данных (`BlockingQueue`)
- параллельную сортировку через `parallelStream`
- межсервисное взаимодействие через `WebClient` (WebFlux)
- парсинг данных ЦБ РФ: курсы валют с сайта ЦБ РФ (`https://www.cbr.ru/scripts/XML_daily.asp`) как по расписанию, так и по онлайн‑запросу
- Swagger UI (springdoc) для удобного тестирования эндпоинтов
- Наличие Unit-тестов:
  - `CurrencyParserApplicationTests` — проверка поднятия контекста Spring Boot
  - `RatesControllerTest` — тест контроллера `/rates`
  - `RatesCollectorServiceTest` — тест сервиса `RatesCollectorService`

## Как запустить

### Требования

- JDK 17+
- Maven 3+

### Шаги

1. Склонировать/скачать проект.
2. В корне проекта выполнить:

   ```bash
   mvn clean package
   ```

3. Запустить приложение:

   ```bash
   java -jar target/currency-parser-0.0.1-SNAPSHOT.jar
   ```

По умолчанию приложение стартует на `http://localhost:8080`.

## Swagger UI

После запуска откройте в браузере:

```text
http://localhost:8080/swagger-ui.html
```

или

```text
http://localhost:8080/swagger-ui/index.html
```

Там будут доступны все эндпоинты контроллера `/rates`, и можно выполнять запросы прямо из Swagger UI.

## Основные эндпоинты

### 1. Запуск многопоточного сбора курсов за сегодня (с сохранением в БД)

```http
POST /rates/collect
```

Этот эндпоинт:

- запускает парсер ЦБ РФ в пуле потоков (`ExecutorService`);
- сохраняет полученные курсы в БД H2 (таблица `CURRENCY_RATES`).

Он нужен для демонстрации многопоточности + работы с БД.

### 2. Получить все сохранённые курсы из БД (с parallelStream)

```http
GET /rates
```

Параметры:

- `sortBy` — `date` (по умолчанию) | `rate` | `code`
- `direction` — `asc` (по умолчанию) | `desc`

Сортировка выполняется через `parallelStream()` — демонстрация параллельной обработки коллекций.

### 3. Онлайн‑курс по дате и валюте 

```http
GET /rates/{date}/{code}
```

- Делает HTTP‑запрос к ЦБ РФ с параметром `date_req` на указанную дату.
- Парсит XML‑ответ, находит нужную валюту и возвращает её курс.

Примеры:

```http
GET /rates/2025-11-20/USD
GET /rates/2025-01-10/EUR
```

> Важно: дата в формате `YYYY-MM-DD`.  
> Если ЦБ не отдаёт данные на эту дату или такой валюты нет — вернётся `404 Not Found`.

## Автоматический запуск по расписанию

Планировщик `@Scheduled` каждые 10 минут автоматически запускает сбор курсов за текущий день:

- класс: `com.example.currencyparser.service.ScheduledTasks`
- метод: `scheduledCollect()`

## H2 Console

```text
http://localhost:8080/h2-console
```

- JDBC URL: `jdbc:h2:mem:currencydb`
- User Name: `sa`
- Password: *(пусто)*

Таблица: `CURRENCY_RATES`.



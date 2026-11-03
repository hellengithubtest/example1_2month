# Spring Security: form login, роли и CSRF

Учебный Spring Boot-проект демонстрирует:

- аутентификацию через HTML-форму;
- хранение паролей в BCrypt;
- разграничение доступа по ролям;
- защиту POST-запросов с помощью CSRF-токена.
- безопасные атрибуты сессионной cookie в production-профиле.

## Зачем нужен CSRF-токен

После входа браузер автоматически отправляет серверу сессионную cookie. Этим может попытаться воспользоваться чужой сайт: незаметно сформировать запрос к приложению от имени уже вошедшего пользователя.

CSRF-токен — дополнительное случайное значение, которое сервер выдаёт своей странице и ожидает получить обратно в изменяющем данные запросе. Чужой сайт не может прочитать страницу приложения и узнать корректный токен, поэтому поддельный запрос будет отклонён.

В этом проекте токен хранится в HTTP-сессии. Форма находится в Thymeleaf-шаблоне и отправляется методом POST:

```html
<form method="post" th:action="@{/login}">
```

Интеграция Thymeleaf со Spring MVC автоматически добавляет в такую форму скрытое поле примерно следующего вида:

```html
<input type="hidden" name="_csrf" value="случайное-значение">
```

При POST `/login` Spring Security сравнивает значение из формы со значением текущей сессии. При отсутствии или подмене токена запрос не проходит аутентификацию.

## Требования

- Java 21;
- Gradle Wrapper (включён в проект, отдельно устанавливать Gradle не нужно).

## Запуск

Из каталога `example1_2month` выполнить:

```bash
./gradlew bootRun
```

Приложение будет доступно по адресу <http://localhost:8090/login>.

После успешного входа USER и MANAGER перенаправляются на <http://localhost:8090/users>,
а ADMIN — на <http://localhost:8090/admin>. Пароли в HTML-представления не
передаются. В верхней панели отображается текущий пользователь и кнопка
безопасного выхода через POST `/logout` с CSRF-токеном.

Если аутентифицированный пользователь открывает раздел, для которого у него нет нужной роли, Spring Security возвращает HTTP `403 Forbidden` и отображает шаблон `/403`. Страница показывает понятное сообщение, текущего пользователя, ссылку назад к доступному разделу и безопасную кнопку выхода.

Тестовые пользователи:

| Логин | Пароль | Роли |
|---|---|---|
| `user1` | `password` | USER |
| `user2` | `password` | USER, MANAGER |
| `admin` | `password` | ADMIN |

После входа `user1` и `user2` перенаправляются на `/users`. Пользователь `admin`
перенаправляется на защищённую панель `/admin`. Прямой запрос обычного USER к
`/admin` возвращает `403 Forbidden` и страницу «Доступ запрещён».

Правила видимости:

- USER видит только собственную учётную запись, в том числе при прямом запросе `/users/{id}`;
- MANAGER видит список всех пользователей, но не может создавать и удалять их;
- ADMIN видит всех пользователей и может создавать или удалять учётные записи на `/admin`.

Панель администратора принимает только фиксированные уровни USER, MANAGER и
ADMIN. Пароль новой записи сохраняется как BCrypt-хэш. Создание и удаление
выполняются POST-формами с CSRF-токеном. Удалить собственную активную учётную
запись администратору нельзя.

## Проверка через браузер

1. Открыть <http://localhost:8090/login>.
2. Открыть инструменты разработчика браузера.
3. На вкладке **Elements** найти элемент `input` с `name="_csrf"`.
4. Убедиться, что его `value` содержит реальный токен, а не строку `${_csrf.token}`.
5. На вкладке **Network** включить сохранение запросов и войти как `user1` / `password`.
6. Открыть запрос `POST /login` и проверить раздел **Form Data**. В нём должны присутствовать `username`, `password` и `_csrf`.
7. Успешный запрос возвращает `302 Found` и перенаправляет на `/users`.

Значение CSRF-токена может изменяться между запросами — это нормальное поведение защиты.

## Проверка через curl

### Успешный запрос с CSRF-токеном

Сначала получить страницу входа и сохранить связанную с ней сессию:

```bash
curl -sS \
  -c csrf-cookies.txt \
  http://localhost:8090/login \
  -o csrf-login.html
```

Проверить, что Thymeleaf сформировал настоящее поле:

```bash
grep 'name="_csrf"' csrf-login.html
```

Извлечь токен из HTML:

```bash
CSRF_TOKEN=$(sed -n 's/.*name="_csrf"[^>]*value="\([^"]*\)".*/\1/p' csrf-login.html | head -n 1)
```

Отправить форму вместе с cookie той же сессии и токеном:

```bash
curl -i \
  -b csrf-cookies.txt \
  --data-urlencode "username=user1" \
  --data-urlencode "password=password" \
  --data-urlencode "_csrf=${CSRF_TOKEN}" \
  http://localhost:8090/login
```

Ожидаемый результат:

```text
HTTP/1.1 302
Location: http://localhost:8090/users
```

### Негативная проверка без CSRF-токена

```bash
curl -i \
  --data-urlencode "username=user1" \
  --data-urlencode "password=password" \
  http://localhost:8090/login
```

Ожидаемый результат в текущей браузерной form-login конфигурации:

```text
HTTP/1.1 302
Location: http://localhost:8090/login
```

Главная проверка: запрос без токена возвращается на `/login` и не перенаправляется на защищённый `/users`. На уровне Spring Security такой запрос отклоняется CSRF-фильтром; способ представления отказа зависит от настроенного обработчика ошибок и клиента.

## Автоматическая проверка

Запустить только тесты CSRF и формы входа:

```bash
./gradlew test --tests LoginCsrfIntegrationTest
```

Тесты этого класса проверяют CSRF, вход, страницу отказа и видимость пользователей:

1. GET `/login` содержит отрендеренное поле `_csrf`.
2. POST `/login` без CSRF-токена получает `403 Forbidden`.
3. POST `/login` с токеном и правильными учётными данными выполняет вход и перенаправляет на `/users`.
4. USER видит только себя и не может получить чужую запись по ID.
5. MANAGER и ADMIN видят полный список пользователей.

Запустить все тесты проекта:

```bash
./gradlew test
```

Запустить только проверки ADMIN endpoint:

```bash
./gradlew test --tests AdminAccessIntegrationTest
```

Они проверяют полный сценарий разграничения доступа: анонимный пользователь
перенаправляется на login, USER получает `403`, ADMIN открывает HTML-панель,
создаёт и удаляет пользователей, пароли хэшируются, компании не удаляются
каскадно, POST без CSRF отклоняется, а собственную активную запись удалить нельзя.

Собрать исполняемый Spring Boot JAR:

```bash
./gradlew bootJar
```

## Безопасная сессионная cookie и HTTPS

При обычном локальном запуске используется HTTP, поэтому атрибут `Secure` у
сессионной cookie не включён: иначе браузер не отправлял бы её на
`http://localhost`. Для production предусмотрен отдельный профиль `prod`. Он:

- включает HTTPS на порту `8443`;
- устанавливает для `JSESSIONID` атрибуты `Secure` и `HttpOnly`;
- устанавливает `SameSite=Lax`;
- получает путь к хранилищу сертификата и пароль только из переменных окружения.

`HttpOnly` запрещает JavaScript читать cookie, `Secure` разрешает браузеру
отправлять её только по HTTPS, а `SameSite=Lax` ограничивает отправку cookie в
межсайтовых запросах. `SameSite` дополняет CSRF-токен, но не заменяет его.

Для локальной проверки production-профиля можно создать тестовый самоподписанный
сертификат (не использовать его на настоящем production-сервере):

```bash
keytool -genkeypair \
  -alias app \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore local-cert.p12 \
  -validity 365 \
  -storepass changeit \
  -dname "CN=localhost, OU=Development, O=IPR, C=RU" \
  -ext "SAN=dns:localhost,ip:127.0.0.1"
```

Запустить приложение с профилем `prod`:

```bash
export SSL_KEY_STORE="file:$(pwd)/local-cert.p12"
export SSL_KEY_STORE_PASSWORD="changeit"
export SSL_KEY_ALIAS="app"
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

Открыть <https://localhost:8443/login>. Браузер предупредит о недоверенном
самоподписанном сертификате — для локальной проверки это ожидаемо.

Получить страницу входа и проверить заголовок `Set-Cookie` через `curl`:

```bash
curl -k -i -c secure-cookies.txt https://localhost:8443/login
```

В заголовке должен присутствовать результат примерно такого вида:

```text
Set-Cookie: JSESSIONID=...; Path=/; Secure; HttpOnly; SameSite=Lax
```

Файлы сертификатов и закрытых ключей (`*.p12`, `*.pfx`, `*.jks`, `*.pem`,
`*.key`) добавлены в `.gitignore` и не должны попадать в репозиторий. В настоящем
production следует использовать сертификат, выданный доверенным центром
сертификации, и хранить пароль в менеджере секретов или переменных окружения.

## Где находится реализация

- `src/main/resources/templates/login.html` — HTML-форма с CSRF-полем;
- `src/main/java/org/springsecurity/example/rest/LoginPageController.java` — отображение страницы `/login`;
- `src/main/java/org/springsecurity/example/security/SecurityConfig.java` — form login, CSRF и правила доступа;
- `src/main/resources/application-prod.yml` — HTTPS и безопасные cookie для профиля `prod`;
- `src/main/java/org/springsecurity/example/rest/AdminController.java` — защищённая панель администратора;
- `src/main/resources/templates/admin.html` — HTML-представление ADMIN endpoint;
- `src/test/java/org/springsecurity/example/LoginCsrfIntegrationTest.java` — интеграционные проверки CSRF;
- `src/test/java/org/springsecurity/example/AdminAccessIntegrationTest.java` — проверки доступа анонимного пользователя, USER и ADMIN;
- `src/test/java/org/springsecurity/example/ProductionCookieConfigurationTest.java` — проверка production-настроек cookie и HTTPS.

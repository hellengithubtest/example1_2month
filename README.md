# Spring Security: form login, роли и CSRF

Учебный Spring Boot-проект демонстрирует:

- аутентификацию через HTML-форму;
- хранение паролей в BCrypt;
- разграничение доступа по ролям;
- защиту POST-запросов с помощью CSRF-токена.

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

После успешного входа приложение перенаправляет на <http://localhost:8090/user>. Это HTML-страница со списком пользователей, ролями и статусами; пароли в представление не передаются. В верхней панели отображается текущий пользователь и кнопка безопасного выхода через POST `/logout` с CSRF-токеном.

Если аутентифицированный пользователь открывает раздел, для которого у него нет нужной роли, Spring Security возвращает HTTP `403 Forbidden` и отображает шаблон `/403`. Страница показывает понятное сообщение, текущего пользователя, ссылку назад к доступному разделу и безопасную кнопку выхода.

Тестовые пользователи:

| Логин | Пароль | Роли |
|---|---|---|
| `user1` | `password` | USER |
| `user2` | `password` | USER, MANAGER |
| `admin` | `password` | ADMIN |

## Проверка через браузер

1. Открыть <http://localhost:8090/login>.
2. Открыть инструменты разработчика браузера.
3. На вкладке **Elements** найти элемент `input` с `name="_csrf"`.
4. Убедиться, что его `value` содержит реальный токен, а не строку `${_csrf.token}`.
5. На вкладке **Network** включить сохранение запросов и войти как `user1` / `password`.
6. Открыть запрос `POST /login` и проверить раздел **Form Data**. В нём должны присутствовать `username`, `password` и `_csrf`.
7. Успешный запрос возвращает `302 Found` и перенаправляет на `/user`.

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
Location: http://localhost:8090/user
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

Главная проверка: запрос без токена возвращается на `/login` и не перенаправляется на защищённый `/user`. На уровне Spring Security такой запрос отклоняется CSRF-фильтром; способ представления отказа зависит от настроенного обработчика ошибок и клиента.

## Автоматическая проверка

Запустить только тесты CSRF и формы входа:

```bash
./gradlew test --tests LoginCsrfIntegrationTest
```

Тесты проверяют три сценария:

1. GET `/login` содержит отрендеренное поле `_csrf`.
2. POST `/login` без CSRF-токена получает `403 Forbidden`.
3. POST `/login` с токеном и правильными учётными данными выполняет вход и перенаправляет на `/user`.

Запустить все тесты проекта:

```bash
./gradlew test
```

Собрать исполняемый Spring Boot JAR:

```bash
./gradlew bootJar
```

## Где находится реализация

- `src/main/resources/templates/login.html` — HTML-форма с CSRF-полем;
- `src/main/java/org/springsecurity/example/rest/LoginPageController.java` — отображение страницы `/login`;
- `src/main/java/org/springsecurity/example/security/SecurityConfig.java` — form login, CSRF и правила доступа;
- `src/test/java/org/springsecurity/example/LoginCsrfIntegrationTest.java` — интеграционные проверки.

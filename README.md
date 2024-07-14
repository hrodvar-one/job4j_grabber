# job4j_grabber
Система запускается по расписанию - раз в минуту.  Период запуска указывается в настройках - app.properties.

Первый сайт будет career.habr.com. Работаем с разделом https://career.habr.com/vacancies/java_developer.  
Программа должна считывать все вакансии c первых 5 страниц относящиеся к Java и записывать их в базу.

<summary>Запуск проекта</summary>

* Запустить метод main из класса Grabber находящийся по пути \src\main\java\ru\job4j\grabber\Grabber.java

<summary>Просмотр вакансий в браузере</summary>

* После запуска проекта открыть в любом браузере ссылку http://localhost:9000
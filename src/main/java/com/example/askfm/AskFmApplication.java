package com.example.askfm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AskFmApplication {

    public static void main(String[] args) {
        SpringApplication.run(AskFmApplication.class, args);
    }

    //посты которые лайкнул юзер
// добавить пагинацию в список юзеров для админа
//редактирование / поста /комантария

    //отхват активности юзера просмотры /подпишики
    //вощврат пароля чреез почту
    //гугл  регистрация
    //репост   поста

    //поставть колокоотчик для получание поств нотификация
    //админ может пополнить счет  баланс
}

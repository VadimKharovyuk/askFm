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


//редактирование / поста /комантария

    //отхват активности юзера просмотры /подпишики

    //количество подпишиков
//
//    <p>Редактировать информацию профиля</p>


    //рекоменрации по Ню фото

    //add lokation  в dto ивента


//: ❌ Неожиданная ошибка при удаление уведомлений: Event not found with id: 23
//2025-02-14T02:42:00.741+04:00 ERROR 20175 --- [askFm] [EventExecutor-9] .
// a.i.SimpleAsyncUncaughtExceptionHandler :
// Unexpected exception occurred invoking async method: public void com.example.askfm.
// EventListener.NotificationListener.handleEventCansel(com.example.askfm.EventListener.E
// ventEvent)


}

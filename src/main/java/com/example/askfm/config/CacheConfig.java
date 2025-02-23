package com.example.askfm.config;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Конфигурация для общего кэша
        Caffeine<Object, Object> defaultCacheBuilder = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000)
                .recordStats();

        // Конфигурация для кэша followers
        Caffeine<Object, Object> followersCacheBuilder = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(45, TimeUnit.MINUTES)
                .initialCapacity(200)//начальное
                .maximumSize(2000) //максимальное
                .recordStats();


        // Конфигурация для userSearch
        Caffeine<Object, Object> userSearchCacheBuilder = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(45, TimeUnit.MINUTES)
                .initialCapacity(200)
                .maximumSize(2000)
                .recordStats();


        // Конфигурация для suggestions
        Caffeine<Object, Object> suggestionsBuilder = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)  // Кэш живет дольше т.к. данные меняются редко
                .expireAfterAccess(90, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000)
                .recordStats();

//посты
        Caffeine<Object, Object> postsBuilder = Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.MINUTES) // 2 часа,
                .expireAfterAccess(180, TimeUnit.MINUTES)
                .initialCapacity(200)
                .maximumSize(3000) // Больше места для постов
                .recordStats();


        //просмотры
        Caffeine<Object, Object> viewsBuilder = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .expireAfterAccess(90, TimeUnit.MINUTES)
                .initialCapacity(500)
                .maximumSize(5000)
                .recordStats();


        // Для лайков
        Caffeine<Object, Object> likesBuilder = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .expireAfterAccess(90, TimeUnit.MINUTES)
                .initialCapacity(500)
                .maximumSize(5000)
                .recordStats();


        Caffeine<Object, Object> photosBuilder = Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.DAYS)  // Кеш живет 2 дня
                .expireAfterAccess(2, TimeUnit.DAYS)
                .initialCapacity(300)                // Больше начальная емкость для фото
                .maximumSize(4000)                   // Большой размер для хранения фото
                .recordStats();



        // Конфигурация кеша для профилей пользователей
        Caffeine<Object, Object> userProfileBuilder = Caffeine.newBuilder()
                .expireAfterWrite(7, TimeUnit.DAYS)      // Кеш живет 7 дней
                .expireAfterAccess(7, TimeUnit.DAYS)
                .initialCapacity(200)
                .maximumSize(2000)
                .recordStats();


        // Кеш для топовых постов
        Caffeine<Object, Object> topPostsBuilder = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)    // Обновление каждые 30 минут
                .expireAfterAccess(1, TimeUnit.HOURS)      // Удаление если не обращались час
                .initialCapacity(100)                       // Начальный размер
                .maximumSize(500)                          // Максимальный размер
                .recordStats();

        cacheManager.registerCustomCache("topPosts", topPostsBuilder.build());



        // Кеш для предпросмотра топовых постов
        Caffeine<Object, Object> topPostsPreviewBuilder = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)    // Обновление каждые 15 минут
                .expireAfterAccess(30, TimeUnit.MINUTES)   // Удаление если не обращались 30 минут
                .initialCapacity(50)                       // Начальный размер
                .maximumSize(200)                          // Максимальный размер
                .recordStats();

        cacheManager.registerCustomCache("topPostsPreview", topPostsPreviewBuilder.build());



        // Кеш для статистики загрузок фото
        Caffeine<Object, Object> photoUploadsStatsCache = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)    // Обновление каждые 15 минут
                .expireAfterAccess(1, TimeUnit.HOURS)      // Удаление если не обращались час
                .initialCapacity(50)                       // Начальный размер
                .maximumSize(200)                          // Максимальный размер
                .recordStats();
        cacheManager.registerCustomCache("photoUploadsStats", photoUploadsStatsCache.build());

        // Кеш для статистики разблокировок
        Caffeine<Object, Object> photoUnlocksStatsCache = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)    // Обновление каждые 15 минут
                .expireAfterAccess(1, TimeUnit.HOURS)      // Удаление если не обращались час
                .initialCapacity(50)                       // Начальный размер
                .maximumSize(200)                          // Максимальный размер
                .recordStats();

        cacheManager.registerCustomCache("photoUnlocksStats", photoUnlocksStatsCache.build());


        // Кэш для активных историй
        Caffeine<Object, Object> storiesBuilder = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)      // Обновление каждый час
                .expireAfterAccess(2, TimeUnit.HOURS)     // Удаление если не обращались 2 часа
                .initialCapacity(100)                     // Начальный размер
                .maximumSize(500)                         // Максимальный размер
                .recordStats();                           // Запись статистики
        cacheManager.registerCustomCache("activeStories", storiesBuilder.build());

        // Кэш для историй подписок
        Caffeine<Object, Object> followingStoriesBuilder = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)   // Обновление каждые 30 минут
                .expireAfterAccess(1, TimeUnit.HOURS)     // Удаление если не обращались 1 час
                .initialCapacity(50)                      // Начальный размер
                .maximumSize(200)                         // Максимальный размер
                .recordStats();
        cacheManager.registerCustomCache("followingStories", followingStoriesBuilder.build());

        // Кэш для просмотров и реакций
        Caffeine<Object, Object> storyStatsBuilder = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)    // Обновление каждые 5 минут
                .expireAfterAccess(15, TimeUnit.MINUTES)  // Удаление если не обращались 15 минут
                .initialCapacity(200)                     // Начальный размер
                .maximumSize(1000)                        // Максимальный размер
                .recordStats();
        cacheManager.registerCustomCache("storyStats", storyStatsBuilder.build());




        // Устанавливаем специальную конфигурацию
        cacheManager.registerCustomCache("followers", followersCacheBuilder.build());
        cacheManager.registerCustomCache("userSearch", userSearchCacheBuilder.build());
        cacheManager.registerCustomCache("suggestions", suggestionsBuilder.build());
        cacheManager.registerCustomCache("posts", postsBuilder.build());
        cacheManager.registerCustomCache("views", viewsBuilder.build());
        cacheManager.registerCustomCache("likes", likesBuilder.build());
        cacheManager.registerCustomCache("userProfiles", userProfileBuilder.build());
        cacheManager.registerCustomCache("userPhotos", photosBuilder.build());




        cacheManager.setCaffeine(defaultCacheBuilder);
        return cacheManager;
    }
}
package com.account.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Баланс API",
                description = "Тестовое задание GoPoints", version = "1.0.0",
                contact = @Contact(
                        name = "Сухобская Аглая",
                        email = "a.sukhobskaya@mail.ru",
                        url = "https://github.com/AglayaSukhobskaya"
                )
        )
)
public class SwaggerConfig {
}

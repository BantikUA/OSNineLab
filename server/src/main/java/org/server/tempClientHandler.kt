package org.client

import java.net.Socket
import kotlin.concurrent.thread

class TempClientHandler(){

    private val serverIp = "192.168.195.25" // IP-адреса сервера
    private val port = 8080            // Порт сервера

    fun runClient() {
        Socket(serverIp, port).use { socket ->
            println("Підключено до сервера $serverIp:$port")

            val fromServerMsg = socket.getInputStream().bufferedReader()
            val toServerMsg = socket.getOutputStream().bufferedWriter()
            while (true) {
                println("Введіть тип повідомлення")
                when (val msgType = readln()) {
                    "REG_USR" -> {
                        println("Введіть ім'я та пароль для реєстрації")
                        val userName = readln()
                        val userPassword = readln()
                        toServerMsg.write("$msgType://:$userName:$userPassword\n")
                        toServerMsg.flush()
                    }

                    "LOG_USR" -> {
                        println("Введіть ім'я та пароль для входу")
                        val userName = readln()
                        val userPassword = readln()
                        toServerMsg.write("$msgType://:$userName:$userPassword\n")
                        toServerMsg.flush()
                    }

                    "MSG_USR" -> {
                        println("Введіть повідомлення")
                        val userMsg = readln()
                        toServerMsg.write("$msgType://:$userMsg\n")
                        toServerMsg.flush()
                    }

                    "EXIT" -> {
                        println("Вихід")
                        break
                    }
                }

                val serverResponse = fromServerMsg.readLine()
                println("Відповідь від сервера: $serverResponse")

            }
        }
    }

}

fun main(){

    // Запускаємо клієнта у основному потоці (або також в окремому, якщо потрібно)

    thread {
        try {
            TempClientHandler().runClient()
        } catch (e: Exception) {
            println("Помилка клієнта: ${e.message}")
        }
    }

}
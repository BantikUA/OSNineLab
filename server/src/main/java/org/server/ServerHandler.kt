import org.server.BlockedWords
import org.server.UserHandler
import java.net.*


import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

import kotlin.concurrent.thread

class ServerHandler {

    private val port = 8080
    private val serverSocket = ServerSocket(port)

    // Мапа всіх клієнтів: ключ — ім'я користувача, значення — сокет клієнта
    private val clients = mutableMapOf<String, Socket>()

    init {
        println("Очікування клієнтів...")
        startBroadcastingIp()
    }

    private val userValidation = UserHandler()
    private val wordsValidation = BlockedWords()

    fun runServer() {
        while (true) {
            val clientSocket = serverSocket.accept()
            println("Підключено клієнта: ${clientSocket.inetAddress.hostAddress}")

            thread {
                clientSocket.use { socket ->
                    val fromClientMsg = socket.getInputStream().bufferedReader()
                    val toClientMsg = socket.getOutputStream().bufferedWriter()

                    var userName: String? = null

                    try {
                        // Постійний цикл обміну даними з клієнтом
                        while (true) {
                            val receivedMessage = fromClientMsg.readLine()
                            val parts = receivedMessage.split("://:")

                            when (val msgType = parts[0]) {
                                "REG_USR" -> {
                                    userName = (parts[1].split(":"))[0]
                                    val userPassword = (parts[1].split(":"))[1]

                                    if(userValidation.isUserNameAvailable(userName)){
                                        userValidation.register(userName, userPassword)

                                        synchronized(clients) {
                                            clients[userName!!] = socket // додаємо клієнта в мапу
                                        }

                                        // TODO Успішно
                                        println("Успішна реєстрація")
                                        toClientMsg.write("1\n")
                                        toClientMsg.flush()
                                    }else{
                                        println("Спроба реєстрації вже існуючого користувача")
                                        // TODO Користувач з таким іменем вже існує!
                                        toClientMsg.write("0\n")
                                        toClientMsg.flush()
                                    }



                                }

                                "LOG_USR" -> {
                                    userName = (parts[1].split(":"))[0]
                                    val userPassword = (parts[1].split(":"))[1]

                                    if(userValidation.login(userName, userPassword)){
                                        synchronized(clients) {
                                            clients[userName] = socket // Оновлюємо клієнта в мапі
                                        }

                                        // TODO Успішно
                                        println("Успішний вхід")
                                        toClientMsg.write("1\n")
                                        toClientMsg.flush()
                                    }else{
                                        // TODO Користувач не існує!
                                        println("Спроба увійти несінуючого користувача")
                                        toClientMsg.write("0\n")
                                        toClientMsg.flush()
                                    }

                                }

                                "MSG_USR" -> {
                                    val message = parts[1]
                                    val forbidWords = wordsValidation.isBlocked(message)

                                    if(forbidWords <= 2){
                                        println("Повідомлення надіслано")
                                        broadcastMessage(message, socket)
                                    }else{
                                        // TODO Кількість слів більше 2
                                        println("Кількість дозволених заборонених слів перевищує 2")
                                        toClientMsg.write("$forbidWords\n")
                                        toClientMsg.flush()
                                    }


                                }

                                else -> {
                                    toClientMsg.write("Невідомий тип повідомлення: $msgType\n")
                                    toClientMsg.flush()
                                }
                            }

                            if (receivedMessage == null) {
                                println("Клієнт відключився: ${socket.inetAddress.hostAddress}")
                                synchronized(clients) {
                                    clients.remove(userName) // Видаляємо клієнта у разі помилки
                                }
                                break
                            }
                        }
                    } catch (e: Exception) {
                        println("Помилка під час роботи з клієнтом: ${e.message}")
                        synchronized(clients) {
                            clients.remove(userName) // Видаляємо клієнта у разі помилки
                        }

                    }
                }
            }
        }
    }

    // Функція для надсилання повідомлень всім клієнтам, крім відправника
    private fun broadcastMessage(message: String, senderSocket: Socket) {
        val userName = clients.entries.find { it.value == senderSocket}?.key
        synchronized(clients) {
            clients.forEach { (_, clientSocket) ->
                try {
                    val toClientMsg = clientSocket.getOutputStream().bufferedWriter()
                    toClientMsg.write("$userName: $message\n")
                    toClientMsg.flush()
                } catch (e: Exception) {
                    println("Помилка надсилання повідомлення клієнту: ${e.message}")
                }
            }
        }
    }

    // Функція для розсилання IP-адреси сервера
    private fun startBroadcastingIp() {
        thread {
            val socket = DatagramSocket()
            val address = InetAddress.getByName("255.255.255.255") // Широкомовлення
            val message = "SERVER_IP:${InetAddress.getLocalHost().hostAddress}".toByteArray()

            while (true) {
                try {
                    val packet = DatagramPacket(message, message.size, address, 9876)
                    socket.send(packet)
                    println("Broadcast IP: ${String(message)}")
                    Thread.sleep(5000) // Кожні 5 секунд
                } catch (e: Exception) {
                    println("Помилка під час розсилання IP: ${e.message}")
                }
            }
        }
    }

}

fun main() {


    ServerHandler().runServer()
}

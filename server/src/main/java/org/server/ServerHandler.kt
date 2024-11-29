import org.server.BlockedWords
import org.server.UserHandler
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class ServerHandler {

    private val port = 8080
    private val serverSocket = ServerSocket(port)

    // Мапа всіх клієнтів: ключ — ім'я користувача, значення — сокет клієнта
    private val clients = mutableMapOf<String?, Socket>()

    init {
        println("Очікування клієнтів...")
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


                                    toClientMsg.write("test")
                                    toClientMsg.flush()

                                    println(userValidation.isUserNameAvailable(userName))


                                    if(userValidation.isUserNameAvailable(userName)){
                                        userValidation.register(userName, userPassword)

                                        println("if")
                                        synchronized(clients) {
                                            clients[userName] = socket // додаємо клієнта в мапу
                                        }

                                        // TODO Успішно
                                        toClientMsg.write("Успішно")
                                        toClientMsg.flush()
                                    }else{
                                        println("else")
                                        // TODO Користувач з таким іменем вже існує!
                                        toClientMsg.write("Користувач з таким іменем вже існує!")
                                        toClientMsg.flush()
                                    }

                                    println("check2")


                                }

                                "LOG_USR" -> {
                                    userName = (parts[1].split(":"))[0]
                                    val userPassword = (parts[1].split(":"))[1]

                                    if(userValidation.login(userName, userPassword)){
                                        synchronized(clients) {
                                            clients[userName] = socket // Оновлюємо клієнта в мапі
                                        }

                                        // TODO Успішно
                                        toClientMsg.write("Успішно")
                                        toClientMsg.flush()
                                    }else{
                                        // TODO Користувач не існує!
                                        toClientMsg.write("Користувач не існує!")
                                        toClientMsg.flush()
                                    }

                                }

                                "MSG_USR" -> {
                                    val message = (parts[1].split(":"))[1]

                                    if(wordsValidation.isBlocked(message) <= 2){
                                        broadcastMessage("$userName: $message\n", socket)
                                    }else{
                                        // TODO Кількість слів більше 2
                                        toClientMsg.write("Кількість слів більше 2")
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
        synchronized(clients) {
            clients.forEach { (userName, clientSocket) ->
                try {
                    val toClientMsg = clientSocket.getOutputStream().bufferedWriter()
                    toClientMsg.write("$userName: $message")
                    toClientMsg.flush()
                } catch (e: Exception) {
                    println("Помилка надсилання повідомлення клієнту: ${e.message}")
                }
            }
        }
    }
}

fun main() {
    ServerHandler().runServer()
}

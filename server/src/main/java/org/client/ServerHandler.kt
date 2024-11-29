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
                                    clients[userName] = socket // Додаємо клієнта в мапу

                                    toClientMsg.write("Ви успішно зареєструвались!\n")
                                    toClientMsg.flush()
                                }

                                "LOG_USR" -> {
                                    userName = (parts[1].split(":"))[0]
                                    val userPassword = (parts[1].split(":"))[1]
                                    clients[userName] = socket // Оновлюємо клієнта в мапі

                                    toClientMsg.write("Ви успішно увійшли!\n")
                                    toClientMsg.flush()
                                }

                                "MSG_USR" -> {
                                    val message = (parts[1].split(":"))[1]
                                    broadcastMessage("$userName: $message\n", socket)
                                }

                                else -> {
                                    toClientMsg.write("Невідомий тип повідомлення: $msgType\n")
                                    toClientMsg.flush()
                                }
                            }

                            if (receivedMessage == null) {
                                println("Клієнт відключився: ${socket.inetAddress.hostAddress}")
                                clients.remove(userName) // Видаляємо клієнта зі списку
                                break
                            }
                        }
                    } catch (e: Exception) {
                        println("Помилка під час роботи з клієнтом: ${e.message}")
                        clients.remove(userName) // Видаляємо клієнта у разі помилки
                    }
                }
            }
        }
    }

    // Функція для надсилання повідомлень всім клієнтам, крім відправника
    private fun broadcastMessage(message: String, senderSocket: Socket) {
        clients.forEach { (_, clientSocket) ->
            try {
                val toClientMsg = clientSocket.getOutputStream().bufferedWriter()
                toClientMsg.write(message)
                toClientMsg.flush()
            } catch (e: Exception) {
                println("Помилка надсилання повідомлення клієнту: ${e.message}")
            }
        }
    }
}

fun main() {
    ServerHandler().runServer()
}

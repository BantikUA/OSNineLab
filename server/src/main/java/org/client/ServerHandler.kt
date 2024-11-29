import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class ServerHandler {

    private val port = 8080
    private val serverSocket = ServerSocket(port)

    // Список всіх клієнтів
    private val clients = mutableListOf<Socket>()

    init {
        println("Очікування клієнтів...")
    }

    fun runServer() {
        while (true) {
            val clientSocket = serverSocket.accept()
            clients.add(clientSocket) // Додаємо клієнта до списку
            println("Підключено клієнта: ${clientSocket.inetAddress.hostAddress}")

            thread {
                clientSocket.use { socket ->
                    val fromClientMsg = socket.getInputStream().bufferedReader()
                    val toClientMsg = socket.getOutputStream().bufferedWriter()

                    try {
                        // Постійний цикл обміну даними з клієнтом
                        while (true) {
                            val receivedMessage = fromClientMsg.readLine()
                            if (receivedMessage == null) {
                                println("Клієнт відключився: ${socket.inetAddress.hostAddress}")
                                clients.remove(socket) // Видаляємо клієнта зі списку
                                break
                            }

                            val parts = receivedMessage.split("://:")
                            when (val msgType = parts[0]) {
                                "REG_USR" -> {
                                    val userName = (parts[1].split(":"))[0]
                                    val userPassword = (parts[1].split(":"))[1]
                                    toClientMsg.write("Ви успішно зареєструвались!\n")
                                    toClientMsg.flush()
                                }

                                "LOG_USR" -> {
                                    val userName = (parts[1].split(":"))[0]
                                    val userPassword = (parts[1].split(":"))[1]
                                    toClientMsg.write("Ви успішно увійшли!\n")
                                    toClientMsg.flush()
                                }

                                "MSG_USR" -> {
                                    val userName = (parts[1].split(":"))[0]
                                    val userMsg = (parts[1].split(":"))[1]

                                    // Надсилаємо повідомлення всім клієнтам
                                    broadcastMessage("$userName: $userMsg\n")
                                }

                                else -> {
                                    toClientMsg.write("Невідомий тип повідомлення: $msgType\n")
                                    toClientMsg.flush()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Помилка під час роботи з клієнтом: ${e.message}")
                        clients.remove(socket) // Видаляємо клієнта у разі помилки
                    }
                }
            }
        }
    }

    // Функція для надсилання повідомлень всім клієнтам, крім відправника
    private fun broadcastMessage(message: String) {
        clients.forEach { client ->
            try {
                val toClientMsg = client.getOutputStream().bufferedWriter()
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

package org.client

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class ServerHandler (msg: String) {

    private val port = 8080
    private val serverSocket = ServerSocket(port)

    init{
        println("Очікування клієнтів...")
    }

    fun runServer() {
        while (true){
            val clientSocket = serverSocket.accept()
            println("Підключено клієнта: ${clientSocket.inetAddress.hostAddress}")

            thread {
                clientSocket.use {
                    val fromClientMsg = it.getInputStream().bufferedReader()
                    val toClientMsg = it.getOutputStream().bufferedWriter()

                    val receivedMessage = fromClientMsg.readLine()
                    val parts = receivedMessage.split("://:")

                    val msgType = parts[0]
                    val userName = (parts[1].split(":"))[0]
                    val userPassword = (parts[1].split(":"))[1]

                    println("Від клієнта:  Тип: $msgType, UserName: $userName, Password: $userPassword\n")

                    // Додам сюди пере
                    registerUser(userName, userPassword)
                    println("User $userName was registered successfully")

                    /*toClientMsg.write("Від клієнта:  Тип: $msgType, UserName: $userName, Password: $userPassword\n")
                    toClientMsg.flush()*/
                }
            }
        }
    }

    private fun registerUser(userName: String, password: String){
        val file = File("server/src/main/resources/usersStorage.txt")

        val userData = "$userName:$password\n"
        file.appendText(userData)
    }

}


fun main(){

    Thread {
        ServerHandler("Hi from server!").runServer()
    }.start()


}
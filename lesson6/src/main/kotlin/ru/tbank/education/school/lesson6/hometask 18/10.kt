import java.net.HttpURLConnection
import java.net.URL

data class User(
    val id: Int,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String,
    val userStatus: Int
)

class SimpleUserClient {
    private val baseUrl = "https://petstore.swagger.io/v2/user"

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299)
            connection.inputStream
        else
            connection.errorStream ?: return "Нет данных от сервера"
        return stream.bufferedReader().readText()
    }

    fun createUser(user: User) {
        val url = URL(baseUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val json = """
            {
              "id": ${user.id},
              "username": "${user.username}",
              "firstName": "${user.firstName}",
              "lastName": "${user.lastName}",
              "email": "${user.email}",
              "password": "${user.password}",
              "phone": "${user.phone}",
              "userStatus": ${user.userStatus}
            }
        """.trimIndent()

        connection.outputStream.use { it.write(json.toByteArray()) }

        val responseCode = connection.responseCode
        println("Создание пользователя: $responseCode")
        println(readResponse(connection))
        connection.disconnect()
    }

    fun getUser(username: String) {
        val url = URL("$baseUrl/$username")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val responseCode = connection.responseCode
        println("Получение пользователя: $responseCode")
        println(readResponse(connection))
        connection.disconnect()
    }

    fun updateUser(username: String, user: User) {
        val url = URL("$baseUrl/$username")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val json = """
            {
              "id": ${user.id},
              "username": "${user.username}",
              "firstName": "${user.firstName}",
              "lastName": "${user.lastName}",
              "email": "${user.email}",
              "password": "${user.password}",
              "phone": "${user.phone}",
              "userStatus": ${user.userStatus}
            }
        """.trimIndent()

        connection.outputStream.use { it.write(json.toByteArray()) }

        val responseCode = connection.responseCode
        println("Обновление пользователя: $responseCode")
        println(readResponse(connection))
        connection.disconnect()
    }

    fun deleteUser(username: String) {
        val url = URL("$baseUrl/$username")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"

        val responseCode = connection.responseCode
        println("Удаление пользователя: $responseCode")
        println(readResponse(connection))
        connection.disconnect()
    }
}

fun main() {
    val client = SimpleUserClient()

    val user = User(
        id = 16,
        username = "maksim_okleev",
        firstName = "Максим",
        lastName = "Оклеев",
        email = "maksim.okleev@example.com",
        password = "qwerty123",
        phone = "+34612345678",
        userStatus = 1
    )

    println("=== POST ===")
    client.createUser(user)

    println("\n=== GET ===")
    client.getUser(user.username)
    println("\n=== PUT ===")
    val updatedUser = user.copy(firstName = "Максимка")
    client.updateUser(user.username, updatedUser)
    println("\n=== DELETE ===")
    client.deleteUser(user.username)
}
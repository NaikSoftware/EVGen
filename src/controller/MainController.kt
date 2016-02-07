package controller

import com.google.gson.Gson
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ProgressBar
import javafx.scene.control.TextField
import javafx.util.StringConverter
import model.User
import java.io.InputStreamReader
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.Executors

/**
 * Created by naik on 06.02.16.
 */
class MainController {

    val random = Random()
    val container = HashSet<Int>()
    val executor = Executors.newFixedThreadPool(1)

    @FXML lateinit var contentPane: Parent
    @FXML lateinit var jdbcField: TextField
    @FXML lateinit var usersField: TextField
    @FXML lateinit var usersPassField: TextField
    @FXML lateinit var friendshipsField: TextField
    @FXML lateinit var progressBar: ProgressBar

    val jdbcUrl: StringProperty = SimpleStringProperty("mysql://localhost:3306/database?user=root&password=1234")
    var usersCount: IntegerProperty = SimpleIntegerProperty(1)
    var usersPass: StringProperty = SimpleStringProperty()
    var friendshipsCount: IntegerProperty = SimpleIntegerProperty(0)

    val insertUser = """INSERT INTO users SET nick_name = ?, phone = ?, email = ?, auth_token = ?,
                        password=?, create_date=NOW(), hidden=FALSE, update_date=NOW(), activated=TRUE"""

    fun initialize() {
        usersField.textProperty().bindBidirectional(usersCount, numberToStringConverter)
        jdbcField.textProperty().bindBidirectional(jdbcUrl)
        usersPassField.textProperty().bindBidirectional(usersPass)
        friendshipsField.textProperty().bindBidirectional(friendshipsCount, numberToStringConverter)

        progressBar.managedProperty().bind(progressBar.visibleProperty()) // visibility behaviour "gone"
        progressBar.visibleProperty().bind(contentPane.disableProperty())
        contentPane.isDisable = false
    }

    @FXML
    fun onGenerate() {
        contentPane.isDisable = true
        executor.submit {
            println("Connect to ${jdbcUrl.get()}")
            try {
                val connection = DriverManager.getConnection("jdbc:${jdbcUrl.get()}")
                println("Connected to ${connection.metaData.databaseProductName}")

                println("Parse json data")
                var users = Gson().fromJson<Array<User>>(
                        InputStreamReader(javaClass.getResourceAsStream("/resources/dataFeb-7-2016.json")),
                        Array<User>::class.java)

                println("Available ${users.count()} users, generate ${usersCount.get()}")

                container.clear()
                val pass = usersPass.get() ?: "password not exists"
                val stmt = connection.prepareStatement(insertUser)
                for (i in 1..usersCount.get().toInt()) {
                    val u = rnd(users.count());
                    println("Get $u")
                    val user = users[u]
                    println("Save $user ")
                    stmt.setString(1, user.name)
                    stmt.setString(2, user.phone)
                    stmt.setString(3, user.email)
                    stmt.setString(4, user.token)
                    stmt.setString(5, pass)
                    stmt.execute()
                }

                connection.close()
            } catch (e: Exception) {
                e.printStackTrace()
                Alert(Alert.AlertType.ERROR, e.message, ButtonType.OK).show()
            } finally {
                contentPane.isDisable = false
            }
        }
    }

    fun rnd(max: Int) : Int {
        var tmp = 0
        var i = 0
        do {
            if (i++ > max * max) throw IllegalArgumentException("Can't generate random number")
            tmp = random.nextInt(max)
        } while (container.contains(tmp))
        container.add(tmp)
        return tmp
    }

    val numberToStringConverter =  object : StringConverter<Number>() {
        override fun toString(`object`: Number?): String? {
            return `object`.toString()
        }

        override fun fromString(string: String?): Number? {
            return if (string.isNullOrBlank()) 0 else string?.toInt()
        }
    }
}
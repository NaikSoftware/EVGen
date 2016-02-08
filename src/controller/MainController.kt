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
import java.sql.PreparedStatement
import java.util.*
import java.util.concurrent.Executors

/**
 * Created by naik on 06.02.16.
 */
class MainController {

    val random = Random()
    val container = ArrayList<Int>()
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

    val insertFriend = """INSERT INTO friends SET create_date=NOW(), hidden=FALSE, update_date=NOW(), name = ?,
                        type= ?, from_user_id= ?, to_user_id= ? """

    val selectUserId = """SELECT id FROM users WHERE email= ? """

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
                        InputStreamReader(javaClass.getResourceAsStream("/resources/data-1000.json")),
                        Array<User>::class.java)

                println("Available ${users.count()} users, generate ${usersCount.get()}")

                container.clear()
                val pass = usersPass.get() ?: "password not exists"
                val stmtUsers = connection.prepareStatement(insertUser)
                val stmtUserId = connection.prepareStatement(selectUserId)
                val stmtFriends = connection.prepareStatement(insertFriend)
                for (i in 1..usersCount.get().toInt()) {
                    val user = users[rnd(users.count())]
                    println("Save $user ")
                    stmtUsers.setString(1, user.name)
                    stmtUsers.setString(2, user.phone)
                    stmtUsers.setString(3, user.email)
                    stmtUsers.setString(4, user.token)
                    stmtUsers.setString(5, pass)
                    stmtUsers.execute()
                }

                var friendships = friendshipsCount.get()
                println("Generate $friendships friendships")
                val outRequests = HashSet<Pair<Int, Int>>()
                val friends = HashSet<Pair<Int, Int>>()
                var i = 0

                loop@ while(friendships > 0 && i <= container.count()) {
                    i++
                    val index = container[i];
                    val friendOne = users[index]
                    var j = 0
                    val maxFriends = random.nextInt(friendships / 3)
                    val perOneFriends: Int = if (maxFriends > friendships) friendships else maxFriends
                    println("Create $perOneFriends friends for ${friendOne.name}")

                    while (j < perOneFriends && j < container.count()) {
                        val otherIndex = getOtherId(index)
                        val friendSecond = users[otherIndex]
                        if (outRequests.contains(Pair(index, otherIndex))
                                || outRequests.contains(Pair(otherIndex, index))
                                || friends.contains(Pair(index, otherIndex))) continue@loop


                        val fromUserId = readUserId(stmtUserId, friendOne)
                        val toUserId = readUserId(stmtUserId, friendSecond)

                        when (random.nextInt(2)) {
                            1 -> { // Out req
                                saveFriend(stmtFriends, friendOne, "REQUEST", fromUserId, toUserId)

                                outRequests.add(Pair(index, otherIndex))
                                friendships--
                                j++
                            } else -> { // Make friends

                                saveFriend(stmtFriends, friendOne, "FRIEND", fromUserId, toUserId)
                                saveFriend(stmtFriends, friendSecond, "FRIEND", toUserId, fromUserId)
                                friendships -= 2
                                j += 2
                            }
                        }
                        println("Generated friends for user ${friendOne.name}")
                    }
                }
                println("Friends generated, not generated $friendships")

                connection.close()
            } catch (e: Exception) {
                e.printStackTrace()
                Alert(Alert.AlertType.ERROR, e.message, ButtonType.OK).show()
            } finally {
                contentPane.isDisable = false
            }
        }
    }

    fun saveFriend(stmt: PreparedStatement, user: User, type: String, fromId: Int, toId: Int) {
        stmt.setString(1, user.name)
        stmt.setString(2, type)
        stmt.setInt(3, fromId)
        stmt.setInt(4, toId)
        stmt.execute()
    }

    fun readUserId(stmt: PreparedStatement, user: User) : Int {
        stmt.setString(1, user.email)
        val result = stmt.executeQuery()
        if (result.next()) {
            return result.getInt(1)
        } else {
            throw IllegalAccessError("User with email ${user.email} not found in DB")
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

    fun getOtherId(id: Int) : Int {
        var i = 0;
        var otherId = 0
        do {
            if (i++ > container.count() * 10) throw IllegalArgumentException("Can't find other friend")
            otherId = container[random.nextInt(container.count())]
        } while(otherId == id)
        return otherId
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

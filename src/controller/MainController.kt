package controller

import com.google.gson.Gson
import javafx.application.Platform
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
import model.*
import org.joda.time.LocalDate
import rest.Google
import rest.Vkontakte
import java.io.InputStreamReader
import java.sql.Date
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.Statement
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

    @FXML lateinit var eventsCountField: TextField
    @FXML lateinit var eventsOwnerField: TextField
    @FXML lateinit var eventsSearchField: TextField

    val jdbcUrl: StringProperty = SimpleStringProperty("mysql://localhost:3306/database?user=root&password=1234&useUnicode=true&characterEncoding=utf-8")
    var usersCount: IntegerProperty = SimpleIntegerProperty(0)
    var usersPass: StringProperty = SimpleStringProperty()
    var friendshipsCount: IntegerProperty = SimpleIntegerProperty(0)
    var eventsCount: IntegerProperty = SimpleIntegerProperty(0)
    var eventsOwnerId: IntegerProperty = SimpleIntegerProperty(0)
    var eventsSearchText: StringProperty = SimpleStringProperty("Music")

    val insertUser = """INSERT INTO users SET nick_name = ?, phone = ?, email = ?, auth_token = ?,
                        password=?, create_date=NOW(), hidden=FALSE, update_date=NOW(), activated=TRUE"""

    val insertFriend = """INSERT INTO friends SET create_date=NOW(), hidden=FALSE, update_date=NOW(), from_user_name = ?,
                        from_user_id= ?, to_user_name= ?, to_user_id= ?,
                        type= ? """

    val selectUserId = """SELECT id FROM users WHERE email= ? """

    val insertEvent = """INSERT INTO events SET create_date=NOW(), update_date=NOW(), hidden=false, description=?, end_date=?,
                        event_type='TASK', max_members=?, min_members=?, name=?, start_date=?,
                        shared=0, status='RECRUITING', location_id=?, user_id=?"""

    val insertLocation = """INSERT INTO locations SET create_date=NOW(), update_date=NOW(), hidden=FALSE, address=?,
                            description=?, latitude=?, longitude=?, place=?, user_id=?"""

    val insertMember = """INSERT INTO members SET event_id=?, user_id=?, status='UNANSWERED', showed=FALSE,
                            create_date=NOW(), update_date=NOW(), hidden=FALSE"""

    val google = Google(49.0241f, 33.3519f, 50000)
    val vkontakte = Vkontakte()

    fun initialize() {
        usersField.textProperty().bindBidirectional(usersCount, numberToStringConverter)
        jdbcField.textProperty().bindBidirectional(jdbcUrl)
        usersPassField.textProperty().bindBidirectional(usersPass)
        friendshipsField.textProperty().bindBidirectional(friendshipsCount, numberToStringConverter)
        eventsCountField.textProperty().bindBidirectional(eventsCount, numberToStringConverter)
        eventsOwnerField.textProperty().bindBidirectional(eventsOwnerId, numberToStringConverter)
        eventsSearchField.textProperty().bindBidirectional(eventsSearchText)

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
                val friends = HashSet<Pair<Int, Int>>()
                var i = 0

                while(friendships > 0 && i < container.count()) {
                    val index = container[i++]
                    val friendOne = users[index]
                    var j = 0
                    //val maxFriends = friendships / 5 + random.nextInt(friendships / 3)
                    val maxFriends = friendships
                    val perOneFriends: Int = if (maxFriends > friendships) friendships else maxFriends
                    println("Create $perOneFriends friends for ${friendOne.name}")

                    loop@ while (j < perOneFriends && j < container.count()) {
                        val otherIndex = getOtherId(index)
                        val friendSecond = users[otherIndex]
                        if (friends.contains(Pair(index, otherIndex))
                                || friends.contains(Pair(otherIndex, index))) {
                            println("Skip $otherIndex")
                            j++
                            continue@loop
                        }


                        val fromUserId = readUserId(stmtUserId, friendOne)
                        val toUserId = readUserId(stmtUserId, friendSecond)

                        when (random.nextInt(2)) {
                            1 -> { // Out req
                                saveFriend(stmtFriends, friendOne, friendSecond, "REQUEST", fromUserId, toUserId)
                                friends.add(Pair(index, otherIndex))
                                friendships--
                                j++

                            } else -> { // Make friends

                                saveFriend(stmtFriends, friendOne, friendSecond, "FRIEND", fromUserId, toUserId)
                                friends.add(Pair(index, otherIndex))
                                friendships--
                                j++
                            }
                        }
                        println("Generated friends for user ${friendOne.name}")
                    }
                }

                container.clear()
                var factEventsCount = 0
                var locationsCreated = 0
                if (eventsCount.get() > 0) {
                    val userId = eventsOwnerId.get()
                    println("\n\nStart generate ${eventsCount.get()} events for $userId user")

                    val stmtEvents = connection.prepareStatement(insertEvent, Statement.RETURN_GENERATED_KEYS)
                    val stmtLocation = connection.prepareStatement(insertLocation, Statement.RETURN_GENERATED_KEYS)
                    val stmtMember = connection.prepareStatement(insertMember)

                    val events = vkontakte.findEvents(eventsSearchText.get(), eventsCount.get())
                    factEventsCount = events.size

                    for (k in 0..factEventsCount - 1) {
                        val locationId = generateLocation(stmtLocation, userId)
                        val eventId = saveEvent(events[k], locationId, userId, stmtEvents)
                        saveMember(eventId, userId, stmtMember)
                        println("Saved event ${events[k]}")
                    }
                    locationsCreated = container.size
                }

                Platform.runLater {
                    Alert(Alert.AlertType.INFORMATION,
                            """Finished:
                            ${usersCount.get()} users created;
                            ${friendshipsCount.get() - friendships} friendships created;
                            $factEventsCount events created for user ${eventsOwnerId.get()};
                            $locationsCreated locations created.""",
                            ButtonType.OK).show()
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

    fun saveEvent(group: GroupDetails, locationId: Int, userId: Int, stmtEvents: PreparedStatement) : Int {
        with(stmtEvents) {
            clearParameters()
            val start = generateStartDate()
            setString(1, clear4byteChars(group.description))
            setDate(2, generateEndDate(start))
            setInt(3, 1)
            setInt(4, 1)
            setString(5, clear4byteChars(group.name))
            setDate(6, start)
            setInt(7, locationId)
            setInt(8, userId)
        }
        return execAndGetId(stmtEvents)
    }

    fun generateLocation(stmtLocation: PreparedStatement, userId: Int): Int {
        if (container.size > 0 && random.nextInt(3) == 0) { // return from cache
            return container[random.nextInt(container.size)]
        }
        with(google.getRandomLocationDeatils() ?: LocationDetails.default) { // else gen new
            stmtLocation.setString(1, vicinity)
            stmtLocation.setString(2, address)
            stmtLocation.setFloat(3, geometry.location.lat)
            stmtLocation.setFloat(4, geometry.location.lng)
            stmtLocation.setString(5, name)
            stmtLocation.setInt(6, userId)
        }
        val id = execAndGetId(stmtLocation)
        container.add(id)
        return id
    }

    fun saveMember(eventId: Int, userId: Int, stmtMember: PreparedStatement) {
        with(stmtMember) {
            setInt(1, eventId)
            setInt(2, userId)
            execute()
        }
    }

    fun saveFriend(stmt: PreparedStatement, userOne: User, userSecond: User, type: String, fromId: Int, toId: Int) {
        stmt.setString(1, userOne.name)
        stmt.setInt(2, fromId)
        stmt.setString(3, userSecond.name)
        stmt.setInt(4, toId)
        stmt.setString(5, type)
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

    fun execAndGetId(stmt: PreparedStatement) : Int {
        stmt.execute()
        val result = stmt.generatedKeys
        result.next()
        val id = result.getInt(1)
        return id
    }

    fun rnd(max: Int) : Int {
        var tmp = 0
        var i = 0
        do {
            if (i++ > max * max * max) throw IllegalArgumentException("Can't generate random number")
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

    fun generateStartDate() : Date {
        return Date(LocalDate().plusDays(random.nextInt(30) - random.nextInt(30)).toDate().time)
    }

    fun generateEndDate(startDate: Date) : Date {
        return Date(LocalDate(startDate.time).plusDays(random.nextInt(7)).toDate().time)
    }

    fun clear4byteChars(text: String?) : String? {
        return text?.filter { c -> Character.isSurrogate(c).not() }
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

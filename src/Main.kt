import javafx.application.Application
import javafx.fxml.FXMLLoader.load
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * Created by naik on 06.02.16.
 */
class Main : Application() {

    val layout = "/resources/main.fxml"

    override fun start(primaryStage: Stage) {
        System.setProperty("prism.lcdtext", "false") // for beautiful fonts
        primaryStage.scene = Scene(load<Parent>(Main::class.java.getResource(layout)))
        primaryStage.show()
    }

    companion object {

        val API_KEY = "AIzaSyBwVAlyxzsx41cD9ck0TcoJtiOwgby4nXE"

        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}
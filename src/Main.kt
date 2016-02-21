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

        val GOOGLE_API_KEY = "AIzaSyBwVAlyxzsx41cD9ck0TcoJtiOwgby4nXE"
        val VK_ACCESS_TOKEN = "31f1d047d4e822c16cde40bbc72da1bd2649ea52c3e201d007f549963e98b2bbc04c536a009e5ebb79322"

        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}
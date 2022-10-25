package programm_starter;

import game.GameModel;
import gui.MillPresenter;
import gui.MillView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application
{

    public static void main(String[] args)
    {
        launch(args);
    }
    @Override
    public void start(Stage mainStage) throws Exception
    {
        MillPresenter presenter = new MillPresenter();
        GameModel model = new GameModel();
        MillView view = new MillView(presenter);
        presenter.startGameFrame();
        presenter.setModel(model);
        presenter.setView(view);
        presenter.initView();
        final Scene scene = new Scene(view,1066,800);
        mainStage.setScene(scene);
        mainStage.setTitle("Mill_Master2020");
        scene.widthProperty().addListener(e -> presenter.onSizeChange());
		scene.heightProperty().addListener(e -> presenter.onSizeChange());
		mainStage.setOnCloseRequest(e -> presenter.onClose());
        mainStage.show();
    }
}

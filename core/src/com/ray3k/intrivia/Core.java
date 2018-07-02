package com.ray3k.intrivia;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Core extends ApplicationAdapter {
    private Stage stage;
    private Skin skin;
    private Array<Question> questions;
    private String title;
    private static final float SPEED_X = -10.0f;
    private static final float SPEED_Y = -10.0f;
    private Sound winSound;
    private Sound loseSound;
    private Sound beepSound;
    private Music music;

    @Override
    public void create() {
        questions = new Array<Question>();
        createQuestions();
        
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        music.play();
        
        winSound = Gdx.audio.newSound(Gdx.files.internal("win.wav"));
        loseSound = Gdx.audio.newSound(Gdx.files.internal("lose.wav"));
        beepSound = Gdx.audio.newSound(Gdx.files.internal("beep.wav"));
        
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        
        skin = new Skin(Gdx.files.internal("Women in History Trivia.json"));
        
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(skin.getDrawable("bg-tiled"));
        stage.addActor(root);
        
        root.defaults().space(50.0f);
        Label label = new Label(title, skin, "title");
        label.setWrap(true);
        label.setAlignment(Align.center);
        root.add(label).growX();
        
        root.row();
        Table table = new Table();
        table.setBackground(skin.getDrawable("black"));
        root.add(table);
        
        int rows = 5;
        int columns = 5;
        float border = 10.0f;
        table.defaults().space(border);
        
        int i = 0;
        int value = 100;
        for (int y = 0; y < columns; y++) {
            for (int x = 0; x < rows; x++) {
                final TextButton textButton = new TextButton("$" + Integer.toString(value), skin, "screen");
                table.add(textButton).size(90.0f, 60.0f);
                
                final int questionIndex = i++;
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event,
                            Actor actor) {
                        music.setVolume(.25f);
                        beepSound.play();
                        showQuestionDialog(questions.get(questionIndex));
                        textButton.setDisabled(true);
                    }
                });
                
                if (x == 0) {
                    table.getCell(textButton).padLeft(border);
                }
                
                if (x == rows - 1) {
                    table.getCell(textButton).padRight(border);
                }
                
                if (y == 0) {
                    table.getCell(textButton).padTop(border);
                }
                
                if (y == columns - 1) {
                    table.getCell(textButton).padBottom(border);
                }
            }
            value += 100;
            table.row();
        }
    }
    
    private void showQuestionDialog(Question question) {
        final Dialog dialog = new Dialog("", skin);
        dialog.setFillParent(true);
        
        Label label = new Label(question.getQuestion(), skin, "question");
        label.setWrap(true);
        label.setAlignment(Align.center);
        dialog.getContentTable().add(label).growX();
        
        Table table = new Table();
        dialog.getButtonTable().add(table);
        
        table.defaults().space(5.0f);
        for (int i = 0; i < question.getAnswers().size; i++) {
            String answer = question.getAnswers().get(i);
            final TextButton textButton = new TextButton(answer, skin);
            table.add(textButton).growX();
            
            if (i == question.getCorrectIndex()) {
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event,
                            Actor actor) {
                        dialog.hide();
                        winSound.play();
                        music.setVolume(1.0f);
                    }
                });
            } else {
                textButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event,
                            Actor actor) {
                        textButton.setDisabled(true);
                        loseSound.play();
                    }
                });
            }
            table.row();
        }
        
        dialog.getButtonTable().getCells().get(dialog.getButtonTable().getCells().size - 1).padBottom(25.0f);
        
        dialog.show(stage);
    }
    
    private void createQuestions() {
        questions.clear();
        
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        Data data = json.fromJson(Data.class, Gdx.files.local("trivia-data.txt"));
                
        title = data.getTitle();
        
        questions.addAll(data.getQuestions());
        
        questions.shuffle();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        
        Gdx.gl.glClearColor(138 / 255.0f, 168 / 255.0f, 186 / 255.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        
        ScrollingTiledDrawable drawable = (ScrollingTiledDrawable) skin.getDrawable("bg-tiled");
        drawable.setOffsetX(drawable.getOffsetX() + SPEED_X * delta);
        drawable.setOffsetY(drawable.getOffsetY() + SPEED_Y * delta);
        
        stage.act();
        stage.draw();
        
        if (Gdx.input.isKeyJustPressed(Keys.F5)) {
            dispose();
            create();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        music.stop();
    }
}

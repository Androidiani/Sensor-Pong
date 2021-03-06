package it.unina.is2project.sensorball.game.pong;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import org.andengine.entity.scene.Scene;
import org.andengine.entity.text.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import it.unina.is2project.sensorball.R;
import it.unina.is2project.sensorball.game.bonus.BubbleBonus;
import it.unina.is2project.sensorball.game.bonus.LifeBonus;
import it.unina.is2project.sensorball.game.entity.GameObject;
import it.unina.is2project.sensorball.stats.database.dao.PlayerDAO;
import it.unina.is2project.sensorball.stats.database.dao.StatOnePlayerDAO;
import it.unina.is2project.sensorball.stats.entity.Player;
import it.unina.is2project.sensorball.stats.entity.StatOnePlayer;

public class GamePongOnePlayer extends GamePong {

    //===========================================
    // DEBUG
    //===========================================
    private final String TAG = "1PlayerGame";               // String for logcat debug

    //===========================================
    // GRAPHICS
    //===========================================
    private Text textScore;                                 // TextView for current score
    private Text textLvl;                                   // TextView for current level
    private Text textEvnt;                                  // TextView for current event
    private int theme_star;                                 // Life theme
    private GameObject lifeStar;                            // Life object
    private List<GameObject> lifeStars = new ArrayList<>(); // List of life object
    private BubbleBonus bubble;                             // Bonus ball
    private LifeBonus lifeBonus;                            // Life bonus

    //===========================================
    // GAME DATA
    //===========================================
    private int score = 0;                              // Indicates the current score
    private int gain = 0;                               // Indicates the current gain
    private int level = 1;                              // Indicates the current level
    private int old_level = 0;                          // Indicates the old level
    private static final float a = 1.3f;                // Coefficient a for level calculation
    private static final float b = 0.01f;               // Coefficient b for level calculation
    private static final float c = 2.6f;                // Coefficient c for level calculation
    private static final float d = 0.001f;              // Coefficient d for level calculation
    private static final int MAX_LIFE = 3;              // Indicates the max number of life
    private int life = MAX_LIFE - 1;                    // Indicates the current life
    private int reach_count = 1;                        // Counter for event
    private static final int MIN_REACH_COUNT = 2;       // Min value of reach count
    private static final int MAX_REACH_COUNT = 6;       // Max value of reach count
    private int event;                                  // Indicates the current event
    private static final int NO_EVENT = 0;              // ID for no event
    private static final int FIRST_ENEMY = 1;           // ID for event first_enemy
    private static final int BUBBLE_BONUS = 2;          // ID for event event
    private static final int CUT_BAR_30 = 3;            // ID for event cut bar 30%
    private static final int LIFE_BONUS = 4;            // ID for event life
    private static final int CUT_BAR_50 = 5;            // ID for event cut bar 50%
    private static final int BIG_BAR = 6;               // ID for event big bar
    private static final int REVERSE = 7;               // ID for event reverted bar
    private static final int FREEZE = 8;                // ID for event freeze
    private static final int RUSH_HOUR = 9;             // ID for event rush hour
    private static final int NUM_BONUS = 10;            // Indicates the number of event

    @Override
    protected Scene onCreateScene() {
        super.onCreateScene();

        // Adding the scoring text to the scene
        textScore = new Text(10, 10, font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textScore);
        textScore.setText(getResources().getString(R.string.text_score) + ": " + score);

        // Adding the level text to the scene
        textLvl = new Text(10, textScore.getY() + textScore.getHeight(), font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textLvl);
        textLvl.setText(getResources().getString(R.string.text_lvl) + " 1");

        // Adding the level text to the scene
        textEvnt = new Text(10, textLvl.getY() + textLvl.getHeight(), font, "", 20, getVertexBufferObjectManager());
        scene.attachChild(textEvnt);

        // Adding lifes to the scene
        for (int i = 1; i <= MAX_LIFE; i++) {
            GameObject lifeTemp = new GameObject(lifeStar);
            lifeTemp.addToScene(scene, 0.05f, 0.05f);
            lifeTemp.setPosition(lifeTemp.getDisplaySize().x - i * lifeTemp.getObjectWidth(), 0);
            lifeStars.add(lifeTemp);
        }

        // Setting up the physics of the game
        settingPhysics();

        // Clear Game variables
        clearGame();

        return scene;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!pause && !animActive)
            pauseGame();
    }

    @Override
    protected void loadGraphics() {
        super.loadGraphics();

        switch (theme) {
            case CLASSIC:
                theme_star = R.drawable.star_white;
                break;
            case GOLD:
                theme_star = R.drawable.star_gold;
                break;
            case BLUE:
                theme_star = R.drawable.star_blue;
                break;
        }

        // Life texture loading
        lifeStar = new GameObject(this, theme_star);
        // Life Bonus
        lifeBonus = new LifeBonus(this, theme_star, ball);
        // Bonus ball loading
        bubble = new BubbleBonus(this, ball);
    }

    @Override
    protected void collidesBottom() {
        super.collidesBottom();

        // Decrement and Detach Life
        Log.d(TAG, "Life: " + life);
        lifeStars.get(life).detach();
        life--;

        // Clear current event
        clearEvent();
        Log.d(TAG, "Event Cleared");

        // Setting NO EVENT for 1 reach count
        event = NO_EVENT;
        gameEvent();
        reach_count = 1;

        // Game Over section
        if (life < 0) {
            Log.d(TAG, "Game Over");
            gameOver();
        }
    }

    @Override
    protected void collidesOverBar() {
        super.collidesOverBar();

        // Score section
        addScore();
        textScore.setText(getResources().getString(R.string.text_score) + ": " + score);
        Log.d(TAG, "Score: " + score);

        // Level section
        changeSpeedByLevel();
        Log.d(TAG, "Level: " + level);

        // Event section
        reach_count--;
        Log.d(TAG, "Reach count: " + reach_count);
        if (reach_count == 0) {
            clearEvent();
            Log.d(TAG, "Event Cleared");
            callEvent();
            Log.d(TAG, "New Game Event: " + event);
            gameEvent();
            // Generating a new reach count
            Random random = new Random();
            reach_count = MIN_REACH_COUNT + random.nextInt(MAX_REACH_COUNT - MIN_REACH_COUNT + 1);
            Log.d(TAG, "New Reach Count: " + reach_count);
        }
    }

    @Override
    protected void clearGame() {
        super.clearGame();
        // Clear game data
        life = MAX_LIFE - 1;
        score = 0;
        gain = 0;
        level = 1;
        old_level = 0;
        reach_count = 1;
        event = NO_EVENT;
    }

    @Override
    public void onBackPressed() {
        if (!animActive) {
            if (!pause)
                pauseGame();

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle(getResources().getString(R.string.text_ttl_oneplayer_leavegame));
            alert.setMessage(getResources().getString(R.string.text_msg_oneplayer_leavegame));
            alert.setPositiveButton(getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.text_no), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // do nothing
                }
            });
            alert.show();
        }
    }

    @Override
    protected void bluetoothExtra() {
        //do nothing
    }

    @Override
    protected void gameEventsCollisionLogic() {
        switch (event) {
            case FIRST_ENEMY:
                previous_event = firstEnemy.collision(previous_event, touch);
                break;
            case BUBBLE_BONUS:
                score = bubble.collision(score, level, textScore);
                break;
            case LIFE_BONUS:
                life = lifeBonus.collision(life, lifeStars);
                break;
            case RUSH_HOUR:
                rushHour.collision();
                break;
        }
    }

    @Override
    protected void gameOver() {
        ball.setHandlerSpeed(0f, 0f);
        bar.setBarSpeed(0f);
        textEvnt.setText(getResources().getString(R.string.text_gameover));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Game over dialog
                AlertDialog.Builder alert = new AlertDialog.Builder(GamePongOnePlayer.this);
                alert.setTitle(getResources().getString(R.string.text_ttl_oneplayer_savegame));
                alert.setMessage(getResources().getString(R.string.text_msg_oneplayer_savegame));
                alert.setPositiveButton(getResources().getString(R.string.text_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        saveGame();
                        finish();
                    }
                });
                alert.setNegativeButton(getResources().getString(R.string.text_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                });
                alert.show();
            }
        });
    }

    @Override
    protected void saveGame() {
        PlayerDAO playerDAO = new PlayerDAO(getApplicationContext());
        Player player = playerDAO.findByNome(nickname);
        long idPlayer;

        if (player == null) {
            player = new Player(nickname);
            idPlayer = playerDAO.insert(player);
        } else idPlayer = player.getId();

        Calendar calendar = Calendar.getInstance();
        String date = "" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + ((calendar.get(Calendar.MONTH) < 9 ? "0" : "")) + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR);

        StatOnePlayer statOnePlayer = new StatOnePlayer((int) idPlayer, date, score);
        StatOnePlayerDAO statOnePlayerDAO = new StatOnePlayerDAO(getApplicationContext());

        statOnePlayerDAO.insert(statOnePlayer);

        playerDAO.close();
        statOnePlayerDAO.close();
    }

    @Override
    protected void addScore() {
        gain = getLevel() * 10;
        score += gain;
        if (event == FIRST_ENEMY)
            score += gain * 2;
        if (event == CUT_BAR_30)
            score += gain * 4;
        if (event == CUT_BAR_50)
            score += gain * 8;
        if (event == REVERSE)
            score += gain * 16;
        if (event == RUSH_HOUR)
            score += gain * 32;
    }

    private int getLevel() {
        if (score >= 50) {
            level = (int) Math.round(a * Math.log((b * score) + c) + d);
            textLvl.setText(getResources().getString(R.string.text_lvl) + " " + level);
        }
        return level;
    }

    private void changeSpeedByLevel() {
        if (old_level != getLevel() && getLevel() % 3 == 0) {
            old_level = getLevel();
            bar.setBarSpeed(1.5f * bar.getBarSpeed());
            ball.setHandlerSpeed(1.5f * ball.getHandlerSpeedX(), 1.5f * ball.getHandlerSpeedY());
            Log.d(TAG, "Speed changed");
        }
    }

    private void gameEvent() {
        switch (event) {
            case NO_EVENT:
                textEvnt.setText("");
                break;
            case FIRST_ENEMY:
                textEvnt.setText(getResources().getString(R.string.text_first_enemy));
                firstEnemy.addToScene(scene);
                break;
            case BUBBLE_BONUS:
                textEvnt.setText(getResources().getString(R.string.text_bubble));
                bubble.addToScene(scene);
                break;
            case CUT_BAR_30:
                textEvnt.setText(getResources().getString(R.string.text_cut_bar_30));
                bar.setObjectWidth(0.7f * bar.getBarWidth());
                break;
            case LIFE_BONUS:
                textEvnt.setText(getResources().getString(R.string.text_lifebonus));
                lifeBonus.addToScene(scene, life);
                break;
            case CUT_BAR_50:
                textEvnt.setText(getResources().getString(R.string.text_cut_bar_50));
                bar.setObjectWidth(0.5f * bar.getBarWidth());
                break;
            case BIG_BAR:
                textEvnt.setText(getResources().getString(R.string.text_big_bar));
                bar.setObjectWidth(1.5f * bar.getBarWidth());
                break;
            case REVERSE:
                textEvnt.setText(getResources().getString(R.string.text_reverse));
                bar.setBarSpeed(-bar.getBarSpeed());
                break;
            case FREEZE:
                textEvnt.setText(getResources().getString(R.string.text_freeze));
                ball.setHandlerSpeed(ball.getHandlerSpeedX() / 2, ball.getHandlerSpeedY() / 2);
                break;
            case RUSH_HOUR:
                textEvnt.setText(getResources().getString(R.string.text_rush));
                rushHour.addToScene(scene);
                break;
        }
    }

    private void clearEvent() {
        switch (event) {
            case NO_EVENT:
                break;
            case FIRST_ENEMY:
                firstEnemy.clear();
                break;
            case BUBBLE_BONUS:
                bubble.clear();
                break;
            case CUT_BAR_30:
                bar.setObjectWidth(bar.getBarWidth());
                break;
            case LIFE_BONUS:
                lifeBonus.clear();
                break;
            case CUT_BAR_50:
                bar.setObjectWidth(bar.getBarWidth());
                break;
            case BIG_BAR:
                bar.setObjectWidth(bar.getBarWidth());
                break;
            case REVERSE:
                bar.setBarSpeed(-bar.getBarSpeed());
                break;
            case FREEZE:
                ball.setHandlerSpeed(ball.getHandlerSpeedX() * 2, ball.getHandlerSpeedY() * 2);
                break;
            case RUSH_HOUR:
                rushHour.clear();
                break;
        }
    }

    private void callEvent() {
        // Generating a new event, different from current event
        Random random = new Random();
        int new_event;
        do {
            new_event = random.nextInt(level) % NUM_BONUS;
        }
        while ((new_event == event && level > 1) || (new_event == LIFE_BONUS && life == MAX_LIFE - 1));
        event = new_event;
    }
}

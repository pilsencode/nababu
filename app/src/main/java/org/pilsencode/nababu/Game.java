package org.pilsencode.nababu;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This class represents game and all associated otherPlayers.
 *
 * Created by veny on 11.11.14.
 */
public class Game implements Drawable {

    // debugging
    public static final boolean D = true;
    public static final String TAG = "nababu";

    // color schema (see http://paletton.com/)
    public static final int SCREEN_BG_COLOR = Color.rgb(170, 121, 57);
    public static final int TEXT_COLOR = Color.rgb(255, 255, 255);
    public static final int FIELD_BG_COLOR = Color.rgb(212, 167, 106);
    public static final int FIELD_BORDER_COLOR = Color.rgb(85, 49, 0);

    /**
     * Screen board base size - every move which is triggered by game event must be standardized
     * to this screen size.
     */
    public static final int FIELD_SIZE_BASE = 1000;

    /**
     * Number which is used to convert moves - it's counted by current mobile screen size and
     * FIELD_SIZE_BASE
     */
    private double sizeMultiplier;

    /**
     * Border width in px (in the dimension of device)
     */
    public static final int BORDER_WIDTH = 10;

    // singleton instance
    private static Game instance = null;

    private Player me;
    private List<Player> otherPlayers = new ArrayList<Player>();

    private int fieldSizeX;
    private int fieldSizeY;

    /**
     * Size of the board where players can move
     */
    private int innerSize;

    /**
     * Flag is this mobile device is server
     */
    private boolean server = false;

    /**
     * Private constructor to defeat instantiation of singleton.
     */
    private Game() { }

    /**
     * Gets singleton instance of game.
     * @return the game
     */
    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    /**
     * Method which receives size of the screen and calculates board size and also
     * other number which are needed for rendering players
     *
     * @param x int X size of mobile screen
     * @param y int Y size of mobile screen
     */
    public void setBoardSize(int x, int y) {
        fieldSizeX = x;
        fieldSizeY = y;
        innerSize = Math.min(fieldSizeX, fieldSizeY) - (2 * BORDER_WIDTH);
        sizeMultiplier = (double)innerSize / FIELD_SIZE_BASE;
    }

    public Player getMe() {
        return me;
    }

    public void setMe(Player player) {
        me = player;
    }

//    public void addAI() {
//        // add another player - temporary
//        addPlayer(new Player("AI"));
//    }

    /**
     * Add player to the list of players
     *
     * @param player
     */
    public void addPlayer(Player player) {
        otherPlayers.add(player);
    }

    /**
     * Add player to the list of players
     * When player already exist in the list of players, he will not be added
     *
     * @param player
     */
    public void addPlayerSkipDuplicity(Player player) {
        if (!existPlayer(player)) {
            otherPlayers.add(player);
        }
    }

    public List<Player> getOtherPlayers() {
        return otherPlayers;
    }

    /**
     * Get player object by name (it returns also "me" if name matches)
     *
     * @param name Name of the player who should be found
     * @return Player or null when player not found
     */
    public Player getPlayer(String name) {
        // check if it's me
        if (null != me && me.getName().equals(name)) {
            return me;
        }

        // check if it's in the list of other players
        for (Player p : otherPlayers) {
            if (name.equals(p.getName())) {
                return p;
            }
        }

        throw new IllegalStateException("player not found, name=" + name);
    }

    /**
     * Check if player already exists in the list of other players
     *
     * @param player Player which should be checked (check by name)
     * @return TRUE if user already exists in the list of other players
     */
    public boolean existPlayer(Player player) {
        // check if it's in the list of other players
        for (Player p : otherPlayers) {
            if (player.getName().equals(p.getName())) {
                return true;
            }
        }

        return false;
    }

    public Player getBaba() {
        if (null != me && me.isBaba()) { return me; }

        for (Player p : otherPlayers) {
            if (p.isBaba()) { return p; }
        }

        throw new IllegalStateException("baba not found");
    }


    /**
     * Checks if a player is caught by baba and return him if yes.
     *
     * @return caught player or <i>null</i> if nobody was caught
     */
    public Player findCaughtPlayer() {
        List<Player> all = allPlayers();

        // find baba and remove her from list
        Player baba = null;
        for (Player p : all) {
            if (p.isBaba()) {
                baba = p;
                all.remove(p);
                break;
            }
        }
        if (null == baba) { throw new IllegalStateException("nobody is baba"); }
        int babaAdaptedRadius = adaptSize(baba.getRadius());

        // test contact
        for (Player p : all) {
            int adaptedRadius = adaptSize(p.getRadius());
            if (
                    ((baba.getCoordinates().x + baba.getRadius() > p.getCoordinates().x - p.getRadius()
                        && baba.getCoordinates().x + baba.getRadius() < p.getCoordinates().x + p.getRadius())
                    || (baba.getCoordinates().x - baba.getRadius() > p.getCoordinates().x - p.getRadius()
                        && baba.getCoordinates().x - baba.getRadius() < p.getCoordinates().x + p.getRadius()))
                &&
                    ((baba.getCoordinates().y + baba.getRadius() > p.getCoordinates().y - p.getRadius()
                            && baba.getCoordinates().y + baba.getRadius() < p.getCoordinates().y + p.getRadius())
                            || (baba.getCoordinates().y - baba.getRadius() > p.getCoordinates().y - p.getRadius()
                            && baba.getCoordinates().y - baba.getRadius() < p.getCoordinates().y + p.getRadius()))
               ) {
                return p;
            }
        }
        return null;
    }

    /**
     * Check if given name is free and can be assigned to a player
     *
     * @param name Nam which should be checked
     * @return TRUE if name is free
     */
    public boolean isNameFree(String name) {
        // check if it's me
        if (me.getName().equals(name)) {
            return false;
        }

        // check if it's in the list of other players
        for (Player p : otherPlayers) {
            if (name.equals(p.getName())) {
                return false;
            }
        }

        // if name was not mine nor any other player's then it's free
        return true;
    }


    /**
     * Returns TRUE when current game instance is server
     *
     * @return Boolean TRUE if this player server
     */
    public boolean isServer() {
        return server;
    }

    /**
     * Setter for server flag
     *
     * @param server Boolean - TRUE or FALSE
     */
    public void setServer(boolean server) {
        this.server = server;
    }

    /**
     * This method is called by onSensorChanged() method
     * It normalizes increments and fires GameEvent
     *
     * @param angleX Tilt angle of the device (-1 - +1) on X axis
     * @param angleY Tilt angle of the device (-1 - +1) on Y axis
     */
    public void moveMe(double angleX, double angleY) {
        Point coordinates = me.getCoordinates();
        coordinates.x += me.getSpeed()*angleX;
        coordinates.y += me.getSpeed()*angleY;

        /* make sure player will not go out from the board */
        if (coordinates.x < me.getRadius()) {
            coordinates.x = me.getRadius();
        }
        if (coordinates.y < me.getRadius()) {
            coordinates.y = me.getRadius();
        }
        if (coordinates.x > (FIELD_SIZE_BASE - me.getRadius())) {
            coordinates.x = FIELD_SIZE_BASE - me.getRadius();
        }
        if (coordinates.y > (FIELD_SIZE_BASE - me.getRadius())) {
            coordinates.y = FIELD_SIZE_BASE - me.getRadius();
        }

        GameEvent event;
        event = new GameEvent(ActionEnum.MOVE, me.getName(), String.valueOf(coordinates.x), String.valueOf(coordinates.y));

        // trigger game event that I moved
        triggerEvent(event);
    }

    /**
     * Starts a new game.
     */
    public void start(String babaName) {
        if (null == babaName || 0 == babaName.trim().length()) {
            throw new IllegalArgumentException("baba name cannot be empty");
        }

        List<Player> all = allPlayers();
        // set Baba according to given name
        // and
        // set coordinates (left-top for baba, right-bottom for others)
        for (Player p : all) {
            p.setBaba(babaName.equals(p.getName()));

            if (p.isBaba()) {
                p.getCoordinates().x = 0 + p.getRadius();
                p.getCoordinates().y = 0 + p.getRadius();
            } else {
                p.getCoordinates().x = FIELD_SIZE_BASE - p.getRadius();
                p.getCoordinates().y = FIELD_SIZE_BASE - p.getRadius();
            }
        }
    }

    /**
     * Reset connections when the game should start again
     */
    public void reset() {
        if (null != me && null != me.getCommunicator()) {
            me.getCommunicator().finish();
        }
        me = null;
        for (Player p : otherPlayers) {
            if (null != p.getCommunicator()) { p.getCommunicator().finish(); }
        }
        otherPlayers.clear();
    }

    // ----------------------------------------------------------- Helper Stuff

    /**
     * Unify handling of own events and events from other players
     *
     * @param event
     */
    public void onGameEvent(Game.GameEvent event) {
        switch (event.action) {
            case MOVE:
                // decode params of move action
                String name = event.params[0];

                // if other player moved, change his position (I'm moving in method moveMe())
                if (!name.equals(Game.getInstance().getMe().getName())) {
                    // find object of player who moved
                    // On the server this player is also in event.player, but on the client side not...
                    int positionX = Integer.valueOf(event.params[1]);
                    int positionY = Integer.valueOf(event.params[2]);

                    Player player = Game.getInstance().getPlayer(name);

                    // move by the player (update his coordinates)
                    player.getCoordinates().x = positionX;
                    player.getCoordinates().y = positionY;
                }

                // send MOVE action to server/clients
                Game.getInstance().sendToOthers(event);

                /* server checks if somebody was caught */
                Player caughtPlayer = null;
                if (Game.getInstance().isServer()) {
                    caughtPlayer = Game.getInstance().findCaughtPlayer();

                    if (null != caughtPlayer) {
                        event = new GameEvent(ActionEnum.BABA, caughtPlayer.getName());
                        // trigger game event somebody was caught
                        triggerEvent(event);
                    }
                }

                break;

            case BABA:
                // inform clients that baba changes
                if (Game.getInstance().isServer()) {
                    Game.getInstance().sendToOtherPlayers(event);
                }

                // update caught counter about
                String caught = event.params[0];
                Game.getInstance().getPlayer(caught).increaseCaught();
                Game.getInstance().start(caught);
                break;
        }
    }

    // ----------------------------------------------------------- Helper Stuff

    /**
     * Creates list of all players (including 'me').
     *
     * @return all players
     */
    public List<Player> allPlayers() {
        List<Player> players = new ArrayList<Player>(otherPlayers);
        players.add(me);
        return players;
    }

    /**
     * Change size of dimension from the normalized on (1000 x 1000) to the device screen size
     *
     * @param size Normalized dimension (FIELD_SIZE_BASE)
     * @return Size of move which will be rendered on this device
     */
    private int adaptSize(int size) {
        // the bigger is screen size in pixels, the bigger must be size of the move
        return (int)(size * sizeMultiplier);
    }

    // --------------------------------------------------------- Drawable Stuff

    private Paint paint = new Paint();

    @Override
    public void draw(Canvas canvas) {
        int rectSize = Math.min(fieldSizeX, fieldSizeY);
        int top = (fieldSizeY - rectSize) / 2;

        // field border
        paint.setColor(FIELD_BORDER_COLOR);
        paint.setStrokeWidth(BORDER_WIDTH);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, top, rectSize, top + rectSize, paint);

        // field background
        paint.setColor(FIELD_BG_COLOR);
        paint.setStrokeWidth(BORDER_WIDTH);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawRect(
                BORDER_WIDTH, top + BORDER_WIDTH,
                rectSize - BORDER_WIDTH, top + rectSize - BORDER_WIDTH, paint);

        // show how many times current player was caught by baba
        paint.setColor(TEXT_COLOR);
//        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(60);
        canvas.drawText(
            "" + (String.valueOf(me.getCaughtCounter())) + "",
            140,
            120,
            paint
        );


        // draw 'app otherPlayers'
        drawAllPlayers(canvas, top);
    }

    /**
     * Renders all otherPlayers.
     */
    private void drawAllPlayers(Canvas canvas, int top) {
        drawPlayer(canvas, me, top);
        for (Player player : otherPlayers) {
            drawPlayer(canvas, player, top);
        }
    }

    /**
     * Draws one player.
     */
    private void drawPlayer(Canvas canvas, Player player, int top) {
        if (!player.isActivated()) {
            return;
        }

        int adaptedX = adaptSize(player.getCoordinates().x);
        int adaptedY = adaptSize(player.getCoordinates().y);
        int adaptedRadius = adaptSize(player.getRadius());

        // draw player
        paint.setColor(player.getColor());
        canvas.drawRect(
                BORDER_WIDTH + adaptedX - adaptedRadius,
                top + BORDER_WIDTH + adaptedY - adaptedRadius,
                BORDER_WIDTH + adaptedX + adaptedRadius,
                top + BORDER_WIDTH + adaptedY + adaptedRadius,
                paint);

        // symbol on 'player'
        int fontSize = player.getRadius() - 10;
        // mark baba with red color
        if (player.isBaba()) {
            paint.setColor(Color.RED);
        } else {
            paint.setColor(Color.BLACK);
        }

        paint.setTypeface(Typeface.MONOSPACE);
        paint.setTextSize(fontSize);
        canvas.drawText(
                player.getSymbol(),
                BORDER_WIDTH + adaptedX - (fontSize / 3),
                top + BORDER_WIDTH + adaptedY + (fontSize / 3), paint);

    }

    // --------------------------------------- Communicating with Players

    /**
     * When this device is server, it sends game event to other players (except the player who send the packet)
     * When this device is client, it sends game event to the server (which will pass it to other players if needed)
     *
     * Maybe args of this method should be "Player sourcePlayer, ActionEnum action, Object... params" ?
     *
     * @param event
     */
    public void sendToOthers(GameEvent event) {
        if (Game.getInstance().isServer()) {
            sendToOtherPlayersExceptSource(event);
        } else {
            sendToServer(event);
        }
    }

    /**
     * Send message to the server
     *
     * Action will not be sent when I'm the server
     */
    public void sendToServer(Game.GameEvent event) {
        if (isServer()) {
            throw new IllegalStateException("I am server, call of sendToServer() is invalid");
        }

        // send packet with this game event to server
        me.getCommunicator().sendPacket(AbstractBTActivity.encodePacket(event.action, event.params));
    }

    /**
     * Send this game event to all other players
     * and also don't send it to server..me ;)
     */
    public void sendToOtherPlayers(Game.GameEvent event) {
        if (!isServer()) {
            throw new IllegalStateException("I am not a server, call of sendToOtherPlayers() is invalid");
        }

        for (Player player : this.otherPlayers) {
            player.getCommunicator().sendPacket(AbstractBTActivity.encodePacket(event.action, event.params));
        }
    }

    /**
     * Send this game event to all other players except to the one who send it to server
     * and also don't send it to server..me ;)
     */
    public void sendToOtherPlayersExceptSource(Game.GameEvent event) {
        if (!isServer()) {
            throw new IllegalStateException("I am not a server, call of sendToOtherPlayersExceptSource() is invalid");
        }

        for (Player player : this.otherPlayers) {
            if (!player.getName().equals(event.player.getName())) {
                player.getCommunicator().sendPacket(AbstractBTActivity.encodePacket(event.action, event.params));
            }
        }
    }

    // --------------------------------------- Communicating with the UI Thread

    /**
     * Object with info about things which happens in the game
     */
    public static class GameEvent {
        /**
         * Action which happened
         */
        public final ActionEnum action;

        /**
         * Player object with Communication object
         * - If I'm the server, then this Player object is the one who send the packet to the server
         * - If I'm client, then this Player object is me and it's communicator can be used to send packets to the server
         */
        public final Player player;

        /**
         * Parameters of the action
         */
        public final String[] params;

        /**
         * Standard constructor
         *
         * @param player Player with communication object - see class parameter comment for more info
         * @param action Action which happened
         * @param params Action parameters
         */
        public GameEvent(Player player, ActionEnum action, String... params) {
            this.player = player;
            this.action = action;
            this.params = params;
        }

        /**
         * Constructor of GameEvent which will be triggered by current player
         * Current player object will be added automatically
         *
         * @param action Action which happened
         * @param params Action parameters
         */
        public GameEvent(ActionEnum action, String... params) {
            this(Game.getInstance().getMe(), action, params);
        }
    }

    /**
     * Interface for Observers which want to listen Game events
     */
    public interface GameEventObserver {
        /**
         * Method which will be called on observer when game event occurs
         *
         * @param event
         */
        void onGameEvent(GameEvent event);
    }

    /**
     * List of registered observers
     */
    private Set<GameEventObserver> observers = new HashSet<GameEventObserver>();

    public void registerEventObserver(GameEventObserver observer) {
        observers.add(observer);
    }

    public void removeEventObserver(GameEventObserver observer) {
        observers.remove(observer);
    }

    public void triggerEvent(GameEvent event) {
        // At first inform Game singleton that event occurred
        Game.getInstance().onGameEvent(event);

        // then inform other observers
        for (GameEventObserver observer : observers) {
            observer.onGameEvent(event);
        }
    }

    /**
     * Handler which is used to communicate from one thread to UI thread
     *
     * Handler gets in constructor destination thread - Looper.getMainLooper() is UI thread
     */
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            GameEvent event = (GameEvent) msg.obj;
            triggerEvent(event);
        }
    };

    public Handler getHandler() {
        return handler;
    }

}

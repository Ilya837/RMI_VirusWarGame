package org.example;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Scanner;
import javafx.util.Pair;


public  class Game implements Player {

    private int PlayerNum;

    private int[][] GameField = new int[10][10];

    private Player Opponent;

    Game(){
        PlayerNum = 1;
    }

    enum Symbols{
        EMPTY,
        CROSS,
        CIRCLE,
        DEADCIRCLE,

        DEADCROSS

    }

    char[] Icons = {' ','x','o', '●', '⦻'  };
    @Override
    public void ConnectMe(int port, int playerNum) throws RemoteException {

        try {
            Registry registry = LocateRegistry.getRegistry(null, port);

            Opponent = (Player) registry.lookup("Player");

            PlayerNum = playerNum;

        } catch (Exception ignored){}

    }
    @Override
    public void Move(int i, int j, int playerNum) throws RemoteException {

        int now = GameField[i][j];
        if(now == Symbols.EMPTY.ordinal()) GameField[i][j] = playerNum;
        if(now == Symbols.CROSS.ordinal()) GameField[i][j] = Symbols.DEADCROSS.ordinal();
        if(now == Symbols.CIRCLE.ordinal()) GameField[i][j] = Symbols.DEADCIRCLE.ordinal();
    }

    void GameCicle(){
        try{
            PrintField();
            Opponent.PrintField();
        } catch (Exception ignored) {}

        int winner = 0;
        boolean end = false;

        while (!end) {

            for (int i = 0; i < 3; i++) {
                try {

                    if(CheckEnd(1)) {
                        winner = 2;
                        end = true;
                        continue;
                    }

                    Pair<Integer, Integer> pair = new Pair<>(0,0);

                    if(PlayerNum == 1)  pair = PlayerMove();
                    if(PlayerNum == 2)  pair = Opponent.PlayerMove();

                    Move(pair.getKey(), pair.getValue(), 1);
                    Opponent.Move(pair.getKey(), pair.getValue(), 1);
                    PrintField();
                    Opponent.PrintField();
                } catch (Exception ignored) {
                }
            }



            for (int i = 0; i < 3; i++) {
                try {
                    if(CheckEnd(2)){
                        winner = 1;
                        end = true;
                        continue;
                    }

                    Pair<Integer, Integer> pair = new Pair<>(0,0);

                    if(PlayerNum == 1)  pair = Opponent.PlayerMove();
                    if(PlayerNum == 2)  pair = PlayerMove();

                    Move(pair.getKey(), pair.getValue(), 2);
                    Opponent.Move(pair.getKey(), pair.getValue(), 2);
                    PrintField();
                    Opponent.PrintField();
                } catch (Exception ignored) {
                }
            }
        }

        if(PlayerNum == winner){
            System.out.println("You win!");

            try {
                Opponent.Println("You lose");
            }catch (Exception ignored){};

        }
        else{
            System.out.println("You lose");
            try {
                Opponent.Println("You win!");
            }catch (Exception ignored){};
        }


    }

    public void Start(){
        System.out.println("1. New Game");
        System.out.println("2. Connect to game");

        Scanner scan = new Scanner(System.in);

        String insert;

        while (true){
            insert = scan.nextLine();
            if(insert.equals("1") || insert.equals("2")) break;
            else System.out.println("Wrong insert");
        }

        if(insert.equals("1")){
            int port = (int)(Math.random() * 1000);
            try {
                Game obj = new Game();
                Player stub = (Player) UnicastRemoteObject.exportObject(obj, 0);

                Registry registry = LocateRegistry.createRegistry(port);
                registry.bind("Player", stub);

                System.out.println("port : " + port);
            }
            catch (Exception ignored) {
            }

        }
        else{
            System.out.println("Введите порт другого игрока");
            int port;

            while (true) {
                if (scan.hasNextInt()){
                    port = scan.nextInt();
                    break;
                }
                else System.out.println("Ошибка ввода");

            }

            try {
                Registry registry = LocateRegistry.getRegistry(null, port);

                Opponent = (Player) registry.lookup("Player");

            } catch (Exception ignored){}

            port = (int)(Math.random() * 1000);

            PlayerNum =(int)(Math.random() * 1000) % 2 + 1;

            try {
                Game obj = new Game();
                Player stub = (Player) UnicastRemoteObject.exportObject(obj, 0);

                Registry registry = LocateRegistry.createRegistry(port);
                registry.bind("Player", stub);

                if(PlayerNum == 1){
                    Opponent.ConnectMe(port, 2);
                }
                else{
                    Opponent.ConnectMe(port, 1);
                }

                GameCicle();

            }
            catch (Exception ignored) {
            }

        }

    }

    boolean CheckFormat(String moveText){

        if (moveText.length() != 2)
            if(moveText.length() == 3 && moveText.charAt(1) == '1' && moveText.charAt(2) == '0'){
                return Character.isLetter(moveText.charAt(0)) && moveText.charAt(0) <= 'j';
            }
            else return false;

        if (!Character.isDigit(moveText.charAt(1)) || moveText.charAt(1) == '0' ) return false;

        return Character.isLetter(moveText.charAt(0)) && moveText.charAt(0) <= 'j';

    }

    boolean CheckMove(int i, int j){

        if(PlayerNum == 1){
            if(GameField[9][0] == Symbols.EMPTY.ordinal()) return (i == 9 && j == 0);

            if(     GameField[i][j] == Symbols.CROSS.ordinal() ||
                    GameField[i][j] == Symbols.DEADCROSS.ordinal() ||
                    GameField[i][j] == Symbols.DEADCIRCLE.ordinal()) return false;


            for(int k = -1; k<2;k++){
                for(int l = -1; l<2;l++){

                    if(j + l > -1 && j + l < 10 && i + k > -1 && i + k < 10) {

                        if (GameField[i + k][j + l] == Symbols.CROSS.ordinal()) return true;

                        if (GameField[i + k][j + l] == Symbols.DEADCIRCLE.ordinal() &&
                                CheckWay(i + k, j + l, Symbols.DEADCIRCLE) ) return true;

                    }
                }

            }

            return false;

        }
        else{
            if(GameField[0][9] == Symbols.EMPTY.ordinal()) return (i == 0 && j == 9);

            if(     GameField[i][j] == Symbols.CIRCLE.ordinal() ||
                    GameField[i][j] == Symbols.DEADCROSS.ordinal() ||
                    GameField[i][j] == Symbols.DEADCIRCLE.ordinal()) return false;



            for(int k = -1; k<2;k++){
                for(int l = -1; l<2;l++){

                    if(j + l > -1 && j + l < 10 && i + k > -1 && i + k < 10) {

                        if (GameField[i + k][j + l] == Symbols.CIRCLE.ordinal()) return true;

                        if (GameField[i + k][j + l] == Symbols.DEADCROSS.ordinal() &&
                                CheckWay(i + k, j + l, Symbols.DEADCROSS)) return true;

                    }
                }

            }

            return false;
        }


    }


    public static int[][] Copy(int[][] src) {
        int[][] dst = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            dst[i] = Arrays.copyOf(src[i], src[i].length);
        }
        return dst;
    }

    void DeadChain (int i,int j, int[][] Field, Symbols symbol){

        Field[i][j] = -2;


        for(int k = -1; k< 2;k++ ){
            for(int l = -1; l<2;l++){
                if( (i + k > -1) && (i + k < 10) && (j + l > -1) && (j + l < 10)){
                    if(Field[i+k][j+l] == symbol.ordinal()){

                        DeadChain(i+k,j+l,Field, symbol);
                    }
                }
            }
        }


    }

    boolean CheckEnd(int playerNum){

        if(GameField[GameField.length-1][0] == Symbols.EMPTY.ordinal() ||
                GameField[0][GameField[0].length-1] == Symbols.EMPTY.ordinal()) return false;

        int[][] Field = Copy(GameField);



        for(int i = Field.length - 1; i > -1 ;i--){
            for(int j = 0; j< Field[i].length;j++){
                if( Field[i][j] == (playerNum == 1 ? Symbols.CROSS.ordinal() : Symbols.CIRCLE.ordinal() )  ){

                    Field[i][j] = -1;

                    for(int k = -1; k< 2;k++ ){
                        for(int l = -1; l<2;l++){
                            if( (i + k > -1) && (i + k < 10) && (j + l > -1) && (j + l < 10)){
                                int tmp = Field[i+ k][j + l];

                                if ( tmp == Symbols.EMPTY.ordinal()) return false;

                                if ( tmp == Symbols.CIRCLE.ordinal() && playerNum == 1) return false;
                                if ( tmp == Symbols.CROSS.ordinal() && playerNum == 2)  return false;

                                if(tmp == Symbols.DEADCIRCLE.ordinal() && playerNum == 1)   DeadChain(i,j,Field, Symbols.DEADCIRCLE);
                                if(tmp == Symbols.DEADCROSS.ordinal() && playerNum == 2)    DeadChain(i,j,Field, Symbols.DEADCROSS);

                            }
                        }
                    }

                }
            }
        }

        for(int i = Field.length - 1; i > -1 ;i--){
            for(int j = 0; j< Field[i].length;j++){
                if( Field[i][j] == -2 ){

                    for(int k = -1; k< 2;k++ ){
                        for(int l = -1; l<2;l++){
                            if( (i + k > -1) && (i + k < 10) && (j + l > -1) && (j + l < 10)){
                                int tmp = Field[i+ k][j + l];

                                if ( tmp == Symbols.EMPTY.ordinal() ) return false;

                                if(tmp == Symbols.CIRCLE.ordinal() && playerNum == 1) return false;
                                if(tmp == Symbols.CROSS.ordinal() && playerNum == 2) return false;

                            }
                        }
                    }

                }
            }
        }

        return true;
    }


    boolean CheckWay(int i, int j, Symbols symbol){

        int[][] Field = Copy(GameField);



        for(int m = -1; m< 2;m++ ){
            for(int n = -1; n<2;n++){

                if( (i + m > -1) && (i + m < 10) && (j + n > -1) && (j + n < 10)){
                    int tmp = Field[i+ m][j + n];

                    if(symbol == Symbols.DEADCROSS)
                        if ( tmp == Symbols.CIRCLE.ordinal()) return true;

                    if(symbol == Symbols.DEADCIRCLE)
                        if ( tmp == Symbols.CROSS.ordinal()) return true;

                }

            }
        }


        if(symbol == Symbols.DEADCROSS) DeadChain(i,j,Field,Symbols.DEADCROSS );
        if(symbol == Symbols.DEADCIRCLE) DeadChain(i,j,Field,Symbols.DEADCIRCLE );

        for(int k = 0; k < Field.length ;k++){
            for(int l = 0; l< Field[k].length;l++){
                if( Field[k][l] == -2 ){

                    for(int m = -1; m< 2;m++ ){
                        for(int n = -1; n<2;n++){

                            if( (k + m > -1) && (k + m < 10) && (l + n > -1) && (l + n < 10)){
                                int tmp = Field[k + m][l + n];

                                if(symbol == Symbols.DEADCROSS)
                                    if ( tmp == Symbols.CIRCLE.ordinal()) return true;

                                if(symbol == Symbols.DEADCIRCLE)
                                    if ( tmp == Symbols.CROSS.ordinal()) return true;

                            }

                        }
                    }

                }
            }
        }


        return false;
    }
    @Override
    public Pair<Integer,Integer> PlayerMove()  throws RemoteException {


        System.out.println("Your turn");

        Scanner scan = new Scanner(System.in);


        String move;
        int i,j;

        while (true) {
            move = scan.nextLine().toLowerCase();

            if(CheckFormat(move)) {



                if (move.length() == 3) i = 0;
                else                    i = 10 - (move.charAt(1) - '0');

                j = (move.charAt(0) - 'a');

                if (CheckMove(i,j)){
                    return new Pair<Integer,Integer>(i, j);

                }
                else System.out.println("Wrong move");

            }
            else System.out.println("Wrong format");

        }

    }

    @Override
    public void PrintField() throws RemoteException{

        //Очистить консоль

        System.out.println("\n");
        for(int i = 0; i< GameField.length;i++) {
            System.out.print(i == 0 ? "10|" :(" " + (10 - i) + "|" ));
            for (int j = 0; j < GameField[0].length; j++) {
                System.out.print(Icons[ GameField[i][j] ] + "|");
            }
            System.out.print("\n");
        }
        System.out.println("   a b c d e f g h i j\n");
    }

    @Override
    public void Println(String str) throws RemoteException {
        System.out.println(str);
    }
}

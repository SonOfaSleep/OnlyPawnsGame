/**
 * All moves that can be done. Needs to be refreshed with valid input each time.
 */
data class AllowedMoves (val input: String) {
    val firstMoveWhite = Regex("${input[0]}2${input[0]}4")
    val firstMoveBlack = Regex("${input[0]}7${input[0]}5")

    val regularMoveWhite = Regex("${input[0]}${input[1]}${input[0]}${input[1] + 1}")
    val regularMoveBlack = Regex("${input[0]}${input[1]}${input[0]}${input[1] - 1}")

    val captureWhite = Regex("${input[0]}${input[1]}[${input[0] - 1}${input[0] + 1}]${input[1] + 1}")
    val captureBlack = Regex("${input[0]}${input[1]}[${input[0] - 1}${input[0] + 1}]${input[1] - 1}")
}

/**
 * For storing moves and check if last move is valid for EnPassant later on
 */
data class MoveHistory (val input: String, val indexXY: MutableList<Int>)

/**
 * For two players and their current score in game.
 */
data class Player (val name: String, var score: Int = 0)

/**
 * The main class where game is about to happen
 */
data class ChessBoardGame (val moveCoord: MutableList<Int> = mutableListOf(0, 0, 0, 0)) {

    /**
     * Here are the main lists for pawns and empty squares.
     */
    private val chessBoard =
        MutableList(8) { mutableListOf("   ", " W ", "   ", "   ", "   ", "   ", " B ", "   ") }

    private var currentMove: String = "white"
    private var opponentPawn: String = " B "
    private lateinit var movesRegex: AllowedMoves

    /**
     * Storing all game moves here
     */
    private var moves = mutableListOf<MoveHistory>(MoveHistory("a0a0", mutableListOf(0, 0, 0, 0))) // All game moves

    /**
     * Players name's
     */
    lateinit var firstPlayer: Player
    lateinit var secondPlayer: Player

    /**
     * This one will be changed all the time. It is important for all next functions.
     */
    private var input = ""

    fun intro() {
        println("Pawns-Only Chess")
        println("First Player's name:")
        firstPlayer = Player(readLine()!!)
        println("Second Player's name:")
        secondPlayer = Player(readLine()!!)
        board()
    }

    /**
     * Rendering current board state in console.
     */
    private fun board() {
        var row = 8
        val fill = "  +---+---+---+---+---+---+---+---+"
        val files = "    a   b   c   d   e   f   g   h"
        for (i in 8 downTo 1) {
            println(fill)
            print("$row |")
            for (index in 0..7) {
                print(chessBoard[index][row - 1])
                print("|")
            }
            println()
            row -= 1
        }
        println(fill)
        println(files)
    }

    /**
     * Converts input from, as example, "a2a3" to moveCoord = mutableListOf(0, 1, 0, 2)
     */
    private fun inputMoveCoordinates(input: String) {
        val mapInput = input.map { it.toString() }
        moveCoord[1] = mapInput[1].toInt() - 1
        moveCoord[3] = mapInput[3].toInt() - 1
        for (index in input.indices step 2) {
            when {
                input[index] == 'a' -> moveCoord[index] = 0
                input[index] == 'b' -> moveCoord[index] = 1
                input[index] == 'c' -> moveCoord[index] = 2
                input[index] == 'd' -> moveCoord[index] = 3
                input[index] == 'e' -> moveCoord[index] = 4
                input[index] == 'f' -> moveCoord[index] = 5
                input[index] == 'g' -> moveCoord[index] = 6
                input[index] == 'h' -> moveCoord[index] = 7
            }
        }
    }

    private fun checkInput(input: String): Boolean {
        val overallRegex = Regex("[a-h][1-8][a-h][1-8]")
        return input.matches(overallRegex) && input.length == 4
    }

    /**
     * Checks if pawn is in start coordinates.
     */
    private fun checkPawn(): Boolean {
        inputMoveCoordinates(input)
        return when (currentMove) {
            "white" -> chessBoard[moveCoord[0]][moveCoord[1]] == " W "
            else -> chessBoard[moveCoord[0]][moveCoord[1]] == " B "
        }
    }

    /**
     * Checks if enPassant is possible
     */
    private fun checkEnPassant(): Boolean {
        return moves[moves.lastIndex].input.matches(".[27].[45]".toRegex()) &&
                moveCoord[2] == moves[moves.lastIndex].indexXY[2] &&
                (moveCoord[3] == moves[moves.lastIndex].indexXY[3] + 1 ||
                moveCoord[3] == moves[moves.lastIndex].indexXY[3] - 1)
    }

    /**
     * Checks if move is valid to do
     */
    private fun checkMove(input: String): Boolean {
        movesRegex = AllowedMoves(input)
        return when (currentMove) {
            "white" -> (input.matches(movesRegex.firstMoveWhite) && chessBoard[moveCoord[2]][moveCoord[3]] == "   " &&
                    chessBoard[moveCoord[2]][moveCoord[3] - 1] == "   ") ||
                    (input.matches(movesRegex.regularMoveWhite) && chessBoard[moveCoord[2]][moveCoord[3]] == "   ") ||
                    (input.matches(movesRegex.captureWhite) &&
                            chessBoard[moveCoord[2]][moveCoord[3]] == opponentPawn) ||
                    (input.matches(movesRegex.captureWhite) && checkEnPassant())

            else -> (input.matches(movesRegex.firstMoveBlack) && chessBoard[moveCoord[2]][moveCoord[3]] == "   " &&
                    chessBoard[moveCoord[2]][moveCoord[3] + 1] == "   ") ||
                    (input.matches(movesRegex.regularMoveBlack) && chessBoard[moveCoord[2]][moveCoord[3]] == "   ") ||
                    (input.matches(movesRegex.captureBlack) &&
                            chessBoard[moveCoord[2]][moveCoord[3]] == opponentPawn) ||
                    (input.matches(movesRegex.captureBlack) && checkEnPassant())
        }
    }

    private fun movePawn() {
        chessBoard[moveCoord[0]][moveCoord[1]] = "   "
        if (checkEnPassant() && input.matches(("${input[0]}.[${input[0] - 1}${input[0] + 1}]" +
                    "[${input[1] - 1}${input[1] + 1}]").toRegex())) {
            chessBoard[moves[moves.lastIndex].indexXY[2]][moves[moves.lastIndex].indexXY[3]] = "   "
        }
        if (chessBoard[moveCoord[2]][moveCoord[3]] == opponentPawn) {
            when (currentMove) {
                "white" -> firstPlayer.score++
                else -> secondPlayer.score++
            }
        }
        chessBoard[moveCoord[2]][moveCoord[3]] = if (currentMove == "white") " W " else " B "
        moves.add(MoveHistory(input, mutableListOf(moveCoord[0], moveCoord[1], moveCoord[2], moveCoord[3])))
    }

    /**
     * During game, it is important to know, who's move is it now. Here we change this after each move.
     */
    private fun changeCurrentMove() {
        currentMove = if (currentMove == "white") "black" else "white"
        opponentPawn = if (currentMove == "white") " B " else " W "
    }

    /**
     * Checking for win condition
     */
    private fun checkWin(): Boolean {
        return when (currentMove) {
            "white" -> firstPlayer.score == 8 || moves[moves.lastIndex].input.matches("[a-h]7[a-h]8".toRegex())
            else -> secondPlayer.score == 8 || moves[moves.lastIndex].input.matches("[a-h]2[a-h]1".toRegex())
        }
    }

    /**
     * Ending game if checkWin() is true
     */
    private fun winStatement() {
        if (checkWin()) {
            println("${currentMove[0].uppercase()}${currentMove.substring(1)} Wins!")
            input = "exit"
        }
    }

    /**
     * Checking in the end of the current player's move if the next player's move is stalemate.
     */
    private fun checkStalemate() {
        changeCurrentMove()
        var possibleMoves = 0
        for (xIndex in chessBoard.indices) {
            val yIndex: Int = chessBoard[xIndex].indexOf(when (currentMove) {"white" -> " W " else -> " B "})
            if (yIndex == -1) continue
            for (i in -1..1) {
                input = when (currentMove) {
                    "white" -> "$xIndex$yIndex${xIndex + i}${yIndex + 1}"
                    else -> "$xIndex$yIndex${xIndex + i}${yIndex - 1}"
                }
                if (input.matches("[0-7][0-7][0-7][0-7]".toRegex())) {
                    moveCoord[2] = xIndex + i
                    moveCoord[3] = when (currentMove) {"white" -> yIndex + 1 else -> yIndex - 1}
                    movesRegex = AllowedMoves(input)
                    if (checkMove(input)) possibleMoves += 1
                }
            }
        }
        changeCurrentMove()
        if (possibleMoves == 0 && firstPlayer.score != 8 && secondPlayer.score != 8) {
            println("Stalemate!")
            input = "exit"
        }
    }

    /**
     * Main game loop
     */
    fun game() {
        while (input != "exit") {
            when (currentMove) {
                "white" -> println("${firstPlayer.name}'s turn:")
                else -> println("${secondPlayer.name}'s turn:")
            }
            input = readLine()!!
            if (input == "exit") return
            when {
                !checkInput(input) -> println("Invalid Input")
                !checkPawn() -> println("No $currentMove pawn at ${input[0]}${input[1]}")
                !checkMove(input) -> println("Invalid Input")
                else -> {
                    movePawn()
                    board()
                    checkStalemate()
                    winStatement()
                    changeCurrentMove()
                }
            }
        }
    }
}

fun main() {
    val game = ChessBoardGame()
    game.intro()
    game.game()
    println("Bye!")
}
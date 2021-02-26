package hw3;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Scanner;
//import com.sun.java_cup.internal.runtime.Scanner;

import api.Cell;
import api.Direction;
import api.MoveRecord;
import api.State;
import api.StringUtil;

/**
 * Basic game state and operations for a the puzzle game "Pearls", which is a
 * simplified version of "Quell".
 * 
 * @author smkautz
 */
public class Pearls {

	/**
	 * Two-dimensional array of Cell objects representing the grid on which the game
	 * is played.
	 */
	private Cell[][] grid;

	/**
	 * Instance of PearlUtil to be used with this game.
	 */
	private PearlUtil util;

	/**
	 * keeps track of total number of moves made by player
	 */
	private int numMoves = 0;

	/**
	 * keeps track of total score (number of pearls collected) as player makes each
	 * move
	 */
	private int score = 0;

	/**
	 * keeps track of whether the game is over or not
	 */
	private boolean isOver = false;

	/**
	 * keeps track of whether the player has won or not
	 */
	private boolean hasWon = false;

	/**
	 * Constructs a game from the given string description. The conventions for
	 * representing cell states as characters can be found in
	 * <code>StringUtil</code>.
	 * 
	 * @param init      string array describing initial cell states
	 * @param givenUtil PearlUtil instance to use in the <code>move</code> method
	 */
	public Pearls(String[] init, PearlUtil givenUtil) {
		grid = StringUtil.createFromStringArray(init);
		util = givenUtil;

	}

	/**
	 * Returns the number of columns in the grid.
	 * 
	 * @return width of the grid
	 */
	public int getColumns() {
		return grid[0].length;
	}

	/**
	 * Returns the number of rows in the grid.
	 * 
	 * @return height of the grid
	 */
	public int getRows() {
		return grid.length;
	}

	/**
	 * Returns the row index for the player's current location.
	 * 
	 * @return
	 */
	public int getCurrentRow() {

		// iterates through grid to find row where player is current located
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j].isPlayerPresent()) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Returns the column index for the player's current location.
	 * 
	 * @return
	 */
	public int getCurrentColumn() {

		// iterates through grid to find column where player is currently located
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j].isPlayerPresent()) {
					return j;
				}
			}
		}
		return -1;
	}

	/**
	 * Returns the cell at the given row and column.
	 * 
	 * @param row row index for the cell
	 * @param col column index for the cell
	 * @return cell at given row and column
	 */
	public Cell getCell(int row, int col) {
		return grid[row][col];
	}

	/**
	 * Returns true if the game is over, false otherwise. The game ends when all
	 * pearls are removed from the grid or when the player lands on a cell with
	 * spikes.
	 * 
	 * @return true if the game is over, false otherwise
	 */
	public boolean isOver() {
		return isOver;
	}

	/**
	 * Returns true if the game is over and the player did not die on spikes.
	 * 
	 * @return
	 */
	public boolean won() {
		return hasWon;
	}

	/**
	 * Returns the current number of moves made in this game.
	 * 
	 * @return
	 */
	public int getMoves() {
		return numMoves;
	}

	/**
	 * Returns the current score (number of pearls disappeared) for this game.
	 * 
	 * @return
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Performs a move along a state sequence in the given direction, updating the
	 * score, the move count, and all affected cells in the grid. The method returns
	 * an array of MoveRecord objects representing the states in original state
	 * sequence before modification, with their <code>movedTo</code> and
	 * <code>disappeared</code> status set to indicate the cell states' new
	 * locations after modification.
	 * 
	 * @param dir direction of the move
	 * @return array of MoveRecord objects describing modified cells
	 */
	public MoveRecord[] move(Direction dir) {

		int startingPearls = countPearls();

		State[] stateOriginal = getStateSequence(dir);

		// creates MoveRecord array with same length as state array
		MoveRecord[] moveRecord = new MoveRecord[stateOriginal.length];

		for (int i = 0; i < stateOriginal.length; i++) {
			moveRecord[i] = new MoveRecord(stateOriginal[i], i);
		}

		// calls on PearlUtil class to move blocks and move player
		util.moveBlocks(stateOriginal, moveRecord);
		util.movePlayer(stateOriginal, moveRecord, dir);

		// retrieves current player index from movePlayer method
		int playerIndex = util.movePlayer(stateOriginal, moveRecord, dir);

		setStateSequence(stateOriginal, dir, playerIndex);

		// update score and move count as needed;
		score += startingPearls - countPearls();
		numMoves++;

		// checks if current location has deadly spikes
		if (State.spikesAreDeadly(grid[getCurrentRow()][getCurrentColumn()].getState(), dir))

		{
			hasWon = false;
			isOver = true;
		}

		// if player is not on spikes and pearls are gone, then updates hasWon to true
		if (countPearls() == 0 && !isOver) {
			hasWon = true;
			isOver = true;
		}

		return moveRecord;
	}

	/**
	 * Finds a valid state sequence in the given direction starting with the
	 * player's current position and ending with a boundary cell as defined by the
	 * method State.isBoundary. The actual cell locations are obtained by following
	 * getNextRow and getNextColumn in the given direction, and the sequence ends
	 * when a boundary state is found. A boundary state is defined by the method
	 * State.isBoundary and is different depending on whether a movable block has
	 * been encountered so far in the state sequence (the player can move through
	 * open gates and portals, but the movable blocks cannot). It can be assumed
	 * that there will eventually be a boundary state (i.e., the grid has no
	 * infinite loops). The first element of the returned array corresponds to the
	 * cell containing the player, and the last element of the array is the boundary
	 * state. This method does not modify the grid or any aspect of the game state.
	 * 
	 * @param dir
	 * @return
	 */
	public State[] getStateSequence(Direction dir) {

		// creates an initial arrayList since the length is unknown
		ArrayList<State> tempState = new ArrayList<>();

		boolean allowPortalJump = true;
		boolean hasMovableObjects = false;
		int tempRow;

		int row = getCurrentRow();
		int column = getCurrentColumn();
		tempState.add(grid[row][column].getState());

		// checks current row/column for movable objects since this parameter is
		// required for the isBoundary method

		if (dir.equals(Direction.UP) || dir.equals(Direction.DOWN)) {
			for (int i = row; i < getRows() - row; i++) {
				if (State.isMovable(grid[i][column].getState())) {
					hasMovableObjects = true;
				}
			}
		}

		if (dir.equals(Direction.LEFT) || dir.equals(Direction.RIGHT)) {
			for (int i = column; i < getColumns() - column; i++) {
				if (State.isMovable(grid[row][i].getState())) {
					hasMovableObjects = true;
				}
			}
		}

		// while the current cell is not a boundary, it will be added to the state array
		// checks if cell is portal before figuring out next row/column
		// allowPortalJump variable ensures that a player does not go back and forth
		// between two portals infinitely

		while (!State.isBoundary(grid[row][column].getState(), hasMovableObjects)) {

			if (grid[row][column].getState() == State.PORTAL && allowPortalJump == true) {
				tempRow = row;
				row = getNextRow(row, column, dir, true);
				column = getNextColumn(tempRow, column, dir, true);

				if (grid[row][column].getState() == State.PORTAL) {
					allowPortalJump = false;
				}
			}

			else {

				tempRow = row;
				row = getNextRow(row, column, dir, false);
				column = getNextColumn(tempRow, column, dir, false);
				allowPortalJump = true;

			}
			tempState.add(grid[row][column].getState());
		}

		// creates an array using the length of the arrayList

		State[] stateSequence = new State[tempState.size()];
		stateSequence = tempState.toArray(stateSequence);
		return stateSequence;

	}

	/**
	 * Sets the given state sequence and updates the player position. This method
	 * effectively retraces the steps for creating a state sequence in the given
	 * direction, starting with the player's current position, and updates the grid
	 * with the new states. The given state sequence can be assumed to be
	 * structurally consistent with the existing grid, e.g., no portal or wall cells
	 * are moved.
	 * 
	 * @param states
	 * @param dir
	 * @param playerIndex
	 */
	public void setStateSequence(State[] states, Direction dir, int playerIndex) {
		int row = getCurrentRow();
		int column = getCurrentColumn();
		int tempRow;
		boolean allowPortalJump = true;

		// makes player "disappear" from starting location
		grid[row][column].setPlayerPresent(false);

		for (int i = 0; i < states.length; i++) {

			// sets player present at the given player index
			if (i == playerIndex) {
				grid[row][column].setPlayerPresent(true);
			}

			// repeats same logic as getStateSequence to get the consecutive rows/columns
			if (grid[row][column].getState() == State.PORTAL && allowPortalJump == true) {

				grid[row][column].setState(states[i]);

				tempRow = row;
				row = getNextRow(row, column, dir, true);
				column = getNextColumn(tempRow, column, dir, true);

				if (grid[row][column].getState() == State.PORTAL) {
					allowPortalJump = false;
				}
			}

			else {

				grid[row][column].setState(states[i]);

				tempRow = row;
				row = getNextRow(row, column, dir, false);
				column = getNextColumn(tempRow, column, dir, false);
				allowPortalJump = true;

			}
		}
	}

	/**
	 * Helper method returns the next row for a state sequence in the given
	 * direction, possibly wrapping around. If the flag doPortalJump is true, then
	 * the next row will be obtained by adding the cell's row offset. (Normally the
	 * caller will set this flag to true when first landing on a portal, but to
	 * false for the second portal of the pair.)//
	 * 
	 * @param row
	 * @param col
	 * @param dir
	 * @param doPortalJump
	 * @return
	 */
	public int getNextRow(int row, int col, Direction dir, boolean doPortalJump) {

		if (doPortalJump) {
			doPortalJump = false;
			return row + grid[row][col].getRowOffset();
		}

		// row will remain the same if direction is left/right
		if (dir.equals(Direction.LEFT) || dir.equals(Direction.RIGHT)) {
			return row;
		}

		// use modular arithmetic to account for up/down movement between rows
		// this formula will account for any "wrapping" around the game
		if (dir.equals(Direction.UP)) {
			row += getRows();
			row = (row - 1) % getRows();
			return row;
		}

		if (dir.equals(Direction.DOWN)) {
			row = (row + 1) % getRows();
			return row;
		}

		return -1;
	}

	/**
	 * Helper method returns the next column for a state sequence in the given
	 * direction, possibly wrapping around. If the flag doPortalJump is true, then
	 * the next column will be obtained by adding the cell's column offset.
	 * (Normally the caller will set this flag to true when first landing on a
	 * portal, but to false for the second portal of the pair.)
	 * 
	 * @param row
	 * @param col
	 * @param dir
	 * @param doPortalJump
	 * @return
	 */
	public int getNextColumn(int row, int col, Direction dir, boolean doPortalJump) {
		if (doPortalJump) {
			doPortalJump = false;
			return col + grid[row][col].getColumnOffset();
		}

		// column will not change if direction is up/down
		if (dir.equals(Direction.UP) || dir.equals(Direction.DOWN)) {
			return col;
		}

		// use modular arithmetic to account for left/right movement between columns
		// this formula will account for any "wrapping" around the game
		if (dir.equals(Direction.LEFT)) {
			col += getColumns();
			col = (col - 1) % getColumns();
			return col;
		}

		if (dir.equals(Direction.RIGHT)) {

			col = (col + 1) % getColumns();
			return col;
		}
		return -1;
	}

	/**
	 * Returns the number of pearls left in the grid.
	 * 
	 * @return
	 */
	public int countPearls() {
		int count = 0;

		// iterates through grid to count total pearls
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j].getState() == State.PEARL) {
					count++;
				}
			}
		}
		return count;
	}
}

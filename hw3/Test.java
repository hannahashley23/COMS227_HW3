package hw3;

import api.State;
import api.StringUtil;
import api.MoveRecord;
import api.Direction;

public class Test {
	
	public static void main(String[] args) {

		PearlUtil util = new PearlUtil();
		String test = "..@.o..@.#";
		State[] states = StringUtil.createFromString(test);
		MoveRecord[] records = new MoveRecord[states.length];
		for (int i = 0; i < states.length; ++i)
		{
			records[i] = new MoveRecord(states[i], i);
			}
		StringUtil.printStateArray(states, 0);
		System.out.println();
		util.movePlayer(states, records, Direction.DOWN);
		StringUtil.printStateArray(states, 8);System.out.println();
		for (int i = 0; i < records.length; ++i)
		{
			System.out.println(i + " " + records[i].toString());
			}	
		
		
	PearlUtil util2 = new PearlUtil();
	String test2 = "..@.o+@+-+.o";
	State[] states2 = StringUtil.createFromString(test2);
	MoveRecord[] records2 = new MoveRecord[states.length];
	
	for(int j = 0; j<states2.length;++j)
	{
		records2[j] = new MoveRecord(states2[j], j);
	}
	
	StringUtil.printStateArray(states2,0);
	System.out.println();
	util2.moveBlocks(states2,records2);
	StringUtil.printStateArray(states2,8);System.out.println();
	
	for(int i = 0;i<records2.length;++i)
	{
		System.out.println(i + " " + records2[i].toString());
	}
}
}

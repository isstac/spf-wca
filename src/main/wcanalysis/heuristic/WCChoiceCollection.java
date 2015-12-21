package wcanalysis.heuristic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import att.grappa.Attribute;
import isstac.structure.cfg.util.DotAttribute;

/**
 * @author Rody Kersten
 */
public class WCChoiceCollection implements DotAttribute, Serializable, Iterable<Integer> {
	private static final long serialVersionUID = 3896815168451849461L;

	private List<Integer> choices = new ArrayList<>();

	@Override
	public Attribute getAttribute() {
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Integer c : choices) {
			sb.append(c.toString()).append('\n');
		}
		return sb.toString();
	}

	public boolean contains(Integer c) {
		return choices.contains(c);
	}

	public boolean addChoice(Integer c) {
		return choices.add(c);
	}

	public int getNumberOfChoices() {
		return choices.size();
	}

	public Collection<Integer> getAllChoices() {
		return choices;
	}

	@Override
	public Iterator<Integer> iterator() {
		return choices.iterator();
	}

	@Override
	public String getLabelString() {
//		StringBuilder choiceBuilder = new StringBuilder();
//		int i = 0;
//		while (i < choices.size()) {
//			choiceBuilder.append("[" + i + "," + ((choices.get(i).getNextChoice() == 1) ? 'T' : 'F') + "]");
//			i++;
//		}
//		return choiceBuilder.toString();
		return "";
	}
}
package ir;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import scope.Scope;
import yal2jvm.SimpleNode;

public class LabelStatement extends Statement {

	public enum LabelType {
		ELSE, LOOP, IF_END, ELSE_END
	}

	private Statement nameRef;
	private LabelType labelType;

	public LabelStatement(Statement ref, LabelType type) {
		super(new SimpleNode(-1), null);
		this.nameRef = ref;
		this.labelType = type;
	}

	@Override
	public List<Supplier<String>> getValue() {
		List<Supplier<String>> list = new ArrayList<Supplier<String>>();

		if (LabelType.LOOP.equals(labelType)) {
			list.add(() -> "goto " + nameRef.getName());
			list.add(() -> nameRef.getName() + "_END:");
		} else if (LabelType.IF_END.equals(labelType)) {
			list.add(() -> nameRef.getName() + "_END:");
		} else if (LabelType.ELSE.equals(labelType)) {
			list.add(() -> "goto "+ nameRef.getName()+"_NEXT");
			list.add(() -> nameRef.getName() + "_END:");
		}  else if (LabelType.ELSE_END.equals(labelType)) {
			list.add(() -> nameRef.getName() + "_NEXT:");
		}
		return list;
	}
}

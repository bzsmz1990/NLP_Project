package ChineseParse;

/**
 * @author mirrorlol
 *
 */
class Production {
	private String[] elements;

	public Production(int i) {
		elements = new String[i];
	}

	public int size() {
		return elements.length;
	}

	public String get(int i) {
		return elements[i];
	}

	public void set(int i, String str) {
		elements[i] = str;
	}

	@Override
	public boolean equals(Object o) {
		if ( o == this) {
			return true;
		}
		if (!(o instanceof Production)) {
			return false;
		}
		Production another = (Production)o;
		if (elements.length != another.size()) {
			return false;
		}
		for (int i = 0; i < elements.length; i++) {
			if (!elements[i].equals(another.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 17;
		for (int i = 0; i < elements.length; i++) {
			result += 31 * elements[i].hashCode();
		}
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < elements.length; i++) {
			builder.append(elements[i]);
			builder.append("\t");
		}
		//builder.append("\n");
		return builder.toString();
	}
}

enum Terminal {
	NN,
	VV,
	JJ,
	NR,
	PU,
	NT,
	NR_SHORT,
	M,
	SB,
	P,
	DT,
	FW,
	_NONE_,
	OD,
	X,
	SP,
	ON,
	CC,
	CD,
	IJ,
	DEC,
	AD,
	DEG,
	VA,
	MSP,
	VC,
	VE,
	CS,
	NN_SHORT,
	DER,
	NT_SHORT,
	AS,
	DEV,
	ETC,
	LB,
	LC,
	PN,
	BA;

	public static boolean containTerminal(String str) {
		if (str == null) {
			return false;
		}
		if (str.equals("")) {
			return false;
		}
		for (Terminal t : Terminal.values()) {
			if (t.toString().equals(str)) {
				return true;
			}
		}
		return false;
	}
}


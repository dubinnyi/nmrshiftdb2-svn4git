package org.openscience.nmrshiftdb.util;

public class QuickCheckRequest {
	private String moltxt;
	private Input[] inputs;
	public String getMoltxt() {
		return moltxt;
	}
	public void setMoltxt(String moltxt) {
		this.moltxt = moltxt;
	}
	
	public Input[] getInputs() {
		return inputs;
	}
	public void setInputs(Input[] inputs) {
		this.inputs = inputs;
	}

	public static class Input{
		private int id;
		private String type;
		private String shifts;
		private String solvent;
		public String getSolvent() {
			return solvent;
		}
		public void setSolvent(String solvent) {
			this.solvent = solvent;
		}
		public String getShifts() {
			return shifts;
		}
		public void setShifts(String shifts) {
			this.shifts = shifts;
		}
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
}

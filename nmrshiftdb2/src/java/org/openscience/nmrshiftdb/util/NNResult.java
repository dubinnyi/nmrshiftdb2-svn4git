package org.openscience.nmrshiftdb.util;

public class NNResult {
	private String smiles;
	private Shift[] shifts;
	private float runtime;
	public float getRuntime() {
		return runtime;
	}
	public void setRuntime(float runtime) {
		this.runtime = runtime;
	}
	public String getSmiles() {
		return smiles;
	}
	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}
	public Shift[] getShifts() {
		return shifts;
	}
	public void setShifts(Shift[] shifts) {
		this.shifts = shifts;
	}


	public static class Shift{
		private int atom_idx;
		public int getAtom_idx() {
			return atom_idx;
		}
		public void setAtom_idx(int atom_idx) {
			this.atom_idx = atom_idx;
		}
		private float pred_mu;
		public float getPred_mu() {
			return pred_mu;
		}
		public void setPred_mu(float pred_mu) {
			this.pred_mu = pred_mu;
		}
		private float pred_std;
		public float getPred_std() {
			return pred_std;
		}
		public void setPred_std(float pred_std) {
			this.pred_std = pred_std;
		}
	}
}
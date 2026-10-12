package org.openscience.nmrshiftdb.util;

public class QuickCheckResponse {
	private Result[] result;
	
	
	public Result[] getResult() {
		return result;
	}

	public void setResult(Result[] result) {
		this.result = result;
	}

	public class Result{
		private int id;
		private String type;
		private Statistics statistics; 
		private Shift[] shifts;

		public Shift[] getShifts() {
			return shifts;
		}

		public void setShifts(Shift[] shifts) {
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

		public Statistics getStatistics() {
			return statistics;
		}

		public void setStatistics(Statistics statistics) {
			this.statistics = statistics;
		}
	}

	public class Statistics{
		private int accept;
		private int warning;
		private int reject;
		private int missing;
		private int total;
		public int getAccept() {
			return accept;
		}
		public void setAccept(int accept) {
			this.accept = accept;
		}
		public int getWarning() {
			return warning;
		}
		public void setWarning(int warning) {
			this.warning = warning;
		}
		public int getReject() {
			return reject;
		}
		public void setReject(int reject) {
			this.reject = reject;
		}
		public int getMissing() {
			return missing;
		}
		public void setMissing(int missing) {
			this.missing = missing;
		}
		public int getTotal() {
			return total;
		}
		public void setTotal(int total) {
			this.total = total;
		}
	}
	
	public class Shift{
		private int atom;
		private double prediction;
		private double real;
		private double diff;
		private String status;
		private String hoseCode;
		private int spheres;
		public int getAtom() {
			return atom;
		}
		public void setAtom(int atom) {
			this.atom = atom;
		}
		public double getPrediction() {
			return prediction;
		}
		public void setPrediction(double prediction) {
			this.prediction = prediction;
		}
		public double getReal() {
			return real;
		}
		public void setReal(double real) {
			this.real = real;
		}
		public double getDiff() {
			return diff;
		}
		public void setDiff(double diff) {
			this.diff = diff;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public String getHoseCode() {
			return hoseCode;
		}
		public void setHoseCode(String hoseCode) {
			this.hoseCode = hoseCode;
		}
		public int getSpheres() {
			return spheres;
		}
		public void setSpheres(int spheres) {
			this.spheres = spheres;
		}
	}
}

package org.openscience.nmrshiftdb.util;

import java.util.Date;

public class JsonExport {
	private int id;
	private String molfile;
	private String[] names;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMolfile() {
		return molfile;
	}
	public void setMolfile(String molfile) {
		this.molfile = molfile;
	}
	public String[] getNames() {
		return names;
	}
	public void setNames(String[] names) {
		this.names = names;
	}
	public Nmr[] getNmr() {
		return nmr;
	}
	public void setNmr(Nmr[] nmr) {
		this.nmr = nmr;
	}
	private Nmr[] nmr;


	public class Nmr{
		private String jcamp;
		private String solvent;
		private String frequency;
		private String[] nucleus;
		private Date date;
		private int dimension;
		private String experiment;
		private Assignment[] assignment;
		private String zip;

		public String getJcamp() {
			return jcamp;
		}
		public void setJcamp(String jcamp) {
			this.jcamp = jcamp;
		}
		public String getSolvent() {
			return solvent;
		}
		public void setSolvent(String solvent) {
			this.solvent = solvent;
		}
		public String getFrequency() {
			return frequency;
		}
		public void setFrequency(String frequency) {
			this.frequency = frequency;
		}
		public String[] getNucleus() {
			return nucleus;
		}
		public void setNucleus(String[] nucleus) {
			this.nucleus = nucleus;
		}
		public Date getDate() {
			return date;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		public int getDimension() {
			return dimension;
		}
		public void setDimension(int dimension) {
			this.dimension = dimension;
		}
		public String getExperiment() {
			return experiment;
		}
		public void setExperiment(String experiment) {
			this.experiment = experiment;
		}
		public Assignment[] getAssignment() {
			return assignment;
		}
		public void setAssignment(Assignment[] assignment) {
			this.assignment = assignment;
		}
		public String getZip() {
			return zip;
		}
		public void setZip(String zip) {
			this.zip = zip;
		}

		public class Assignment{
			private int atom;
			private float delta;
			private String multiplicity;
			public int getAtom() {
				return atom;
			}
			public void setAtom(int atom) {
				this.atom = atom;
			}
			public float getDelta() {
				return delta;
			}
			public void setDelta(float delta) {
				this.delta = delta;
			}
			public String getMultiplicity() {
				return multiplicity;
			}
			public void setMultiplicity(String multiplicity) {
				this.multiplicity = multiplicity;
			}
		}
	}
}
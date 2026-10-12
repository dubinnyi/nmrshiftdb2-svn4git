package org.openscience.nmrshiftdb;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.torque.Torque;
import org.apache.torque.TorqueException;
import org.apache.turbine.util.TurbineConfig;
import org.openscience.nmrshiftdb.om.DBAtom;
import org.openscience.nmrshiftdb.om.DBChemicalName;
import org.openscience.nmrshiftdb.om.DBMolecule;
import org.openscience.nmrshiftdb.om.DBMoleculePeer;
import org.openscience.nmrshiftdb.om.DBSignal;
import org.openscience.nmrshiftdb.om.DBSpectrum;
import org.openscience.nmrshiftdb.util.JsonExport;
import org.openscience.nmrshiftdb.util.exception.NmrshiftdbException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ExportJson {

	public static void main(String[] args) throws TorqueException, SQLException, IOException, NmrshiftdbException {
		  Torque.init("/home/shk3/workspaceluna2/nmrshiftdb2/src/conf/jetspeed/WEB-INF/conf/Torque.properties");
		  TurbineConfig config = new TurbineConfig("/home/shk3/workspaceluna2/nmrshiftdb2/src", "conf/jetspeed/WEB-INF/conf/TurbineResources.properties");
		  config.initialize();
		  Connection dbconn = Torque.getConnection();
		  Statement stmt = dbconn.createStatement();
		  ResultSet rs = stmt.executeQuery("select MOLECULE_ID from MOLECULE where MOLECULE_ID>=60000000");
		  while(rs.next()){
			  DBMolecule mol=DBMoleculePeer.retrieveByPK(rs.getInt(1));
			  FileOutputStream fos=new FileOutputStream("/tmp/nmrshiftdb2_"+mol.getMoleculeId()+".json");
			  JsonExport export=new JsonExport();
			  export.setId(mol.getMoleculeId());
			  export.setMolfile(mol.getStructureFile(1, true));
			  String[] names=new String[mol.getDBChemicalNames().size()];
			  int i=0;
			  for(DBChemicalName name : mol.getDBChemicalNames()){
				  names[i]=name.getName();
				  i++;
			  }
			  export.setNames(names);
			  List<DBSpectrum> spectra = mol.selectSpectra();
			  JsonExport.Nmr[] nmr = new JsonExport.Nmr[spectra.size()];
			  i=0;
			  for(DBSpectrum spectrum : spectra){
				  nmr[i]=export.new Nmr();
				  nmr[i].setDate(spectrum.getDate());
				  nmr[i].setDimension(spectrum.getDBSpectrumType().getDimensionality());
				  if(spectrum.getDBSpectrumType().getDimensionality()==1)
					  nmr[i].setExperiment("1d");
				  else
					  nmr[i].setExperiment(spectrum.getSpectrumTypeName());
				  nmr[i].setFrequency(spectrum.getFrequency());
				  if(spectrum.getJcamp()!=null)
					  nmr[i].setJcamp(new String(spectrum.getJcamp()));
				  String[] nuclei=new String[spectrum.getDBSpectrumType().getDimensionality()];
				  for(int l=0;l<spectrum.getDBSpectrumType().getDimensionality();l++){
					  nuclei[l]=spectrum.getDBSpectrumType().getDBIsotope(l+1).getElementSymbol();
				  }
				  nmr[i].setNucleus(nuclei);
				  nmr[i].setSolvent(spectrum.getSolvent());
				  if(spectrum.getRawData()!=null)
					  nmr[i].setZip(new String(spectrum.getRawData()));
				  List<DBSignal> shifts = spectrum.getDBSignals();
				  List<JsonExport.Nmr.Assignment> assignments = new ArrayList<JsonExport.Nmr.Assignment>();
				  for(DBSignal signal : shifts){
					  for(DBAtom atom : signal.getAtoms()){
						  JsonExport.Nmr.Assignment assignment=nmr[i].new Assignment();
						  assignment.setAtom(atom.getMdlNumberPlus1());
						  assignment.setDelta(signal.getDBShift(1).getValue());
						  assignment.setMultiplicity(signal.getMultiplicity());
						  assignments.add(assignment);
					  }
				  }
				  nmr[i].setAssignment(assignments.toArray(new JsonExport.Nmr.Assignment[0]));
				  i++;
			  }
			  export.setNmr(nmr);
			  ObjectMapper mapper = new ObjectMapper();
			  mapper.writeValue(fos,export);
			  fos.close();
		  }
	}

}

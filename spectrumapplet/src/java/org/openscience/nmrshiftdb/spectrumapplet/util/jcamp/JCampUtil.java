package org.openscience.nmrshiftdb.spectrumapplet.util.jcamp;

import org.jcamp.math.*;

import org.jcamp.parser.*;

import org.jcamp.spectrum.*;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.data.ir.*;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.*;
import org.openscience.nmrshiftdb.spectrumapplet.util.*;

import java.io.*;

import java.util.*;

/**
 * JCAMP utils
 *
 * @version 1.0
 * @author Dr. Guenter Schneider
 */
public class JCampUtil {

    /**
    *  Parses a jcmap file
    *
    * @param  jcamp                    the data to parse
    * @exception  Exception            Description of Exception
    */
    public static AbstractDataContainer parseJcampData(String jcamp)
                                                throws Exception {
        AbstractDataContainer r = null;
        Spectrum jcampSpectrum  = JCAMPReader_Ext.getInstance().createSpectrum(jcamp);
        if (jcampSpectrum instanceof NMRSpectrum) {
            r = processNMR(jcampSpectrum);
        }
        else if (jcampSpectrum instanceof IRSpectrum_Ext) {
            r = processIR(jcampSpectrum);
        }
        else {
            throw new Exception("Spectrum type not supported: " + jcampSpectrum.getClass().getName());
        }
        return r;
    }


    /**
     *  Parses a jcmap NMR file
     *
     * @param  jcamp                    the data to parse
     * @exception  Exception            Description of Exception
     */
    private static RawNmrData processNMR(Spectrum jcampSpectrum)
                                  throws Exception {
        NMRSpectrum nmrspectrum  = (NMRSpectrum) jcampSpectrum;
        ArrayList numberSpectrum = new ArrayList();
        Peak jp                  = null;
        if (nmrspectrum.hasPeakTable()) {
            Peak[] peaks = nmrspectrum.getPeakTable();
            for (int i = 0; i < peaks.length; i++) {
                jp = peaks[i];
                //System.out.println("JCamp-Peak: " + jp);
                NmrPeak p = new NmrPeak();
                p.setChemicalShift((float) jp.getPosition()[0]);
                p.setIntensity((float) jp.getHeight());
                //System.out.println("Parsed Peak: " + p);
                numberSpectrum.add(p);
            }
        }
        else {
            testOutput(nmrspectrum);
            Vector v = new MinHeightPeakPicking(0, 1).calculate(nmrspectrum);
            for (int i = 0; i < v.size(); i++) {
                jp = (Peak) v.get(i);
                //System.out.println("JCamp-MinHeightPeak: " + jp);
                NmrPeak p = new NmrPeak();
                p.setChemicalShift((float) jp.getPosition()[0]);
                p.setIntensity((float) jp.getHeight());
                //System.out.println("Parsed Peak: " + p);
                numberSpectrum.add(p);
            }
        }
        double factor = 1;
        double freq   = nmrspectrum.getFrequency();
        if (nmrspectrum.getXAxisLabel().toUpperCase().indexOf("HZ") > -1) {
            factor = 1 / freq;
        }
        RawNmrData d = new RawNmrData();
        d.setNumberProperty(d.FREQ, freq);
        if (nmrspectrum.getNucleus().toUpperCase().indexOf("13C") > -1) {
            d.setSpectrumProperty(d.SPEC_TYPE, d.SPECTRUM_13C);
        }
        else {
            d.setSpectrumProperty(d.SPEC_TYPE, d.SPECTRUM_1H);
        }
        if (numberSpectrum.size() > 0) {
            d.addPeaks(numberSpectrum, factor);
        }
        d.addCoordinates(nmrspectrum.getXData().toArray(), nmrspectrum.getYData().toArray(), factor);
        return d;
    }


    private static void testOutput(NMRSpectrum spectrum) {
        System.out.println(" ---- XUNITS:            " + spectrum.getXAxisLabel());
        System.out.println(" ---- OBSERVE FREQUENCY: " + spectrum.getFrequency());
        System.out.println(" ---- OBSERVE NUCLEUS:   " + spectrum.getNucleus());
        IOrderedArray1D xdata = spectrum.getXData();
        IArray1D idata        = spectrum.getYData();
        System.out.println("Points: " + xdata.getLength());
        for (int i = 0; i < xdata.getLength(); i++) {
            //System.out.println("x= "+xdata.pointAt(i)+"   y= "+idata.pointAt(i));
        }
    }


    /**
    *  Parses a jcmap IR file
    *
    * @param  jcamp                    the data to parse
    * @exception  Exception            Description of Exception
    */
    private static RawIRdata processIR(Spectrum jcampSpectrum) {
        IRSpectrum_Ext irspectrum = (IRSpectrum_Ext) jcampSpectrum;
        if (!irspectrum.isTransmission()) {
            irspectrum.convertToTransmittance();
        }
        System.out.println("specType: " + irspectrum.getYData().getUnit());
        ArrayList numberSpectrum = new ArrayList();
        Peak jp                  = null;
        if (irspectrum.hasPeakTable()) {
            Peak[] peaks = irspectrum.getPeakTable();
            for (int i = 0; i < peaks.length; i++) {
                jp = peaks[i];
                //System.out.println("JCamp-Peak: " + jp);
                SpectrumPeak p = new SpectrumPeak();
                p.setValueX((float) jp.getPosition()[0]);
                p.setIntensity((float) jp.getHeight());
                //System.out.println("Parsed Peak: " + p);
                numberSpectrum.add(p);
            }
        }
        else {
            testOutputIR(irspectrum);
            Vector v = new MinHeightPeakPicking(0, 1).calculate(irspectrum);
            for (int i = 0; i < v.size(); i++) {
                jp = (Peak) v.get(i);
                SpectrumPeak p = new SpectrumPeak();
                p.setValueX((float) jp.getPosition()[0]);
                p.setIntensity((float) jp.getHeight());
                numberSpectrum.add(p);
            }
        }
        RawIRdata d = new RawIRdata();
        d.addPeaks(numberSpectrum, 1);
        d.setSpectrumProperty(d.SPEC_TYPE, d.SPECTRUM_IR);
        //d.setNumberProperty(d.FREQ, freq);
        double[] y = irspectrum.getYData().toArray();
        d.addCoordinates(irspectrum.getXData().toArray(), y, 1);
        return d;
    }


    private static void testOutputIR(IRSpectrum_Ext spectrum) {
        System.out.println(" ---- XUNITS:            " + spectrum.getXAxisLabel());
        System.out.println(" ---- YUNITS:            " + spectrum.getYAxisLabel());
        IOrderedArray1D xdata = spectrum.getXData();
        IArray1D idata        = spectrum.getYData();
        System.out.println("Points: " + xdata.getLength());
        for (int i = 0; i < xdata.getLength(); i++) {
            //System.out.println("x= " + xdata.pointAt(i) + "   y= " + idata.pointAt(i));
        }
    }
}

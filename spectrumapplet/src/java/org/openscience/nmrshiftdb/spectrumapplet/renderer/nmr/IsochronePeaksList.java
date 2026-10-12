package org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr;

import java.util.ArrayList;
import java.util.List;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.Coupling;
import org.openscience.nmrshiftdb.spectrumapplet.data.nmr.NmrPeak;


public class IsochronePeaksList{

        List peaks;

    public IsochronePeaksList()
    {
         ;
         peaks = new ArrayList();
         return;
    }


    public void addPeak(NmrPeak nmrpeak)
    {
         peaks.add(nmrpeak);
         return;
    }


    public int getPeakCount()
    {
         return (peaks.size());
    }


    public Object[] getLineData(boolean i ,boolean j)
    {
         int k = peaks.size();
         Object[] object = new Object[4];
         int[] lArr = new int[3 * k];
         double[] mArr = new double[3 * k];
         int[] nArr = new int[k];
         object[0]=lArr;
         object[1]=mArr;
         object[2]=nArr;
         object[3]=new double[2];
         ((double[])object[3])[0]=Double.MAX_VALUE;
         ((double[])object[3])[0]=Double.MIN_VALUE;
         int o = 0;
         double p = 0.0;
         double q = 0.0;
         int r = 0;
         for (;r < k;) {
              NmrPeak nmrpeak = (NmrPeak)peaks.get(r);
              if(nmrpeak.getValueX()>((double[])object[3])[1])
                  ((double[])object[3])[1]=nmrpeak.getValueX();
              if(nmrpeak.getValueX()<((double[])object[3])[0])
                  ((double[])object[3])[0]=nmrpeak.getValueX();
              Coupling[] couplingArr = nmrpeak.getCouplings();
              if (!i && couplingArr.length != 1 && !nmrpeak.isSolventPeak())
                   o = nmrpeak.getCouplings()[0].getMultiplicity();

              else
                   o = 0;

              nArr[r] = o;
              lArr[r * 3] = nmrpeak.getX();
              lArr[r * 3 + 1] = lArr[r * 3];
              lArr[r * 3 + 2] = lArr[r * 3];
              if (!j && nmrpeak.getSignedIntensity() < 0.0) {
                   mArr[r * 3] = p;
                   mArr[r * 3 + 1] = p + nmrpeak.getSignedIntensity();
                   mArr[r * 3 + 2] = p;
                   p = mArr[r * 3 + 1];
              }
              else {
                   mArr[r * 3] = q;
                   mArr[r * 3 + 1] = q + nmrpeak.getIntensity();
                   mArr[r * 3 + 2] = q;
                   q = mArr[r * 3 + 1];
              }
              r++;
         }
         return (object);
    }

}
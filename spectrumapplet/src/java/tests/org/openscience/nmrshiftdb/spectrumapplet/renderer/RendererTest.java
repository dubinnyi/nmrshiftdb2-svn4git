package tests.org.openscience.nmrshiftdb.spectrumapplet.renderer;

import org.openscience.nmrshiftdb.spectrumapplet.data.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.*;
import org.openscience.nmrshiftdb.spectrumapplet.renderer.nmr.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class RendererTest {
    public final SpectrumNavigation sn;
    private JFrame frame;

    public RendererTest(String predictSpec, String prediction, String expSpectrum, String specType, String solvent, String freq, boolean showNavigation, boolean isgraphic) {
                          this(predictSpec,prediction,expSpectrum, specType, solvent, freq, showNavigation, isgraphic,350,200);
                        }

    public RendererTest(String predictSpec, String prediction, String expSpectrum, String specType,
                        String solvent, String freq, boolean showNavigation, boolean isgraphic, int widht, int height) {
        SpectrumModel model = new SpectrumModel();
        model.setShowNavigationView(showNavigation);
        frame = new JFrame("SpectraRendererTest");
        frame.getContentPane().setLayout(new BorderLayout());
        sn = new SpectrumNavigation(specType, freq, solvent, predictSpec, prediction, expSpectrum, model, null, isgraphic,false, Color.LIGHT_GRAY);
        activateKeyListener(frame, sn.getDetailSpectrums()[0]);
        frame.getContentPane().add(sn, BorderLayout.CENTER);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(widht, height);
        frame.setVisible(true);
    }

    public void setSize(int width, int hight) {
        frame.setSize(width, hight);
        frame.validate();
    }

    public RendererTest(AbstractDataContainer p) {
        frame = new JFrame("SpectraRendererTest");
        frame.getContentPane().setLayout(new BorderLayout());
        sn = new SpectrumNavigation(p, null, null,false, Color.LIGHT_GRAY);
        activateKeyListener(frame, sn.getDetailSpectrums()[0]);
        frame.getContentPane().add(sn, BorderLayout.CENTER);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setSize(350, 250);
        frame.setVisible(true);
    }

    public void updateView() {
        Dimension d = frame.getSize();
        setSize(d.width + 1, d.height);
    }


    public void setParameter(int paramNo, String value) {
        sn.setAppletParameter(paramNo, value);
    }


    public void activateKeyListener(JFrame f, final NmrSpecRenderer graph) {
        //System.out.println("Keylistener for: "+graph);
        f.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    graph.keyPressed(e);
                }
            });
    }


    public JFrame getFrame() {
        return frame;
    }
}

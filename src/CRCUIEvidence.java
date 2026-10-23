import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
/** Runs the real Swing panel offscreen; no desktop session is required. */
public class CRCUIEvidence {
    static void layout(Container c) { c.doLayout(); for(Component child:c.getComponents()) if(child instanceof Container) layout((Container)child); }
    static void save(CRCApp app,String path) throws Exception {
        app.setSize(1050,680); layout(app);
        BufferedImage img=new BufferedImage(1050,680,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=img.createGraphics(); app.printAll(g); g.dispose(); ImageIO.write(img,"png",new File(path));
    }
    public static void main(String[] args) throws Exception {
        String directory=args.length==0?"evidence":args[0]; new File(directory).mkdirs();
        SwingUtilities.invokeAndWait(()-> { try {
            CRCApp app=new CRCApp(); app.generate(); app.verify();
            if(!app.status.getText().startsWith("NO ERROR DETECTED")) throw new AssertionError();
            save(app,directory+"/01_clean_frame.png");
            app.generate(); app.positions.setText("1"); app.flip(); app.verify();
            if(!app.status.getText().startsWith("ERROR DETECTED")) throw new AssertionError();
            save(app,directory+"/02_detected_error.png");
            app.generate(); app.positions.setText("1,4,5"); app.flip(); app.verify();
            if(!app.status.getText().startsWith("NO ERROR DETECTED")) throw new AssertionError();
            save(app,directory+"/03_undetected_error.png");
            app.gen.setText("1011"); boolean guarded=false;
            try { app.verify(); } catch(IllegalArgumentException e) { guarded=true; }
            if(!guarded) throw new AssertionError("generator guard");
            app.reset(); if(app.activeGenerator!=null || !app.log.getText().isEmpty()) throw new AssertionError("reset");
            System.out.println("PASS: Swing generate, flip, verify, generator guard and reset; three offscreen captures.");
        } catch(Exception e) {throw new RuntimeException(e);} });
    }
}

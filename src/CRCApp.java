import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Sender, channel and receiver demonstration with an exportable XOR trace. */
public class CRCApp extends JPanel {
    final JTextField data = new JTextField("1101011011");
    final JTextField gen = new JTextField("10011");
    final JTextField sent = new JTextField();
    final JTextField received = new JTextField();
    final JTextField positions = new JTextField("1");
    final JTextArea log = new JTextArea();
    final JLabel status = new JLabel("Enter data and generator, then generate a frame.");
    String activeGenerator;
    public CRCApp() {
        super(new BorderLayout());
        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(new EmptyBorder(18,18,18,18));
        JPanel fields = new JPanel(new GridLayout(0,2,10,8));
        fields.add(new JLabel("Sender data (binary, up to 256 bits)")); fields.add(data);
        fields.add(new JLabel("Generator (full polynomial bits)")); fields.add(gen);
        fields.add(new JLabel("Transmitted frame")); fields.add(sent); sent.setEditable(false);
        fields.add(new JLabel("Received frame (editable)")); fields.add(received);
        fields.add(new JLabel("Flip positions (1 = leftmost bit)")); fields.add(positions);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton(buttons,"Generate frame",this::generate);
        addButton(buttons,"Flip bits",this::flip);
        addButton(buttons,"Verify received",this::verify);
        addButton(buttons,"Export log",this::exportLog);
        addButton(buttons,"Reset",this::reset);
        JPanel top = new JPanel(new BorderLayout(0,10)); top.add(fields); top.add(buttons,BorderLayout.SOUTH);
        root.add(top,BorderLayout.NORTH);
        log.setEditable(false); log.setFont(new Font(Font.MONOSPACED,Font.PLAIN,13));
        root.add(new JScrollPane(log)); root.add(status,BorderLayout.SOUTH);
        add(root); setPreferredSize(new Dimension(1050,680));
    }
    void addButton(JPanel panel, String title, Runnable action) {
        JButton b = new JButton(title); panel.add(b);
        b.addActionListener(e -> { try { action.run(); }
            catch (IllegalArgumentException ex) { status.setText(ex.getMessage());
                JOptionPane.showMessageDialog(this,ex.getMessage(),"Input error",JOptionPane.WARNING_MESSAGE); }
        });
    }
    void generate() {
        String d=data.getText().trim(), g=gen.getText().trim();
        if(d.length()>256) throw new IllegalArgumentException("Use at most 256 data bits for a readable trace.");
        StringBuilder trace=new StringBuilder(); String frame=CRC.encode(d,g,trace);
        activeGenerator=g; sent.setText(frame); received.setText(frame);
        log.setText("CRC ERROR DETECTION\nData: "+d+"\nGenerator: "+g+
            "\nAppended data: "+d+"0".repeat(g.length()-1)+"\n"+trace+
            "CRC: "+frame.substring(d.length())+"\nTransmitted: "+frame+"\n");
        status.setText("Frame generated. Flip bits or verify directly."); log.setCaretPosition(0);
    }
    void requireFrame() {
        if(activeGenerator==null) throw new IllegalArgumentException("Generate a frame first.");
        if(!gen.getText().trim().equals(activeGenerator))
            throw new IllegalArgumentException("Generator changed. Generate a new frame first.");
        CRC.bits(received.getText().trim(),"Received frame");
        if(received.getText().trim().length()!=sent.getText().length())
            throw new IllegalArgumentException("Received and transmitted frame lengths must match.");
    }
    void flip() {
        requireFrame(); received.setText(CRC.flip(received.getText().trim(),positions.getText()));
        log.append("Flipped bit positions: "+positions.getText()+"\n");
        status.setText("Bits changed. Verify the received frame.");
    }
    void verify() {
        requireFrame(); String frame=received.getText().trim(); StringBuilder trace=new StringBuilder();
        String rem=CRC.remainder(frame,activeGenerator,trace);
        String result=rem.contains("1")?"ERROR DETECTED":"NO ERROR DETECTED";
        log.append("\nReceived: "+frame+"\nReceiver division\n"+trace+"Syndrome: "+rem+"\n"+result+"\n");
        if(!rem.contains("1")) log.append("A zero syndrome does not prove that the frame is unchanged.\n");
        status.setText(result+" | Syndrome: "+rem); log.setCaretPosition(log.getDocument().getLength());
    }
    void exportLog() {
        if(log.getText().isEmpty()) throw new IllegalArgumentException("Generate a frame before exporting.");
        JFileChooser chooser=new JFileChooser(); chooser.setSelectedFile(new java.io.File("crc_log.txt"));
        if(chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION) {
            java.io.File file=chooser.getSelectedFile();
            if(file.exists() && JOptionPane.showConfirmDialog(this,"Replace existing file?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            try { Files.writeString(file.toPath(),log.getText(),StandardCharsets.UTF_8); status.setText("Log saved."); }
            catch(java.io.IOException ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"Save failed",JOptionPane.ERROR_MESSAGE); }
        }
    }
    void reset() {
        data.setText("1101011011"); gen.setText("10011"); sent.setText(""); received.setText("");
        positions.setText("1"); log.setText(""); activeGenerator=null; status.setText("Ready to generate a frame.");
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()-> {
            JFrame window=new JFrame("CRC Error Detection | Computer Networks Mini Project");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setContentPane(new CRCApp()); window.pack();
            window.setLocationRelativeTo(null); window.setVisible(true);
        });
    }
}

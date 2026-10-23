import java.math.BigInteger;
public class CRCTest {
    static int checks;
    static void check(boolean condition, String name) {
        checks++; if(!condition) throw new AssertionError(name);
    }
    // Independent integer-polynomial division oracle.
    static String oracle(String d,String g) {
        BigInteger x=new BigInteger(d,2).shiftLeft(g.length()-1), p=new BigInteger(g,2);
        while(x.bitLength()>=p.bitLength()) x=x.xor(p.shiftLeft(x.bitLength()-p.bitLength()));
        String r=x.toString(2); return "0".repeat(g.length()-1-r.length())+r;
    }
    static void rejects(Runnable action) {
        boolean rejected=false; try {action.run();} catch(IllegalArgumentException ex) {rejected=true;}
        check(rejected,"invalid input rejected");
    }
    public static void main(String[] args) {
        String g="10011", frame=CRC.encode("1101011011",g,null);
        check(frame.equals("11010110111110"),"known vector");
        check(CRC.passes(frame,g),"clean frame");
        for(int i=1;i<=frame.length();i++) check(!CRC.passes(CRC.flip(frame,""+i),g),"single bit "+i);
        String undetected=CRC.flip(frame,"1,4,5");
        check(!undetected.equals(frame) && CRC.passes(undetected,g),"generator multiple escapes detection");
        check(CRC.encode("0000",g,null).equals("00000000"),"leading zeros");
        for(String p:new String[]{"11","101","1011","10011","11001"})
            for(int n=1;n<=8;n++) for(int d=0;d<(1<<n);d++) {
                String data=String.format("%"+n+"s",Integer.toBinaryString(d)).replace(' ','0');
                String coded=CRC.encode(data,p,null);
                check(coded.equals(data+oracle(data,p)),"independent oracle");
                check(CRC.passes(coded,p),"encoded frame valid");
            }
        rejects(()->CRC.encode("102","10011",null));
        rejects(()->CRC.encode("","10011",null));
        rejects(()->CRC.encode("101","0101",null));
        rejects(()->CRC.encode("101","10010",null));
        rejects(()->CRC.encode("101","1",null));
        rejects(()->CRC.remainder("1","10011",null));
        rejects(()->CRC.flip(frame,"0")); rejects(()->CRC.flip(frame,"15"));
        rejects(()->CRC.flip(frame,"1,1")); rejects(()->CRC.flip(frame,"1,"));
        System.out.println("PASS: "+checks+" checks");
        System.out.println("Data=1101011011 Generator=10011 CRC=1110 Frame="+frame);
        System.out.println("Clean syndrome="+CRC.remainder(frame,g,null));
        System.out.println("Bit 1 flipped="+CRC.flip(frame,"1")+" Syndrome="+CRC.remainder(CRC.flip(frame,"1"),g,null));
        System.out.println("Bits 1,4,5 flipped="+undetected+" Syndrome="+CRC.remainder(undetected,g,null));
    }
}

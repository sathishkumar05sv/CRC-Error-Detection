/** Educational systematic CRC: MSB first, zero initial state, no final XOR. */
public final class CRC {
    private CRC() {}
    public static void bits(String value, String label) {
        if (value == null || !value.matches("[01]+"))
            throw new IllegalArgumentException(label + " must contain only 0 and 1.");
    }
    public static void generator(String g) {
        bits(g, "Generator");
        if (g.length() < 2 || g.length() > 33 || g.charAt(0) != '1'
                || g.charAt(g.length()-1) != '1')
            throw new IllegalArgumentException("Generator: 2 to 33 bits, starting and ending with 1.");
    }
    public static String remainder(String input, String g, StringBuilder trace) {
        bits(input, "Input"); generator(g);
        if (input.length() < g.length()-1)
            throw new IllegalArgumentException("Frame is shorter than the CRC width.");
        char[] work = input.toCharArray();
        for (int i=0; i<=work.length-g.length(); i++) {
            if (work[i]=='1') {
                for (int j=0; j<g.length(); j++)
                    work[i+j] = work[i+j] == g.charAt(j) ? '0' : '1';
                if (trace != null) trace.append("XOR at bit ").append(i+1)
                    .append(": ").append(work).append('\n');
            }
        }
        return new String(work, work.length-g.length()+1, g.length()-1);
    }
    public static String encode(String data, String g, StringBuilder trace) {
        bits(data, "Data"); generator(g);
        return data + remainder(data + "0".repeat(g.length()-1), g, trace);
    }
    public static boolean passes(String frame, String g) {
        return !remainder(frame, g, null).contains("1");
    }
    /** One-based bit positions from the left; duplicates are rejected. */
    public static String flip(String frame, String positions) {
        bits(frame, "Frame");
        if (positions.trim().isEmpty()) throw new IllegalArgumentException("Enter bit positions, e.g. 1,4.");
        char[] out = frame.toCharArray();
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        for (String token : positions.split(",", -1)) {
            int p;
            try { p=Integer.parseInt(token.trim()); }
            catch (NumberFormatException e) { throw new IllegalArgumentException("Use comma-separated whole numbers."); }
            if (p<1 || p>out.length || !seen.add(p))
                throw new IllegalArgumentException("Positions must be unique and between 1 and " + out.length + ".");
            out[p-1] = out[p-1]=='0' ? '1' : '0';
        }
        return new String(out);
    }
}

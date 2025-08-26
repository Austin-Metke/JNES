package cpu6502;

public class RomPage implements MemoryPage {
    private final byte[] data;
    private final int baseAddress;

    public RomPage(byte[] data, int baseAddress) {
        this.data = data;
        this.baseAddress = baseAddress;
    }

    @Override
    public int read(int addr) {
        int offset = addr - baseAddress;
        // Mirror into ROM size so a 16KB PRG can appear at $8000-$BFFF and $C000-$FFFF
        if (data.length > 0) {
            // Normalize offset to [0, data.length)
            offset %= data.length;
            if (offset < 0) offset += data.length;
            return data[offset] & 0xFF;
        }
        return 0xFF;
    }


    @Override
    public void write(int addr, int value) {
        // ROM: no writes allowed
    }
}

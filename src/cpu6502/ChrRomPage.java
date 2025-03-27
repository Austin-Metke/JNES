package cpu6502;

public class ChrRomPage implements MemoryPage {
    private final byte[] data;

    public ChrRomPage(byte[] data) {
        this.data = data;
    }

    @Override
    public int read(int addr) {
        return data[addr & 0x3FF] & 0xFF; // 1KB page
    }

    @Override
    public void write(int addr, int value) {
        // Do nothing: CHR ROM is read-only
    }
}

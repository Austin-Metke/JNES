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
        if (offset < 0 || offset >= data.length) {
            return 0xFF; // or 0x00, or log an error
        }
        return data[offset] & 0xFF;
    }


    @Override
    public void write(int addr, int value) {
        // ROM: no writes allowed
    }
}

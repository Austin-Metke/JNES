package cpu6502;

import java.util.Arrays;

public class RamPage implements MemoryPage {
    private final byte[] data;

    public RamPage(int size) {
        data = new byte[size];
        // Optional: clear it explicitly
        Arrays.fill(data, (byte) 0x00);
    }

    @Override
    public int read(int addr) {
        return data[addr & (data.length - 1)] & 0xFF;
    }

    @Override
    public void write(int addr, int value) {
        data[addr & (data.length - 1)] = (byte) (value & 0xFF);
    }
}

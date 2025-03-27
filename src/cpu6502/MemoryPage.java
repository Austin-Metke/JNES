package cpu6502;

public interface MemoryPage {
    int read(int addr);
    void write(int addr, int value);
}

package cpu6502;

import java.io.FileOutputStream;
import java.io.IOException;

public class Memory {
    private final int[] ram = new int[65536];
    private static final int PAGE_SIZE = 0x1000;
    private static final int NUM_PAGES = 0x10000/PAGE_SIZE;

    private MemoryPage[] pageTable = new MemoryPage[NUM_PAGES];

    public void mapPage(int pageIndex, MemoryPage page) {
        pageTable[pageIndex] = page;
    }

    public void mapRange(int startAddr, int endAddr, MemoryPage page) {
        for(int addr = startAddr; addr <= endAddr; addr+= PAGE_SIZE) {
            pageTable[addr/PAGE_SIZE] = page;
        }
    }

    public int read(int addr) {
        int pageIndex = addr / PAGE_SIZE;
        MemoryPage page = pageTable[pageIndex];
        return (page != null) ? page.read(addr) : 0xFF; // <-- pass full addr
    }




    public void write(int addr, int value) {
        MemoryPage page = pageTable[addr / PAGE_SIZE];
        if (page != null) {
            page.write(addr & (PAGE_SIZE - 1), value);
        }
    }
    public int readWord(int addr) {
        int lo = read(addr)&0xFF;
        int hi = read(addr + 1)&0xFF;
        return (hi << 8) | lo;
    }

    public void dump() {
        dump(0x0000, 0xFFFF);
    }

    public void dumpToBinaryFile(int start, int end, String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            for (int addr = start; addr <= end; addr++) {
                fos.write(read(addr) & 0xFF);
            }
            System.out.println("cpu6502.Memory dumped to " + filename);
        } catch (IOException e) {
            System.err.println("Failed to dump memory: " + e.getMessage());
        }
    }

    public void dumpToBinaryFile(String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            for (int addr = 0x0000; addr <= 0xFFFF; addr++) {
                fos.write(read(addr) & 0xFF);
            }
            System.out.println("cpu6502.Memory dumped to " + filename);
        } catch (IOException e) {
            System.err.println("Failed to dump memory: " + e.getMessage());
        }
    }


    public void dump(int start, int end) {
        for (int addr = start; addr <= end; addr++) {
            System.out.printf("[$%04X] = %02X\n", addr, read(addr));
        }
    }

    public String getPageType(int pageIndex) {
        MemoryPage page = pageTable[pageIndex];
        return (page == null) ? "null" : page.getClass().getSimpleName();
    }


}

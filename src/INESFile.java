import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class INESFile {
    public final int prgSize;
    public final int chrSize;
    public final byte[] prgRom;
    public final byte[] chrRom;
    public final int mapper;
    public final boolean hasTrainer;

    public INESFile(String filePath) throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(filePath));

        // Check NES file magic number
        if (data[0] != 'N' || data[1] != 'E' || data[2] != 'S' || data[3] != 0x1A) {
            throw new IOException("Invalid iNES file (missing NES header)");
        }

        int prgRomUnits = data[4]; // in 16KB
        int chrRomUnits = data[5]; // in 8KB

        this.prgSize = prgRomUnits * 16 * 1024;
        this.chrSize = chrRomUnits * 8 * 1024;

        int flags6 = data[6] & 0xFF;
        int flags7 = data[7] & 0xFF;

        this.mapper = ((flags7 & 0xF0) << 4) | (flags6 >> 4);
        this.hasTrainer = (flags6 & 0x04) != 0;

        int prgStart = 16 + (hasTrainer ? 512 : 0);

        this.prgRom = Arrays.copyOfRange(data, prgStart, prgStart + prgSize);
        if (chrSize > 0) {
            this.chrRom = Arrays.copyOfRange(data, prgStart + prgSize, prgStart + prgSize + chrSize);
        } else {
            this.chrRom = null; // Many ROMs use CHR RAM instead
        }
    }
}

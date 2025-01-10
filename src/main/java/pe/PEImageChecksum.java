package pe;

import java.util.zip.Checksum;

class PEImageChecksum implements Checksum {
    private static final long MAX_UNSIGNED_INT = 4294967296L;

    private long checksum;

    private long position;

    private final long checksumOffset;

    private boolean checksumOffsetSkipped;

    public PEImageChecksum(long l) {
        this.checksumOffset = l;
    }

    public void update(int n) {
        throw new UnsupportedOperationException("Checksum can only be updated with buffers");
    }

    public void update(byte[] arrby, int n1, int n2) {
        long l = this.checksum;
        for (int i = n1; i < n1 + n2; i += 4) {
            if (!this.checksumOffsetSkipped && this.position + i == this.checksumOffset) {
                this.checksumOffsetSkipped = true;
            } else {
                long l1 = ((arrby[i] & 0xFF) + ((arrby[i + 1] & 0xFF) << 8) + ((arrby[i + 2] & 0xFF) << 16)) + ((arrby[i + 3] & 0xFFL) << 24);
                l += l1;
                if (l > 4294967296L)
                    l = (l & 0xFFFFFFFFL) + (l >> 32);
            }
        }
        this.checksum = l;
        this.position += (n2 - n1);
    }

    public long getValue() {
        long l = this.checksum;
        l = (l >> 16) + (l & 0xFFFFL);
        l = (l >> 16) + l;
        return (l & 0xFFFFL) + this.position;
    }

    public void reset() {
        this.checksum = 0L;
        this.position = 0L;
        this.checksumOffsetSkipped = false;
    }
}

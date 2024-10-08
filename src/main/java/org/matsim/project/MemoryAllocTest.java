package org.matsim.project;

public class MemoryAllocTest {
    public static void main(String[] args) {
        final long GIGABYTE = 1024L * 1024L * 1024L;
        final int CHUNK_SIZE = 1024 * 1024 * 1024; // 1GB chunks
        long size = 200 * GIGABYTE;
        int chunks = (int) (size / CHUNK_SIZE);
        byte[][] bigArray = new byte[chunks][];

        try {
            System.out.println("Attempting to allocate " + size + " bytes of memory in chunks...");
            for (int i = 0; i < chunks; i++) {
                bigArray[i] = new byte[CHUNK_SIZE];
                System.out.println("Allocated " + i + " gigabytes.");
            }
            System.out.println("Successfully allocated memory.");

            // Adding a 10 second pause
            System.out.println("Pausing for 10 seconds...");
            Thread.sleep(10000);  // 10000 milliseconds = 10 seconds
            System.out.println("Resume operation.");

        } catch (OutOfMemoryError e) {
            System.out.println("Memory allocation failed: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted: " + e.getMessage());
        }
    }
}




















